/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.dao;

import com.healthit.dslservice.DslException;
import com.healthit.dslservice.Filter;
import static com.healthit.dslservice.dao.IhrisDao.log;
import com.healthit.dslservice.dto.ihris.CadreGroup;
import com.healthit.dslservice.dto.kemsa.Commodity;
import com.healthit.dslservice.util.CacheKeys;
import com.healthit.dslservice.util.Database;
import com.healthit.dslservice.util.DslCache;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;
import org.apache.log4j.Logger;

/**
 *
 * @author duncan
 */
public class KemsaDao {

    final static Logger log = Logger.getLogger(KemsaDao.class.getCanonicalName());
    Cache cache = DslCache.getCache();
    private StringBuilder getALlCommoditiesBuilder = new StringBuilder("Select c_order_id as id,mfl as mflcode,product as name,"
            + "qtyordered as orderQuantity,order_year as orderYear,order_month as orderMonth from fact_kemsa_order_dsl where mfl is not null ");

    private String getCommodityNames = "select distinct(product) as commodity_name from fact_kemsa_order_dsl order by commodity_name";

    public List<Commodity> getAllCommodities(
            String startDate,
            String endDate,
            List<String> mflCode
    ) throws DslException {

        if (mflCode != null) {
            if (!mflCode.isEmpty()) {
                Iterator i = mflCode.iterator();
                String append = "";
                int count = 0;
                while (i.hasNext()) {
                    if (count == 0) {
                        append = " and code=" + (String) i.next() + " ";
                        count = count + 1;
                    } else {
                        append = " or code=" + (String) i.next() + " ";
                    }
                }
                getALlCommoditiesBuilder.append(append);
            }
        }

        Filter filter = new Filter();
        String orderBy = " order by orderYear,orderMonth ";
        getALlCommoditiesBuilder.append(orderBy + " OFFSET " + filter.getOffset() + " " + " LIMIT " + filter.getLimit());

        List<Commodity> cadreGroupList = new ArrayList();
        Database db = new Database();
        ResultSet rs = db.executeQuery(getALlCommoditiesBuilder.toString());
        log.info("Fetching Commodities");
        try {
            while (rs.next()) {
                Commodity commodity = new Commodity();
                commodity.setId(rs.getString("id"));
                commodity.setMflcode(rs.getString("mflcode"));
                commodity.setName(rs.getString("name"));
                commodity.setOrderMonth(rs.getString("orderMonth"));
                commodity.setOrderQuantity(rs.getString("orderQuantity"));
                commodity.setOrderYear(rs.getString("orderYear"));

                cadreGroupList.add(commodity);
            }
        } catch (SQLException ex) {
            log.error(ex);
        } finally {
            db.CloseConnection();
        }
        return cadreGroupList;
    }

    public List<String> getCommodityNames() throws DslException {
        List<String> commodityNames = new ArrayList();
        Element ele = cache.get(CacheKeys.commodityNames);
        if (ele == null) {
            long startTime = System.nanoTime();
            Database db = new Database();
            ResultSet rs = db.executeQuery(getCommodityNames);
            log.info("Fetching commodity names");
            try {
                while (rs.next()) {

                    commodityNames.add(rs.getString("commodity_name"));
                }
                cache.put(new Element(CacheKeys.commodityNames, commodityNames));
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                db.CloseConnection();
            }
        } else {
            long startTime = System.nanoTime();
            commodityNames = (List<String>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return commodityNames;
    }

}
