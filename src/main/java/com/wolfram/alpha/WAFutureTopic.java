/*
 * Created on Sep 19, 2010
 *
 */
package com.wolfram.alpha;

import com.wolfram.alpha.visitor.Visitable;


public interface WAFutureTopic extends Visitable {

    String getTopic();
    String getMessage();
}
