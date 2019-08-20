/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.dao;

import com.healthit.dslservice.DslException;
import static com.healthit.dslservice.dao.FacilityDao.log;
import com.healthit.dslservice.dto.ihris.Cadre;
import com.healthit.dslservice.dto.KephLevel;
import com.healthit.dslservice.dto.adminstrationlevel.Facility;
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
import java.util.List;
import java.util.logging.Level;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.log4j.Logger;

/**
 *
 * @author duncan
 */
public class IhrisDao {

    final static Logger log = Logger.getLogger(FacilityDao.class);
    Cache cache = DslCache.getCache();

    private String aLlCadreGroup = "Select cadreid,cadrename from dim_ihris_cadre";
    private String aLlCadre = "Select dataelementid as id,dataelementname as cadrename, cadreid as cadre_group_id from dim_ihris_dataelement";
    private String cadresByGroup = "Select dataelementid as id,dataelementname as cadrename, cadreid as cadre_group_id from dim_ihris_dataelement where cadreid=?";

    private String nationalCadreGroupCount = "select count(*) as cadre_count,cadre_group.cadreid as id,cadre_group.cadrename as cadre from fact_ihris ihris \n"
            + "inner join dim_ihris_dataelement cadree on cast(cadree.dataelementid as varchar) = cast(ihris.job_category_id as varchar) \n"
            + "inner join dim_ihris_cadre cadre_group on cadre_group.cadreid=cadree.cadreid  \n"
            + "where ihris.hire_date<='@end_year@-12-31' \n"
            + "group by cadre,cadre_group.cadreid order by cadre desc";

    /**
     *
     * @param pe semi colon separated period
     * @param ou semi colon separated org unit ids
     * @param cadre semi colon separated cadre ids
     * @param cadreGroup semi colon separated cadreGroup ids
     * @return cadre allocation objects
     * @throws DslException
     */
    public List<CadreAllocation> getCadreGroupAllocation(String pe, String ou, String cadreGroup) throws DslException {
        int year = Calendar.getInstance().get(Calendar.YEAR);
        if (pe != null) {

            RequestParameters.isValidPeriodParamer(pe);
            if (pe.length() == 4) {
                year = Integer.parseInt(pe);
            }
            nationalCadreGroupCount=nationalCadreGroupCount.replace("@end_year@", Integer.toString(year));
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
                cadreAllocationList.add(cadreAllocation);
            }
        } catch (SQLException ex) {
            log.error(ex);
        } finally {
            db.CloseConnection();
        }
        return cadreAllocationList;
    }

    /**
     *
     * @param pe semi colon separated period
     * @param ou semi colon separated org unit ids
     * @param cadre semi colon separated cadre ids
     * @param cadreGroup semi colon separated cadreGroup ids
     * @return cadre allocation objects
     * @throws DslException
     */
    public List<CadreAllocation> getCadreAllocation(String pe, String ou, String cadre, String cadreGroup) throws DslException {
        List<CadreAllocation> cadreAllocationList = new ArrayList();
        Database db = new Database();
        ResultSet rs = db.executeQuery(nationalCadreGroupCount.replace("@end_year@", "2019"));
        log.info("Fetching cadre groups");
        try {
            while (rs.next()) {
                CadreAllocation cadreAllocation = new CadreAllocation();
                cadreAllocation.setCadre(rs.getString("cadre"));
                cadreAllocation.setCadreCount(rs.getString("cadre_count"));
                cadreAllocation.setId(rs.getString("id"));
                cadreAllocationList.add(cadreAllocation);
            }
        } catch (SQLException ex) {
            log.error(ex);
        } finally {
            db.CloseConnection();
        }
        return cadreAllocationList;
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
