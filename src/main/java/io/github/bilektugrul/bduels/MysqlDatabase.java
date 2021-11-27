//* Copyright (C) 2021 Despical
package io.github.bilektugrul.bduels;

import com.zaxxer.hikari.HikariDataSource;
import org.bukkit.configuration.file.FileConfiguration;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

/**
 * @author Despical
 * <p>
 * Created at 30.05.2020
 * @version 1.1
 */
public class MysqlDatabase {

    private HikariDataSource hikariDataSource;
    private boolean forceClose = false;
    private final Logger databaseLogger;

    public MysqlDatabase(Logger logger, FileConfiguration config) {
        this.databaseLogger = logger;
        String user = config.getString("database.mysql-settings.user");
        String password = config.getString("database.mysql-settings.password");
        String jdbcUrl = config.getString("database.mysql-settings.url");
        databaseLogger.info("MySQL bağlantısı ayarlanıyor...");
        configureConnPool(user, password, jdbcUrl);

        try (Connection connection = hikariDataSource.getConnection()) {
            if (connection == null) {
                databaseLogger.severe("Veritabanına bağlanılamadı.");
                forceClose = true;
            }
        } catch (SQLException e) {
            databaseLogger.warning("Veritabanına bağlantıda sorun yaşanıyor. Eklenti devre dışı bırakılacak.");
            forceClose = true;
        }
    }

    private void configureConnPool(String user, String password, String jdbcUrl) {
        try {
            databaseLogger.info("Creating HikariCP Configuration...");
            HikariDataSource config = new HikariDataSource();
            config.setJdbcUrl(jdbcUrl);
            config.addDataSourceProperty("user", user);
            config.addDataSourceProperty("password", password);
            hikariDataSource = config;
            databaseLogger.info("Setting up MySQL Connection pool...");
            databaseLogger.info("Connection pool successfully configured. ");
        } catch (Exception e) {
            e.printStackTrace();
            databaseLogger.warning("Cannot connect to MySQL database!");
            databaseLogger.warning("Check configuration of your database settings!");
        }
    }

    private void configureConnPool(String user, String password, String host, String database, int port) {
        try {
            databaseLogger.info("Creating HikariCP Configuration...");
            HikariDataSource config = new HikariDataSource();
            config.setDataSourceClassName("com.mysql.cj.jdbc.MysqlDataSource");
            config.addDataSourceProperty("serverName", host);
            config.addDataSourceProperty("portNumber", port);
            config.addDataSourceProperty("databaseName", database);
            config.addDataSourceProperty("user", user);
            config.addDataSourceProperty("password", password);
            hikariDataSource = config;
            databaseLogger.info("Setting up MySQL Connection pool...");
            databaseLogger.info("Connection pool successfully configured. ");
        } catch (Exception e) {
            e.printStackTrace();
            databaseLogger.warning("Cannot connect to MySQL database!");
            databaseLogger.warning("Check configuration of your database settings!");
        }
    }

    public void executeUpdate(String query) {
        try (Connection connection = getConnection()) {
            try (Statement statement = connection.createStatement()) {
                statement.executeUpdate(query);
            }
        } catch (SQLException e) {
            databaseLogger.warning("Failed to execute update: " + query);
        }
    }

    public void shutdownConnPool() {
        try {
            databaseLogger.info("Shutting down connection pool. Trying to close all connections.");

            if (!hikariDataSource.isClosed()) {
                hikariDataSource.close();
                databaseLogger.info("Pool successfully shutdown.");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        Connection conn = null;

        try {
            conn = hikariDataSource.getConnection();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return conn;
    }

    public boolean shouldClose() {
        return forceClose;
    }

}