/*
 * $Id: ClassDomainRestriction.java 1710 2012-03-23 19:53:25Z euzenat $
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

public class ClassDomainRestriction extends ClassRestriction implements Cloneable {

    ClassExpression domain = null;
    boolean universal = true;

    /**
     * Constructs a typeCondition with the given restriction.
     * 
     * @param p
     *            the PathExpression to which the restriction applies
     * @param pred
     * wether the constraint is universal (true) or existential (false)
     * @param cl
     *            the ClassExpression restricting the domain
     * @throws NullPointerException
     *             if the restriction is null
     */
    public ClassDomainRestriction(final PathExpression p,
				  final boolean pred,
				  final ClassExpression cl) {
	super(p);
	// Check that this is a property
	universal = pred;
	domain = cl;
    }

    public ClassDomainRestriction(final PathExpression p,
				final ClassExpression cl) {
	super(p);
	// Check that this is a property
	domain = cl;
    }

    public void accept( EDOALVisitor visitor ) throws AlignmentException {
	visitor.visit( this );
    }
    public TYPE accept( TypeCheckingVisitor visitor  ) throws AlignmentException {
	return visitor.visit(this);
    }

    public ClassExpression getDomain() {
	return domain;
    }

    public void setDomain( ClassExpression cl ) {
	domain = cl;
    }

    public boolean isUniversal() {
	return universal;
    }

}
