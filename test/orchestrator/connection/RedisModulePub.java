package wtf.devil.cengbot.orchestrator.connection;

import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.async.RedisAsyncCommands;
import io.lettuce.core.api.sync.RedisCommands;
import wtf.devil.cengbot.orchestrator.Heartbeat;
import wtf.devil.cengbot.utils.objects.BotConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public final class RedisModulePub implements AutoCloseable {

    private static final long HEARTBEAT_PERIOD_MS = 10_000; // 1/6th the stale timeout
    private final StatefulRedisConnection<String, String> connection;
    private final RedisAsyncCommands<String, String> async;
    private final RedisCommands<String, String> sync;
    private final String redisPrefix;

    private final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "redis-heartbeat");
        t.setDaemon(true);
        return t;
    });
    private volatile ScheduledFuture<?> heartbeatTask;

    public RedisModulePub(RedisModule redisModule, String redisPrefix) {
        this.connection = redisModule.client().connect();
        this.async = connection.async();
        this.sync = connection.sync();
        this.redisPrefix = redisPrefix;

        startHeartbeat();
    }

    public void publishAsync(String channel, String message) {
        async.publish(redisPrefix + channel, message);
    }

    public synchronized void startHeartbeat() {
        if (heartbeatTask != null && !heartbeatTask.isDone()) return;

        heartbeatTask = scheduler.scheduleAtFixedRate(() -> {
            try {
                async.publish(redisPrefix + "status", Heartbeat.toJson(Heartbeat.create()));
            } catch (Throwable t) {
                t.printStackTrace();
            }
        }, 0, HEARTBEAT_PERIOD_MS, TimeUnit.MILLISECONDS);
    }

    @Override
    public void close() {
        connection.close();
    }


}