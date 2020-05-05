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

    private String sourceSql = "SELECT id,source FROM survey_source";

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

    private List<Map<String, String>> getBasicIndicatorInfo(int sourceId) throws DslException {
        String indicatorQuery = "";
        List<Map<String, String>> result = new ArrayList();
        if (sourceId == 2 || sourceId == 3 || sourceId == 4 || sourceId == 5 || sourceId == 6) {
            indicatorQuery = "SELECT Distinct ind.indicator_id as id, ind.name as name, '" + DataSource.getSources().get(sourceId) + "' as source '' as description,"
                    + " '" + sourceId + "' as source_id FROM "
                    + " dim_survey_indicator ind inner join fact_survey fs on ind.indicator_id=fs.indicator_id "
                    + " inner join dim_survey_source source on fs.source_id = source.source_id where "
                    + "source.name='" + DataSource.getSources().get(sourceId) + "'";
        } else if (sourceId == 7) {
            indicatorQuery = "SELECT Distinct ind.indicator_id as id, ind.name as name, '" + DataSource.getSources().get(sourceId) + "' as source, description, "
                    + "'" + sourceId + "' as source_id  FROM "
                    + " dim_steps_indicator ind";
        } else if (sourceId == 8) {
            indicatorQuery = "SELECT Distinct ind.indicator_id as id, ind.name as name, '" + DataSource.getSources().get(sourceId) + "' as source, description, "
                    + "'" + sourceId + "' as source_id  FROM "
                    + " dim_steps_indicator ind";
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
                payLoad.put("source id", rs.getString("source_id"));
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

    /**
     * fetches a list of indicators for a given source id
     *
     * @param id
     * @return
     * @throws DslException
     */
    public List<Map<String, String>> getIndicators(String id) throws DslException {
        int sourceId = Integer.parseInt(id);
        List<Map<String, String>> result = new ArrayList();
        return getBasicIndicatorInfo(sourceId);
    }

    /**
     * Builds sql to use for fetching requested steps srvey data and metadata
     *
     * @param sourceId
     * @param indicatorId
     * @param orgId
     * @param category_id
     * @return
     * @throws DslException
     */
    private String getSurveySql(int sourceId, int indicatorId, String orgId, String category_id) throws DslException {
        log.debug("get survey sql funct");
        String catFilter = "";
        if (category_id != null) {
            if (category_id.trim().length() != 0) {
                String[] catGroups = category_id.trim().split(",");
                for (int y = 0; y < catGroups.length; y++) {
                    String[] catLength = catGroups[y].trim().split(";");
                    String genderFilter = "";
                    String ageFilter = "";
                    for (int x = 0; x < catLength.length; x++) {
                        String getGenderDtailSQL = "select survey_age_id, survey_gender_id from survey_combine_category where id=" + catLength[x];
                        Database db = new Database();
                        ResultSet rs = db.executeQuery(getGenderDtailSQL);

                        try {
                            if (rs.next()) {
                                String ageID = rs.getString("survey_age_id");
                                String genderId = rs.getString("survey_gender_id");

                                if (ageID != null) {
                                    ageFilter = " dim_surv_age.age_id=" + Integer.parseInt(ageID);
                                }
                                if (genderId != null) {
                                    genderFilter = " dim_surv_gender.gender_id=" + Integer.parseInt(genderId);
                                }
                                if (x == 0) {
                                    catFilter += " and (";
                                }
                                if (x > 0) {
                                    catFilter += " or ";
                                }
                                catFilter += ageFilter + genderFilter;
                                if (x == catLength.length - 1) {
                                    catFilter += ")";
                                }
                            }
                        } catch (SQLException ex) {
                            log.error(ex);
                        }

                    }
                }

            }

        }
        log.debug("Build survey common org unit join");
        String orgSeg = "";
        if (orgId != null && orgId != "18") {
            orgSeg = " inner join surv_comm_org surv_org on surv_org.name=dim_surv_org.name and surv_comm_org.id=" + orgId;
        } else {
            orgSeg = " inner join surv_comm_org surv_org on surv_org.name=dim_surv_org.name and surv_org.name='Kenya'";
        }

        String getSurveyDataSql = "SELECT Distinct fs.value as value, dim_ind.indicator_id as id, dim_ind.name as name, '" + DataSource.getSources().get(sourceId) + "' as source, '' as description, "
                + "  '" + sourceId + "' as source_id, dim_surv_age.age as age,surv_cat2.id age_id, dim_surv_gender.name as gender,surv_cat.id gender_id,"
                + " surv_org.name as org_name,  surv_org.id as orgId FROM dim_survey_indicator dim_ind "
                + " inner join fact_survey fs on dim_ind.indicator_id=fs.indicator_id   "
                + " inner join dim_survey_gender dim_surv_gender on dim_surv_gender.gender_id = fs.gender_id"
                + " inner join dim_survey_age dim_surv_age on dim_surv_age.age_id = fs.age_id  "
                + " inner join survey_category surv_cat on surv_cat.category=dim_surv_gender.name "
                + " inner join survey_category surv_cat2 on surv_cat2.category=dim_surv_age.age"
                + " inner join dim_survey_source source on fs.source_id = source.source_id   "
                + " inner join dim_survey_orgunit dim_surv_org on fs.orgunit_id=dim_surv_org.orgunit_id   "
                + orgSeg
                + "      where source.name='" + DataSource.getSources().get(sourceId) + "'"
                + "     and dim_ind.indicator_id =" + indicatorId
                + catFilter;

        log.debug(getSurveyDataSql);
        return getSurveyDataSql;
    }

    /**
     * Builds sql to use for fetching requested survey data and metadata
     *
     * @param sourceId
     * @param indicatorId
     * @param orgId
     * @param category_id
     * @return
     * @throws DslException
     */
    private String getStepsSurveySql(int sourceId, int indicatorId, String orgId, String category_id) throws DslException {
        log.debug("get steps survey sql funct");
        String catFilter = "";
        if (category_id != null) {
            if (category_id.trim().length() != 0) {
                String[] catGroups = category_id.trim().split(",");
                for (int y = 0; y < catGroups.length; y++) {
                    String[] catLength = catGroups[y].trim().split(";");
                    String genderFilter = "";
                    String ageFilter = "";
                    for (int x = 0; x < catLength.length; x++) {
                        String getGenderDtailSQL = "select steps_age_id, steps_gender_id from survey_combine_category where id=" + catLength[x];
                        Database db = new Database();
                        ResultSet rs = db.executeQuery(getGenderDtailSQL);

                        try {
                            if (rs.next()) {
                                String ageID = rs.getString("steps_age_id");
                                String genderId = rs.getString("steps_gender_id");

                                if (ageID != null) {
                                    ageFilter = " dim_stps_age.age_id=" + Integer.parseInt(ageID);
                                }
                                if (genderId != null) {
                                    genderFilter = " dim_stps_gender.gender_id=" + Integer.parseInt(genderId);
                                }
                                if (x == 0) {
                                    catFilter += " and (";
                                }
                                if (x > 0) {
                                    catFilter += " or ";
                                }
                                catFilter += ageFilter + genderFilter;
                                if (x == catLength.length - 1) {
                                    catFilter += ")";
                                }
                            }
                        } catch (SQLException ex) {
                            log.error(ex);
                        }

                    }
                }

            }

        }
        log.debug("Build survey common org unit join");
        String orgSeg = "";
        if (orgId != null && orgId != "18") {
            orgSeg = " inner join surv_comm_org comm_org on dim_stps_org.name=comm_org.name and comm_org.id=" + orgId;
        } else {
            orgSeg = " inner join surv_comm_org comm_org on dim_stps_org.name=comm_org.name and comm_org.name='Kenya'";
        }

        String getSurveyDataSql = "SELECT dim_stps_indicator.indicator_id as id, dim_stps_org.name as org_name,comm_org.id as orgId, dim_stps_age.age as age, "
                + " surv_comb_cat.id as age_id , value as value,dim_stps_indicator.name as name, '" + DataSource.getSources().get(sourceId) + "' as source, "
                + " dim_stps_indicator.description as description, '" + sourceId + "' as source_id, dim_stps_gender.name as gender,surv_comb_cat2.id gender_id "
                + " FROM public.fact_steps fs "
                + " inner join dim_steps_age dim_stps_age on  fs.age_id = dim_stps_age.age_id"
                + " inner join dim_steps_orgunit dim_stps_org on dim_stps_org.orgunit_id = fs.orgunit_id"
                + " inner join dim_steps_gender dim_stps_gender on fs.gender_id=dim_stps_gender.gender_id"
                + " inner join survey_combine_category surv_comb_cat on surv_comb_cat.steps_age_id = dim_stps_age.age_id "
                + " inner join survey_combine_category surv_comb_cat2 on surv_comb_cat2.steps_gender_id = dim_stps_gender.gender_id "
                + orgSeg
                + "  inner join dim_steps_indicator dim_stps_indicator on fs.indicator_id = dim_stps_indicator.indicator_id"
                + "  where "
                + "  dim_stps_indicator.indicator_id =" + indicatorId
                + catFilter;

        log.debug(getSurveyDataSql);
        return getSurveyDataSql;
    }

    /**
     * Gets all avaialble data for a particular indicator
     *
     * @param sourceId
     * @param indicatorId
     * @return
     */
    private String getSurveyAvailableDimesions(int sourceId, int indicatorId) {
        log.debug("get survey available dimesions");
        String sql = "";
        if (sourceId == 2 || sourceId == 3 || sourceId == 4 || sourceId == 5 || sourceId == 6) {
            sql = "Select fs.indicator_id, surv_org.name as org_name,surv_org.id as orgunit_id,surv_cat2.id as age_id, surv_cat2.category as age,surv_cat.id as gender_id,surv_cat.category as gender FROM fact_survey fs"
                    + " inner join dim_survey_indicator dim_ind  on dim_ind.indicator_id=fs.indicator_id   "
                    + " inner join dim_survey_gender dim_surv_gender on dim_surv_gender.gender_id = fs.gender_id"
                    + " inner join dim_survey_age dim_surv_age on dim_surv_age.age_id = fs.age_id  "
                    + " inner join dim_survey_source source on fs.source_id = source.source_id   "
                    + " inner join dim_survey_orgunit dim_surv_org on fs.orgunit_id=dim_surv_org.orgunit_id   "
                    + " inner join surv_comm_org surv_org on surv_org.name=dim_surv_org.name "
                    + " inner join survey_category surv_cat on surv_cat.category=dim_surv_gender.name"
                    + " inner join survey_category surv_cat2 on surv_cat2.category=dim_surv_age.age"
                    + " where source.name='" + DataSource.getSources().get(sourceId) + "'"
                    + " and dim_ind.indicator_id =" + indicatorId;

        } else if (sourceId == 7) {

            sql = "Select fs.indicator_id, surv_org.name as org_name,surv_org.id as orgunit_id,surv_cat2.id as age_id, surv_cat2.category as age,surv_cat.id as gender_id,surv_cat.category as gender "
                    + " FROM public.fact_steps fs "
                    + " inner join dim_steps_age dim_stps_age on  fs.age_id = dim_stps_age.age_id"
                    + " inner join dim_steps_gender dim_stps_gender on  fs.gender_id = dim_stps_gender.gender_id"
                    + " inner join dim_steps_orgunit dim_stps_org on dim_stps_org.orgunit_id = fs.orgunit_id"
                    + " inner join survey_category surv_cat on surv_cat.category=dim_stps_gender.name"
                    + " inner join survey_category surv_cat2 on surv_cat2.category=dim_stps_age.age"
                    + " inner join surv_comm_org surv_org on dim_stps_org.name=dim_stps_org.name"
                    + "  inner join dim_steps_indicator dim_stps_indicator on fs.indicator_id = dim_stps_indicator.indicator_id"
                    + "  where "
                    + "  dim_stps_indicator.indicator_id =" + indicatorId;
            log.debug(sql);
        }
        return sql;
    }

    /**
     * Gets survey data requested and the metadata
     *
     * @param queryToRun
     * @return
     * @throws DslException
     */
    private Map<String, Object> getCoreSurveyData(String queryToRun) throws DslException {
        log.debug("get core survey data values");
        Database db = new Database();
        ResultSet rs = db.executeQuery(queryToRun);
        Map<String, Object> result = new HashMap();
        List<Map> indicatorDetails = new ArrayList();
        List<Map> orgUnits = new ArrayList();
        List<List<Map<String, Object>>> categories = new ArrayList();
        List<String> periods = new ArrayList();
        List<String> addedOrgsUnit = new ArrayList();
        List<String> addedIndicators = new ArrayList();
        List<Map> data = new ArrayList();
        log.debug("Build payload");
        try {
            while (rs.next()) {
                //indicator

                String sourceId = rs.getString("source_id");
                int indicatorId = rs.getInt("id");
                String addedVals = sourceId + ":" + Integer.toString(indicatorId);
                if (!addedIndicators.contains(addedVals)) {
                    addedIndicators.add(addedVals);
                    Map<String, Object> indicatorDetail = new HashMap();
                    indicatorDetail.put("name", rs.getString("name"));
                    indicatorDetail.put("id", rs.getInt("id"));
                    indicatorDetail.put("source", rs.getString("source"));
                    indicatorDetail.put("source id", sourceId);
                    indicatorDetail.put("description", rs.getString("description"));
                    indicatorDetails.add(indicatorDetail);
                }

                String orgName = rs.getString("org_name");

                if (!addedOrgsUnit.contains(orgName)) {
                    //orgunits
                    Map<String, Object> orgUnit = new HashMap();
                    if (orgName.equals("Kenya")) {
                        orgUnit.put("id", 18);
                    } else {
                        orgUnit.put("id", rs.getInt("orgId"));
                    }
                    orgUnit.put("name", orgName);
                    addedOrgsUnit.add(orgName);
                    orgUnits.add(orgUnit);
                }

                //categories & data
                List<Map<String, Object>> category = new ArrayList();
                Map<String, Object> dataHolder = new HashMap();

                String gender = rs.getString("gender");
                String age = rs.getString("age");
                if (!gender.equals("n/a")) {
                    Map<String, Object> categoryHolder = new HashMap();
                    categoryHolder.put("name", gender);
                    categoryHolder.put("id", rs.getInt("gender_id"));
                    category.add(categoryHolder);
                }
                if (!age.equals("n/a")) {
                    Map<String, Object> categoryHolder = new HashMap();
                    categoryHolder.put("name", age);
                    categoryHolder.put("id", rs.getInt("age_id"));
                    category.add(categoryHolder);
                }
                if (category.size() != 0) {
                    categories.add(category);
                    dataHolder.put("category", category);
                }

                dataHolder.put("source_id", rs.getInt("source_id"));
                dataHolder.put("indicator_id", rs.getInt("id"));
                dataHolder.put("value", rs.getInt("value"));
                data.add(dataHolder);

            }
        } catch (SQLException ex) {
            Message msg = new Message();
            msg.setMesageContent(ex.getMessage());
            msg.setMessageType(MessageType.SQL_QUERY_ERROR);
            throw new DslException(msg);
        } finally {
            db.CloseConnection();
        }

        result.put("indicators", indicatorDetails);
        result.put("orgunits", orgUnits);
        result.put("categories", categories);
        result.put("periods", periods);
        Map<String, Object> envelop = new HashMap();
        envelop.put("metadata", result);
        envelop.put("data", data);
        return envelop;
    }

    private Map<String, Object> getAvailableDimesionData(String queryToRun) throws DslException {
        log.debug("get available dimension data");
        Database db = new Database();
        ResultSet rs = db.executeQuery(queryToRun);
        Map<String, Object> result = new HashMap();
        List<Map> orgUnits = new ArrayList();
        List<String> addedOrgsUnit = new ArrayList();
        List<String> addedCategoriesUnit = new ArrayList();
        List<List<Map<String, Object>>> categories = new ArrayList();
        List<String> periods = new ArrayList();
        try {
            while (rs.next()) {
                //orgunits
                Map<String, Object> orgUnit = new HashMap();
                String orgName = rs.getString("org_name");
                if (orgName.equals("Kenya")) {
                    orgUnit.put("id", 18);
                } else {
                    orgUnit.put("id", rs.getInt("orgunit_id"));
                }
                orgUnit.put("name", orgName);
                if (!addedOrgsUnit.contains(orgName)) {
                    addedOrgsUnit.add(orgName);
                    orgUnits.add(orgUnit);
                }

                //categories
                List<Map<String, Object>> category = new ArrayList();

                String gender = rs.getString("gender");
                String age = rs.getString("age");
                String ageGender="";
                if (!gender.equals("n/a")) {
                    Map<String, Object> categoryHolder = new HashMap();
                    categoryHolder.put("name", gender);
                    int genderId=rs.getInt("gender_id");
                    categoryHolder.put("id",genderId);
                    category.add(categoryHolder);
                    ageGender+=genderId;
                }
                if (!age.equals("n/a")) {
                    Map<String, Object> categoryHolder = new HashMap();
                    categoryHolder.put("name", age);
                    int ageId=rs.getInt("age_id");
                    categoryHolder.put("id", ageId);
                    category.add(categoryHolder);
                    ageGender+=ageId;
                }
                if (category.size() != 0 && !addedCategoriesUnit.contains(ageGender) && ageGender.length()!=0) {
                    addedCategoriesUnit.add(ageGender);
                    categories.add(category);
                }

            }
        } catch (SQLException ex) {
            Message msg = new Message();
            msg.setMesageContent(ex.getMessage());
            msg.setMessageType(MessageType.SQL_QUERY_ERROR);
            throw new DslException(msg);
        } finally {
            db.CloseConnection();
        }
        log.debug("build payload");
        result.put("orgunits", orgUnits);
        result.put("categories", categories);
        result.put("periods", periods);
        Map<String, Object> envelop = new HashMap();
        envelop.put("available", result);
        return envelop;
    }

    private String getStepsSurveySql() {
        String sql = "SELECT fact_id, indicator_id, dim_stps_org.name ,comm_org.id  as org_id, dim_stps_age.age, surv_comb_cat.id as age_id , value"
                + "FROM public.fact_steps fs\n"
                + "inner join dim_steps_age dim_stps_age on  fs.age_id = dim_stps_age.age_id"
                + "inner join dim_steps_orgunit dim_stps_org on dim_stps_org.orgunit_id = fs.orgunit_id"
                + "inner join survey_combine_category surv_comb_cat on surv_comb_cat.steps_age_id = dim_stps_age.age_id "
                + "inner join surv_comm_org comm_org on dim_stps_org.name=comm_org.name";
        return sql;
    }

    /**
     *
     * @param survData Map<String, Object> with keys: metadata, available, data
     * The metadata is a Map<String, Object> that holds metadata to return and
     * has keys indicators,orgunits,categories,periods The available is a
     * Map<String, Object> that holds a list of available category data for the
     * indicator The data is a list of maps holding availble data value with
     * keys, source_id, category group (array of maps/dict), indicator id and
     * value
     * @return
     */
    private Map<String, Object> resultAssember(Map<String, Object> survData) {
        Map<String, Object> envelop = new HashMap();
        Map<String, Object> result = new HashMap();
        Map<String, Object> dictionary = new HashMap();
        log.debug("surv meta type :"+survData.get("metadata"));
        Map<String, Object> survMeta = (Map<String, Object>) survData.get("metadata");

        dictionary.put("indicators", survMeta.get("indicators"));
        dictionary.put("orgunits", survMeta.get("orgunits"));
        dictionary.put("categories", survMeta.get("categories"));
        dictionary.put("periods", survMeta.get("periods"));
        dictionary.put("available", survData.get("available"));

        result.put("dictionary", dictionary);
        result.put("data", survData.get("data"));
        envelop.put("result", result);
        return envelop;
    }

    public Map<String, Object> getIndicatorValue(String sId, String iId, String orgId, String pe, String category_id) throws DslException {
        log.info("get survey data values");
        int sourceId = Integer.parseInt(sId);
        int indicatorId = Integer.parseInt(iId);
        Map<String, Object> result = new HashMap();
        if (sourceId == 2 || sourceId == 3 || sourceId == 4 || sourceId == 5 || sourceId == 6) {
            String indicatorQuery = getSurveySql(sourceId, indicatorId, orgId, category_id);
            Map<String, Object> coreSurvey = getCoreSurveyData(indicatorQuery);
            String availableDataDimesions = getSurveyAvailableDimesions(sourceId, indicatorId);
            Map<String, Object> surveyDimensions = getAvailableDimesionData(availableDataDimesions);

            log.info("assemble survey payload");
//            Map<String, Object> dictionary = new HashMap();
//
            Map<String, Object> survMeta = (Map<String, Object>) coreSurvey.get("metadata");
//
//            dictionary.put("indicators", survMeta.get("indicators"));
//            dictionary.put("orgunits", survMeta.get("orgunits"));
//            dictionary.put("categories", survMeta.get("categories"));
//            dictionary.put("periods", survMeta.get("periods"));
//            dictionary.put("available", surveyDimensions.get("available"));
//
//            result.put("dictionary", dictionary);
//            result.put("data", coreSurvey.get("data"));

            Map<String, Object> survData = new HashMap();
            survData.put("metadata", survMeta);
            survData.put("available", surveyDimensions.get("available"));
            survData.put("data", coreSurvey.get("data"));

            return resultAssember(survData);
        } else if (sourceId == 7) {
            String indicatorQuery = getStepsSurveySql(sourceId, indicatorId, orgId, category_id);
            Map<String, Object> coreSurvey = getCoreSurveyData(indicatorQuery);
            String availableDataDimesions = getSurveyAvailableDimesions(sourceId, indicatorId);
            Map<String, Object> surveyDimensions = getAvailableDimesionData(availableDataDimesions);
            Map<String, Object> survMeta = (Map<String, Object>) coreSurvey.get("metadata");

            Map<String, Object> survData = new HashMap();
            survData.put("metadata", survMeta);
            survData.put("available", surveyDimensions.get("available"));
            survData.put("data", coreSurvey.get("data"));
            return resultAssember(survData);
        } else if (sourceId == 8) {
            String indicatorQuery = "SELECT Distinct ind.indicator_id as id, ind.name as name, '" + DataSource.getSources().get(sourceId) + "' as source, description, "
                    + "'" + sourceId + "' as source_id  FROM "
                    + " dim_khds_indicator ind";
        } else {
            Message msg = new Message();
            msg.setMesageContent("This id does not exist ");
            msg.setMessageType(MessageType.MISSING_DB_ENRTY_VALUE);
            throw new DslException(msg);
        }

        return result;
    }

}
