package wtf.devil.cengbot.utils.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wtf.devil.cengbot.Main;

import javax.sql.rowset.CachedRowSet;
import javax.sql.rowset.RowSetProvider;
import java.sql.*;

import static wtf.devil.cengbot.Constants.databaseConnectionURI;

public class UserDatabase {
    private static Logger logger = LogManager.getLogger(Main.class);

    public static boolean healthCheck(long discordID) {
        try {
            if (!UserDatabase.doesUserExistInDB(discordID)) {
                UserDatabase.createUserInDB(discordID);
            }
            return true;
        } catch (SQLException exception) {
            return false;
        }
    }

    public static long getLong(long discordID, String column) throws SQLException {
        try {
            CachedRowSet crs = getUserData(discordID, column);

            long value = 0L;

            if (crs.next()) {
                value = crs.getLong(1);
            }

            return value;

        } catch (SQLException exception) {
            throw exception;
        }
    }

    public static int getInt(long discordID, String column) throws SQLException {
        try {
            CachedRowSet crs = getUserData(discordID, column);

            int value = 0;

            if (crs.next()) {
                value = crs.getInt(1);
            }

            return value;

        } catch (SQLException exception) {
            throw exception;
        }
    }

    public static double getDouble(long discordID, String column) throws SQLException {
        try {
            CachedRowSet crs = getUserData(discordID, column);

            double value = 0;

            if (crs.next()) {
                value = crs.getDouble(1);
            }

            return value;

        } catch (SQLException exception) {
            throw exception;
        }
    }

    public static void setValue(long discordID, String column, String value) throws SQLException {
        Connection conn = null;
        PreparedStatement stmt = null;

        String sqlStatement = "UPDATE users SET " + column + " = ? WHERE discord_id = ?";

        try {
            conn = DriverManager.getConnection(databaseConnectionURI);
            stmt = conn.prepareStatement(sqlStatement);

            stmt.setString(1, value);
            stmt.setLong(2, discordID);
            stmt.executeUpdate();


        } catch (SQLException exception) {
            throw exception;
        } finally {
            closerDBHandlers(conn, stmt);
            genericLogMsg(discordID, "set " + column + " to " + value);
        }
    }

    public static String getNickname(long discordID) throws SQLException {
        try {
            CachedRowSet crs = getUserData(discordID, "nickname");

            String value = null;

            if (crs.next()) {
                value = crs.getString(1);
            }

            return value;

        } catch (SQLException exception) {
            throw exception;
        }
    }


    public static void storeLatestNickname(long discordID, String nickname) throws SQLException {
        try {
            if (doesUserExistInDB(discordID)) {
                if (getNickname(discordID) == null || !getNickname(discordID).equals(nickname)) {
                    setValue(discordID, "nickname", nickname);
                }
            }
        } catch (SQLException exception) {
            throw exception;
        }
    }

    public static boolean doesUserExistInDB(long discordID) {
        try {
            Connection conn = DriverManager.getConnection(databaseConnectionURI);
            Statement stmt = conn.createStatement();

            String sqlStatement = "SELECT COUNT(discord_id) FROM users WHERE discord_id = " + discordID;

            boolean isUserInDB = stmt.executeQuery(sqlStatement).getBoolean(1);

            closerDBHandlers(conn, stmt);

            return isUserInDB;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    public static CachedRowSet getRichest(int limit) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DriverManager.getConnection(databaseConnectionURI);
            conn.setAutoCommit(false);

            stmt = conn.createStatement();

            String sqlStatement = "SELECT cash,bank,nickname FROM users ORDER BY cash DESC, bank DESC LIMIT " + limit;
            ResultSet result = stmt.executeQuery(sqlStatement);

            CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
            crs.populate(result);

            //System.out.println("ID: " + RS.getString(1) + " - " + RS.getString(2) + " - " + RS.getString(3) + " - " + RS.getString(4) + " - " + RS.getString(5) + " - " + RS.getString(6));


            return crs;

        } catch (SQLException exception) {
            throw exception;
        } finally {
            closerDBHandlers(conn, stmt);
            genericLogMsg(0, "data accessed");
        }
    }

    public static CachedRowSet getUserData(long discordID, String columnsToReturn) throws SQLException {
        Connection conn = null;
        Statement stmt = null;
        try {
            conn = DriverManager.getConnection(databaseConnectionURI);
            conn.setAutoCommit(false);

            stmt = conn.createStatement();

            String sqlStatement = "SELECT " + columnsToReturn + " FROM users WHERE discord_id = " + discordID;
            ResultSet result = stmt.executeQuery(sqlStatement);

            CachedRowSet crs = RowSetProvider.newFactory().createCachedRowSet();
            crs.populate(result);

            if (crs.size() <= 0) {
                // user doesn't exist.
                closerDBHandlers(conn, stmt);
                genericLogMsg(discordID, "returned no data");

                createUserInDB(discordID);
                return getUserData(discordID, columnsToReturn);
            }

           //System.out.println("ID: " + RS.getString(1) + " - " + RS.getString(2) + " - " + RS.getString(3) + " - " + RS.getString(4) + " - " + RS.getString(5) + " - " + RS.getString(6));


            return crs;

        } catch (SQLException exception) {
            throw exception;
        } finally {
            closerDBHandlers(conn, stmt);
            genericLogMsg(discordID, "data accessed");
        }
    }

    public static void createUserInDB(long discordID) throws SQLException {
        Connection conn = null;
        Statement stmt = null;

        try {
            conn = DriverManager.getConnection(databaseConnectionURI);
            stmt = conn.createStatement();

            String sqlStatement = "INSERT INTO users (discord_id) VALUES (" + discordID + ")";
            stmt.executeUpdate(sqlStatement);

        } catch (SQLException exception) {
            throw exception;
        } finally {
            closerDBHandlers(conn, stmt);
            genericLogMsg(discordID, "user created");
        }
    }

    private static void genericLogMsg(long discordID, String msg) {
        logger.info("(" + discordID + "-DB) " + msg);
    }

    private static void closerDBHandlers(Connection conn, Statement stmt) {
        try {
            if (conn != null) {
                conn.close();
            }
            if (stmt != null) {
                stmt.close();
            }
        } catch (SQLException exception) {
            exception.printStackTrace();
        }
    }
}
