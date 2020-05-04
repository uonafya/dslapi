/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslweb.controller;

import com.healthit.dslservice.DslException;
import com.healthit.dslservice.dao.LocationDao;
import com.healthit.dslservice.dto.adminstrationlevel.Constituency;
import com.healthit.dslservice.dto.adminstrationlevel.County;
import com.healthit.dslservice.dto.adminstrationlevel.Facility;
import com.healthit.dslservice.dto.adminstrationlevel.Ward;
import com.healthit.dslservice.message.Message;
import java.util.List;
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
public class Location {

    final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Location.class);

    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "/wards", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getALlWards() {
        try {
            LocationDao locationDao = new LocationDao();
            List<Ward> wardList = locationDao.getALlWards();
            return new ResponseEntity<List>(wardList, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("unknow request " + e);
            return new ResponseEntity<String>("Unknown request", HttpStatus.BAD_REQUEST);
        }

    }

    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "/subcounties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getConstituencies() {
        try {
            LocationDao locationDao = new LocationDao();
            List<Constituency> constituencyList = locationDao.getSubCounties();
            return new ResponseEntity<List>(constituencyList, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("unknow request " + e);
            return new ResponseEntity<String>("Unknown request", HttpStatus.BAD_REQUEST);
        }

    }

    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "/counties", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCounties() {
        try {
            LocationDao locationDao = new LocationDao();
            List<County> countyList = locationDao.getCounties();
            return new ResponseEntity<List>(countyList, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("unknow request " + e);
            return new ResponseEntity<String>("Unknown request", HttpStatus.BAD_REQUEST);
        }

    }

}
