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
public class CadreAllocation implements Cloneable {

    private String cadre;
    private String cadreCount; //total number allocated
    private String id;
    private String period;

    public String getCadre() {
        return cadre;
    }

    public void setCadre(String cadre) {
        this.cadre = cadre;
    }

    public String getCadreCount() {
        return cadreCount;
    }

    public void setCadreCount(String cadreCount) {
        this.cadreCount = cadreCount;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPeriod() {
        return period;
    }

    public void setPeriod(String period) {
        this.period = period;
    }

    public Object clone() throws CloneNotSupportedException {
        CadreAllocation cadreAllocation = (CadreAllocation) super.clone();
        return cadreAllocation;
    }

}
