package wtf.devil.cengbot.commands;

import org.javacord.api.event.message.MessageCreateEvent;
import wtf.devil.cengbot.commands.calculators.ParallelCommand;
import wtf.devil.cengbot.commands.dev.CheatCommand;
import wtf.devil.cengbot.commands.dev.TestCommand;
import wtf.devil.cengbot.commands.economy.*;
import wtf.devil.cengbot.commands.general.HelpCommand;
import wtf.devil.cengbot.commands.general.SnipeCommand;
import wtf.devil.cengbot.commands.general.UserInfoCommand;
import wtf.devil.cengbot.utils.database.UserDatabase;
import wtf.devil.cengbot.utils.objects.commandTypes;

import java.sql.SQLException;

public class CommandManager {
    public CommandManager(commandTypes commandModules, MessageCreateEvent event, String command, String[] params, long startTimestamp) {
        switch (commandModules) {
            case CORE:
                GeneralCommandManager(event, command, params);
                break;
            case ECONOMY:
                EconomyCommandManager(event, command, params);
                break;
            case CALCULATORS:
                CalculatorsCommandManager(event, command, params);
                break;
            case DEV:
                DevCommandManager(event, command, params, startTimestamp);
                break;
        }
    }

    private void GeneralCommandManager(MessageCreateEvent event, String command, String[] params) {
        switch (command) {
            case "help":
                new HelpCommand(event, params);
                break;
            case "info":
                new UserInfoCommand(event, params);
                break;
            case "snipe":
                new SnipeCommand(event, params);
                break;
        }
    }

    private void EconomyCommandManager(MessageCreateEvent event, String command, String[] params) {
        if (/*event.getChannel().getId() == 799083105184382997L && */UserDatabase.healthCheck(event.getMessageAuthor().getId())) {
            try {
                UserDatabase.storeLatestNickname(event.getMessageAuthor().getId(), event.getMessageAuthor().getDiscriminatedName());
                //event.getMessageAuthor().getDisplayName()
            } catch (SQLException e) {
                e.printStackTrace();
            }

            switch (command) {
                case "balance":
                    new BalanceCommand(event, params);
                    break;
                case "beg":
                    new BegCommand(event, params);
                    break;
                case "pay":
                    //new PayCommand(event, params);
                    event.getChannel().sendMessage("Command is disabled.");
                    break;
                case "rich":
                    new RichCommand(event, params);
                    break;
                case "rob":
                    new RobCommand(event, params);
                    break;
                case "withdraw":
                    new WithdrawCommand(event, params);
                    break;
                case "deposit":
                    new DepositCommand(event, params);
                    break;
            }


        } else if (UserDatabase.healthCheck(event.getMessageAuthor().getId())) {
            // something very wrong

        } else {
            event.getChannel().sendMessage("Economy commands are only available in <#799083105184382997>");
        }
    }

    private void CalculatorsCommandManager(MessageCreateEvent event, String command, String[] params) {
        switch (command) {
            case "parallel":
                new ParallelCommand(event, params);
                break;
        }
    }

    private void DevCommandManager(MessageCreateEvent event, String command, String[] params, long startTimestamp) {
        if (event.getMessageAuthor().isServerAdmin() || command.equalsIgnoreCase("test")) {
            switch (command) {
                case "test":
                    //new TestCommand(event, params, startTimestamp);
                    event.getChannel().sendMessage("Command is disabled.");
                    break;
                case "cheat":
                    new CheatCommand(event, params);
                    break;
            }
        } else {
            event.getChannel().sendMessage("No permissions.");
        }
    }
}
