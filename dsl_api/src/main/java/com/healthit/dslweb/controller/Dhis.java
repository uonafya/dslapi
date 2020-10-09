package com.healthit.dslweb.controller;

import com.google.gson.Gson;
import com.healthit.dslservice.DslException;
import com.healthit.dslservice.dao.DhisDao;
import com.healthit.dslservice.dao.FacilityDao;
import com.healthit.dslservice.dao.IhrisDao;
import com.healthit.dslservice.dto.adminstrationlevel.Facility;
import com.healthit.dslservice.dto.dhis.Indicator;
import com.healthit.dslservice.dto.dhis.IndicatorGoup;
import com.healthit.dslservice.dto.dhis.IndicatorValue;
import com.healthit.dslservice.dto.ihris.Cadre;
import com.healthit.dslservice.dto.ihris.CadreGroup;
import com.healthit.dslservice.message.Message;
import com.healthit.dslweb.service.JsonBuilder;
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
 * @author duncan
 */
@Controller
public class Dhis {

    final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Dhis.class);

    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "/indicators", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getIndicators(
            @RequestParam(value = "pe", required = false) String pe,
            @RequestParam(value = "ouid", required = false) String ouid,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "groupId", required = false) String groupId // return list of indicators in this group id
    ) {
        try {
            DhisDao dhisDao = new DhisDao();
            log.info("parameters passed: " + "pe: " + pe + " ouid: " + ouid + " id: " + id);
            if (pe != null
                    || ouid != null
                    || id != null) {
                log.info("indicators without filter");
                Map<String, Map> indicatorValue = dhisDao.getKPIValue(pe, ouid, id, null);
                log.debug(indicatorValue);
                return new ResponseEntity<Map<String, Map>>(indicatorValue, HttpStatus.OK);

            } else if (groupId != null) {// return list of indicators in this group id
                log.info("indicators with group id");
                List<Indicator> indicatorList = dhisDao.getIndicatorsByGroup(Integer.parseInt(groupId));
                log.debug(indicatorList);
                return new ResponseEntity<List>(indicatorList, HttpStatus.OK);
            } else {
                log.info("raw indicators");
                List<Indicator> indicatorList = dhisDao.getIndicators();
                log.debug(indicatorList);
                return new ResponseEntity<List>(indicatorList, HttpStatus.OK);
            }
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("unknow request " + e);
            return new ResponseEntity<String>("Unknown request", HttpStatus.BAD_REQUEST);
        }

    }

    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "/indicators/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getIndicatorsByLevel(
            @RequestParam(value = "pe", required = false) String pe,
            @RequestParam(value = "ouid", required = false) String ouid,
            @RequestParam(value = "level", required = false) String level,
            @PathVariable("id") String id
    ) {
        try {
            DhisDao dhisDao = new DhisDao();
            log.info("indicators without filter");
            Map<String, Map> indicatorValue = dhisDao.getKPIValue(pe, ouid, id, level);
            log.debug(indicatorValue);
            return new ResponseEntity<Map<String, Map>>(indicatorValue, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("unknow request " + e);
            return new ResponseEntity<String>("Unknown request", HttpStatus.BAD_REQUEST);
        }

    }

    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "/indicatorgroups", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getIndicatorGroups(@RequestParam(value = "pe", required = false) String pe,
            @RequestParam(value = "ou", required = false) String ou,
            @RequestParam(value = "id", required = false) String id
    ) {
        try {
            DhisDao dhisDao = new DhisDao();
            List<IndicatorGoup> indicatorGroupList = dhisDao.getIndicatorGroups(pe, ou, id);
            return new ResponseEntity<List>(indicatorGroupList, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("unknow request " + e);
            return new ResponseEntity<String>("Unknown request", HttpStatus.BAD_REQUEST);
        }

    }

    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "/forecast/{indicatorid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> forecast(
            @RequestParam(value = "periodspan", required = false) String periodSpan,
            @RequestParam(value = "periodtype", required = false) String periodType,
            @RequestParam(value = "ouid", required = false) String ouid,
            @PathVariable("indicatorid") String indicatorId
    ) {
        //for documentation pourpose
        if (periodSpan != null || periodType != null) {
            if (periodSpan.trim().equals("x") || periodSpan.trim().equals("x")) {
                return forecastDummy();
            }
        }
        try {
            DhisDao dhisDao = new DhisDao();
            log.info("indicators without filter");
            Map<String, Map> predictedData = dhisDao.predict(indicatorId, ouid, periodType, periodSpan);
            return new ResponseEntity<Map<String, Map>>(predictedData, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    /**
     * This method is for documentation pourpose for predefined data returned by
     * predictorr
     *
     * @param periodSpan
     * @param periodType
     * @param ouid
     * @param indicatorId
     * @return
     */
    private ResponseEntity<?> forecastDummy() {
        Gson gson = new Gson();
        BufferedReader br = new BufferedReader(new InputStreamReader(
                this.getClass().getResourceAsStream("/predict.json")));
        Map results = gson.fromJson(br, Map.class);
        log.info("got data =====> ");
        log.info(results);
        return new ResponseEntity<Map<String, Map>>(results, HttpStatus.OK);

    }

    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "/indicator_correlation/{indicatorid}/{ouid}/{compareIndicators}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getIndicatorIndicatorCorrelation(
            @PathVariable(value = "ouid", required = false) String ouid,
            @PathVariable(value = "compareIndicators", required = false) String compareIndicators,
            @PathVariable("indicatorid") String indicatorId
    ) {

        try {
            DhisDao dhisDao = new DhisDao();
            Map<String, Map> correlationData = dhisDao.getIndicatorToIndicatorCorrelation(indicatorId, ouid, compareIndicators);
            return new ResponseEntity<Map<String, Map>>(correlationData, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "/weather_correlation/{indicatorid}/{ouid}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getIndicatorWeatherCorrelation(
            @PathVariable(value = "ouid", required = false) String ouid,
            @PathVariable("indicatorid") String indicatorId
    ) {

        try {
            DhisDao dhisDao = new DhisDao();
            Map<String, Map> correlationData = dhisDao.getWeatherToIndicatorCorrelation(indicatorId, ouid);
            return new ResponseEntity<Map<String, Map>>(correlationData, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "/forecast/indicator_weather/{indicatorid}/{ouid}/{weather_id}/{periodrange}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getForecastIndicatorWeather(
            @PathVariable(value = "ouid", required = false) String ouid,
            @PathVariable("indicatorid") String indicatorId,
            @PathVariable("weather_id") String weather_id,
            @PathVariable("periodrange") String period_range
    ) {

        try {
            DhisDao dhisDao = new DhisDao();
            Map<String, Map> forecastData = dhisDao.getWeatherToIndicatorForecast(indicatorId, ouid,weather_id,period_range);
            return new ResponseEntity<Map<String, Map>>(forecastData, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    
    
    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "/forecast/indicator_indicator/{indicatorid}/{ouid}/{compareIndicators}/{periodrange}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getForecastIndicatorToIndicator(
            @PathVariable(value = "ouid", required = false) String ouid,
            @PathVariable(value = "compareIndicators", required = false) String compareIndicators,
            @PathVariable("indicatorid") String indicatorId,
            @PathVariable("periodrange") String period_range
    ) {

        try {
            DhisDao dhisDao = new DhisDao();
            Map<String, Map> forecastData = dhisDao.getIndicatorToIndicatorForecast(indicatorId, ouid,compareIndicators,period_range);
            return new ResponseEntity<Map<String, Map>>(forecastData, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    

}
