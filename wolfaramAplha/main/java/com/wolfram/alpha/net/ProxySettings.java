/*
 * Created on Nov 5, 2006
 *
 */
package com.wolfram.alpha.net;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpHost;


/**
 * Responsible for all things related to proxies. This includes storing the manually-set proxy
 * host and port (if any), deciding what proxy, if any, to use for a given request
 * (determined by manual or system settings), and serving up username/password credentials if required
 * for an authenticating proxy.
 *
 * The one thing this class does not do is handle prompting the user for username and password
 * if necessary. That is handled in Mathematica code triggered by a 407 HTTP status.
 *
 * The HttpHandler instance holds one instance of this class and uses it when necessary or passes
 * it to other classes that need to use it.
 *
 * @author tgayley
 *
 */
public class ProxySettings {

    private static ProxySettings instance = null;  // Singleton

    private int useProxy;  // 0 = false, 1 = automatic, 2 = true
    private String httpProxyHost;   // For these values, null or 0 means ignore.
    private int httpProxyPort;

    private String proxyUsername;
    private String proxyPassword;

    public static final int PROXY_NONE = 0;
    public static final int PROXY_AUTOMATIC = 1;
    public static final int PROXY_MANUAL = 2;


    private ProxySettings() {
        // Try to grab system-specific proxy settings (e.g., from Internet Explorer on Windows).
        // Actual acquisition of these values is done in setupProxy().
        System.setProperty("java.net.useSystemProxies", "true");
        ProxySelector.setDefault(MyProxySelector.getInstance());
    }


    public static synchronized ProxySettings getInstance() {
        if (instance == null)
            instance = new ProxySettings();
        return instance;
    }


    public synchronized void setProxyInfo(int useProxy, String httpProxyHost, int httpProxyPort) {

        this.useProxy = useProxy;
        this.httpProxyHost = httpProxyHost;
        this.httpProxyPort = httpProxyPort;
    }


    synchronized int getUseProxy() {
        return useProxy;
    }


    //////////////////////////////  Authentication  /////////////////////////////////

    synchronized String getProxyUsername() {
        return proxyUsername;
    }

    public synchronized void setProxyUsername(String username) {
        proxyUsername = username;
    }

    synchronized String getProxyPassword() {
        return proxyPassword;
    }

    public synchronized void setProxyPassword(String password) {
        proxyPassword = password;
    }



    ///////////////////////////////  Proxy config  ////////////////////////////////////

    // Decide what proxy, if any, to use for a request. Manual settings take precedence,
    // then we fall back to using system settings (e.g., Internet Explorer settings on Windows)
    // as handled by the ProxySelector class introduced in Java 1.5.
    //
    // Public so that it can be called from Mathematica.
    //
    public synchronized String[] getProxyHostAndPort(String url) {

        int port = 0;
        String host = null;

        if (useProxy == PROXY_AUTOMATIC) {
            ProxySelector ps = ProxySelector.getDefault(); // Will get MyProxySelector.
            try {
                URI uri = new URI(url);
                List<Proxy> proxyList = ps.select(uri);
                int len = proxyList.size();
                for (int i = 0; i < len; i++) {
                    Proxy p = (Proxy) proxyList.get (i);
                    InetSocketAddress addr = (InetSocketAddress) p.address();
                    if (addr != null) {
                        host = addr.getHostName();
                        port = addr.getPort();
                        break;
                    }
                }
            } catch (Exception e) {
                // TODO: Handle?
            }
        } else if (useProxy == PROXY_MANUAL) {
            String protocol;
            int colonPos = url.indexOf(':');
            if (colonPos != -1) {
                protocol = url.substring(0, colonPos).toLowerCase();
            } else {
                // Shouldn't happen, but might as well do something and let it fail later.
                protocol = "http";
            }
            if (protocol.equals("http")) {
                host = httpProxyHost;
                port = httpProxyPort;
            }
            // Don't handle directly Socks calls.
        }

        return new String[]{host, String.valueOf(port)};
    }


    public synchronized HttpHost getProxyForHttpClient(String url) {

        String[] hostAndPort = getProxyHostAndPort(url);
        String host = hostAndPort[0];
        int port = Integer.parseInt(hostAndPort[1]);

        if (port != 0 && host != null) {
            return new HttpHost(host, port, "http");
        } else {
            return null;
        }
    }


    public synchronized Proxy getProxyForJavaNet(String url) {

        String[] hostAndPort = getProxyHostAndPort(url);
        String host = hostAndPort[0];
        int port = Integer.parseInt(hostAndPort[1]);

        // If we got settings for both host and port, use them; otherwise no proxy.
        if (port != 0 && host != null)
            return new Proxy(Proxy.Type.HTTP, new InetSocketAddress(host, port));
        else
            return null; // Don't return Proxy.NO_PROXY, as that seems not to be a usable value--just a sentinel.
    }


    // This class is plugged in as the default system-wide ProxySelector.
    // It's a slightly weird setup, since ProxySettings calls this class for Automatic
    // proxy configuration, and this class calls back to ProxySettings for Manual and
    // Direct proxy settings. Things would be a little cleaner if we didn't have to worry
    // about Java 1.4 compatibility (this class inherits from a 1.5-only class).

    static class MyProxySelector extends ProxySelector {

        static MyProxySelector instance = null;
        ProxySelector origSelector = null;

        // The list we return when we want to indicate to the caller that no proxy should be used.
        // In older versions of Java you could return an empty list, but in newer ones there is a bug
        // where the java.net code will throw a NullPointerException if you return an empty list.
        private final List<Proxy> NO_PROXY_LIST = new ArrayList<Proxy>(1);

        private MyProxySelector() {
            origSelector = ProxySelector.getDefault();
            NO_PROXY_LIST.add(Proxy.NO_PROXY);
        }

        static synchronized ProxySelector getInstance() {
            if (instance == null)
                instance = new MyProxySelector();
            return instance;
        }

        public List<Proxy> select(URI uri) {

            int useProxy = ProxySettings.getInstance().getUseProxy();
            if (useProxy == PROXY_AUTOMATIC)
                return origSelector.select(uri);
            else if (useProxy == PROXY_MANUAL) {
                Proxy p = (Proxy) ProxySettings.getInstance().getProxyForJavaNet(uri.toString());
                if (p != null) {
                    List<Proxy> proxies = new ArrayList<Proxy>(1);
                    proxies.add(p);
                    return proxies;
                } else {
                    return NO_PROXY_LIST;
                }
            } else {
                return NO_PROXY_LIST;
            }
        }

        public void connectFailed(URI uri, SocketAddress sa, IOException ioe) {
            origSelector.connectFailed(uri, sa, ioe);
        }

    }

}
