/*
 * Created on Dec 9, 2009
 *
 */
package com.wolfram.alpha.impl;

import java.io.Serializable;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.visitor.Visitable;
import com.wolfram.alpha.visitor.Visitor;


public class WAPlainTextImpl implements WAPlainText, Visitable, Serializable {

    private String text;
    
    private static final long serialVersionUID = 7613237059547988592L;

    
    WAPlainTextImpl(Element thisElement) throws WAException {
        NodeList children = thisElement.getChildNodes();
        text = children.getLength() > 0 ? children.item(0).getNodeValue() : "";
    }

    
    ////////////////////  WAPlainText interface  //////////////////////////////
    
    public String getText() {
        return text;
    }
    
    
    ///////////////////////////  Visitor interface  ////////////////////////////
    
    public void accept(Visitor v) {
        v.visit(this);
    }
    
}
