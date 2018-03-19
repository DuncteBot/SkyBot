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
 * Created on Nov 8, 2009
 *
 */
package com.wolfram.alpha.impl;

import com.wolfram.alpha.WAQuery;
import com.wolfram.alpha.WAQueryParameters;

import java.io.Serializable;
import java.util.List;


public class WAQueryImpl extends WAQueryParametersImpl implements WAQuery, Serializable {


    private static final long serialVersionUID = -1282976731786573517L;


    public WAQueryImpl(WAQueryParameters params) {
        super(params);
    }


    public WAQuery copy() {
        return new WAQueryImpl(this);
    }


    // Creates the URL representation of this query, not including server, path, and appid param. Result starts with &.
    public String toString() {

        StringBuilder s = new StringBuilder(600);

        List<String[]> params = getParameters();
        for (String[] param : params) {
            s.append("&");
            s.append(param[0]);
            s.append("=");
            s.append(param[1]);
        }

        if (signature != null) {
            s.append("&sig=");
            s.append(signature);
        }

        return s.toString();
    }

}
