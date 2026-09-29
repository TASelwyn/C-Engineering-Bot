package wtf.devil.cengbot.utils.database;

import java.sql.*;
import java.util.Optional;

import static wtf.devil.cengbot.Constants.databaseConnectionURI;

// Kept separate from UserDatabase since its setValue logs values, which would leak tokens into the logs
public class WooclapDatabase {

    private static volatile boolean tableReady = false;

    public static Optional<String> getAuthToken(long discordID) throws SQLException {
        ensureTable();
        try (Connection conn = DriverManager.getConnection(databaseConnectionURI);
             PreparedStatement stmt = conn.prepareStatement("SELECT auth_token FROM wooclap_tokens WHERE discord_id = ?")) {
            stmt.setLong(1, discordID);
            try (ResultSet result = stmt.executeQuery()) {
                return result.next() ? Optional.ofNullable(result.getString(1)) : Optional.empty();
            }
        }
    }

    public static void setAuthToken(long discordID, String authToken) throws SQLException {
        ensureTable();
        try (Connection conn = DriverManager.getConnection(databaseConnectionURI);
             PreparedStatement stmt = conn.prepareStatement(
                     "INSERT INTO wooclap_tokens (discord_id, auth_token) VALUES (?, ?) " +
                     "ON CONFLICT(discord_id) DO UPDATE SET auth_token = excluded.auth_token")) {
            stmt.setLong(1, discordID);
            stmt.setString(2, authToken);
            stmt.executeUpdate();
        }
    }

    // Returns true if the user had a token saved
    public static boolean clearAuthToken(long discordID) throws SQLException {
        ensureTable();
        try (Connection conn = DriverManager.getConnection(databaseConnectionURI);
             PreparedStatement stmt = conn.prepareStatement("DELETE FROM wooclap_tokens WHERE discord_id = ?")) {
            stmt.setLong(1, discordID);
            return stmt.executeUpdate() > 0;
        }
    }

    private static void ensureTable() throws SQLException {
        if (tableReady) {
            return;
        }
        try (Connection conn = DriverManager.getConnection(databaseConnectionURI);
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("CREATE TABLE IF NOT EXISTS wooclap_tokens (" +
                    "discord_id INTEGER NOT NULL PRIMARY KEY, " +
                    "auth_token TEXT NOT NULL)");
        }
        tableReady = true;
    }
}
