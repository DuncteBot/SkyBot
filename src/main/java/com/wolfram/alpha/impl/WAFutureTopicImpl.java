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
 * Created on Sep 19, 2010
 *
 */
package com.wolfram.alpha.impl;

import com.wolfram.alpha.WAFutureTopic;
import com.wolfram.alpha.visitor.Visitor;
import org.w3c.dom.Element;

import java.io.Serializable;


public class WAFutureTopicImpl implements WAFutureTopic, Serializable {
    
    private static final long serialVersionUID = -511306768207916575L;
    private String msg;
    private String topic;
    
    
    WAFutureTopicImpl(Element thisElement) {
        
        msg = thisElement.getAttribute("msg");
        topic = thisElement.getAttribute("topic");
    }
    
    
    public String getMessage() {
        return msg;
    }
    
    
    public String getTopic() {
        return topic;
    }
    
    
    ///////////////////////////  Visitor interface  ////////////////////////////
    
    public void accept(Visitor v) {
        v.visit(this);
    }
    
}
