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
 * Created on Aug 19, 2006
 *
 */
package com.wolfram.alpha.net.apache;

import com.wolfram.alpha.net.ProxySettings;
import com.wolfram.alpha.net.WAHttpException;
import com.wolfram.alpha.net.impl.HttpTransaction;
import org.apache.http.*;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

@SuppressWarnings("deprecation")
public class ApacheHttpTransaction implements HttpTransaction {

    private HttpClient httpClient;
    private HttpGet httpGet;
    private HttpResponse response;
    private HttpEntity entity;
    private URL url;
    private ProxySettings proxySettings;
    private int maxRetryCount;
    private int socketTimeoutMillis;  // use -1 for default
    private volatile boolean noRetry = false;

    ApacheHttpTransaction(HttpClient httpClient, URL url, ProxySettings proxySettings,
                          int maxRetryCount, int socketTimeoutMillis) {

        this.httpClient = httpClient;
        this.url = url;
        this.proxySettings = proxySettings != null ? proxySettings : ProxySettings.getInstance();
        this.maxRetryCount = maxRetryCount;
        this.socketTimeoutMillis = socketTimeoutMillis;
    }


    /**
     * If this returns without throwing, then you can (and must) proceed to reading the
     * content using getResponseAsString() or getResponseAsStream(). If it throws, then
     * you do not have to read. You must always call release().
     *
     * @throws WAHttpException which can be any other exception lol
     */
    public void execute() throws WAHttpException {

        httpGet = new HttpGet(url.toString());
        HttpHost proxy = proxySettings.getProxyForHttpClient(url.toString());
        httpClient.getParams().setParameter(ConnRoutePNames.DEFAULT_PROXY, proxy);
        
        try {
            response = httpClient.execute(httpGet);
            int statusCode = response.getStatusLine().getStatusCode();
            if (statusCode != HttpStatus.SC_OK)
                throw new WAHttpException(statusCode);
            entity = response.getEntity();
        } catch (Exception e) {
            // This also releases all resources.
            httpGet.abort();
            if (e instanceof WAHttpException)
                throw (WAHttpException) e;
            else
                throw new WAHttpException(e);
        }
    }


    public void release() {
        if (entity != null) {
            try {
                // Ensure the connection gets released to the manager
                entity.consumeContent();
            } catch (IOException e) {
                // Connection keep-alive will not be possible. Nothing to do.
            }
        }
    }


    public String[][] getResponseHeaders() {

        Header[] hdrs = response.getAllHeaders();
        String[][] result = new String[hdrs.length][];
        for (int i = 0; i < hdrs.length; i++) {
            result[i] = new String[]{hdrs[i].getName(), hdrs[i].getValue()};
        }
        return result;
    }

    /**
     * Only works for simple headers (ones that do not contain mltiple elements).
     *
     * @param headerName the key name of the header
     * @return the response header as a {@link String}
     */
    public String getResponseHeader(String headerName) {

        Header hdr = response.getFirstHeader(headerName);
        return hdr == null ? null : hdr.getValue();
    }
    
    // Negative number if not known.
    public long getContentLength() {
        return entity == null ? -1 : entity.getContentLength();
    }

    public String getCharSet() {
        return EntityUtils.getContentCharSet(entity);
    }
    
    
    public InputStream getResponseStream() throws IOException {
        return entity.getContent();
    }

    public String getResponseString() throws IOException {
        return EntityUtils.toString(entity);
    }

    // Typically called on another thread.
    public void setNoRetry() {
        noRetry = true;
    }

    // Called on another thread. Only aborts downloading, not attempts to connect.
    // Not currently used (cancel() method of FileDownloader is called instead).
    public void abort() {
        httpGet.abort();
    }

    
    /***

     ///////////////////////////  HttpMethodRetryHandler Class  /////////////////////////////

     class RetryHandler implements HttpMethodRetryHandler {

     public boolean retryMethod(final HttpMethod method, final IOException exception, int retryCount) {

     logger.info("In retryMethod(), retry count: " + retryCount + ", exception: " + exception);

     // Much of this code taken from org.apache.commons.httpclient.DefaultHttpMethodRetryHandler,
     // but we want full control so we write our own handler.

     if (retryCount > maxRetryCount || noRetry)
     return false;

     // See http://jakarta.apache.org/commons/httpclient/exception-handling.html for more on these.
     if (exception instanceof NoHttpResponseException) {
     // Retry if the server dropped connection on us
     return true;
     } else if (exception instanceof ConnectionPoolTimeoutException) {
     // Retry if the problem is only that there isn't a thread available (not likely).
     return true;
     } else if (exception instanceof ConnectTimeoutException) {
     // Retry if we couldn't connect.
     return true;
     } else if (exception instanceof UnknownHostException) {
     // Unknown host
     return false;
     } else if (exception instanceof NoRouteToHostException) {
     // Host unreachable
     return false;
     } else if (exception instanceof SocketException) {
     // SocketException includes the "malformed reply form SOCKS server" error that
     // seems to induce an unavoidable 5 min timeout. Don't want to retry on that
     // error, and probably on any other SocketException.
     return false;
     } else if (exception instanceof SSLHandshakeException) {
     // We refused to accept the site's certificate, and perhaps other authentication errors.
     return false;
     }

     if (!method.isRequestSent()) {
     // Retry if the request has not been sent fully or
     // if it's OK to retry methods that have been sent
     return true;
     }
     // otherwise do not retry
     return false;
     }
     }
     ***/
    
    public int getMaxRetryCount() {
        return maxRetryCount;
    }

    public int getSocketTimeoutMillis() {
        return socketTimeoutMillis;
    }

    public boolean isNoRetry() {
        return noRetry;
    }

}
