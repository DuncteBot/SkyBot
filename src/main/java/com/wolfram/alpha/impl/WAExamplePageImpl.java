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
