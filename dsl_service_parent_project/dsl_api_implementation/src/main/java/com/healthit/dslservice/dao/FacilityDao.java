/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.dao;

import com.healthit.dslservice.DslException;
import com.healthit.dslservice.dto.KephLevel;
import com.healthit.dslservice.dto.OrgUnitAmenities;
import com.healthit.dslservice.dto.adminstrationlevel.Facility;
import com.healthit.dslservice.dto.kmfl.FacilityLevel;
import com.healthit.dslservice.dto.kmfl.FacilityType;
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
import java.util.HashMap;
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
public class FacilityDao {

    final static Logger log = Logger.getLogger(FacilityDao.class);
    private String getALlFacilties = "Select dhis_organisation_unit_id as id,dhis_organisation_unit_name as name,parentid,'5' as level from common_organisation_unit where level='facility' order by name desc";

    private String getALlFaciltiesByType = "Select dhis_organisation_unit_id as id,dhis_organisation_unit_name as name,parentid, '5' as level from"
            + " common_organisation_unit commorg inner join facilities_facility ff on ff.code=commorg.dhis_organisation_unit_id where level='facility' and ff.facilitytype_sk=? order by name desc";

    private String getAllFaciltiesCountByType = "Select count(*),ft.\"name\" from common_organisation_unit commorg inner join\n"
            + "facilities_facility ff on ff.code=commorg.dhis_organisation_unit_id \n"
            + "inner join facilities_facilitytype ft  on ff.facilitytype_sk=ft.facilitytype_sk\n"
            + "where level='facility' group by ft.facilitytype_sk;";

    private String getFaciltiesNumberOfBeds = "Select sum(number_of_beds) from facilities_facility ff where \n"
            + "CASE WHEN (select \"level\" from common_organisation_unit where dhis_organisation_unit_id=)"
            + " as level='facility' THEN ff.code=(select mfl_code from common_organisation_unit where "
            + "dhis_organisation_unit_id=)"
            + "common_organisation_unit commorg on ff.code=commorg.dhis_organisation_unit_id \n"
            + "where level='facility' group by ft.facilitytype_sk;";

// bed count
    private String getNumberOfBeds = "Select sum(number_of_beds) as count from facilities_facility ";

    private String getNumberOfBedsPerFacility = "Select sum(number_of_beds) as count,ff.code as code,ff.name as name, 5 as level,ff.ward_sk as parentid from facilities_facility ff  where ff.code in(@orgunit@)";

    private String wardNoOfBedsSeg = "Select sum(number_of_beds) as count,commorg_ward.dhis_organisation_unit_id as code,commorg_ward.dhis_organisation_unit_name as name, commorg_ward.parentid,4 as level\n"
            + "from facilities_facility  ff inner join\n"
            + " common_organisation_unit commorg on ff.code=commorg.mfl_code \n"
            + " inner join common_organisation_unit commorg_ward on commorg.parentid=commorg_ward.dhis_organisation_unit_id\n"
            + " where commorg_ward.dhis_organisation_unit_id in(@orgunit@)\n"
            + " group by commorg_ward.dhis_organisation_unit_id,commorg_ward.dhis_organisation_unit_name, commorg_ward.parentid";

    private String subCountyNoOfBedsSeg = "Select sum(number_of_beds) as count,commorg_subcount.dhis_organisation_unit_id as code,commorg_subcount.dhis_organisation_unit_name as name, commorg_subcount.parentid,3 as level\n"
            + "from facilities_facility  ff inner join\n"
            + " common_organisation_unit commorg on ff.code=commorg.mfl_code \n"
            + " inner join common_organisation_unit commorg_ward on commorg.parentid=commorg_ward.dhis_organisation_unit_id\n"
            + " inner join common_organisation_unit commorg_subcount on commorg_ward.parentid=commorg_subcount.dhis_organisation_unit_id\n"
            + " where commorg_subcount.dhis_organisation_unit_id in(@orgunit@)\n"
            + " group by commorg_subcount.dhis_organisation_unit_id,commorg_subcount.dhis_organisation_unit_name, commorg_subcount.parentid";

    private String countyNoOfBedsSeg = "Select sum(number_of_beds) as count,commorg_county.dhis_organisation_unit_id as code,commorg_county.dhis_organisation_unit_name as name, commorg_county.parentid,2 as level\n"
            + "from facilities_facility  ff inner join\n"
            + " common_organisation_unit commorg on ff.code=commorg.mfl_code \n"
            + " inner join common_organisation_unit commorg_ward on commorg.parentid=commorg_ward.dhis_organisation_unit_id\n"
            + " inner join common_organisation_unit commorg_subcount on commorg_ward.parentid=commorg_subcount.dhis_organisation_unit_id\n"
            + " inner join common_organisation_unit commorg_county on commorg_subcount.parentid=commorg_county.dhis_organisation_unit_id\n"
            + " where commorg_county.dhis_organisation_unit_id in(@orgunit@)\n"
            + " group by commorg_county.dhis_organisation_unit_id,commorg_county.dhis_organisation_unit_name, commorg_county.parentid";

    //===========>bed count end<===========
    //cots count
    private String getNumberOfCots = "Select sum(number_of_cots) as count from facilities_facility ";

    private String getNumberOfCotsPerFacility = "Select sum(number_of_cots) as count,ff.code as code,ff.name as name, 5 as level,ff.ward_sk as parentid from facilities_facility ff  where ff.code in(@orgunit@)";

    private String wardNoOfCotsSeg = "Select sum(number_of_cots) as count,commorg_ward.dhis_organisation_unit_id as code,commorg_ward.dhis_organisation_unit_name as name, commorg_ward.parentid,4 as level\n"
            + "from facilities_facility  ff inner join\n"
            + " common_organisation_unit commorg on ff.code=commorg.mfl_code \n"
            + " inner join common_organisation_unit commorg_ward on commorg.parentid=commorg_ward.dhis_organisation_unit_id\n"
            + " where commorg_ward.dhis_organisation_unit_id in(@orgunit@)\n"
            + " group by commorg_ward.dhis_organisation_unit_id,commorg_ward.dhis_organisation_unit_name, commorg_ward.parentid";

    private String subCountyNoOfCotsSeg = "Select sum(number_of_cots) as count,commorg_subcount.dhis_organisation_unit_id as code,commorg_subcount.dhis_organisation_unit_name as name, commorg_subcount.parentid,3 as level\n"
            + "from facilities_facility  ff inner join\n"
            + " common_organisation_unit commorg on ff.code=commorg.mfl_code \n"
            + " inner join common_organisation_unit commorg_ward on commorg.parentid=commorg_ward.dhis_organisation_unit_id\n"
            + " inner join common_organisation_unit commorg_subcount on commorg_ward.parentid=commorg_subcount.dhis_organisation_unit_id\n"
            + " where commorg_subcount.dhis_organisation_unit_id in(@orgunit@)\n"
            + " group by commorg_subcount.dhis_organisation_unit_id,commorg_subcount.dhis_organisation_unit_name, commorg_subcount.parentid";

    private String countyNoOfCotsSeg = "Select sum(number_of_cots) as count,commorg_county.dhis_organisation_unit_id as code,commorg_county.dhis_organisation_unit_name as name, commorg_county.parentid,2 as level\n"
            + "from facilities_facility  ff inner join\n"
            + " common_organisation_unit commorg on ff.code=commorg.mfl_code \n"
            + " inner join common_organisation_unit commorg_ward on commorg.parentid=commorg_ward.dhis_organisation_unit_id\n"
            + " inner join common_organisation_unit commorg_subcount on commorg_ward.parentid=commorg_subcount.dhis_organisation_unit_id\n"
            + " inner join common_organisation_unit commorg_county on commorg_subcount.parentid=commorg_county.dhis_organisation_unit_id\n"
            + " where commorg_county.dhis_organisation_unit_id in(@orgunit@)\n"
            + " group by commorg_county.dhis_organisation_unit_id,commorg_county.dhis_organisation_unit_name, commorg_county.parentid";
    //==============> cot count end <===============

    private String getFaciltiesByLevels = "Select dhis_organisation_unit_id as id,dhis_organisation_unit_name as name,parentid, '5' as level from"
            + " common_organisation_unit commorg inner join facilities_facility ff on ff.code=commorg.dhis_organisation_unit_id where level='facility' and ff.kephlevel_sk=? order by name desc";

    private String getFacilityLevels = "SELECT kephlevel_sk as id,name  FROM public.facilities_kephlevel";

    private String getFacilityTypes = "SELECT facilitytype_sk as id,name  FROM public.facilities_facilitytype";

    private String getFacilityRegulatingBody = "SELECT regulatingbody_sk as id,name  FROM public.facilities_regulatingbody";

    private String getFaciltiesByRegulatingBody = "Select dhis_organisation_unit_id as id,dhis_organisation_unit_name as name,parentid, '5' as level from"
            + " common_organisation_unit commorg inner join facilities_facility ff on ff.code=commorg.dhis_organisation_unit_id where level='facility' and ff.regulatingbody_sk=? order by name desc";

    private String getOrgUnitLevelQry = "Select hierarchylevel,dhis_organisation_unit_id from common_organisation_unit where CAST(dhis_organisation_unit_id AS varchar) in(?)";

    private String getFacilityOwnerType = "SELECT ownertype_sk as id,name  FROM public.facilities_ownertype";

    private String getFaciltiesByOwnerType = "Select dhis_organisation_unit_id as id,dhis_organisation_unit_name as name,parentid,'5' as level from"
            + " common_organisation_unit commorg inner join facilities_facility ff on ff.code=commorg.dhis_organisation_unit_id "
            + "inner join facilities_owner fo on fo.owner_sk=ff.owner_sk inner join facilities_ownertype fot on fot.id=fo.owner_type_id  where "
            + "level='facility' and fot.ownertype_sk=? order by name desc";

    Cache cache = DslCache.getCache();

    public List<Facility> getFacilities() throws DslException {
        List<Facility> facilityList = new ArrayList();

        log.info("Fetching facilities");
        Element ele = cache.get(CacheKeys.facilityList);
        //log.info("Element from cache " + output);
        if (ele == null) {
            long startTime = System.nanoTime();

            PreparedStatement ps = null;
            ResultSet rs = null;
            Connection conn = null;
            try {
                conn = DatabaseSource.getConnection();
                ps = conn.prepareStatement(getALlFacilties, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                log.info("Query to run: " + ps.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    Facility facility = new Facility();
                    facility.setParentid(rs.getString("parentid"));
                    facility.setLevel(Integer.parseInt(rs.getString("level")));
                    //facility.setFacilityOwner(rs.getString("owner_id"));
                    facility.setId(rs.getString("id"));
                    //KephLevel l = KephLevel.getKephLevel(Integer.parseInt(rs.getString("kephlevel_sk")));
                    //facility.setKephLevel(l);
                    facility.setName(rs.getString("name"));
                    //facility.setSubCountyId(rs.getString("sub_county_id"));
                    facilityList.add(facility);
                }
                cache.put(new Element(CacheKeys.facilityList, facilityList));

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
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data " + (endTime - startTime) / 1000000);
        } else {
            long startTime = System.nanoTime();
            facilityList = (List<Facility>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return facilityList;
    }

    public List<FacilityLevel> getFacilitiesLevel() throws DslException {
        List<FacilityLevel> facilityLevelList = new ArrayList();

        log.info("Fetching facilities level");
        Element ele = cache.get(CacheKeys.facilityLevel);
        String output = (ele == null ? null : ele.getObjectValue().toString());
        if (ele == null) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Connection conn = null;

            try {
                conn = DatabaseSource.getConnection();
                ps = conn.prepareStatement(getFacilityLevels, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                log.info("Query to run: " + ps.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    FacilityLevel facilityLevel = new FacilityLevel();
                    facilityLevel.setId(rs.getString("id"));
                    facilityLevel.setName(rs.getString("name"));
                    facilityLevelList.add(facilityLevel);
                }
                cache.put(new Element(CacheKeys.facilityLevel, facilityLevelList));

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
            facilityLevelList = (List<FacilityLevel>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return facilityLevelList;
    }

    public List<FacilityType> getFacilitiesType() throws DslException {
        List<FacilityType> facilityTypeList = new ArrayList();

        log.info("Fetching facilities types");
        Element ele = cache.get(CacheKeys.facilityType);
        if (ele == null) {
            PreparedStatement ps = null;
            ResultSet rs = null;
            Connection conn = null;
            try {
                conn = DatabaseSource.getConnection();
                ps = conn.prepareStatement(getFacilityTypes, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                log.info("Query to run: " + ps.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    FacilityType facilityType = new FacilityType();
                    facilityType.setId(rs.getString("id"));
                    facilityType.setName(rs.getString("name"));
                    facilityTypeList.add(facilityType);
                }
                cache.put(new Element(CacheKeys.facilityType, facilityTypeList));

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
            facilityTypeList = (List<FacilityType>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return facilityTypeList;
    }

    public Map<String, Integer> getAllFacilitiesByType() throws DslException {
        log.info("Fetching facilities by type");
        Element ele = cache.get("AllfaclitiesByType");
        Map facilityCount = new HashMap();
        if (ele == null) {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;

            try {
                conn = DatabaseSource.getConnection();
                ps = conn.prepareStatement(getAllFaciltiesCountByType);
                rs = ps.executeQuery();
                while (rs.next()) {
                    facilityCount.put(rs.getString("name"), rs.getString("count"));
                }
                cache.put(new Element("AllfaclitiesByType", facilityCount));

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
            facilityCount = (Map<String, Integer>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return facilityCount;
    }

    public List<Facility> getFacilitiesByType(int typeId) throws DslException {
        List<Facility> facilityList = new ArrayList();
        log.info("Fetching facilities by type");
        Element ele = cache.get("faclitiesByType" + typeId);
        if (ele == null) {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                conn = DatabaseSource.getConnection();
                ps = conn.prepareStatement(getALlFaciltiesByType);
                ps.setInt(1, typeId);
                rs = ps.executeQuery();
                while (rs.next()) {
                    Facility facility = new Facility();
                    facility.setParentid(rs.getString("parentid"));
                    facility.setId(rs.getString("id"));
                    facility.setLevel(Integer.parseInt(rs.getString("level")));
                    facility.setName(rs.getString("name"));
                    facilityList.add(facility);
                }
                cache.put(new Element("faclitiesByType" + typeId, facilityList));

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
            facilityList = (List<Facility>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return facilityList;
    }

    public List<Facility> getFacilitiesByLevel(int levelId) throws DslException {
        List<Facility> facilityList = new ArrayList();
        log.info("Fetching facilities by level");
        Element ele = cache.get("faciltiesByLevels" + levelId);
        if (ele == null) {
            long startTime = System.nanoTime();
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                conn = DatabaseSource.getConnection();
                ps = conn.prepareStatement(getFaciltiesByLevels);
                ps.setInt(1, levelId);
                rs = ps.executeQuery();
                while (rs.next()) {
                    Facility facility = new Facility();
                    facility.setParentid(rs.getString("parentid"));
                    facility.setId(rs.getString("id"));
                    facility.setLevel(Integer.parseInt(rs.getString("level")));
                    facility.setName(rs.getString("name"));
                    facilityList.add(facility);
                }
                cache.put(new Element("faciltiesByLevels" + levelId, facilityList));
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
            facilityList = (List<Facility>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return facilityList;
    }

    public List<FacilityType> getFacilitiesRegulatingBody() throws DslException {

        List<FacilityType> facilityTypeList = new ArrayList();

        log.info("Fetching facilities Regulating Body");
        Element ele = cache.get(CacheKeys.facilityRegulatingBody);
        String output = (ele == null ? null : ele.getObjectValue().toString());
        //log.info("Element from cache " + output);
        if (ele == null) {

            PreparedStatement ps = null;
            ResultSet rs = null;
            Connection conn = null;
            try {
                conn = DatabaseSource.getConnection();
                ps = conn.prepareStatement(getFacilityRegulatingBody, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                log.info("Query to run: " + ps.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    FacilityType facilityType = new FacilityType();
                    facilityType.setId(rs.getString("id"));
                    facilityType.setName(rs.getString("name"));
                    facilityTypeList.add(facilityType);
                }
                cache.put(new Element(CacheKeys.facilityRegulatingBody, facilityTypeList));

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
            facilityTypeList = (List<FacilityType>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return facilityTypeList;
    }

    public List<Facility> getFacilitiesByRegulatingBody(int regulatingBodyId) throws DslException {
        List<Facility> facilityList = new ArrayList();
        log.info("Fetching facilities by level");
        Element ele = cache.get("faciltiesByregulatingBody" + regulatingBodyId);
        String output = (ele == null ? null : ele.getObjectValue().toString());
        if (ele == null) {
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                conn = conn = DatabaseSource.getConnection();
                ps = conn.prepareStatement(getFaciltiesByRegulatingBody);
                ps.setInt(1, regulatingBodyId);
                rs = ps.executeQuery();
                while (rs.next()) {
                    Facility facility = new Facility();
                    facility.setParentid(rs.getString("parentid"));
                    facility.setId(rs.getString("id"));
                    facility.setName(rs.getString("name"));
                    facilityList.add(facility);
                }
                cache.put(new Element("faciltiesByregulatingBody" + regulatingBodyId, facilityList));
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
            facilityList = (List<Facility>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return facilityList;
    }

    public List<FacilityType> getFacilitiesOwnerType() throws DslException {

        List<FacilityType> facilityTypeList = new ArrayList();

        log.info("Fetching facilities owner type");
        Element ele = cache.get(CacheKeys.facilityOwnerType);
        String output = (ele == null ? null : ele.getObjectValue().toString());
        if (ele == null) {

            PreparedStatement ps = null;
            ResultSet rs = null;
            Connection conn = null;
            try {
                conn = DatabaseSource.getConnection();
                ps = conn.prepareStatement(getFacilityOwnerType, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                log.info("Query to run: " + ps.toString());
                rs = ps.executeQuery();

                while (rs.next()) {
                    FacilityType facilityType = new FacilityType();
                    facilityType.setId(rs.getString("id"));
                    facilityType.setName(rs.getString("name"));
                    facilityTypeList.add(facilityType);
                }
                cache.put(new Element(CacheKeys.facilityOwnerType, facilityTypeList));

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
            facilityTypeList = (List<FacilityType>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return facilityTypeList;
    }

    private Map<Integer, List> getOrgIdAndTheirLevels(String ouid) throws DslException {

        StringBuilder qToRun = new StringBuilder("Select hierarchylevel,dhis_organisation_unit_id from common_organisation_unit where dhis_organisation_unit_id in(");
        String ouidList[] = ouid.split(";");
        for (int x = 0; x < ouidList.length; x++) {
            if (x == 0) {
                qToRun.append(Integer.parseInt(ouidList[x]));
            } else {
                qToRun.append("," + Integer.parseInt(ouidList[x]));
            }
        }

        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        Map<Integer, List> orgLevel = new HashMap();
        try {
            conn = DatabaseSource.getConnection();
            ps = conn.prepareStatement(qToRun.toString() + ")", ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
            log.info("Query to run: " + ps.toString());
            rs = ps.executeQuery();
            while (rs.next()) {
                int hierarchylevel = rs.getInt("hierarchylevel");
                if (orgLevel.containsKey(hierarchylevel)) {
                    orgLevel.get(hierarchylevel).add(rs.getInt("dhis_organisation_unit_id"));
                } else {
                    List<Integer> orunitId = new ArrayList();
                    orunitId.add(rs.getInt("dhis_organisation_unit_id"));
                    orgLevel.put(hierarchylevel, orunitId);
                }

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

        return orgLevel;
    }

    private String orgUnitsList(String ouid) {

        String[] orgUnits = ouid.split(";");
        StringBuilder orgUnitParameters = new StringBuilder();

        for (int x = 0; x < orgUnits.length; x++) {
            if (x == 0) {
                orgUnitParameters.append(orgUnits[x]);
            } else {
                orgUnitParameters.append("," + orgUnits[x]);
            }
        }
        return orgUnitParameters.toString();
    }

    private List<OrgUnitAmenities> getCotCapacity(Map<Integer, List> orgLevel) throws DslException {
        List<OrgUnitAmenities> orgUnitCotCapacityList = new ArrayList();

        PreparedStatement ps = null;
        ResultSet rsCout = null;
        Connection conn = null;
        try {
            conn = DatabaseSource.getConnection();

            for (Map.Entry<Integer, List> entry : orgLevel.entrySet()) {
                System.out.println(entry.getKey() + "/" + entry.getValue());

                log.info("org unit level: " + entry.getKey());
                if (entry.getKey() == 1 || entry.getKey() == null) {

                    ps = conn.prepareStatement(getNumberOfCots, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    log.info("Query to run: " + ps.toString());
                    rsCout = ps.executeQuery();

                    while (rsCout.next()) {
                        OrgUnitAmenities orgAmenities = new OrgUnitAmenities();
                        orgAmenities.setCount(rsCout.getInt("count"));
                        orgAmenities.setId("18");
                        orgAmenities.setLevel(1);
                        orgAmenities.setName("Kenya");
                        orgAmenities.setParentid("0");
                        orgUnitCotCapacityList.add(orgAmenities);
                    }
                } else if (entry.getKey() == 2) {
                    countyNoOfCotsSeg = countyNoOfCotsSeg.replaceAll("@orgunit@", orgUnitsList(entry.getValue().toString().replace("[", "").replace("]", "").replaceAll("\"", "")));

                    ps = conn.prepareStatement(countyNoOfCotsSeg, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    log.info("Query to run: " + ps.toString());
                    rsCout = ps.executeQuery();

                    while (rsCout.next()) {
                        OrgUnitAmenities orgAmenities = new OrgUnitAmenities();
                        orgAmenities.setCount(rsCout.getInt("count"));
                        orgAmenities.setId(rsCout.getString("code"));
                        orgAmenities.setLevel(rsCout.getInt("level"));
                        orgAmenities.setName(rsCout.getString("name"));
                        orgAmenities.setParentid("18");
                        orgUnitCotCapacityList.add(orgAmenities);
                    }

                } else if (entry.getKey() == 3) {
                    subCountyNoOfCotsSeg = subCountyNoOfCotsSeg.replaceAll("@orgunit@", orgUnitsList(entry.getValue().toString().replace("[", "").replace("]", "").replaceAll("\"", "")));

                    ps = conn.prepareStatement(subCountyNoOfCotsSeg, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    log.info("Query to run: " + ps.toString());
                    rsCout = ps.executeQuery();

                    while (rsCout.next()) {
                        OrgUnitAmenities orgAmenities = new OrgUnitAmenities();
                        orgAmenities.setCount(rsCout.getInt("count"));
                        orgAmenities.setId(rsCout.getString("code"));
                        orgAmenities.setLevel(rsCout.getInt("level"));
                        orgAmenities.setName(rsCout.getString("name"));
                        orgAmenities.setParentid(rsCout.getString("parentid"));
                        orgUnitCotCapacityList.add(orgAmenities);
                    }
                } else if (entry.getKey() == 4) {
                    wardNoOfCotsSeg = wardNoOfCotsSeg.replaceAll("@orgunit@", orgUnitsList(entry.getValue().toString().replace("[", "").replace("]", "").replaceAll("\"", "")));

                    ps = conn.prepareStatement(wardNoOfCotsSeg, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    log.info("Query to run: " + ps.toString());
                    rsCout = ps.executeQuery();

                    while (rsCout.next()) {
                        OrgUnitAmenities orgAmenities = new OrgUnitAmenities();
                        orgAmenities.setCount(rsCout.getInt("count"));
                        orgAmenities.setId(rsCout.getString("code"));
                        orgAmenities.setLevel(rsCout.getInt("level"));
                        orgAmenities.setName(rsCout.getString("name"));
                        orgAmenities.setParentid(rsCout.getString("parentid"));
                        orgUnitCotCapacityList.add(orgAmenities);
                    }
                } else if (entry.getKey() == 5) {

                    getNumberOfCotsPerFacility = getNumberOfCotsPerFacility.replaceAll("@orgunit@", orgUnitsList(entry.getValue().toString().replace("[", "").replace("]", "").replaceAll("\"", "")));

                    ps = conn.prepareStatement(getNumberOfCotsPerFacility, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    log.info("Query to run: " + ps.toString());
                    rsCout = ps.executeQuery();

                    while (rsCout.next()) {
                        OrgUnitAmenities orgAmenities = new OrgUnitAmenities();
                        orgAmenities.setCount(rsCout.getInt("count"));
                        orgAmenities.setId(rsCout.getString("code"));
                        orgAmenities.setLevel(rsCout.getInt("level"));
                        orgAmenities.setName(rsCout.getString("name"));
                        orgAmenities.setParentid(rsCout.getString("parentid"));
                        orgUnitCotCapacityList.add(orgAmenities);
                    }
                }
            }
        } catch (SQLException ex) {
            Message msg = new Message();
            msg.setMessageType(MessageType.SQL_QUERY_ERROR);
            msg.setMesageContent(ex.getMessage());
            throw new DslException(msg);
        } finally {
            DatabaseSource.close(rsCout);
            DatabaseSource.close(ps);
            DatabaseSource.close(conn);
        }
        return orgUnitCotCapacityList;
    }

    private List<OrgUnitAmenities> getBedCapacity(Map<Integer, List> orgLevel) throws DslException {

        List<OrgUnitAmenities> orgUnitBedCapacityList = new ArrayList();
        PreparedStatement ps = null;
        ResultSet rsCout = null;
        Connection conn = null;
        try {
            conn = DatabaseSource.getConnection();

            for (Map.Entry<Integer, List> entry : orgLevel.entrySet()) {
                System.out.println(entry.getKey() + "/" + entry.getValue());

                log.info("org unit level: " + entry.getKey());
                if (entry.getKey() == 1 || entry.getKey() == null) {

                    ps = conn.prepareStatement(getNumberOfBeds, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    log.info("Query to run: " + ps.toString());
                    rsCout = ps.executeQuery();

                    while (rsCout.next()) {
                        OrgUnitAmenities orgAmenities = new OrgUnitAmenities();
                        orgAmenities.setCount(rsCout.getInt("count"));
                        orgAmenities.setId("18");
                        orgAmenities.setLevel(1);
                        orgAmenities.setName("Kenya");
                        orgAmenities.setParentid("0");
                        orgUnitBedCapacityList.add(orgAmenities);
                    }
                } else if (entry.getKey() == 2) {
                    countyNoOfBedsSeg = countyNoOfBedsSeg.replaceAll("@orgunit@", orgUnitsList(entry.getValue().toString().replace("[", "").replace("]", "").replaceAll("\"", "")));

                    ps = conn.prepareStatement(countyNoOfBedsSeg, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    log.info("Query to run: " + ps.toString());
                    rsCout = ps.executeQuery();

                    while (rsCout.next()) {
                        OrgUnitAmenities orgAmenities = new OrgUnitAmenities();
                        orgAmenities.setCount(rsCout.getInt("count"));
                        orgAmenities.setId(rsCout.getString("code"));
                        orgAmenities.setLevel(rsCout.getInt("level"));
                        orgAmenities.setName(rsCout.getString("name"));
                        orgAmenities.setParentid("18");
                        orgUnitBedCapacityList.add(orgAmenities);
                    }

                } else if (entry.getKey() == 3) {
                    subCountyNoOfBedsSeg = subCountyNoOfBedsSeg.replaceAll("@orgunit@", orgUnitsList(entry.getValue().toString().replace("[", "").replace("]", "").replaceAll("\"", "")));

                    ps = conn.prepareStatement(subCountyNoOfBedsSeg, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    log.info("Query to run: " + ps.toString());
                    rsCout = ps.executeQuery();

                    while (rsCout.next()) {
                        OrgUnitAmenities orgAmenities = new OrgUnitAmenities();
                        orgAmenities.setCount(rsCout.getInt("count"));
                        orgAmenities.setId(rsCout.getString("code"));
                        orgAmenities.setLevel(rsCout.getInt("level"));
                        orgAmenities.setName(rsCout.getString("name"));
                        orgAmenities.setParentid(rsCout.getString("parentid"));
                        orgUnitBedCapacityList.add(orgAmenities);
                    }
                } else if (entry.getKey() == 4) {
                    wardNoOfBedsSeg = wardNoOfBedsSeg.replaceAll("@orgunit@", orgUnitsList(entry.getValue().toString().replace("[", "").replace("]", "").replaceAll("\"", "")));

                    ps = conn.prepareStatement(wardNoOfBedsSeg, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    log.info("Query to run: " + ps.toString());
                    rsCout = ps.executeQuery();

                    while (rsCout.next()) {
                        OrgUnitAmenities orgAmenities = new OrgUnitAmenities();
                        orgAmenities.setCount(rsCout.getInt("count"));
                        orgAmenities.setId(rsCout.getString("code"));
                        orgAmenities.setLevel(rsCout.getInt("level"));
                        orgAmenities.setName(rsCout.getString("name"));
                        orgAmenities.setParentid(rsCout.getString("parentid"));
                        orgUnitBedCapacityList.add(orgAmenities);
                    }
                } else if (entry.getKey() == 5) {

                    getNumberOfBedsPerFacility = getNumberOfBedsPerFacility.replaceAll("@orgunit@", orgUnitsList(entry.getValue().toString().replace("[", "").replace("]", "").replaceAll("\"", "")));

                    ps = conn.prepareStatement(getNumberOfBedsPerFacility, ResultSet.TYPE_SCROLL_SENSITIVE, ResultSet.CONCUR_UPDATABLE);
                    log.info("Query to run: " + ps.toString());
                    rsCout = ps.executeQuery();

                    while (rsCout.next()) {
                        OrgUnitAmenities orgAmenities = new OrgUnitAmenities();
                        orgAmenities.setCount(rsCout.getInt("count"));
                        orgAmenities.setId(rsCout.getString("code"));
                        orgAmenities.setLevel(rsCout.getInt("level"));
                        orgAmenities.setName(rsCout.getString("name"));
                        orgAmenities.setParentid(rsCout.getString("parentid"));
                        orgUnitBedCapacityList.add(orgAmenities);
                    }
                }
            }

        } catch (SQLException ex) {
            Message msg = new Message();
            msg.setMessageType(MessageType.SQL_QUERY_ERROR);
            msg.setMesageContent(ex.getMessage());
            throw new DslException(msg);
        } finally {
            DatabaseSource.close(rsCout);
            DatabaseSource.close(ps);
            DatabaseSource.close(conn);
        }
        return orgUnitBedCapacityList;
    }

    public List<OrgUnitAmenities> getFacilitiesBedCapacity(String ouid) throws DslException {
        List<OrgUnitAmenities> orgUnitBedCapacity = null;
        log.info("Fetching facilities owner type");
        Element ele = cache.get("bed" + ouid);
        String output = (ele == null ? null : ele.getObjectValue().toString());
        //log.info("Element from cache " + output);
        if (ele == null) {

            Map<Integer, List> orgLevel = getOrgIdAndTheirLevels(ouid);
            orgUnitBedCapacity = getBedCapacity(orgLevel);
            cache.put(new Element("bed" + ouid, orgUnitBedCapacity));

        } else {
            long startTime = System.nanoTime();
            orgUnitBedCapacity = (List<OrgUnitAmenities>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return orgUnitBedCapacity;
    }

    public List<OrgUnitAmenities> getFacilitiesCotCapacity(String ouid) throws DslException {

        List<OrgUnitAmenities> orgUnitCotCapacity = null;
        log.info("Fetching facilities owner type");
        Element ele = cache.get("cot" + ouid);
        String output = (ele == null ? null : ele.getObjectValue().toString());
        //log.info("Element from cache " + output);
        if (ele == null) {
            long startTime = System.nanoTime();

            Map<Integer, List> orgLevel = getOrgIdAndTheirLevels(ouid);
            orgUnitCotCapacity = getCotCapacity(orgLevel);
            cache.put(new Element("cot" + ouid, orgUnitCotCapacity));

            long endTime = System.nanoTime();
            log.info("Time taken to fetch data " + (endTime - startTime) / 1000000);
        } else {
            long startTime = System.nanoTime();
            orgUnitCotCapacity = (List<OrgUnitAmenities>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return orgUnitCotCapacity;
    }

    public List<Facility> getFacilitiesByOwnerType(int ownerTypeId) throws DslException {
        List<Facility> facilityList = new ArrayList();
        log.info("Fetching facilities by owner type");
        Element ele = cache.get("faciltiesOwnerType" + ownerTypeId);
        String output = (ele == null ? null : ele.getObjectValue().toString());
        if (ele == null) {
            long startTime = System.nanoTime();
            Connection conn = null;
            PreparedStatement ps = null;
            ResultSet rs = null;
            try {
                conn = DatabaseSource.getConnection();
                ps = conn.prepareStatement(getFaciltiesByOwnerType);
                ps.setInt(1, ownerTypeId);
                rs = ps.executeQuery();
                while (rs.next()) {
                    Facility facility = new Facility();
                    facility.setParentid(rs.getString("parentid"));
                    facility.setLevel(Integer.parseInt(rs.getString("level")));
                    facility.setId(rs.getString("id"));
                    facility.setName(rs.getString("name"));
                    facilityList.add(facility);
                }
                cache.put(new Element("faciltiesOwnerType" + ownerTypeId, facilityList));
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
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data " + (endTime - startTime) / 1000000);
        } else {
            long startTime = System.nanoTime();
            facilityList = (List<Facility>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return facilityList;
    }

}
