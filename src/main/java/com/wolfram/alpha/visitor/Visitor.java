/*
 * Created on Dec 8, 2009
 *
 */
package com.wolfram.alpha.visitor;

import com.wolfram.alpha.*;


public interface Visitor {

    void visit(WAQueryResult obj);

    void visit(WAPod obj);

    void visit(WASubpod obj);

    void visit(WAAssumption obj);

    void visit(WAWarning obj);

    void visit(WAInfo obj);

    void visit(WAPodState obj);

    void visit(WARelatedLink obj);

    void visit(WARelatedExample obj);

    void visit(WASourceInfo obj);

    void visit(WAFutureTopic obj);

    void visit(WAExamplePage obj);

    void visit(WALink obj);

    void visit(WAReinterpretWarning obj);

    void visit(WAUnits obj);

    // Content types
    void visit(WAPlainText obj);

    void visit(WAImage obj);

    void visit(WASound obj);

}
