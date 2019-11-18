/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.dao;

import com.healthit.dslservice.DslException;
import com.healthit.dslservice.dto.KephLevel;
import com.healthit.dslservice.dto.adminstrationlevel.Facility;
import com.healthit.dslservice.dto.kmfl.FacilityLevel;
import com.healthit.dslservice.dto.kmfl.FacilityType;
import com.healthit.dslservice.message.Message;
import com.healthit.dslservice.message.MessageType;
import com.healthit.dslservice.util.CacheKeys;
import com.healthit.dslservice.util.Database;
import com.healthit.dslservice.util.DslCache;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
    
    private String getAllFaciltiesCountByType = "Select count(*),ft.\"name\" from common_organisation_unit commorg inner join\n" +
                                                "facilities_facility ff on ff.code=commorg.dhis_organisation_unit_id \n" +
                                                "inner join facilities_facilitytype ft  on ff.facilitytype_sk=ft.facilitytype_sk\n" +
                                                "where level='facility' group by ft.facilitytype_sk;";
    
    private String getFaciltiesByLevels = "Select dhis_organisation_unit_id as id,dhis_organisation_unit_name as name,parentid, '5' as level from"
            + " common_organisation_unit commorg inner join facilities_facility ff on ff.code=commorg.dhis_organisation_unit_id where level='facility' and ff.kephlevel_sk=? order by name desc";
    
    private String getFacilityLevels = "SELECT kephlevel_sk as id,name  FROM public.facilities_kephlevel";

    private String getFacilityTypes = "SELECT facilitytype_sk as id,name  FROM public.facilities_facilitytype";
    
    private String getFacilityRegulatingBody = "SELECT regulatingbody_sk as id,name  FROM public.facilities_regulatingbody";
    
    private String getFaciltiesByRegulatingBody = "Select dhis_organisation_unit_id as id,dhis_organisation_unit_name as name,parentid, '5' as level from"
            + " common_organisation_unit commorg inner join facilities_facility ff on ff.code=commorg.dhis_organisation_unit_id where level='facility' and ff.regulatingbody_sk=? order by name desc";
    
    
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
        String output = (ele == null ? null : ele.getObjectValue().toString());
        //log.info("Element from cache " + output);
        if (ele == null) {
            long startTime = System.nanoTime();
            Database db = new Database();
            ResultSet rs = db.executeQuery(getALlFacilties);
            try {
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
                db.CloseConnection();
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
        //log.info("Element from cache " + output);
        if (ele == null) {
            long startTime = System.nanoTime();
            Database db = new Database();
            ResultSet rs = db.executeQuery(getFacilityLevels);
            try {
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
                db.CloseConnection();
            }
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data " + (endTime - startTime) / 1000000);
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
        String output = (ele == null ? null : ele.getObjectValue().toString());
        //log.info("Element from cache " + output);
        if (ele == null) {
            long startTime = System.nanoTime();
            Database db = new Database();
            ResultSet rs = db.executeQuery(getFacilityTypes);
            try {
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
                db.CloseConnection();
            }
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data " + (endTime - startTime) / 1000000);
        } else {
            long startTime = System.nanoTime();
            facilityTypeList = (List<FacilityType>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return facilityTypeList;
    }

    
    public Map<String,Integer> getAllFacilitiesByType() throws DslException {
        log.info("Fetching facilities by type");
        Element ele = cache.get("AllfaclitiesByType");
        Map facilityCount=new HashMap();
        if (ele == null) {
            long startTime = System.nanoTime();
            Database db = new Database();
            try {
                Connection conn = db.getConn();
                PreparedStatement ps = conn.prepareStatement(getAllFaciltiesCountByType);
                ResultSet rs = ps.executeQuery();
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
                db.CloseConnection();
            }
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data " + (endTime - startTime) / 1000000);
        } else {
            long startTime = System.nanoTime();
            facilityCount = (Map<String,Integer>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return facilityCount;
    }
    
    public List<Facility> getFacilitiesByType(int typeId) throws DslException {
        List<Facility> facilityList = new ArrayList();
        log.info("Fetching facilities by type");
        Element ele = cache.get("faclitiesByType" + typeId);
        String output = (ele == null ? null : ele.getObjectValue().toString());
        if (ele == null) {
            long startTime = System.nanoTime();
            Database db = new Database();
            try {
                Connection conn = db.getConn();
                PreparedStatement ps = conn.prepareStatement(getALlFaciltiesByType);
                ps.setInt(1, typeId);
                ResultSet rs = ps.executeQuery();
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
                db.CloseConnection();
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
    
    public List<Facility> getFacilitiesByLevel(int levelId) throws DslException {
        List<Facility> facilityList = new ArrayList();
        log.info("Fetching facilities by level");
        Element ele = cache.get("faciltiesByLevels" + levelId);
        String output = (ele == null ? null : ele.getObjectValue().toString());
        if (ele == null) {
            long startTime = System.nanoTime();
            Database db = new Database();
            try {
                Connection conn = db.getConn();
                PreparedStatement ps = conn.prepareStatement(getFaciltiesByLevels);
                ps.setInt(1, levelId);
                ResultSet rs = ps.executeQuery();
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
                db.CloseConnection();
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
    

    
    
    
    
    public List<FacilityType> getFacilitiesRegulatingBody() throws DslException {
         
        List<FacilityType> facilityTypeList = new ArrayList();

        log.info("Fetching facilities Regulating Body");
        Element ele = cache.get(CacheKeys.facilityRegulatingBody);
        String output = (ele == null ? null : ele.getObjectValue().toString());
        //log.info("Element from cache " + output);
        if (ele == null) {
            long startTime = System.nanoTime();
            Database db = new Database();
            ResultSet rs = db.executeQuery(getFacilityRegulatingBody);
            try {
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
                db.CloseConnection();
            }
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data " + (endTime - startTime) / 1000000);
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
            long startTime = System.nanoTime();
            Database db = new Database();
            try {
                Connection conn = db.getConn();
                PreparedStatement ps = conn.prepareStatement(getFaciltiesByRegulatingBody);
                ps.setInt(1, regulatingBodyId);
                ResultSet rs = ps.executeQuery();
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
                db.CloseConnection();
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
    
    
    
    public List<FacilityType> getFacilitiesOwnerType() throws DslException {
         
        List<FacilityType> facilityTypeList = new ArrayList();

        log.info("Fetching facilities owner type");
        Element ele = cache.get(CacheKeys.facilityOwnerType);
        String output = (ele == null ? null : ele.getObjectValue().toString());
        //log.info("Element from cache " + output);
        if (ele == null) {
            long startTime = System.nanoTime();
            Database db = new Database();
            ResultSet rs = db.executeQuery(getFacilityOwnerType);
            try {
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
                db.CloseConnection();
            }
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data " + (endTime - startTime) / 1000000);
        } else {
            long startTime = System.nanoTime();
            facilityTypeList = (List<FacilityType>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return facilityTypeList;
    }
    
    public List<Facility> getFacilitiesByOwnerType(int ownerTypeId) throws DslException {
        List<Facility> facilityList = new ArrayList();
        log.info("Fetching facilities by owner type");
        Element ele = cache.get("faciltiesOwnerType" + ownerTypeId);
        String output = (ele == null ? null : ele.getObjectValue().toString());
        if (ele == null) {
            long startTime = System.nanoTime();
            Database db = new Database();
            try {
                Connection conn = db.getConn();
                PreparedStatement ps = conn.prepareStatement(getFaciltiesByOwnerType);
                ps.setInt(1, ownerTypeId);
                ResultSet rs = ps.executeQuery();
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
                db.CloseConnection();
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
