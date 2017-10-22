/*
 * Created on Feb 8, 2010
 *
 */
package com.wolfram.alpha;

import com.wolfram.alpha.visitor.Visitable;


public interface WALink extends Visitable {

    String getURL();
    String getText();
    String getTitle();
}
