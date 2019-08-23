/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslweb.resources;

import com.healthit.dslservice.DslException;
import com.healthit.dslservice.dao.FacilityDao;
import com.healthit.dslservice.dao.KemsaDao;
import com.healthit.dslservice.dto.adminstrationlevel.Facility;
import com.healthit.dslservice.dto.kemsa.Commodity;
import com.healthit.dslservice.message.Message;
import java.util.List;
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
public class Kemsa {


    @ResponseBody
    @RequestMapping(value = "/commodities", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getAllCommodities() {
        try {
            KemsaDao kemsa = new KemsaDao();
            List<Commodity> commodityList = kemsa.getAllCommodities(
                    null,
                    null,
                    null
            );
            return new ResponseEntity<List>(commodityList, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
    
    @ResponseBody
    @RequestMapping(value = "/commodity_names", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<?> getCommodityNames() {
        try {
            KemsaDao kemsa = new KemsaDao();
            List<String> commodityNameList = kemsa.getCommodityNames();
            return new ResponseEntity<List>(commodityNameList, HttpStatus.OK);
        } catch (DslException ex) {
            return new ResponseEntity<Message>(ex.getMsg(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }
   
}
