package com.healthit.dslweb.service;

import com.healthit.dslservice.util.DslCache;
import com.healthit.dslservice.util.PropertiesLoader;
import com.healthit.dslservice.util.RequestEntity;
import com.healthit.dslweb.service.QueryInterpreter;
import java.util.List;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONObject;
import static com.healthit.dslservice.util.strings.DataTypeConverter.getJSONFromObject;

import java.util.Iterator;
import javax.servlet.http.HttpSession;
import net.sf.ehcache.Cache;
import net.sf.ehcache.Element;

/**
 *
 * @author duncan
 */
public class QueryProcessor {


    public String prepareResponse(Map pBody) {

        QueryInterpreter queryInterpreterObj = new QueryInterpreter();
        JSONObject jsonObj = new JSONObject(pBody);
        JSONArray array = jsonObj.getJSONArray("query");

        //check if data in cache
        Cache cache = DslCache.getCache();
        List<RequestEntity> rqtEntities = PropertiesLoader._sort(array);
        Iterator i = rqtEntities.iterator();
        // cache key naming pattern = indicator-name(subject),orgnaisation-unitname, period
        String orgUnit = "";
        String period = "";
        String indicator = "";

        while (i.hasNext()) {
            RequestEntity rstEnty = (RequestEntity) i.next();
            try {
                orgUnit = (String) rstEnty.getOrgUnitID();
            } catch (Exception e) {

                JSONObject orgUnitIdOb = (JSONObject) rstEnty.getOrgUnitID();
                orgUnit = (String) orgUnitIdOb.getJSONArray(rstEnty.getOrgUnitType()).get(0);
                System.out.println(orgUnit);

            }

//            rstEnty.getOrgUnitType();
//            rstEnty.getPeriodType();
            String subJect = rstEnty.getSubject();
            if (!(subJect.contains("locality")) && !(subJect.contains("date"))) {
                indicator = subJect;
            }
            period = getPeriod(rstEnty);
        }
        String cachecKey = indicator + orgUnit + period;
        cachecKey = cachecKey.trim().replaceAll("\\s+", "");
        System.out.println("the cache key is " + cachecKey);

        Element ele = cache.get(cachecKey);
        String queryResults;
        if (ele == null) {
            Map<String, List<Object>> rsults = queryInterpreterObj.interpretQuery(array);
            queryResults = getJSONFromObject(rsults);
            cache.put(new Element(cachecKey, queryResults));
        } else {
            System.out.println("the cache has value");
            queryResults = (String) ele.getObjectValue();
        }
        return queryResults;
    }

    private String getPeriod(RequestEntity requestString) {
        try {
            JSONObject period = (JSONObject) requestString.getPeriod();
            String periodTypeRaw = requestString.getPeriodType();
            if (periodTypeRaw.equals("yearly")) {
                String startYear = (String) period.getJSONArray("start_year").get(0);
                String endYear = (String) period.getJSONArray("end_year").get(0);
                return startYear + endYear;
            }
            if (periodTypeRaw.equals("monthly")) {
                return (String) period.getJSONArray("start_year").get(0);
//                startMonth = (String) period.getJSONArray("start_month").get(0);
//                endMonth = (String) period.getJSONArray("end_month").get(0);
            }
        } catch (Exception ex) {
            System.err.println(ex);
        }
        return "";
    }

}
