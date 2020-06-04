package com.healthit.dslweb.controller;

import com.healthit.dslservice.DslException;
import com.healthit.dslservice.dao.LocationDao;
import com.healthit.dslservice.dao.PandemicDao;
import com.healthit.dslservice.dao.SurveyDao;
import com.healthit.dslservice.dto.adminstrationlevel.Constituency;
import com.healthit.dslservice.message.Message;
import com.healthit.dslservice.message.MessageType;
import java.text.ParseException;
import java.util.List;
import java.util.Map;
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
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 *
 * @author duncan
 */
@Controller
public class Pandemic {

    final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Pandemic.class);

    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "/pandemics", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getPandemics() {
        try {
            PandemicDao pandemicDao = new PandemicDao();
            List<Map> pandemicList = pandemicDao.getPandemics();
            return new ResponseEntity<List<Map>>(pandemicList, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("unknow request " + e);
            return new ResponseEntity<String>("Unknown request", HttpStatus.BAD_REQUEST);
        }

    }

    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "pandemics/{pandemicSource}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getIndicatorValue(
            @RequestParam(value = "id", required = false) String indicatorId,
            @RequestParam(value = "orgId", required = false) String orgId,
            @RequestParam(value = "start_date", required = false) String startDate,
            @RequestParam(value = "end_date", required = false) String endDate,
            @PathVariable(value = "pandemicSource", required = true) String pandemicSource
    ) {
        
        log.info("parameters ===>");
        log.info(indicatorId);
        log.info(orgId);
        log.info(startDate);
        log.info(endDate);
        log.info(pandemicSource);
        log.info("===========>");
        
        PandemicDao pandemicDao = new PandemicDao();
        try {
            if (startDate != null || endDate != null) {

                try {
                    if (startDate != null) {
                        Date _startDate = new SimpleDateFormat("yyyy-MM-dd").parse(startDate);
                    }
                } catch (ParseException ex) {
                    Message msg = new Message();
                    msg.setMesageContent("Wrong start date format. Should be yyyy-MM-dd");
                    msg.setMessageType(MessageType.YEAR_FORMAT_ERROR);
                    throw new DslException(msg);
                }

                try {
                    if (endDate != null) {
                        Date _endDate = new SimpleDateFormat("yyyy-MM-dd").parse(endDate);
                    }
                } catch (ParseException ex) {
                    Message msg = new Message();
                    msg.setMesageContent("Wronf end date format. Should be yyyy-MM-dd");
                    msg.setMessageType(MessageType.YEAR_FORMAT_ERROR);
                    throw new DslException(msg);
                }

            }

            return new ResponseEntity(pandemicDao.getPandemicData(pandemicSource, indicatorId, orgId, startDate, endDate), HttpStatus.OK);

        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

}
