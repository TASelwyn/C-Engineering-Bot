package wtf.devil.cengbot;


import wtf.devil.cengbot.utils.objects.commandTypes;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public final class Constants {

    // Formatters
    public static final NumberFormat numFormatter = new DecimalFormat("###,###,###");
    public static final NumberFormat percentFormatter = new DecimalFormat("#0.#####%");

    public static final String rootDir = "data/";
    public static final String logsLocation = rootDir + "logs/";
    public static final String configLocation = rootDir + "config.json";

    public static final String databaseFile = "data/cengbot.sqlite";
    public static final String databaseConnectionURI = "jdbc:sqlite:" + databaseFile;

    // Commands
    public static final String commandPrefix = "c.";
    public static final commandTypes[] modules = {commandTypes.CORE, commandTypes.ECONOMY, commandTypes.CALCULATORS, commandTypes.DEV};
    public static final String[][][] commands = {{{"help"}, {"info"}, /*{"snipe"}*/}, {{"balance", "bal", "b"}, {"beg"}, {"rich"}, {"rob"}, {"deposit", "dep", "d"}, {"withdraw", "with", "w"}, {"pay"}}, {{"parallel", "pres", "circuits"}}, {{"test"}, {"cheat"}}};
    public static final String[][][] commandsHelp = {
            {{"help", "Lists all available commands and how to use them if you follow the command with another command.", "help"}, {"info", "Shows some basic information (id, name, etc.) about the user who used this command. Alternatively, you can @ mention someone to get their information.", "info @user"}, /*{"snipe"}*/},
            {{"balance", "Finds the user's balance (cash & in their bank) for the bot's economy features.", "balance @user"}, {"beg", "Pretend to be a beggar and get some cash.", "beg"}, {"rich", "Sees the leaderboard of richest people on the server.", "rich"}, {"rob", "Attempts to rob the mentioned user.", "rob"}, {"deposit", "Deposit your cash into your bank's vault.", "deposit max"}, {"withdraw", "Withdraw money from your vault into cash.", "withdraw 10k"}, {"pay", "Give a user some cash.", "pay @user 10k"}},
            {{"parallel", "Parallel resistance calculator.", "pres `5k||10k||3k`"}},
            {{"test", "No documentation.", "No example usage."}, {"cheat", "Cheats in money to a users cash or bank reserves.", "cheat cash @user 50k"}}
    };
}