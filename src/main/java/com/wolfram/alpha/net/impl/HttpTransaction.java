/*
 * Created on Aug 19, 2006
 *
 */
package com.wolfram.alpha.net.impl;

import java.io.IOException;
import java.io.InputStream;

import com.wolfram.alpha.net.WAHttpException;


public interface HttpTransaction {

    void execute() throws WAHttpException;
    
    void release();
    
    long getContentLength();

    String getCharSet() throws IOException;

    String[][] getResponseHeaders() throws IOException;
    
    InputStream getResponseStream() throws IOException;
    
    String getResponseString() throws IOException;

    // IS this needed?
    void setNoRetry();

    void abort();

}
