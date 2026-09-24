package wtf.devil.cengbot.orchestrator.connection;

import io.lettuce.core.ClientOptions;
import io.lettuce.core.MaintNotificationsConfig;
import io.lettuce.core.RedisClient;

public final class RedisModule implements AutoCloseable {
    private final RedisClient client;

    public RedisModule(String redisUri) {
        client = RedisClient.create(redisUri);
        // Disable maintenance notifications from Redis
        ClientOptions options = ClientOptions.builder().maintNotificationsConfig(MaintNotificationsConfig.disabled()).build();
        client.setOptions(options);
    }

    public RedisClient client() {
        return client;
    }

    @Override
    public void close() {
        client.shutdown();
    }
}
