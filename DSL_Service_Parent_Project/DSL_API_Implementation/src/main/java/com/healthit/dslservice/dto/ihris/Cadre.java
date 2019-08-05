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
public class Cadre {

    private String id;
    private String name;
    private String cadreGroupId;

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

    public String getCadreGroupId() {
        return cadreGroupId;
    }

    public void setCadreGroupId(String cadreGroupId) {
        this.cadreGroupId = cadreGroupId;
    }

}
