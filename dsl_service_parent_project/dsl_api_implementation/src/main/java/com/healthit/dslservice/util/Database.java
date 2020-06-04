/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.List;
import org.apache.log4j.Logger;
import com.healthit.dslservice.DslException;
import com.healthit.dslservice.message.Message;
import com.healthit.dslservice.message.MessageType;
import java.io.IOException;
import java.util.Properties;
import org.apache.commons.dbcp2.BasicDataSource;

/**
 *
 * @author duncan
 */
public class Database {

    private static Connection conn = null;
    private static BasicDataSource dataSource;
    private static Properties properties = null;

    private static final String DB_USERNAME = "db.username";
    private static final String DB_PASSWORD = "db.password";
    private static final String DB_URL = "db.url";
    private static final String DB_DRIVER_CLASS = "driver.class.name";

    final static Logger log
            = Logger.getLogger(Database.class.getCanonicalName());

    public Database() throws DslException {
        connect();

    }

    private static void connect() throws DslException {
        try {
            if (conn != null) {
                if (!conn.isValid(3)) {
                    createDbConnection();
                }
            } else {
                createDbConnection();
            }

        } catch (Exception ex) {
            log.error(ex);
            Message msg = new Message();
            msg.setMessageType(MessageType.DB_CONNECTION_ERROR);
            msg.setMesageContent("Database connection error: " + ex.getMessage());
            throw new DslException(msg);
        }
    }

    private static void createDbConnection() throws SQLException, IOException {
        log.info("creating connection pool");
        properties = new Properties();
        properties.load(Database.class.getResourceAsStream("/database.properties"));
        dataSource = new BasicDataSource();
        dataSource.setDriverClassName(properties.getProperty(DB_DRIVER_CLASS));
        dataSource.setUrl(properties.getProperty(DB_URL));
        dataSource.setUsername(properties.getProperty(DB_USERNAME));
        dataSource.setPassword(properties.getProperty(DB_PASSWORD));

        dataSource.setMinIdle(100);
        dataSource.setMaxIdle(1000);

        dataSource.setMaxTotal(100);

        conn = dataSource.getConnection();
    }

    public Connection getConn() {
        return conn;
    }

    public ResultSet executeQuery(String sql) {
        PreparedStatement ps;
        ResultSet rs = null;
        log.info("number of open pooled connections: " + dataSource.getNumActive());
        try {
            ps = conn.prepareStatement(sql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            log.info("Query to run: " + ps.toString());
            rs = ps.executeQuery();
        } catch (SQLException ex) {
            log.error(ex);
        }
        return rs;
    }

    /**
     *
     * @param sql
     * @param parameters list of query parameters map, with value as key and
     * datatype [string, integer or date]
     * @return
     */
    public ResultSet executeQuery(String sql, List<Map> parameters) {
        log.info("Unconcat query: " + sql);
        PreparedStatement ps;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);

            Iterator i = parameters.iterator();
            int index = 1;
            while (i.hasNext()) {
                Map param = (Map) i.next();
                if (param.get("type") == "string") {
                    ps.setString(index, (String) param.get("value"));
                }
                if (param.get("type") == "integer") {
                    log.info("value data == >");
                    log.info(param.get("value"));
                    String val = (String) param.get("value");
                    ps.setInt(index, Integer.parseInt(val));
                }
                index += 1;
            }
            log.info("Query to run: " + ps.toString());
            rs = ps.executeQuery();
        } catch (SQLException ex) {
            log.error(ex);
        }
        return rs;
    }

    /**
     * Gets the resultset with movaable cursor for column count
     *
     * @param sql
     * @return list with resultset and rows count
     *
     */
    public Map<String, Object> executeQueryWithColumnCount(String sql) {
        Map<String, Object> reslts = new HashMap();
        PreparedStatement ps;
        ResultSet rs = null;
        try {

            Statement s
                    = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE,
                            ResultSet.CONCUR_READ_ONLY);
            rs = s.executeQuery(sql);
            rs.last();
            int count = rs.getRow();
            rs.beforeFirst();
            reslts.put("resultset", rs);
            reslts.put("columncount", count);
        } catch (SQLException ex) {
            log.error(ex);
        }
        return reslts;
    }

    public void CloseConnection() {

    }

}
