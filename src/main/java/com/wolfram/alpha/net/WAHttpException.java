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
 * Created on Dec 6, 2009
 *
 */
package com.wolfram.alpha.net;


import org.apache.http.HttpStatus;


/**
 * Either has httpStatusCode member set to a value other than HttpStatus.SC_OK or wraps another
 * exception such as IOException or HttpException.
 * 
 * @author tgayley
 *
 */
public class WAHttpException extends Exception {

    public int httpStatusCode = HttpStatus.SC_OK;
    
    private static final long serialVersionUID = 59955069668288618L;

    
    // Do not add other constructors without reviewing uses of HttpHandlerException,
    // especially in Mathematica PacletManager code. Code depends on the assumption that all these
    // exceptions either have httpStatusCode != SC_OK, or they wrap another exception.
    
    public WAHttpException(int httpStatusCode) {
        this.httpStatusCode = httpStatusCode;
    }

    public WAHttpException(Throwable arg0) {
        super(arg0);
    }

    
    public String getMessage() {
        if (httpStatusCode != HttpStatus.SC_OK) {
            return statusCodeToMessageString(httpStatusCode);
        } else {
            return super.getMessage();
        }
    }
    
    
    private static String statusCodeToMessageString(int httpStatusCode) {
        switch (httpStatusCode) {
            case 404:
                return "HTTP Error 404: File not found on server";
            case 503:
                return "HTTP Error 503: Service unavailable";
            default:
                return "HTTP Error " + String.valueOf(httpStatusCode);
        }
    }
}
