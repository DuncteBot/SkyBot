/*
 * Created on Nov 8, 2009
 *
 */
package com.wolfram.alpha.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
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

import com.wolfram.alpha.WAAssumption;
import com.wolfram.alpha.WAExamplePage;
import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAFutureTopic;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryResult;
import com.wolfram.alpha.WARelatedExample;
import com.wolfram.alpha.WARelatedLink;
import com.wolfram.alpha.WASourceInfo;
import com.wolfram.alpha.WAWarning;
import com.wolfram.alpha.net.HttpProvider;
import com.wolfram.alpha.visitor.Visitable;
import com.wolfram.alpha.visitor.Visitor;


public class WAQueryResultImpl implements WAQueryResult, Visitable, Serializable {
    
    private transient WAQuery query;
    private byte[] bytes;
    private transient File tempDir;
    private transient HttpProvider http;
    
    private transient Object userData;
    
    //States
    private boolean imagesAcquired = false;
    
    // Attributes
    private boolean success;
    private boolean error;
    private int errorCode = 0;
    private String errorMessage;
    private String[] dataTypes = EMPTY_STRING_ARRAY;
    private String[] timedoutScanners = EMPTY_STRING_ARRAY;
    private double timing;
    private double parseTiming;
    private String version;
    private String recalcURL = "";
    
    // Subelements
    private WAPodImpl[] pods = WAPodImpl.EMPTY_ARRAY;
    private WAAssumptionImpl[] assumptions = WAAssumptionImpl.EMPTY_ARRAY;
    private WAWarningImpl[] warnings = WAWarningImpl.EMPTY_ARRAY;
    private WARelatedLinkImpl[] relatedLinks = WARelatedLinkImpl.EMPTY_ARRAY;
    private WASourceInfoImpl[] sources = WASourceInfoImpl.EMPTY_ARRAY;
    private String[] didYouMeans = EMPTY_STRING_ARRAY;
    private WARelatedExampleImpl[] relatedExamples = WARelatedExampleImpl.EMPTY_ARRAY;
    private String[] languageMessage = EMPTY_STRING_ARRAY;
    private String[] splatTips = EMPTY_STRING_ARRAY;
    private WAFutureTopic futureTopic;
    private WAExamplePage examplePage;
    
    
    private static final String[] EMPTY_STRING_ARRAY = new String[]{};

    private static final long serialVersionUID = 6045494030310944812L;

    
    // TODO: Provide soem way to release the bytes array. The only reason to keep it around is for
    // the getXML() method, but many users will never want to call that. Perhaps the ctor can take an
    // arg that says whether to store the byte array or not.
    
   
    // I _could_ lazily parse the bytes, but I think it's better to force parsing errors to
    // occur in a well-defined place, not potentially from every accessor.
    public WAQueryResultImpl(WAQuery query, byte[] bytes, HttpProvider http, File tempDir) throws WAException {
        
        this.query = query;
        this.http = http;
        this.bytes = bytes;
        this.tempDir = tempDir;
        try {
            /*** OLD SAX style, abandoned
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser parser = factory.newSAXParser();
            AlphaSAXHandler handler = new AlphaSAXHandler(this);
            parser.parse(new ByteArrayInputStream(bytes), handler);
            ***/
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(new ByteArrayInputStream(bytes));
            
            // Now build my object tree.
            //NodeList queryResultNode = doc.getElementsByTagName("queryresult");
            createFromDOM(doc.getDocumentElement());
            
        } catch (ParserConfigurationException e) {
            // Probably impossible in any realistic circumstance.
            throw new WAException(e);
        } catch (FactoryConfigurationError e) {
            // Probably impossible in any realistic circumstance.
            throw new WAException(e);
        } catch (IOException e) {
            // Don't think there can be an IOException on a ByteArrayInputStream.
            throw new WAException(e);
        } catch (SAXException e) {
            throw new WAException(e);
        }
    }
    

    public boolean isSuccess() {
        return success;
    }
    
    public boolean isError() {
        return error;
    }
    
    public int getErrorCode() {
        return errorCode;
    }
    
    public String getErrorMessage() {
        return errorMessage;
    }

    
    // Can be null (if this is a recalc query, performed by WAEngine.performReclaculate(), or a deserialized instance).
    public WAQuery getQuery() {
        return query;
    }
    
    public int getNumPods() {
        return pods.length;
    }
    
    public String[] getDataTypes() {
        return dataTypes;
    }
    
    public String[] getTimedoutScanners() {
        return timedoutScanners;
    }
    
    public double getTiming() {
        return timing;
    }
    
    public double getParseTiming() {
        return parseTiming;
    }
    
    public String getAPIVersion() {
        return version;
    }
    
    public String getRecalculateURL() {
        return recalcURL;
    }
    
    // Because the pods array is modified in the merge methods, which are called on threads, we must ensure
    // that access to the array is synchronized.
    public synchronized WAPod[] getPods() {
        return pods;
    }
    
    public WAAssumption[] getAssumptions() {
        return assumptions;
    }
    
    public WAWarning[] getWarnings() {
        return warnings;
    }
    
    public String[] getTips() {
        return splatTips;
    }
    
    public WARelatedLink[] getRelatedLinks() {
        return relatedLinks;
    }
    
    public WASourceInfo[] getSources() {
        return sources;
    }

    public String[] getDidYouMeans() {
        return didYouMeans;
    }

    public WARelatedExample[] getRelatedExamples() {
        return relatedExamples;
    }
    
    public String[] getLanguageMessage() {
        return languageMessage;
    }

    public WAFutureTopic getFutureTopic() {
        return futureTopic;
    }
    
    public  WAExamplePage getExamplePage() {
        return examplePage;
    }
    
    // This only gets pod images, not <info> or <sidebarlinks> or others.
    public synchronized void acquireImages() {
        
        if (!imagesAcquired) {
            for (WAPodImpl pod : pods) {
                try {
                    pod.acquireImages();
                } catch (WAException e) {
                    // What to do here? Need to finish getting all even if there is an exception.
                }
            }
            imagesAcquired = true;
        }
    }
    
    
    public void finishAsync() {
        
        acquireImages();
        List<Thread> runningThreads = new ArrayList<Thread>(pods.length);
        WAPod[] pods = getPods();
        for (int i = 0; i < pods.length; i++) {
            final WAPod pod = pods[i];
            if (pod.getAsyncURL() != null) {
                Thread t = new Thread(new Runnable() {
                    public void run() {
                        try {
                            pod.finishAsync();
                        } catch (WAException e) {
                            // TODO: What here?
                        }
                    }                     
                });
                t.start();
                runningThreads.add(t);
            }
        }
        for (Thread t : runningThreads) {
            try {
                t.join();
            } catch (InterruptedException e) {
                // TODO: What here?
            }
        }
    }
    
    
    // These are used for updating a WAQueryResult with info from another result, either a podstate chnage or a recalculate.
    // They don't keep the object in a totally consistent state. For example, calling getXML() after a merge will not
    // give you updated XML. These are just convenience functions for clients who don't want to know the correct way
    // to merge in these new types of results. The "important" getters, like getPods(), work as desired after a merge.
    
    public synchronized void mergeRecalculateResult(WAQueryResult recalcQueryResult) {
        
        // TODO: Merge in new <sources> or other elements that are relevant in a recalc.
        WAPod[] recalcPods = recalcQueryResult.getPods();
        WAPod[] oldPods = getPods();
        if (recalcPods.length > 0) {
            WAPodImpl[] allPods = new WAPodImpl[oldPods.length + recalcPods.length];
            System.arraycopy(oldPods, 0, allPods, 0, oldPods.length);
            System.arraycopy(recalcPods, 0, allPods, oldPods.length, recalcPods.length);
            pods = allPods;
        }
    }

    public synchronized void mergePodstateResult(WAQueryResult podstateQueryResult) {
        
        WAPod[] newPods = podstateQueryResult.getPods();
        // Should always be 1. If not, just skip the merge.
        if (newPods.length == 1) {
            WAPod newPod = newPods[0];
            String podTitle = newPod.getTitle();
            WAPod[] oldPods = getPods();
            for (int i = 0; i < oldPods.length; i++) {
                if (podTitle.equals(oldPods[i].getTitle())) {
                    oldPods[i] = newPod;
                    break;
                }
            }
        }
    }


    public String getXML() {
        try {
            return new String(bytes, "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            // Not possible.
            return null;
        }
    }


    public void release() {
        // Could do this like acquireImages, via explicit interface methods, or by a visitor.
        // Probably via visitor, since it doesnt seem likely that a user would want to release
        // a specific pod. Probably just want to throw away everything when done with a query.
    }
    
    
    public void setUserData(Object obj) {
        userData = obj;
    }
    
    public Object getUserData() {
        return userData;
    }

    
    ///////////////////////////  createFromDOM  ///////////////////////////////
    
    private void createFromDOM(Element thisElement) throws WAException {
                
        // Get attribute values of <queryresult> element
        success = thisElement.getAttribute("success").equals("true");
        error = thisElement.getAttribute("error").equals("true");
        
        if (error) {
            // Error case is missing many attributes and has different subelement content.
            // Format of error element:  <error><code>42</code><msg>blah blah</msg></code>.
            NodeList children = thisElement.getElementsByTagName("error");
            // Should have length 1.
            if (children.getLength() > 0) {
                Element errorElement = (Element) children.item(0);
                children = errorElement.getElementsByTagName("code");
                if (children.getLength() > 0) {
                    try {
                        errorCode = Integer.parseInt(children.item(0).getFirstChild().getNodeValue());
                        children.item(0).getFirstChild();
                    } catch (NumberFormatException e) {
                        // Do nothing in this unlikely event. errorCode remains its default value of 0 ("no error").
                    }
                }
                children = errorElement.getElementsByTagName("msg");
                if (children.getLength() > 0) {
                    errorMessage = children.item(0).getFirstChild().getNodeValue();
                }
            }
        } else {
            try {
                timing = Double.parseDouble(thisElement.getAttribute("timing"));
            } catch (NumberFormatException e) {}
            try {
                parseTiming = Double.parseDouble(thisElement.getAttribute("timing"));
            } catch (NumberFormatException e) {}
            version = thisElement.getAttribute("version");
            dataTypes = thisElement.getAttribute("datatypes").split(",");
            timedoutScanners = thisElement.getAttribute("timedout").split(",");
            recalcURL = thisElement.getAttribute("recalculate");
            
            NodeList podElements = thisElement.getElementsByTagName("pod");
            int numPods = podElements.getLength();
            pods = new WAPodImpl[numPods];
            for (int i = 0; i < numPods; i++)
                pods[i] = new WAPodImpl((Element) podElements.item(i), http, tempDir);
            
            NodeList assumptionElements = thisElement.getElementsByTagName("assumption");
            int numAssums = assumptionElements.getLength();
            assumptions = new WAAssumptionImpl[numAssums];
            for (int i = 0; i < numAssums; i++)
                assumptions[i] = new WAAssumptionImpl((Element) assumptionElements.item(i));
            
            NodeList warningsElements = thisElement.getElementsByTagName("warnings");
            // There should be 0 or 1. This is the <warnings> element, not the elements for each
            // individual warning type (<spellcheck>, <delimiters> etc.)
            if (warningsElements.getLength() > 0) {
                Element warningsElement = (Element) warningsElements.item(0);
                NodeList children = warningsElement.getChildNodes();
                int numNodes = children.getLength();
                ArrayList<Element> warningElements = new ArrayList<Element>();
                for (int i = 0; i < numNodes; i++) {
                    Node child = children.item(i);
                    if (child instanceof Element)
                        warningElements.add((Element) child);
                }
                int numWarnings = warningElements.size();
                warnings = new WAWarningImpl[numWarnings];
                for (int i = 0; i < numWarnings; i++) {
                    Element warningElement = warningElements.get(i);
                    if ("reinterpret".equals(warningElement.getNodeName()))
                        warnings[i] = new WAReinterpretWarningImpl(warningElement);
                    else
                        warnings[i] = new WAWarningImpl(warningElement);
                }
            }
            
            NodeList tipsElements = thisElement.getElementsByTagName("tips");
            // There should be 0 or 1. This is the <tips> element, not the elements for each
            // individual <tip>.
            if (tipsElements.getLength() > 0) {
                Element tipsElement = (Element) tipsElements.item(0);
                NodeList children = tipsElement.getChildNodes();
                int numNodes = children.getLength();
                ArrayList<Element> tipElements = new ArrayList<Element>();
                for (int i = 0; i < numNodes; i++) {
                    Node child = children.item(i);
                    if (child instanceof Element)
                        tipElements.add((Element) child);
                }
                int numTips = tipElements.size();
                splatTips = new String[numTips];
                for (int i = 0; i < numTips; i++)
                    splatTips[i] = new String(tipElements.get(i).getAttribute("text"));
            }
            
            NodeList relatedLinkElements = thisElement.getElementsByTagName("sidebarlink");
            int numRelateds = relatedLinkElements.getLength();
            relatedLinks = new WARelatedLinkImpl[numRelateds];
            for (int i = 0; i < numRelateds; i++)
                relatedLinks[i] = new WARelatedLinkImpl((Element) relatedLinkElements.item(i), http, tempDir);            

            NodeList didYouMeanElements = thisElement.getElementsByTagName("didyoumean");
            int numDidYouMeans = didYouMeanElements.getLength();
            if (numDidYouMeans > 0) {
                didYouMeans = new String[numDidYouMeans];
                for (int i = 0; i < numDidYouMeans; i++) {
                    Node textNode = didYouMeanElements.item(i).getFirstChild();
                    didYouMeans[i] = textNode.getNodeValue(); 
                }
            }
            
            NodeList relatedExampleElements = thisElement.getElementsByTagName("relatedexample");
            int numRelatedExamples = relatedExampleElements.getLength();
            if (numRelatedExamples > 0) {
                relatedExamples = new WARelatedExampleImpl[numRelatedExamples];
                for (int i = 0; i < numRelatedExamples; i++)
                    relatedExamples[i] = new WARelatedExampleImpl((Element) relatedExampleElements.item(i), http, tempDir);             
            }

            NodeList languageMsgElements = thisElement.getElementsByTagName("languagemsg");
            int numLanugageMsgs = languageMsgElements.getLength();
            // Should be 0 or 1.
            if (numLanugageMsgs > 0) {
                Element languageMsgElement = (Element) languageMsgElements.item(0);
                String english = languageMsgElement.getAttribute("english");
                String foreign = languageMsgElement.getAttribute("other");
                languageMessage = new String[]{english, foreign};
            }

            NodeList sourcesElements = thisElement.getElementsByTagName("sources");
            // There should be 0 or 1. This is the <sources> element, not the elements for each
            // individual <source>.)
            if (sourcesElements.getLength() > 0) {
                Element sourcesElement = (Element) sourcesElements.item(0);
                NodeList children = sourcesElement.getChildNodes();
                int numNodes = children.getLength();
                ArrayList<Element> sourceElements = new ArrayList<Element>();
                for (int i = 0; i < numNodes; i++) {
                    Node child = children.item(i);
                    if (child instanceof Element)
                        sourceElements.add((Element) child);
                }
                int numSources = sourceElements.size();
                sources = new WASourceInfoImpl[numSources];
                for (int i = 0; i < numSources; i++)
                    sources[i] = new WASourceInfoImpl((Element) sourceElements.get(i));            
            }
            
            NodeList futureTopicElements = thisElement.getElementsByTagName("futuretopic");
            // There should be 0 or 1.
            if (futureTopicElements.getLength() > 0)
                futureTopic = new WAFutureTopicImpl((Element) futureTopicElements.item(0));            
            
            NodeList examplePageElements = thisElement.getElementsByTagName("examplepage");
            // There should be 0 or 1.
            if (examplePageElements.getLength() > 0)
                examplePage = new WAExamplePageImpl((Element) examplePageElements.item(0));            
        }
    }
    
    
    ///////////////////////////  Visitor interface  ////////////////////////////
    
    public void accept(Visitor v) {
        
        v.visit(this);
        for (WAPod pod : pods) {
            pod.accept(v);
        }
    }


}
