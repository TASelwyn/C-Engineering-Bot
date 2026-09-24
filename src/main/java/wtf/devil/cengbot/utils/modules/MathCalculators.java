package wtf.devil.cengbot.utils.modules;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wtf.devil.cengbot.DevilsBot;

public class MathCalculators {
    private static Logger logger = LogManager.getLogger(DevilsBot.class);

    public static double parallelResistance(String[] params) {
        double eqvResistance;
        double addUp = 0;

        for (String param : params) {
            addUp += 1 / (Parsers.parseOhmsStringToDouble(param));
        }

        eqvResistance = 1 / addUp;
        return eqvResistance;
    }
}