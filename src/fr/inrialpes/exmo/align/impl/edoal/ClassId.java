/*
 * $Id: ClassId.java 1710 2012-03-23 19:53:25Z euzenat $
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.5 - 2006
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

import java.util.Collection;

import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor;
import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor.TYPE;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * A simple Id to represent a Class.
 * 
 */
public class ClassId extends ClassExpression implements Id {

    /** Holds the identifier. */
    private String id;
    
    URI uri;

    /**
     * Creates an anonymous class pattern
     */
    public ClassId() {}

    public ClassId(final String id) throws AlignmentException {
	if (id == null) {
	    throw new NullPointerException("The id must not be null");
	}
	if (id.length() <= 0) {
	    throw new IllegalArgumentException(
					       "The id must be longer than 0 characters");
	}
	this.id = id;
	try {
	    uri = new URI( id );
	} catch ( URISyntaxException mfuex ) {
	    throw new AlignmentException( "Not an URI "+id, mfuex );
	}
    }
    
    public ClassId( final URI u ) {
	if ( u == null) {
	    throw new NullPointerException("The URI must not be null");
	}
	this.uri = u;
	id = u.toString();
    }
    
    public void accept( EDOALVisitor visitor ) throws AlignmentException {
	visitor.visit( this );
    }
    public TYPE accept( TypeCheckingVisitor visitor  ) throws AlignmentException {
	return visitor.visit(this);
    }

    public URI getURI(){
	return uri;
    }

    public String plainText() {
	return id;
    }
    
    /**
     * <p>
     * Returns a simple description of this object. <b>The format of the
     * returned String is undocumented and subject to change.</b>
     * <p>
     * <p>
     * An expamle return String could be:
     * <code>classId: http://my/super/class</code>
     * </p>
     */
    public String toString() {
	return "classId: " + id;
    }
    
    public int hashCode() {
	int result = 17;
	result = result * 37 + id.hashCode();
	return result;
    }
    
    public boolean equals(final Object obj) {
	if (obj == this) {
	    return true;
	}
	if (!(obj instanceof ClassId)) {
	    return false;
	}
	ClassId i = (ClassId) obj;
	return id.equals(i.id);
    }
    
    /*
    public Object clone() {
	return super.clone();
    }
    */
}
    
