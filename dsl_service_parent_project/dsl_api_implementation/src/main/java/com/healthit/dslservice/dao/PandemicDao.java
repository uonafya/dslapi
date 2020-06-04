package com.healthit.dslservice.dao;

import com.healthit.dslservice.DslException;
import static com.healthit.dslservice.dao.DhisDao.log;
import com.healthit.dslservice.dto.PandemicIndicator;
import com.healthit.dslservice.dto.dhis.IndicatorGoup;
import com.healthit.dslservice.util.CacheKeys;
import com.healthit.dslservice.util.Database;
import com.healthit.dslservice.util.DslCache;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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

    private String selectPandemicIndicators = "Select pandemic_indicator_id as id, name FROM dim_pandemic_indicator";
    private String selectPandemicDataSql = "select fp.indicator_id as indicatorId,dim_pindc.name as indicator_name, dimp.date as date, \n"
            + "porg.name as orgUnit, fp.orgunit_id as orgId ,porg.latitude as org_latitude, porg.longitude as org_longitude"
            + ",fp.value as value from fact_pandemic fp \n"
            + "inner join dim_pandemic_period dimp on dimp.pandemic_period_id=fp.period_id\n"
            + "inner join pandemic_comm_org_unit porg on porg.pan_org_id =fp.orgunit_id\n"
            + "inner join dim_pandemic_indicator dim_pindc on dim_pindc.pandemic_indicator_id =fp.indicator_id";

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
            Database db = new Database();
            ResultSet rs = db.executeQuery(selectPandemicIndicators);
            log.info("Fetching indicator groups");
            try {
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
            } finally {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    log.error(ex);
                }
            }
        } else {
            long startTime = System.nanoTime();
            pandemicList = (List<Map>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return pandemicList;
    }

    public Map<String, Map> getPandemicData(String pandemicSource, String indicatorId, String orgId, String startDate, String endDate) throws DslException {
        String cacheName = pandemicSource + indicatorId + orgId + startDate + endDate;
        Element ele = cache.get(cacheName);
        Map<String, Map> result = new HashMap();
        Map<String, Object> dictionary = new HashMap();
        Map<Integer, List<Map>> data = new HashMap();

        List<Map<String, Object>> orgUnitsList = new ArrayList();
        List<Integer> addedOrgUnits = new ArrayList();
        List<PandemicIndicator> indicatorList = new ArrayList();
        List<Map<String, String>> period = new ArrayList();

        if (ele == null) {
            Database db = new Database();
            ResultSet rs = db.executeQuery(selectPandemicDataSql);
            log.info("Fetching indicator groups");
            try {
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

                cache.put(new Element(cacheName, result));
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                try {
                    rs.close();
                } catch (SQLException ex) {
                    log.error(ex);
                }
            }
        } else {
            long startTime = System.nanoTime();
            result = (Map<String, Map>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return result;
    }
}
