/*
 * Created on Feb 8, 2010
 *
 */
package com.wolfram.alpha;

import com.wolfram.alpha.visitor.Visitable;


public interface WAUnits extends Visitable {

    String[] getLongNames();
    String[] getShortNames();
    WAImage getImage();
}
