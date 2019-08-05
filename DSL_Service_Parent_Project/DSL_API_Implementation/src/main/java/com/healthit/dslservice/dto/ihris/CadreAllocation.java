/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.dto.ihris;

/**
 *
 * @author duncan
 */
public class CadreAllocation {
    private String cadreid;
    private String cadreNumber; //total number allocated
    private String mflcode;
    private String period;

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }
    
    public String getCadreid() {
        return cadreid;
    }

    public void setCadreid(String cadreid) {
        this.cadreid = cadreid;
    }
    

    public String getCadreNumber() {
        return cadreNumber;
    }

    public void setCadreNumber(String cadreNumber) {
        this.cadreNumber = cadreNumber;
    }

    public String getMflcode() {
        return mflcode;
    }

    public void setMflcode(String mflcode) {
        this.mflcode = mflcode;
    }
    
    
    
}
