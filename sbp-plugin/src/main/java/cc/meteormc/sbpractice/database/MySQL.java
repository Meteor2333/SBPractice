package cc.meteormc.sbpractice.database;

import cc.meteormc.sbpractice.Main;
import cc.meteormc.sbpractice.api.storage.Database;
import cc.meteormc.sbpractice.api.storage.data.PlayerData;
import cc.meteormc.sbpractice.config.MainConfig;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.*;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class MySQL implements Database {
    private final HikariDataSource dataSource;

    public MySQL() {
        String host = MainConfig.MYSQL.HOST.resolve();
        String database = MainConfig.MYSQL.DATABASE.resolve();
        String username = MainConfig.MYSQL.USER.getOrDefault();
        String password = MainConfig.MYSQL.PASSWORD.getOrDefault();
        int port = MainConfig.MYSQL.PORT.resolve();
        boolean ssl = MainConfig.MYSQL.USESSL.resolve();

        final long time = System.currentTimeMillis();
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName("SBPracticePool");
        hikariConfig.setMaximumPoolSize(10);
        hikariConfig.setMaxLifetime(1800000L);
        hikariConfig.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);
        hikariConfig.setUsername(username);
        hikariConfig.setPassword(password);
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
        this.dataSource = new HikariDataSource(hikariConfig);
        if (System.currentTimeMillis() - time >= 5000) {
            Main.get().getLogger().warning("It took " + (System.currentTimeMillis() - time) / 1000 + " ms to establish a database connection! Using this remote connection is not recommended!");
        }
    }

    @Override
    public void connect() {
        try (Connection connection = this.dataSource.getConnection()) {
            String sql = "CREATE TABLE IF NOT EXISTS stats (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, uuid VARCHAR(200), restores INT(200));";
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(sql);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Main.get().getLogger().severe("Could not connect to database! Please verify your credentials and make sure that the server IP is whitelisted in MySQL.");
        }
    }

    @Override
    public PlayerData.PlayerStats getPlayerStats(UUID uuid) {
        try (Connection connection = this.dataSource.getConnection()) {
            String sql = "SELECT restores FROM stats WHERE uuid = ?;";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return new PlayerData.PlayerStats(uuid, result.getInt(1));
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Main.get().getLogger().severe("Could not connect to database! Please verify your credentials and make sure that the server IP is whitelisted in MySQL.");
        }
        return new PlayerData.PlayerStats(uuid, 0);
    }

    @Override
    public void setPlayerStats(PlayerData.PlayerStats playerStats) {
        try (Connection connection = this.dataSource.getConnection()) {
            if (hasData(playerStats.getUuid())) {
                String sql = "UPDATE stats SET restores=? WHERE uuid = ?;";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setInt(1, playerStats.getRestores());
                    statement.setString(2, playerStats.getUuid().toString());
                    statement.executeUpdate();
                }
            } else {
                String sql = "INSERT INTO stats (uuid, restores) VALUES (?, ?);";
                try (PreparedStatement statement = connection.prepareStatement(sql)) {
                    statement.setString(1, playerStats.getUuid().toString());
                    statement.setInt(2, playerStats.getRestores());
                    statement.executeUpdate();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Main.get().getLogger().severe("Could not connect to database! Please verify your credentials and make sure that the server IP is whitelisted in MySQL.");
        }
    }

    private boolean hasData(UUID uuid) {
        try (Connection connection = this.dataSource.getConnection()) {
            String sql = "SELECT uuid FROM stats WHERE uuid = ?;";
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid.toString());
                try (ResultSet result = statement.executeQuery()) {
                    return result.next();
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
            Main.get().getLogger().severe("Could not connect to database! Please verify your credentials and make sure that the server IP is whitelisted in MySQL.");
        }
        return false;
    }
}
