/*
 * Created on Sep 19, 2010
 *
 */
package com.wolfram.alpha.impl;

import java.io.Serializable;

import org.w3c.dom.Element;

import com.wolfram.alpha.WAFutureTopic;
import com.wolfram.alpha.visitor.Visitor;


public class WAFutureTopicImpl implements WAFutureTopic, Serializable {

    private String msg;
    private String topic;
    
    private static final long serialVersionUID = -511306768207916575L;

    
    WAFutureTopicImpl(Element thisElement) {
        
        msg = thisElement.getAttribute("msg");
        topic = thisElement.getAttribute("topic");
    }


    public String getMessage() {
        return msg;
    }


    public String getTopic() {
        return topic;
    }


    ///////////////////////////  Visitor interface  ////////////////////////////
    
    public void accept(Visitor v) {
        v.visit(this);
    }

}
