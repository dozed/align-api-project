/*
 * $Id: ClassTypeRestriction.java 1710 2012-03-23 19:53:25Z euzenat $
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

import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor;
import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor.TYPE;

/**
 * <p>
 * Represents a type typeCondition tag for PropertyExpressions.
 * </p>
 * <p>
 * Created on 24-Mar-2005 Committed by $Author: poettler_ric $
 * </p>
 * 
 * @version $Id: ClassTypeRestriction.java 1710 2012-03-23 19:53:25Z euzenat $
 */
public class ClassTypeRestriction extends ClassRestriction implements Cloneable {

    Datatype type = null;
    boolean universal = true;

    /**
     * Constructs a ClassTypeRestriction with the given restriction.
     * 
     * @param p
     *            the restricted PathExpression
     * @param pred
     * wether the constraint is universal (true) or existential (false)
     * @param t
     *            the Datatype to which this path is restricted
     * @throws NullPointerException
     *             if the restriction is null
     * NOTE: Currently the predicate is not visible in the syntax which only
     * authorises type (so universal)
     */
    public ClassTypeRestriction(final PathExpression p,
				  final boolean pred,
				final Datatype t) {
	super(p);
	// Check that this is a property
	universal = pred;
	type = t;
    }

    public ClassTypeRestriction(final PathExpression p,
				final Datatype t) {
	super(p);
	// Check that this is a property
	type = t;
    }

    public void accept( EDOALVisitor visitor ) throws AlignmentException {
	visitor.visit( this );
    }
    public TYPE accept( TypeCheckingVisitor visitor  ) throws AlignmentException {
	return visitor.visit(this);
    }

    public Datatype getType() {
	return type;
    }

    public void setType( Datatype t ) {
	type = t;
    }

    public boolean isUniversal() {
	return universal;
    }
}
