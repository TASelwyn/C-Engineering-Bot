package wtf.devil.cengbot.utils.modules;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;
import org.apache.logging.log4j.core.config.Property;
import wtf.devil.cengbot.DevilsBot;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static wtf.devil.cengbot.Constants.wooclapLogChannelId;

/**
 * Forwards everything Wooclapper logs into the Wooclap log channel.
 * Lines are buffered and flushed periodically so Discord rate limits aren't hit. Each flush edits the
 * bot's last log message, until someone else posts in the channel or it fills up, then a new one is started.
 */
public final class WooclapLogRouter extends AbstractAppender {

    private static final Logger logger = LogManager.getLogger(WooclapLogRouter.class);

    private static final String WOOCLAPPER_LOGGER = "tech.selwyn.wooclapper";
    private static final int DISCORD_LIMIT = 2000;
    private static final long FLUSH_INTERVAL_SECONDS = 3;
    private static final Pattern TOKEN_PATTERN = Pattern.compile("(?i)(auth ?token:?\\s*|bearer\\s+)\\S+");

    private static final WooclapLogRouter INSTANCE = new WooclapLogRouter();

    private final Set<String> knownTokens = ConcurrentHashMap.newKeySet();
    private final StringBuilder pending = new StringBuilder();

    // Only touched from the flusher thread
    private Message currentMessage;
    private String currentContent = "";
    // Set when someone else posts in the log channel, so the next flush starts a fresh message below theirs
    private volatile boolean newMessageNeeded = true;

    private WooclapLogRouter() {
        super("WooclapDiscord", null, null, true, Property.EMPTY_ARRAY);
    }

    // Attaches the router to Wooclapper's loggers. Call before creating the Wooclapper.
    public static void install() {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();

        INSTANCE.start();
        config.addAppender(INSTANCE);

        LoggerConfig loggerConfig = LoggerConfig.newBuilder()
                .withLoggerName(WOOCLAPPER_LOGGER)
                .withLevel(Level.INFO)
                .withAdditivity(true)
                .withConfig(config)
                .build();
        loggerConfig.addAppender(INSTANCE, Level.INFO, null);
        config.addLogger(WOOCLAPPER_LOGGER, loggerConfig);
        context.updateLoggers();

        ScheduledExecutorService flusher = Executors.newSingleThreadScheduledExecutor(runnable -> {
            Thread thread = new Thread(runnable, "wooclap-log-flusher");
            thread.setDaemon(true);
            return thread;
        });
        flusher.scheduleAtFixedRate(INSTANCE::flush, FLUSH_INTERVAL_SECONDS, FLUSH_INTERVAL_SECONDS, TimeUnit.SECONDS);
    }

    public static ListenerAdapter channelActivityListener() {
        return new ListenerAdapter() {
            @Override
            public void onMessageReceived(MessageReceivedEvent event) {
                if (event.getChannel().getIdLong() == wooclapLogChannelId && !event.getAuthor().equals(event.getJDA().getSelfUser())) {
                    INSTANCE.newMessageNeeded = true;
                }
            }
        };
    }

    // Called after the log channel is cleared, so the next flush posts a new message instead of editing a deleted one
    public static void startNewMessage() {
        INSTANCE.newMessageNeeded = true;
    }

    // Tokens are scrubbed from anything forwarded to Discord
    public static void registerToken(String authToken) {
        INSTANCE.knownTokens.add(authToken);
    }

    // Queues text for the log channel alongside Wooclapper's own output
    public static void post(String text) {
        INSTANCE.queue(INSTANCE.scrub(text));
    }

    @Override
    public void append(LogEvent event) {
        queue(format(event));
    }

    private void queue(String text) {
        synchronized (pending) {
            pending.append(text);
            if (!text.endsWith("\n")) {
                pending.append('\n');
            }
        }
    }

    private String format(LogEvent event) {
        String message = event.getMessage().getFormattedMessage();
        if (event.getThrown() != null) {
            message += " (" + event.getThrown() + ")";
        }
        if (event.getLevel().isMoreSpecificThan(Level.WARN)) {
            message = "[" + event.getLevel() + "] " + message;
        }
        return scrub(message);
    }

    private String scrub(String message) {
        for (String token : knownTokens) {
            message = message.replace(token, "[redacted]");
        }
        return TOKEN_PATTERN.matcher(message).replaceAll("$1[redacted]").replace("```", "'''");
    }

    private void flush() {
        String text;
        synchronized (pending) {
            if (pending.isEmpty()) {
                return;
            }
            text = pending.toString();
            pending.setLength(0);
        }

        JDA jda = DevilsBot.getJda();
        MessageChannel channel = jda == null ? null : jda.getChannelById(MessageChannel.class, wooclapLogChannelId);
        if (channel == null) {
            logger.warn("Wooclap log channel {} is unavailable, dropping {} chars of Wooclapper output", wooclapLogChannelId, text.length());
            return;
        }

        try {
            send(channel, text);
        } catch (RuntimeException e) {
            logger.warn("Failed to forward Wooclapper output to channel {}", wooclapLogChannelId, e);
        }
    }

    private void send(MessageChannel channel, String text) {
        boolean canEdit = currentMessage != null && !newMessageNeeded;
        List<String> chunks = chunk(canEdit ? currentContent + text : text);

        int next = 0;
        if (canEdit) {
            try {
                currentMessage = currentMessage.editMessage(codeBlock(chunks.getFirst())).complete();
                currentContent = chunks.getFirst();
                next = 1;
            } catch (ErrorResponseException e) {
                // Message was deleted, so post everything fresh instead
                chunks = chunk(text);
            }
        }

        for (; next < chunks.size(); next++) {
            currentMessage = channel.sendMessage(codeBlock(chunks.get(next))).complete();
            currentContent = chunks.get(next);
            newMessageNeeded = false;
        }
    }

    private static String codeBlock(String content) {
        return "```\n" + content + "```";
    }

    // Splits on line boundaries so each message fits in Discord's limit with the code block fence
    private static List<String> chunk(String text) {
        int max = DISCORD_LIMIT - 10;
        List<String> chunks = new ArrayList<>();
        StringBuilder current = new StringBuilder();

        Matcher lines = Pattern.compile("[^\n]*\n").matcher(text);
        while (lines.find()) {
            String line = lines.group();
            if (line.length() > max) {
                line = line.substring(0, max - 4) + "...\n";
            }
            if (current.length() + line.length() > max) {
                chunks.add(current.toString());
                current.setLength(0);
            }
            current.append(line);
        }
        if (!current.isEmpty()) {
            chunks.add(current.toString());
        }
        return chunks;
    }
}
