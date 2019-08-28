/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslweb.resources;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author duncan
 */
@Controller
public class Dsl {

    final static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(Dsl.class);

    @RequestMapping({"/","/home"})
    public String showHomePage1() {
        return "api-guide.html";
    }
   

}
