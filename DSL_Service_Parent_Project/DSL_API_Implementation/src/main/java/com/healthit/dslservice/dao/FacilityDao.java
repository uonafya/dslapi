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
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.log4j.Logger;

/**
 *
 * @author duncan
 */
public class FacilityDao {

    final static Logger log = Logger.getLogger(FacilityDao.class);
    private String getALlFacilties = "Select dhis_organisation_unit_id as id,dhis_organisation_unit_name as name,parentid from common_organisation_unit where level='facility' order by name desc";
    
    private String getFacilityLevels = "SELECT kephlevel_sk as id,name  FROM public.facilities_kephlevel";
    
    private String getFacilityTypes = "SELECT facilitytype_sk as id,name  FROM public.facilities_facilitytype";

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
                    facility.setWardId(rs.getString("parentid"));
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
                Message msg=new Message();
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
                Message msg=new Message();
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
                Message msg=new Message();
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
    
}
