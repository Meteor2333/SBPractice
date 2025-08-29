package cc.meteormc.sbpractice.database;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.storage.Database;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import org.bukkit.Bukkit;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

public class SQLite implements Database {
    private String url;

    private Connection connection;

    public SQLite() {
        String path = Main.get().getDataFolder().getPath();
        new File(path).mkdirs();
        File file = new File(path, "stats.db");
        if (!file.exists()) {
            try {
                if (!file.createNewFile()) {
                    Main.get().getLogger().warning("Failed to create new file: " + file.getName());
                    return;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Main.get().getLogger().warning("Failed to create new file: " + file.getName());
            }
        }
        this.url = "jdbc:sqlite:" + file;
    }

    @Override
    public void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            DriverManager.getConnection(this.url);
        } catch (ClassNotFoundException e) {
            Bukkit.getPluginManager().disablePlugin(Main.get());
        } catch (SQLException e) {
            e.printStackTrace();
        }

        try {
            checkConnection();
            String sql = "CREATE TABLE IF NOT EXISTS stats (id INTEGER PRIMARY KEY AUTOINCREMENT, uuid VARCHAR(36), restores INTEGER(10));";
            try (Statement statement = this.connection.createStatement()) {
                statement.executeUpdate(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public PlayerData.PlayerStats getPlayerStats(UUID uuid) {
        String sql = "SELECT * FROM stats WHERE uuid = ?;";
        try {
            checkConnection();
            try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return new PlayerData.PlayerStats(uuid, result.getInt("restores"));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new PlayerData.PlayerStats(uuid, 0);
    }

    @Override
    public void setPlayerStats(PlayerData.PlayerStats playerStats) {
        String sql;
        try {
            checkConnection();
            if (hasData(playerStats.getUuid())) {
                sql = "UPDATE stats SET restores=? WHERE uuid = ?;";
                try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
                    statement.setInt(1, playerStats.getRestores());
                    statement.setString(2, playerStats.getUuid().toString());
                    statement.executeUpdate();
                }
            } else {
                sql = "INSERT INTO stats (uuid, restores) VALUES(?, ?);";
                try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
                    statement.setString(1, playerStats.getUuid().toString());
                    statement.setInt(2, playerStats.getRestores());
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
            try (PreparedStatement statement = this.connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    return result.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }

    private void checkConnection() throws SQLException {
        if (this.connection == null || this.connection.isClosed())
            this.connection = DriverManager.getConnection(this.url);
    }
}
