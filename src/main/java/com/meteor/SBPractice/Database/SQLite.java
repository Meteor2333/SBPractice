package com.meteor.SBPractice.Database;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Main;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

@SuppressWarnings({"CallToPrintStackTrace", "ResultOfMethodCallIgnored"})
public class SQLite implements Database {
    private final String url;

    private Connection connection;

    public SQLite() {
        File dir = Main.getPlugin().getDataFolder();
        if (!dir.exists()) dir.mkdir();
        File file = new File(dir, "stats.db");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } this.url = "jdbc:sqlite:" + file;
        try {
            Class.forName("org.sqlite.JDBC");
            DriverManager.getConnection(url);
        } catch (ClassNotFoundException e) {
            Main.getPlugin().getLogger().severe("§cCould Not Found SQLite Driver on your system!");
            Bukkit.getPluginManager().disablePlugin(Main.getPlugin());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize() {
        try {
            checkConnection();
            String sql = "CREATE TABLE IF NOT EXISTS stats (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "uuid VARCHAR(36), break_blocks INTEGER(10), place_blocks INTEGER(10), jumps INTEGER(10), restores INTEGER(10), online_times INTEGER(10));";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SBPPlayer.PlayerStats getPlayerStats(UUID uuid) {
        String sql = "SELECT * FROM stats WHERE uuid = ?;";
        try {
            checkConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return new SBPPlayer.PlayerStats(uuid, result.getInt("break_blocks"), result.getInt("place_blocks"), result.getInt("jumps"), result.getInt("restores"), result.getInt("online_times"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } return new SBPPlayer.PlayerStats(uuid, 0, 0, 0, 0, 0);
    }

    @Override
    public void setPlayerStats(SBPPlayer.PlayerStats playerStats) {
        String sql;
        try {
            checkConnection();
            if (hasData(playerStats.getUuid())) {
                sql = "UPDATE stats SET break_blocks=?, place_blocks=?, jumps=?, restores=?, online_times=? WHERE uuid = ?;";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, playerStats.getBreakBlocks());
                    statement.setInt(2, playerStats.getPlaceBlocks());
                    statement.setInt(3, playerStats.getJumps());
                    statement.setInt(4, playerStats.getRestores());
                    statement.setInt(5, playerStats.getOnlineTimes());
                    statement.setString(6, playerStats.getUuid().toString());
                    statement.executeUpdate();
                }
            } else {
                sql = "INSERT INTO stats (uuid, break_blocks, place_blocks, jumps, restores, online_times) VALUES(?, ?, ?, ?, ?, ?);";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, playerStats.getUuid().toString());
                    statement.setInt(2, playerStats.getBreakBlocks());
                    statement.setInt(3, playerStats.getPlaceBlocks());
                    statement.setInt(4, playerStats.getJumps());
                    statement.setInt(5, playerStats.getRestores());
                    statement.setInt(6, playerStats.getOnlineTimes());
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private boolean hasData(UUID uuid) {
        String sql = "SELECT uuid FROM stats WHERE uuid = ?;";
        try {
            checkConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    return result.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } return false;
    }

    private void checkConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed())
            this.connection = DriverManager.getConnection(url);
    }
}
