/*
 * $Id: GenericReflectiveVisitor.java 1669 2012-01-31 16:43:38Z euzenat $
 *
 * Copyright (C) INRIA, 2012
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.impl.renderer; 

import java.lang.reflect.Method;
import java.lang.reflect.InvocationTargetException;

import org.semanticweb.owl.align.AlignmentException;

/**
 * This class offers the tools for implementing Reflective visitors,
 * i.e., visitors in which the visit method will depend on the actual
 * class of the visited object.
 *
 * This is useful for the AlignmentVisitors because: Alignment, Cell
 * and Relation may be extended.
 */
public class GenericReflectiveVisitor {

    /**
     * Finds the visit(X) method corresponding to the object class (subclass of a root class)
     * Look-up for X in the superclass of c (up to root, otherwise it loops)
     * If not found, look it up in the implemented interface
     * (there may be such a method for interfaces)
     */
    protected Method getMethod( Class c, Class root ) {
	Class  newc = c;
	Method m    = null;
	while ( m == null  &&  newc != root && newc != null ) { // null father of Object.class?
	    String method = newc.getName();
	    method = "visit";// + method.substring( method.lastIndexOf('.') + 1 );
	    try {
		m = getClass().getMethod( method, new Class[] { newc } );
	    } catch ( NoSuchMethodException ex ) {
		newc = newc.getSuperclass();
	    }
	}
	if ( m == null ) {//newc == Object.class ) {
	    // System.out.println( "Searching for interfaces" );
	    Class[] interfaces = c.getInterfaces();
	    for ( int i=0; i < interfaces.length; i++ ) {
		if ( interfaces[i] != root ) {
		    String method = interfaces[i].getName();
		    //method = "visit" + method.substring( method.lastIndexOf('.') + 1 );
		    try {
			m = getClass().getMethod( method, new Class[] { interfaces[i] } );
		    } catch ( NoSuchMethodException ex ) { }
		}
	    }
	}
	/*
	if ( m == null )
	    try {
		//m = getClass().getMethod( "visitObject", new Class[] { Object.class } );
		m = getClass().getMethod( "visit", new Class[] { Object.class } );
	    } catch (Exception ex) { }
	*/
	return m;
    }

    public boolean subsumedInvocableMethod( Object visitor, Object o, Class cl ) throws AlignmentException {
	Method method = getMethod( o.getClass(), cl );
	if ( method != null ) {
	    try {
		method.invoke( visitor, new Object[] {o} );
		return true;
	    } catch ( IllegalAccessException iaex ) {
		iaex.printStackTrace();
	    } catch ( InvocationTargetException itex ) { 
		if ( itex.getCause() instanceof AlignmentException ) {
		    throw (AlignmentException)itex.getCause();
		} else {
		    itex.printStackTrace();
		}
	    }
	}
	return false;
    }

}
