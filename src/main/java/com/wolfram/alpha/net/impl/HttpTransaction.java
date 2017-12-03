/*
 * Skybot, a multipurpose discord bot
 *      Copyright (C) 2017  Duncan "duncte123" Sterken & Ramid "ramidzkh" Khan & Maurice R S "Sanduhr32"
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
package com.wolfram.alpha.net.impl;

import com.wolfram.alpha.net.WAHttpException;

import java.io.IOException;
import java.io.InputStream;


public interface HttpTransaction {

    void execute() throws WAHttpException;
    
    void release();
    
    long getContentLength();

    String getCharSet() throws IOException;

    String[][] getResponseHeaders() throws IOException;
    
    InputStream getResponseStream() throws IOException;
    
    String getResponseString() throws IOException;

    // IS this needed?
    void setNoRetry();

    void abort();

}
