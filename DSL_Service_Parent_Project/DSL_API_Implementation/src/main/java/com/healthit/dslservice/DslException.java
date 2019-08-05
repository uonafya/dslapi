/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.healthit.dslservice;

import com.healthit.dslservice.message.Message;

/**
 *
 * @author duncan
 */
public class DslException extends Exception{
    private Message msg;
    
    public DslException(Message msg){
        this.msg=msg;
    }

    public Message getMsg() {
        return msg;
    }

}
