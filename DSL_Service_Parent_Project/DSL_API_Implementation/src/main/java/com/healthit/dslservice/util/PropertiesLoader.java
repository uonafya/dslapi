/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.log4j.Logger;
import java.util.Properties;
import java.util.Set;
import org.json.JSONArray;

/**
 *
 * @author duncan
 */
public class PropertiesLoader {

    final static Logger log = Logger.getLogger(PropertiesLoader.class.getCanonicalName());

    public Properties getPropertiesFile(Properties propertyFile, String propertyFileName) {
        log.info("loading properties file: "+ propertyFileName);
        try {
            if (propertyFile == null) {
                log.debug("properties file object null, loading to memory");
                propertyFile = new Properties();
                //InputStream s = Properties.class.getResourceAsStream("query_matcher.properties");
                InputStream s =  PropertiesLoader.class.getClassLoader().getResourceAsStream(propertyFileName);
                String p;
                if (s==null) p="is null"; else p="not null";
                log.debug("Stream status "+p);
                propertyFile.load(s);
                s.close();
            }

        } catch (IOException ex) {
            log.error(ex);
        }
        return propertyFile;
    }

    public Set<Object> getAllKeys(Properties prop) {
        Set<Object> keys = prop.keySet();
        return keys;
    }
    
    /**
     * Checks the request body for indicators/subjects with available metadata
     * and calls relevant functions to process meta info.
     *
     * @param pBody
     * @return
     */
    public static List<RequestEntity> _sort(JSONArray pBody) {
        log.info("metadata sorting");
        RequestBodyDissolver requestBodyDissolver = new RequestBodyDissolver();
        List<RequestEntity> rqtEntities = requestBodyDissolver.dissolve(pBody);
        log.info("The meta list " + rqtEntities);
        return rqtEntities;
    }
}
