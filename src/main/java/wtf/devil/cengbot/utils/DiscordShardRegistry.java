package wtf.devil.cengbot.utils;

import org.javacord.api.DiscordApi;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public final class DiscordShardRegistry {

    private static final ConcurrentMap<Integer, DiscordApi> shards =
            new ConcurrentHashMap<>();

    private DiscordShardRegistry() {}

    public static void register(DiscordApi api) {
        shards.put(api.getCurrentShard(), api);
    }

    public static DiscordApi getShard(int shardId) {
        return shards.get(shardId);
    }

    public static DiscordApi getApi() {
        return shards.get(0);
    }
}
