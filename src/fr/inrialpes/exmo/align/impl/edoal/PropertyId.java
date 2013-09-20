/*
 * $Id: PropertyId.java 1710 2012-03-23 19:53:25Z euzenat $
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.7 - 2006 -- then AttributeExpr.java
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

import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor;
import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor.TYPE;

/**
 * <p>
 * A simple Id to represent a Property.
 * </p>
 * 
 * @version $Id: PropertyId.java 1710 2012-03-23 19:53:25Z euzenat $
 */
public class PropertyId extends PropertyExpression implements Id {
    /** Holds the identifier. */
    private String id;
	
    URI uri;

    /**
     * Creates an anonymous property pattern
     */
    public PropertyId() {}

    public PropertyId( final URI u ) {
	if ( u == null ) {
	    throw new NullPointerException("The URI must not be null");
	}
	uri = u;
	id = u.toString();
    }
	
    public void accept( EDOALVisitor visitor ) throws AlignmentException {
	visitor.visit( this );
    }
    public TYPE accept(TypeCheckingVisitor visitor) throws AlignmentException {
	return visitor.visit(this);
    }

    public URI getURI(){
	return uri;
    }

    public String plainText() {
	return toString();
    }

    /**
     * <p>
     * Returns a simple description of this object. <b>The format of the
     * returned String is undocumented and subject to change.</b>
     * <p>
     * <p>
     * An expamle return String could be:
     * <code>attributeId: http://my/super/attribute</code>
     * </p>
     */
    public String toString() {
	return "PropertyId: " + id;
    }
	
}
