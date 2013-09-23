/*
 * $Id: RelationCoDomainRestriction.java 1710 2012-03-23 19:53:25Z euzenat $
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.4 - 2006
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
 * Represents a coDomainRestriction tag for RelationExpressions.
 * </p>
 * 
 * @version $Id: RelationCoDomainRestriction.java 1710 2012-03-23 19:53:25Z euzenat $
 * 
 */
public class RelationCoDomainRestriction extends RelationRestriction {

    protected ClassExpression codomain = null; 

    /**
     * Constructs a simple RelationCoDomainRestriction
     * 
     * @throws NullPointerException
     *             if the restriction is null
     */
    public RelationCoDomainRestriction() {
	super();
    }
    
    /**
     * Constructs a RelationCoDomainRestriction with the given restriction.
     * 
     * @param cod
     *            the restricting target class expression as codomain
     * @throws NullPointerException
     *             if the restriction is null
     */
    public RelationCoDomainRestriction( final ClassExpression cod ) {
	super();
	codomain = cod;
    }

    public void accept( EDOALVisitor visitor ) throws AlignmentException {
	visitor.visit( this );
    }
    public TYPE accept( TypeCheckingVisitor visitor ) throws AlignmentException {
	return visitor.visit(this);
    }

    public ClassExpression getCoDomain() {
	return codomain;
    }
    
}
