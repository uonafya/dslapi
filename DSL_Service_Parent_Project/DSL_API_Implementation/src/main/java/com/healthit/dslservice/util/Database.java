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
import java.util.Map;
import java.util.List;
import org.apache.log4j.Logger;

/**
 *
 * @author duncan
 */
public class Database {

    private Connection conn = null;
    private static final String url = "jdbc:postgresql://host:port/database";
    private static final String user = "";
    private static final String password = "";
    final static Logger log
            = Logger.getLogger(Database.class.getCanonicalName());
    
    public Database(){
        connect();
    }
    
    private Boolean connect() {
        Boolean isConneced = false;
        try {
            Class.forName("org.postgresql.Driver");
            try {
                log.info("Making database connection");
                conn = DriverManager.getConnection(Database.url, Database.user,
                        Database.password);
                log.info("Connected to the PostgreSQL server successfully.");
            } catch (SQLException e) {
                log.error(e.getMessage());
            }
            isConneced = true;
        } catch (ClassNotFoundException ex) {
            log.error(ex);
        }

        return isConneced;
    }

    public Connection getConn() {
        return conn;
    }

    public ResultSet executeQuery(String sql) {
        PreparedStatement ps;
        ResultSet rs = null;
        try {
            ps = conn.prepareStatement(sql);
            log.info("Query to run: "+ps.toString());
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
        try {
            conn.close();
        } catch (SQLException ex) {
            log.error(ex);
        }
    }

}
