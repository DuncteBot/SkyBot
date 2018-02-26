/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
 * Created on Feb 8, 2010
 *
 */
package com.wolfram.alpha.impl;

import com.wolfram.alpha.WALink;
import com.wolfram.alpha.visitor.Visitor;
import org.w3c.dom.Element;

import java.io.Serializable;


public class WALinkImpl implements WALink, Serializable {
    
    private static final long serialVersionUID = 8863194509191889875L;
    private String url;
    private String text;
    private String title;

    
    WALinkImpl(Element thisElement) {
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
