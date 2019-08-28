/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslweb.resources;

import com.healthit.dslservice.DslException;
import com.healthit.dslservice.dao.FacilityDao;
import com.healthit.dslservice.dao.IhrisDao;
import com.healthit.dslservice.dto.adminstrationlevel.Facility;
import com.healthit.dslservice.dto.ihris.Cadre;
import com.healthit.dslservice.dto.ihris.CadreAllocation;
import com.healthit.dslservice.dto.ihris.CadreGroup;
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
public class Ihris {
    final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Ihris.class);
    @ResponseBody
    @RequestMapping(value = "/cadregroups/{cadreGroupId}", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllCadresGroup(@PathVariable("cadreGroupId") int cadreGroupId) {
        System.out.println("by group");
        try {
            IhrisDao ihris = new IhrisDao();
            List<Cadre> cadreGroupList = ihris.getCadresByGroup(cadreGroupId);
            return new ResponseEntity<List>(cadreGroupList, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch(Exception e){
            log.error("unknow request "+e);
            return new ResponseEntity<String >("Unknown request", HttpStatus.BAD_REQUEST);
        }

    }

    @ResponseBody
    @RequestMapping(value = "/cadregroups", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCadreGroups(
            @RequestParam(value = "pe", required = false) String pe,
            @RequestParam(value = "ou", required = false) String ou,
            @RequestParam(value = "cadregroupid", required = false) String cadreGroup) {
        System.out.println("without group");
        try {

            IhrisDao ihris = new IhrisDao();
            if (    pe == null
                    && ou == null
                    && cadreGroup == null) {
                List<CadreGroup> cadreGroupList = ihris.getAllCadresGroup();
                return new ResponseEntity<List>(cadreGroupList, HttpStatus.OK);
            } else {
                List<CadreAllocation> cadreAllocationList = ihris.getCadreGroupAllocation( pe,  ou,  cadreGroup);
                return new ResponseEntity<List>(cadreAllocationList, HttpStatus.OK);
            }
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch(Exception e){
            log.error("unknow request "+e);
            return new ResponseEntity<String >("Unknown request", HttpStatus.BAD_REQUEST);
        }

    }

    @ResponseBody
    @RequestMapping(value = "/cadres", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllCadres(
            @RequestParam(value = "pe", required = false) String pe,
            @RequestParam(value = "ouid", required = false) String ou,
            @RequestParam(value = "id", required = false) String cadre,
            @RequestParam(value = "cadreGroup", required = false) String cadreGroup
    ) {
        try {
            IhrisDao ihris = new IhrisDao();
            if (pe == null
                    && ou == null
                    && cadre == null
                    && cadreGroup == null) {
                List<Cadre> cadreList = ihris.getAllCadres();
                return new ResponseEntity<List>(cadreList, HttpStatus.OK);
            } else {
                List<CadreAllocation> cadreAllocationList = ihris.getCadreAllocation(pe, ou, cadre, cadreGroup);
                return new ResponseEntity<List>(cadreAllocationList, HttpStatus.OK);
            }

        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }catch(Exception e){
            log.error("unknow request "+e);
            return new ResponseEntity<String >("Unknown request", HttpStatus.BAD_REQUEST);
        }

    }

}
