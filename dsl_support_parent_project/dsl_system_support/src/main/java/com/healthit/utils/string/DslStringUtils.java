/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.utils.string;

import com.healthit.DslException;
import com.healthit.message.Message;
import com.healthit.message.MessageType;

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

    public static void validateIntFromString(String s, String splitter) throws DslException {
        String[] sUnits = s.split(splitter);
        for (int x = 0; x < sUnits.length; x++) {
            try {
                Integer.parseInt(sUnits[x]);
            } catch (Exception ex) {
                Message msg = new Message();
                msg.setMesageContent("Wrong number format give in parameter");
                msg.setMessageType(MessageType.NUMBER_FORMAT_ERROR);
                throw new DslException(msg);
            }
        }
    }
}
