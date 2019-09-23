/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.dto.adminstrationlevel;

import com.healthit.dslservice.dto.KephLevel;

/**
 *
 * @author duncan
 */
public class Facility extends Adminstration {

    private String wardId;
//    private String subCountyId;
//    private String facilityOwner;
//    private KephLevel kephLevel;

    public String getWardId() {
        return wardId;
    }

    public void setWardId(String wardId) {
        this.wardId = wardId;
    }
}
