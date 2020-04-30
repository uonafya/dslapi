/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.dao;

import com.healthit.dslservice.DslException;
import static com.healthit.dslservice.dao.IhrisDao.log;
import com.healthit.dslservice.dto.adminstrationlevel.Constituency;
import com.healthit.dslservice.dto.adminstrationlevel.County;
import com.healthit.dslservice.dto.adminstrationlevel.SubCounty;
import com.healthit.dslservice.dto.adminstrationlevel.Ward;
import com.healthit.dslservice.dto.ihris.CadreGroup;
import com.healthit.dslservice.dto.kemsa.Commodity;
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
public class LocationDao {

    Cache cache = DslCache.getCache();

    final static Logger log = Logger.getLogger(FacilityDao.class);
    private String getALlWards = "Select dhis_organisation_unit_id as ward_id, \n" +
"CONCAT(dhis_organisation_unit_name,' (',(select shortname from dim_dhis_organisationunit where\n" +
"organisationunitid=comm_org.parentid),')') as name,parentid,hierarchylevel from common_organisation_unit comm_org where level='ward' order by name desc";

    private String getAllConstituencies = "Select dhis_organisation_unit_id as subcounty_id,dhis_organisation_unit_name as name,hierarchylevel,parentid from common_organisation_unit where level='subcounty' order by name desc";
    private String getAllCounties = "Select dhis_organisation_unit_id as county_id,'18' as parentid,dhis_organisation_unit_name as name,hierarchylevel from common_organisation_unit where level='county' order by name desc";

    public List<Ward> getALlWards() throws DslException {

        List<Ward> wardList = new ArrayList();
        Element ele = cache.get(CacheKeys.wards);
        if (ele == null) {
            Database db = new Database();
            ResultSet rs = db.executeQuery(getALlWards);
            log.info("Fetching getWards");
            try {
                while (rs.next()) {
                    Ward ward = new Ward();
                    ward.setId(rs.getString("ward_id"));
                    ward.setName(rs.getString("name"));
                    ward.setParentid(rs.getString("parentid"));
                    ward.setLevel(rs.getInt("hierarchylevel"));
                    wardList.add(ward);
                }
                cache.put(new Element(CacheKeys.wards, wardList));
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                db.CloseConnection();
            }
        } else {
            long startTime = System.nanoTime();
            wardList = (List<Ward>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return wardList;
    }

    public List<County> getCounties() throws DslException {
        List<County> countyList = new ArrayList();
        Element ele = cache.get(CacheKeys.counties);
        if (ele == null) {
            Database db = new Database();
            ResultSet rs = db.executeQuery(getAllCounties);
            log.info("Fetching Counties");
            try {
                while (rs.next()) {
                    County county = new County();
                    county.setId(rs.getString("county_id"));
                    county.setName(rs.getString("name"));
                    county.setParentid(rs.getString("parentid"));
                    county.setLevel(rs.getInt("hierarchylevel"));
                    countyList.add(county);
                }
                cache.put(new Element(CacheKeys.counties, countyList));
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                db.CloseConnection();
            }
        } else {
            long startTime = System.nanoTime();
            countyList = (List<County>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return countyList;
    }

    public List<Constituency> getSubCounties() throws DslException {
        List<Constituency> constituencyList = new ArrayList();
        Element ele = cache.get(CacheKeys.constituencies);
        if (ele == null) {
            Database db = new Database();
            ResultSet rs = db.executeQuery(getAllConstituencies);
            log.info("Fetching Constituencies");
            try {
                while (rs.next()) {
                    Constituency constituency = new Constituency();
                    constituency.setId(rs.getString("subcounty_id"));
                    constituency.setName(rs.getString("name"));
                    constituency.setParentid(rs.getString("parentid"));
                    constituency.setLevel(rs.getInt("hierarchylevel"));
                    constituencyList.add(constituency);
                }
                cache.put(new Element(CacheKeys.constituencies, constituencyList));
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                db.CloseConnection();
            }
        } else {
            long startTime = System.nanoTime();
            constituencyList = (List<Constituency>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return constituencyList;
    }

}
