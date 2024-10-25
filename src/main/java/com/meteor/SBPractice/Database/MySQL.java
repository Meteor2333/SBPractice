package com.meteor.SBPractice.Database;

import com.meteor.SBPractice.Api.SBPPlayer;
import com.meteor.SBPractice.Main;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("CallToPrintStackTrace")
public class MySQL implements Database {
    private HikariDataSource dataSource;
    private final String host;
    private final String database;
    private final String user;
    private final String pass;
    private final int port;
    private final boolean ssl;

    public MySQL() {
        this.host = Main.getPlugin().getConfig().getString("database.host");
        this.database = Main.getPlugin().getConfig().getString("database.database");
        this.user = Main.getPlugin().getConfig().getString("database.user");
        this.pass = Main.getPlugin().getConfig().getString("database.pass");
        this.port = Main.getPlugin().getConfig().getInt("database.port");
        this.ssl = Main.getPlugin().getConfig().getBoolean("database.ssl");
    }

    public boolean connect() {
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("SBPractice");
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMaxLifetime(1800000L);
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        hikariConfig.setUsername(user);
        hikariConfig.setPassword(pass);
        hikariConfig.addDataSourceProperty("useSSL", String.valueOf(ssl));
        hikariConfig.addDataSourceProperty("characterEncoding", "utf8");
        hikariConfig.addDataSourceProperty("encoding", "UTF-8");
        hikariConfig.addDataSourceProperty("useUnicode", "true");
        hikariConfig.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikariConfig.addDataSourceProperty("jdbcCompliantTruncation", "false");
        hikariConfig.addDataSourceProperty("cachePrepStmts", "true");
        hikariConfig.addDataSourceProperty("prepStmtCacheSize", "275");
        hikariConfig.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikariConfig.addDataSourceProperty("socketTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(30)));
        dataSource = new HikariDataSource(hikariConfig);
        try {
            dataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    @Override
    public void initialize() {
        try (Connection connection = dataSource.getConnection()) {
            String sql = "CREATE TABLE IF NOT EXISTS stats (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                    "uuid VARCHAR(200), break_blocks INT(200), place_blocks INT(200), jumps INT(200), restores INT(200), online_times INT(200));";

            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @Override
    public SBPPlayer.PlayerStats getPlayerStats(UUID uuid) {
        String sql = "SELECT break_blocks, place_blocks, jumps, restores, online_times FROM stats WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return new SBPPlayer.PlayerStats(uuid, result.getInt(1), result.getInt(2), result.getInt(3), result.getInt(4), result.getInt(5));
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
        try (Connection connection = dataSource.getConnection()) {
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
                sql = "INSERT INTO stats (uuid, break_blocks, place_blocks, jumps, restores, online_times) VALUES (?, ?, ?, ?, ?, ?);";
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
        try (Connection connection = dataSource.getConnection()) {
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
}
