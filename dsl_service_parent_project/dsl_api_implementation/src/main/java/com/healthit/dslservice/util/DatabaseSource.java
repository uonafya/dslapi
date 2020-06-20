package com.healthit.dslservice.util;

import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 *
 * @author duncan
 */
public class DatabaseSource {

    private static BasicDataSource dataSource;

    final static org.apache.log4j.Logger log
            = org.apache.log4j.Logger.getLogger(DatabaseSource.class.getCanonicalName());

    static {
        dataSource = new BasicDataSource();
        String DB_USERNAME = "db.username";
        String DB_PASSWORD = "db.password";
        String DB_URL = "db.url";
        String DB_DRIVER_CLASS = "driver.class.name";

        try {
            Properties properties = new Properties();
            properties.load(DatabaseSource.class.getResourceAsStream("/database.properties"));

            dataSource.setDriverClassName(properties.getProperty(DB_DRIVER_CLASS));
            dataSource.setUrl(properties.getProperty(DB_URL));
            dataSource.setUsername(properties.getProperty(DB_USERNAME));
            dataSource.setPassword(properties.getProperty(DB_PASSWORD));

            dataSource.setMinIdle(5);
            dataSource.setMaxIdle(10);
            dataSource.setMaxTotal(150);
            dataSource.setInitialSize(2);

        } catch (IOException ex) {
            log.error(ex);
        }
    }

    public static Connection getConnection() throws SQLException {
        log.info("Active db connection from pool: " + dataSource.getNumActive());
        return dataSource.getConnection();
    }

    private DatabaseSource() {
    }

    public static void close(ResultSet c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (SQLException e) {
            //
        }
    }

    public static void close(Statement c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (SQLException e) {
            //
        }
    }

    public static void close(Connection c) {
        try {
            if (c != null) {
                c.close();
            }
        } catch (SQLException e) {
            //
        }
    }
}
