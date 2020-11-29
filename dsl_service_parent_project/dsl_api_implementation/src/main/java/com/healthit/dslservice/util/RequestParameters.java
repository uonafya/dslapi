/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.util;

import com.healthit.DslException;
import com.healthit.message.Message;
import com.healthit.message.MessageType;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.log4j.Logger;

/**
 *
 * @author duncan
 */
public class RequestParameters {

    final static Logger log = Logger.getLogger(RequestParameters.class);
    Cache cache = DslCache.getCache();

    public static void isValidPeriod(String pe) throws DslException {
        String[] periodList = pe.split(";");
        for (int x = 0; x < periodList.length; x++) {
            try {
                Integer.parseInt(periodList[x]);
            } catch (Exception e) {
                Message msg = new Message();
                msg.setMessageType(MessageType.NUMBER_FORMAT_ERROR);
                msg.setMesageContent("Please check the period parameter(s), format YYYY or YYYYmm");
                DslException dslExc = new DslException(msg);
                throw dslExc;
            }
            if (periodList[x].trim().length() != 4 && periodList[x].trim().length() != 6) {
                log.info("the period passed: " + periodList[x]);
                Message msg = new Message();
                msg.setMessageType(MessageType.YEAR_FORMAT_ERROR);
                msg.setMesageContent("Please check the period parameter(s), format YYYY or YYYYmm");
                DslException dslExc = new DslException(msg);
                throw dslExc;
            }
        }
    }

    public static String getOruntiLevel(String ouId) throws DslException {
        log.info("fetching org levels");
        RequestParameters rsqt = new RequestParameters();
        Element ele = rsqt.cache.get(CacheKeys.orgUnitLevelToId);
        Map<String, String> orgIdToOrgLevel = new HashMap();
        if (ele != null) {
            log.debug("cache org level is not null");
            orgIdToOrgLevel = (Map<String, String>) ele.getObjectValue();
        } else {
            log.debug("cache org level is null");
            String ogrunitLevel = "select level,dhis_organisation_unit_id as id from common_organisation_unit";

            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {

                conn = DatabaseSource.getConnection();
                ps = conn.prepareStatement(ogrunitLevel);
                rs = ps.executeQuery();
                log.debug("find org unti level query " + ps.toString());
                while (rs.next()) {
                    orgIdToOrgLevel.put(rs.getString("id"), rs.getString("level"));
                }
                rsqt.cache.put(new Element(CacheKeys.orgUnitLevelToId, orgIdToOrgLevel));
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                DatabaseSource.close(rs);
                DatabaseSource.close(ps);
                DatabaseSource.close(conn);
            }

        }
        if (orgIdToOrgLevel.get(ouId) == null) {
            Message msg = new Message();
            msg.setMessageType(MessageType.MISSING_DB_ENRTY_VALUE);
            msg.setMesageContent("Could not find org unit with id " + ouId);
            throw new DslException(msg);
        }
        return orgIdToOrgLevel.get(ouId);
    }
}
