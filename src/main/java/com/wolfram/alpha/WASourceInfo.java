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
