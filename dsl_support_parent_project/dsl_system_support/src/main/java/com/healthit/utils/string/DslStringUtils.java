/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.utils.string;

/**
 *
 * @author duncanndiithi
 */
public class DslStringUtils {
    public static String toCommaSperated(String s) {

        String[] sUnits = s.split(",");
        StringBuilder sUnitParameters = new StringBuilder();

        for (int x = 0; x < sUnits.length; x++) {
            if (x == 0) {
                sUnitParameters.append(sUnits[x]);
            } else {
                sUnitParameters.append("," + sUnits[x]);
            }
        }
        return sUnitParameters.toString();
    }
}
