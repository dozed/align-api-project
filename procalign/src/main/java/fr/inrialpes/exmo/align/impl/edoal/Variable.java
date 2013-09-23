/*
 * $Id: Variable.java 1710 2012-03-23 19:53:25Z euzenat $
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

package fr.inrialpes.exmo.align.impl.edoal;

import java.util.Set;
import java.util.HashSet;

/**
 * A simple Id to represent a Instance.
 * 
 */

public class Variable {

    private String name;

    private Set<Expression> occurences;

    /**
     * Constructs a Variable
     * 
     * @param name
     *            the name of the variable
     */
    public Variable( final String name ) {
	if ( name == null ) throw new NullPointerException("The name must not be null");
	this.name = name;
	occurences = new HashSet<Expression>();
    }
    
    public String name() { return name; }

    public void addOccurence( Expression expr ) { occurences.add( expr ); }

    /**
     * Returns the Id.
     * 
     * @return the id.
     */
    public String plainText() {
	return toString();
    }
    
    /**
     * <p>
     * Returns a simple description of this object. <b>The format of the
     * returned String is undocumented and subject to change.</b>
     * </p>
     */
    public String toString() {
	return "variable: " + name;
    }

}
