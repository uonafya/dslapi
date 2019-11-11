package com.healthit.dslweb.resources;

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
                Map<String, Map> indicatorValue = dhisDao.getKPIValue(pe, ouid, id);
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
    public ResponseEntity<?> getIndicators(
            @RequestParam(value = "pe", required = false) String pe,
            @RequestParam(value = "ouid", required = false) String ouid,
            @PathVariable("id") String id
    ) {
        try {
            DhisDao dhisDao = new DhisDao();
            log.info("indicators without filter");
            Map<String, Map> indicatorValue = dhisDao.getKPIValue(pe, ouid, id);
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
        
        try {
            DhisDao dhisDao = new DhisDao();
            log.info("indicators without filter");
            Map<String, Map> predictedData = dhisDao.predict(indicatorId, ouid, periodType, periodSpan);
            return new ResponseEntity<Map<String, Map>>(predictedData, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

}
