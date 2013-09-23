/*
 * $Id: Value.java 1710 2012-03-23 19:53:25Z euzenat $
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.2 - 2006
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

import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor;
import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor.TYPE;

/**
 * <p>
 * Id to represent a simple valuestring.
 * </p>
 * 
 * @version $Id: Value.java 1710 2012-03-23 19:53:25Z euzenat $
 */
public class Value implements ValueExpression { //implements Cloneable, Visitable {

    /** Holds the value */
    private String value;

    /** The eventual type of the value */
    private URI type;

    /**
     * Constructs an object with the given value.
     * 
     * @param value
     *            the value for this object.
     * @throws NullPointerException
     *             if the value is {@code null}
     * @throws IllegalArgumentException
     *             if the value isn't longer than 0
     */
    public Value( final String value ) {
	if (value == null) {
	    throw new NullPointerException("The value should not be null");
	}
	this.value = value;
    }

    public Value( final String value, final URI type ) {
	if (value == null) {
	    throw new NullPointerException("The value should not be null");
	}
	if (type == null) {
	    throw new NullPointerException("The type is null");
	}
	this.value = value;
	this.type = type;
    }

    public void accept( EDOALVisitor visitor ) throws AlignmentException {
	visitor.visit(this);
    }
    public TYPE accept( TypeCheckingVisitor visitor ) throws AlignmentException {
	return visitor.visit(this);
    }

    public String getValue() {
	return value;
    }

    public URI getType() {
	return type;
    }

    public int hashCode() {
	return 5*value.hashCode();
    }

    public boolean equals(final Object o) {
	if ( o == this ) {
	    return true;
	}
	if (!(o instanceof Value)) {
	    return false;
	}
	Value s = (Value) o;
	return value.equals(s.value);
    }
    /*
    public Object clone() {
	return super.clone();
    }
    */

    /**
     * <p>
     * Returns a short description about this object. <b>The format of the
     * returned string is undocumentd and subject to change.</b>
     * </p>
     * <p>
     * An example return string could be: {@code 15}
     * </p>
     */
    public String toString() {
	return value;
    }
}
