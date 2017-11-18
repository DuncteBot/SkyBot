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
 * Created on Mar 1, 2010
 *
 */
package com.wolfram.alpha.impl;

import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAImage;
import com.wolfram.alpha.WARelatedExample;
import com.wolfram.alpha.net.HttpProvider;
import com.wolfram.alpha.visitor.Visitor;
import org.w3c.dom.Element;

import java.io.File;
import java.io.Serializable;


public class WARelatedExampleImpl implements WARelatedExample, Serializable {
    
    static final WARelatedExampleImpl[] EMPTY_ARRAY = new WARelatedExampleImpl[0];
    private static final long serialVersionUID = -1235014424251757805L;
    private String input;
    private String desc;
    private String category;
    private WAImage categoryThumb;
    private String categoryPage;
    
    
    WARelatedExampleImpl(Element thisElement, HttpProvider http, File tempDir) throws WAException {
        
        input = thisElement.getAttribute("input");
        desc = thisElement.getAttribute("desc");
        category = thisElement.getAttribute("category");
        categoryThumb = new WAImageImpl(thisElement.getAttribute("categorythumb"), http, tempDir);
        categoryPage = thisElement.getAttribute("categorypage");
        if (input.equals("")) input = null;
        if (desc.equals("")) desc = null;
        if (category.equals("")) category = null;
        if (categoryPage.equals("")) categoryPage = null;
    }
    
    
    public String getInput() {
        return input;
    }
    
    public String getDescription() {
        return desc;
    }
    
    public String getCategory() {
        return category;
    }
    
    public String getCategoryPage() {
        return categoryPage;
    }
    
    public WAImage getCategoryThumb() {
        return categoryThumb;
    }
    
    
    ///////////////////////////  Visitable interface  ////////////////////////////
    
    public void accept(Visitor v) {
        v.visit(this);
    }
    
}
