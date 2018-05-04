/*
 * Created on Feb 8, 2010
 *
 */
package com.wolfram.alpha.impl;

import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WALink;
import com.wolfram.alpha.visitor.Visitor;
import org.w3c.dom.Element;

import java.io.Serializable;


public class WALinkImpl implements WALink, Serializable {

    private static final long serialVersionUID = 8863194509191889875L;
    private String url;
    private String text;
    private String title;


    WALinkImpl(Element thisElement) throws WAException {
        url = thisElement.getAttribute("url");
        text = thisElement.getAttribute("text");
        title = thisElement.getAttribute("title");
    }

    ////////////////////  WALink interface  //////////////////////////////

    public String getURL() {
        return url;
    }

    public String getText() {
        return text;
    }

    public String getTitle() {
        return title;
    }


    ////////////////////  Visitable interface  //////////////////////////////

    public void accept(Visitor v) {
        // TODO Auto-generated method stub
    }

}
