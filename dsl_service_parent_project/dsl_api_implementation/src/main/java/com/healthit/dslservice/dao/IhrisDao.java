/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.dao;

import com.healthit.dslservice.DslException;
import com.healthit.dslservice.dto.ihris.Cadre;
import com.healthit.dslservice.dto.ihris.CadreAllocation;
import com.healthit.dslservice.dto.ihris.CadreGroup;
import com.healthit.dslservice.message.Message;
import com.healthit.dslservice.message.MessageType;
import com.healthit.dslservice.util.CacheKeys;
import com.healthit.dslservice.util.Database;
import com.healthit.dslservice.util.DslCache;
import com.healthit.dslservice.util.RequestParameters;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.log4j.Logger;

/**
 *
 * @author duncan
 */
public class IhrisDao {

    final static Logger log = Logger.getLogger(IhrisDao.class);
    Cache cache = DslCache.getCache();

    private String aLlCadreGroup = "Select cadreid,cadrename from dim_ihris_cadre";
    private String aLlCadre = "Select dataelementid as id,dataelementname as cadrename, cadreid as cadre_group_id from dim_ihris_dataelement";
    private String cadresByGroup = "Select dataelementid as id,dataelementname as cadrename, cadreid as cadre_group_id from dim_ihris_dataelement where cadreid=?";

    private String nationalCadreGroupCount = "select count(*) as cadre_count,cadre_group.cadreid as id,cadre_group.cadrename as cadre from fact_ihris ihris \n"
            + "inner join dim_ihris_dataelement cadree on cast(cadree.dataelementid as varchar) = cast(ihris.job_category_id as varchar) \n"
            + "inner join dim_ihris_cadre cadre_group on cadre_group.cadreid=cadree.cadreid  @ou_join@\n"
            + "where @pe@ @ou@ @cadreGroup@ \n"
            + "group by cadre,cadre_group.cadreid order by cadre desc";

    private String nationalCadreCount = "select count(*) as cadre_count,cadree.dataelementid as id,cadree.dataelementname as cadre from fact_ihris ihris \n"
            + "inner join dim_ihris_dataelement cadree on cast(cadree.dataelementid as varchar) = cast(ihris.job_category_id as varchar) @ou_join@\n"
            + "where @pe@ @ou@ @cadre@ \n"
            + "group by cadre,cadree.dataelementid order by cadre desc";

    private boolean appendAnd = false;
    private String getOrgUnit = "select dhis_organisation_unit_name as name,dhis_organisation_unit_id as id from common_organisation_unit where dhis_organisation_unit_id=?";

    /**
     *
     * @param pe period from http request
     * @return qeuery string appended with period patameter
     * @throws DslException
     */
    private String insertPeriodPart(String pe, String sqlString) throws DslException {
        log.info("insert period part " + pe);
        String periodString = "";
        RequestParameters.isValidPeriod(pe);

        if (pe.trim().length() == 4) {
            String replacement = " ihris.hire_date<='@end_year@-12-31' ".replace("@end_year@", pe);
            periodString = replacement;
        } else {
            String paramYear = pe.substring(0, 4);
            String paramMonth = pe.substring(4, 6);
            String replacement = " ihris.hire_date<='@end_year@-@month@-31' ".replace("@end_year@", paramYear).replace("@month@", paramMonth);
            periodString = replacement;
        }
        log.debug("replacement string " + periodString);
        log.debug("insert period part query " + sqlString);
        sqlString = sqlString.replace("@pe@", periodString);
        log.debug("insert period part final query " + sqlString);
        return sqlString;

    }

    /**
     *
     * @param ou organisation unit id from http request
     * @return qeuery string appended with org unit patameter
     * @throws DslException
     */
    private String insertOrgUntiPart(String ou, String level, String sqlString) throws DslException {
        String join = "";
        log.info("org unti level: " + level);
        if ("facility".equals(level.trim())) {
            join = " inner join common_organisation_unit com_org on cast(com_org.mfl_code as varchar) = cast(ihris.mfl_code as varchar) ";
        } else if ("ward".equals(level.trim())) {
            join = "inner join facilities_facility ff on cast(ff.code as varchar) =cast(ihris.mfl_code as varchar)"
                    + "inner join common_organisation_unit com_org on com_org.mfl_code=ff.ward_sk";
        } else if ("subcounty".equals(level.trim())) {
            join = "inner join facilities_facility ff on cast(ff.code as varchar) =cast(ihris.mfl_code as varchar) "
                    + "inner join common_ward com_ward on cast(com_ward.ward_sk as varchar) = cast(ff.ward_sk as varchar)"
                    + "inner join common_organisation_unit com_org on cast(com_org.mfl_code as varchar) = cast(com_ward.constituency_sk as varchar)";
        } else if ("county".equals(level.trim())) {
            join = "inner join facilities_facility ff on cast(ff.code as varchar) =cast(ihris.mfl_code as varchar) "
                    + "inner join common_ward com_ward on cast(com_ward.ward_sk as varchar)= cast(ff.ward_sk as varchar)"
                    + "inner join common_constituency com_consti on cast(com_ward.constituency_sk as varchar) =  cast(com_consti.constituency_sk as varchar)"
                    + "inner join common_county com_county on cast(com_county.id as varchar) = cast(com_consti.county_id as varchar)"
                    + "inner join common_organisation_unit com_org on cast(com_org.mfl_code as varchar) = cast(com_county.code as varchar)";
        }
        log.debug("The oug unit join query: " + join);
        sqlString = sqlString.replace("@ou_join@", join);
        log.debug("the query after replace: " + join);
        // @ou_join@
        String replacement;
        if (appendAnd) {
            replacement = " and com_org.dhis_organisation_unit_id= " + ou;
        } else {
            replacement = " com_org.dhis_organisation_unit_id=" + ou;
            appendAnd = true;
        }
        sqlString = sqlString.replace("@ou@", replacement);

        return sqlString;
    }

    /**
     *
     * @param ou organisation unit id from http request
     * @return qeuery string appended with org unit patameter
     * @throws DslException
     */
    private String insertCadreGroupPart(String cadreGroup) throws DslException {
        String replacement;
        if (appendAnd) {
            replacement = " and cadre_group.cadreid = " + cadreGroup;
        } else {
            replacement = " cadre_group.cadreid =" + cadreGroup;
            appendAnd = true;
        }
        nationalCadreGroupCount = nationalCadreGroupCount.replace("@cadreGroup@", replacement);

        return nationalCadreGroupCount;
    }

    /**
     *
     * @param ou organisation unit id from http request
     * @return qeuery string appended with org unit patameter
     * @throws DslException
     */
    private String insertCadrePart(String cadre) throws DslException {
        log.info("insert cadre part " + cadre);
        String replacement;
        if (appendAnd) {
            replacement = " and cadree.dataelementid = " + cadre;
        } else {
            replacement = " cadree.dataelementid =" + cadre;
            appendAnd = true;
        }
        nationalCadreCount = nationalCadreCount.replace("@cadre@", replacement);

        return nationalCadreCount;
    }

    /**
     * Allocation of cadres by cadre groups
     *
     * @param pe semi colon separated period
     * @param ou semi colon separated org unit ids
     * @param cadre semi colon separated cadre ids
     * @param cadreGroup semi colon separated cadreGroup ids
     * @return cadre allocation objects
     * @throws DslException
     */
    public Map<String, Object> getCadreGroupAllocation(String pe, String ou, String cadreGroup) throws DslException {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        Map<String, Object> resultsetEnvelop = new HashMap();
        log.info(cadreGroup);
        if (pe != null) {
            nationalCadreGroupCount = insertPeriodPart(pe, nationalCadreGroupCount);
            appendAnd = true;
        } else {
            String curYr = Integer.toString(currentYear);
            nationalCadreGroupCount = insertPeriodPart(curYr, nationalCadreGroupCount);
            appendAnd = true;
        }

        if (ou != null) {
            if (!ou.equals("18")) {
                String level = RequestParameters.getOruntiLevel(ou);
                nationalCadreGroupCount = insertOrgUntiPart(ou, level, nationalCadreGroupCount);
            } else {
                nationalCadreGroupCount = nationalCadreGroupCount.replace("@ou@", "");
                nationalCadreGroupCount = nationalCadreGroupCount.replace("@ou_join@", "");
            }
        } else {
            nationalCadreGroupCount = nationalCadreGroupCount.replace("@ou@", "");
            nationalCadreGroupCount = nationalCadreGroupCount.replace("@ou_join@", "");
        }

        if (cadreGroup != null) {
            nationalCadreGroupCount = insertCadreGroupPart(cadreGroup);
        } else {
            nationalCadreGroupCount = nationalCadreGroupCount.replace("@cadreGroup@", "");
        }

        List<CadreAllocation> cadreAllocationList = new ArrayList();
        Database db = new Database();

        ResultSet rs = db.executeQuery(nationalCadreGroupCount);
        log.info("Fetching cadre groups");
        try {
            while (rs.next()) {
                CadreAllocation cadreAllocation = new CadreAllocation();
                cadreAllocation.setCadre(rs.getString("cadre"));
                cadreAllocation.setCadreCount(rs.getString("cadre_count"));
                cadreAllocation.setId(rs.getString("id"));
                cadreAllocation.setPeriod(pe);
                cadreAllocationList.add(cadreAllocation);
            }
        } catch (SQLException ex) {
            log.error(ex);
        } finally {
            db.CloseConnection();
        }

        Map<String, Object> metadata = new HashMap();
        Map<String, Object> orgdetail = new HashMap();
        if (ou != null) {
            if (ou.equals("18")) {
                metadata.put("orgunitname", "Kenya");
                metadata.put("orgunitid", "18");
            } else {
                orgdetail = getOrgUnitDetails(ou);
                metadata.put("orgunitname", orgdetail.get("name"));
                metadata.put("orgunitid", orgdetail.get("id"));
            }
        } else {
            metadata.put("orgunitname", "Kenya");
            metadata.put("orgunitid", "18");
        }

        resultsetEnvelop.put("data", cadreAllocationList);
        resultsetEnvelop.put("metadata", metadata);
        return resultsetEnvelop;

    }

    private List<CadreAllocation> formatCadreAllocationMonthly(ResultSet rs, String requestedPeriod) throws SQLException {

        if (rs.next()) {
            rs.beforeFirst();
        } else {
            return null;
        }

        Map<String, CadreAllocation> yearMonthCadreAllocMap = new LinkedHashMap();
        List<String> availableMonthCadres = new ArrayList();
        while (rs.next()) {
            CadreAllocation cadreAllocation = new CadreAllocation();
            cadreAllocation.setCadre(rs.getString("cadre"));
            String cCount = rs.getString("cadre_count");
            String month = rs.getString("month");
            String year = requestedPeriod;
            String yearMonth = year + month;
            cadreAllocation.setCadreCount(cCount);
            cadreAllocation.setId(rs.getString("id"));
            cadreAllocation.setPeriod(yearMonth);

            //fix previously missing cadre allocations
            if (month.equals('1')) {
                log.info("ihris filler month equal to one");
                availableMonthCadres.add(yearMonth);
            } else if (availableMonthCadres.size() == 0) {
                //get top value in array
                log.info("ihris filler month zero");
                log.debug("Current month " + Integer.parseInt(month));
                int missingCadreCountMonths = Integer.parseInt(month) - 1;
                for (int x = missingCadreCountMonths; x >= 1; x--) {
                    log.debug(x);
                    try {
                        CadreAllocation missingCadreAlloc = (CadreAllocation) cadreAllocation.clone();
                        log.debug(year + Integer.toString(x));
                        missingCadreAlloc.setPeriod(year + Integer.toString(x));

                        yearMonthCadreAllocMap.put(year + Integer.toString(x), missingCadreAlloc);
                    } catch (CloneNotSupportedException ex) {
                        log.error(ex);
                    }
                }
                availableMonthCadres.add(yearMonth);
            } else {
                log.info("ihris filler month has values");
                //get top value in array
                String topYearMonth = availableMonthCadres.get(availableMonthCadres.size() - 1);
                int missingCadreCountMonths = Integer.parseInt(yearMonth.substring(4)) - Integer.parseInt(topYearMonth.substring(4));
                for (int x = 1; x < missingCadreCountMonths; x++) {

                    CadreAllocation previousAvailableCadreAlloc = yearMonthCadreAllocMap.get(topYearMonth);
                    int newMnth = Integer.parseInt(topYearMonth.substring(4)) + x;
                    String newYearMnt = topYearMonth.substring(0, 4) + newMnth;

                    try {
                        CadreAllocation missingCadreAlloc = (CadreAllocation) previousAvailableCadreAlloc.clone();
                        missingCadreAlloc.setPeriod(newYearMnt);
                        yearMonthCadreAllocMap.put(newYearMnt, missingCadreAlloc);
                    } catch (CloneNotSupportedException ex) {
                        log.error(ex);
                    }

                }
                availableMonthCadres.add(yearMonth);
            }
            // store current cadre allocation from result set
            if (yearMonthCadreAllocMap.containsKey(yearMonth)) {
                CadreAllocation cAllocation = yearMonthCadreAllocMap.get(yearMonth);
                String cCountMap = cAllocation.getCadreCount();
                if (Integer.parseInt(cCount) > Integer.parseInt(cCountMap)) {
                    yearMonthCadreAllocMap.get(yearMonth).setCadreCount(cCount);
                }
            } else {
                yearMonthCadreAllocMap.put(yearMonth, cadreAllocation);
            }

        }
        log.info("yearMonthCadreAllocMap.size(): " + yearMonthCadreAllocMap.size());
        //fill remaining months if not 12 months there
        if (yearMonthCadreAllocMap.size() < 12) {
            log.info("ihris filler month less that 12 months");
            //get top value in array
            String topYearMonth = availableMonthCadres.get(availableMonthCadres.size() - 1);
            int missingCadreCountMonths = 12 - yearMonthCadreAllocMap.size();
            for (int x = 1; x <= missingCadreCountMonths; x++) {
                log.debug("filler loop " + x);
                log.debug("top year month " + topYearMonth);
                CadreAllocation previousAvailableCadreAlloc = yearMonthCadreAllocMap.get(topYearMonth);
                int newMnth = Integer.parseInt(topYearMonth.substring(4)) + x;
                String newYearMnt = topYearMonth.substring(0, 4) + newMnth;

                log.debug("new month " + Integer.parseInt(topYearMonth) + x);
                try {
                    CadreAllocation missingCadreAlloc = (CadreAllocation) previousAvailableCadreAlloc.clone();
                    missingCadreAlloc.setPeriod(newYearMnt);
                    yearMonthCadreAllocMap.put(newYearMnt, missingCadreAlloc);
                } catch (CloneNotSupportedException ex) {
                    log.error(ex);
                }
            }
        }
        List<CadreAllocation> cadreAllocationList = new ArrayList(yearMonthCadreAllocMap.values());
        return cadreAllocationList;
    }

    /**
     * Creates cadreCountQuery if the period types passed is monthly
     *
     * @param pe period to query
     * @param ou organisation unit id to query
     * @param cadre cadre id to query
     * @param periodtype period type to query
     * @param cadreQueryString query string to append query parameters
     * @return
     * @throws DslException
     */
    private String createsCadreCountQueryIfMonthlyTypePeriod(String pe, String ou, String cadre, String periodtype, boolean isRecurse) throws DslException {
        RequestParameters.isValidPeriod(pe);
        String paramYear;
        String periodFilter;
        String ordering = isRecurse ? "desc limit 1" : "asc";
        nationalCadreCount = "select id,cadre,cadre_count, date_part('year', hire_date) as year,date_part('month', hire_date) as month from"
                + " (select cadree.dataelementid as id,cadree.dataelementname as cadre, to_char(hire_date, 'YYYY-MM')  as yearmonth,hire_date,\n"
                + "count(*) OVER(ORDER BY hire_date) AS cadre_count \n"
                + "from fact_ihris ihris inner join dim_ihris_dataelement cadree on cast(cadree.dataelementid as varchar) = cast(ihris.job_category_id as varchar) \n"
                + " @ou_join@ \n"
                + "@cadre@ @ou@ ) x @pe@ group by hire_date,cadre,yearmonth,id,cadre_count order by year,month " + ordering;
//  
        log.debug("period not null: " + pe);
        if (pe.length() != 0 && pe.length() >= 4) {
            log.debug("period length not 0");
            paramYear = pe.substring(0, 4);
            periodFilter = " where date_part('year', hire_date)='" + paramYear + "' ";
            nationalCadreCount = nationalCadreCount.replace("@pe@", periodFilter);
            log.debug(periodFilter);
            log.debug(nationalCadreCount);
        } else {
            log.debug("period lenght less than 4 or is empty");
            periodFilter = " where date_part('year', hire_date)='" + pe + "' ";
            nationalCadreCount = nationalCadreCount.replace("@pe@", periodFilter);
        }

        if (cadre != null) {
            log.debug("cadre is not null: " + cadre);
            String cadreFilter = " where cadree.dataelementid='" + cadre + "' ";
            nationalCadreCount = nationalCadreCount.replace("@cadre@", cadreFilter);
            appendAnd = true;
        } else {
            log.debug("cadre is null: " + cadre);
            nationalCadreCount = nationalCadreCount.replace("@cadre@", " ");
        }

        pe = null;
        cadre = null;
        return nationalCadreCount;
    }

    /**
     * Appends query parameters to query
     *
     * @param pe period to query
     * @param ou organisation unit id to query
     * @param cadre cadre id to query
     * @param periodtype period type to query
     * @param cadreQueryString query string to append query parameters
     * @return
     * @throws DslException
     */
    private String appendCadresCountParts(String pe, String ou, String cadre, String periodtype) throws DslException {
        log.info("Appending cadres count query parts");
        if (pe != null) {
            nationalCadreCount = insertPeriodPart(pe, nationalCadreCount);
            appendAnd = true;
        } else {
            nationalCadreCount = nationalCadreCount.replace("@pe@", "");
        }

        if (cadre != null) {
            nationalCadreCount = insertCadrePart(cadre);
        } else {
            nationalCadreCount = nationalCadreCount.replace("@cadre@", "");
        }

        if (ou != null) {

            if (!ou.equals("18")) {
                String level = RequestParameters.getOruntiLevel(ou);
                nationalCadreCount = insertOrgUntiPart(ou, level, nationalCadreCount);
            } else {
                nationalCadreCount = nationalCadreCount.replace("@ou@", "");
                nationalCadreCount = nationalCadreCount.replace("@ou_join@", "");
            }

        } else {
            nationalCadreCount = nationalCadreCount.replace("@ou@", "");
            nationalCadreCount = nationalCadreCount.replace("@ou_join@", "");
        }

        return nationalCadreCount;
    }

    private Map<String, Object> getOrgUnitDetails(String ouid) {
        Map<String, Object> orgUnit = new HashMap();
        Database db = null;
        try {
            List paramsList = new ArrayList();
            Map<String, String> ouidParam = new HashMap();
            ouidParam.put("value", ouid);
            ouidParam.put("type", "integer");
            paramsList.add(ouidParam);
            db = new Database();
            ResultSet rs = db.executeQuery(getOrgUnit, paramsList);

            if (rs.next()) {
                orgUnit.put("id", rs.getString("id"));
                orgUnit.put("name", rs.getString("name"));
            }

        } catch (DslException ex) {
            log.error(ex);
        } catch (SQLException ex) {
            log.error(ex);
        } finally {
            try {
                db.CloseConnection();
            } catch (Exception ex) {
            }
        }
        return orgUnit;
    }

    /**
     *
     * @param pe semi colon separated period
     * @param ou semi colon separated org unit ids
     * @param cadre semi colon separated cadre ids
     * @return cadre allocation objects
     * @throws DslException
     */
    public Map<String, Object> getCadreAllocation(String pe, String ou, String cadre, String periodtype) throws DslException {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);
        if (pe == null) {
            pe = Integer.toString(currentYear);
        }
        if (periodtype != null && periodtype.equals("monthly")) {
            if (pe.length() > 4) {
                pe = pe.substring(0, 5);
            }
            nationalCadreCount = createsCadreCountQueryIfMonthlyTypePeriod(pe, ou, cadre, periodtype, false);
        }
        String requestedPeriod = pe;

        nationalCadreCount = appendCadresCountParts(pe, ou, cadre, periodtype);
        Map<String, Object> resultsetEnvelop = new HashMap();
        List<CadreAllocation> cadreAllocationList = new ArrayList();
        log.info("Fetching cadre allocations");
        Database db = new Database();
        ResultSet rs = db.executeQuery(nationalCadreCount);

        try {
            if (periodtype != null && periodtype.equals("monthly")) {
                log.debug("loop till last cadre count");
                cadreAllocationList = formatCadreAllocationMonthly(rs, requestedPeriod);
                log.debug(cadreAllocationList);
                while (cadreAllocationList == null) {
                    pe = Integer.toString((Integer.parseInt(pe) - 1));
                    if (pe.equals("2008")) {
                        Message msg = new Message();
                        msg.setMessageType(MessageType.MISSING_DATA);
                        msg.setMesageContent("No data for this orunit on requested this period");
                        throw new DslException(msg);
                    }
                    nationalCadreCount = createsCadreCountQueryIfMonthlyTypePeriod(pe, ou, cadre, periodtype, true);
                    nationalCadreCount = appendCadresCountParts(pe, ou, cadre, periodtype);
                    rs = db.executeQuery(nationalCadreCount);
                    cadreAllocationList = formatCadreAllocationMonthly(rs, requestedPeriod);
                }
            } else {
                while (rs.next()) {
                    CadreAllocation cadreAllocation = new CadreAllocation();
                    cadreAllocation.setCadre(rs.getString("cadre"));
                    cadreAllocation.setCadreCount(rs.getString("cadre_count"));
                    cadreAllocation.setId(rs.getString("id"));
                    cadreAllocation.setPeriod(pe);
                    cadreAllocationList.add(cadreAllocation);
                }
            }

        } catch (SQLException ex) {
            log.error(ex);
        } finally {
            db.CloseConnection();
        }
        Map<String, Object> metadata = new HashMap();
        Map<String, Object> orgdetail = new HashMap();
        if (ou.equals("18")) {
            metadata.put("orgunitname", "Kenya");
            metadata.put("orgunitid", "18");
        } else {
            orgdetail = getOrgUnitDetails(ou);
            metadata.put("orgunitname", orgdetail.get("name"));
            metadata.put("orgunitid", orgdetail.get("id"));
        }

        resultsetEnvelop.put("data", cadreAllocationList);
        resultsetEnvelop.put("metadata", metadata);
        return resultsetEnvelop;
    }

    public List<CadreGroup> getAllCadresGroup() throws DslException {
        List<CadreGroup> cadreGroupList = new ArrayList();
        Element ele = cache.get(CacheKeys.cadreGroups);
        if (ele == null) {
            long startTime = System.nanoTime();
            Database db = new Database();
            ResultSet rs = db.executeQuery(aLlCadreGroup);
            log.info("Fetching cadre groups");
            try {
                while (rs.next()) {
                    CadreGroup cadreGroup = new CadreGroup();
                    cadreGroup.setId(rs.getString("cadreid"));
                    cadreGroup.setName(rs.getString("cadrename"));
                    cadreGroupList.add(cadreGroup);
                }
                cache.put(new Element(CacheKeys.cadreGroups, cadreGroupList));
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                db.CloseConnection();
            }
        } else {
            long startTime = System.nanoTime();
            cadreGroupList = (List<CadreGroup>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return cadreGroupList;
    }

    public List<Cadre> getAllCadres() throws DslException {
        List<Cadre> cadreList = new ArrayList();
        Element ele = cache.get(CacheKeys.cadres);
        if (ele == null) {
            Database db = new Database();
            ResultSet rs = db.executeQuery(aLlCadre);
            log.info("Fetching cadres");
            try {
                while (rs.next()) {
                    Cadre cadre = new Cadre();
                    cadre.setId(rs.getString("id"));
                    cadre.setName(rs.getString("cadrename"));
                    cadre.setCadreGroupId(rs.getString("cadre_group_id"));
                    cadreList.add(cadre);
                }
                cache.put(new Element(CacheKeys.cadres, cadreList));
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                db.CloseConnection();
            }
        } else {
            long startTime = System.nanoTime();
            cadreList = (List<Cadre>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return cadreList;
    }

    public List<Cadre> getCadresByGroup(int groupId) throws DslException {
        List<Cadre> cadreList = new ArrayList();

        Element ele = cache.get("cadresByGroup" + groupId);
        if (ele == null) {
            try {
                Database db = new Database();
                Connection conn = db.getConn();
                PreparedStatement ps = conn.prepareStatement(cadresByGroup);
                ps.setInt(1, groupId);
                ResultSet rs = ps.executeQuery();
                log.info("Fetching cadres");
                try {
                    while (rs.next()) {
                        Cadre cadre = new Cadre();
                        cadre.setId(rs.getString("id"));
                        cadre.setName(rs.getString("cadrename"));
                        cadre.setCadreGroupId(rs.getString("cadre_group_id"));
                        cadreList.add(cadre);
                    }
                    cache.put(new Element("cadresByGroup" + groupId, cadreList));
                } catch (SQLException ex) {
                    log.error(ex);
                } finally {
                    db.CloseConnection();
                }
            } catch (SQLException ex) {
                java.util.logging.Logger.getLogger(IhrisDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            long startTime = System.nanoTime();
            cadreList = (List<Cadre>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return cadreList;
    }
}
