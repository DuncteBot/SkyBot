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
