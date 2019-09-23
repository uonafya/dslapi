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
public class Ward extends Adminstration{
    private String subcountyId;

    public String getSubcountyId() {
        return subcountyId;
    }

    public void setSubcountyId(String subcountyId) {
        this.subcountyId = subcountyId;
    }

    
}
