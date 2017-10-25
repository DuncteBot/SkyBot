/*
 * Created on Nov 8, 2009
 *
 */
package com.wolfram.alpha;

import com.wolfram.alpha.visitor.Visitable;


public interface WAPodState extends Visitable {

    String[] getNames();
    String[] getInputs();
    int getCurrentIndex();
    // Returns modified copy; not a mutator.
    WAPodState setCurrentIndex(int index);
    // Not called by users.
    long getID();
}
