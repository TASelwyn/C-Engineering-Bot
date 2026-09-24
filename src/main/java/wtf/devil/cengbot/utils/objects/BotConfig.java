package wtf.devil.cengbot.utils.objects;

public class BotConfig {

    private final String token;
//    private final String redisUri;
//    private final String redisPrefix;

    public BotConfig() {
        this.token = "";
//        this.redisUri = "";
//        this.redisPrefix = "";
    }

    public BotConfig(String discordToken) {
        this.token = discordToken;
//        this.redisUri = redisUri;
//        this.redisPrefix = redisPrefix;
    }

    public String getToken() {
        return token;
    }

//    public String getRedisUri() {
//        return redisUri;
//    }
//
//    public String getRedisPrefix() {
//        return redisPrefix + ":";
//    }

    public boolean isValid() {
        return token != null && !token.isBlank();
    }
}
