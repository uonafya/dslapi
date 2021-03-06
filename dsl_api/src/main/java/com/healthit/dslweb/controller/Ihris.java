/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslweb.controller;

import com.healthit.DslException;
import com.healthit.dslservice.dao.FacilityDao;
import com.healthit.dslservice.dao.IhrisDao;
import com.healthit.dslservice.dto.adminstrationlevel.Facility;
import com.healthit.dslservice.dto.ihris.Cadre;
import com.healthit.dslservice.dto.ihris.CadreAllocation;
import com.healthit.dslservice.dto.ihris.CadreGroup;
import com.healthit.message.Message;
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
public class Ihris {

    final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Ihris.class);

    @CrossOrigin
    @ResponseBody
    @RequestMapping(value = "/cadregroups", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCadreGroups(
            @RequestParam(value = "pe", required = false) String pe,
            @RequestParam(value = "ouid", required = false) String ouid,
            @RequestParam(value = "id", required = false) String cadreGroup) {
        System.out.println("without group");
        try {

            IhrisDao ihris = new IhrisDao();
            if (pe == null
                    && ouid == null
                    && cadreGroup == null) {
                List<CadreGroup> cadreGroupList = ihris.getAllCadresGroup();
                return new ResponseEntity<List>(cadreGroupList, HttpStatus.OK);
            } else {
                Map<String, Object> cadreAllocationList = ihris.getCadreGroupAllocation(pe, ouid, cadreGroup);
                log.info("======>>>");
                log.info(cadreAllocationList);
                return new ResponseEntity<Map<String, Object>>(cadreAllocationList, HttpStatus.OK);
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
    @RequestMapping(value = "/cadres", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCadres(
            @RequestParam(value = "pe", required = false) String pe,
            @RequestParam(value = "ouid", required = false) String ou,
            @RequestParam(value = "id", required = false) String cadre,
            @RequestParam(value = "periodtype", required = false) String periodtype,
            @RequestParam(value = "groupId", required = false) String groupId
    ) {
        try {
            IhrisDao ihris = new IhrisDao();
            if (pe != null
                    || ou != null
                    || cadre != null) {

                Map<String, Object> cadreAllocationList = ihris.getCadreAllocation(pe, ou, cadre,periodtype);
                return new ResponseEntity<Map<String, Object>>(cadreAllocationList, HttpStatus.OK);
            } else if (groupId != null) {// return list of indicators in this group id
                List<Cadre> cadreGroupList = ihris.getCadresByGroup(Integer.parseInt(groupId));
                return new ResponseEntity<List>(cadreGroupList, HttpStatus.OK);
            } else {
                List<Cadre> cadreList = ihris.getAllCadres();
                return new ResponseEntity<List>(cadreList, HttpStatus.OK);
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
    @RequestMapping(value = "/cadres/{id}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getSingleCadreAllocation(
            @RequestParam(value = "pe", required = false) String pe,
            @RequestParam(value = "ouid", required = false) String ou,
            @RequestParam(value = "periodtype", required = false) String periodtype,
            @PathVariable("id") String id
    ) {
        try {
            IhrisDao ihris = new IhrisDao();
            Map<String, Object> cadreAllocationList = ihris.getCadreAllocation(pe, ou, id,periodtype);
            return new ResponseEntity<Map<String, Object>>(cadreAllocationList, HttpStatus.OK);

        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        } catch (Exception e) {
            log.error("unknow request " + e);
            return new ResponseEntity<String>("Unknown request", HttpStatus.BAD_REQUEST);
        }

    }

}
