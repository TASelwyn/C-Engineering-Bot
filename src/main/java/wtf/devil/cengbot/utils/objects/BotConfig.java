package wtf.devil.cengbot.utils.objects;

public class BotConfig {

    private final String token;
//    private final String redisUri;
//    private final String redisPrefix;

    // Postgres connection. Each can be overridden with DATABASE_URL / DATABASE_USER / DATABASE_PASSWORD
    private final String databaseUrl;
    private final String databaseUser;
    private final String databasePassword;

    public BotConfig() {
        this.token = "";
//        this.redisUri = "";
//        this.redisPrefix = "";
        this.databaseUrl = "jdbc:postgresql://localhost:5432/cengbot";
        this.databaseUser = "cengbot";
        this.databasePassword = "cengbot";
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

    public String getDatabaseUrl() {
        return envOr("DATABASE_URL", databaseUrl);
    }

    public String getDatabaseUser() {
        return envOr("DATABASE_USER", databaseUser);
    }

    public String getDatabasePassword() {
        return envOr("DATABASE_PASSWORD", databasePassword);
    }

    private static String envOr(String name, String fallback) {
        String value = System.getenv(name);
        return value != null && !value.isBlank() ? value : fallback;
    }

    public boolean isValid() {
        return token != null && !token.isBlank();
    }
}
