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
import org.apache.log4j.Logger;

/**
 *
 * @author duncan
 */
public class Database {

    private Connection conn = null;
    private final String url = "jdbc:postgresql://41.89.93.180:5432/mohdsl";
    private final String user = "dsl";
    private final String password = "dsl2015";
    final static Logger log
            = Logger.getLogger(Database.class.getCanonicalName());

    private Boolean connect() {
        Boolean isConneced = false;
        try {
            Class.forName("org.postgresql.Driver");
            Database db = new Database();
            try {
                log.info("Making database connection");
                conn = DriverManager.getConnection(db.url, db.user,
                        db.password);
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

    public ResultSet executeQuery(String sql) {
        PreparedStatement ps;
        ResultSet rs = null;
        connect();
        try {
            ps = conn.prepareStatement(sql);
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
        connect();
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
