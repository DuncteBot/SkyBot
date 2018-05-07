/*
 * Created on Dec 9, 2009
 *
 */
package com.wolfram.alpha;

import com.wolfram.alpha.visitor.Visitable;

import java.io.File;


public interface WASound extends Visitable {

    String getURL();

    String getFormat();

    File getFile();

    void acquireSound();

}
