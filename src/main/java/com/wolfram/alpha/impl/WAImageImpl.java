/*
 * Created on Dec 9, 2009
 *
 */
package com.wolfram.alpha.impl;


import java.io.File;
import java.io.Serializable;
import java.net.URL;

import org.w3c.dom.Element;

import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAImage;
import com.wolfram.alpha.net.HttpProvider;
import com.wolfram.alpha.net.URLFetcher;
import com.wolfram.alpha.visitor.Visitable;
import com.wolfram.alpha.visitor.Visitor;


public class WAImageImpl implements WAImage, Visitable, Serializable {

    private String url;
    private String alt;
    private String title;
    private int format;
    private int[] dimensions;
    
    private volatile File file;
    private volatile int cachedHashCode = 0;
    private volatile boolean imageAcquired = false;
    
    private transient HttpProvider http;
    private transient File tempDir;
    
    private static final long serialVersionUID = 8073460264016968534L;

    
    WAImageImpl(Element thisElement, HttpProvider http, File tempDir) throws WAException {
        
        this(thisElement.getAttribute("src"), http, tempDir);
        
        alt = thisElement.getAttribute("alt");
        title = thisElement.getAttribute("title");
        try {
            int width = Integer.parseInt(thisElement.getAttribute("width"));
            int height = Integer.parseInt(thisElement.getAttribute("height"));
            dimensions = new int[]{width, height};
        } catch (NumberFormatException e) {}
    }

    
    // This ctor for use when not being created from an <img> element, like for the thumbnail images in WARelatedExample.
    WAImageImpl(String url, HttpProvider http, File tempDir) throws WAException {
        
        this.http = http;
        this.tempDir = tempDir;
        this.url = url;
        
        format = FORMAT_UNKNOWN;
        // Relying on image URLs having MSPStoreType=image/xxxx
        int index = url.lastIndexOf("MSPStoreType=image/");
        if (index > 0) {
            String fmtString = url.substring(index + 19, index + 22);
            if (fmtString.equals("gif"))
                format = FORMAT_GIF;
            else if (fmtString.equals("png"))
                format = FORMAT_PNG;
        } else if (url.endsWith(".gif")) {
            format = FORMAT_GIF;
        } else if (url.endsWith(".png")) {
            format = FORMAT_PNG;
        }
    }
    
    
    ////////////////////  WAImage interface  //////////////////////////////
    
    public String getURL() {
        return url;
    }
    
    public String getAlt() {
        return alt;
    }
    
    public String getTitle() {
        return title;
    }
    
    public int getFormat() {
        return format;
    }
    
    public int[] getDimensions() {
        return dimensions;
    }
    
    public synchronized File getFile() {
        return file;
    }
    
    public void acquireImage() {
        
        // If this is a deserialized instance, http will  be null. Such instances are "dead"; they can
        // never retrieve new content from the web.
        if (!imageAcquired && http != null) {
            try {
                String suffix;
                if (format == WAImage.FORMAT_GIF)
                    suffix = ".gif";
                else if (format == WAImage.FORMAT_PNG)
                    suffix = ".png";
                else 
                    suffix = ".tmp";
                String outFile = File.createTempFile("WAImage", suffix, tempDir).getAbsolutePath();
                URLFetcher fetcher = new URLFetcher(new URL(url), outFile, http, null);
                fetcher.fetch();
                setFile(fetcher.getFile());
            } catch (Exception e) {
                // TODO: report?
            }
            imageAcquired = true;
        }
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
        
        if (cachedHashCode != 0)
            return cachedHashCode;
        
        int result = 17;
        result = 37*result + title.hashCode();
        if (file != null)
            result = 37*result + file.hashCode();
        cachedHashCode = result;
        return result;
    }
    
    
    /////////////////////////////////////////////
    
    // Download is done higher up, then we just stuff the file in.
    synchronized void setFile(File file) {
        this.file = file;
        cachedHashCode = 0;  // Force recompute of hash now that content has changed.
    }
    
    ///////////////////////////  Visitor interface  ////////////////////////////
    
    public void accept(Visitor v) {
        v.visit(this);
    }

}
