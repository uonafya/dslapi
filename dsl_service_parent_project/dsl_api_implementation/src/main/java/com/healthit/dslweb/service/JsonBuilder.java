/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslweb.service;

import com.healthit.DslException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author duncan
 * @deprecated
 */
public class JsonBuilder {

    static List queryParametersList = new ArrayList();

//set indicator to fetch
    public static List setIndicatorValues(String indicatorType, String indicator) {
        Map<String, Object> indicatorValuesToQuery = new HashMap();
        String[] indic = new String[1];
        indic[0] = indicator;
        indicatorValuesToQuery.put("what", indicatorType);
        Map<String, String[]> filter = new HashMap();
        filter.put("indicator", indic);
        indicatorValuesToQuery.put("filter", filter);
        queryParametersList.add(indicatorValuesToQuery);
        return queryParametersList;
    }

    public static List setFacilityValues(String indicatorType, String facility) {
        Map<String, Object> indicatorValuesToQuery = new HashMap();
        indicatorValuesToQuery.put("what", indicatorType);
        String[] facil = new String[1];
        facil[0] = facility;
        Map<String, String[]> facilityMap = new HashMap();
        facilityMap.put("facility", facil);
        indicatorValuesToQuery.put("filter", facilityMap);
        queryParametersList.add(indicatorValuesToQuery);
        return queryParametersList;
    }

    public static List setIhrisValues(String indicatorType, String cadre) {
        Map<String, Object> humanResourceValuesToQuery = new HashMap();
        humanResourceValuesToQuery.put("what", indicatorType);
        Map<String, String[]> cadreMap = new HashMap();
        String[] cadr = new String[1];
        cadr[0] = cadre;
        cadreMap.put("cadre", cadr);
        humanResourceValuesToQuery.put("filter", cadreMap);
        queryParametersList.add(humanResourceValuesToQuery);
        return queryParametersList;
    }

    public static List setLocality(String org_level, String filter) {
        Map<String, Object> localityValuesToQuery = new HashMap();
        localityValuesToQuery.put("what", "locality:" + org_level);
        localityValuesToQuery.put("filter", filter);
        queryParametersList.add(localityValuesToQuery);
        return queryParametersList;
    }

    public static List setKemsaValues(String commodityType) {
        Map<String, Object> commodityValuesToQuery = new HashMap();
        commodityValuesToQuery.put("what", commodityType);
        queryParametersList.add(commodityValuesToQuery);
        return queryParametersList;
    }

    public static void indicatorHandler(String dataName, String filter) {
        JsonBuilder jsonBuilder;
        if (dataName.indexOf("facility") != -1) {
            setFacilityValues(dataName, filter);
        } else if (dataName.indexOf("indicator") != -1) {
            setIndicatorValues(dataName, filter);
        } else if (dataName.indexOf("human_resource") != -1) {
            setIhrisValues(dataName, filter);
        }
    }

//prepare final query parameters
    public static Map prepareQueryPropertiesToSubmit() {
        Map<String, List> queryToSubmit = new HashMap();
        queryToSubmit.put("query", queryParametersList);
//    var queryPropertiesToSubmit = queryToSubmit;
        return queryToSubmit;
    }

//set period option
    public static List setPeriodValues(String periodType, String startDate, String endDate) {
        Map<String, Object> dateValuesToQuery = new HashMap();
        dateValuesToQuery.put("filter", new HashMap<String, Object>());
        Map filtr = (Map) dateValuesToQuery.get("filter");
        String[] str = new String[1];
        if (periodType == "monthly") {
            dateValuesToQuery.put("what", "date:yearly:monthly");
            str[0] = "1";
            filtr.put("start_month", str);
            str = new String[1];
            str[0] = "12";
            filtr.put("end_month", str);
        } else if (periodType == "yearly") {
            dateValuesToQuery.put("what", "date:yearly");
        }
        str = new String[1];
        str[0] = startDate;
        filtr.put("start_year", str);
        str = new String[1];
        str[0] = endDate;
        filtr.put("end_year", str);
        queryParametersList.add(dateValuesToQuery);
        return queryParametersList;
    }

// set the required data here
    public void init() throws DslException {
        JsonBuilder jsonBuilder = new JsonBuilder();
        String indicatorName = "HIV+ test rate - PMTCT -ANC";
        String indicatorType = "indicator:average:with_filter";
        JsonBuilder.setPeriodValues("monthly", "2017", "2017");
    setIndicatorValues(indicatorType, indicatorName);
        Map queryPropertiesToSubmit = prepareQueryPropertiesToSubmit();
        QueryProcessor qProcessor=new QueryProcessor();
        String queryResults=qProcessor.prepareResponse(queryPropertiesToSubmit);
        
    }

}
