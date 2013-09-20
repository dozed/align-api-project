/*
 * $Id: PropertyValueRestriction.java 1710 2012-03-23 19:53:25Z euzenat $
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

import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor;
import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor.TYPE;

/**
 * <p>
 * Represents a attributeValueRestriction tag for a ClassExpressions.
 * </p>
 * <p>
 * Created on 24-Mar-2005 Committed by $Author: poettler_ric $
 * </p>
 * 
 * @version $Id: PropertyValueRestriction.java 1710 2012-03-23 19:53:25Z euzenat $
 */
public class PropertyValueRestriction extends PropertyRestriction implements Cloneable {

    Comparator comparator = null;
    ValueExpression value = null;

    /**
     * Constructs a simple PropertyValueRestriction
     * 
     * @throws NullPointerException
     *             if the restriction is null
     */
    public PropertyValueRestriction() {
	super();
    }

    /**
     * Constructs a PropertyValueRestriction with the given restriction.
     * 
     * @param comp
     *            the comparator between the restricted property and the restricting value
     * @param v
     *            the target restricting value
     * @throws NullPointerException
     *             if the restriction is null
     */
    public PropertyValueRestriction( final Comparator comp, final ValueExpression v ) {
	super();
	comparator = comp;
	value = v;
    }

    public void accept( EDOALVisitor visitor ) throws AlignmentException {
	visitor.visit( this );
    }
    public TYPE accept( TypeCheckingVisitor visitor ) throws AlignmentException {
	return visitor.visit(this);
    }

    public Comparator getComparator(){
	return comparator;
    }
    
    public void setComparator( Comparator comp ){
	comparator = comp;
    }
    
    public ValueExpression getValue(){
	return value;
    }
    public void setValue( ValueExpression v ){
	value = v;
    }
    
    /*
    public Object clone() {
	return super.clone();
    }
    */
}
