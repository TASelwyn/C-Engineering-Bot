package wtf.devil.cengbot.utils;

import net.dv8tion.jda.api.exceptions.ErrorHandler;
import net.dv8tion.jda.api.requests.ErrorResponse;

public final class MessageUtils {

    public static final ErrorHandler IGNORE_MISSING_PERMS = new ErrorHandler()
            .ignore(ErrorResponse.MISSING_PERMISSIONS, ErrorResponse.MISSING_ACCESS);

    private MessageUtils() {}
}
