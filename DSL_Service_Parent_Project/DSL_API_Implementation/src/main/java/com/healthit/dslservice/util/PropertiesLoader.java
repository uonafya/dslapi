/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.util;

import java.io.IOException;
import java.io.InputStream;
import org.apache.log4j.Logger;
import java.util.Properties;
import java.util.Set;

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
}
