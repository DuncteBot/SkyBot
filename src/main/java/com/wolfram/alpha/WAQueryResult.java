/*
 * Created on Nov 8, 2009
 *
 */
package com.wolfram.alpha;

import com.wolfram.alpha.visitor.Visitable;


public interface WAQueryResult extends Visitable {

    // Can be null (if this is a recalc query, performed by WAEngine.performReclaculate()).
    WAQuery getQuery();
    
    boolean isSuccess();
    
    boolean isError();
    
    int getErrorCode();
    
    String getErrorMessage();
    
    int getNumPods();
    
    String[] getDataTypes();
    
    String[] getTimedoutScanners();
    
    double getTiming();
    
    double getParseTiming();
    
    String getRecalculateURL();
    
    String getAPIVersion();
    
    WAPod[] getPods();
    
    WAAssumption[] getAssumptions();
    
    WAWarning[] getWarnings();
    
    String[] getTips();
    
    WARelatedLink[] getRelatedLinks();
    
    String[] getDidYouMeans();
    
    WARelatedExample[] getRelatedExamples();
    
    WASourceInfo[] getSources();
    
    String[] getLanguageMessage();

    WAFutureTopic getFutureTopic();

    WAExamplePage getExamplePage();

    void acquireImages();
    
    void finishAsync();
    
    void release();
    
    String getXML();
    
    void setUserData(Object obj);
    
    Object getUserData();
    
    void mergeRecalculateResult(WAQueryResult recalcQueryResult);

    void mergePodstateResult(WAQueryResult podstateQueryResult);

}
