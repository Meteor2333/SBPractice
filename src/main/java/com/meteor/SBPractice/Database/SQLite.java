package com.meteor.SBPractice.Database;

import com.meteor.SBPractice.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

import java.io.File;
import java.io.IOException;
import java.sql.*;
import java.util.UUID;

@SuppressWarnings({"CallToPrintStackTrace", "ResultOfMethodCallIgnored"})
public class SQLite implements Database {
    private String url;

    private Connection connection;

    public SQLite() {
        File dir = Main.getPlugin().getDataFolder();
        if (!dir.exists()) dir.mkdir();
        File file = new File(dir, "data.db");
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
            Bukkit.getConsoleSender().sendMessage(ChatColor.RED + "[SBPractice] Could Not Found SQLite Driver on your system!");
            Bukkit.getPluginManager().disablePlugin(Main.getPlugin());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void initialize() {
        try {
            checkConnection();
            String sql = "CREATE TABLE IF NOT EXISTS papi_data (id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "uuid VARCHAR(36), destructions INTEGER(10), placements INTEGER(10), restores INTEGER(10));";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void setDestructions(UUID key, int value) {
        String sql;
        try {
            checkConnection();
            if (hasData(key)) {
                sql = "UPDATE papi_data SET destructions=?, placements=?, restores=? WHERE uuid = ?;";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, value);
                    statement.setInt(2, getPlacements(key));
                    statement.setInt(3, getRestores(key));
                    statement.setString(4, key.toString());
                    statement.executeUpdate();
                }
            } else {
                sql = "INSERT INTO papi_data (uuid, destructions, placements, restores) VALUES(?, ?, ?, ?);";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, key.toString());
                    statement.setInt(2, value);
                    statement.setInt(3, getPlacements(key));
                    statement.setInt(4, getRestores(key));
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getDestructions(UUID key) {
        String sql = "SELECT * FROM papi_data WHERE uuid = ?;";
        try {
            checkConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, key.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getInt("destructions");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } return 0;
    }

    @Override
    public void setPlacements(UUID key, int value) {
        String sql;
        try {
            checkConnection();
            if (hasData(key)) {
                sql = "UPDATE papi_data SET destructions=?, placements=?, restores=? WHERE uuid = ?;";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, getDestructions(key));
                    statement.setInt(2, value);
                    statement.setInt(3, getRestores(key));
                    statement.setString(4, key.toString());
                    statement.executeUpdate();
                }
            } else {
                sql = "INSERT INTO papi_data (uuid, destructions, placements, restores) VALUES(?, ?, ?, ?);";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, key.toString());
                    statement.setInt(2, getDestructions(key));
                    statement.setInt(3, value);
                    statement.setInt(4, getRestores(key));
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getPlacements(UUID key) {
        String sql = "SELECT * FROM papi_data WHERE uuid = ?;";
        try {
            checkConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, key.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getInt("placements");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } return 0;
    }

    @Override
    public void setRestores(UUID key, int value) {
        String sql;
        try {
            checkConnection();
            if (hasData(key)) {
                sql = "UPDATE papi_data SET destructions=?, placements=?, restores=? WHERE uuid = ?;";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, getDestructions(key));
                    statement.setInt(2, getPlacements(key));
                    statement.setInt(3, value);
                    statement.setString(4, key.toString());
                    statement.executeUpdate();
                }
            } else {
                sql = "INSERT INTO papi_data (uuid, destructions, placements, restores) VALUES(?, ?, ?, ?);";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, key.toString());
                    statement.setInt(2, getDestructions(key));
                    statement.setInt(3, getPlacements(key));
                    statement.setInt(4, value);
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public int getRestores(UUID key) {
        String sql = "SELECT * FROM papi_data WHERE uuid = ?;";
        try {
            checkConnection();
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, key.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getInt("restores");
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } return 0;
    }

    private boolean hasData(UUID uuid) {
        String sql = "SELECT uuid FROM papi_data WHERE uuid = ?;";
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
