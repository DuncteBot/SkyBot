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
 *
 */

/*
 * Created on Feb 24, 2010
 *
 */
package com.wolfram.alpha.impl;

import java.io.Serializable;

import org.w3c.dom.Element;

import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WASourceInfo;
import com.wolfram.alpha.visitor.Visitor;

// This is called sidebarlinks in the XML.


public class WASourceInfoImpl implements WASourceInfo, Serializable {

    private String url;
    private String formattedUrl;
    private String text;    
    
    static final WASourceInfoImpl[] EMPTY_ARRAY = new WASourceInfoImpl[0];

    private static final long serialVersionUID = -6541107289959358774L;

    
    WASourceInfoImpl(Element thisElement) throws WAException {
        
        text = thisElement.getAttribute("text");
        url = thisElement.getAttribute("url");
        // Transform:
        //    http://www.wolframalpha.com/sources/CityDataSourceInformationNotes.html
        // into:
        //    http://www.wolframalpha.com/input/sources.jsp?sources=CityData
        if (url.endsWith("SourceInformationNotes.html")) {
            String dataType = url.substring(url.lastIndexOf('/') + 1, url.length() - "SourceInformationNotes.html".length());
            formattedUrl = "http://www.wolframalpha.com/input/sources.jsp?sources=" + dataType;
        } else {
            formattedUrl = url;
        }
    }


    public String getText() {
        return text;
    }

    public String getURL() {
        return url;
    }

    public String getFormattedURL() {
        return formattedUrl;
    }

    
    public void accept(Visitor v) {
        v.visit(this);
    }

}
