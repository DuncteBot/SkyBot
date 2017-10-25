/*
 * Created on Dec 5, 2009
 *
 */
package com.wolfram.alpha;


public class WAException extends Exception {

    private static final long serialVersionUID = 6178883112953158149L;

    public WAException(Throwable t) {
        super(t);
    }
    
    public WAException(String s) {
        super(s);
    }
    
}
