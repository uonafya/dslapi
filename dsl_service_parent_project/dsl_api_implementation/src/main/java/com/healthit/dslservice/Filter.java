package com.healthit.dslservice;


/**
 *
 * @author duncan
 */
public class Filter{


    private int offset=1000;
    private int limit=1000;
 

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        this.limit = limit;
    }

    
}