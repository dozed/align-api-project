/*
 * $Id: PropertyTypeRestriction.java 1710 2012-03-23 19:53:25Z euzenat $
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.6 - 2006 -- then AttributeTypeCondition.java
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
 * Represents a attributeTypeRestriction tag for a ClassExpressions.
 * </p>
 * <p>
 * Created on 24-Mar-2005 Committed by $Author: poettler_ric $
 * </p>
 * 
 * @version $Id: PropertyTypeRestriction.java 1710 2012-03-23 19:53:25Z euzenat $
 */
public class PropertyTypeRestriction extends PropertyRestriction implements Cloneable {

    Datatype type = null;

    /**
     * Constructs a simple PropertyTypeRestriction
     * 
     * @throws NullPointerException
     *             if the restriction is null
     */
    public PropertyTypeRestriction() {
	super();
    }

    /**
     * Constructs a PropertyTypeRestriction with the given restriction.
     * 
     * @param t
     *            the restricting target datatype
     * @throws NullPointerException
     *             if the restriction is null
     */
    public PropertyTypeRestriction( final Datatype t ) {
	super();
	type = t;
    }

    public void accept( EDOALVisitor visitor ) throws AlignmentException {
	visitor.visit( this );
    }
    public TYPE accept( TypeCheckingVisitor visitor ) throws AlignmentException {
	return visitor.visit(this);
    }

    public Datatype getType() {
	return type;
    }
    public void setType( Datatype t ) {
	type = t;
    }

    /*
    public Object clone() {
	return super.clone();
    }
    */
}
