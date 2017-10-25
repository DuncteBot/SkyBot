/*
 * Created on Dec 9, 2009
 *
 */
package com.wolfram.alpha.impl;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAPodState;
import com.wolfram.alpha.visitor.Visitable;
import com.wolfram.alpha.visitor.Visitor;

// TODO: Synchronization needs work...

public class WAPodStateImpl implements WAPodState, Visitable, Serializable {

    private String[] names = EMPTY_STRING_ARRAY;
    private String[] inputs = EMPTY_STRING_ARRAY;
    private String current = null;
    private int currentIndex = -1;
    private long id = 0;
    
    static final WAPodStateImpl[] EMPTY_ARRAY = new WAPodStateImpl[0];

    private static final String[] EMPTY_STRING_ARRAY = new String[]{};
    private static final String[] DEFAULT_NAME_ARRAY = new String[]{""};
    
    private static final long serialVersionUID = -253401729369983369L;

    
    WAPodStateImpl(Element thisElement) throws WAException {
        createFromDOM(thisElement);
    }
    
    private WAPodStateImpl() {}
    
    // Because all podstates stored in a WAQueryParameters are represented as WAPodStates, there is a need
    // to create a "dummy" podstate from just an input string. This is used by the addPodState(String) signature.
    // It is only used in performing a query, so its name value is irrelevant.
    WAPodStateImpl(String input) {
        this(input, 0);
    }
    
    public WAPodStateImpl(String input, long id) {
        inputs = new String[]{input};
        names = DEFAULT_NAME_ARRAY;
        currentIndex = 0;
        this.id = id;
    }
    
    
    private synchronized void createFromDOM(Element thisElement) throws WAException {
        
        // Two types:
        //
        // <state name="foo" input="bar"/>
        //
        // <statelist count=n value="current">
        //    <state name="name" input="input"/>
        // </statelist>

        String nodeName = thisElement.getNodeName();
        if ("state".equals(nodeName)) {
            String name = thisElement.getAttribute("name");
            String input = thisElement.getAttribute("input");
            // Old-style API results only have a name and not an input attribute. Support old API
            // by using name value as input value. This is probably not important, as by the time this
            // library is in use, all API servers will be running new-style code.
            if ("".equals(input))
                input = name;
            names = new String[]{name};
            inputs = new String[]{input};
        } else if ("statelist".equals(nodeName)) {
            String cur = thisElement.getAttribute("value");
            if (!"".equals(cur))
                current = cur;
            // Program defensively and don't assume that every element in a <statelist> is a <state>,
            // although we have no intention of making such a change in the API output.
            NodeList states = thisElement.getChildNodes();
            int numStates = states.getLength();
            List<Node> stateElements = new ArrayList<Node>(numStates);
            for (int i = 0; i < numStates; i++) {
                Node stateNode = states.item(i);
                if ("state".equals(stateNode.getNodeName()))
                    stateElements.add(stateNode);
            }
            int numStatesFound = stateElements.size();
            names = new String[numStatesFound];
            inputs = new String[numStatesFound];
            for (int i = 0; i < numStatesFound; i++) {
                Element stateElement = (Element) stateElements.get(i);
                String name = stateElement.getAttribute("name");
                String input = stateElement.getAttribute("input");
                // Old-style API results only have a name and not an input attribute. Support old API
                // by using name value as input value. This is probably not important, as by the time this
                // library is in use, all API servers will be running new-style code.
                if ("".equals(input))
                    input = name;
                names[i] = name;
                inputs[i] = input;
            }
            computeID();
        }
    }

    
    public String[] getNames() {
        return names;
    }

    public String[] getInputs() {
        return inputs;
    }

    public int getCurrentIndex() {
        
        if (currentIndex >= 0) {
            // Cached value was already computed.
            return currentIndex;
        } else if (current == null) {
            // Not a multi-value type of podstate.
            currentIndex = 0;
        } else {
            // Compute and cache.
            for (int i = 0; i < names.length; i++)
                if (current.equals(names[i])) {
                    currentIndex = i;
                    break;
                }
        }
        return currentIndex;
    }
    
    // Only call this for a <statelist> type of podstate.
    public WAPodState setCurrentIndex(int index) {
        
        WAPodStateImpl newState = new WAPodStateImpl();
        newState.names = this.names;
        newState.inputs = this.inputs;
        newState.currentIndex = index;
        newState.current = newState.names[index];
        newState.computeID();
        return newState;
    }
    
    public long getID() {
        return id;
    }
    
    ///////////////////////////////
    
    private void computeID() {
        // The id is basically a hash of the input strings.
        id = 17;
        for (String s : inputs)
            id += 37*id + s.hashCode();
    }
    
    ///////////////////////////  Visitor interface  ////////////////////////////
    
    public void accept(Visitor v) {
        v.visit(this);
    }

}
