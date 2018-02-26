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
 * Created on Dec 7, 2009
 *
 */
package com.wolfram.alpha.net.j2se;

import com.wolfram.alpha.net.ProxySettings;
import com.wolfram.alpha.net.WAHttpException;
import com.wolfram.alpha.net.impl.HttpTransaction;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.Proxy;
import java.net.URL;


public class J2SEHttpTransaction implements HttpTransaction {
    
    private HttpURLConnection conn;
    private URL url;
    private String userAgent;
    private ProxySettings proxySettings;
    
    
    J2SEHttpTransaction(URL url, ProxySettings proxySettings, String userAgent) {
        this.url = url;
        this.userAgent = userAgent;
        this.proxySettings = proxySettings != null ? proxySettings : ProxySettings.getInstance();
    }
    

    public void abort() {
        
    }


    public void execute() throws WAHttpException {

        try {
            Proxy proxy = proxySettings.getProxyForJavaNet(url.toString());
            if (proxy != null) {
                conn = (HttpURLConnection) url.openConnection(proxy);
            } else {
                conn = (HttpURLConnection) url.openConnection();
            }
            conn.setRequestMethod("GET");
            
            // TODO: This value
            conn.setReadTimeout(15 * 1000);
            conn.connect();
            
            int statusCode = conn.getResponseCode();
            if (statusCode != HttpURLConnection.HTTP_OK) {
                throw new WAHttpException(statusCode);
            }
            
        } catch (IOException e) {
            throw new WAHttpException(e);
        }
        // TODO: Authentication issues? socket timeouts? User agent setting?
    }


    public long getContentLength() {
        return conn.getContentLength();
    }

    public String getCharSet() {
        String charset = "ISO-8859-1";
        return charset;

    }

    public String[][] getResponseHeaders() {

        // TODO Auto-generated method stub
        return null;
    }


    public InputStream getResponseStream() throws IOException {
        return conn.getInputStream();
    }


    public String getResponseString() throws IOException {
        
        InputStream strm = conn.getInputStream();
        if (strm == null)
            return null;
        
        // Because we cast to int, will fail for huge downloads (>2Gb), but those wouldn't fit into
        // memory anyway (will double in size as a string).
        int contentLength = (int) getContentLength();
        ByteArrayOutputStream outStrm = new ByteArrayOutputStream(contentLength);
        byte[] buf = new byte[8192];
        int len;
        while ((len = strm.read(buf)) > 0) {
            outStrm.write(buf, 0, len);
        }
        outStrm.close();
        try {
            // For general-purpose use, would want to replace hard-coded ISO-8859-1 with value determined from response.
            return new String(outStrm.toByteArray(), "ISO-8859-1");
        } catch (UnsupportedEncodingException e) {
            return null; // Will never happen
        }
    }


    public void release() {
        try {
            if (conn != null) {
                InputStream strm = conn.getInputStream();
                if (strm != null)
                    strm.close();
            }
        } catch (IOException ignored) {
        }
    }


    public void setNoRetry() {

        // TODO Auto-generated method stub

    }
    
    public String getUserAgent() {
        return userAgent;
    }

}
