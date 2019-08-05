/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.dao;

import com.healthit.dslservice.DslException;
import com.healthit.dslservice.Filter;
import com.healthit.dslservice.dto.KephLevel;
import com.healthit.dslservice.dto.adminstrationlevel.Facility;
import com.healthit.dslservice.dto.dhis.Indicator;
import com.healthit.dslservice.dto.ihris.CadreAllocation;
import com.healthit.dslservice.util.CacheKeys;
import com.healthit.dslservice.util.Database;
import com.healthit.dslservice.util.DslCache;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
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
    private StringBuilder getALlFaciltiesBuilder = new StringBuilder(
            "Select \"Indicator ID\" as id,\"Indicator name\" as indicatorName,code as kmflcode,value as kpivalue,"
            + " \"Group name\" as indicatorGroup, startdate, enddate "
            + "from vw_mohdsl_dhis where code is not null "
    );

    private String getIndicatorNames = "select DISTINCT \"Indicator name\" as indicatorName,\"Group name\" as indicatorGroup from vw_mohdsl_dhis where code is not null order by indicatorName";
    private String getIndicatorGroups = "select DISTINCT(\"Group name\") as indicatorGroup from vw_mohdsl_dhis where code is not null order by indicatorGroup";
    
    private Map<String,String> groupTable=new HashMap();
    
    Cache cache = DslCache.getCache();

    public List<Indicator> getIndicators(
            List<String> idicatorName,
            List<String> indicatorGroup,
            String startDate,
            String endDate,
            List<String> mflCode
    ) throws DslException {

        if (idicatorName != null) {
            if (!idicatorName.isEmpty()) {
                Iterator i = idicatorName.iterator();
                String append = "";
                int count = 0;
                while (i.hasNext()) {
                    if (count == 0) {
                        append = " and indicatorName=" + (String) i.next() + " ";
                        count = count + 1;
                    } else {
                        append = " or indicatorName=" + (String) i.next() + " ";
                    }
                }
                getALlFaciltiesBuilder.append(append);
            }
        }

        if (indicatorGroup != null) {
            if (!indicatorGroup.isEmpty()) {
                Iterator i = indicatorGroup.iterator();
                String append = "";
                int count = 0;
                while (i.hasNext()) {
                    if (count == 0) {
                        append = " and indicatorGroup=" + (String) i.next() + " ";
                        count = count + 1;
                    } else {
                        append = " or indicatorGroup=" + (String) i.next() + " ";
                    }
                }
                getALlFaciltiesBuilder.append(append);
            }
        }

        if (mflCode != null) {
            if (!mflCode.isEmpty()) {
                Iterator i = mflCode.iterator();
                String append = "";
                int count = 0;
                while (i.hasNext()) {
                    if (count == 0) {
                        append = " and code=" + (String) i.next() + " ";
                        count = count + 1;
                    } else {
                        append = " or code=" + (String) i.next() + " ";
                    }
                }
                getALlFaciltiesBuilder.append(append);
            }
        }

        Filter filter = new Filter();
        String orderBy = " order by startdate,enddate ";
        getALlFaciltiesBuilder.append(orderBy + " OFFSET " + filter.getOffset() + " " + " LIMIT " + filter.getLimit());

        List<Indicator> indicatorList = new ArrayList();
        Database db = new Database();
        ResultSet rs = db.executeQuery(getALlFaciltiesBuilder.toString());
        log.info("Fetching ndicators");
        try {
            while (rs.next()) {
                Indicator indicator = new Indicator();
                indicator.setEndDate(rs.getString("enddate"));
                indicator.setId(rs.getString("id"));
                indicator.setIdicatorName(rs.getString("indicatorName"));
                indicator.setIndicatorGroup(rs.getString("indicatorGroup"));
                indicator.setIndicatorValue(rs.getString("kpivalue"));
                indicator.setMflCode(rs.getString("kmflcode"));
                indicator.setStartDate(rs.getString("startdate"));
                indicatorList.add(indicator);
            }
        } catch (SQLException ex) {
            log.error(ex);
        } finally {
            db.CloseConnection();
        }
        return indicatorList;
    }

    public List<Map<String, String>> getIndicatorNames() throws DslException {
        List<Map<String, String>> indicatorNames = new ArrayList();
        Element ele = cache.get(CacheKeys.indicatorName);

        if (ele == null) {
            Database db = new Database();
            ResultSet rs = db.executeQuery(getIndicatorNames);
            log.info("Fetching indicator Names");
            try {
                int count=1;
                
                //populate groupTable if empty
                if(groupTable.isEmpty()){
                     Element indicatorGroupTableEle = cache.get(CacheKeys.indicatorGroupTable);
                     groupTable = (Map<String,String>) indicatorGroupTableEle.getObjectValue();
                     if(groupTable.isEmpty()){
                         getIndicatorGroups();
                     }
                }
                while (rs.next()) {
                    Map<String, String> indicatorName = new HashMap();
                    indicatorName.put("name", rs.getString("indicatorName"));  // put("name",rs.getString("indicatorName"))
                    indicatorName.put("id", Integer.toString(count));
                    indicatorName.put("groupId", groupTable.get(rs.getString("indicatorGroup")));
                    indicatorNames.add(indicatorName);
                    count+=1;
                }
                cache.put(new Element(CacheKeys.indicatorName, indicatorNames));
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                db.CloseConnection();
            }
        } else {
            long startTime = System.nanoTime();
            indicatorNames = (List<Map<String, String>>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return indicatorNames;
    }

    public List<Map<String, String>> getIndicatorGroups() throws DslException {
        List<Map<String, String>> indicatorNames = new ArrayList();

        Element ele = cache.get(CacheKeys.indicatorGroup);
        
        if (ele == null) {
            Database db = new Database();
            ResultSet rs = db.executeQuery(getIndicatorGroups);
            log.info("Fetching indicator groups");
            try {
                int count=1;
                while (rs.next()) {
                    Map<String, String> indicatorGroupName = new HashMap();
                    groupTable.put(rs.getString("indicatorGroup"), Integer.toString(count));
                    count+=1;
                    indicatorGroupName.put("name", rs.getString("indicatorGroup"));
                    indicatorGroupName.put("id", Integer.toString(count));
                    indicatorNames.add(indicatorGroupName);
                }
                cache.put(new Element(CacheKeys.indicatorGroup, indicatorNames));
                cache.put(new Element(CacheKeys.indicatorGroupTable, groupTable));
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                db.CloseConnection();
            }
        }else {
            long startTime = System.nanoTime();
            indicatorNames = (List<Map<String, String>>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return indicatorNames;
    }
}
