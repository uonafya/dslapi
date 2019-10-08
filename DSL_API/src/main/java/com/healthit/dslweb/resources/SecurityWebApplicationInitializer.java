/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslweb.resources;


import org.springframework.security.web.context.*;
/**
 *
 * @author duncan
 */
public class SecurityWebApplicationInitializer  extends AbstractSecurityWebApplicationInitializer {

    public SecurityWebApplicationInitializer() {
        super(SecurityConfig.class);
    }
}

