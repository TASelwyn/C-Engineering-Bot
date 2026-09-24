package wtf.devil.cengbot.utils.watchers;

import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildLeaveEvent;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wtf.devil.cengbot.DevilsBot;

public class BotLifecycleListener extends ListenerAdapter {

    private static final Logger logger = LogManager.getLogger(DevilsBot.class);

    @Override
    public void onReady(ReadyEvent event) {
        logger.info("Logged in as " + event.getJDA().getSelfUser().getName() + ", ready!");
    }

    @Override
    public void onGuildJoin(GuildJoinEvent event) {
        logger.info("Joined server " + event.getGuild().getName());
    }

    @Override
    public void onGuildLeave(GuildLeaveEvent event) {
        logger.info("Left server " + event.getGuild().getName());
    }
}
