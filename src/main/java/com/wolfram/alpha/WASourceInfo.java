/*
 * Created on Feb 24, 2010
 *
 */
package com.wolfram.alpha;

import com.wolfram.alpha.visitor.Visitable;


public interface WASourceInfo extends Visitable {
    
    // There are two forms for the HTML data for source info. The first is a link to "raw" HTML that would be easy for clients to operate
    // on to format as desired. This is the URL that Android and iPhone request, although server-side code detects this
    // and replaces the file with a specially-formatted one. This wouldn't happen for other API clients. This URL is what is
    // returned by getURL(). The second form for source info is the HTML page that you would see on the website
    // (it has a banner that looks like the rest of the website). Clients might want to direct users to that page instead,
    // and this is what is returned by getFormattedURL().
    String getURL();
    String getFormattedURL();
    String getText();
}
