package wtf.devil.cengbot.orchestrator;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonSyntaxException;

public record Heartbeat(String status, long timestamp) {

    private static final long HEARTBEAT_STALE_THRESHOLD_MS = 60_000;
    private static final Gson GSON = new GsonBuilder().create();

    public static Heartbeat create() {
        return new Heartbeat("alive", System.currentTimeMillis());
    }

    public boolean isStale() {
        if (timestamp > System.currentTimeMillis()) { return false; }

        return System.currentTimeMillis() - timestamp > HEARTBEAT_STALE_THRESHOLD_MS;
    }

    public static Heartbeat fromJson(String json) {
        try {
            return GSON.fromJson(json, Heartbeat.class);
        } catch (JsonSyntaxException e) {
            return null;
        }
    }

    public static String toJson(Heartbeat heartbeat) {
        return GSON.toJson(heartbeat);
    }
}
