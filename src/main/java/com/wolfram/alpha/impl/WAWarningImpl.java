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
 * Created on Dec 8, 2009
 *
 */
package com.wolfram.alpha.impl;

import com.wolfram.alpha.WAWarning;
import com.wolfram.alpha.visitor.Visitable;
import com.wolfram.alpha.visitor.Visitor;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import java.io.Serializable;
import java.util.ArrayList;

// Warnings are not well defined in structure, bit they all have a text attribute. The other attributes are made
// available as an array of string pairs: {{"word", "Foo"}, {"suggestion", "Bar"}}.

public class WAWarningImpl implements WAWarning, Visitable, Serializable {

    static final WAWarningImpl[] EMPTY_ARRAY = new WAWarningImpl[0];
    private static final String[][] NO_ATTRIBUTES = new String[0][2];
    private static final long serialVersionUID = 2599384508960192266L;
    private String type;
    private String text;
    private String[][] attributes = NO_ATTRIBUTES;


    WAWarningImpl(Element thisElement) {

        type = thisElement.getNodeName();
        text = thisElement.getAttribute("text");
        ArrayList<String[]> attrPairs = new ArrayList<>();
        NamedNodeMap attrs = thisElement.getAttributes();
        int numAttrs = attrs.getLength();
        for (int i = 0; i < numAttrs; i++) {
            Node attr = attrs.item(i);
            String attrName = attr.getNodeName();
            if (!attrName.equals("text")) {
                String attrValue = attr.getNodeValue();
                attrPairs.add(new String[]{attrName, attrValue});
            }
        }
        if (attrPairs.size() > 0)
            attributes = attrPairs.toArray(new String[attrPairs.size()][2]);
    }


    public String[][] getAttributes() {
        return attributes;
    }


    public String getText() {
        return text;
    }


    public String getType() {
        return type;
    }


    ///////////////////////////  Visitor interface  ////////////////////////////

    public void accept(Visitor v) {
        v.visit(this);
    }


}
