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
 * Created on Nov 10, 2009
 *
 */
package com.wolfram.alpha;


import com.wolfram.alpha.visitor.Visitable;


public interface WAPod extends Visitable {

    String getTitle();

    boolean isError();

    int getNumSubpods();

    String getScanner();

    int getPosition();

    String getID();

    String getAsyncURL();

    WAException getAsyncException();

    WASubpod[] getSubpods();

    WAPodState[] getPodStates();

    WAInfo[] getInfos();

    WASound[] getSounds();

    void acquireImages() throws WAException;

    void finishAsync() throws WAException;

    Object getUserData();

    void setUserData(Object obj);

}
