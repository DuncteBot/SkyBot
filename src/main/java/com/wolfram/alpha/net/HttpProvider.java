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


import com.wolfram.alpha.net.impl.HttpTransaction;

import java.net.URL;


// IF thids never gets anything more than createTransaction(), it should probably be a class, HttpTransactionFactory.

public interface HttpProvider {

    HttpTransaction createHttpTransaction(URL url, ProxySettings proxySettings);
    
    // TODO: Don't like this. If style is to create one provider and use it always, then having a setter
    // can change state of all uses of this provider in other threads. Better to have a factory that
    // creates providers with certain params (like useragent), and these are not singletons. Thus if you
    // want to change useragent in a session, or have multiple different types of transactions, you just
    // create different providers. MAYBE this is OK, Have setters for all params here.
    void setUserAgent(String agent);

}
