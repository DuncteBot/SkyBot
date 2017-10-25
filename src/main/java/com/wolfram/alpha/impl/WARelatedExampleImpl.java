/*
 * Created on Mar 1, 2010
 *
 */
package com.wolfram.alpha.impl;

import java.io.File;
import java.io.Serializable;

import org.w3c.dom.Element;

import com.wolfram.alpha.WAException;
import com.wolfram.alpha.WAImage;
import com.wolfram.alpha.WARelatedExample;
import com.wolfram.alpha.net.HttpProvider;
import com.wolfram.alpha.visitor.Visitor;


public class WARelatedExampleImpl implements WARelatedExample, Serializable {

    private String input;
    private String desc;
    private String category;
    private WAImage categoryThumb;
    private String categoryPage;   
    
    static final WARelatedExampleImpl[] EMPTY_ARRAY = new WARelatedExampleImpl[0];

    private static final long serialVersionUID = -1235014424251757805L;

    
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
