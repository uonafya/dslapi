/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.dao;

import com.healthit.dslservice.DslException;
import com.healthit.dslservice.dto.dhis.Indicator;
import com.healthit.dslservice.dto.dhis.IndicatorGoup;
import com.healthit.dslservice.dto.dhis.IndicatorValue;
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
    private String getIndicatorGroup = "SELECT \"Indicator ID\" as id, indicatorname as name, indicatorgroupid as groupId\n"
            + "FROM public.vw_indicator_to_indicatorgroup where indicatorgroupid=?";
    private String getIndicatorGroups = "SELECT DISTINCT indicatorgroupid as id, group_name as name\n"
            + "FROM public.vw_indicator_to_indicatorgroup;";

    private String getKPIWholeYear = "select ROUND( kpivalue, 2 ) as value,\"Indicator name\" as indicator_name,cast(to_char(startdate, 'YYYY') as int) as year \n"
            + ",cast(to_char(startdate, 'MM') as int) as month, \"Indicator ID\" as id,\"Org unit id\" as ouid,dhis.\"Organisation Unit Name\" as ouname \n"
            + "from vw_mohdsl_dhis_indicators dhis "
            + "where @pe@"
            + " @id@ "
            + "@ouid@"
            + "group by year,month,\"Indicator name\",kpivalue,\"Org unit id\",ouname,id";

    private Map<String, String> groupTable = new HashMap();

    Cache cache = DslCache.getCache();

    private boolean appendAnd = false;

    /**
     *
     * @param pe period from http request
     * @return qeuery string appended with period patameter
     * @throws DslException
     */
    private String insertPeriodPart(String pe, String sqlString) throws DslException {
        String periodYearSpanSql = " startdate between to_date(( @start_year@  || '-'|| 1 || '-'||'1'),'YYYY-MM-DD') and to_date((@end_year@ || '-'|| 12 || '-'||'31'),'YYYY-MM-DD') ";
        String periodPerMontSql = " startdate between to_date(( @start_year@  || '-'|| @month@ || '-'||'1'),'YYYY-MM-DD') and to_date((@end_year@ || '-'|| @month@ || '-'||'31'),'YYYY-MM-DD') ";

        String periodString = "";
        RequestParameters.isValidPeriod(pe);
        if (pe.length() == 4) {
            String replacement = periodYearSpanSql.replace("@end_year@", pe).replace("@start_year@", pe);
            periodString = replacement;
        } else {
            String paramYear = pe.substring(0, 4);
            String paramMonth = pe.substring(4, 6);
            String replacement = periodYearSpanSql.replace("@end_year@", paramYear).replace("@month@", paramMonth);
            periodString = replacement;
        }

        sqlString = sqlString.replace("@pe@", periodString);
        return sqlString;

    }

    /**
     *
     * @param ou organisation unit id from http request
     * @return qeuery string appended with org unit patameter
     * @throws DslException
     */
    private String insertOrgUntiPart(String ouid, String sqlString) throws DslException {

        String replacement;
        if (appendAnd) {
            replacement = " and \"Org unit id\" in (@ouid@) ".replace("@ouid@", ouid);
        } else {
            replacement = " \"Org unit id\" in (@ouid@) ".replace("@ouid@", ouid);
            appendAnd = true;
        }
        sqlString = sqlString.replace("@ouid@", replacement);

        return sqlString;
    }

    /**
     *
     * @param ou organisation unit id from http request
     * @return qeuery string appended with org unit patameter
     * @throws DslException
     */
    private String insertIdPart(String id, String sqlString) throws DslException {
        String replacement;
        if (appendAnd) {
            replacement = " and \"Indicator ID\" in (@indicator@) ".replace("@indicator@", id);
        } else {
            replacement = "  \"Indicator ID\" in (@indicator@) ".replace("@indicator@", id);
        }
        sqlString = sqlString.replace("@id@", replacement);

        return sqlString;
    }

    /**
     *
     * @param ou organisation unit id from http request
     * @return qeuery string appended with org unit patameter
     * @throws DslException
     */
    private String insertCadrePart(String cadre, String sqlString) throws DslException {
        String replacement;
        if (appendAnd) {
            replacement = " and cadree.dataelementid = " + cadre;
        } else {
            replacement = " cadree.dataelementid =" + cadre;
            appendAnd = true;
        }
        sqlString = sqlString.replace("@cadre@", replacement);

        return sqlString;
    }

    /**
     * fetch indicator related data
     *
     * @param pe period
     * @param ou organisation unit
     * @param indicator indicator id
     * @return indicator list
     * @throws DslException
     */
    public List<Indicator> getIndicators() throws DslException {
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

    public List<IndicatorValue> getKPIValue(String pe, String ouid, String id) throws DslException {

        if (pe != null) {
            getKPIWholeYear = insertPeriodPart(pe, getKPIWholeYear);
            appendAnd = true;
        } else {
            int currentYear = Calendar.getInstance().get(Calendar.YEAR);
            getKPIWholeYear = insertPeriodPart(Integer.toString(currentYear), getKPIWholeYear); //current years' values
            appendAnd = true;
        }

        if (ouid != null) {

            getKPIWholeYear = insertOrgUntiPart(ouid, getKPIWholeYear);
        } else {

            if (appendAnd) {
                getKPIWholeYear = getKPIWholeYear.replace("@ouid@", " and \"Org unit id\"=18 "); //kenya (national id ) = 18
            } else {
                getKPIWholeYear = getKPIWholeYear.replace("@ouid@", " \"Org unit id\"=18 "); //kenya (national id ) = 18
                appendAnd = true;
            }

        }

        if (id != null) {
            getKPIWholeYear = insertIdPart(id, getKPIWholeYear);
        } else {
            Message msg=new Message();
            msg.setMesageContent("please add indicator id paramerter, '?id=xxx'");
            msg.setMessageType(MessageType.MISSING_PARAMETER_VALUE);
            throw new DslException(msg);
        }
        log.info("indicator query to run: " + getKPIWholeYear);
        List<IndicatorValue> kpiList = new ArrayList();
        Database db = new Database();
        ResultSet rs = db.executeQuery(getKPIWholeYear);
        log.info("Fetching KPI values");
        try {
            while (rs.next()) {
                IndicatorValue indicatorValue = new IndicatorValue();
                indicatorValue.setId(rs.getString("id"));
                indicatorValue.setName(rs.getString("indicator_name"));
                indicatorValue.setOuName(rs.getString("ouname"));
                indicatorValue.setOuid(rs.getString("ouid"));
                indicatorValue.setPe(rs.getString("year") + rs.getString("month"));
                indicatorValue.setValue(rs.getString("value"));
                kpiList.add(indicatorValue);
            }
        } catch (SQLException ex) {
            log.error(ex);
        } finally {
            db.CloseConnection();
        }
        return kpiList;
    }

    public List<Indicator> getIndicatorsByGroup(int groupId) throws DslException {
        List<Indicator> indicatorList = new ArrayList();

        Element ele = cache.get("indicatorByGroup" + groupId);
        if (ele == null) {
            try {
                Database db = new Database();
                Connection conn = db.getConn();
                PreparedStatement ps = conn.prepareStatement(getIndicatorGroup);
                ps.setInt(1, groupId);
                ResultSet rs = ps.executeQuery();
                log.info("Fetching cadres");
                try {
                    while (rs.next()) {
                        Indicator indicator = new Indicator();
                        indicator.setId(rs.getString("id"));
                        indicator.setName(rs.getString("name"));
                        indicator.setGroupId(rs.getString("groupId"));
                        indicatorList.add(indicator);
                    }
                    cache.put(new Element("indicatorByGroup" + groupId, indicatorList));
                } catch (SQLException ex) {
                    log.error(ex);
                } finally {
                    db.CloseConnection();
                }
            } catch (SQLException ex) {
                log.error(ex);
            }
        } else {
            long startTime = System.nanoTime();
            indicatorList = (List<Indicator>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
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
                    IndicatorGoup indGroup = new IndicatorGoup();
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
