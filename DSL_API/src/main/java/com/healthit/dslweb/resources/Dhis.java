package com.healthit.dslweb.resources;

import com.healthit.dslservice.DslException;
import com.healthit.dslservice.dao.DhisDao;
import com.healthit.dslservice.dao.FacilityDao;
import com.healthit.dslservice.dao.IhrisDao;
import com.healthit.dslservice.dto.adminstrationlevel.Facility;
import com.healthit.dslservice.dto.dhis.Indicator;
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
    @ResponseBody
    @RequestMapping(value = "/kpi2", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllCadres() {
        JsonBuilder jsBUilder=new JsonBuilder();
        jsBUilder.init();
        if (true) {
            return new ResponseEntity<String>("No Content found for this number", HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity(HttpStatus.OK);
        }
    }
    
    
    @ResponseBody
    @RequestMapping(value = "/indicators", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getIndicators(
     @RequestParam(value = "pe", required = false) String pe,
            @RequestParam(value = "ou", required = false) String ou,
            @RequestParam(value = "id", required = false) String id,
            @RequestParam(value = "groupid", required = false) String groupId
    ) {
        try {
            DhisDao dhisDao=new DhisDao();
            List<Indicator> indicatorList = dhisDao.getIndicators( pe,  ou,  id,  groupId);
            return new ResponseEntity<List>(indicatorList, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    
    @ResponseBody
    @RequestMapping(value = "/indicatorgroup", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getIndicatorGroups() {
        try {
            DhisDao dhisDao=new DhisDao();
            List<Map<String,String>> indicatorGroupList = dhisDao.getIndicatorGroups();
            return new ResponseEntity<List>(indicatorGroupList, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    
}
