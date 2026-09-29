package wtf.devil.cengbot.utils.modules;

import net.dv8tion.jda.api.entities.Mentions;
import net.dv8tion.jda.api.entities.User;

import java.util.LinkedHashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class Parsers {

    private static final Pattern USER_ID_PATTERN = Pattern.compile("<@!?(\\d{17,20})>|\\b(\\d{17,20})\\b");

    /*
     * Finds the users named in text, by mention (<@id>) or raw user ID, in the order they appear.
     * Bots found in the resolved mentions are left out.
     */
    public static Set<Long> parseUserIds(String text, Mentions mentions) {
        Set<Long> bots = mentions.getUsers().stream()
                .filter(User::isBot)
                .map(User::getIdLong)
                .collect(Collectors.toSet());

        Set<Long> userIds = new LinkedHashSet<>();
        Matcher matcher = USER_ID_PATTERN.matcher(text);
        while (matcher.find()) {
            long id = Long.parseLong(matcher.group(1) != null ? matcher.group(1) : matcher.group(2));
            if (!bots.contains(id)) {
                userIds.add(id);
            }
        }
        return userIds;
    }

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
}
