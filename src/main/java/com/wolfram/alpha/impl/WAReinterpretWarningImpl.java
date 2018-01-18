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
 * Created on Sep 19, 2010
 *
 */
package com.wolfram.alpha.impl;

import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAReinterpretWarning;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import java.io.Serializable;


public class WAReinterpretWarningImpl extends WAWarningImpl implements WAReinterpretWarning, Serializable {
    
    private static final long serialVersionUID = 7006649850656408617L;
    private String newInterpretation;
    private String[] alternatives;
    
    
    WAReinterpretWarningImpl(Element thisElement) throws WAException {
        
        super(thisElement);
        newInterpretation = thisElement.getAttribute("new");
        NodeList alternativeNodes = thisElement.getElementsByTagName("alternative");
        int numAlternatives = alternativeNodes.getLength();
        alternatives = new String[numAlternatives];
        for (int i = 0; i < numAlternatives; i++)
            alternatives[i] = alternativeNodes.item(i).getFirstChild().getNodeValue();
    }
    
    
    public String[] getAlternatives() {
        return alternatives;
    }
    
    
    public String getNew() {
        return newInterpretation;
    }
    
}
