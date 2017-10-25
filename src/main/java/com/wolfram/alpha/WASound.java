/*
 * Created on Dec 9, 2009
 *
 */
package com.wolfram.alpha;

import java.io.File;

import com.wolfram.alpha.visitor.Visitable;


public interface WASound extends Visitable {
    
    String getURL();
    
    String getFormat();
    
    File getFile();
    
    void acquireSound();

}
