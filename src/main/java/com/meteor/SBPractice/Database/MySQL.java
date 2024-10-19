package com.meteor.SBPractice.Database;

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
            String sql = "CREATE TABLE IF NOT EXISTS papi_data (id INT NOT NULL AUTO_INCREMENT PRIMARY KEY, " +
                    "uuid VARCHAR(200), destructions INT(200), placements INT(200), restores INT(200));";

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
        try (Connection connection = dataSource.getConnection()) {
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
                sql = "INSERT INTO papi_data (uuid, destructions, placements, restores) VALUES (?, ?, ?, ?);";
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
        String sql = "SELECT destructions, placements, restores FROM papi_data WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, key.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getInt(1);
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
        try (Connection connection = dataSource.getConnection()) {
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
                sql = "INSERT INTO papi_data (uuid, destructions, placements, restores) VALUES (?, ?, ?, ?);";
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
        String sql = "SELECT destructions, placements, restores FROM papi_data WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, key.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getInt(2);
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
        try (Connection connection = dataSource.getConnection()) {
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
                sql = "INSERT INTO papi_data (uuid, destructions, placements, restores) VALUES (?, ?, ?, ?);";
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
        String sql = "SELECT destructions, placements, restores FROM papi_data WHERE uuid = ?;";
        try (Connection connection = dataSource.getConnection()) {
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, key.toString());
                try (ResultSet result = statement.executeQuery()) {
                    if (result.next()) {
                        return result.getInt(3);
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        } return 0;
    }

    private boolean hasData(UUID uuid) {
        String sql = "SELECT uuid FROM papi_data WHERE uuid = ?;";
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
