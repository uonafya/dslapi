package com.healthit.dslservice.service.query;

import com.healthit.dslservice.dao.FacilityDao;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONObject;
import org.apache.commons.lang3.StringUtils;

/**
 *
 * @author duncan
 */
public class QueryParameterPopulator {

    final static Logger log = Logger.getLogger(QueryParameterPopulator.class.getCanonicalName());

    /**
     * 
     * @param finalQuery
     * @param jsoObj json object with locality filter values
     * @param parameterPlaceholder
     * @return
     */
    public static String populateParametersWithRequestFilterValues(String finalQuery, JSONObject jsoObj, Map<String, String> parameterPlaceholder) {
        log.debug("locality paramerter populator");
        JSONObject Obj = jsoObj.getJSONObject("filter");
        Iterator parameterPlaceholderKeys = parameterPlaceholder.keySet().iterator();
        log.debug("palceholder keys " + parameterPlaceholder.toString());
        while (parameterPlaceholderKeys.hasNext()) {
            String placeholderKey = (String) parameterPlaceholderKeys.next();
            log.debug("The placeholder key " + placeholderKey);
            JSONArray itemIdsToReplace = Obj.getJSONArray(placeholderKey);
            String placeHolder = parameterPlaceholder.get(placeholderKey);
            log.debug("The placeholder " + placeholderKey);
            if (placeholderKey.equals("county") || placeholderKey.equals("cadre") || placeholderKey.equals("indicator") ) {
                List intList = Arrays.asList(itemIdsToReplace.toList());
                finalQuery = populateStringParameterValues(finalQuery, itemIdsToReplace, placeHolder);
                log.debug("String with values for replacement " + intList.toString());
            } else {
                List intList = Arrays.asList(itemIdsToReplace.toList());
                log.debug("String with values for replacement " + intList.toString());
                finalQuery = finalQuery.replaceAll(placeHolder, intList.toString().replaceAll("\\[", "").replaceAll("\\]", ""));
            }
        }
        log.debug("Query from locality populator " + finalQuery);
        return finalQuery;

    }
    /**
     * Populates placeholders with a list of strings if the column type in database is a string value
     * @param finalQuery
     * @param itemIdsToReplace
     * @param placeHolder
     * @return 
     */
    public static String populateStringParameterValues(String finalQuery, JSONArray itemIdsToReplace, String placeHolder) {
        StringBuilder itemList = new StringBuilder();
        itemList.append('\'');
        boolean isFirstValue = true;
        for (int x = 0; x <= itemIdsToReplace.length() - 1; x++) {
            if (isFirstValue) {
                itemList.append(itemIdsToReplace.getString(x) + '\'');
                isFirstValue = false;
            } else {
                itemList.append(",'"+itemIdsToReplace.getString(x) + '\'');
            }
        }
        log.debug("the built list " + itemList.toString());
        finalQuery = finalQuery.replaceAll(placeHolder, itemList.toString());
        return finalQuery;
    }

    public static String populateCountyIds(String finalQuery) {
        return finalQuery;
    }

}
