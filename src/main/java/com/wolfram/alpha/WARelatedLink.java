/*
 * Created on Feb 24, 2010
 *
 */
package com.wolfram.alpha;

import com.wolfram.alpha.visitor.Visitable;


public interface WARelatedLink extends Visitable {
    
    String getURL();
    String getText();
    String getSource();
    String getExcerpt();
    WAImage getImage();

}
