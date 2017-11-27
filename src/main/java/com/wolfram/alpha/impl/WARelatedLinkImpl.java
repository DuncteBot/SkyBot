/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Sanduhr32
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

/*
 * Created on Feb 24, 2010
 *
 */
package com.wolfram.alpha.impl;

import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAImage;
import com.wolfram.alpha.WARelatedLink;
import com.wolfram.alpha.net.HttpProvider;
import com.wolfram.alpha.visitor.Visitor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.Serializable;

// This is called sidebarlinks in the XML.

public class WARelatedLinkImpl implements WARelatedLink, Serializable {
    
    static final WARelatedLinkImpl[] EMPTY_ARRAY = new WARelatedLinkImpl[0];
    private static final long serialVersionUID = -4694106442074004620L;
    private String url;
    private String text;
    private String source;
    private String excerpt;
    private WAImage image;
    
    
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
