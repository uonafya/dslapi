/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package com.healthit.dslweb.controller;
import com.healthit.dslservice.DslException;
import com.healthit.dslservice.dao.SurveyDao;
import com.healthit.dslservice.message.Message;
import com.healthit.dslservice.message.MessageType;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author duncanndiithi
 */
@Controller
@RequestMapping("/survey")
public class SurveyController {

    final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(SurveyController.class);

    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "sources", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSurveySources() {
        SurveyDao survey = new SurveyDao();
        try {
            return new ResponseEntity(survey.getDataSources(), HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @RequestMapping(value = "sources/{sourceId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getIndicatorValue(
            @RequestParam(value = "id", required = false) String indicatorId,
            @RequestParam(value = "orgId", required = false) String orgId,
            @RequestParam(value = "pe", required = false) String pe,
            @RequestParam(value = "catId", required = false) String category_id,
            @PathVariable(value = "sourceId", required = true) String sourceId
    ) {
        SurveyDao survey = new SurveyDao();
        try {
            if (indicatorId == null && (orgId != null || pe != null || category_id != null)) {
                Message msg = new Message();
                msg.setMesageContent("Wrong URL construction");
                msg.setMessageType(MessageType.MISSING_PARAMETER_VALUE);
                throw new DslException(msg);
            }
            if (indicatorId == null) {
                return new ResponseEntity(survey.getIndicators(sourceId), HttpStatus.OK);
            } else {
                return new ResponseEntity(survey.getIndicatorValue(sourceId, indicatorId, orgId, pe, category_id), HttpStatus.OK);
            }
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
