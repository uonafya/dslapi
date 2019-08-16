/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslweb.resources;

import com.healthit.dslservice.DslException;
import com.healthit.dslservice.dao.FacilityDao;
import com.healthit.dslservice.dto.adminstrationlevel.Facility;
import com.healthit.dslservice.dto.kmfl.FacilityLevel;
import com.healthit.dslservice.dto.kmfl.FacilityType;
import com.healthit.dslservice.message.Message;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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
public class Kmfl {
    @ResponseBody
    @RequestMapping(value = "/facilities_ty", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllFacilities(@RequestParam String msisdn) {
        
       
        if (true) {
            return new ResponseEntity<String>("No Content found for this number", HttpStatus.NOT_FOUND);
        } else {
            return new ResponseEntity(HttpStatus.OK);
        }
    }
    
    @ResponseBody
    @RequestMapping(value = "/facilities", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllFacility() {
        try {
            FacilityDao facilityDao=new FacilityDao();
            List<Facility> facilityList = facilityDao.getFacilities();
            return new ResponseEntity<List>(facilityList, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    
    
    @ResponseBody
    @RequestMapping(value = "/facilitylevel", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllFacilityLevels() {
        try {
            FacilityDao facilityDao=new FacilityDao();
            List<FacilityLevel> facilityLevel = facilityDao.getFacilitiesLevel();
            return new ResponseEntity<List>(facilityLevel, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    
    @ResponseBody
    @RequestMapping(value = "/facilitytype", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllFacilityTypes() {
        try {
            FacilityDao facilityDao=new FacilityDao();
            List<FacilityType> facilityList = facilityDao.getFacilitiesType();
            return new ResponseEntity<List>(facilityList, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    
    
    @ResponseBody
    @RequestMapping(value = "/facilitytype/{facilityTypeId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllCadresGroup(@PathVariable("facilityTypeId") int facilityTypeId) {
        System.out.println("by group");
        try {
            FacilityDao facilityDao=new FacilityDao();
            List<Facility> facilityList  = facilityDao.getFacilitiesByType(facilityTypeId);
            return new ResponseEntity<List>(facilityList, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    
}
