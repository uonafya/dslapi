/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice;

import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author duncanndiithi
 */
public class DataSource {

    public static Map<Integer, String> getSources() {
        Map<Integer, String> sources = new HashMap();
        sources.put(1, "KHIS");
        sources.put(2, "GHO 2016");
        sources.put(3, "Annual WHO Global TB Report 2018");
        sources.put(4, "KENPHIA 2019");
        sources.put(5, "NHA 2015/16");
        sources.put(6, "HFA 2018/2019");
        sources.put(7, "Steps 2015");
        sources.put(8, "KDHS 2014");
        return sources;
    }

}
