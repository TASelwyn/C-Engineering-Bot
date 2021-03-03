package wtf.devil.cengbot.utils.database;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import wtf.devil.cengbot.Constants;
import wtf.devil.cengbot.Main;

import java.io.File;
import java.sql.*;

import static wtf.devil.cengbot.Constants.databaseConnectionURI;

public class DatabaseManager {
    private static Logger logger = LogManager.getLogger(Main.class);

    public static boolean databaseTestConnection() {
        File databaseFile = new File(Constants.databaseFile);
        Connection conn;

        if (databaseFile.exists() && databaseFile.isFile() && databaseFile.length() > 0) {
            try {
                conn = DriverManager.getConnection(databaseConnectionURI);
                DatabaseMetaData meta = conn.getMetaData();
                logger.info("Connected to: " + meta.getDriverName());

                Statement stmt = conn.createStatement();
                logger.info("Found " + stmt.executeQuery("SELECT COUNT(discord_id) FROM users").getInt(1) + " users in the database.");
                stmt.close();

                return true;
            } catch (SQLException e) {
                e.printStackTrace();
            }
        } else if (!databaseFile.exists() && databaseFile.length() == 0) {
            logger.info("Database does not exist. Creating...");
            createDatabase();
        } else if (databaseFile.isDirectory()) {
            logger.warn("Database URI cannot be a directory.");
        }
        return false;
    }

    private static void createDatabase() {
        try {
            Connection conn = DriverManager.getConnection(databaseConnectionURI);
            Statement stmt = conn.createStatement();
            stmt.executeUpdate("create table users (" +
                    "    discord_id long not null" +
                    "        constraint users_pk" +
                    "            primary key," +
                    "    add nickname String," +
                    "    cash long default 0 not null," +
                    "    bank long default 0 not null," +
                    "    multiplier double default 1 not null," +
                    "    level int default 1 not null," +
                    "    vault_level int default 1 not null);" +
                    "create unique index users_discord_id_uindex" +
                    "    on users (discord_id);");
            stmt.executeUpdate("INSERT INTO users VALUES(598372060728918016,100000,500000,1,1,5)");
            stmt.close();
            logger.info("Database has been successfully created.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}