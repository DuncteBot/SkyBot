/*
 * Created on Dec 8, 2009
 *
 */
package com.wolfram.alpha.visitor;

import com.wolfram.alpha.WAAssumption;
import com.wolfram.alpha.WAExamplePage;
import com.wolfram.alpha.WAFutureTopic;
import com.wolfram.alpha.WAImage;
import com.wolfram.alpha.WAInfo;
import com.wolfram.alpha.WALink;
import com.wolfram.alpha.WAPlainText;
import com.wolfram.alpha.WAPod;
import com.wolfram.alpha.WAPodState;
import com.wolfram.alpha.WAQueryResult;
import com.wolfram.alpha.WAReinterpretWarning;
import com.wolfram.alpha.WARelatedExample;
import com.wolfram.alpha.WARelatedLink;
import com.wolfram.alpha.WASound;
import com.wolfram.alpha.WASourceInfo;
import com.wolfram.alpha.WASubpod;
import com.wolfram.alpha.WAUnits;
import com.wolfram.alpha.WAWarning;


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
