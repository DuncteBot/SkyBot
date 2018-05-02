/*
 * Created on Dec 4, 2009
 *
 */
package com.wolfram.alpha.impl;

import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.ListIterator;

import com.wolfram.alpha.WAPodState;
import com.wolfram.alpha.WAQueryParameters;


public class WAQueryParametersImpl implements WAQueryParameters, Serializable {

    protected String input;
    protected String appid;
        
    protected List<String> formats = new ArrayList<String>(5);
    
    protected double async = -1;
    protected double scanTimeout = -1;
    protected double podTimeout = -1;
    protected double formatTimeout = -1;

    protected int width = -1;
    protected int maxWidth = -1;
    protected int plotWidth = -1;
    protected double magnification = 1.0;
    
    protected String ip;
    protected String location;
    // Objects, not double, so that they can have null == unassigned
    protected Double latitude;
    protected Double longitude;
    
    protected Boolean metric;
    protected String currency;
    protected String countryCode;
    
    protected Boolean allowTranslation;
    
    protected boolean includeRelatedLinks;
    protected boolean allowReinterpret = true;
    protected String signature;
    
    protected List<String[]> extraParams = new ArrayList<String[]>(1);
    
    protected List<String> podTitles = new ArrayList<String>(5);
    protected List<String> podScanners = new ArrayList<String>(5); 
    protected List<Integer> podIndices = new ArrayList<Integer>(5);
    protected List<String> includePodIDs = new ArrayList<String>(5);
    protected List<String> excludePodIDs = new ArrayList<String>(5);
    
    protected List<WAPodState> podStates = new ArrayList<WAPodState>(5);
    protected List<String> assumptions = new ArrayList<String>(5);
   
    private static final long serialVersionUID = 2222070970297271641L;

    
    /********************************  Constructor  ********************************/
    
    public WAQueryParametersImpl() {}
    
    // Copy constructor.
    // When a WAEngine creates a query, it wants to pass in its WAQueryParameters. But it can't pass a
    // reference to its own object because the two versions need to be isolated from changes in the other.
    // Thus we provide a copy constructor that clones the necessary parts.
    public WAQueryParametersImpl(WAQueryParameters orig) {
        
        this.input = orig.getInput();
        this.async = orig.getAsync();
        this.formats.addAll(Arrays.asList(orig.getFormats()));
        this.scanTimeout = orig.getScanTimeout();
        this.podTimeout = orig.getPodTimeout();
        this.formatTimeout = orig.getFormatTimeout();
        this.width = orig.getWidth();
        this.maxWidth = orig.getMaxWidth();
        this.plotWidth = orig.getPlotWidth();
        this.magnification = orig.getMagnification();
        this.ip = orig.getIP();
        this.location = orig.getLocation();
        double[] latlong = orig.getLatLong();
        this.latitude = latlong != null ? latlong[0] : null;
        this.longitude = latlong != null ? latlong[1] : null;
        this.metric = orig.isMetric();
        this.currency = orig.getCurrency();
        this.countryCode = orig.getCountryCode();
        this.allowTranslation = orig.isAllowTranslation();
        this.podTitles.addAll(Arrays.asList(orig.getPodTitles()));
        this.podScanners.addAll(Arrays.asList(orig.getPodScanners()));
        for (int index : orig.getPodIndexes())
            this.podIndices.add(new Integer(index));
        this.includePodIDs.addAll(Arrays.asList(orig.getIncludePodIDs()));
        this.excludePodIDs.addAll(Arrays.asList(orig.getExcludePodIDs()));
        this.podStates.addAll(Arrays.asList(orig.getPodStates()));
        this.assumptions.addAll(Arrays.asList(orig.getAssumptions()));
        this.includeRelatedLinks = orig.isRelatedLinks();
        this.allowReinterpret = orig.isReinterpret();
        // NOTE that we do no copying of the String[2] arrays. That means if you modify the contents
        // of one of the arrays, the change can be seen by other WAQueryParameters objects that were
        // created by copying the original. Therefore, users of the (obscure) extraParams() feature
        // should take care never to alter one of these pairs in place; rather, create a new one and
        // replace the old one.
        this.extraParams.addAll(orig.getExtraParams());
        
        // Don't copy signature. It cannot be set in advance.
    }
    
        
    /**********************************  Methods  **********************************/    
        
    public String getInput() {
        return input;
    }
    
    public void setInput(String input) {
        this.input = input;
    }
    

    public String[] getFormats() {
        return formats.toArray(new String[formats.size()]);
    }
    
    public void addFormat(String format) {
        format = format.toLowerCase();
        if (!formats.contains(format))
            formats.add(format);
    }
    
    
    ////////////////  Timeouts and async  //////////////////
    
    public double getScanTimeout() {
        return scanTimeout;
    }
    
    public void setScanTimeout(double timeout) {
        this.scanTimeout = timeout;
    }
    
    
    public double getPodTimeout() {
        return podTimeout;
    }
    
    public void setPodTimeout(double timeout) {
        this.podTimeout = timeout;
    }
    
    
    public double getFormatTimeout() {
        return formatTimeout;
    }
    
    public void setFormatTimeout(double timeout) {
        this.formatTimeout = timeout;
    }
    
    
    public double getAsync() {
        return async;
    }
    
    public void setAsync(double async) {
        this.async = async;
    }
    
    
    /////////////  Location-related properties  ///////////////
    
    public String getIP() {
        return ip;
    }
    
    public void setIP(String ip) {
        this.ip = ip;
    }
    
    
    public double[] getLatLong() {
        if (latitude == null || longitude == null)
            return null;
        else
            return new double[] {latitude.doubleValue(), longitude.doubleValue()};
    }
    
    public void setLatLong(String latlong) throws IllegalArgumentException {
        
        // TODO: Better support for other type of spec, with minutes, etc.
        String[] parts = latlong.split(",");
        if (parts.length != 2)
            throw new IllegalArgumentException("latlong specification must be two numbers separated by a comma");
        latitude = new Double(parts[0].trim());
        longitude = new Double(parts[1].trim());
    }
    
    public void setLatitude(double latitude) {
        this.latitude = new Double(latitude);
    }
    
    public void setLongitude(double longitude) {
        this.longitude = new Double(longitude);
    }
    
    public String getLocation() {
        return location;
    }
    
    public void setLocation(String loc) {
        this.location = loc;
    }
    
    public Boolean isMetric() {
        return metric;
    }
    
    public void setMetric(Boolean metric) {
        this.metric = metric;
    }
    
    public String getCurrency() {
        return currency;
    }
    
    public void setCurrency(String currency) {
        this.currency = currency;
    }
    
    
    public String getCountryCode() {
        return countryCode;
    }
    
    public void setCountryCode(String code) {
        this.countryCode = code;
    }
    
    public Boolean isAllowTranslation() {
        return allowTranslation;
    }
    
    public void setAllowTranslation(Boolean allow) {
        this.allowTranslation = allow;
    }
    
    
    ///////////////////  Widths  /////////////////////
    
    public int getWidth() {
        return width;
    }
    
    public void setWidth(int width) {
        this.width = width;
    }
    
    
    public int getMaxWidth() {
        return maxWidth;
    }
    
    public void setMaxWidth(int width) {
        this.maxWidth = width;
    }
    
    
    public int getPlotWidth() {
        return plotWidth;
    }
    
    public void setPlotWidth(int width) {
        this.plotWidth = width;
    }
    
    public double getMagnification() {
        return magnification;
    }
    
    public void setMagnification(double mag) {
        this.magnification = mag;
    }

    
    ////////////////  Pod selection  //////////////////
    
    public String[] getPodTitles() {
        return podTitles.toArray(new String[podTitles.size()]);
    }
    
    public void addPodTitle(String podtitle) {
        if (!podTitles.contains(podtitle))
            podTitles.add(podtitle);
    }
    
    public void clearPodTitles() {
        podTitles.clear();
    }
    
    
    public int[] getPodIndexes() {
        int[] result = new int[podIndices.size()];
        int i = 0;
        for (Integer index : podIndices) {
            result[i++] = index.intValue();
        }
        return result;
    }
    
    public void addPodIndex(int podindex) {
        Integer index = new Integer(podindex);
        if (!podIndices.contains(index))
            podIndices.add(index);
    }
    
    public void clearPodIndexes() {
        podIndices.clear();
    }

    
    public String[] getPodScanners() {
        return podScanners.toArray(new String[podScanners.size()]);
    }
    
    public void addPodScanner(String podscanner) {
        if (!podScanners.contains(podscanner))
            podScanners.add(podscanner);
    }
       
    public void clearPodScanners() {
        podScanners.clear();
    }

    
    public String[] getIncludePodIDs() {
        return includePodIDs.toArray(new String[includePodIDs.size()]);
    }
    
    public void addIncludePodID(String podid) {
        if (!includePodIDs.contains(podid))
            includePodIDs.add(podid);
    }
    
    public void clearIncludePodIDs() {
        includePodIDs.clear();
    }

    
    public String[] getExcludePodIDs() {
        return excludePodIDs.toArray(new String[excludePodIDs.size()]);
    }
    
    public void addExcludePodID(String podid) {
        if (!excludePodIDs.contains(podid))
            excludePodIDs.add(podid);
    }
    
    public void clearExcludePodIDs() {
        excludePodIDs.clear();        
    }

    
    //////////////  Assumptions and podstates  /////////////////
    
    public WAPodState[] getPodStates() {
        return podStates.toArray(new WAPodState[podStates.size()]);
    }

    public void addPodState(String podstate) {
        addPodState(new WAPodStateImpl(podstate));
    }
      
    public void addPodState(String podstate, long id) {
        addPodState(new WAPodStateImpl(podstate, id));
    }
      
    public void addPodState(WAPodState podstate) {
        // When adding a podstate, we treat single-value and <statelist> podstates differently. Single-state ones
        // just accumulate (eg. More,More,More), whereas statelist ones do not (no point in doing earthquakes "last 24 hours"
        // and then "this month"; we only want the most-recently chosen value).
        boolean isReplacingExistingState = false;
        if (podstate.getInputs().length > 1) {
            long newID = podstate.getID();
            ListIterator<WAPodState> iter = podStates.listIterator();
            while (iter.hasNext()) {
                WAPodState state = iter.next();
                long id = state.getID();
                if (id == newID) {
                    // If we get here, we have found an old podstate that is the same one as the one we are adding (except
                    // of course possibly a differently currently-selected index). Replace with the new one and exit.
                    iter.set(podstate);
                    isReplacingExistingState = true;
                    break;
                }
            }
        }
        if (!isReplacingExistingState)
            podStates.add(podstate);
    }
    
    public void clearPodStates() {
        podStates.clear();
    }

    
    public String[] getAssumptions() {
        return assumptions.toArray(new String[assumptions.size()]);
    }
    
    public void addAssumption(String assumption) {
        
        String newLhs = assumption.split("_")[0];
        // This is "add if new, modify existing one if a different RHS".
        boolean wasFound = false;
        ListIterator<String> iter = assumptions.listIterator();
        while (iter.hasNext()) {
            String oldAssumption = iter.next();
            String oldLhs = oldAssumption.split("_")[0];
            if (newLhs.equals(oldLhs)) {
                iter.set(assumption);
                wasFound = true;
                break;
            }
        }
        if (!wasFound)
            assumptions.add(assumption);
    }

    public void clearAssumptions() {
        assumptions.clear();
    }

    
    //////////////////////////////  Misc  ////////////////////////////////
    
    public void setRelatedLinks(boolean include) {
        includeRelatedLinks = include;
    }
    
    public boolean isRelatedLinks() {
        return includeRelatedLinks;
    }
    
    public void setReinterpret(boolean allowReinterpret) {
        this.allowReinterpret = allowReinterpret;
    }
    
    public boolean isReinterpret() {
        return allowReinterpret;
    }
    
    public void setSignature(String sig) {
        this.signature = sig;
    }
    
    // Returns a ref to the original, not a copy. Clients use carefully!
    public List<String[]> getExtraParams() {
        return extraParams;
    }
    
    
    // Leave out appid and sig.
    public List<String[]> getParameters() {
        
        List<String[]> params = new ArrayList<String[]>(15);
        StringBuilder s = new StringBuilder();
        String[] param;
        
        if (input != null) {
            param = new String[2];
            param[0] = "input";
            param[1] = encode(input);
            params.add(param);
        }
        
        int numFormats = formats.size();
        if (numFormats > 0) {
            param = new String[2];
            param[0] = "format";
            s.setLength(0);
            for (int i = 0; i < numFormats; i++) {
                s.append(formats.get(i));
                if (i < numFormats - 1)
                    s.append(",");
            }
            param[1] = s.toString();
            params.add(param);
        }

        param = new String[2];
        param[0] = "async";
        if (async == 0)
            param[1] = "true";
        else if (async > 0)
            param[1] = Double.toString(async);
        else
            param[1] = "false";
        params.add(param);

        if (scanTimeout > 0) {
            param = new String[2];
            param[0] = "scantimeout";
            param[1] = Double.toString(scanTimeout);
            params.add(param);
        }
        if (podTimeout > 0) {
            param = new String[2];
            param[0] = "podtimeout";
            param[1] = Double.toString(podTimeout);
            params.add(param);
        }
        if (formatTimeout > 0) {
            param = new String[2];
            param[0] = "formattimeout";
            param[1] = Double.toString(formatTimeout);
            params.add(param);
        }
        
        if (ip != null) {
            param = new String[2];
            param[0] = "ip";
            param[1] = ip;
            params.add(param);
        }
        if (latitude != null && longitude != null) {
            param = new String[2];
            param[0] = "latlong";
            param[1] = Double.toString(latitude) + "," + Double.toString(longitude);
            params.add(param);
        }
        if (location != null) {
            param = new String[2];
            param[0] = "location";
            param[1] = encode(location);
            params.add(param);
        }
        if (metric != null) {
            param = new String[2];
            param[0] = "units";
            param[1] = metric.booleanValue() ? "metric" : "nonmetric";
            params.add(param);
        }
        if (currency != null) {
            param = new String[2];
            param[0] = "currency";
            param[1] = currency;
            params.add(param);
        }
        if (countryCode != null) {
            param = new String[2];
            param[0] = "countrycode";
            param[1] = countryCode;
            params.add(param);
        }
        if (allowTranslation != null) {
            param = new String[2];
            param[0] = "translation";
            param[1] = allowTranslation.booleanValue() ? "true" : "false";
            params.add(param);
        }
        if (includeRelatedLinks) {
            param = new String[2];
            param[0] = "sidebarlinks";
            param[1] = "true";
            params.add(param);
        }
        if (allowReinterpret) {
            param = new String[2];
            param[0] = "reinterpret";
            param[1] = "true";
            params.add(param);
        }
        
        for (String t : podTitles) {
            param = new String[2];
            param[0] = "podtitle";
            param[1] = encode(t);
            params.add(param);
        }
        for (String sc : podScanners) {
            param = new String[2];
            param[0] = "scanner";
            param[1] = sc;
            params.add(param);
        }
        for (Integer i : podIndices) {
            param = new String[2];
            param[0] = "podindex";
            param[1] = Integer.toString(i);
            params.add(param);
        }
        for (String id : includePodIDs) {
            param = new String[2];
            param[0] = "includepodid";
            param[1] = encode(id);
            params.add(param);
        }
        for (String id : excludePodIDs) {
            param = new String[2];
            param[0] = "excludepodid";
            param[1] = encode(id);
            params.add(param);
        }
        
        for (String a : assumptions) {
            param = new String[2];
            param[0] = "assumption";
            // Assumption strings are already encoded in the output, so not encoded here.
            param[1] = a;
            params.add(param);
        }
        for (WAPodState p : podStates) {
            param = new String[2];
            param[0] = "podstate";
            param[1] = encode(p.getInputs()[p.getCurrentIndex()]);
            params.add(param);
        }
        
        if (width > 0) {
            param = new String[2];
            param[0] = "width";
            param[1] = Integer.toString(width);
            params.add(param);
        }
        if (maxWidth > 0) {
            param = new String[2];
            param[0] = "maxwidth";
            param[1] = Integer.toString(maxWidth);
            params.add(param);
        }
        if (plotWidth > 0) {
            param = new String[2];
            param[0] = "plotwidth";
            param[1] = Integer.toString(plotWidth);
            params.add(param);
        }
        if (magnification != 1.0) {
            param = new String[2];
            param[0] = "mag";
            param[1] = Double.toString(magnification);
            params.add(param);
        }
        for (String[] paramPair : extraParams) {
            param = new String[2];
            param[0] = paramPair[0];
            param[1] = paramPair[1];
            params.add(param);
        }
        
        return params;
    }
    
    
    private static String encode(String s) {
        try {
            return java.net.URLEncoder.encode(s, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            return null; // Will never happen, because UTF-8 is always supported. Doesn't matter what we return.
        }
    }

    
    //////////////////  From URL  ////////////////
    
    // Parse from a URL, either http://api.wolframalpha.com/....?input=foo&appid=bar...  or just input=foo&appid=bar...
    public void fillFromURL(String url) {
        
        int questionMarkPos = url.indexOf("?");
        // Works for ? not present (questionMarkPos == -1, then)
        String queryString = url.substring(questionMarkPos + 1);
        HashMap<String,List<String>> parmsMap = new HashMap<String,List<String>>();
        String params[] = queryString.split("&");
        for (String param : params) {
           String temp[] = param.split("=");
           if (temp.length == 2) {
               try {
                   String value = java.net.URLDecoder.decode(temp[1], "UTF-8");
                   List<String> values = parmsMap.get(temp[0]);
                   if (values == null)
                       values = new ArrayList<String>();
                   values.add(value);
                   parmsMap.put(temp[0], values);
               } catch (UnsupportedEncodingException e) {
                    // Do nothing; param will be lost. But exception is impossible.
               }
           }
        }
        List<String> input = parmsMap.get("input");
        if (input != null) setInput(input.get(0));
        List<String> formats = parmsMap.get("format");
        if (formats != null) {
            String[] fmts = formats.get(0).split(",");
            for (String fmt : fmts) addFormat(fmt);
        }
        List<String> ip = parmsMap.get("ip");
        if (ip != null) setIP(ip.get(0));
        List<String> latlong = parmsMap.get("latlong");
        if (latlong != null) setLatLong(latlong.get(0));
        List<String> location = parmsMap.get("location");
        if (location != null) setLocation(location.get(0));
        List<String> units = parmsMap.get("units");
        if (units != null) setMetric(units.equals("metric"));
        List<String> currency = parmsMap.get("currency");
        if (currency != null) setCurrency(currency.get(0));
        List<String> countryCode = parmsMap.get("countrycode");
        if ( countryCode != null) setCountryCode(countryCode.get(0));
        List<String> allowTranslation = parmsMap.get("translation");
        if (allowTranslation != null) setAllowTranslation(allowTranslation.equals("true"));
        List<String> podTitles = parmsMap.get("podtitle");
        if (podTitles != null)
            for (String title : podTitles) addPodTitle(title);
        List<String> podScanners = parmsMap.get("podscanner");
        if (podScanners != null)
            for (String scanner : podScanners) addPodScanner(scanner);
        List<String> podIndexes = parmsMap.get("podindex");
        if (podIndexes != null)
            for (String index : podIndexes) addPodIndex(Integer.parseInt(index));
        List<String> includePods = parmsMap.get("includepodid");
        if (includePods != null)
            for (String include : includePods) addIncludePodID(include);
        List<String> excludePods = parmsMap.get("excludepodid");
        if (excludePods != null)
            for (String exclude : excludePods) addExcludePodID(exclude);
        List<String> assumptions = parmsMap.get("assumption");
        if (assumptions != null)
            for (String a : assumptions) addAssumption(a);
        List<String> podStates = parmsMap.get("podstate");
        if (podStates != null)
            for (String state : podStates) addPodState(state);
        List<String> relatedLinks = parmsMap.get("sidebarlinks");
        if (relatedLinks != null) setRelatedLinks(relatedLinks.get(0).equals("true"));
        List<String> reinterpret = parmsMap.get("reinterpret");
        if (reinterpret != null) setReinterpret(reinterpret.get(0).equals("true"));
        
        // Lump together all the calls that interpret numbers. If any of these throws, we will just lose
        // setting for that param and the others that follow.
        try {
            List<String> width = parmsMap.get("width");
            if (width != null) setWidth(Integer.parseInt(width.get(0)));
            List<String> maxWidth = parmsMap.get("maxwidth");
            if (maxWidth != null) setMaxWidth(Integer.parseInt(maxWidth.get(0)));
            List<String> plotWidth = parmsMap.get("plotwidth");
            if (plotWidth != null) setPlotWidth(Integer.parseInt(plotWidth.get(0)));
            List<String> magnification = parmsMap.get("mag");
            if (magnification != null) setMagnification(Double.parseDouble(magnification.get(0)));
            List<String> async = parmsMap.get("async");
            if (async != null) {
                String asyncValue = async.get(0);
                if (asyncValue.equals("true"))
                    setAsync(0);
                else if (asyncValue.equals("false"))
                    setAsync(-1);
                else
                    setAsync(Double.parseDouble(asyncValue));
            }
            List<String> scantimeout = parmsMap.get("scantimeout");
            if (scantimeout != null) setScanTimeout(Double.parseDouble(scantimeout.get(0)));
            List<String> podtimeout = parmsMap.get("podtimeout");
            if (podtimeout != null) setPodTimeout(Double.parseDouble(podtimeout.get(0)));
            List<String> formattimeout = parmsMap.get("formattimeout");
            if (formattimeout != null) setFormatTimeout(Double.parseDouble(formattimeout.get(0)));
        } catch (NumberFormatException e) {
            // Do nothing.
        }
    }
    
    
    public String toWebsiteURL() {
        
        StringBuffer url = new StringBuffer("http://www.wolframalpha.com/input/?i=");
        try {
            url.append(URLEncoder.encode(input, "UTF-8"));
        } catch (UnsupportedEncodingException e) {}
        for (String a : getAssumptions()) {
            url.append("&a=");
            url.append(a);
        }
        return url.toString();        
    }

}
