/*
 * $Id: QueryTypeMismatchException.java 384 2007-02-02 11:09:40Z euzenat $
 *
 * Copyright (C) INRIA Rhône-Alpes, 2006-2007
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

/*
 * QueryTypeMismatchException.java
 *
 * Created on March 20, 2006, 11:03 AM
 * 
 */

package fr.inrialpes.exmo.queryprocessor;

/**
 *
 * @author Arun Sharma
 */
public class QueryTypeMismatchException extends Exception {

    static final long serialVersionUID = 200;
    /**
     * Create a new <code>QueryTypeMismatchException</code> with no
     * detail mesage.
     */
    public QueryTypeMismatchException() {
    	super();
    }

    /**
     * Create a new <code>QueryTypeMismatchException</code> with
     * the <code>String</code> specified as an error message.
     *
     * @param message The error message for the exception.
     */
    public QueryTypeMismatchException(String message) {
    	super(message);
    }

    /**
     * Create a new <code>QueryTypeMismatchException</code> with
     * the <code>String</code> specified as an error message and the
     * <code>Throwable</code> that caused this exception to be raised.
     * @param message The error message for the exception
     * @param cause The cause of the exception
     */
    public QueryTypeMismatchException(String message, Throwable cause) {
    	super(message, cause);
    }
}
