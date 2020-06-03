package com.healthit.dslweb.controller;

import com.healthit.dslservice.DslException;
import com.healthit.dslservice.dao.LocationDao;
import com.healthit.dslservice.dao.PandemicDao;
import com.healthit.dslservice.dto.adminstrationlevel.Constituency;
import com.healthit.dslservice.message.Message;
import java.util.List;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

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

}
