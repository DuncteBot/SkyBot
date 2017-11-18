/*
 * Created on Sep 20, 2010
 *
 */
package com.wolfram.alpha.impl;

import com.wolfram.alpha.WAExamplePage;
import com.wolfram.alpha.visitor.Visitor;
import org.w3c.dom.Element;

import java.io.Serializable;


public class WAExamplePageImpl implements WAExamplePage, Serializable {
    
    private static final long serialVersionUID = -1376525662169680759L;
    private String category;
    private String url;
    
    
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
