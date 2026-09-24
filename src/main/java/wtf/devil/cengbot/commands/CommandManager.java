package wtf.devil.cengbot.commands;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
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
    public CommandManager(commandTypes commandModules, MessageReceivedEvent event, String command, String[] params, long startTimestamp) {
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

    private void GeneralCommandManager(MessageReceivedEvent event, String command, String[] params) {
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
            case "wooclap":
                new CheatCommand(event, params);
        }
    }

    private void EconomyCommandManager(MessageReceivedEvent event, String command, String[] params) {
        if (/*event.getChannel().getIdLong() == 799083105184382997L && */UserDatabase.healthCheck(event.getAuthor().getIdLong())) {
            try {
                String currentName = event.getMember() != null ? event.getMember().getEffectiveName() : event.getAuthor().getName();
                UserDatabase.storeLatestNickname(event.getAuthor().getIdLong(), currentName);
                //event.getMember().getEffectiveName()
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
                    event.getChannel().sendMessage("Command is disabled.").queue();
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


        } else if (UserDatabase.healthCheck(event.getAuthor().getIdLong())) {
            // something very wrong

        } else {
            event.getChannel().sendMessage("Economy commands are only available in <#799083105184382997>").queue();
        }
    }

    private void CalculatorsCommandManager(MessageReceivedEvent event, String command, String[] params) {
        switch (command) {
            case "parallel":
                new ParallelCommand(event, params);
                break;
        }
    }

    private void DevCommandManager(MessageReceivedEvent event, String command, String[] params, long startTimestamp) {
        boolean isServerAdmin = event.isFromGuild() && event.getMember() != null && event.getMember().hasPermission(Permission.ADMINISTRATOR);

        if (isServerAdmin || command.equalsIgnoreCase("test")) {
            switch (command) {
                case "test":
                    new TestCommand(event, params, startTimestamp);
                    event.getChannel().sendMessage("Command is disabled.").queue();
                    break;
                case "cheat":
                    new CheatCommand(event, params);
                    break;
            }
        } else {
            event.getChannel().sendMessage("No permissions.").queue();
        }
    }
}
