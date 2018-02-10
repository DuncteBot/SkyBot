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
 * Created on Dec 9, 2009
 *
 */
package com.wolfram.alpha.impl;

import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.visitor.Visitable;
import com.wolfram.alpha.visitor.Visitor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.Serializable;


public class WAPlainTextImpl implements WAPlainText, Visitable, Serializable {
    
    private static final long serialVersionUID = 7613237059547988592L;
    private String text;
    
    
    WAPlainTextImpl(Element thisElement) {
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
