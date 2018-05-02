/*
 * Created on Dec 9, 2009
 *
 */
package com.wolfram.alpha;

import com.wolfram.alpha.visitor.Visitable;

import java.io.File;


public interface WAImage extends Visitable {

    public static final int FORMAT_UNKNOWN = 0;
    public static final int FORMAT_GIF = 1;
    public static final int FORMAT_PNG = 2;

    String getURL();

    String getAlt();

    String getTitle();

    int getFormat();

    int[] getDimensions();

    File getFile();

    void acquireImage();
}
