/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.dto.adminstrationlevel;

/**
 *
 * @author duncan
 */
public class Constituency extends Adminstration{
   
    private String parentid;

    public String getParentid() {
        return parentid;
    }

    public void setParentid(String parentid) {
        this.parentid = parentid;
    }
    
    
}
