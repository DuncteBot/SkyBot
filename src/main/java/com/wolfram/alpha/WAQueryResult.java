/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017 - 2018  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published
 * by the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

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
    
    Object getUserData();
    
    void setUserData(Object obj);
    
    void mergeRecalculateResult(WAQueryResult recalcQueryResult);
    
    void mergePodstateResult(WAQueryResult podstateQueryResult);
    
}
