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
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Level;
import javax.ws.rs.core.MediaType;
import org.apache.log4j.Logger;

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
                String desc = rs.getString("description");
                if (desc == null) {
                    desc = "";
                } else {
                    desc = desc;
                }
                indicator.setDescription(desc);
                indicatorList.add(indicator);
            }
        } catch (SQLException ex) {
            log.error(ex);
        } finally {
            db.CloseConnection();
        }
        return indicatorList;
    }

    public Map<String, Map> predict(String indicatorid,String ouid,String periodtype,String periodspan){
        Properties prop = new Properties();
                
        String propFileName = "settings.properties";

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
        
        
        Map<String, Map> result = new HashMap();
        try {
            
            Client client = Client.create();
            if(ouid == null || ouid.isEmpty()) ouid="18";
            String predictor_url = "http://"+host+":"+predictor_port+"/forecast/"+indicatorid+"?ouid="+ouid;
            if(periodtype != null && !periodtype.isEmpty()) predictor_url=predictor_url+"&periodtype="+periodtype;
            if(periodspan != null && !periodspan.isEmpty()) periodspan=predictor_url+"&periodspan="+periodspan;
            //verse_url=URLEncoder.encode(verse_url, "UTF-8");
            log.info("The url: " + predictor_url);
            WebResource webResource = client
                    .resource(predictor_url);

            ClientResponse response = webResource.accept(MediaType.APPLICATION_XML)
                    .get(ClientResponse.class);

            if (response.getStatus() != 200) {
                log.error("Failed to predict data : "
                        + response.getStatus()+ ":" +response.toString()+ " : "+response.getEntity(String.class));
                throw new RuntimeException();
            }

            String output = response.getEntity(String.class);

            log.info("Output from Server .... \n");
            log.info(output);

        } catch (Exception e) {

            e.printStackTrace();

        }
        return result;
    }
    
    private Map<String, Map> preparePayload(String pe, String ouid, String id, ResultSet rs) throws SQLException {
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
            String _ouid = rs.getString("id");

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
                addedIndicators.add(_ouid);
            }

            List<String> periodsParams = new ArrayList();
            periodsParams.add(pe);
            parameters.put("period", periodsParams);

            List<String> locationParams = new ArrayList();
            locationParams.add(ouid);
            parameters.put("location", locationParams);

            List<String> indicatorParams = new ArrayList();
            indicatorParams.add(id);
            parameters.put("indicators", indicatorParams);

            //data
            Map<String, String> dataValues = new HashMap();
            dataValues.put("value", rs.getString("value"));
            String mnth = rs.getString("month");
            dataValues.put("period", rs.getString("year") + ((mnth.length() > 1) ? mnth : "0" + mnth)); // ensure leading 0 if month is single digit eg. 20191 > 201901
            dataValues.put("ou", ouid);

            if (data.containsKey(rs.getString("id"))) {
                indicatorList = data.get(rs.getString("id"));
                indicatorList.add(dataValues);
            } else {
                indicatorList = new ArrayList();
                indicatorList.add(dataValues);
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

    public Map<String, Map> getKPIValue(String pe, String ouid, String id) throws DslException {
        Element ele = cache.get(pe + ouid + id);
        Map<String, Map> envelop = new HashMap();

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

                getKPIWholeYear = insertOrgUntiPart(ouid, getKPIWholeYear);
            } else {
                ouid = "18"; //kenya (default national id ) = 18
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
                Message msg = new Message();
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
                Map<String, Map> result;
                result = preparePayload(pe, ouid, id, rs);
                envelop.put("result", result);
                cache.put(new Element(pe + ouid + id, envelop));
                return envelop;
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                db.CloseConnection();
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
            try {
                Database db = new Database();
                Connection conn = db.getConn();
                PreparedStatement ps = conn.prepareStatement(getIndicatorGroup);
                ps.setInt(1, groupId);
                log.info("Fetching indicators");
                log.info(ps.toString());
                ResultSet rs = ps.executeQuery();

                try {
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
                    indGroup.setId(rs.getString("id"));
                    indGroup.setName(rs.getString("name"));
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
