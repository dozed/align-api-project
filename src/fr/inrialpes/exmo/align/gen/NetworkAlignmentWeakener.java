/*
 * $Id: NetworkAlignmentWeakener.java 1659 2011-12-28 10:50:46Z euzenat $
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

import java.util.ArrayList;
import java.util.Collections;
import java.util.Properties;
import java.net.URI;

/**
 * AlignmentWeakener
 *
 * suppress n% of the correspondences at random in each alignments
 * n is a number between 0. and 1.
 * Returns a brand new BasicOntologyNetwork (with new alignments and cells)
 * the @threshold parameter tells if the corrrespondences are suppressed at random (false) of by suppressing the n% of lower confidence (true)
 */

public class NetworkAlignmentWeakener implements OntologyNetworkWeakener {

    public OntologyNetwork weaken( OntologyNetwork on, int n, Properties p ) throws AlignmentException {
	throw new AlignmentException( "Cannot weaken alignments by fixed number of correspondences" );
    }

    public OntologyNetwork weaken( OntologyNetwork on, double n, Properties p ) throws AlignmentException {
	if ( on == null ) throw new AlignmentException( "cannot weaken null ontology network" );
	if ( n < 0. || n > 1. )
	    throw new AlignmentException( "Argument must be between 0 and 1.: "+n );
	boolean threshold = (p != null && p.getProperty("threshold") != null);
	OntologyNetwork newon = new BasicOntologyNetwork();
	for ( URI ontouri : on.getOntologies() ){
	    newon.addOntology( ontouri );
	}
	for ( Alignment al : on.getAlignments() ){
	    Alignment newal = (Alignment)al.clone();
	    if ( threshold ) {
		newal.cut( "perc", 1.-n );
	    } else {
		int size = newal.nbCells();
		// --------------------------------------------------------------------
		// JE: Here is a tricky one.
		// Using collection schuffle randomly reorganises a list
		// Then choosing the fist n% and destroying them in the Set is performed
		// The complexity is O(copy=N)+O(shuffle=N)+n%*O(delete=N)
		// That's not bad... (and also avoid checking if the same nb is drawn)
		// But in practice other solutions may be better, like:
		// Generating randomly n%*N numbers between 0 and N (util.Random)
		// Ordering them 
		// Traversing the initial structure and deleting the choosen ones...
		// This one (deleting when traversing) is tricky in Java.
		// --------------------------------------------------------------------
		ArrayList<Cell> array = new ArrayList<Cell>( size );
		for ( Cell c : newal ) {
		    array.add( c );
		}
		Collections.shuffle( array );
		for ( int i = (int)(n*size)-1; i >= 0; i-- ){
		    newal.remCell( array.get( i ) );
		}
	    }
	    newon.addAlignment( newal );
	}
	return newon;
    }

}
