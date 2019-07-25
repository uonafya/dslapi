/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dsl_api_impl.model.humanresource;

import com.healthit.cache.CacheKeys;
import com.healthit.cache.DslCache;
import com.healthit.database.Database;
//import com.healthit.dslservice.dao.FacilityDao;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.log4j.Logger;
import org.apache.olingo.commons.api.data.Entity;
import org.apache.olingo.commons.api.data.EntityCollection;
import org.apache.olingo.commons.api.data.Property;
import org.apache.olingo.commons.api.data.ValueType;

/**
 *
 * @author duncan
 */
public class IhrisDao {

    final static Logger log = Logger.getLogger(IhrisDao.class);
    Cache cache = DslCache.getCache();

    private String getALlCadreGroup = "Select cadreid,cadrename from dim_ihris_cadre";
    private String getALlCadre = "Select uid as id,dataelementname as cadrename, cadreid as cadre_group_id from dim_ihris_dataelement";
    private String getCadreAllocation = "Select dataelementid as cadreid,periodid,mflcode,value from fact_ihris_datavalue where periodid is not null";

//    public List<CadreAllocation> getCadreAllocation() throws DslException {
//        List<CadreAllocation> cadreGroupList = new ArrayList();
//        Database db = new Database();
//        ResultSet rs = db.executeQuery(getCadreAllocation);
//        log.info("Fetching cadre groups");
//        try {
//            while (rs.next()) {
//                CadreAllocation cadreAllocationList = new CadreAllocation();
//                cadreAllocationList.setCadreid(rs.getString("cadreid"));
//                cadreAllocationList.setCadreNumber(rs.getString("value"));
//                cadreAllocationList.setMflcode(rs.getString("mflcode"));
//                cadreAllocationList.setPeriod(rs.getString("periodid"));
//
//                cadreGroupList.add(cadreAllocationList);
//            }
//        } catch (SQLException ex) {
//            log.error(ex);
//        } finally {
//            db.CloseConnection();
//        }
//        return cadreGroupList;
//    }
//    public List<CadreGroup> getAllCadresGroup() throws DslException {
//        List<CadreGroup> cadreGroupList = new ArrayList();
//        Element ele = cache.get(CacheKeys.cadreGroups);
//        if (ele == null) {
//            long startTime = System.nanoTime();
//            Database db = new Database();
//            ResultSet rs = db.executeQuery(getALlCadreGroup);
//            log.info("Fetching cadre groups");
//            try {
//                while (rs.next()) {
//                    CadreGroup cadreGroup = new CadreGroup();
//                    cadreGroup.setId(rs.getString("cadreid"));
//                    cadreGroup.setName(rs.getString("cadrename"));
//                    cadreGroupList.add(cadreGroup);
//                }
//                cache.put(new Element(CacheKeys.cadreGroups, cadreGroupList));
//            } catch (SQLException ex) {
//                log.error(ex);
//            } finally {
//                db.CloseConnection();
//            }
//        } else {
//            long startTime = System.nanoTime();
//            cadreGroupList = (List<CadreGroup>) ele.getObjectValue();
//            long endTime = System.nanoTime();
//            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
//        }
//        return cadreGroupList;
//    }
    public EntityCollection getAllCadres() {

//        List<Entity>
        EntityCollection retEntitySet = new EntityCollection();
        Element ele = cache.get(CacheKeys.cadres);
        if (ele == null) {
            Database db = new Database();
            ResultSet rs = db.executeQuery(getALlCadre);
            log.info("Fetching cadres");
            try {
                while (rs.next()) {
                    Entity entity = new Entity();
                    entity.addProperty(new Property(null, "ID", ValueType.PRIMITIVE, rs.getString("id")));
                    entity.addProperty(new Property(null, "Name", ValueType.PRIMITIVE, rs.getString("cadrename")));
//                    cadre.setCadreGroupId(rs.getString("cadre_group_id"));
                    retEntitySet.getEntities().add(entity);
                }
                cache.put(new Element(CacheKeys.cadres, retEntitySet));
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                db.CloseConnection();
            }
        } else {
            long startTime = System.nanoTime();
            retEntitySet = (EntityCollection) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return retEntitySet;
    }
}
