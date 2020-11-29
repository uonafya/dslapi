package com.healthit.message;

/**
 *
 * @author duncan
 */
public class MessageType {
    public final static String DB_CONNECTION_ERROR="Error Code: 1001 Database Connection Error";
    public final static String SQL_QUERY_ERROR="Error Code: 1003 SQL query Error";
    public final static String NUMBER_FORMAT_ERROR="Error Code: 1005 Wrong number format";
    public final static String YEAR_FORMAT_ERROR="Error Code: 1007 Wrong period format";
    public final static String MISSING_DB_ENRTY_VALUE="Error Code: 1009 Object not found";
    public final static String MISSING_PARAMETER_VALUE="Error Code: 1011 Missing parameter value";
    public final static String ORGUNIT_LEVEL="Error Code: 1013 Provided level higher than orgunit level";
    public final static String MISSING_DATA="Error Code: 1015 No data found";
}
