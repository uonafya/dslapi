package com.healthit.dslweb.service;

import com.healthit.dslservice.DslException;
import com.healthit.dslservice.service.query.QueryParameterPopulator;
import com.healthit.dslservice.util.Database;
import com.healthit.dslservice.util.PropertiesLoader;
import com.healthit.dslservice.util.strings.RandomStringGenerator;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import org.apache.log4j.Logger;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 *
 * @author duncan
 */
public class QueryInterpreter {

    final static Logger log = Logger.getLogger(QueryInterpreter.class.getCanonicalName());
    static Properties queriesFile;
    static Properties queriesMatcherFile;
    static Set<String> tableAlias = new HashSet();

    /**
     *
     * @param array array containing all query parameters to interpret to sql
     * string
     * @return generated sql string to run
     */
    public Map<String, List<Object>> interpretQuery(JSONArray array) {
        JSONArray _httpJsonArray = array; //original array placeholder
        array = addFilterLevelsToMainQueryAttributes(array);
        List<Map<String, Object>> queriesToRun = new ArrayList();
        JSONObject jsoObj;
        for (Object o : array) {
            jsoObj = (JSONObject) o;
            Map<String, Object> s = getQueryToRun(jsoObj.getString("what"));
            if (s != null) {
                queriesToRun.add(s);
            }
        }
        String finalQuery = getQuery(queriesToRun);
        log.info("calling query populator " + array.length());
        finalQuery = populatQueryParameters(finalQuery, _httpJsonArray);
        Map<String, List<Object>> results = runSqlQuery(finalQuery);
        return results;
    }

    /**
     * Replace http filter values to placeholders in the sql queries
     *
     * @param finalQuery
     * @param array original json array from the http request with the parameter
     * values
     * @return replace query with real filter values
     */
    private String populatQueryParameters(String finalQuery, JSONArray array) {
        for (Object o : array) {
            JSONObject jsoObj = (JSONObject) o;
            String[] queryNamesFromUI = jsoObj.getString("what").split(":");
            log.debug("check if locality object in list" + Arrays.toString(queryNamesFromUI));
            // if (Arrays.asList(queryNamesFromUI).contains("locality")) {
            log.debug("locality object is in list");
            try {
                log.debug("the objected with period and locality " + jsoObj.getJSONObject("filter").toString());
                JSONObject filters = jsoObj.getJSONObject("filter");
                Map<String, String> parameterPlaceholder = getSqlParameterPlaceHolderToReplace(filters);
                finalQuery = QueryParameterPopulator.populateParametersWithRequestFilterValues(finalQuery, jsoObj, parameterPlaceholder);
            } catch (JSONException e) {
                log.error(e);
            }

            // }
        }
        return finalQuery;
    }

    /**
     *
     * @param Obj the json object containg key and values used for filtering in
     * the final sql
     * @return the placeholder used in sql statement.
     */
    private Map<String, String> getSqlParameterPlaceHolderToReplace(JSONObject Obj) {
        log.debug("getting list of query parameters mappers");
        //get key to replace
        String parameterPlaceholder = null;
        Properties props = null;
        PropertiesLoader propLoader = new PropertiesLoader();
        props = propLoader.getPropertiesFile(props, "query_parameters_mapper.properties");
        Set<Object> queryMatcherPropsKeys = propLoader.getAllKeys(props);
        log.debug("Place holder keys " + queryMatcherPropsKeys.toString());
        Map<String, String> parameterPlaceholders = new HashMap();
        log.debug("The set " + Obj.toString());
        for (Object k : queryMatcherPropsKeys) {
            String key = (String) k;
            log.debug("query parameters key from file " + key);
            Iterator i = Obj.keys();
            while (i.hasNext()) {
                log.debug("filtering object has values ");
                String x = (String) i.next();
                log.debug("key twp " + x);
                log.debug("key twp2 " + key);
                if (key.equals(x)) {
                    parameterPlaceholder = props.getProperty(key);
                    parameterPlaceholders.put(key, parameterPlaceholder);
                }
            }
        }
        log.debug("Returned placeholder map " + parameterPlaceholders);
        return parameterPlaceholders;
    }

    /**
     * Faced(@see Faced design pattern) to filter level populator methods
     *
     * @param array
     * @return
     */
    private JSONArray addFilterLevelsToMainQueryAttributes(JSONArray array) {
        array = injectPeriodicityFilterLevel(array);
        array = injectLocalityFilterLevel(array);

        log.info("Http json array with injeced filters " + array.toString());
        return array;
    }

    /**
     * Adds the periods to the query name( the name is used for retrieving
     * actual query from file), eg ward, ward,contituency,
     * ward:contituency:county
     *
     * @param array
     * @return array
     */
    private JSONArray injectPeriodicityFilterLevel(JSONArray array) {
        JSONArray arrayReplacement = array;
        for (Object o : array) {
            JSONObject jsoObj = (JSONObject) o;

            String[] queryNamesFromUI = jsoObj.getString("what").split(":");
            log.debug("The what value " + Arrays.toString(queryNamesFromUI));
            if (Arrays.asList(queryNamesFromUI).contains("date")) {
                arrayReplacement = new JSONArray();
                for (Object obj : array) {
                    JSONObject jsoObj2 = (JSONObject) obj;
                    String[] queryFetchValues = jsoObj2.getString("what").split(":");
                    if (!Arrays.asList(queryFetchValues).contains("date") && !Arrays.asList(queryFetchValues).contains("locality")) {
                        JSONObject jsonObj = new JSONObject();
                        try {
                            try {
                                jsoObj2.getJSONObject("filter");
                            } catch (Exception e) {
                                JSONObject objt = new JSONObject();
                                jsoObj2.put("filter", objt);
                            }

                            List list = Arrays.asList(queryFetchValues);
                            if (list.contains("commodity") || list.contains("human_resource") || list.contains("indicator")) {
                                jsoObj2 = injectYearANDMonth(jsoObj2, jsoObj);
                            }
                        } catch (Exception e) {
                            log.error(e);
                        }
                        arrayReplacement.put(jsoObj2);
                        log.debug("The period injected array1 " + arrayReplacement.toString());
                    } else if (Arrays.asList(queryFetchValues).contains("locality")) {
                        arrayReplacement.put(jsoObj2);
                        log.debug("The period injected array2 " + arrayReplacement.toString());
                    } else {
                    }
                }
                log.debug("The period injected array " + arrayReplacement.toString());
                return arrayReplacement;
            }
        }
        return arrayReplacement;
    }

    /**
     *
     * @param jsoObj2
     * @param jsoObj the json object with period(date) attributes
     * @return
     * @throws ParseException
     */
    private JSONObject injectYearANDMonth(JSONObject jsoObj2, JSONObject jsoObj) throws ParseException {
        log.debug("http Dates json object " + jsoObj);
        Map map3 = new HashMap();
        Map map1 = jsoObj2.getJSONObject("filter").toMap();
        String startYear = jsoObj.getJSONObject("filter").getJSONArray("start_year").getString(0);
        String sartMonth = "0";
        String endMonth = "0";
        try {// try becuase month may be empty if using yearly filter period
            sartMonth = jsoObj.getJSONObject("filter").getJSONArray("start_month").getString(0);
            endMonth = jsoObj.getJSONObject("filter").getJSONArray("end_month").getString(0);
        } catch (Exception e) {

        }
        int month = Integer.parseInt(sartMonth);
        int year = Integer.parseInt(startYear);
        Map<String, List> periodMap = new HashMap();
        List<Integer> periodList = new ArrayList();
        periodList.add(year);
        periodMap.put("start_year", periodList);
        periodList = new ArrayList();
        periodList.add(month);
        periodMap.put("start_month", periodList);
        log.debug("Start period " + periodMap.toString());
        String endYear = jsoObj.getJSONObject("filter").getJSONArray("end_year").getString(0);

        month = Integer.parseInt(endMonth);
        year = Integer.parseInt(endYear);
        periodList = new ArrayList();
        periodList.add(year);
        periodMap.put("end_year", periodList);
        periodList = new ArrayList();
        periodList.add(month);
        periodMap.put("end_month", periodList);
        log.debug("end period " + periodMap.toString());
        map3.putAll(map1);
        map3.putAll(periodMap);
        jsoObj2.put("filter", new JSONObject(map3));
        String whatReplaced = jsoObj2.getString("what") + jsoObj.getString("what").replace("date", "");
        log.debug("Got it fast " + whatReplaced);
        jsoObj2.put("what", whatReplaced);
        return jsoObj2;
    }

    /**
     * Adds the locality(administrative) level to the query name(used for
     * retrieving actual query from file), eg ward, ward,contituency,
     * ward:contituency:county
     *
     * @param array
     * @return array
     */
    private JSONArray injectLocalityFilterLevel(JSONArray array) {
        JSONArray arrayReplace = array;
        for (Object o : array) {
            JSONObject jsoObj = (JSONObject) o;
            String[] queryNamesFromUI = jsoObj.getString("what").split(":");

            if (Arrays.asList(queryNamesFromUI).contains("locality")) {
                //we create a new json array without the locality filter object, rather we append then to the other objects in their filter objects
                arrayReplace = new JSONArray();
                for (Object obj : array) {
                    JSONObject jsoObj2 = (JSONObject) obj;
                    String[] queryFetchValues = jsoObj2.getString("what").split(":");
                    if (!Arrays.asList(queryFetchValues).contains("date") && !Arrays.asList(queryFetchValues).contains("locality")) {
                        JSONObject jsonObj = new JSONObject();
                        //jsoObj object in out loop object that contains all the locality filter attributes
                        //jsoObj2 this is current json object with what needs to be queried from database, eg commodity county
                        // replace("locality", "") ensures we dont add locality as name to the what(what to query- (name of query) saved on file) object
                        String whatReplaced = jsoObj2.getString("what") + jsoObj.getString("what").replace("locality", "");
                        jsonObj.put("what", whatReplaced);
                        try {
                            jsonObj.put("filter", jsoObj2.getString("filter"));
                        } catch (Exception e) {
                            //pass
                        }
                        arrayReplace.put(jsonObj);
                    }
                    if (Arrays.asList(queryFetchValues).contains("date")) {
                        JSONObject jsonObj = new JSONObject();
                        String whatReplaced = jsoObj2.getString("what");
                        jsonObj.put("what", whatReplaced);
                        jsonObj.put("filter", jsoObj2.getJSONObject("filter"));
                        arrayReplace.put(jsonObj);
                    }
                }
                return arrayReplace;
            }
        }

        return arrayReplace;
    }

    /**
     * Add columns names to the returned dataset
     *
     * @param columnsCount
     * @param rsMetaData
     * @param reslts
     * @param colmns
     * @return
     * @throws SQLException
     */
    private List<Map<String, String>> addColumnsHeaders(
            int columnsCount,
            ResultSetMetaData rsMetaData,
            String[][] reslts,
            List<Map<String, String>> colmns) throws SQLException {
        for (int x = 1; x <= columnsCount; x++) {
            log.debug("column name " + rsMetaData.getColumnName(x));
            reslts[x - 1][0] = rsMetaData.getColumnName(x);
            Map<String, String> colum = new HashMap();
            colum.put("title", rsMetaData.getColumnName(x));
            colmns.add(colum);
        }
        return colmns;
    }

    /**
     * Build the dataset/data body and populates with values gotten from the
     * database
     *
     * @param rs
     * @param columnsCount
     * @param rsMetaData
     * @param wrapperMap
     * @param reslts
     * @return
     * @throws SQLException
     */
    private Map<String, List<Object>> buildDataSet(
            ResultSet rs,
            int columnsCount,
            ResultSetMetaData rsMetaData,
            Map<String, List<Object>> wrapperMap,
            String[][] reslts) throws SQLException {
        List<List> reslts1 = new ArrayList();
        int rowIndex = 1; //initialize to 1 because first row occupied by column headers
        log.info("Getting resultset rows data");
        String colType;
        while (rs.next()) {
            rowIndex = rowIndex + 1;
            List<String> resultRow = new ArrayList();
            for (int x = 1; x <= columnsCount; x++) {
                colType = rsMetaData.getColumnTypeName(x);
                String val = "";
                try {
                    val = rs.getObject(x).toString();
                } catch (NullPointerException e) {
                    log.error(e);
                }
                reslts[x - 1][rowIndex - 1] = val;
                //resultRow.add(rs.getObject(x).toString());
                resultRow.add(val);
            }
            reslts1.add(resultRow);
            wrapperMap.put("data", (List<Object>) (Object) reslts1);
        }
        return wrapperMap;
    }

    public Map<String, List<Object>> runSqlQuery(String sqlQuery) {
        Database db = new Database();
        List<List> reslts1 = null;
        Map<String, List<Object>> wrapperMap = null;
        try {
            String[][] reslts;
            wrapperMap = new HashMap();
            Map<String, Object> results = db.executeQueryWithColumnCount(sqlQuery);
            ResultSet rs = (ResultSet) results.get("resultset");
            int rowsCount = (int) results.get("columncount");
            log.info("total row count from results " + rowsCount);
            ResultSetMetaData rsMetaData = rs.getMetaData();
            int columnsCount = rsMetaData.getColumnCount();
            log.info("total column count from results " + columnsCount);
            reslts = new String[columnsCount][rowsCount + 1];
            //add columns heads
            log.info("Getting columns headers");
            List<Map<String, String>> colmns = new ArrayList();
            colmns = addColumnsHeaders(columnsCount, rsMetaData, reslts, colmns);
            wrapperMap.put("columns", (List<Object>) (Object) colmns);
            wrapperMap = buildDataSet(rs, columnsCount, rsMetaData, wrapperMap, reslts);

        } catch (SQLException ex) {
            log.error(ex);
        } finally {
            db.CloseConnection();
        }
        return wrapperMap;
    }

    /**
     * Generates table finalJoinValues and gets the final query
     *
     * @param queriesToRun list of queries to run and their attribues including,
     * values to fetch, the join elements
     * @return Final Sql string
     */
    private String getQuery(List<Map<String, Object>> queriesToRun) {
        List<Map<String, Object>> _queriesToRun = queriesToRun;
        int queriesToRunListLength = queriesToRun.size() - 1;
        log.info("Queries to run are: " + (queriesToRunListLength + 1));
        String[] tableAliases = new String[queriesToRunListLength + 1];
        for (int x = 0; x <= queriesToRunListLength; x++) {
            String tableAliasName = RandomStringGenerator.getRandomString(10);
            while (tableAlias.contains(tableAliasName)) {
                tableAliasName = RandomStringGenerator.getRandomString(10);
            }
            tableAliases[x] = tableAliasName;
            tableAlias.add(tableAliasName);
            _queriesToRun.get(x).put("alias", tableAliasName);
        }
        return buildSql(_queriesToRun, queriesToRunListLength);

    }

    /**
     * creates the select from part of the sql from passed attributes
     *
     * @param finalQueryToRun finalquery to run string builder
     * @param _queriesToRun queries to run with finalJoinValues
     * @param queriesToRunListLength number of queries that need to be run
     * @return
     */
    private StringBuilder createSelectSqlSegment(StringBuilder finalQueryToRun, List<Map<String, Object>> _queriesToRun, int queriesToRunListLength) {
        boolean isFirstFetchValue = true;
        for (int x = 0; x <= queriesToRunListLength; x++) {
            //   append fetch values
            String r = (String) _queriesToRun.get(x).get("fetchValues");
            JSONArray fetchValues = new JSONArray(r);
            String alias = (String) _queriesToRun.get(x).get("alias");
            for (int y = 0; y <= fetchValues.length() - 1; y++) {
                StringBuilder g = new StringBuilder();
                log.info("fetch value to append " + alias + "." + fetchValues.getString(y) + " ");
                if (fetchValues.getString(y).length() != 0) {
                    if (isFirstFetchValue) {
                        finalQueryToRun.append(alias + "." + fetchValues.getString(y) + " ");
                        isFirstFetchValue = false;
                    } else {
                        finalQueryToRun.append("," + alias + "." + fetchValues.getString(y) + " ");
                    }
                }

            }
        }
        finalQueryToRun = addJoinValuesToSelectSegment(_queriesToRun, finalQueryToRun);
        return finalQueryToRun;
    }

    private StringBuilder addJoinValuesToSelectSegment(List<Map<String, Object>> _queriesToRun, StringBuilder finalQueryToRun) {
        List<String[]> sqlJoinValues = getJoinValues(_queriesToRun, 0);
        log.debug("The queries to run attribs " + _queriesToRun.toString());
        //String alias = (String) _queriesToRun.get(0).get("alias");
        log.debug("the join length " + sqlJoinValues.size());
        for (int x = 0; x <= sqlJoinValues.get(1).length - 1; x++) {
            if(sqlJoinValues.get(1)[x].trim().length()==0){ //check if join value is empty
                continue;
            }
            StringBuilder addCoalesce = new StringBuilder();
            addCoalesce.append("coalesce(");
            boolean isFirstAppend = true;
            for (int y = 0; y <= _queriesToRun.size() - 1; y++) {
                String alias = (String) _queriesToRun.get(y).get("alias");
                if (isFirstAppend) {
                    addCoalesce.append(alias + "." + sqlJoinValues.get(1)[x] + " ");
                    isFirstAppend = false;
                } else {

                    addCoalesce.append("," + alias + "." + sqlJoinValues.get(1)[x] + " ");
                }
            }
            addCoalesce.append(") as " + sqlJoinValues.get(1)[x] + " ");
            finalQueryToRun.append("," + addCoalesce.toString());
            //finalQueryToRun.append("," + alias + "." + sqlJoinValues.get(1)[x] + " ");
        }
        log.debug("The coalesed string " + finalQueryToRun);
        return finalQueryToRun;
    }
    
    
    /**
     *
     * @param finalQueryToRun String builder object for stiching different parts
     * of the final query
     * @param _queriesToRun list of queries to run and their attribues
     * including, values to fetch, the join elements and finalJoinValues
     * @param loopIndex index indicating current subquery in context
     * @return
     */
    private StringBuilder createFromSqlSegment(StringBuilder finalQueryToRun, List<Map<String, Object>> _queriesToRun, int loopIndex) {
        finalQueryToRun.append("from ");
        finalQueryToRun.append("(" + _queriesToRun.get(loopIndex).get("querySring") + ")");
        finalQueryToRun.append(" " + _queriesToRun.get(loopIndex).get("alias"));
        return finalQueryToRun;
    }

    /**
     *
     * @param finalQueryToRun String builder object for stiching different parts
     * of the final query
     * @param _queriesToRun list of queries to run and their attribues
     * including, values to fetch, the join elements and finalJoinValues
     * @param loopIndex index indicating current subquery in context
     * @return
     */
    private StringBuilder createJoinSqlSegment(StringBuilder finalQueryToRun, List<Map<String, Object>> _queriesToRun, int loopIndex) {
        finalQueryToRun.append(" full outer join ");
        finalQueryToRun.append("(" + _queriesToRun.get(loopIndex).get("querySring") + ")");
        finalQueryToRun.append(" " + _queriesToRun.get(loopIndex).get("alias"));
        finalQueryToRun.append(" on ");
        return finalQueryToRun;
    }

    /**
     *
     * @param _queriesToRun list of queries to run and their attribues
     * including, values to fetch, the join elements and finalJoinValues
     * @param loopIndex index indicating current subquery in context
     * @return
     */
    private List<String[]> getJoinValues(List<Map<String, Object>> _queriesToRun, int loopIndex) {
        List<String[]> finalJoinValues = new ArrayList();
        String joinVals = (String) _queriesToRun.get(loopIndex).get("joins");
        String[] joinValues = joinVals.replace("[", "").replace("]", "").replace("'", "").split(",");
        String[] previousSubqueryJoinValues = null;
        try {
            String _previousSubqueryJoinValues = (String) _queriesToRun.get(loopIndex - 1).get("joins");
            previousSubqueryJoinValues = _previousSubqueryJoinValues.replace("[", "").replace("]", "").replace("'", "").split(",");
        } catch (Exception e) {
            //pass
        }
        finalJoinValues.add(previousSubqueryJoinValues);
        finalJoinValues.add(joinValues);
        return finalJoinValues;
    }

    /**
     *
     * @param finalQueryToRun String builder object for stiching different parts
     * of the final query
     * @param loopIndex index indicating current subquery in context
     * @param previousSubqueryJoinValues the attributes/names from the previous
     * subquery in the loop that are used to inner join with current subquery
     * @param joinValues the attributes/names from the current subquery in the
     * loop that are used to inner join with previous subquery
     * @param alias current subquery alias
     * @param previousSubqueryJAlias previous subquery alias
     * @return
     */
    private StringBuilder createJoinOnSqlSegment(
            StringBuilder finalQueryToRun,
            int loopIndex, String[] previousSubqueryJoinValues,
            String[] joinValues,
            String alias,
            String previousSubqueryJAlias
    ) {
        int joinCount = 1;
        for (int y = 0; y <= joinValues.length - 1; y++) {
            if (joinCount == 1) {
            } else {
                finalQueryToRun.append(" and ");
            }
            for (int p = 0; p <= previousSubqueryJoinValues.length - 1; p++) {
                log.info("we join using same column names on this query and the previous one");
                if (joinValues[y].equals(previousSubqueryJoinValues[p])) {
                    log.info("joins values to join " + alias + "." + joinValues[y] + "=" + previousSubqueryJAlias + "." + previousSubqueryJoinValues[p]);
                    finalQueryToRun.append(alias + "." + joinValues[y] + "=" + previousSubqueryJAlias + "." + previousSubqueryJoinValues[p]);
                    joinCount = joinCount + 1;
                }
            }
        }
        return finalQueryToRun;
    }

    /**
     *
     * @param queriesToRun list of queries to run and their attribues including,
     * values to fetch, the join elements and finalJoinValues
     * @param queriesToRunLength the number of independent queries to perform
     * joins
     * @return runnable sql string
     */
    private String buildSql(List<Map<String, Object>> queriesToRun, int queriesToRunLength) {
        List<Map<String, Object>> _queriesToRun = queriesToRun;
        int queriesToRunListLength = queriesToRunLength;
        StringBuilder finalQueryToRun = new StringBuilder("Select ");
        finalQueryToRun = createSelectSqlSegment(finalQueryToRun, _queriesToRun, queriesToRunLength);

        boolean isFirstQuery = true;
        for (int x = 0; x <= queriesToRunListLength; x++) {
            //   querySring joins fetchValues
            if (isFirstQuery) {
                finalQueryToRun = createFromSqlSegment(finalQueryToRun, _queriesToRun, x);
                isFirstQuery = false;
                continue;
            }
            finalQueryToRun = createJoinSqlSegment(finalQueryToRun, _queriesToRun, x);
            String alias = (String) _queriesToRun.get(x).get("alias"); //current query alias
            String previousSubqueryJAlias = (String) _queriesToRun.get(x - 1).get("alias"); //previous query alias
            List<String[]> joinValues = getJoinValues(_queriesToRun, x);
            finalQueryToRun = createJoinOnSqlSegment(finalQueryToRun, x, joinValues.get(0), joinValues.get(1), alias, previousSubqueryJAlias);
        }
        log.info("Final query " + finalQueryToRun.toString());
        return finalQueryToRun.toString();
    }

    /**
     *
     * @param queryName the query name to use in fetching the query from file
     * @return query to run.
     */
    private Map<String, Object> getQueryToRun(String queryName) {
        log.info("Fetching query to run, passed name: " + queryName);
        String[] queryNamesFromUI = queryName.split(":");
        PropertiesLoader propLoader = new PropertiesLoader();
        Properties prop = propLoader.getPropertiesFile(queriesMatcherFile, "query_matcher.properties");
        Set<Object> queryMatcherPropsKeys = propLoader.getAllKeys(prop);
        Map<String, Object> queriesToAttributes = null;

        for (Object k : queryMatcherPropsKeys) {
            String key = (String) k;
            String[] queryMatcherPropKeyParts = key.split("-");
            boolean queryNameMatch = checkMatchingQueryName(queryNamesFromUI, queryMatcherPropKeyParts);

            if (queryNameMatch == true) {
                return getQueryAndItsAttributesFromFile(prop, key);
            }
        }
        return queriesToAttributes;
    }

    private Map<String, Object> getQueryAndItsAttributesFromFile(Properties prop, String propertyFileKey) {
        PropertiesLoader propLoader = new PropertiesLoader();
        String queryAttributes = prop.getProperty(propertyFileKey); //get the properties file of the query and the key(name)
        log.info("the query attributes 2 "+queryAttributes);
        log.info("Properties file of the query "+propertyFileKey);
        String[] qAttributes = queryAttributes.split(":");
        Properties queryFile = null;
        prop = propLoader.getPropertiesFile(queryFile, qAttributes[1]);
        log.info("Third parameter " + qAttributes[2]);
        Map<String, Object> queriesToAttributes = new HashMap();
        queriesToAttributes.put("querySring", prop.getProperty(qAttributes[0])); // the sql query
        log.debug("The join parameters are " + qAttributes[2]);
        queriesToAttributes.put("joins", qAttributes[2]); //columns that can be used to join to other queries
        queriesToAttributes.put("fetchValues", qAttributes[3]); //values that can be pulled from this query
        return queriesToAttributes; //return query to run
    }

    private boolean checkMatchingQueryName(String[] queryNamesFromUI, String[] queryMatcherPropKey) {
        boolean queryNameMatch = true;
        log.info("Query names to match " + Arrays.toString(queryNamesFromUI) + " and  from file: " + Arrays.toString(queryMatcherPropKey) + " length: " + queryNamesFromUI.length + " " + queryMatcherPropKey.length);

        if (queryNamesFromUI.length != queryMatcherPropKey.length) {
            return false; //if not of same length then contents of ui string and query_matcher are not the same
        }
        for (int x = 0; x <= queryNamesFromUI.length - 1; x++) {
            if (Arrays.asList(queryMatcherPropKey).contains(queryNamesFromUI[x])) {
                continue;
            } else {
                queryNameMatch = false;
                break;
            }
        }
        log.info("Did query strings match: " + queryNameMatch);
        return queryNameMatch;
    }

}
