/*
 * Created on Dec 5, 2009
 *
 */
package com.wolfram.alpha.net;

import com.wolfram.alpha.net.apache.ApacheHttpProvider;


public class HttpProviderFactory {
    
    // Will need to be a Map of providers when I support more than just the default provider.
    private static HttpProvider provider;
    
    // Return singleton instance (at least, a singleton for each type of provider).
    public static synchronized HttpProvider getDefaultHttpProvider() {
        if (provider == null)
            provider = new ApacheHttpProvider();
        return provider;
    }

}
