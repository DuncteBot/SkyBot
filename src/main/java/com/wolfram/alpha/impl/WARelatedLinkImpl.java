/*
 * Created on Feb 24, 2010
 *
 */
package com.wolfram.alpha.impl;

import java.io.File;
import java.io.Serializable;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAImage;
import com.wolfram.alpha.WARelatedLink;
import com.wolfram.alpha.net.HttpProvider;
import com.wolfram.alpha.visitor.Visitor;

// This is called sidebarlinks in the XML.

public class WARelatedLinkImpl implements WARelatedLink, Serializable {

    private String url;
    private String text;
    private String source;
    private String excerpt;
    private WAImage image;
        
    static final WARelatedLinkImpl[] EMPTY_ARRAY = new WARelatedLinkImpl[0];

    private static final long serialVersionUID = -4694106442074004620L;

    
    WARelatedLinkImpl(Element thisElement, HttpProvider http, File tempDir) throws WAException {
        
        text = thisElement.getAttribute("text");
        url = thisElement.getAttribute("url");
        source = thisElement.getAttribute("source");
        if (text.equals("")) text = null;
        if (url.equals("")) url = null;
        if (source.equals("")) source = null;
        // Get the <excerpt> text.
        NodeList excerptElements = thisElement.getElementsByTagName("excerpt");
        // Should just be one or zero.
        int numExcerptElements = excerptElements.getLength();
        if (numExcerptElements > 0) {
            Element excerptElement = (Element) excerptElements.item(0);
            excerpt = excerptElement.getFirstChild().getNodeValue();
        }
        // Get the <img>.
        NodeList imgElements = thisElement.getElementsByTagName("img");
        // Should just be one.
        int numImgElements = imgElements.getLength();
        if (numImgElements > 0) {
            Element imgElement = (Element) imgElements.item(0);
            image = new WAImageImpl(imgElement, http, tempDir);
        }
    }


    public String getSource() {
        return source;
    }


    public String getText() {
        return text;
    }


    public String getURL() {
        return url;
    }


    public String getExcerpt() {
        return excerpt;
    }


    public WAImage getImage() {
        return image;
    }


    public void accept(Visitor v) {
        v.visit(this);
    }

}
