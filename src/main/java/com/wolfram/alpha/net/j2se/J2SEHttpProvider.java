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
 * Created on Dec 5, 2009
 *
 */
package com.wolfram.alpha.net.j2se;

import com.wolfram.alpha.net.HttpProvider;
import com.wolfram.alpha.net.ProxySettings;
import com.wolfram.alpha.net.impl.HttpTransaction;

import java.net.URL;


public class J2SEHttpProvider implements HttpProvider {

    
    private String userAgent = "Wolfram|Alpha Java Binding 1.1";

    
    public HttpTransaction createHttpTransaction(URL url, ProxySettings proxySettings) {
        return new J2SEHttpTransaction(url, proxySettings, userAgent);
    }


    public void setUserAgent(String agent) {
        this.userAgent = agent;
    }

}
