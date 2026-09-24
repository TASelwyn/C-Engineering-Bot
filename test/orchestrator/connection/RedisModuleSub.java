package wtf.devil.cengbot.orchestrator.connection;

import io.lettuce.core.RedisFuture;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import io.lettuce.core.pubsub.api.async.RedisPubSubAsyncCommands;

import java.util.Collection;
import java.util.concurrent.Executor;

public final class RedisModuleSub implements AutoCloseable {

    @FunctionalInterface
    public interface MessageHandler {
        void onMessage(String channel, String message);
    }

    private final StatefulRedisPubSubConnection<String, String> connection;
    private final RedisPubSubAsyncCommands<String, String> async;
    private final Executor callbackExecutor;

    public RedisModuleSub(RedisModule redisModule, Executor callbackExecutor, MessageHandler handler) {
        this.connection = redisModule.client().connectPubSub();
        this.async = connection.async();
        this.callbackExecutor = callbackExecutor;

        connection.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String channel, String message) {
                callbackExecutor.execute(() -> handler.onMessage(channel, message));
            }
        });
    }

    public RedisFuture<Void> subscribe(Collection<String> channels) {
        return async.subscribe(channels.toArray(String[]::new));
    }

    public RedisFuture<Void> unsubscribe(Collection<String> channels) {
        return async.unsubscribe(channels.toArray(String[]::new));
    }

    @Override
    public void close() {
        connection.close();

    }
}