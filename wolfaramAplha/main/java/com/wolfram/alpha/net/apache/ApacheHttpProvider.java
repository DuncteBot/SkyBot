/*
 * Created on Dec 5, 2009
 *
 */
package com.wolfram.alpha.net.apache;

import java.net.URL;

import org.apache.http.HttpVersion;
import org.apache.http.client.HttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.conn.tsccm.ThreadSafeClientConnManager;
import org.apache.http.params.BasicHttpParams;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.conn.params.ConnManagerParams;
import org.apache.http.conn.params.ConnPerRoute;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.conn.scheme.PlainSocketFactory;
import org.apache.http.conn.scheme.Scheme;
import org.apache.http.conn.scheme.SchemeRegistry;
import org.apache.http.conn.ssl.SSLSocketFactory;

import com.wolfram.alpha.net.HttpProvider;
import com.wolfram.alpha.net.ProxySettings;
import com.wolfram.alpha.net.impl.HttpTransaction;



public class ApacheHttpProvider implements HttpProvider {

    
    private static HttpClient httpClient;
    private static HttpParams params;
    private int socketTimeoutMillis = SOCKET_TIMEOUT_MILLIS;

    
    // HttpClient configuration settings.
    private static final String DEFAULT_USER_AGENT = "Wolfram|Alpha Java Binding 1.1";
    private static final int CONNECTION_TIMEOUT_MILLIS = 8000;
    // This is how long to wait for data after the connection is made. Has to be long enough to
    // accommodate query-type URLs that do work on the server before returning any data.
    private static final int SOCKET_TIMEOUT_MILLIS = 20000;
    // Default is 2.
    private static final int MAX_CONNECTIONS_PER_ROUTE = 8;
    private static final int MAX_RETRY_COUNT = 1;


    // This static block initializes Apache HttpClient.
    static {
        // Set up Apache HttpClient library for multi-threaded access.
        SchemeRegistry schemeRegistry = new SchemeRegistry();
        schemeRegistry.register(new Scheme("http", PlainSocketFactory.getSocketFactory(), 80));
        schemeRegistry.register(new Scheme("https", SSLSocketFactory.getSocketFactory(), 443));
        params = new BasicHttpParams();
        ConnManagerParams.setMaxConnectionsPerRoute(params, new ConnPerRoute() {
            public int getMaxForRoute(HttpRoute route) { return MAX_CONNECTIONS_PER_ROUTE; }
        });
        HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
        HttpProtocolParams.setUserAgent(params, DEFAULT_USER_AGENT);
        HttpConnectionParams.setConnectionTimeout(params, CONNECTION_TIMEOUT_MILLIS);
        HttpConnectionParams.setSoTimeout(params, SOCKET_TIMEOUT_MILLIS);
        ThreadSafeClientConnManager cm = new ThreadSafeClientConnManager(params, schemeRegistry);
        httpClient = new DefaultHttpClient(cm, params);
        // Retry handler? See http://hc.apache.org/httpcomponents-client/tutorial/html/fundamentals.html#d4e37
        
        // Credentials ??
        //httpClient.getParams().setParameter(CredentialsProvider.PROVIDER, ProxySettings.getInstance());
    }

    
    // TODO: Prove that this modifies the value for future requests.
    public void setUserAgent(String agent) {
        HttpProtocolParams.setUserAgent(params, agent);
    }

    public HttpTransaction createHttpTransaction(URL url, ProxySettings proxySettings) {
        return new ApacheHttpTransaction(httpClient, url, proxySettings, MAX_RETRY_COUNT, socketTimeoutMillis);
    }

    
    public HttpClient getHttpClient() {
        return httpClient;
    }

    
    ///////////////  Whither these ????  ////////////////////////
    
    private static ProxySettings proxySettings;

    public void setProxyInfo(int useProxy, String httpProxyHost, int httpProxyPort,
                                        String socksProxyHost, int socksProxyPort) {
        proxySettings.setProxyInfo(useProxy, httpProxyHost, httpProxyPort);
    }

    public void setProxyCredentials(String username, String password) {
        proxySettings.setProxyUsername(username);
        proxySettings.setProxyPassword(password);
    }


    public String[] getProxyHostAndPort(String url) {
        return proxySettings.getProxyHostAndPort(url);
    }

}
