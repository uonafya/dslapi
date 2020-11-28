/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.util;

/**
 *
 * @author duncan
 * @deprecated
 */
public class RequestEntity {

    private String subject;
    private Object orgUnitID;
    private String orgUnitType;
    private String periodType;
    private Object period;

    public String getSubject() {
        return subject;
    }

    public void setSubject(String subject) {
        this.subject = subject;
    }

    public String getOrgUnitType() {
        return orgUnitType;
    }

    public void setOrgUnitType(String orgUnitType) {
        this.orgUnitType = orgUnitType;
    }

    public Object getOrgUnitID() {
        return orgUnitID;
    }

    public void setOrgUnitID(Object orgUnitID) {
        this.orgUnitID = orgUnitID;
    }

    public String getPeriodType() {
        return periodType;
    }

    public void setPeriodType(String periodType) {
        this.periodType = periodType;
    }

    public Object getPeriod() {
        return period;
    }

    public void setPeriod(Object period) {
        this.period = period;
    }

}
