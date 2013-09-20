/*
 * $Id: ValueConstraint.java 1662 2012-01-14 15:47:39Z euzenat $
 *
 * Copyright (C) INRIA, 2010, 2012
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

// JE: 2012 this class seems not used anywhere
public class ValueConstraint implements Cloneable {

    // JE: 2010 this must be replaced by path-or-value
    Value value = null;
    Comparator comparator;

    public ValueConstraint() {
	super();
    }

    public ValueConstraint( Value v, Comparator comp ) {
	super();
	value = v;
	comparator = comp;
    }    

    /*
      public void accept( EDOALVisitor visitor ) throws AlignmentException {
	visitor.visit(this);
    }
    */

}
