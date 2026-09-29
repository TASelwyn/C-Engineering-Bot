package wtf.devil.cengbot;

import java.text.DecimalFormat;
import java.text.NumberFormat;

public final class Constants {

    // Formatters
    public static final NumberFormat numFormatter = new DecimalFormat("###,###,###");
    public static final NumberFormat percentFormatter = new DecimalFormat("#0.#####%");

    public static final String rootDir = "data/";
    public static final String logsLocation = rootDir + "logs/";
    public static final String configLocation = rootDir + "config/config.json";

    // Commands
    public static final String commandPrefix = "c.";

    // Wooclap
    public static final long wooclapLogChannelId = 1552777993808646204L;
    // Command aliases/help/module wiring now lives in wtf.devil.cengbot.commands.CommandRegistry.
}