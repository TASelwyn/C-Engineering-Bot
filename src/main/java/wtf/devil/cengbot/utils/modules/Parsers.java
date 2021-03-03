package wtf.devil.cengbot.utils.modules;

import wtf.devil.cengbot.Constants;
import wtf.devil.cengbot.utils.objects.commandTypes;

public class Parsers {

    public static long parseStringToLong(String input) {
        try {
            // Multiplier for k/m/b/t
            long multiplier;
            char multiplierKey = ' ';

            String parseString = input.toLowerCase().replaceAll(" +", "").replaceAll(",", "");

            if (parseString.length() > 0 && Character.isLetter(parseString.charAt(parseString.length() - 1))) {
                multiplierKey = parseString.charAt(parseString.length() - 1);
                parseString = parseString.replaceAll(String.valueOf(parseString.charAt(input.length() - 1)), "");
            }

            long inputValue = Long.parseLong(parseString);

            // k is thousand
            // m is million
            // b is billion
            // t is trillion
            switch (multiplierKey) {
                case 'k':
                    multiplier = 1000L;
                    break;
                case 'm':
                    multiplier = 1000000L;
                    break;
                case 'b':
                    multiplier = 1000000000L;
                    break;
                case 't':
                    multiplier = 1000000000000L;
                    break;
                default:
                    multiplier = 1L;
                    break;
            }

            return inputValue * multiplier;
        } catch (NumberFormatException e) {
            return 0;
            //throw e;
        }
    }

    public static double parseOhmsStringToDouble(String input) {
        try {
            char multiplierKey = ' ';

            String parseString = input.toLowerCase().replaceAll(" +", "").replaceAll(",", "");

            if (parseString.length() > 0 && Character.isLetter(parseString.charAt(parseString.length() - 1))) {
                multiplierKey = parseString.charAt(parseString.length() - 1);
                parseString = parseString.replaceAll(String.valueOf(parseString.charAt(input.length() - 1)), "");
            }

            double inputValue = Double.parseDouble(parseString);

            // k is femto (10^-15)
            // k is thousand
            // g is giga (10^9)
            double multiplier;
            switch (multiplierKey) {
                case 'f':
                    multiplier = 0.000000000000001;
                    break;
                case 'k':
                    multiplier = 1000;
                    break;
                case 'g':
                    multiplier = 1000000000;
                    break;
                default:
                    multiplier = 1;
                    break;
            }

            return inputValue * multiplier;
        } catch (NumberFormatException e) {
            return 0;
            //throw e;
        }
    }

    public static String parseStringToCommand(String commandAlias) {
        // Returns command string from command alias
        String command = "";

        loop:
        for (int i = 0; i < Constants.modules.length; i++) {
            for (int j = 0; j < Constants.commands[i].length; j++) {
                for (int k = 0; k < Constants.commands[i][j].length; k++) {
                    if (commandAlias.toLowerCase().equals(Constants.commands[i][j][k])) {
                        command = Constants.commands[i][j][0].toLowerCase();
                        break loop;
                    }
                }
            }
        }

        return command;
    }

    public static commandTypes parseStringToCommandModule(String command) {
        // This function will not work with command aliases.
        commandTypes commandModule = null;

        loop:
        for (int i = 0; i < Constants.modules.length; i++) {
            for (int j = 0; j < Constants.commands[i].length; j++) {
                if (command.toLowerCase().equals(Constants.commands[i][j][0])) {
                    switch (i) {
                        case 0:
                            commandModule = commandTypes.CORE;
                            break;
                        case 1:
                            commandModule = commandTypes.ECONOMY;
                            break;
                        case 2:
                            commandModule = commandTypes.CALCULATORS;
                            break;
                        case 3:
                            commandModule = commandTypes.DEV;
                            break;
                    }
                    break loop;
                }
            }
        }

        return commandModule;
    }
}
