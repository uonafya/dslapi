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
import com.healthit.dslservice.util.DatabaseSource;
import com.healthit.dslservice.util.DslCache;
import java.sql.Connection;
import java.sql.PreparedStatement;
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

            PreparedStatement ps = null;
            ResultSet rs = null;
            Connection conn = null;
            try {
                conn = DatabaseSource.getConnection();
                ps = conn.prepareStatement(sourceSql, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                log.info("Query to run: " + ps.toString());
                rs = ps.executeQuery();

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
                DatabaseSource.close(rs);
                DatabaseSource.close(ps);
                DatabaseSource.close(conn);
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
            indicatorQuery = "SELECT Distinct ind.indicator_id as id, ind.name as name, '" + DataSource.getSources().get(sourceId) + "' as source, '' as description,"
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
                    + " dim_khds_indicator ind";
        } else {
            Message msg = new Message();
            msg.setMesageContent("This id does not exist ");
            msg.setMessageType(MessageType.MISSING_DB_ENRTY_VALUE);
            throw new DslException(msg);
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = DatabaseSource.getConnection();
            ps = conn.prepareStatement(indicatorQuery, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            log.info("Query to run: " + ps.toString());
            rs = ps.executeQuery();

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

    private String getSurveyCategoryFilterQueryPart(String category_id) throws DslException {
        String catFilter = "";

        if (category_id.trim().length() != 0) {
            String[] catGroups = category_id.trim().split(",");
            for (int y = 0; y < catGroups.length; y++) {
                String[] catLength = catGroups[y].trim().split(";");

                String innerCatFilter = "";
                if (y == 0) {
                    catFilter += " and ("; //outer bracket
                    innerCatFilter += " (";
                } else {
                    innerCatFilter += " or (";
                }

                for (int x = 0; x < catLength.length; x++) {
                    String genderFilter = "";
                    String ageFilter = "";
                    String categoryFilter = "";
                    String getGenderDtailSQL = "select survey_age_id, survey_gender_id, gen_survey_category_id from survey_combine_category where id=" + catLength[x];

                    PreparedStatement ps = null;
                    ResultSet rs = null;
                    Connection conn = null;
                    try {
                        conn = DatabaseSource.getConnection();
                        ps = conn.prepareStatement(getGenderDtailSQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        log.info("Query to run: " + ps.toString());
                        rs = ps.executeQuery();

                        if (rs.next()) {
                            String ageID = rs.getString("survey_age_id");
                            String genderId = rs.getString("survey_gender_id");
                            String _category_id = rs.getString("gen_survey_category_id");

                            if (ageID != null) {
                                ageFilter = " dim_surv_age.age_id=" + Integer.parseInt(ageID);
                            }
                            if (genderId != null) {
                                genderFilter = " dim_surv_gender.gender_id=" + Integer.parseInt(genderId);
                            }
                            if (_category_id != null) {
                                categoryFilter = " dim_surv_category.category_id=" + Integer.parseInt(_category_id);
                            }

                            if (x == 0) {
                                innerCatFilter += ageFilter + genderFilter + categoryFilter;
                            } else {
                                innerCatFilter += " and " + ageFilter + genderFilter + categoryFilter;
                            }
                        }
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

                }
                innerCatFilter += ")";
                catFilter = catFilter + innerCatFilter;
                if (y == catGroups.length - 1) {
                    catFilter += ")"; //outer bracket
                }
            }

        }
        return catFilter;
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
            catFilter = getSurveyCategoryFilterQueryPart(category_id);
        }
        log.debug("Build survey common org unit join");
        String orgSeg = "";
        if (orgId != null && !orgId.equals("18")) {
            orgSeg = " inner join surv_comm_org surv_org on surv_org.name=dim_surv_org.name and surv_org.id=" + orgId;
        } else {
            orgSeg = " inner join surv_comm_org surv_org on surv_org.name=dim_surv_org.name and surv_org.name='Kenya'";
        }

        String getSurveyDataSql = "SELECT Distinct fs.value as value, dim_ind.indicator_id as id, dim_ind.name as name, '" + DataSource.getSources().get(sourceId) + "' as source, '' as description, "
                + "  '" + sourceId + "' as source_id, dim_surv_age.age as age,surv_cat2.id age_id, dim_surv_gender.name as gender,surv_cat.id gender_id,"
                + " surv_org.name as org_name,  surv_org.id as orgId, surv_comb_cat.cat as category_name, surv_comb_cat.id as category_id FROM dim_survey_indicator dim_ind "
                + " inner join fact_survey fs on dim_ind.indicator_id=fs.indicator_id   "
                + " inner join dim_survey_gender dim_surv_gender on dim_surv_gender.gender_id = fs.gender_id"
                + " inner join dim_survey_age dim_surv_age on dim_surv_age.age_id = fs.age_id  "
                + " inner join dim_survey_category dim_surv_category on dim_surv_category.category_id = fs.category_id  "
                + " inner join survey_category surv_cat on surv_cat.category=dim_surv_gender.name "
                + " inner join survey_category surv_cat2 on surv_cat2.category=dim_surv_age.age"
                + " inner join survey_combine_category surv_comb_cat on surv_comb_cat.gen_survey_category_id = dim_surv_category.category_id "
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
     * Generates filter for where clause (age and gender) for steps data
     *
     * @param category_id
     * @return
     * @throws DslException
     */
    private String getStepsCategoryFilterQueryPart(String category_id) throws DslException {
        String catFilter = "";
        if (category_id.trim().length() != 0) {
            String[] catGroups = category_id.trim().split(",");
            log.debug("category groups " + catGroups + " length: " + catGroups.length);
            for (int y = 0; y < catGroups.length; y++) {
                String[] catLength = catGroups[y].trim().split(";");
                log.debug("category group " + catLength + " length " + catLength.length);
                String innerCatFilter = "";
                if (y == 0) {
                    catFilter += " and ("; //outer bracket
                    innerCatFilter += " (";
                } else {
                    innerCatFilter += " or (";
                }

                for (int x = 0; x < catLength.length; x++) {
                    String genderFilter = "";
                    String ageFilter = "";
                    log.debug("Select steps categories");
                    String getGenderDtailSQL = "select steps_age_id, steps_gender_id from survey_combine_category where id=" + catLength[x];

                    PreparedStatement ps = null;
                    ResultSet rs = null;
                    Connection conn = null;
                    try {
                        conn = DatabaseSource.getConnection();
                        ps = conn.prepareStatement(getGenderDtailSQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        log.info("Query to run: " + ps.toString());
                        rs = ps.executeQuery();

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
                                innerCatFilter += ageFilter + genderFilter;
                            } else {
                                innerCatFilter += " and " + ageFilter + genderFilter;
                            }

                        }
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

                }

                innerCatFilter += ")";
                catFilter = catFilter + innerCatFilter;
                if (y == catGroups.length - 1) {
                    catFilter += ")"; //outer bracket
                }

            }

        }
        return catFilter;
    }

    private String getKdhsCategoryFilterQueryPart(String category_id) throws DslException {
        String catFilter = "";
        if (category_id.trim().length() != 0) {
            String[] catGroups = category_id.trim().split(",");
            for (int y = 0; y < catGroups.length; y++) {
                String[] catLength = catGroups[y].trim().split(";");

                String innerCatFilter = "";
                if (y == 0) {
                    catFilter += " and ("; //outer bracket
                    innerCatFilter += " (";
                } else {
                    innerCatFilter += " or (";
                }

                for (int x = 0; x < catLength.length; x++) {
                    String categoryFilter = "";
                    String getGenderDtailSQL = "select kdhs_category_id from survey_combine_category where id=" + catLength[x];

                    PreparedStatement ps = null;
                    ResultSet rs = null;
                    Connection conn = null;
                    try {
                        conn = DatabaseSource.getConnection();
                        ps = conn.prepareStatement(getGenderDtailSQL, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                        log.info("Query to run: " + ps.toString());
                        rs = ps.executeQuery();

                        if (rs.next()) {
                            String kdhs_category_id = rs.getString("kdhs_category_id");

                            categoryFilter = " dim_surv_cat.category_id=" + Integer.parseInt(kdhs_category_id);

                            if (x == 0) {
                                innerCatFilter += categoryFilter;
                            } else {
                                innerCatFilter += " and " + categoryFilter;
                            }

                        }
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

                }
                innerCatFilter += ")";
                catFilter = catFilter + innerCatFilter;
                if (y == catGroups.length - 1) {
                    catFilter += ")"; //outer bracket
                }
            }

        }
        return catFilter;
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
            catFilter = getStepsCategoryFilterQueryPart(category_id);

        }
        log.debug("Build survey common org unit join");
        String orgSeg = "";
        if (orgId != null && !orgId.equals("18")) {
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

    private String getKdhsSurveySql(int sourceId, int indicatorId, String orgId, String category_id, String pe) throws DslException {
        log.debug("get kdhs survey sql funct");
        String catFilter = "";
        log.debug("check category_id status");
        if (category_id != null) {
            log.debug("category_id not null");
            catFilter = getKdhsCategoryFilterQueryPart(category_id);
        }
        log.debug("Build survey common org unit join");
        String orgSeg = "";
        if (orgId != null && !orgId.equals("18")) {
            orgSeg = " inner join surv_comm_org comm_org on dim_surv_org.name=comm_org.name and comm_org.id=" + orgId;
        } else {
            orgSeg = " inner join surv_comm_org comm_org on dim_surv_org.name=comm_org.name and comm_org.name='Kenya'";
        }
        if (pe != null) {
            try {
                Integer.parseInt(pe);
                if (pe.length() != 4) {
                    throw new Exception();
                }
            } catch (Exception ex) {
                Message msg = new Message();
                msg.setMesageContent("The period: " + pe + " must be a valid period number");
                msg.setMessageType(MessageType.NUMBER_FORMAT_ERROR);
                throw new DslException(msg);
            }
            catFilter = " and fs.period='" + pe + "' " + catFilter;

        }

        String getSurveyDataSql = "SELECT dim_surv_indicator.indicator_id as id, dim_surv_org.name as org_name,comm_org.id as orgId, surv_comb_cat.id as category_id, "
                + " value as value,fs.period as period, dim_surv_indicator.name as name, '" + DataSource.getSources().get(sourceId) + "' as source,surv_comb_cat.cat as category_name, "
                + " dim_surv_indicator.description as description, '" + sourceId + "' as source_id "
                + " FROM public.fact_kdhs fs "
                + " inner join dim_kdhs_category dim_surv_cat on  fs.category_id = dim_surv_cat.category_id"
                + " inner join dim_khds_orgunit dim_surv_org on dim_surv_org.orgunit_id = fs.orgunit_id"
                + " inner join survey_combine_category surv_comb_cat on surv_comb_cat.kdhs_category_id = dim_surv_cat.category_id "
                + orgSeg
                + "  inner join dim_khds_indicator dim_surv_indicator on fs.indicator_id = dim_surv_indicator.indicator_id"
                + "  where "
                + "  dim_surv_indicator.indicator_id =" + indicatorId
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
    private String getSurveyAvailableDimesions(int sourceId, int indicatorId, String orgId, String pe, String category_id) throws DslException {
        log.debug("get survey available dimesions");
        String sql = "";
        if (sourceId == 2 || sourceId == 3 || sourceId == 4 || sourceId == 5 || sourceId == 6) { //others

            String orgFilter = orgId != null ? " and surv_org.id=" + orgId : "";
            String catFilter = "";
            if (category_id != null) {
                try {
                    catFilter = getSurveyCategoryFilterQueryPart(category_id);
                } catch (DslException ex) {
                    log.error(ex);
                }
            }

            sql = "Select fs.indicator_id, surv_org.name as org_name,surv_org.id as orgunit_id,surv_cat2.id as age_id, surv_cat3.category as category_name, surv_cat3.id as category_id,surv_cat2.category as age,surv_cat.id as gender_id,surv_cat.category as gender FROM fact_survey fs"
                    + " inner join dim_survey_indicator dim_ind  on dim_ind.indicator_id=fs.indicator_id   "
                    + " inner join dim_survey_gender dim_surv_gender on dim_surv_gender.gender_id = fs.gender_id"
                    + " inner join dim_survey_age dim_surv_age on dim_surv_age.age_id = fs.age_id  "
                    + " inner join dim_survey_source source on fs.source_id = source.source_id   "
                    + " inner join dim_survey_orgunit dim_surv_org on fs.orgunit_id=dim_surv_org.orgunit_id   "
                    + " inner join dim_survey_category dim_surv_category on fs.category_id=dim_surv_category.category_id   "
                    + " inner join surv_comm_org surv_org on surv_org.name=dim_surv_org.name "
                    + " inner join survey_category surv_cat on surv_cat.category=dim_surv_gender.name"
                    + " inner join survey_category surv_cat3 on surv_cat3.category=dim_surv_category.name"
                    + " inner join survey_category surv_cat2 on surv_cat2.category=dim_surv_age.age"
                    + " where source.name='" + DataSource.getSources().get(sourceId) + "'"
                    + " and dim_ind.indicator_id =" + indicatorId + orgFilter + catFilter;

        } else if (sourceId == 7) { //steps
            String orgFilter = orgId != null ? " and surv_org.id=" + orgId : "";
            String catFilter = "";
            if (category_id != null) {
                try {
                    catFilter = getStepsCategoryFilterQueryPart(category_id);
                } catch (DslException ex) {
                    log.error(ex);
                }
            }
            sql = "Select fs.indicator_id, surv_org.name as org_name,surv_org.id as orgunit_id,surv_cat2.id as age_id, surv_cat2.category as age,surv_cat.id as gender_id,surv_cat.category as gender "
                    + " FROM public.fact_steps fs "
                    + " inner join dim_steps_age dim_stps_age on  fs.age_id = dim_stps_age.age_id"
                    + " inner join dim_steps_gender dim_stps_gender on  fs.gender_id = dim_stps_gender.gender_id"
                    + " inner join dim_steps_orgunit dim_stps_org on dim_stps_org.orgunit_id = fs.orgunit_id"
                    + " inner join survey_category surv_cat on surv_cat.category=dim_stps_gender.name"
                    + " inner join survey_category surv_cat2 on surv_cat2.category=dim_stps_age.age"
                    + " inner join surv_comm_org surv_org on dim_stps_org.name=surv_org.name"
                    + "  inner join dim_steps_indicator dim_stps_indicator on fs.indicator_id = dim_stps_indicator.indicator_id"
                    + "  where "
                    + "  dim_stps_indicator.indicator_id =" + indicatorId + orgFilter + catFilter;
            log.debug(sql);
        } else if (sourceId == 8) {//kdhs

            String orgFilter = orgId != null ? " and surv_org.id=" + orgId : "";
            String peFilter = pe != null ? " and fs.period='" + pe + "'" : "";
            String categoryFilter = "";
            if (category_id != null) {
                try {
                    categoryFilter = getKdhsCategoryFilterQueryPart(category_id);
                } catch (DslException ex) {
                    log.error(ex);
                }
            }
            sql = "Select fs.indicator_id, surv_org.name as org_name,surv_org.id as orgunit_id,surv_cat.id as age_id,surv_cat.category as category_name, surv_cat.id as category_id, fs.period as period "
                    + " FROM public.fact_kdhs fs "
                    + " inner join dim_kdhs_category dim_surv_cat on  fs.category_id = dim_surv_cat.category_id"
                    + " inner join dim_khds_orgunit dim_kdhs_org on dim_kdhs_org.orgunit_id = fs.orgunit_id"
                    + " inner join survey_category surv_cat on surv_cat.category=dim_surv_cat.name"
                    + " inner join surv_comm_org surv_org on dim_kdhs_org.name=surv_org.name"
                    + "  inner join dim_khds_indicator dim_kdhs_indicator on fs.indicator_id = dim_kdhs_indicator.indicator_id"
                    + "  where "
                    + "  dim_kdhs_indicator.indicator_id =" + indicatorId + orgFilter + categoryFilter + peFilter;
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
    private Map<String, Object> getCoreSurveyData(String queryToRun, String survey_type) throws DslException {
        log.debug("get core survey data values");

        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;

        Map<String, Object> result = new HashMap();
        List<Map> indicatorDetails = new ArrayList();
        List<Map> orgUnits = new ArrayList();
        List<List<Map<String, Object>>> categories = new ArrayList();
        List<String> periods = new ArrayList();
        List<String> addedOrgsUnit = new ArrayList();
        List<String> addedCategoriesUnit = new ArrayList();
        List<String> addedIndicators = new ArrayList();
        List<Map> data = new ArrayList();
        log.debug("Build payload");
        try {
            conn = DatabaseSource.getConnection();
            ps = conn.prepareStatement(queryToRun, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            log.info("Query to run: " + ps.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                //indicator

                String sourceId = rs.getString("source_id");
                int indicatorId = rs.getInt("id");
                String priod = "";
                if (survey_type == "kdhs") {
                    String dbPeriod = rs.getString("period");
                    if (dbPeriod != null) {
                        if (dbPeriod.equals("n/a")) {
                            priod = "";
                        } else {
                            priod = dbPeriod;
                        }
                    }
                }

                String addedVals = sourceId + ":" + Integer.toString(indicatorId) + ":" + priod;
                if (!addedIndicators.contains(addedVals)) {
                    addedIndicators.add(addedVals);
                    Map<String, Object> indicatorDetail = new HashMap();
                    indicatorDetail.put("name", rs.getString("name"));
                    indicatorDetail.put("id", rs.getInt("id"));
                    indicatorDetail.put("source", rs.getString("source"));
                    indicatorDetail.put("source id", sourceId);
                    indicatorDetail.put("description", rs.getString("description"));
                    if (survey_type == "kdhs" && priod.length() != 0) {
                        indicatorDetail.put("period", priod);
                        periods.add(priod);
                    }
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
                String gender = "";
                String age = "";
                String kdhs_category = "";
                String _category = "";

                try {
                    gender = rs.getString("gender");
                } catch (Exception e) {

                }

                try {
                    age = rs.getString("age");
                } catch (Exception e) {

                }

                try {
                    kdhs_category = rs.getString("category_name");
                } catch (Exception e) {

                }

                if (!gender.equals("n/a") && gender.trim().length() != 0) {
                    try {
                        Map<String, Object> categoryHolder = new HashMap();
                        int genderId = rs.getInt("gender_id");
                        categoryHolder.put("name", gender);
                        categoryHolder.put("id", genderId);
                        category.add(categoryHolder);
                        _category += genderId;
                    } catch (Exception e) {

                    }

                }

                if (!age.equals("n/a") && age.trim().length() != 0) {

                    try {
                        Map<String, Object> categoryHolder = new HashMap();
                        int ageId = rs.getInt("age_id");
                        categoryHolder.put("name", age);
                        categoryHolder.put("id", ageId);
                        category.add(categoryHolder);
                        _category += ageId;
                    } catch (Exception e) {

                    }

                }
                if (!kdhs_category.equals("n/a") && kdhs_category.trim().length() != 0) {

                    try {
                        Map<String, Object> categoryHolder = new HashMap();
                        int categoryId = rs.getInt("category_id");
                        categoryHolder.put("name", kdhs_category);
                        categoryHolder.put("id", categoryId);
                        category.add(categoryHolder);
                        _category += categoryId;
                    } catch (Exception e) {

                    }

                }

                if (category.size() != 0) {
                    if (!addedCategoriesUnit.contains(_category) && _category.length() != 0) {
                        categories.add(category);
                        addedCategoriesUnit.add(_category);
                    }

                    dataHolder.put("category", category);
                }

                dataHolder.put("source_id", rs.getInt("source_id"));
                dataHolder.put("indicator_id", rs.getInt("id"));
                dataHolder.put("value", rs.getInt("value"));
                if (survey_type == "kdhs" && priod.length() != 0) {
                    dataHolder.put("period", priod);
                }
                data.add(dataHolder);

            }
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

        result.put("indicators", indicatorDetails);
        result.put("orgunits", orgUnits);
        result.put("categories", categories);
        result.put("periods", periods);
        Map<String, Object> envelop = new HashMap();
        envelop.put("metadata", result);
        envelop.put("data", data);
        return envelop;
    }

    private Map<String, Object> getAvailableDimesionData(String queryToRun, String survey_type) throws DslException {
        log.debug("get available dimension data");

        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;

        Map<String, Object> result = new HashMap();
        List<Map> orgUnits = new ArrayList();
        List<String> addedOrgsUnit = new ArrayList();
        List<String> addedCategoriesUnit = new ArrayList();
        List<List<Map<String, Object>>> categories = new ArrayList();
        List<String> periods = new ArrayList();
        try {
            conn = DatabaseSource.getConnection();
            ps = conn.prepareStatement(queryToRun, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            log.info("Query to run: " + ps.toString());
            rs = ps.executeQuery();
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

                String gender = "";
                String age = "";
                String _category = "";
                String results_category = "";

                try {
                    gender = rs.getString("gender");
                } catch (Exception e) {

                }

                try {
                    age = rs.getString("age");
                } catch (Exception e) {

                }

                try {
                    results_category = rs.getString("category_name");
                } catch (Exception e) {

                }

                if (!gender.equals("n/a") && gender.trim().length() != 0) {
                    try {
                        Map<String, Object> categoryHolder = new HashMap();
                        categoryHolder.put("name", gender);
                        int genderId = rs.getInt("gender_id");
                        categoryHolder.put("id", genderId);
                        category.add(categoryHolder);
                        _category += genderId;
                    } catch (Exception e) {

                    }

                }
                if (!age.equals("n/a") && age.trim().length() != 0) {
                    try {
                        Map<String, Object> categoryHolder = new HashMap();
                        categoryHolder.put("name", age);
                        int ageId = rs.getInt("age_id");
                        categoryHolder.put("id", ageId);
                        category.add(categoryHolder);
                        _category += ageId;
                    } catch (Exception e) {

                    }

                }
                if (!results_category.equals("n/a") && results_category.trim().length() != 0) {

                    try {
                        Map<String, Object> categoryHolder = new HashMap();
                        categoryHolder.put("name", results_category);
                        int catId = rs.getInt("category_id");
                        categoryHolder.put("id", catId);
                        category.add(categoryHolder);
                        _category += catId;
                    } catch (Exception e) {
                        //pass
                    }

                }
                //periods
                if (survey_type.equals("kdhs")) {
                    String period = rs.getString("period");
                    if (period != null && !period.equals("n/a")) {
                        periods.add(period);
                    }
                }
                if (category.size() != 0 && !addedCategoriesUnit.contains(_category) && _category.length() != 0) {
                    addedCategoriesUnit.add(_category);
                    categories.add(category);
                }

            }
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
        log.debug("build payload");
        result.put("orgunits", orgUnits);
        result.put("categories", categories);
        result.put("periods", periods);
        Map<String, Object> envelop = new HashMap();
        envelop.put("available", result);
        return envelop;
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
        log.debug("surv meta type :" + survData.get("metadata"));
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
        String cacheName = sId + iId + orgId + pe + category_id;
        Element ele = cache.get(sId + iId + orgId + pe + category_id);
        Map<String, Object> result = new HashMap();
        if (ele == null) {

            log.info("get survey data values");
            int sourceId = Integer.parseInt(sId);
            int indicatorId = Integer.parseInt(iId);
            if (sourceId == 2 || sourceId == 3 || sourceId == 4 || sourceId == 5 || sourceId == 6) {
                String indicatorQuery = getSurveySql(sourceId, indicatorId, orgId, category_id);
                Map<String, Object> coreSurvey = getCoreSurveyData(indicatorQuery, "others");
                String availableDataDimesions = getSurveyAvailableDimesions(sourceId, indicatorId, orgId, pe, category_id);
                Map<String, Object> surveyDimensions = getAvailableDimesionData(availableDataDimesions, "others");

                log.info("assemble survey payload");
                Map<String, Object> survMeta = (Map<String, Object>) coreSurvey.get("metadata");

                Map<String, Object> survData = new HashMap();
                survData.put("metadata", survMeta);
                survData.put("available", surveyDimensions.get("available"));
                survData.put("data", coreSurvey.get("data"));
                result = resultAssember(survData);
                cache.put(new Element(cacheName, result));
                return result;
            } else if (sourceId == 7) {
                String indicatorQuery = getStepsSurveySql(sourceId, indicatorId, orgId, category_id);
                Map<String, Object> coreSurvey = getCoreSurveyData(indicatorQuery, "steps");
                String availableDataDimesions = getSurveyAvailableDimesions(sourceId, indicatorId, orgId, pe, category_id);
                Map<String, Object> surveyDimensions = getAvailableDimesionData(availableDataDimesions, "steps");
                Map<String, Object> survMeta = (Map<String, Object>) coreSurvey.get("metadata");

                Map<String, Object> survData = new HashMap();
                survData.put("metadata", survMeta);
                survData.put("available", surveyDimensions.get("available"));
                survData.put("data", coreSurvey.get("data"));
                result = resultAssember(survData);
                cache.put(new Element(cacheName, result));
                return result;
            } else if (sourceId == 8) {
                String indicatorQuery = getKdhsSurveySql(sourceId, indicatorId, orgId, category_id, pe);
                Map<String, Object> coreSurvey = getCoreSurveyData(indicatorQuery, "kdhs");
                String availableDataDimesions = getSurveyAvailableDimesions(sourceId, indicatorId, orgId, pe, category_id);
                Map<String, Object> surveyDimensions = getAvailableDimesionData(availableDataDimesions, "kdhs");
                Map<String, Object> survMeta = (Map<String, Object>) coreSurvey.get("metadata");

                Map<String, Object> survData = new HashMap();
                survData.put("metadata", survMeta);
                survData.put("available", surveyDimensions.get("available"));
                survData.put("data", coreSurvey.get("data"));
                result = resultAssember(survData);
                cache.put(new Element(cacheName, result));
                return result;
            } else {
                Message msg = new Message();
                msg.setMesageContent("This id does not exist ");
                msg.setMessageType(MessageType.MISSING_DB_ENRTY_VALUE);
                throw new DslException(msg);
            }
        } else {
            long startTime = System.nanoTime();
            result = (Map<String, Object>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
            return result;
        }

    }

}
