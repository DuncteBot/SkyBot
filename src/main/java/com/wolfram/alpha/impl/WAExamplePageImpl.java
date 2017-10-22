/*
 * Created on Sep 20, 2010
 *
 */
package com.wolfram.alpha.impl;

import java.io.Serializable;

import org.w3c.dom.Element;

import com.wolfram.alpha.WAExamplePage;
import com.wolfram.alpha.visitor.Visitor;


public class WAExamplePageImpl implements WAExamplePage, Serializable {

    private String category;
    private String url;
    
    private static final long serialVersionUID = -1376525662169680759L;

    
    WAExamplePageImpl(Element thisElement) {
        
        category = thisElement.getAttribute("category");
        url = thisElement.getAttribute("url");
    }


    public String getCategory() {
        return category;
    }


    public String getURL() {
        return url;
    }


    ///////////////////////////  Visitor interface  ////////////////////////////
    
    public void accept(Visitor v) {
        v.visit(this);
    }

}
