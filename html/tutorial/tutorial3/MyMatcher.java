/*
 * $Id: MyMatcher.java 1311 2010-03-07 22:51:10Z euzenat $
 *
 * Copyright (C) INRIA, 2010
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

//package my.domain;

import java.net.URI;
import java.lang.Double;
import java.lang.Iterable;
import java.util.Set;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentException;
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;

// This is a simple matcher
// Which is based on a standard matcher of the ALignment API

public class MyMatcher implements Iterable<Object[]> {

    Set<Object[]> result;

    public MyMatcher() {};

    public void match( URI u1, URI u2 ) {
	result = new HashSet<Object[]>();
	// Create matcher
	AlignmentProcess al = new StringDistAlignment();
	try {
	    al.init( u1, u2 );
	    // Run matcher
	    al.align( (Alignment)null, new Properties() );
	    // Extract result
	    for ( Cell c : al ) {
		Object[] r = new Object[4];
		r[0] = c.getObject1AsURI( al );
		r[1] = c.getObject2AsURI( al );
		r[2] = c.getRelation().toString();
		r[3] = new Double( c.getStrength() );
		result.add( r );
	    }
	} catch (AlignmentException ex) {
	    ex.printStackTrace();
	}
    }

    public Iterator<Object[]> iterator() {
	return result.iterator();
    }


}

