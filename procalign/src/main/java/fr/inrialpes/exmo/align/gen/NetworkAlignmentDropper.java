/*
 * $Id: NetworkAlignmentDropper.java 1659 2011-12-28 10:50:46Z euzenat $
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

import java.net.URI;
import java.util.Properties;
import java.util.Set;
import java.util.Collections;
import java.util.ArrayList;

/**
 * NetworkAlignmentDropper
 *
 * randomly drops n% of all alignments
 * n is a number between 0. and 1.
 * Returns a brand new BasicOntologyNetwork (with the initial alignments)
 */
public class NetworkAlignmentDropper implements OntologyNetworkWeakener {

    public OntologyNetwork weaken( OntologyNetwork on, double n, Properties p ) throws AlignmentException {
	//System.err.println( " >>>> "+n );
	if ( on == null ) throw new AlignmentException( "cannot weaken null ontology network" );
	if ( n < 0. || n > 1. )
	    throw new AlignmentException( "Argument must be between 0 and 1.: "+n );
	return weaken( on, (int)(n*(double)on.getAlignments().size()), p );
    }

    public OntologyNetwork weaken( OntologyNetwork on, int n, Properties p ) throws AlignmentException {
	//System.err.println( " >>>> "+n );
	if ( on == null ) throw new AlignmentException( "cannot weaken null ontology network" );
	if ( n < 0 )
	    throw new AlignmentException( "Argument must be a positive integer: "+n );
	OntologyNetwork newon = new BasicOntologyNetwork();
	for ( URI ontouri : on.getOntologies() ){
	    newon.addOntology( ontouri );
	}
	Set<Alignment> alignments = on.getAlignments();
	int size = alignments.size();
	if ( n > size ) return newon;
	ArrayList<Alignment> array = new ArrayList<Alignment>( size );
	for ( Alignment al : alignments ){
	    array.add( al );
	}
	Collections.shuffle( array );
	for ( int i = size - n -1; i >= 0; i-- ) {
	    newon.addAlignment( array.get( i ) );
	}
	return newon;
    }
}
