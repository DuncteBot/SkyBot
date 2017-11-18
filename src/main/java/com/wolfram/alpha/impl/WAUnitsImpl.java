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
 * Created on Feb 8, 2010
 *
 */
package com.wolfram.alpha.impl;

import java.io.File;
import java.io.Serializable;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAImage;
import com.wolfram.alpha.WAUnits;
import com.wolfram.alpha.net.HttpProvider;
import com.wolfram.alpha.visitor.Visitor;


public class WAUnitsImpl implements WAUnits, Serializable {
    
    private String[] shortNames;
    private String[] longNames;
    private WAImage image;

    private static final long serialVersionUID = -1635250906549142822L;

    
    WAUnitsImpl(Element thisElement, HttpProvider http, File tempDir) throws WAException {    
        
        int numUnits = Integer.parseInt(thisElement.getAttribute("count"));
        shortNames = new String[numUnits];
        longNames = new String[numUnits];
        NodeList subElements = thisElement.getChildNodes();
        int numSubElements = subElements.getLength();
        int unitElementIndex = 0;
        for (int i = 0; i < numSubElements; i++) {
            Node child = subElements.item(i);
            String name = child.getNodeName();
            if ("unit".equals(name)) {
                Element unit = (Element) child;
                shortNames[unitElementIndex] = unit.getAttribute("short");
                longNames[unitElementIndex] = unit.getAttribute("long");
                unitElementIndex++;
            } else if ("img".equals(name)) {
                image = new WAImageImpl((Element) child, http, tempDir);
            }
        }
    }
    
    
    ////////////////////  WAUnits interface  //////////////////////////////
    
    public WAImage getImage() {
        return image;
    }

    public String[] getLongNames() {
        return longNames;
    }

    public String[] getShortNames() {
        return shortNames;
    }


    ////////////////////  Visitable interface  //////////////////////////////
    
    public void accept(Visitor v) {
        // TODO Auto-generated method stub
    }

}
