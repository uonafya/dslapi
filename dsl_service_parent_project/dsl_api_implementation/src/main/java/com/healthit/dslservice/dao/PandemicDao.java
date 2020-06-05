package com.healthit.dslservice.dao;

import com.healthit.dslservice.DslException;
import com.healthit.dslservice.dto.PandemicIndicator;
import com.healthit.dslservice.dto.dhis.IndicatorGoup;
import com.healthit.dslservice.message.Message;
import com.healthit.dslservice.message.MessageType;
import com.healthit.dslservice.util.CacheKeys;
import com.healthit.dslservice.util.DatabaseSource;
import com.healthit.dslservice.util.DslCache;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
public class PandemicDao {

    final static Logger log = Logger.getLogger(PandemicDao.class);
    Cache cache = DslCache.getCache();
    private boolean isPandemicDataWhereClauseAppended = false;

    private String selectPandemicIndicators = "Select pandemic_indicator_id as id, name FROM dim_pandemic_indicator";
    private String selectPandemicDataSql = "select fp.indicator_id as indicatorId,dim_pindc.name as indicator_name, dimp.date as date, \n"
            + "porg.name as orgUnit, fp.orgunit_id as orgId ,porg.latitude as org_latitude, porg.longitude as org_longitude"
            + ",fp.value as value from fact_pandemic fp \n"
            + "inner join dim_pandemic_period dimp on dimp.pandemic_period_id=fp.period_id\n"
            + "inner join pandemic_comm_org_unit porg on porg.pan_org_id =fp.orgunit_id\n"
            + "inner join dim_pandemic_indicator dim_pindc on dim_pindc.pandemic_indicator_id =fp.indicator_id ";

    /**
     * Gets a list of pandemics
     *
     * @return List<Map> A list of pandemics and their associated indciators
     * @throws DslException
     */
    public List<Map> getPandemics() throws DslException {
        List<Map> pandemicList = new ArrayList();
        List<PandemicIndicator> pandemicIndicatorList = new ArrayList();
        Map<String, Object> pandemics = new HashMap();
        pandemics.put("id", "covid19");
        pandemics.put("name", "covid 19");
        pandemics.put("initial_report_date", "2020-03-17");

        Element ele = cache.get(CacheKeys.pandemicsList);

        if (ele == null) {

            PreparedStatement ps = null;
            ResultSet rs = null;
            Connection conn = null;
            try {
                conn = DatabaseSource.getConnection();
                ps = conn.prepareStatement(selectPandemicIndicators, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                log.info("Query to run: " + ps.toString());
                rs = ps.executeQuery();

                log.info("Fetching indicator groups");

                while (rs.next()) {
                    PandemicIndicator pandemicIndicator = new PandemicIndicator();
                    pandemicIndicator.setId(rs.getInt("id"));
                    pandemicIndicator.setName(rs.getString("name"));
                    pandemicIndicatorList.add(pandemicIndicator);
                }
                pandemics.put("indicators", pandemicIndicatorList);
                pandemicList.add(pandemics);
                cache.put(new Element(CacheKeys.pandemicsList, pandemicList));
            } catch (SQLException ex) {
                log.error(ex);
                Message msg = new Message();
                msg.setMessageType(MessageType.SQL_QUERY_ERROR);
                msg.setMesageContent(ex.getMessage());
                throw new DslException(msg);
            } finally {
                DatabaseSource.close(rs);
                DatabaseSource.close(ps);
                DatabaseSource.close(conn);
            }
        } else {
            long startTime = System.nanoTime();
            pandemicList = (List<Map>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return pandemicList;
    }

    private void addIndicatorFilterClause(String indicatorId) throws DslException {

        try {
            Integer.parseInt(indicatorId);
        } catch (Exception ex) {
            Message msg = new Message();
            msg.setMesageContent("The indicator id should be a number");
            msg.setMessageType(MessageType.NUMBER_FORMAT_ERROR);
            throw new DslException(msg);
        }
        String indicatorFilter = " fp.indicator_id=" + Integer.parseInt(indicatorId);
        if (!isPandemicDataWhereClauseAppended) {
            selectPandemicDataSql = selectPandemicDataSql + " where " + indicatorFilter;
            isPandemicDataWhereClauseAppended = true;
        } else {
            selectPandemicDataSql = selectPandemicDataSql + " and " + indicatorFilter;
        }
    }

    private void addOrgunitFilterClause(String orgId) throws DslException {

        try {
            Integer.parseInt(orgId);
        } catch (Exception ex) {
            Message msg = new Message();
            msg.setMesageContent("The organisation unit id should be a number");
            msg.setMessageType(MessageType.NUMBER_FORMAT_ERROR);
            throw new DslException(msg);
        }
        String orgunitFilter = " fp.orgunit_id=" + Integer.parseInt(orgId);
        if (!isPandemicDataWhereClauseAppended) {
            selectPandemicDataSql = selectPandemicDataSql + " where " + orgunitFilter;
            isPandemicDataWhereClauseAppended = true;
        } else {
            selectPandemicDataSql = selectPandemicDataSql + " and " + orgunitFilter;
        }
    }

    private void addStartDateFilterClause(String startDate) throws DslException {

        try {

            Date startD = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);

        } catch (Exception ex) {
            Message msg = new Message();
            msg.setMesageContent("Wrong startDate format, should be : yyyy-MM-dd");
            msg.setMessageType(MessageType.YEAR_FORMAT_ERROR);
            throw new DslException(msg);
        }
        String startDateFilter = " dimp.date>='" + startDate + "'";
        if (!isPandemicDataWhereClauseAppended) {
            selectPandemicDataSql = selectPandemicDataSql + " where " + startDateFilter;
            isPandemicDataWhereClauseAppended = true;
        } else {
            selectPandemicDataSql = selectPandemicDataSql + " and " + startDateFilter;
        }
    }

    private void addEndDateFilterClause(String endDate) throws DslException {

        try {

            Date startD = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);

        } catch (Exception ex) {
            Message msg = new Message();
            msg.setMesageContent("Wrong startDate format, should be : yyyy-MM-dd");
            msg.setMessageType(MessageType.YEAR_FORMAT_ERROR);
            throw new DslException(msg);
        }
        String endDateFilter = " dimp.date<='" + endDate + "'";
        if (!isPandemicDataWhereClauseAppended) {
            selectPandemicDataSql = selectPandemicDataSql + " where " + endDateFilter;
            isPandemicDataWhereClauseAppended = true;
        } else {
            selectPandemicDataSql = selectPandemicDataSql + " and " + endDateFilter;
        }
    }

    public Map<String, Map> getPandemicData(String pandemicSource, String indicatorId, String orgId, String startDate, String endDate) throws DslException {
        String cacheName = pandemicSource + indicatorId + orgId + startDate + endDate;
        Element ele = cache.get(cacheName);
        Map<String, Map> envelop = new HashMap();
        Map<String, Map> result = new HashMap();
        Map<String, Object> dictionary = new HashMap();
        Map<Integer, List<Map>> data = new HashMap();

        List<Map<String, Object>> orgUnitsList = new ArrayList();
        List<Integer> addedOrgUnits = new ArrayList();
        List<PandemicIndicator> indicatorList = new ArrayList();
        List<Map<String, String>> period = new ArrayList();

        if (ele == null) {

            if (indicatorId != null) {
                addIndicatorFilterClause(indicatorId);
            }

            if (orgId != null) {
                addOrgunitFilterClause(orgId);
            }

            if (startDate != null) {
                addStartDateFilterClause(startDate);
            }

            if (endDate != null) {
                addEndDateFilterClause(endDate);
            }

            PreparedStatement ps = null;
            ResultSet rs = null;
            Connection conn = null;
            try {
                conn = DatabaseSource.getConnection();
                ps = conn.prepareStatement(selectPandemicDataSql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                log.info("Query to run: " + ps.toString());
                rs = ps.executeQuery();

                log.info("Fetching indicator groups");

                while (rs.next()) {
                    Map dataUnit = new HashMap(); // keys: period, ouid, value
                    int dbIndicatorId = rs.getInt("indicatorId");
                    int dbOrgId = rs.getInt("orgId");
                    String dbOrgUnitName = rs.getString("orgUnit");
                    String dbIndicatorName = rs.getString("indicator_name");
                    if (data.containsKey(dbIndicatorId)) {

                        dataUnit.put("period", rs.getDate("date"));
                        dataUnit.put("ou", dbOrgId);
                        dataUnit.put("value", rs.getInt("value"));
                        data.get(dbIndicatorId).add(dataUnit);

                    } else {
                        List<Map> dataList = new ArrayList();
                        PandemicIndicator pIndic = new PandemicIndicator();
                        pIndic.setId(dbIndicatorId);
                        pIndic.setName(dbIndicatorName);
                        indicatorList.add(pIndic);

                        dataUnit.put("period", rs.getDate("date"));
                        dataUnit.put("ou", rs.getInt("orgId"));
                        dataUnit.put("value", rs.getInt("value"));
                        dataList.add(dataUnit);
                        data.put(dbIndicatorId, dataList);

                    }
                    if (!addedOrgUnits.contains(dbOrgId)) {
                        Map<String, Object> orgMapUnit = new HashMap();
                        orgMapUnit.put("id", dbOrgId);
                        orgMapUnit.put("name", dbOrgUnitName);
                        orgMapUnit.put("latitude", rs.getString("org_latitude"));
                        orgMapUnit.put("longitude", rs.getString("org_longitude"));
                        orgUnitsList.add(orgMapUnit);
                        addedOrgUnits.add(dbOrgId);
                    }

                }

                dictionary.put("orgunits", orgUnitsList);
                dictionary.put("indicators", indicatorList);

                result.put("data", data);
                result.put("dictionary", dictionary);
                envelop.put("result", result);
                cache.put(new Element(cacheName, envelop));
            } catch (SQLException ex) {
                log.error(ex);
                Message msg = new Message();
                msg.setMessageType(MessageType.SQL_QUERY_ERROR);
                msg.setMesageContent(ex.getMessage());
                throw new DslException(msg);
            } finally {
                DatabaseSource.close(rs);
                DatabaseSource.close(ps);
                DatabaseSource.close(conn);
            }
        } else {
            long startTime = System.nanoTime();
            envelop = (Map<String, Map>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return envelop;
    }
}
