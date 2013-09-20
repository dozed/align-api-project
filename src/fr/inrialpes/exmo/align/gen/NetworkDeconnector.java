/*
 * $Id: NetworkDeconnector.java 1659 2011-12-28 10:50:46Z euzenat $
 *
 * Copyright (C) INRIA, 2009-2011
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package fr.inrialpes.exmo.align.gen;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.OntologyNetwork;

import fr.inrialpes.exmo.align.impl.BasicOntologyNetwork;

import java.util.Properties;

/**
 * NetworkDeconnector
 *
 * suppress alignments in the ontology network so that it retain n-connectivity,
 * i.e., any pairs of ontologies connected by less than n alignments
 * are still connected through at most n alignments. 
 * JE: this is an interesting graph theoretic problem and I do not know where
 * to find it.
 * 
 * NOT IMPLEMENTED
 */

public class NetworkDeconnector implements OntologyNetworkWeakener {

    public OntologyNetwork weaken( OntologyNetwork on, int n, Properties p ) throws AlignmentException {
	if ( on == null ) throw new AlignmentException( "cannot weaken null ontology network" );
	return on;
    }
    public OntologyNetwork weaken( OntologyNetwork on, double n, Properties p )  throws AlignmentException {
	if ( on == null ) throw new AlignmentException( "cannot weaken null ontology network" );
	if ( n < 0. || n > 1. )
	    throw new AlignmentException( "Argument must be between 0 and 1.: "+n );
	return on;
    }
}
