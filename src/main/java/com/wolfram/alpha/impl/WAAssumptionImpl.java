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

import com.wolfram.alpha.WAAssumption;
import com.wolfram.alpha.visitor.Visitable;
import com.wolfram.alpha.visitor.Visitor;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.Serializable;


public class WAAssumptionImpl implements WAAssumption, Visitable, Serializable {

    static final WAAssumptionImpl[] EMPTY_ARRAY = new WAAssumptionImpl[0];
    private static final long serialVersionUID = -7699189119552569080L;
    private String type;
    private int count;
    private String word;
    private String description;
    private int current = -1;
    private String[] names;
    private String[] inputs;
    private String[] descriptions;
    private String[] words;
    private boolean[] valids;


    WAAssumptionImpl(Element thisElement) {

        type = thisElement.getAttribute("type");
        word = thisElement.getAttribute("word");
        if (word.equals("")) word = null;
        description = thisElement.getAttribute("desc");
        if (description.equals("")) description = null;
        // These two will fall back to their default values if the attributes are not present. In the case of 'count' that
        // should never happen, although 'current' is often missing.
        try {
            count = Integer.parseInt(thisElement.getAttribute("count"));
        } catch (NumberFormatException ignored) {
        }
        try {
            current = Integer.parseInt(thisElement.getAttribute("current"));
        } catch (NumberFormatException ignored) {
        }

        NodeList valueElements = thisElement.getElementsByTagName("value");
        int numValueElements = valueElements.getLength();
        names = new String[numValueElements];
        inputs = new String[numValueElements];
        descriptions = new String[numValueElements];
        words = new String[numValueElements];
        valids = new boolean[numValueElements];
        for (int i = 0; i < numValueElements; i++) {
            Element value = (Element) valueElements.item(i);
            names[i] = value.getAttribute("name");
            inputs[i] = value.getAttribute("input");
            descriptions[i] = value.getAttribute("desc");
            words[i] = value.getAttribute("word");
            valids[i] = !value.getAttribute("valid").equals("false");
        }
    }


    public String getType() {
        return type;
    }

    public int getCount() {
        return count;
    }

    public String getWord() {
        return word;
    }

    public String getDescription() {
        return description;
    }

    public int getCurrent() {
        return current;
    }


    public String[] getNames() {
        return names;
    }

    public String[] getDescriptions() {
        return descriptions;
    }

    public String[] getInputs() {
        return inputs;
    }

    public String[] getWords() {
        return words;
    }

    public boolean[] getValidities() {
        return valids;
    }


    ///////////////////////////  Visitor interface  ////////////////////////////

    public void accept(Visitor v) {
        v.visit(this);
    }

}
