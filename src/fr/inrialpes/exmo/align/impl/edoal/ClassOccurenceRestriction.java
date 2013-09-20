/*
 * $Id: ClassOccurenceRestriction.java 1710 2012-03-23 19:53:25Z euzenat $
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.5 - 2006 -- then AttributeOccurenceCondition.java
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
 * Represents a attributeOccurenceRestriction tag for a ClassExpressions.
 * </p>
 * 
 * @version $Id: ClassOccurenceRestriction.java 1710 2012-03-23 19:53:25Z euzenat $
 */
public class ClassOccurenceRestriction extends ClassRestriction implements Cloneable {

    Comparator comparator = null;
    int occurence = 1;

    /**
     * Constructs a ClassOccurenceRestriction with the given restriction.
     * 
     * @param p
     *            the constrained PathExpression
     * @param c
     *            the restriction Comparator
     * @param n
     *            the number of occurence compared
     * @throws NullPointerException
     *             if the restriction is null
     */
    public ClassOccurenceRestriction( final PathExpression p, Comparator c, int n ) {
	super( p );
	constrainedPath = p;
	comparator = c;
	occurence = n;
    }

    public void accept( EDOALVisitor visitor ) throws AlignmentException {
	visitor.visit( this );
    }
    public TYPE accept(TypeCheckingVisitor visitor) throws AlignmentException {
	return visitor.visit(this);
    }

    public int getOccurence() { return occurence; }
    public void setOccurence( int n ) { occurence = n; }
    public Comparator getComparator() { return comparator; }
    public void setComparator( Comparator c ) { comparator = c; }
    /*
    public Object clone() {
	return super.clone();
    }
    */
}
