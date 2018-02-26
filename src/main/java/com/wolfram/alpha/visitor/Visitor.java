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
 * Created on Dec 8, 2009
 *
 */
package com.wolfram.alpha.visitor;

import com.wolfram.alpha.*;


public interface Visitor {
    
    void visit(WAQueryResult obj);

    void visit(WAPod obj);

    void visit(WASubpod obj);

    void visit(WAAssumption obj);

    void visit(WAWarning obj);

    void visit(WAInfo obj);

    void visit(WAPodState obj);

    void visit(WARelatedLink obj);

    void visit(WARelatedExample obj);

    void visit(WASourceInfo obj);

    void visit(WAFutureTopic obj);

    void visit(WAExamplePage obj);

    void visit(WALink obj);

    void visit(WAReinterpretWarning obj);

    void visit(WAUnits obj);
    
    // Content types
    void visit(WAPlainText obj);

    void visit(WAImage obj);

    void visit(WASound obj);

}
