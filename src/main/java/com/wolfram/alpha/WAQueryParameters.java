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
 * Created on Dec 4, 2009
 *
 */
package com.wolfram.alpha;

import java.util.List;


public interface WAQueryParameters {
    
    String getInput();
    
    void setInput(String input);
    
    String[] getFormats();
    
    void addFormat(String format);
    
    ////////////////  Timeouts and async  //////////////////
    
    double getScanTimeout();
    
    void setScanTimeout(double timeout);
    
    double getPodTimeout();
    
    void setPodTimeout(double timeout);
    
    double getFormatTimeout();
    
    void setFormatTimeout(double timeout);
    
    // Values < 0 mean async off, 0 means async=true (default timeout), > 0 means async is that value.
    double getAsync();
    
    void setAsync(double async);
    
    ////////////  Location-related properties  /////////////
    
    String getIP();
    
    void setIP(String ip);
    
    double[] getLatLong();
    
    void setLatLong(String latlong);
    
    void setLatitude(double latitude);
    
    void setLongitude(double longitude);
    
    String getLocation();
    
    void setLocation(String loc);
    
    Boolean isMetric();
    
    void setMetric(Boolean metric);
    
    String getCurrency();
    
    void setCurrency(String currency);
    
    String getCountryCode();
    
    void setCountryCode(String code);
    
    Boolean isAllowTranslation();
    
    void setAllowTranslation(Boolean allow);
    
    ///////////////////  Widths  /////////////////////
    
    int getWidth();
    
    void setWidth(int width);
    
    int getMaxWidth();
    
    void setMaxWidth(int width);
    
    int getPlotWidth();
    
    void setPlotWidth(int width);
    
    double getMagnification();
    
    void setMagnification(double mag);
    
    ////////////////  Pod selection  //////////////////
    
    String[] getPodTitles();
    
    void addPodTitle(String podtitle);
    
    void clearPodTitles();
    
    int[] getPodIndexes();
    
    void addPodIndex(int podindex);
    
    void clearPodIndexes();
    
    String[] getPodScanners();
    
    void addPodScanner(String podscanner);
    
    void clearPodScanners();
    
    String[] getIncludePodIDs();
    
    void addIncludePodID(String podid);
    
    void clearIncludePodIDs();
    
    String[] getExcludePodIDs();
    
    void addExcludePodID(String podid);
    
    void clearExcludePodIDs();
    
    ///////////  Assumptions and podstates  ////////////
    
    String[] getAssumptions();
    
    void addAssumption(String assumption);
    
    void clearAssumptions();
    
    WAPodState[] getPodStates();
    
    // This doesn't really work for <statelist>-type states. The query will work, but if you modify an existing
    // query, you won't get the new state replacing the old state. Instead, you get chaining, so the old replacement happens
    // followed by the new replacement. Probably still works, but it's inefficient. (I'm not sure if the order is guaranteed,
    // so the old state might still be in effect.)
    void addPodState(String podstate);
    
    // This one is for <statelist> types, but not intended to be called by users, since they don't know what the id is.
    void addPodState(String podstate, long id);
    
    void addPodState(WAPodState podstate);
    
    void clearPodStates();
    
    //////////////////////  Misc  ////////////////////
    
    boolean isRelatedLinks();
    
    void setRelatedLinks(boolean include);
    
    boolean isReinterpret();
    
    void setReinterpret(boolean reinterpret);
    
    void setSignature(String sig);
    
    List<String[]> getExtraParams();
    
    List<String[]> getParameters();
    
    //////////////////  From URL  ////////////////
    
    // Take values from an API-style URL.
    void fillFromURL(String url);
    
    String toWebsiteURL();
    
}
