/*
 * Created on Dec 8, 2009
 *
 */
package com.wolfram.alpha.impl;

import java.io.Serializable;

import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import com.wolfram.alpha.WAAssumption;
import com.wolfram.alpha.WAException;
import com.wolfram.alpha.visitor.Visitable;
import com.wolfram.alpha.visitor.Visitor;


public class WAAssumptionImpl implements WAAssumption, Visitable, Serializable {

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
    
    
    static final WAAssumptionImpl[] EMPTY_ARRAY = new WAAssumptionImpl[0];

    private static final long serialVersionUID = -7699189119552569080L;

    
    WAAssumptionImpl(Element thisElement) throws WAException {
        
        type = thisElement.getAttribute("type");
        word = thisElement.getAttribute("word");
        if (word.equals("")) word = null;
        description = thisElement.getAttribute("desc");
        if (description.equals("")) description = null;
        // These two will fall back to their default values if the attributes are not present. In the case of 'count' that
        // should never happen, although 'current' is often missing.
        try { count = Integer.parseInt(thisElement.getAttribute("count")); } catch (NumberFormatException e) {}
        try { current = Integer.parseInt(thisElement.getAttribute("current")); } catch (NumberFormatException e) {}
        
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
            valids[i] = value.getAttribute("valid").equals("false") ? false : true;
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
