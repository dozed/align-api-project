/*
 * $Id: Datatype.java 1712 2012-03-24 14:03:15Z euzenat $
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

import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor;
import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor.TYPE;

/**
 * <p>
 * Id to represent a datatype
 * </p>
 */

public class Datatype { //implements Cloneable

    /** Holds the type */
    private String type;

    /**
     * Constructs an object with the given type.
     * 
     * @param type
     *            the type for this object.
     * @throws NullPointerException
     *             if the type is {@code null}
     * @throws IllegalArgumentException
     *             if the type isn't longer than 0
     */
    public Datatype( final String type ) {
	if (type == null) {
	    throw new NullPointerException("The type must not be null");
	}
	this.type = type;
    }

    public void accept( EDOALVisitor visitor) throws AlignmentException {
        visitor.visit( this );
    }
    public TYPE accept( TypeCheckingVisitor visitor  ) throws AlignmentException {
	return visitor.visit(this);
    }

    public String getType() {
	return type;
    }

    public int hashCode() {
	return type.hashCode();
    }

    public boolean equals(final Object o) {
	if ( o == this ) {
	    return true;
	}
	if (!(o instanceof Datatype)) {
	    return false;
	}
	Datatype s = (Datatype) o;
	return type.equals(s.type);
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
	return type;
    }
}
