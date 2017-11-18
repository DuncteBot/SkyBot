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
 * Created on Dec 9, 2009
 *
 */
package com.wolfram.alpha.impl;

import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAInfo;
import com.wolfram.alpha.net.HttpProvider;
import com.wolfram.alpha.visitor.Visitable;
import com.wolfram.alpha.visitor.Visitor;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


public class WAInfoImpl implements WAInfo, Serializable {

    static final WAInfoImpl[] EMPTY_ARRAY = new WAInfoImpl[0];
    private static final long serialVersionUID = 687066271144463657L;
    private String text;
    private Visitable[] contentElements = EMPTY_VISITABLE_ARRAY;

    
    WAInfoImpl(Element thisElement, HttpProvider http, File tempDir) throws WAException {
        
        text = thisElement.getAttribute("text");
        
        NodeList subElements = thisElement.getChildNodes();
        int numSubElements = subElements.getLength();
        List<Visitable> contentList = new ArrayList<Visitable>(numSubElements);
        for (int i = 0; i < numSubElements; i++) {
            Node child = subElements.item(i);
            String name = child.getNodeName();
            if ("link".equals(name)) {
                contentList.add(new WALinkImpl((Element) child));
            } else if ("img".equals(name)) {
                contentList.add(new WAImageImpl((Element) child, http, tempDir));
            } else if ("units".equals(name)) {
                contentList.add(new WAUnitsImpl((Element) child, http, tempDir));
            }
        }
        contentElements = contentList.toArray(new Visitable[contentList.size()]);
    }
    
    
    public Visitable[] getContents() {
        return contentElements;
    }

    public String getText() {
        return text;
    }

    
    ///////////////////////////  Visitor interface  ////////////////////////////
    
    public void accept(Visitor v) {
        v.visit(this);
    }

}
