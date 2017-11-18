/*
 * Created on Nov 8, 2009
 *
 */
package com.wolfram.alpha;

import com.wolfram.alpha.impl.WAQueryImpl;
import com.wolfram.alpha.impl.WAQueryParametersImpl;
import com.wolfram.alpha.impl.WAQueryResultImpl;
import com.wolfram.alpha.net.HttpProvider;
import com.wolfram.alpha.net.HttpProviderFactory;
import com.wolfram.alpha.net.URLFetcher;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;


public class WAEngine extends WAQueryParametersImpl {
    
    private static final long serialVersionUID = -5237279408150019312L;
    HttpProvider http;
    File downloadDir;
    // These fields belong to the engine. They are not propagated to the WAQuery. If you want different values
    // for any of these in your app, create a differnt WAEngine. You can execute WAQuery objects created by one
    // WAEngine in another.
    private String server = "api.wolframalpha.com";
    private String path = "/v2/query";
    private String appid;
    
    // TODO. These ctors are a total mess. Cut them down and add setters.
    
    public WAEngine() {
        this(HttpProviderFactory.getDefaultHttpProvider(), null);
    }
    
    public WAEngine(HttpProvider http, File downloadDir) {
        // A user would never call this ctor with null, but other ctors might. This allows us to provide
        // ctors that take, say, an appid and an HTTPProvider, but users can pass null for the HTTPProvider
        // if they want the default.
        this.http = (http != null) ? http : HttpProviderFactory.getDefaultHttpProvider();
        if (downloadDir != null) {
            this.downloadDir = downloadDir;
        } else {
            String tempDir = System.getProperty("java.io.tempdir");
            if (tempDir != null)
                this.downloadDir = new File(tempDir);
        }
    }
    
    public WAEngine(String appid, HttpProvider http, File downloadDir) {
        this(http, downloadDir);
        this.appid = appid;
    }
    
    public WAEngine(String appid, String server) {
        this();
        this.server = server;
        this.appid = appid;
    }
    
    public WAEngine(String appid, String server, HttpProvider http) {
        this(http, null);
        this.server = server;
        this.appid = appid;
    }
    
    // Advanced users only.
    public WAEngine(String appid, String server, String path, HttpProvider http, File downloadDir) {
        this(appid, http, downloadDir);
        this.server = server;
        this.path = path;
    }
    
    // TODO: Total mess. This one doesn't even work right...
    // Use the params from another object.
    public WAEngine(WAQueryParameters params, HttpProvider http, File downloadDir) {
        super(params);
        this.http = http;
    }
    
    
    public String getAppID() {
        return appid;
    }
    
    public void setAppID(String appid) {
        this.appid = appid;
    }
    
    
    public WAQuery createQuery() {
        return new WAQueryImpl(this);
    }
    
    public WAQuery createQuery(String input) {
        WAQuery query = createQuery();
        query.setInput(input);
        return query;
    }
    
    // Parse from a URL, either http://api.wolframalpha.com/....?input=foo&appid=bar...  or just input=foo&appid=bar...
    public WAQuery createQueryFromURL(String url) {
        WAQuery query = createQuery();
        query.fillFromURL(url);
        return query;
    }
    
    
    public WAQueryResult performQuery(WAQuery query) throws WAException {
        
        URL url;
        try {
            url = new URL(toURL(query));
        } catch (MalformedURLException e) {
            // Sure this can never happen.
            throw new WAException(e);
        }
        
        URLFetcher fetcher = new URLFetcher(url, null, http, null);
        fetcher.fetch();
        if (fetcher.wasCancelled())
            throw new WAException("Download of url " + url + " was cancelled");
        if (fetcher.getException() != null)
            throw new WAException(fetcher.getException());
        return new WAQueryResultImpl(query, fetcher.getBytes(), http, downloadDir);
    }
    
    
    public WAQueryResult performRecalculate(String recalcURL) throws WAException {
        
        URL url;
        try {
            url = new URL(recalcURL);
        } catch (MalformedURLException e) {
            // Sure this can never happen.
            throw new WAException(e);
        }
        
        URLFetcher fetcher = new URLFetcher(url, null, http, null);
        fetcher.fetch();
        if (fetcher.wasCancelled())
            throw new WAException("Download of url " + url + " was cancelled");
        if (fetcher.getException() != null)
            throw new WAException(fetcher.getException());
        return new WAQueryResultImpl(null, fetcher.getBytes(), http, downloadDir);
    }
    
    
    public String toURL(WAQuery query) {
        return "http://" + server + path + "?" + "appid=" + appid + query;
    }
    
    
    // Users can of course call HttpProviderFactory.getDefaultHttpProvider() directly, but it's convenient to have
    // the engine return the one it is using.
    public HttpProvider getHttpProvider() {
        return http;
    }
    
    
    public File getDownloadDir() {
        return downloadDir;
    }
    
}
