package cc.meteormc.sbpractice.database;

import cc.meteormc.sbpractice.config.MainConfig;
import com.j256.ormlite.jdbc.DataSourceConnectionSource;
import com.j256.ormlite.jdbc.db.MysqlDatabaseType;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.SQLException;
import java.util.concurrent.TimeUnit;

public class MySQLSource extends DataSourceConnectionSource {
    public MySQLSource() throws SQLException {
        String host = MainConfig.MYSQL.HOST.resolve();
        String database = MainConfig.MYSQL.DATABASE.resolve();
        String username = MainConfig.MYSQL.USER.getOrDefault();
        String password = MainConfig.MYSQL.PASSWORD.getOrDefault();
        int port = MainConfig.MYSQL.PORT.resolve();
        boolean ssl = MainConfig.MYSQL.USESSL.resolve();

        HikariConfig hikari = new HikariConfig();
        hikari.setPoolName("sbpractice-db-pool");
        hikari.setJdbcUrl("jdbc:mysql://" + host + ":" + port + "/" + database);

        hikari.setUsername(username);
        hikari.setPassword(password);

        hikari.setMaximumPoolSize(10);
        hikari.setMinimumIdle(10);
        hikari.setMaxLifetime(TimeUnit.MINUTES.toMillis(30L));
        hikari.setKeepaliveTime(TimeUnit.MILLISECONDS.toMillis(0L));
        hikari.setConnectionTimeout(TimeUnit.SECONDS.toMillis(5L));

        hikari.addDataSourceProperty("useSSL", String.valueOf(ssl));
        hikari.addDataSourceProperty("cachePrepStmts", "true");
        hikari.addDataSourceProperty("prepStmtCacheSize", "275");
        hikari.addDataSourceProperty("prepStmtCacheSqlLimit", "2048");
        hikari.addDataSourceProperty("useServerPrepStmts", "true");
        hikari.addDataSourceProperty("useLocalSessionState", "true");
        hikari.addDataSourceProperty("rewriteBatchedStatements", "true");
        hikari.addDataSourceProperty("jdbcCompliantTruncation", "false");
        hikari.addDataSourceProperty("cacheResultSetMetadata", "true");
        hikari.addDataSourceProperty("cacheServerConfiguration", "true");
        hikari.addDataSourceProperty("elideSetAutoCommits", "true");
        hikari.addDataSourceProperty("maintainTimeStats", "false");
        hikari.addDataSourceProperty("alwaysSendSetIsolation", "false");
        hikari.addDataSourceProperty("cacheCallableStmts", "true");
        hikari.addDataSourceProperty("serverTimezone", "UTC");
        hikari.addDataSourceProperty("socketTimeout", String.valueOf(TimeUnit.SECONDS.toMillis(30L)));

        this.setDataSource(new HikariDataSource(hikari));
        this.setDatabaseType(new MysqlDatabaseType());
        this.setDatabaseUrl(hikari.getJdbcUrl());
        this.initialize();
    }
}
