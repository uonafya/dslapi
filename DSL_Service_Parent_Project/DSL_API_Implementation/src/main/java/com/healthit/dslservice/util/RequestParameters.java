/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.util;

import com.healthit.dslservice.DslException;
import com.healthit.dslservice.message.Message;
import com.healthit.dslservice.message.MessageType;
import org.apache.log4j.Logger;

/**
 *
 * @author duncan
 */
public class RequestParameters {
    final static Logger log = Logger.getLogger(RequestParameters.class);
    public static void isValidPeriodParamer(String pe) throws DslException {
        try {
            Integer.parseInt(pe);
        } catch (Exception e) {
            Message msg = new Message();
            msg.setMessageType(MessageType.NUMBER_FORMAT_ERROR);
            msg.setMesageContent("Please check the period parameter, format YYYY or YYYYmm");
            DslException dslExc = new DslException(msg);
            throw dslExc;
        }
        if (pe.trim().length() != 4 && pe.trim().length() != 6) {
            log.info("the period passed: "+pe);
            Message msg = new Message();
            msg.setMessageType(MessageType.YEAR_FORMAT_ERROR);
            msg.setMesageContent("Please check the period parameter, format YYYY or YYYYmm");
            DslException dslExc = new DslException(msg);
            throw dslExc;
        }
    }
}
