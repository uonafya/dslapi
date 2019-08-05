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
    private String subCountyId;
    private String facilityOwner;
    private KephLevel kephLevel;

    public String getWardId() {
        return wardId;
    }

    public void setWardId(String wardId) {
        this.wardId = wardId;
    }

    public String getSubCountyId() {
        return subCountyId;
    }

    public void setSubCountyId(String subCountyId) {
        this.subCountyId = subCountyId;
    }

    public String getFacilityOwner() {
        return facilityOwner;
    }

    public void setFacilityOwner(String facilityOwner) {
        this.facilityOwner = facilityOwner;
    }

    public KephLevel getKephLevel() {
        return kephLevel;
    }

    public void setKephLevel(KephLevel kephLevel) {
        this.kephLevel = kephLevel;
    }

}
