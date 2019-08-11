package com.healthit.dslservice.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author duncan
 */
public class RequestBodyDissolver {

    final static Logger log = Logger.getLogger(RequestBodyDissolver.class.getCanonicalName());

    /**
     * Gets the values from the jason array post data
     *
     * @param requestBody
     * @return
     */
    private Map<String, Object> getRequestsPart(JSONArray requestBody) {
        Map<String, Object> requestBodyValue = new HashMap();
        for (Object o : requestBody) {

            JSONObject jsoObj = (JSONObject) o;
            StringBuilder metadataSourceName = new StringBuilder();
            String[] queryNamesFromUI = jsoObj.getString("what").split(":");
            metadataSourceName.append(queryNamesFromUI[0]);

            log.info("we got data " + queryNamesFromUI[0]);

            if (queryNamesFromUI[0].equals("date")) {
                JSONObject filters = jsoObj.getJSONObject("filter");
                requestBodyValue.put("period", filters);
                int len=queryNamesFromUI.length;
                requestBodyValue.put("periodType", queryNamesFromUI[len-1]);
            }

            if (queryNamesFromUI[0].equals("locality")) {
                JSONObject filters = jsoObj.getJSONObject("filter");
                requestBodyValue.put("orgUnitID", filters);
                requestBodyValue.put("orgUnitType", queryNamesFromUI[1]);
            }
        }
        return requestBodyValue;
    }

    /**
     * Gets the main subject(indicator) from the jason array post data
     *
     * @param requestBody
     * @return
     */
    private List<String> getRequestSubject(JSONArray requestBody) {
        System.out.println("===============nu leave this");
        System.out.println(requestBody);
        System.out.println("===============nu leave this");
        List<String> subjects = new ArrayList();
        for (Object o : requestBody) {

            JSONObject jsoObj = (JSONObject) o;

            StringBuilder metadataSourceName = new StringBuilder();

            String[] queryNamesFromUI = jsoObj.getString("what").split(":");

            log.info("the object " + jsoObj.toString());
            JSONObject filters = null;
            JSONArray keys = null;
            try {
                filters = jsoObj.getJSONObject("filter");
                keys = filters.names();
            } catch (JSONException ex) {
                log.error(ex);
            }

            if (keys != null) {
                for (Object obj : keys) {
                    String key = (String) obj;
                    JSONArray filterValues = filters.getJSONArray(key);
                    for (Object filterValueObj : filterValues) {
                        metadataSourceName.append(queryNamesFromUI[0]);
                        log.info("Filter value " + filterValueObj);
                        String filterValue;
                        try {
                            filterValue = (String) filterValueObj;
                        } catch (ClassCastException ex) {
                            filterValue = String.valueOf(filterValueObj);
                        }

                        metadataSourceName.append("-").append(key).append("-").append(filterValue);

                        subjects.add(metadataSourceName.toString());
                        metadataSourceName = new StringBuilder();
                    }
                }
            }

        }
        return subjects;
    }

    public List<RequestEntity> dissolve(JSONArray requestBody) {
        List<RequestEntity> requestEntities = new ArrayList();
        Map<String, Object> bodValues = getRequestsPart(requestBody);
        List<String> subjects = getRequestSubject(requestBody);
        Iterator i = subjects.iterator();
        while (i.hasNext()) {
            RequestEntity rqstEntity = new RequestEntity();
            rqstEntity.setSubject((String) i.next());
            rqstEntity.setPeriodType((String) bodValues.get("periodType"));
            rqstEntity.setPeriod(bodValues.get("period"));
            rqstEntity.setOrgUnitID(bodValues.get("orgUnitID"));
            rqstEntity.setOrgUnitType((String) bodValues.get("orgUnitType"));
            requestEntities.add(rqstEntity);
        }

        for (Map.Entry<String, Object> entry : bodValues.entrySet()) {
            System.out.println("Key : " + entry.getKey() + " value : " + entry.getValue());
        }
        return requestEntities;
    }
}
