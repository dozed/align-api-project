/*
 * $Id: NonTransitiveImplicationRelation.java 1710 2012-03-23 19:53:25Z euzenat $
¨*
 * Copyright (C) INRIA, 2004-2005, 2008, 2011-2012
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

package fr.inrialpes.exmo.align.impl.rel; 

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;

import fr.inrialpes.exmo.align.impl.BasicRelation;

import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor;

/**
 * Represents a non transitive implication relation.
 * as it can be found in C-OWL and other works
 *
 * @version $Id: NonTransitiveImplicationRelation.java 1710 2012-03-23 19:53:25Z euzenat $ 
 */

public class NonTransitiveImplicationRelation extends BasicRelation
{
    public void accept( AlignmentVisitor visitor) throws AlignmentException {
        visitor.visit( this );
    }
    public void accept( TypeCheckingVisitor visitor ) throws AlignmentException {
	visitor.visit(this);
    }

    static final String prettyLabel = "~>";

    /** Creation **/
    public NonTransitiveImplicationRelation(){
	super(prettyLabel);
    }
    
    private static NonTransitiveImplicationRelation instance = null;

    public static NonTransitiveImplicationRelation getInstance() {
	if ( instance == null ) instance = new NonTransitiveImplicationRelation();
	return instance;
    }

}


