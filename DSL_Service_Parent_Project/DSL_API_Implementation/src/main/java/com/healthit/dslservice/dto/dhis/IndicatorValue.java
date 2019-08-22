/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.dto.dhis;

/**
 *
 * @author duncan
 */
public class IndicatorValue {
    private String id;
    private String name;
    private String pe;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPe() {
        return pe;
    }

    public void setPe(String pe) {
        this.pe = pe;
    }
    
    
}
