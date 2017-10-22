/*
 * Created on Sep 19, 2010
 *
 */
package com.wolfram.alpha.impl;

import java.io.Serializable;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAReinterpretWarning;


public class WAReinterpretWarningImpl extends WAWarningImpl implements WAReinterpretWarning, Serializable {

    private String newInterpretation;
    private String[] alternatives;

    private static final long serialVersionUID = 7006649850656408617L;

    
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
