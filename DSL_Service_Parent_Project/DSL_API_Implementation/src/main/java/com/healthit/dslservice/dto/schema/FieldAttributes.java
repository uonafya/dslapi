/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice.dto.schema;

import java.util.List;

/**
 *
 * @author duncan
 */
public class FieldAttributes {

    private FieldAttributes() {
    }
    
    private Field dependentField;
    private List<Aggregation> aggregationType;
    private Table sourceTable;
    private DataType dataType;
    private String alias;

    public static class AttributesBuilder {

        private Field dependentField;
        private List<Aggregation> aggregationType;
        private Table sourceTable;
        private DataType dataType;
        private String alias;

        public FieldAttributes build() {
            FieldAttributes fieldAttributes = new FieldAttributes();
            fieldAttributes.dependentField = this.dependentField;
            fieldAttributes.aggregationType = this.aggregationType;
            fieldAttributes.sourceTable = this.sourceTable;
            fieldAttributes.dataType = this.dataType;
            fieldAttributes.alias = this.alias;
            return fieldAttributes;
        }
    }

}
