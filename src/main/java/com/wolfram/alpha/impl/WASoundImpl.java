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
import com.wolfram.alpha.WASound;
import com.wolfram.alpha.net.HttpProvider;
import com.wolfram.alpha.net.URLFetcher;
import com.wolfram.alpha.visitor.Visitable;
import com.wolfram.alpha.visitor.Visitor;

// This class is written exactly like WAImageImpl, as if sound downloads will be done in advance and stored
// as files to be passed to the media player. The player also supports streaming, though, and that would be
// a better choice. At the moment, there appear to be problems with streaming. I have encountered failures in MEdiaPlayer.prepare(),
// and although these might be problems with our servers not being set up to deliver progressive downloads,
// the internet is filled with people claiming problems with streaming on various devices and Android versions.
// Even if we switch to a streaming mode, it makes sense for this class to support download-to-file mode. We can
// avoid using the feature if we decide streaming is working.

public class WASoundImpl implements WASound, Visitable, Serializable {

    private String url;
    private String format;
    
    private volatile File file;
    private volatile int cachedHashCode = 0;
    private volatile boolean soundAcquired = false;
    
    private transient HttpProvider http;
    private transient File tempDir;
   
    static final WASoundImpl[] EMPTY_ARRAY = new WASoundImpl[0];

    private static final long serialVersionUID = 3863860206159745210L;

    
    WASoundImpl(Element thisElement, HttpProvider http, File tempDir) throws WAException {
        
        url = thisElement.getAttribute("url");
        format = thisElement.getAttribute("type");
        if (format.equals("audio/x-wav"))
            format = "WAV";
        else if (format.equals("audio/midi"))
            format = "MIDI";
        this.http = http;
        this.tempDir = tempDir;
    }

    
    ////////////////////  WASound interface  //////////////////////////////
    
    public String getURL() {
        return url;
    }
    
    public String getFormat() {
        return format;
    }
    
    public synchronized File getFile() {
        return file;
    }
    
    public void acquireSound() {
        
        // If this is a deserialized instance, http will  be null. Such instances are "dead"; they can
        // never retrieve new content from the web.
        // The only synchronization needed here is that soundAcquired is volatile.
        if (!soundAcquired && http != null) {
            if (!url.equals("")) {
                try {
                    String suffix;
                    if (format == "WAV")
                        suffix = ".wav";
                    else if (format == "MIDI")
                        suffix = ".mid";
                    else 
                        suffix = ".tmp";
                    String outFile = File.createTempFile("WASound", suffix, tempDir).getAbsolutePath();
                    URLFetcher fetcher = new URLFetcher(new URL(url), outFile, http, null);
                    fetcher.fetch();
                    setFile(fetcher.getFile());
                } catch (Exception e) {
                    // TODO: report?
                }
                
            }
            soundAcquired = true;
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
        result = 37*result + url.hashCode();
        if (file != null)
            result = 37*result + file.hashCode();
        cachedHashCode = result;
        return result;
    }
    
    
    ////////////////////////////////////////////
    
    private synchronized void setFile(File file) {
        this.file = file;
        cachedHashCode = 0;  // Force recompute of hash now that content has changed.
    }
    
    ///////////////////////////  Visitor interface  ////////////////////////////
    
    public void accept(Visitor v) {
        v.visit(this);
    }

}
