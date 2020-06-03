package com.healthit.dslservice.dao;

import com.healthit.dslservice.DslException;
import static com.healthit.dslservice.dao.DhisDao.log;
import com.healthit.dslservice.dto.PandemicIndicator;
import com.healthit.dslservice.dto.dhis.IndicatorGoup;
import com.healthit.dslservice.util.CacheKeys;
import com.healthit.dslservice.util.Database;
import com.healthit.dslservice.util.DslCache;
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
 * @author duncanndiithi
 */
public class PandemicDao {

    final static Logger log = Logger.getLogger(PandemicDao.class);
    Cache cache = DslCache.getCache();

    private String selectPandemicIndicators = "Select pandemic_indicator_id as id, name FROM dim_pandemic_indicator";

    /**
     * Gets a list of pandemics
     * @return List<Map> A list of pandemics and their associated indciators
     * @throws DslException 
     */
    public List<Map> getPandemics() throws DslException {
        List<Map> pandemicList = new ArrayList();
        List<PandemicIndicator> pandemicIndicatorList = new ArrayList();
        Map<String, Object> pandemics = new HashMap();
        pandemics.put("id", "covid19");
        pandemics.put("name", "covid 19");
        pandemics.put("initial_report_date", "2020-03-17");

        Element ele = cache.get(CacheKeys.indicatorGroup);

        if (ele == null) {
            Database db = new Database();
            ResultSet rs = db.executeQuery(selectPandemicIndicators);
            log.info("Fetching indicator groups");
            try {
                while (rs.next()) {
                    PandemicIndicator pandemicIndicator = new PandemicIndicator();
                    pandemicIndicator.setId(rs.getInt("id"));
                    pandemicIndicator.setName(rs.getString("name"));
                    pandemicIndicatorList.add(pandemicIndicator);
                }
                pandemics.put("indicators", pandemicIndicatorList);
                pandemicList.add(pandemics);
                cache.put(new Element(CacheKeys.pandemicsList, pandemicList));
            } catch (SQLException ex) {
                log.error(ex);
            } finally {
                db.CloseConnection();
            }
        } else {
            long startTime = System.nanoTime();
            pandemicList = (List<Map>) ele.getObjectValue();
            long endTime = System.nanoTime();
            log.info("Time taken to fetch data from cache " + (endTime - startTime) / 1000000);
        }
        return pandemicList;
    }
}
