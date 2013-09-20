/*
 * $Id: Comparator.java 1724 2012-05-09 20:35:49Z euzenat $
 *
 * Copyright (C) INRIA, 2009-2010, 2012
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

package fr.inrialpes.exmo.align.impl.edoal;

import java.net.URI;
import java.net.URISyntaxException;

import fr.inrialpes.exmo.align.impl.Namespace;

public class Comparator {

    URI uri = null;

    // This replaces the Comparator class
    // SHOULD CERTAINLY BE AN ENUM
    public static Comparator EQUAL = initComparator( Namespace.EDOAL.prefix+"equals", 0 );
    public static Comparator LOWER = initComparator( Namespace.EDOAL.prefix+"lower-than", -1 );
    public static Comparator GREATER = initComparator( Namespace.EDOAL.prefix+"greater-than", 1 );

    public static Comparator getComparator( URI u ) {
	if ( u.equals( EQUAL.getURI() ) ) {
	    return EQUAL;
	} else if ( u.equals( LOWER.getURI() ) ) {
	    return LOWER;
	} else if ( u.equals( GREATER.getURI() ) ) {
	    return GREATER;
	} else return new Comparator( u );
    }

    protected Comparator() {
	super();
    }

    public Comparator( URI u ) {
	super();
	uri = u;
    }

    private static Comparator initComparator( String uri, int rank ){
	try {
	    return new Comparator( new URI( uri ) );
	} catch ( URISyntaxException usex ) {
	    return new Comparator();
	}
    }
    
    public URI getURI() {
	return uri;
    }

}
