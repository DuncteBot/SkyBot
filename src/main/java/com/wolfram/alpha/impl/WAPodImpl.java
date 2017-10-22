/*
 * Created on Dec 8, 2009
 *
 */
package com.wolfram.alpha.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.FactoryConfigurationError;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAInfo;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAPodState;
import com.wolfram.alpha.WASound;
import com.wolfram.alpha.WASubpod;
import com.wolfram.alpha.net.HttpProvider;
import com.wolfram.alpha.net.URLFetcher;
import com.wolfram.alpha.visitor.Visitable;
import com.wolfram.alpha.visitor.Visitor;

// This class needs heavy synchronization because its contents can change during download of async content.
// Subpods, in contrast, are almost immutable once created, except for the image download.

public class WAPodImpl implements WAPod, Visitable, Serializable {

    private transient HttpProvider http;
    private transient File tempDir;
    
    private String title;
    private boolean error;
    private String scanner;
    private int position;
    private String id;
    private String asyncURL;
    private WAException asyncException;
    private WASubpodImpl[] subpods = WASubpodImpl.EMPTY_ARRAY;
    private WAPodStateImpl[] podstates = WAPodStateImpl.EMPTY_ARRAY;
    private WAInfoImpl[] infos = WAInfoImpl.EMPTY_ARRAY;
    private WASoundImpl[] sounds = WASoundImpl.EMPTY_ARRAY;
    
    private transient Object userData;
    
    static final WAPodImpl[] EMPTY_ARRAY = new WAPodImpl[0];

    private static final long serialVersionUID = 7267507688992616456L;

    
    WAPodImpl(Element thisElement, HttpProvider http, File tempDir) throws WAException {
        
        this.http = http;
        this.tempDir = tempDir;
        createFromDOM(thisElement);
    }
    
    
    private synchronized void createFromDOM(Element thisElement) throws WAException {
        
        // Get attribute values of <pod> element
        error = thisElement.getAttribute("error").equals("true");    
        // The only time error=true is for a pod obtained from an async URL. Normal pods are simply
        // absent if they have an error. Error pods are missing all other attributes and have no subelement content.
        if (!error) {
            title = thisElement.getAttribute("title");
            scanner = thisElement.getAttribute("scanner");
            try {
                position = Integer.parseInt(thisElement.getAttribute("position"));
            } catch (NumberFormatException e) {}
            id = thisElement.getAttribute("id");
            asyncURL = thisElement.getAttribute("async");
            if (asyncURL.equals(""))
                asyncURL = null;
           
            // subpods
            NodeList subpodElements = thisElement.getElementsByTagName("subpod");
            int numSubpods = subpodElements.getLength();
            subpods = new WASubpodImpl[numSubpods];
            for (int i = 0; i < numSubpods; i++)
                subpods[i] = new WASubpodImpl((Element) subpodElements.item(i), http, tempDir);
            
            // states
            NodeList podstatesElements = thisElement.getElementsByTagName("states");
            // Should be 0 or 1; this is the number of <states> elements, not the number
            // of <state> elements within a <states> element.
            int numStatesElements = podstatesElements.getLength();
            if (numStatesElements > 0) {
                Element statesElement = (Element) podstatesElements.item(0);
                NodeList subElements = statesElement.getChildNodes();
                // Program defensively and don't assume that every element in a <states> is a <state>
                // or <statelist>, although we have no intention of making such a change in the API output.
                int numSubElements = subElements.getLength();
                List<Node> stateAndStatelistNodes = new ArrayList<Node>(numSubElements);
                for (int i = 0; i < numSubElements; i++) {
                    Node child = subElements.item(i);
                    String name = child.getNodeName();
                    if ("state".equals(name) || "statelist".equals(name))
                        stateAndStatelistNodes.add(child);
                }
                int numStates = stateAndStatelistNodes.size();
                podstates = new WAPodStateImpl[numStates];
                for (int i = 0; i < numStates; i++)
                    podstates[i] = new WAPodStateImpl((Element) stateAndStatelistNodes.get(i));
            }
            
            // infos
            NodeList infosElements = thisElement.getElementsByTagName("infos");
            // Should be 0 or 1; this is the number of <infos> elements, not the number
            // of <info> elements within an <infos> element.
            int numInfosElements = infosElements.getLength();
            if (numInfosElements > 0) {
                Element infosElement = (Element) infosElements.item(0);
                NodeList subElements = infosElement.getChildNodes();
                // Program defensively and don't assume that every element in an <infos> is an <info>,
                // although we have no intention of making such a change in the API output.
                int numSubElements = subElements.getLength();
                List<Node> infoNodes = new ArrayList<Node>(numSubElements);
                for (int i = 0; i < numSubElements; i++) {
                    Node child = subElements.item(i);
                    String name = child.getNodeName();
                    if ("info".equals(name))
                        infoNodes.add(child);
                }
                int numInfos = infoNodes.size();
                infos = new WAInfoImpl[numInfos];
                for (int i = 0; i < numInfos; i++)
                    infos[i] = new WAInfoImpl((Element) infoNodes.get(i), http, tempDir);
            }

            NodeList soundElements = thisElement.getElementsByTagName("sounds");
            int numSoundElements = soundElements.getLength();
            if (numSoundElements > 0) {
                Element soundElement = (Element) soundElements.item(0);
                NodeList subElements = soundElement.getChildNodes();
                // Program defensively and don't assume that every element in a <sounds> is an <sound>,
                // although we have no intention of making such a change in the API output.
                int numSubElements = subElements.getLength();
                List<Node> soundNodes = new ArrayList<Node>(numSubElements);
                for (int i = 0; i < numSubElements; i++) {
                    Node child = subElements.item(i);
                    String name = child.getNodeName();
                    if ("sound".equals(name))
                        soundNodes.add(child);
                }
                int numSounds = soundNodes.size();
                sounds = new WASoundImpl[numSounds];
                for (int i = 0; i < numSounds; i++)
                    sounds[i] = new WASoundImpl((Element) soundNodes.get(i), http, tempDir);
            }

        }

    }
    
    
    //////////////////////////  WAPod interface  ///////////////////////////
    
    public synchronized String getTitle() {
        return title;
    }
    
    public synchronized boolean isError() {
        return error;
    }
    
    public synchronized int getNumSubpods() {
        return subpods.length;
    }
    
    public synchronized String getScanner() {
        return scanner;
    }
    
    public synchronized int getPosition() {
        return position;
    }
    
    public synchronized String getID() {
        return id;
    }
    
    public synchronized String getAsyncURL() {
        return asyncURL;
    }
    
    public synchronized WASubpod[] getSubpods() {
        return subpods;
    }
    
    public synchronized WAPodState[] getPodStates() {
        return podstates;
    }
    
    public synchronized WAInfo[] getInfos() {
        return infos;
    }

    public synchronized WASound[] getSounds() {
        return sounds;
    }
    
    
    public synchronized WAException getAsyncException() {
        return asyncException;
    }
    
    
    public void acquireImages() throws WAException {
        
        WASubpodImpl[] sps;
        synchronized (this) {
            sps = subpods;
        }
        for (WASubpodImpl sub : sps) {
            sub.acquireImage();
        }
    }
    
    public void finishAsync() throws WAException {
        
        // This is structured so that it holds the synchronization lock for the shortest possible period
        // of time--only when reading or setting instance state. Do not want to hold the lock during download
        // of async pods. Note that two different threads could call finishAsync() at close to the same time
        // and trigger two separate downloads. This is not really a problem. The synchronization issue we care
        // about is making sure that changes made by a thread calling finishAsync() are immediately seen by 
        // other threads reading instance state.
        
        String url;
        WAException newAsyncException = null;
        synchronized (this) {
            url = asyncURL;
            if (url != null)
                asyncException = null;
        }
        if (url != null) {
            try {
                URLFetcher fetcher = new URLFetcher(new URL(url), null, http, null);
                fetcher.fetch();
                if (fetcher.wasCancelled())  
                    throw new WAException("Download of url " + asyncURL + " was cancelled");
                if (fetcher.getException() != null)
                    throw new WAException(fetcher.getException());
                byte[] bytes = fetcher.getBytes();
                DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
                DocumentBuilder builder = factory.newDocumentBuilder();
                Document doc = builder.parse(new ByteArrayInputStream(bytes));
                synchronized (this) {
                    createFromDOM(doc.getDocumentElement());
                    asyncURL = null;
                }
                acquireImages();
            } catch (ParserConfigurationException e) {
                // Probably impossible in any realistic circumstance.
                newAsyncException = new WAException(e);
            } catch (FactoryConfigurationError e) {
                // Probably impossible in any realistic circumstance.
                newAsyncException = new WAException(e);
            } catch (IOException e) {
                newAsyncException = new WAException(e);
            } catch (SAXException e) {
                newAsyncException = new WAException(e);
            }
            if (newAsyncException != null) {
                synchronized (this) {
                    asyncException = newAsyncException;
                }
                throw newAsyncException;
            }
        }
    }

    
    // It is not essential that access to userData be synchronized here. Users could synchronize on their.
    // But the Android app is the main client for this feature, and the Android app needs synchronization, so
    // I'll put it in here.
    
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
    
    @Override
    public synchronized int hashCode() {
        
        int result = 17;
        result = 37*result + title.hashCode();
        // The only content that can change in this class is the error state and asyncException (these from
        // an async content download), the userData, and the contents of the subpods and the 
        // sounds. Both of those objects can change as their content is downloaded (e.g., image and sound URLs)
        // after initial creation of the object. Since hashCode() is currently used only for layout
        // considerations, we don't really care about sounds (their visual representation on screen is not
        // affected by whether the sound file has been downloaded or not), but for completeness and to
        // avoid possible future bugs, we include them.
        result = 37*result + (error ? 1 : 0);
        if (asyncException != null)
            result = 37*result + asyncException.hashCode();
        if (userData != null)
            result = 37*result + userData.hashCode();
        for (WASubpod subpod : subpods)
            result = 37*result + subpod.hashCode();
        for (WASound sound : sounds)
            result = 37*result + sound.hashCode();
        return result;
    }
    
    
    ///////////////////////////  Visitor interface  ////////////////////////////
    
    public void accept(Visitor v) {
        v.visit(this);
        for (WASubpod subpod : subpods)
            subpod.accept(v);
    }

}
