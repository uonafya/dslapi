/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.dao;

import com.healthit.dslservice.DataSource;
import com.healthit.dslservice.DslException;
import static com.healthit.dslservice.dao.DhisDao.log;
import com.healthit.dslservice.dto.dhis.IndicatorValue;
import com.healthit.dslservice.message.Message;
import com.healthit.dslservice.message.MessageType;
import com.healthit.dslservice.util.CacheKeys;
import com.healthit.dslservice.util.Database;
import com.healthit.dslservice.util.DslCache;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.log4j.Logger;

/**
 *
 * @author duncanndiithi
 */
public class SurveyDao {

    final static Logger log = Logger.getLogger(SurveyDao.class);
    Cache cache = DslCache.getCache();

    private String sourceSql = "SELECT id,source FROM source";

    public List<Map<String, String>> getDataSources() throws DslException {
        Element ele = cache.get(CacheKeys.surveySources);
        List<Map<String, String>> result = new ArrayList();
        if (ele == null) {
            Database db = new Database();
            ResultSet rs = db.executeQuery(sourceSql);
            try {
                while (rs.next()) {
                    Map<String, String> payLoad = new HashMap();
                    payLoad.put("id", rs.getString("id"));
                    payLoad.put("name", rs.getString("source"));
                    result.add(payLoad);
                }
                cache.put(new Element(CacheKeys.surveySources, result));
                return result;
            } catch (SQLException ex) {
                Message msg = new Message();
                msg.setMesageContent("Unable to get sources ");
                msg.setMessageType(MessageType.SQL_QUERY_ERROR);
                throw new DslException(msg);
            } finally {
                db.CloseConnection();
            }
        } else {
            long startTime = System.nanoTime();
            result = (List<Map<String, String>>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
            return result;
        }
    }

    public List<Map<String, String>> getIndicators(String id) throws DslException {
        int sourceId = Integer.parseInt(id);
        List<Map<String, String>> result = new ArrayList();
        String indicatorQuery = "";
        if (sourceId == 2 || sourceId == 3 || sourceId == 4 || sourceId == 5 || sourceId == 6) {
            indicatorQuery = "SELECT Distinct ind.indicator_id as id, ind.name as name, '" + DataSource.getSources().get(sourceId) + "' as source '' as description FROM "
                    + " dim_survey_indicator ind inner join fact_survey fs on ind.indicator_id=fs.indicator_id "
                    + " inner join dim_survey_source source on fs.source_id = source.source_id where "
                    + "source.name=" + DataSource.getSources().get(sourceId);
        } else if (sourceId == 7) {
            indicatorQuery = "SELECT Distinct ind.indicator_id as id, ind.name as name, '" + DataSource.getSources().get(sourceId) + "' as source, description FROM "
                    + " dim_steps_indicator ind dim_steps_indicator";
        } else if (sourceId == 8) {
            indicatorQuery = "SELECT Distinct ind.indicator_id as id, ind.name as name, '" + DataSource.getSources().get(sourceId) + "' as source, description FROM "
                    + " dim_steps_indicator ind dim_khds_indicator";
        } else {
            Message msg = new Message();
            msg.setMesageContent("This id does not exist ");
            msg.setMessageType(MessageType.MISSING_DB_ENRTY_VALUE);
            throw new DslException(msg);
        }

        Database db = new Database();
        ResultSet rs = db.executeQuery(indicatorQuery);
        try {
            while (rs.next()) {
                Map<String, String> payLoad = new HashMap();
                payLoad.put("id", rs.getString("id"));
                payLoad.put("name", rs.getString("name"));
                payLoad.put("source", rs.getString("source"));
                payLoad.put("description", rs.getString("description"));
                result.add(payLoad);
            }
        } catch (SQLException ex) {
            Message msg = new Message();
            msg.setMesageContent(ex.getMessage());
            msg.setMessageType(MessageType.SQL_QUERY_ERROR);
            throw new DslException(msg);
        }
        return result;
    }
    
    public List<Map<String, String>> getIndicatorValue(String sId,String iId) throws DslException {
        int sourceId = Integer.parseInt(sId);
        int indicatorId = Integer.parseInt(iId);
        List<Map<String, String>> result = new ArrayList();
        String indicatorQuery = "";
        if (sourceId == 2 || sourceId == 3 || sourceId == 4 || sourceId == 5 || sourceId == 6) {
            indicatorQuery = "SELECT Distinct ind.indicator_id as id, ind.name as name, '" + DataSource.getSources().get(sourceId) + "' as source '' as description FROM "
                    + " dim_survey_indicator ind inner join fact_survey fs on ind.indicator_id=fs.indicator_id "
                    + " inner join dim_survey_source source on fs.source_id = source.source_id where "
                    + "source.name=" + DataSource.getSources().get(sourceId);
        } else if (sourceId == 7) {
            indicatorQuery = "SELECT Distinct ind.indicator_id as id, ind.name as name, '" + DataSource.getSources().get(sourceId) + "' as source, description FROM "
                    + " dim_steps_indicator ind dim_steps_indicator";
        } else if (sourceId == 8) {
            indicatorQuery = "SELECT Distinct ind.indicator_id as id, ind.name as name, '" + DataSource.getSources().get(sourceId) + "' as source, description FROM "
                    + " dim_steps_indicator ind dim_khds_indicator";
        } else {
            Message msg = new Message();
            msg.setMesageContent("This id does not exist ");
            msg.setMessageType(MessageType.MISSING_DB_ENRTY_VALUE);
            throw new DslException(msg);
        }

        Database db = new Database();
        ResultSet rs = db.executeQuery(indicatorQuery);
        try {
            while (rs.next()) {
                Map<String, String> payLoad = new HashMap();
                payLoad.put("id", rs.getString("id"));
                payLoad.put("name", rs.getString("name"));
                payLoad.put("source", rs.getString("source"));
                payLoad.put("description", rs.getString("description"));
                result.add(payLoad);
            }
        } catch (SQLException ex) {
            Message msg = new Message();
            msg.setMesageContent(ex.getMessage());
            msg.setMessageType(MessageType.SQL_QUERY_ERROR);
            throw new DslException(msg);
        }
        return result;
    }

}
