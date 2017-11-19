/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Sanduhr32
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
 *
 */

/*
 * Created on Feb 4, 2007
 *
 */
package com.wolfram.alpha.net;

import com.wolfram.alpha.WAException;
import com.wolfram.alpha.net.impl.HttpTransaction;

import java.io.*;
import java.net.URL;
import java.util.logging.Logger;


public class URLFetcher {

    // Largest result that will be allowed to be returned as a byte[] instead of in a file.
    private static final int MAX_BUFFER_SIZE = 1000000;
    public static Logger logger = Logger.getLogger("com.wolfram.alpha.net.URLFetcher");
    HttpProvider http;
    private URL url;
    private String outFile;
    private ProxySettings proxySettings;
    // These are volatile because this class is typically run on one thread and
    // queried on another.
    private volatile HttpTransaction trans;
    private volatile boolean wasCancelled = false;
    private volatile boolean isFinished = false;
    private volatile File downloadedFile = null;
    private volatile byte[] bytes = null;
    private volatile int totalBytes = -1;
    private volatile int totalBytesDownloaded = -1;
    private volatile String charSet = null;
    // WAHttpException, HttpException, IOException
    private volatile Exception exception = null;
    

    // TODO: outFile = null means get data as string. Improve getResponseString() to be safer for large responses.
    public URLFetcher(URL url, String outFile, HttpProvider http, ProxySettings proxySettings) {

        this.url = url;
        this.outFile = outFile;
        this.http = http;
        this.proxySettings = proxySettings;
    }


    // TODO: What is diff between this and abort()? Is this necesary?
    public void cancel() {
        wasCancelled = true;
        // Although we cannot abort attempts to connect, at least we can tell it to not retry.
        if (trans != null)
            trans.setNoRetry();
    }
    
    public boolean wasCancelled() {
        return wasCancelled;
    }
    
    /**
     * Doesn't mean that it finished successfully; could have been cancelled.
     *
     * @return
     */
    public boolean isFinished() {
        return isFinished;
    }
    
    
    public String getFilename() {
        return outFile;
    }
    
    public File getFile() {
        return wasCancelled ? null : downloadedFile;
    }
    
    public byte[] getBytes() {
        return wasCancelled ? null : bytes;
    }
    
    /**
     * @return -1 if not known
     */
    public int getTotalBytes() {
        return totalBytes;
    }
    
    public int getTotalBytesDownloaded() {
        return totalBytesDownloaded;
    }
    
    // returns -1.0 if not known.
    public double getProgress() {
        
        if (isFinished())
            return 1.0;
        int totalBytes = getTotalBytes();
        if (totalBytes == -1)
            return -1.0;
        return ((double) getTotalBytesDownloaded()) / totalBytes;
    }
    
    // Useful if you want to convert to a String.
    public String getCharSet() {
        return charSet;
    }

    public Exception getException() {
        return exception;
    }
    
    public void fetch() {
        
        try {
            if (wasCancelled)
                return;
            
            //long start = System.currentTimeMillis();
            
            // TODO: Output?
            // logger.info("Downloading url " + url);
            
            InputStream responseStream = null;
            OutputStream outStream = null;
            boolean useFile = outFile != null;
            
            try {
                trans = http.createHttpTransaction(url, proxySettings);
                trans.execute();
                long contentLength = trans.getContentLength();
                charSet = trans.getCharSet();
                responseStream = trans.getResponseStream();
                
                // Create the output stream we will write into. Will be either a buf[] or a file.
                if (useFile) {
                    if (outFile.length() > 0) {
                        downloadedFile = new File(outFile);
                    } else {
                        // If user passed in "" for outFile, create one in the standard temp dir.
                        downloadedFile = File.createTempFile("WolframAlphaAPI", ".tmp", null);
                        outFile = downloadedFile.getAbsolutePath();
                    }
                    outStream = new FileOutputStream(downloadedFile);
                } else {
                    // Want result in byte[] buffer. First check if it is too large.
                    if (contentLength == -1 || contentLength > MAX_BUFFER_SIZE)
                        throw new WAException("Content from URL " + url + " is unknown or too large to be buffered in memory. Read into a file instead.");
                    outStream = new ByteArrayOutputStream((int) contentLength);
                }

                // Read the data into the file/buffer.
                byte[] buf = new byte[8192];
                totalBytesDownloaded = 0;
                long maxBytesToDownload = useFile ? Long.MAX_VALUE : contentLength;
                int numRead;
                // TODO: Probably add Thread.wasInterrupted() test (or whatever it is called), to this loop.
                while ((numRead = responseStream.read(buf)) != -1 && !wasCancelled) {
                    totalBytesDownloaded += numRead;
                    if (totalBytesDownloaded <= maxBytesToDownload)
                        outStream.write(buf, 0, numRead);
                }
                // Might be useful someday to handle all the checked exceptions differently...
            } catch (WAHttpException e) {
                exception = e;
            } catch (IOException e) {
                exception = e;
            } catch (Exception e) {
                exception = e;
            } finally {
                if (responseStream != null)
                    try {
                        responseStream.close();
                    } catch (Exception e) {
                    }
                if (trans != null)
                    trans.release();
                if (outStream != null) {
                    if (!wasCancelled && !useFile)
                        bytes = ((ByteArrayOutputStream) outStream).toByteArray();
                    try {
                        outStream.close();
                    } catch (Exception e) {
                    }
                }
                if (wasCancelled && downloadedFile != null) {
                    downloadedFile.delete();
                    downloadedFile = null;
                }
            }
            
            /*
            if (exception != null) {
                // logger.warning("Exception downloading URL " + url + ". " + exception);
            }
            
            if (wasCancelled)
                logger.info("Download of URL " + url + " was cancelled by user. Elapsed millis: " + 
                                (System.currentTimeMillis() - start));
            else
                logger.info("Finished downloading URL " + url +
                        ". Elapsed millis: " + (System.currentTimeMillis() - start));
            */
        } finally {
            isFinished = true;
        }
    }

}
