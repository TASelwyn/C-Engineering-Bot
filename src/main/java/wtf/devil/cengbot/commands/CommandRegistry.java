package wtf.devil.cengbot.commands;

import wtf.devil.cengbot.commands.calculators.ParallelCommand;
import wtf.devil.cengbot.commands.dev.CheatCommand;
import wtf.devil.cengbot.commands.dev.TestCommand;
import wtf.devil.cengbot.commands.dev.WooclapClearCommand;
import wtf.devil.cengbot.commands.dev.WooclapDropCommand;
import wtf.devil.cengbot.commands.dev.WooclapPullCommand;
import wtf.devil.cengbot.commands.economy.*;
import wtf.devil.cengbot.commands.general.HelpCommand;
import wtf.devil.cengbot.commands.general.UserInfoCommand;
import wtf.devil.cengbot.commands.general.WooclapCommand;
import wtf.devil.cengbot.utils.objects.CommandDefinition;
import wtf.devil.cengbot.utils.objects.commandTypes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static wtf.devil.cengbot.utils.objects.commandTypes.*;

/**
 * Single source of truth for every chat command: its aliases, module, help text and
 * handler all live together instead of being spread across Constants/Parsers/CommandManager.
 */
public final class CommandRegistry {

    private static final List<CommandDefinition> COMMANDS = List.of(
            new CommandDefinition("help", List.of(), CORE,
                    "Lists all available commands and how to use them if you follow the command with another command.",
                    "help", HelpCommand::new),
            new CommandDefinition("info", List.of(), CORE,
                    "Shows some basic information (id, name, etc.) about the user who used this command. Alternatively, you can @ mention someone to get their information.",
                    "info @user", UserInfoCommand::new),
            new CommandDefinition("join", List.of("wooclap"), CORE,
                    "Joins a Wooclap event with your saved token. If you don't have one saved or it stops working, click Enter token and you'll be joined as soon as it's saved.",
                    "join <event_code>", WooclapCommand::new),
            // "snipe" is intentionally not registered here - SnipeCommand exists but isn't functional yet.

            new CommandDefinition("balance", List.of("bal", "b"), ECONOMY,
                    "Finds the user's balance (cash & in their bank) for the bot's economy features.",
                    "balance @user", BalanceCommand::new),
            new CommandDefinition("beg", List.of(), ECONOMY,
                    "Pretend to be a beggar and get some cash.",
                    "beg", BegCommand::new),
            new CommandDefinition("rich", List.of(), ECONOMY,
                    "Sees the leaderboard of the richest people on the server.",
                    "rich", RichCommand::new),
            new CommandDefinition("rob", List.of(), ECONOMY,
                    "Attempts to rob the mentioned user.",
                    "rob", RobCommand::new),
            new CommandDefinition("deposit", List.of("dep", "d"), ECONOMY,
                    "Deposit your cash into your bank's vault.",
                    "deposit max", DepositCommand::new),
            new CommandDefinition("withdraw", List.of("with", "w"), ECONOMY,
                    "Withdraw money from your vault into cash.",
                    "withdraw 10k", WithdrawCommand::new),
            new CommandDefinition("pay", List.of(), ECONOMY,
                    "Give a user some cash.",
                    "pay @user 10k", (event, params) -> event.getChannel().sendMessage("Command is disabled.").queue()),

            new CommandDefinition("parallel", List.of("pres", "circuits"), CALCULATORS,
                    "Parallel resistance calculator.",
                    "pres `5k||10k||3k`", ParallelCommand::new),

            new CommandDefinition("test", List.of(), DEV,
                    "No documentation.",
                    "No example usage.", (event, params) -> {
                        new TestCommand(event, params);
                        event.getChannel().sendMessage("Command is disabled.").queue();
                    }),
            new CommandDefinition("cheat", List.of(), DEV,
                    "Cheats in money to a users cash or bank reserves.",
                    "cheat cash @user 50k", CheatCommand::new),
            new CommandDefinition("wooclappull", List.of("wpull"), DEV,
                    "Joins other users into a Wooclap event using their saved tokens. Users without a saved token are pinged to set one, then joined automatically.",
                    "wooclappull <event_code> @user @user", WooclapPullCommand::new),
            new CommandDefinition("wooclapdrop", List.of("wdrop"), DEV,
                    "Drops other users out of a Wooclap event, so the bot stops answering it for them. Also cancels their queued join for it.",
                    "wooclapdrop <event_code> @user @user", WooclapDropCommand::new),

            new CommandDefinition("wooclapclear", List.of("wclear"), OWNER,
                    "Deletes every message in the Wooclap log channel.",
                    "wooclapclear", WooclapClearCommand::new)
    );

    private static final Map<String, CommandDefinition> BY_ALIAS = new HashMap<>();

    static {
        for (CommandDefinition definition : COMMANDS) {
            BY_ALIAS.put(definition.name().toLowerCase(), definition);
            definition.aliases().forEach(alias -> BY_ALIAS.put(alias.toLowerCase(), definition));
        }
    }

    private CommandRegistry() {}

    public static Optional<CommandDefinition> find(String alias) {
        return Optional.ofNullable(BY_ALIAS.get(alias.toLowerCase()));
    }

    public static List<CommandDefinition> inModule(commandTypes module) {
        return COMMANDS.stream().filter(definition -> definition.module() == module).toList();
    }
}
