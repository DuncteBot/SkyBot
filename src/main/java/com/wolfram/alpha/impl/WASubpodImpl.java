/*
 * Created on Dec 9, 2009
 *
 */
package com.wolfram.alpha.impl;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WASubpod;
import com.wolfram.alpha.net.HttpProvider;
import com.wolfram.alpha.visitor.Visitable;
import com.wolfram.alpha.visitor.Visitor;


public class WASubpodImpl implements WASubpod, Visitable, Serializable {

    private String title;
    
    private volatile boolean imageAcquired = false;
    private Object userData;
    
    private transient HttpProvider http;

       
    private Visitable[] contentElements = EMPTY_VISITABLE_ARRAY;
    
    static final WASubpodImpl[] EMPTY_ARRAY = new WASubpodImpl[0];
    
    private static final long serialVersionUID = 7545052461156130238L;

    
    WASubpodImpl(Element thisElement, HttpProvider http, File tempDir) throws WAException {
        
        this.http = http;

        title = thisElement.getAttribute("title");
        
        NodeList subElements = thisElement.getChildNodes();
        int numSubElements = subElements.getLength();
        List<Visitable> contentList = new ArrayList<Visitable>(numSubElements);
        for (int i = 0; i < numSubElements; i++) {
            Node child = subElements.item(i);
            String name = child.getNodeName();
            if ("plaintext".equals(name)) {
                contentList.add(new WAPlainTextImpl((Element) child));
            } else if ("img".equals(name)) {
                contentList.add(new WAImageImpl((Element) child, http, tempDir));
            }
        }
        contentElements = contentList.toArray(new Visitable[contentList.size()]);
    }
    
    
    ////////////////////  WASubpod interface  //////////////////////////////
    
    public String getTitle() {
        return title;
    }
    
    public Visitable[] getContents() {
        return contentElements;
    }
    
    public synchronized void setUserData(Object obj) {
        userData = obj;
    }
    
    public synchronized Object getUserData() {
        return userData;
    }

    ////////////////////////  hashCode()  /////////////////////////
    
    // We use hashCode() as a "content code" to tell us quickly whether the object's content
    // has changed since some point in the past. Note that we do not override equals() as well, 
    // but it is not necessary to override equals() when overriding hashCode() (although it _is_
    // necessary to override hashCode() when overriding equals()). Every field on which the
    // hashcode depends must be either immutable or volatile (or otherwise controlled by synchronized
    // blocks), as these values are changed on a background thread.
    // This is not a particularly good hash function, but it doesn't need to be. The only property
    // that really matters is that its value changes when the content of this object changes.
    
    public synchronized int hashCode() {
        
        int result = 17;
        result = 37*result + title.hashCode();
        for (Object obj : contentElements) {
            if (obj instanceof WAImageImpl) {
                result = 37*result + obj.hashCode();
                break;
            }
        }
        if (userData != null)
            result = 37*result + userData.hashCode();
        return result;
    }
    
    
    /////////////////////////////////////////////
    
    public void acquireImage() {
        
        // If this is a deserialized instance, http will be null. Such instances are "dead"; they can
        // never retrieve new content from the web.
        // The only synchronization needed here is that imageAcquired is volatile.
        if (!imageAcquired && http != null) {
            for (Object elem : contentElements) {
                if (elem instanceof WAImageImpl) {
                    WAImageImpl image = (WAImageImpl) elem;
                    image.acquireImage();
                    break;
                }
            }
            imageAcquired = true;
        }
    }
    

    ///////////////////////////  Visitable interface  ////////////////////////////
    
    public void accept(Visitor v) {
        v.visit(this);
    }
}
