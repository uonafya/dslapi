/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.dao;

import com.google.gson.Gson;
import com.healthit.DslException;
import com.healthit.dslservice.dto.dhis.Indicator;
import com.healthit.dslservice.dto.dhis.IndicatorGoup;
import com.healthit.dslservice.dto.dhis.IndicatorValue;
import com.healthit.message.Message;
import com.healthit.message.MessageType;
import com.healthit.dslservice.util.CacheKeys;
import com.healthit.dslservice.util.DatabaseSource;
import com.healthit.dslservice.util.DslCache;
import com.healthit.dslservice.util.RequestParameters;
import com.healthit.utils.string.DslStringUtils;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import org.apache.log4j.Logger;
import org.glassfish.jersey.client.ClientResponse;

/**
 *
 * @author duncan
 */
//@Component
public class DhisDao {

    final static Logger log = Logger.getLogger(DhisDao.class);

    private String getIndicatorNames = "SELECT \"Indicator ID\" as id, indicatorname as name,indicator_description as description, indicatorgroupid as groupId\n"
            + "FROM public.vw_indicator_to_indicatorgroup";
    private String getIndicatorGroup = "SELECT \"Indicator ID\" as id, indicatorname as name,indicator_description as description, indicatorgroupid as groupId\n"
            + "FROM public.vw_indicator_to_indicatorgroup where indicatorgroupid=?";
    private String getIndicatorGroups = "SELECT DISTINCT indicatorgroupid as id, group_name as name\n"
            + "FROM public.vw_indicator_to_indicatorgroup;";

    private String getKPIWholeYear = "select ROUND( kpivalue, 2 ) as value,\"Indicator name\" as indicator_name,\"Indicator description\" as description, "
            + "cast(to_char(startdate, 'YYYY') as int) as year \n"
            + ",cast(to_char(startdate, 'MM') as int) as month,_datecreated, lastupdated, \"Indicator ID\" as id,\"Org unit id\" as ouid,dhis.\"Organisation Unit Name\" as ouname \n"
            + "from vw_mohdsl_dhis_indicators dhis "
            + "where @pe@"
            + " @id@ "
            + "@ouid@"
            + " group by year,month,\"Indicator name\",\"Indicator description\",kpivalue,\"Org unit id\",ouname,id,_datecreated, lastupdated"
            + " order by month ";

    private String getOrgUnitLevelQry = "Select hierarchylevel from common_organisation_unit where dhis_organisation_unit_id=?";
    private String getOrgUnit = "select dhis_organisation_unit_name as name,dhis_organisation_unit_id as id from common_organisation_unit where dhis_organisation_unit_id=?";
    private String getIndicatorData = "select indicatorid as id, indicatorname as name, lastupdated, _datecreated, description from dim_dhis_indicator where indicatorid=?";

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
        Map<String, String> monthDays = new HashMap();
        monthDays.put("01", "31");
        monthDays.put("02", "28");
        monthDays.put("03", "31");
        monthDays.put("04", "30");
        monthDays.put("05", "31");
        monthDays.put("06", "30");
        monthDays.put("07", "31");
        monthDays.put("08", "31");
        monthDays.put("09", "30");
        monthDays.put("10", "31");
        monthDays.put("11", "30");
        monthDays.put("12", "31");

        String periodYearSpanSql = " startdate between to_date(( @start_year@  || '-'|| 1 || '-'||'1'),'YYYY-MM-DD') and to_date((@end_year@ || '-'|| 12 || '-'||'31'),'YYYY-MM-DD') ";
        String periodPerMontSql = " startdate between to_date(( @start_year@  || '-'|| @month@ || '-'||1),'YYYY-MM-DD') and to_date((@end_year@ || '-'|| @month@ || '-'||@day@),'YYYY-MM-DD') ";

        StringBuilder periodString = new StringBuilder();
        RequestParameters.isValidPeriod(pe);

        String[] periods = pe.split(";");

        for (int x = 0; x < periods.length; x++) {
            log.debug("periods replacer ===<");

            if (periods[x].length() == 4) {
                log.debug("periods replacer ===< 1");
                String replacement = periodYearSpanSql.replace("@end_year@", periods[x]).replace("@start_year@", periods[x]);
                if (x > 0) {
                    periodString.append(" or " + replacement);
                } else {
                    periodString.append(replacement);
                }

            } else {
                log.debug("periods replacer ===< 2");
                String paramYear = periods[x].substring(0, 4);
                String paramMonth = periods[x].substring(4, 6);
                String replacement = periodPerMontSql.replace("@end_year@", paramYear).replace("@month@", paramMonth).replace("@start_year@", paramYear).replace("@day@", monthDays.get(paramMonth));
                if (x > 0) {
                    periodString.append(" or " + replacement);
                } else {
                    periodString.append(replacement);
                }
            }
        }

        log.debug("periods gotten ===<");
        log.debug(periodString.toString());
        sqlString = sqlString.replace("@pe@", " (" + periodString.toString() + ") ");
        return sqlString;

    }

    private String indicatorIdList(String id) {

        String[] indicatorIds = id.split(";");
        StringBuilder indicatorParameters = new StringBuilder();

        for (int x = 0; x < indicatorIds.length; x++) {
            if (x == 0) {
                indicatorParameters.append(indicatorIds[x]);
            } else {
                indicatorParameters.append("," + indicatorIds[x]);
            }
        }
        return indicatorParameters.toString();
    }

    private String insertOrgUntiPart(String ouid, String sqlString) throws DslException {

        String replacement;
        String orgUnitParameters = DslStringUtils.toCommaSperated(ouid);

        log.info("Org units parameters : " + orgUnitParameters);

        if (appendAnd) {
            replacement = " and \"Org unit id\" in (@ouid@) ".replace("@ouid@", orgUnitParameters);
        } else {
            replacement = " \"Org unit id\" in (@ouid@) ".replace("@ouid@", orgUnitParameters);
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
    private String insertOrgUntiPartByLevel(String ouid, String level, String sqlString) throws DslException {

        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        int orgLevel = 1;
        try {
            conn = DatabaseSource.getConnection();
            ps = conn.prepareStatement(getOrgUnitLevelQry, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ps.setInt(1, Integer.parseInt(ouid));

            log.info("Query to run: " + ps.toString());
            rs = ps.executeQuery();

            while (rs.next()) {
                orgLevel = rs.getInt("hierarchylevel");
            }
        } catch (SQLException ex) {
            Message msg = new Message();
            msg.setMessageType(MessageType.SQL_QUERY_ERROR);
            msg.setMesageContent(ex.getMessage());
            throw new DslException(msg);
        } finally {
            DatabaseSource.close(rs);
            DatabaseSource.close(ps);
            DatabaseSource.close(conn);
        }
        try {
            Integer.parseInt(level);

        } catch (Exception ex) {
            Message msg = new Message();
            msg.setMessageType(MessageType.NUMBER_FORMAT_ERROR);
            msg.setMesageContent(ex.getMessage());
            throw new DslException(msg);
        }

        if (orgLevel == Integer.parseInt(level)) { //if equal no need to drill down
            return insertOrgUntiPart(ouid, sqlString);
        }
        if (orgLevel > Integer.parseInt(level)) {

            Message msg = new Message();
            msg.setMessageType(MessageType.ORGUNIT_LEVEL);
            msg.setMesageContent("Level should be below given orgunit requested");
            throw new DslException(msg);
        }

        int levelToDrill = Integer.parseInt(level) - orgLevel;
        log.debug("levels ========>");
        log.debug(Integer.parseInt(level));
        log.debug(orgLevel);
        log.debug(levelToDrill);
        String replacement;
        String innerSelect = "select \"Org unit id\" from vw_mohdsl_dhis_indicators where parentid";
        switch (levelToDrill) {
            case 1:

                if (appendAnd) {
                    replacement = " and \"Org unit id\" in (" + innerSelect + "=@ouid@) ".replaceAll("@ouid@", ouid);
                } else {
                    replacement = " \"Org unit id\" in (" + innerSelect + "=@ouid@) ".replaceAll("@ouid@", ouid);
                    appendAnd = true;
                }
                sqlString = sqlString.replace("@ouid@", replacement);

                return sqlString;

            case 2:
                if (appendAnd) {
                    replacement = " and \"Org unit id\" in (" + innerSelect + " in( " + innerSelect + "=@ouid@) ) ".replaceAll("@ouid@", ouid);
                } else {
                    replacement = " \"Org unit id\" in (" + innerSelect + " in( " + innerSelect + "=@ouid@) ) ".replaceAll("@ouid@", ouid);
                    appendAnd = true;
                }
                sqlString = sqlString.replace("@ouid@", replacement);

                return sqlString;

            case 3:
                if (appendAnd) {
                    replacement = " and \"Org unit id\" in (" + innerSelect + " in( " + innerSelect + " in(" + innerSelect + "=@ouid@)) ) ".replaceAll("@ouid@", ouid);
                } else {
                    replacement = " \"Org unit id\" in (" + innerSelect + " in( " + innerSelect + " in(" + innerSelect + "=@ouid@)) ) ".replaceAll("@ouid@", ouid);
                    appendAnd = true;
                }
                sqlString = sqlString.replace("@ouid@", replacement);

                return sqlString;

            case 4:
                if (appendAnd) {
                    replacement = " \"Org unit id\" in (" + innerSelect + " in( " + innerSelect + " in(" + innerSelect + "in(" + innerSelect + "=@ouid@))) ) ".replaceAll("@ouid@", ouid);
                } else {
                    replacement = " \"Org unit id\" in (" + innerSelect + " in( " + innerSelect + " in(" + innerSelect + "in(" + innerSelect + "=@ouid@))) ) ".replaceAll("@ouid@", ouid);
                    appendAnd = true;
                }
                sqlString = sqlString.replace("@ouid@", replacement);

                return sqlString;
            default:
                return "";
        }

    }

    /**
     *
     * @param id indicator id from http request
     * @return qeuery string appended with org unit patameter
     * @throws DslException
     */
    private String insertIdPart(String id, String sqlString) throws DslException {
        String replacement;

        String idParameters = indicatorIdList(id);

        log.info("indicator id parameters : " + idParameters);

        if (appendAnd) {
            replacement = " and \"Indicator ID\" in (@indicator@) ".replace("@indicator@", idParameters);
        } else {
            replacement = "  \"Indicator ID\" in (@indicator@) ".replace("@indicator@", idParameters);
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

        Element ele = cache.get(CacheKeys.indicatorName);

        if (ele == null) {

            PreparedStatement ps = null;
            ResultSet rs = null;
            Connection conn = null;

            try {
                conn = DatabaseSource.getConnection();
                ps = conn.prepareStatement(getIndicatorNames, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                log.info("Query to run: " + ps.toString());
                rs = ps.executeQuery();
                log.info("Fetching ndicators");

                while (rs.next()) {
                    Indicator indicator = new Indicator();
                    indicator.setId(rs.getString("id"));
                    indicator.setName(rs.getString("name"));
                    indicator.setGroupId(rs.getString("groupId"));
                    String desc = rs.getString("description");
                    if (desc == null) {
                        desc = "";
                    } else {
                        desc = desc;
                    }
                    indicator.setDescription(desc);
                    indicatorList.add(indicator);
                }
                cache.put(new Element(CacheKeys.indicatorName, indicatorList));
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                DatabaseSource.close(rs);
                DatabaseSource.close(ps);
                DatabaseSource.close(conn);
            }
        } else {
            long startTime = System.nanoTime();
            indicatorList = (List<Indicator>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return indicatorList;
    }

    private Map<String, Object> getDictionary(String peSpan, String peType, String ouid, String id) throws DslException {

        if (ouid == null) {
            ouid = "18";
        }
        if (peType == null) {
            peType = "yearly";
        }
        if (peSpan == null) {
            peSpan = "2";
        }

        Map<String, Object> dictionary = new HashMap();
        List<Map> orgUnits = new ArrayList();
        List<Map> indicators = new ArrayList();
        Map<String, Object> parameters = new HashMap();
        List<String> addedOuid = new ArrayList();
        List<String> addedIndicators = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;

        try {
            conn = DatabaseSource.getConnection();

            if (ouid.equals("18")) {
                Map<String, Object> orgUnitMetadata = new HashMap();
                orgUnitMetadata.put("id", ouid);
                orgUnitMetadata.put("name", "Kenya");
                orgUnits.add(orgUnitMetadata);
                addedOuid.add(ouid);

            } else {
                //private String getOrgUnit = "select dhis_organisation_unit_name as name,dhis_organisation_unit_id as id from common_organisation_unit where dhis_organisation_unit_id=?";

                ps = conn.prepareStatement(getOrgUnit, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ps.setInt(1, Integer.parseInt(ouid));

                log.info("Query to run: " + ps.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    Map<String, Object> orgUnitMetadata = new HashMap();
                    String _ouid = rs.getString("id");
                    if (!addedOuid.contains(_ouid)) {
                        orgUnitMetadata.put("id", rs.getString("id"));
                        orgUnitMetadata.put("name", rs.getString("name"));
                        orgUnits.add(orgUnitMetadata);
                        addedOuid.add(_ouid);
                    }
                }
            }

            List<String> periodsParams = new ArrayList();
            periodsParams.add(peSpan);
            parameters.put("periodspan", periodsParams);

            List<String> locationParams = new ArrayList();
            locationParams.add(ouid);
            parameters.put("location", locationParams);

            List<String> indicatorParams = new ArrayList();
            indicatorParams.add(id);
            parameters.put("indicators", indicatorParams);

            List<String> periodsType = new ArrayList();
            periodsType.add(peType);
            parameters.put("periodtype", periodsType);

            ps = conn.prepareStatement(getIndicatorData, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            ps.setInt(1, Integer.parseInt(id));

            log.info("Query to run: " + ps.toString());
            rs = ps.executeQuery();

            while (rs.next()) {

                String _ouid = rs.getString("id");

                Map<String, Object> indicatorMetadata = new HashMap();
                String _indicatorId = rs.getString("id");
                if (!addedIndicators.contains(_indicatorId)) {
                    indicatorMetadata.put("id", _indicatorId);
                    indicatorMetadata.put("name", rs.getString("name"));
                    indicatorMetadata.put("last_updated", rs.getString("lastupdated"));
                    indicatorMetadata.put("date_created", rs.getString("_datecreated"));
                    indicatorMetadata.put("description", rs.getString("description"));
                    indicatorMetadata.put("source", "KHIS");
                    indicators.add(indicatorMetadata);
                    addedIndicators.add(_ouid);
                }

            }

            dictionary.put("orgunits", orgUnits);
            dictionary.put("indicators", indicators);
            dictionary.put("parameters", parameters);

        } catch (SQLException ex) {
            Message msg = new Message();
            msg.setMesageContent(ex.getMessage());
            msg.setMessageType(MessageType.SQL_QUERY_ERROR);
            throw new DslException(msg);
        } finally {
            DatabaseSource.close(rs);
            DatabaseSource.close(ps);
            DatabaseSource.close(conn);
        }
        return dictionary;
    }

    private Map<String, Map> preparePayload(String pe, String ouid, String id, ResultSet rs, boolean isMeta) throws SQLException {
        Map<String, Map> result = new HashMap();
        Map<String, Object> dictionary = new HashMap();
        List<Map> orgUnits = new ArrayList();
        List<Map> indicators = new ArrayList();
        Map<String, Object> parameters = new HashMap();
        Map<String, List> data = new HashMap();
        List<Map> indicatorList;
        List<String> addedOuid = new ArrayList();
        List<String> addedIndicators = new ArrayList();

        while (rs.next()) {

            Map<String, Object> orgUnitMetadata = new HashMap();
            String _ouid = rs.getString("ouid");

            if (!addedOuid.contains(_ouid)) {
                orgUnitMetadata.put("id", rs.getString("ouid"));
                orgUnitMetadata.put("name", rs.getString("ouname"));
                orgUnits.add(orgUnitMetadata);
                addedOuid.add(_ouid);
            }

            Map<String, Object> indicatorMetadata = new HashMap();
            String _indicatorId = rs.getString("id");
            if (!addedIndicators.contains(_indicatorId)) {
                indicatorMetadata.put("id", _indicatorId);
                indicatorMetadata.put("name", rs.getString("indicator_name"));
                indicatorMetadata.put("last_updated", rs.getString("lastupdated"));
                indicatorMetadata.put("date_created", rs.getString("_datecreated"));
                indicatorMetadata.put("description", rs.getString("description"));
                indicatorMetadata.put("source", "KHIS");
                indicators.add(indicatorMetadata);
                addedIndicators.add(_indicatorId);
            }

            List<String> periodsParams = Arrays.asList(pe.split(";"));
            parameters.put("period", periodsParams);

            List<Map> locationParams = new ArrayList();

            if (ouid != null) {
                if (ouid.trim().equals("18")) {
                    Map<String, String> locationP = new HashMap();
                    locationP.put("ouid", "18");
                    locationP.put("name", "Kenya");
                    locationParams.add(locationP);

                } else {
                    List paramsList = new ArrayList();
                    Map params = new HashMap();
                    paramsList.add(params);
                    String queryToRUn = "Select dhis_organisation_unit_name as name,dhis_organisation_unit_id as ouid from common_organisation_unit where dhis_organisation_unit_id in(#)";
                    queryToRUn = queryToRUn.replace("#", DslStringUtils.toCommaSperated(ouid).replaceAll("\"", ""));
                    log.debug(queryToRUn);

                    PreparedStatement ps = null;
                    ResultSet rsOuidName = null;
                    Connection conn = null;

                    try {
                        conn = DatabaseSource.getConnection();
                        ps = conn.prepareStatement(queryToRUn, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        log.info("Query to run: " + ps.toString());

                        rsOuidName = rsOuidName = ps.executeQuery();
                        while (rsOuidName.next()) {
                            Map<String, String> locationP = new HashMap();
                            locationP.put("ouid", rsOuidName.getString("ouid"));
                            locationP.put("name", rsOuidName.getString("name"));
                            locationParams.add(locationP);
                        }
                    } catch (Exception ex) {

                    } finally {
                        DatabaseSource.close(rsOuidName);
                        DatabaseSource.close(ps);
                        DatabaseSource.close(conn);
                    }

                }

            }

            parameters.put("location", locationParams);

            List<String> indicatorParams = Arrays.asList(id.split(";"));
            parameters.put("indicators", indicatorParams);

            //data
            Map<String, String> dataValues = new HashMap();
            if (!isMeta) {
                dataValues.put("value", rs.getString("value"));
                String mnth = rs.getString("month");
                dataValues.put("period", rs.getString("year") + ((mnth.length() > 1) ? mnth : "0" + mnth)); // ensure leading 0 if month is single digit eg. 20191 > 201901
                dataValues.put("ou", rs.getString("ouid"));

                if (data.containsKey(rs.getString("id"))) {
                    indicatorList = data.get(rs.getString("id"));
                    indicatorList.add(dataValues);
                } else {
                    indicatorList = new ArrayList();
                    indicatorList.add(dataValues);
                }
            } else {
                indicatorList = new ArrayList();
            }

            data.put(rs.getString("id"), indicatorList);

        }

        dictionary.put("orgunits", orgUnits);
        dictionary.put("indicators", indicators);
        dictionary.put("parameters", parameters);

        result.put("dictionary", dictionary);
        result.put("data", data);

        return result;
    }

    private void appendNationalQuerySegment() {
        if (appendAnd) {
            getKPIWholeYear = getKPIWholeYear.replace("@ouid@", " and \"Org unit id\"=18 "); //kenya (national id ) = 18
        } else {
            getKPIWholeYear = getKPIWholeYear.replace("@ouid@", " \"Org unit id\"=18 "); //kenya (national id ) = 18
            appendAnd = true;
        }
    }

    private String getIndicatorMeta(String pe, String ouid, String id, String level) throws DslException {
        appendAnd = false;
        getKPIWholeYear = "select \"Indicator name\" as indicator_name,\"Indicator description\" as description, "
                + "cast(to_char(startdate, 'YYYY') as int) as year \n"
                + ",cast(to_char(startdate, 'MM') as int) as month,_datecreated, lastupdated, \"Indicator ID\" as id,\"Org unit id\" as ouid,dhis.\"Organisation Unit Name\" as ouname \n"
                + "from vw_mohdsl_dhis_indicators dhis "
                + "where "
                + "@ouid@"
                + " @id@ "
                + " group by year,month,\"Indicator name\",\"Indicator description\",kpivalue,\"Org unit id\",ouname,id,_datecreated, lastupdated"
                + " order by month ";

        if (ouid != null) {
            String[] orgUnits = ouid.split(";"); //how many orgunits have been passed

            if (level == null || orgUnits.length > 1) {
                getKPIWholeYear = insertOrgUntiPart(ouid, getKPIWholeYear);
            } else if (level == "1" && ouid == "18") {
                ouid = "18"; //kenya (default national id ) = 18
                appendNationalQuerySegment();
            } else {
                getKPIWholeYear = insertOrgUntiPartByLevel(ouid, level, getKPIWholeYear);
                log.info(getKPIWholeYear);
            }
        } else {
            ouid = "18"; //kenya (default national id ) = 18
            if (level == "1" || level == null) {
                appendNationalQuerySegment();
            } else {
                getKPIWholeYear = insertOrgUntiPartByLevel(ouid, level, getKPIWholeYear);
                log.info(getKPIWholeYear);
            }
        }
        if (id != null) {
            getKPIWholeYear = insertIdPart(id, getKPIWholeYear);
        } else {
            Message msg = new Message();
            msg.setMesageContent("please add indicator id paramerter, '?id=xxx'");
            msg.setMessageType(MessageType.MISSING_PARAMETER_VALUE);
            throw new DslException(msg);
        }

        return getKPIWholeYear;

    }

    public Map<String, Map> getKPIValue(String pe, String ouid, String id, String level) throws DslException {
        Element ele = cache.get(pe + ouid + id + level);
        Map<String, Map> envelop = new HashMap();
        log.debug(" pe: " + pe + " ouid: " + ouid + " id: " + id + " level: " + level);
        if (ele == null) {

            if (pe != null) {
                getKPIWholeYear = insertPeriodPart(pe, getKPIWholeYear);
                appendAnd = true;
            } else {
                int currentYear = Calendar.getInstance().get(Calendar.YEAR);
                pe = Integer.toString(currentYear);
                getKPIWholeYear = insertPeriodPart(Integer.toString(currentYear), getKPIWholeYear); //current years' values
                appendAnd = true;
            }
            if (ouid != null) {
                String[] orgUnits = ouid.split(";"); //how many orgunits have been passed

                if (level == null || orgUnits.length > 1) {
                    getKPIWholeYear = insertOrgUntiPart(ouid, getKPIWholeYear);
                } else if (level == "1" && ouid == "18") {
                    ouid = "18"; //kenya (default national id ) = 18
                    appendNationalQuerySegment();
                } else {
                    getKPIWholeYear = insertOrgUntiPartByLevel(ouid, level, getKPIWholeYear);
                    log.info(getKPIWholeYear);
                }
            } else {
                ouid = "18"; //kenya (default national id ) = 18
                if (level == "1" || level == null) {
                    appendNationalQuerySegment();
                } else {
                    getKPIWholeYear = insertOrgUntiPartByLevel(ouid, level, getKPIWholeYear);
                    log.info(getKPIWholeYear);
                }
            }
            if (id != null) {
                getKPIWholeYear = insertIdPart(id, getKPIWholeYear);
            } else {
                Message msg = new Message();
                msg.setMesageContent("please add indicator id paramerter, '?id=xxx'");
                msg.setMessageType(MessageType.MISSING_PARAMETER_VALUE);
                throw new DslException(msg);
            }
            log.info("indicator query to run: " + getKPIWholeYear);
            List<IndicatorValue> kpiList = new ArrayList();

            PreparedStatement ps = null;
            ResultSet rs = null;
            Connection conn = null;

            try {
                conn = DatabaseSource.getConnection();
                ps = conn.prepareStatement(getKPIWholeYear, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                log.info("Query to run round one: " + ps.toString());
                rs = ps.executeQuery();
                log.info("Fetching KPI values");
                Map<String, Map> result;
                result = preparePayload(pe, ouid, id, rs, false);
                log.info(result.get("data"));
                if (result.get("data").size() == 0) {
                    getKPIWholeYear = getIndicatorMeta(pe, ouid, id, level);
                    ps = conn.prepareStatement(getKPIWholeYear, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    log.info("Query to run round two: " + ps.toString());
                    rs = ps.executeQuery();
                    log.info("Fetching KPI values");
                    result = preparePayload(pe, ouid, id, rs, true);
                }
                envelop.put("result", result);
                cache.put(new Element(pe + ouid + id + level, envelop));
                return envelop;
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                DatabaseSource.close(rs);
                DatabaseSource.close(ps);
                DatabaseSource.close(conn);
            }
            cache.put(new Element(pe + ouid + id, envelop));
            return envelop;
        } else {
            long startTime = System.nanoTime();
            envelop = (Map<String, Map>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
            return envelop;
        }
    }

    public List<Indicator> getIndicatorsByGroup(int groupId) throws DslException {
        List<Indicator> indicatorList = new ArrayList();

        Element ele = cache.get("indicatorByGroup" + groupId);
        if (ele == null) {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                conn = DatabaseSource.getConnection();
                ps = conn.prepareStatement(getIndicatorGroup, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                ps.setInt(1, groupId);
                log.info("Query to run: " + ps.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    Indicator indicator = new Indicator();
                    indicator.setId(rs.getString("id"));
                    indicator.setGroupId(rs.getString("groupId"));
                    String desc = rs.getString("description");
                    if (desc == null) {
                        desc = "";
                    } else {
                        desc = desc;
                    }
                    indicator.setDescription(desc);
                    String name = rs.getString("name");
                    indicator.setName(name);
                    indicatorList.add(indicator);
                }
                cache.put(new Element("indicatorByGroup" + groupId, indicatorList));

            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                DatabaseSource.close(rs);
                DatabaseSource.close(ps);
                DatabaseSource.close(conn);
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
            log.info("Fetching indicator groups");
            PreparedStatement ps = null;
            ResultSet rs = null;
            Connection conn = null;

            try {
                conn = DatabaseSource.getConnection();
                ps = conn.prepareStatement(getIndicatorGroups, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                log.info("Query to run: " + ps.toString());
                rs = ps.executeQuery();

                int count = 1;
                while (rs.next()) {
                    IndicatorGoup indGroup = new IndicatorGoup();
                    indGroup.setId(rs.getString("id"));
                    indGroup.setName(rs.getString("name"));
                    indicatorGroupList.add(indGroup);
                }
                cache.put(new Element(CacheKeys.indicatorGroup, indicatorGroupList));
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                DatabaseSource.close(rs);
                DatabaseSource.close(ps);
                DatabaseSource.close(conn);
            }
        } else {
            long startTime = System.nanoTime();
            indicatorGroupList = (List<IndicatorGoup>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return indicatorGroupList;
    }

    public Map<String, Map> getIndicatorToIndicatorCorrelation(String indicatorId, String ouid, String compareIndicators) throws DslException {
        Properties prop = new Properties();

        Map<String, Object> dictionary = null;
        Map<String, List> correlateData = new HashMap();

        Map<String, Object> result = new HashMap();
        log.info("get preictor dictionary");
        String propFileName = "settings.properties";
        log.info("get correlation server url settings");
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

        if (inputStream != null) {
            try {
                prop.load(inputStream);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(DhisDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            log.error("property file '" + propFileName + "' not found in the classpath");
        }

        String host = prop.getProperty("predictor_host");
        String predictor_port = prop.getProperty("predictor_port");

        log.info("connect to predictor server");
        try {
            log.debug("predictor server client instance");
            if (ouid == null || ouid.isEmpty()) {
                ouid = "18";
            }
            String correlation_url = "http://" + host + ":" + predictor_port + "/indicator_correlation/" + indicatorId + "/" + ouid + "/" + compareIndicators;

            //verse_url=URLEncoder.encode(verse_url, "UTF-8");
            log.info("The url: " + correlation_url);
            Client client = ClientBuilder.newClient();

            WebTarget webResource = client
                    .target(correlation_url);

            Invocation.Builder invocationBuilder
                    = webResource.request(MediaType.APPLICATION_JSON);

            Response response = invocationBuilder.get();

            if (response.getStatus() != 200) {
                log.error("Failed to predict data : "
                        + response.getStatus() + ":" + response.toString() + " : " + response.readEntity(String.class));

                throw new RuntimeException();
            }

//            Map dataMap = response.getEntity(Map.class);
            Gson gson = new Gson();
            String output = response.readEntity(String.class);
            correlateData = gson.fromJson(output, Map.class);

            log.info("Output from Server .... \n");
            log.info(correlateData);

        } catch (Exception e) {
            log.error(e);
        }

        Map<String, Map> envelop = new HashMap();
        envelop.put("result", correlateData);
        return envelop;
    }

    public Map<String, Map> getWeatherToIndicatorCorrelation(String indicatorId, String ouid) throws DslException {
        Properties prop = new Properties();

        Map<String, Object> dictionary = null;
        Map<String, List> correlateData = new HashMap();

        Map<String, Object> result = new HashMap();
        String propFileName = "settings.properties";
        log.info("get correlation server url settings");
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

        if (inputStream != null) {
            try {
                prop.load(inputStream);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(DhisDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            log.error("property file '" + propFileName + "' not found in the classpath");
        }

        String host = prop.getProperty("predictor_host");
        String predictor_port = prop.getProperty("predictor_port");

        log.info("connect to predictor server");
        try {
            String correlation_url = "http://" + host + ":" + predictor_port + "/weather_correlation/" + indicatorId + "/" + ouid;
            log.debug("predictor server client instance " + correlation_url);
            log.debug("got this far");
            if (ouid == null || ouid.isEmpty()) {
                ouid = "18";
            }

            Client client = ClientBuilder.newClient();
            WebTarget webResource = client
                    .target(correlation_url);
            Invocation.Builder invocationBuilder
                    = webResource.request(MediaType.APPLICATION_JSON);
            Response response = invocationBuilder.get();

            if (response.getStatus() != 200) {
                log.error("Failed to predict data : "
                        + response.getStatus() + ":" + response.toString() + " : " + response.readEntity(String.class));

                throw new RuntimeException();
            }

//            Map dataMap = response.getEntity(Map.class);
            Gson gson = new Gson();
            String output = response.readEntity(String.class);
            correlateData = gson.fromJson(output, Map.class);

            log.info("Output from Server .... \n");
            log.info(correlateData);
        } catch (Exception e) {
            log.error(e);
        }

        Map<String, Map> envelop = new HashMap();
        envelop.put("result", correlateData);
        return envelop;
    }

    public Map<String, Map> predict(String indicatorid, String ouid, String periodtype, String periodspan) throws DslException {
        Properties prop = new Properties();
        Map<String, Object> dictionary = null;
        Map<String, Object> result = new HashMap();
        Map<String, List> prdictData = new HashMap();
        log.info("get preictor dictionary");
        dictionary = getDictionary(periodspan, periodtype, ouid, indicatorid);
        String propFileName = "settings.properties";
        log.info("get preictor server url settings");
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

        if (inputStream != null) {
            try {
                prop.load(inputStream);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(DhisDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            log.error("property file '" + propFileName + "' not found in the classpath");
        }

        String host = prop.getProperty("predictor_host");
        String predictor_port = prop.getProperty("predictor_port");

        log.info("connect to predictor server");
        try {
            log.debug("predictor server client instance");

            log.debug("ouid: " + ouid + " periodspan: " + periodspan);
            if (ouid == null || ouid.isEmpty()) {
                ouid = "18";
            }
            String predictor_url = "http://" + host + ":" + predictor_port + "/forecast/" + indicatorid + "?ouid=" + ouid;
            if (periodtype != null) {
                if (!periodtype.isEmpty()) {
                    predictor_url = predictor_url + "&periodtype=" + periodtype;
                }
            }

            if (periodspan != null) {
                if (!periodspan.isEmpty()) {
                    predictor_url = predictor_url + "&periodspan=" + periodspan;
                }
            }
            //verse_url=URLEncoder.encode(verse_url, "UTF-8");
            log.info("The url: " + predictor_url);
            Client client = ClientBuilder.newClient();

            WebTarget webResource = client
                    .target(predictor_url);

            Invocation.Builder invocationBuilder
                    = webResource.request(MediaType.APPLICATION_JSON);

            Response response = invocationBuilder.get();

            if (response.getStatus() != 200) {
                log.error("Failed to predict data : "
                        + response.getStatus() + ":" + response.toString() + " : " + response.readEntity(String.class));

                throw new RuntimeException();
            }

//            Map dataMap = response.getEntity(Map.class);
            Gson gson = new Gson();
            String output = response.readEntity(String.class);
            prdictData = gson.fromJson(output, Map.class);

            log.info("Output from Server .... \n");
            log.info(prdictData);

        } catch (Exception e) {
            log.error(e);
        }
        result.put("dictionary", dictionary);
        result.put("data", prdictData.get("data"));

        Map<String, Map> envelop = new HashMap();
        envelop.put("result", result);
        return envelop;
    }

    public Map<String, Map> getWeatherToIndicatorForecast(
            String indicatorId,
            String ouid,
            String weather_id,
            String period_range) throws DslException {
        
        Properties prop = new Properties();

        Map<String, Object> dictionary = null;
        Map<String, List> correlateData = new HashMap();

        Map<String, Object> result = new HashMap();
        String propFileName = "settings.properties";
        log.info("get correlation server url settings");
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

        if (inputStream != null) {
            try {
                prop.load(inputStream);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(DhisDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            log.error("property file '" + propFileName + "' not found in the classpath");
        }

        String host = prop.getProperty("predictor_host");
        String predictor_port = prop.getProperty("predictor_port");

        log.info("connect to predictor server");
        try {
            String forecast_url = "http://" + host + ":" + predictor_port + "/indicator_weather_forecast/" + indicatorId + "/" + ouid
                    + "/" + weather_id + "/" + period_range + "/";
            log.debug("predictor server client instance " + forecast_url);
            log.debug("got this far");
            if (ouid == null || ouid.isEmpty()) {
                ouid = "18";
            }

            Client client = ClientBuilder.newClient();
            WebTarget webResource = client
                    .target(forecast_url);
            Invocation.Builder invocationBuilder
                    = webResource.request(MediaType.APPLICATION_JSON);
            Response response = invocationBuilder.get();

            if (response.getStatus() != 200) {
                log.error("Failed to predict data : "
                        + response.getStatus() + ":" + response.toString() + " : " + response.readEntity(String.class));

                throw new RuntimeException();
            }

//            Map dataMap = response.getEntity(Map.class);
            Gson gson = new Gson();
            String output = response.readEntity(String.class);
            correlateData = gson.fromJson(output, Map.class);

            log.info("Output from Server .... \n");
            log.info(correlateData);
        } catch (Exception e) {
            log.error(e);
        }

        Map<String, Map> envelop = new HashMap();
        envelop.put("result", correlateData);
        return envelop;
    }
    
    
     public Map<String, Map> getIndicatorToIndicatorForecast( String indicatorId, String ouid, String compareIndicators,String period_range) throws DslException {

        Properties prop = new Properties();

        Map<String, Object> dictionary = null;
        Map<String, List> correlateData = new HashMap();

        Map<String, Object> result = new HashMap();
        String propFileName = "settings.properties";
        log.info("get correlation server url settings");
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(propFileName);

        if (inputStream != null) {
            try {
                prop.load(inputStream);
            } catch (IOException ex) {
                java.util.logging.Logger.getLogger(DhisDao.class.getName()).log(Level.SEVERE, null, ex);
            }
        } else {
            log.error("property file '" + propFileName + "' not found in the classpath");
        }

        String host = prop.getProperty("predictor_host");
        String predictor_port = prop.getProperty("predictor_port");

        log.info("connect to predictor server");
        try {
            String forecast_url = "http://" + host + ":" + predictor_port + "/indicator_forecast/" + indicatorId + "/" + ouid
                    + "/" + compareIndicators + "/" + period_range + "/";
            log.debug("predictor server client instance " + forecast_url);
            log.debug("got this far");
            if (ouid == null || ouid.isEmpty()) {
                ouid = "18";
            }

            Client client = ClientBuilder.newClient();
            WebTarget webResource = client
                    .target(forecast_url);
            Invocation.Builder invocationBuilder
                    = webResource.request(MediaType.APPLICATION_JSON);
            Response response = invocationBuilder.get();

            if (response.getStatus() != 200) {
                log.error("Failed to predict data : "
                        + response.getStatus() + ":" + response.toString() + " : " + response.readEntity(String.class));

                throw new RuntimeException();
            }

//            Map dataMap = response.getEntity(Map.class);
            Gson gson = new Gson();
            String output = response.readEntity(String.class);
            correlateData = gson.fromJson(output, Map.class);

            log.info("Output from Server .... \n");
            log.info(correlateData);
        } catch (Exception e) {
            log.error(e);
        }

        Map<String, Map> envelop = new HashMap();
        envelop.put("result", correlateData);
        return envelop;
    }

}
