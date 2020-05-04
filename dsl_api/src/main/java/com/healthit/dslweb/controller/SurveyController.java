/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslweb.controller;

import com.google.gson.Gson;
import com.healthit.dslservice.DslException;
import com.healthit.dslservice.dao.SurveyDao;
import com.healthit.dslservice.dto.dhis.Indicator;
import com.healthit.dslservice.dto.dhis.IndicatorGoup;
import com.healthit.dslservice.message.Message;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
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

    @RequestMapping(value = "sources/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getIndicatorsInSurveySource(
            @PathVariable("id") String id
    ) {
        SurveyDao survey = new SurveyDao();
        try {
            return new ResponseEntity(survey.getIndicators(id), HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
    
    @RequestMapping(value = "sources/{sourceId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getIndicatorValue(
            @RequestParam(value = "id") String indicatorId,
            @PathVariable("sourceId") String sourceId
    ) {
        SurveyDao survey = new SurveyDao();
        try {
            return new ResponseEntity(survey.getIndicatorValue(sourceId,indicatorId), HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
