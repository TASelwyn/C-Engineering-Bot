package wtf.devil.cengbot.commands.general;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.api.utils.SplitUtil;
import tech.selwyn.wooclapper.model.Wooclap;
import wtf.devil.cengbot.DevilsBot;

import java.util.List;
import java.util.Optional;

/*
 * Posts the answers to every question in a Wooclap event the bot is in, for everyone in the channel to read.
 */
public class WooclapQuestionsCommand {

    public static final String SLASH_NAME = "questions";

    public static final SlashCommandData SLASH_COMMAND = Commands.slash(SLASH_NAME, "Post the answers to a Wooclap event's questions to this channel.")
            .addOption(OptionType.STRING, "code", "The Wooclap event code", true);

    private static final int DISCORD_LIMIT = 2000;

    public static void onSlashCommand(SlashCommandInteractionEvent event) {
        OptionMapping codeOption = event.getOption("code");
        if (codeOption == null) {
            event.reply("Invalid usage. `/questions <code>`").setEphemeral(true).queue();
            return;
        }

        String eventCode = codeOption.getAsString().trim().toUpperCase();
        Optional<Wooclap> wooclap = DevilsBot.getWooclapper().getWooclapByCode(eventCode);
        if (wooclap.isEmpty()) {
            event.reply("The bot isn't in Wooclap event `" + eventCode + "`. Use `/join " + eventCode + "` first.").setEphemeral(true).queue();
            return;
        }

        String answers = wooclap.get().getAllAnswersText();
        if (answers == null || answers.isBlank()) {
            event.reply("No questions in `" + eventCode + "` yet.").queue();
            return;
        }

        List<String> chunks = SplitUtil.split(answers, DISCORD_LIMIT, SplitUtil.Strategy.NEWLINE, SplitUtil.Strategy.ANYWHERE);
        event.reply(chunks.getFirst()).setAllowedMentions(List.of()).queue();
        for (String chunk : chunks.subList(1, chunks.size())) {
            event.getHook().sendMessage(chunk).setAllowedMentions(List.of()).queue();
        }
    }
}
