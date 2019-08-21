/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.dao;

import com.healthit.dslservice.DslException;
import com.healthit.dslservice.dto.ihris.Cadre;
import com.healthit.dslservice.dto.ihris.CadreAllocation;
import com.healthit.dslservice.dto.ihris.CadreGroup;
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

    final static Logger log = Logger.getLogger(IhrisDao.class);
    Cache cache = DslCache.getCache();

    private String aLlCadreGroup = "Select cadreid,cadrename from dim_ihris_cadre";
    private String aLlCadre = "Select dataelementid as id,dataelementname as cadrename, cadreid as cadre_group_id from dim_ihris_dataelement";
    private String cadresByGroup = "Select dataelementid as id,dataelementname as cadrename, cadreid as cadre_group_id from dim_ihris_dataelement where cadreid=?";

    private String nationalCadreGroupCount = "select count(*) as cadre_count,cadre_group.cadreid as id,cadre_group.cadrename as cadre from fact_ihris ihris \n"
            + "inner join dim_ihris_dataelement cadree on cast(cadree.dataelementid as varchar) = cast(ihris.job_category_id as varchar) \n"
            + "inner join dim_ihris_cadre cadre_group on cadre_group.cadreid=cadree.cadreid  @ou_join@\n"
            + "where @pe@ @ou@ @cadreGroup@ \n"
            + "group by cadre,cadre_group.cadreid order by cadre desc";

    private String nationalCadreCount = "select count(*) as cadre_count,cadree.dataelementid as id,cadree.dataelementname as cadre from fact_ihris ihris \n"
            + "inner join dim_ihris_dataelement cadree on cast(cadree.dataelementid as varchar) = cast(ihris.job_category_id as varchar) @ou_join@\n"
            + "where @pe@ @ou@ @cadre@ \n"
            + "group by cadre,cadree.dataelementid order by cadre desc";

    private boolean appendAnd = false;

    /**
     *
     * @param pe period from http request
     * @return qeuery string appended with period patameter
     * @throws DslException
     */
    private String insertPeriodPart(String pe, String sqlString) throws DslException {
        String periodString = "";
        RequestParameters.isValidPeriod(pe);
        if (pe.length() == 4) {
            String replacement = " ihris.hire_date<='@end_year@-12-31' ".replace("@end_year@", pe);
            periodString = replacement;
        } else {
            String paramYear = pe.substring(0, 4);
            String paramMonth = pe.substring(4, 6);
            String replacement = " ihris.hire_date<='@end_year@-@month@-31' ".replace("@end_year@", paramYear).replace("@month@", paramMonth);
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
    private String insertOrgUntiPart(String ou, String level, String sqlString) throws DslException {
        String join = "";
        log.info("org unti level: " + level);
        if ("facility".equals(level.trim())) {
            join = " inner join common_organisation_unit com_org on cast(com_org.mfl_code as varchar) = cast(ihris.mfl_code as varchar) ";
        } else if ("ward".equals(level.trim())) {
            join = "inner join facilities_facility ff on cast(ff.code as varchar) =cast(ihris.mfl_code as varchar)"
                    + "inner join common_organisation_unit com_org on com_org.mfl_code=ff.ward_sk";
        } else if ("subcounty".equals(level.trim())) {
            join = "inner join facilities_facility ff on cast(ff.code as varchar) =cast(ihris.mfl_code as varchar) "
                    + "inner join common_ward com_ward on cast(com_ward.ward_sk as varchar) = cast(ff.ward_sk as varchar)"
                    + "inner join common_organisation_unit com_org on cast(com_org.mfl_code as varchar) = cast(com_ward.constituency_sk as varchar)";
        } else if ("county".equals(level.trim())) {
            join = "inner join facilities_facility ff on cast(ff.code as varchar) =cast(ihris.mfl_code as varchar) "
                    + "inner join common_ward com_ward on cast(com_ward.ward_sk as varchar)= cast(ff.ward_sk as varchar)"
                    + "inner join common_constituency com_consti on cast(com_ward.constituency_sk as varchar) =  cast(com_consti.constituency_sk as varchar)"
                    + "inner join common_county com_county on cast(com_county.id as varchar) = cast(com_consti.county_id as varchar)"
                    + "inner join common_organisation_unit com_org on cast(com_org.mfl_code as varchar) = cast(com_county.code as varchar)";
        }
        log.debug("The oug unit join query: " + join);
        sqlString = sqlString.replace("@ou_join@", join);
        log.debug("the query after replace: " + join);
        // @ou_join@
        String replacement;
        if (appendAnd) {
            replacement = " and com_org.dhis_organisation_unit_id= " + ou;
        } else {
            replacement = " com_org.dhis_organisation_unit_id=" + ou;
            appendAnd = true;
        }
        sqlString = sqlString.replace("@ou@", replacement);

        return sqlString;
    }

    /**
     *
     * @param ou organisation unit id from http request
     * @return qeuery string appended with org unit patameter
     * @throws DslException
     */
    private String insertCadreGroupPart(String cadreGroup) throws DslException {
        String replacement;
        if (appendAnd) {
            replacement = " and cadre_group.cadreid = " + cadreGroup;
        } else {
            replacement = " cadre_group.cadreid =" + cadreGroup;
            appendAnd = true;
        }
        nationalCadreGroupCount = nationalCadreGroupCount.replace("@cadreGroup@", replacement);

        return nationalCadreGroupCount;
    }

    /**
     *
     * @param ou organisation unit id from http request
     * @return qeuery string appended with org unit patameter
     * @throws DslException
     */
    private String insertCadrePart(String cadre) throws DslException {
        String replacement;
        if (appendAnd) {
            replacement = " and cadree.dataelementid = " + cadre;
        } else {
            replacement = " cadree.dataelementid =" + cadre;
            appendAnd = true;
        }
        nationalCadreCount = nationalCadreCount.replace("@cadre@", replacement);

        return nationalCadreCount;
    }

    /**
     * Allocation of cadres by cadre groups
     *
     * @param pe semi colon separated period
     * @param ou semi colon separated org unit ids
     * @param cadre semi colon separated cadre ids
     * @param cadreGroup semi colon separated cadreGroup ids
     * @return cadre allocation objects
     * @throws DslException
     */
    public List<CadreAllocation> getCadreGroupAllocation(String pe, String ou, String cadreGroup) throws DslException {
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        if (pe != null) {
            nationalCadreGroupCount = insertPeriodPart(pe, nationalCadreGroupCount);
            appendAnd = true;
        } else {
            nationalCadreGroupCount = nationalCadreGroupCount.replace("@pe@", "");
        }

        if (ou != null) {
            String level = RequestParameters.getOruntiLevel(ou);
            nationalCadreGroupCount=insertOrgUntiPart(ou, level,nationalCadreGroupCount);
        } else {
            nationalCadreGroupCount = nationalCadreGroupCount.replace("@ou@", "");
            nationalCadreGroupCount = nationalCadreGroupCount.replace("@ou_join@", "");
        }

        if (cadreGroup != null) {
            nationalCadreGroupCount = insertCadreGroupPart(cadreGroup);
        } else {
            nationalCadreGroupCount = nationalCadreGroupCount.replace("@cadreGroup@", "");
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
        if (pe != null) {
            nationalCadreCount = insertPeriodPart(pe,nationalCadreCount);
            appendAnd = true;
        } else {
            nationalCadreCount = nationalCadreCount.replace("@pe@", "");
        }

        if (ou != null) {
            String level = RequestParameters.getOruntiLevel(ou);
            nationalCadreCount=insertOrgUntiPart(ou, level,nationalCadreCount);
        } else {
            nationalCadreCount = nationalCadreCount.replace("@ou@", "");
            nationalCadreCount = nationalCadreCount.replace("@ou_join@", "");
        }

        if (cadre != null) {
            nationalCadreCount = insertCadrePart(cadre);
        } else {
            nationalCadreCount = nationalCadreCount.replace("@cadre@", "");
        }

        List<CadreAllocation> cadreAllocationList = new ArrayList();
        Database db = new Database();

        ResultSet rs = db.executeQuery(nationalCadreCount);
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
