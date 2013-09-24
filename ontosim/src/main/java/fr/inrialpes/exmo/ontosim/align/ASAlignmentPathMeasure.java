/**
 * $Id: ASAlignmentPathMeasure.java 111 2011-05-27 09:18:55Z jdavid $
 *
 *   Copyright 2009 INRIA, Université Pierre Mendès France
 *   
 *   $filename$ is part of OntoSim.
 *
 *   OntoSim is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   OntoSim is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with OntoSim; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package fr.inrialpes.exmo.ontosim.align;

import org.semanticweb.owl.align.OntologyNetwork;

import fr.inrialpes.exmo.ontowrap.LoadedOntology;

/**
 * This class implements measures depending on the existence of an path between the ontologies in the alignment space.
 * It is implemented on top of the "shortest path" measure, since this does not really cost more and this is easier to maintain
 */
public class ASAlignmentPathMeasure extends ASShortestPathMeasure {

    public ASAlignmentPathMeasure(){
	super();
    }
    
    public ASAlignmentPathMeasure( OntologyNetwork noo ){
	super( noo );
    }

    public double getMeasureValue( LoadedOntology<?> o1, LoadedOntology<?> o2 ) {
	int v = getValue( o1, o2 );
	if ( v == 0. ) return 0;
	else if ( v == 1. ) return 1./3.;
	else if ( v < norm ) return 2./3.; // a path exists
	else return 1.;
    }
}
