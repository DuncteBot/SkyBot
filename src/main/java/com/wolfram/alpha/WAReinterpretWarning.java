/*
 * Created on Sep 19, 2010
 *
 */
package com.wolfram.alpha;


public interface WAReinterpretWarning extends WAWarning {

    String getNew();
    String[] getAlternatives();
}
