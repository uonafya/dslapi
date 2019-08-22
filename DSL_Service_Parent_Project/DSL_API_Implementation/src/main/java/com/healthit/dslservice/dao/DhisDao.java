/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.dao;

import com.healthit.dslservice.DslException;
import com.healthit.dslservice.dto.dhis.Indicator;
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
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.log4j.Logger;
import org.springframework.stereotype.Component;

/**
 *
 * @author duncan
 */
@Component
public class DhisDao {

    final static Logger log = Logger.getLogger(FacilityDao.class);

    private String getIndicatorNames = "SELECT \"Indicator ID\" as id, indicatorname as name, indicatorgroupid as groupId\n"
            + "FROM public.vw_indicator_to_indicatorgroup";
    private String getIndicatorGroups = "SELECT DISTINCT indicatorgroupid as id, group_name as name\n" +
"FROM public.vw_indicator_to_indicatorgroup;";

    private Map<String, String> groupTable = new HashMap();

    Cache cache = DslCache.getCache();

    /**
     * fetch indicator related data
     *
     * @param pe period
     * @param ou organisation unit
     * @param indicator indicator id
     * @return indicator list
     * @throws DslException
     */
    public List<Indicator> getIndicators(String pe, String ou, String indicatorId, String groupId) throws DslException {
//        if (pe != null) {
//            nationalCadreGroupCount = insertPeriodPart(pe, nationalCadreGroupCount);
//            appendAnd = true;
//        } else {
//            nationalCadreGroupCount = nationalCadreGroupCount.replace("@pe@", "");
//        }
//
//        if (ou != null) {
//            String level = RequestParameters.getOruntiLevel(ou);
//            nationalCadreGroupCount = insertOrgUntiPart(ou, level, nationalCadreGroupCount);
//        } else {
//            nationalCadreGroupCount = nationalCadreGroupCount.replace("@ou@", "");
//            nationalCadreGroupCount = nationalCadreGroupCount.replace("@ou_join@", "");
//        }
//
//        if (cadreGroup != null) {
//            nationalCadreGroupCount = insertCadreGroupPart(cadreGroup);
//        } else {
//            nationalCadreGroupCount = nationalCadreGroupCount.replace("@cadreGroup@", "");
//        }
        List<Indicator> indicatorList = new ArrayList();
        Database db = new Database();
        ResultSet rs = db.executeQuery(getIndicatorNames);
        log.info("Fetching ndicators");
        try {
            while (rs.next()) {
                Indicator indicator = new Indicator();
                indicator.setId(rs.getString("id"));
                indicator.setName(rs.getString("name"));
                indicator.setGroupId(rs.getString("groupId"));
                indicatorList.add(indicator);
            }
        } catch (SQLException ex) {
            log.error(ex);
        } finally {
            db.CloseConnection();
        }
        return indicatorList;
    }

    public List<IndicatorGoup> getIndicatorGroups(String pe, String ou, String id) throws DslException {
        List<IndicatorGoup> indicatorGroupList = new ArrayList();

        Element ele = cache.get(CacheKeys.indicatorGroup);

        if (ele == null) {
            Database db = new Database();
            ResultSet rs = db.executeQuery(getIndicatorGroups);
            log.info("Fetching indicator groups");
            try {
                int count = 1;
                while (rs.next()) {
                    IndicatorGoup indGroup=new IndicatorGoup();
                    indGroup.setId(rs.getString("name"));
                    indGroup.setName(rs.getString("id"));
                    indicatorGroupList.add(indGroup);
                }
                cache.put(new Element(CacheKeys.indicatorGroup, indicatorGroupList));
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                db.CloseConnection();
            }
        } else {
            long startTime = System.nanoTime();
            indicatorGroupList = (List<IndicatorGoup>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return indicatorGroupList;
    }
}
