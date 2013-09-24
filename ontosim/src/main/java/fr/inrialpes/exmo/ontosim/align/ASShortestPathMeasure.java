/**
 * $Id: ASShortestPathMeasure.java 111 2011-05-27 09:18:55Z jdavid $
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

import java.net.URI;
import java.util.Hashtable;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.OntologyNetwork;

import fr.inrialpes.exmo.ontosim.OntoSimException;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.Ontology;

/**
 * A measure between ontologies in the alignment space depending of the length of the shortest path between the ontologies.
 * This measure is directed, i.e., it is not symmetric
 */
public class ASShortestPathMeasure extends AbstractAlignmentSpaceMeasure<LoadedOntology<?>> {

    public static enum NORM { diameter, cardinal };

    private boolean inited = false;
    protected NORM modality = NORM.cardinal;
    protected int size = 0;
    protected int norm = 0;
    protected int diameter = 0;
    protected Hashtable<URI,Integer> index;
    protected int[][] matrix;

    public ASShortestPathMeasure( OntologyNetwork noo ){
	super( noo );
	inited = false;
    }

    public ASShortestPathMeasure() {
	super();
	inited = false;
    }

    public TYPES getMType(){
	return TYPES.distance;
    };

    public NORM getNormModality() { return modality; };
    public void setNormModality( NORM mod ) { 
	modality = mod;
	if ( modality == NORM.diameter ) norm = diameter+1;
	else norm = size;
    };

    public int getValue( Ontology o1, Ontology o2 ) {
	if ( !inited ) init();
	try {
	    int i = index.get( o1.getURI() ).intValue();
	    int j = index.get( o2.getURI() ).intValue();
	    return matrix[i][j];
	}
	catch (NullPointerException e ) { return size;}
    }
   
    public void setAlignmentSpace(OntologyNetwork noo) {
	super.setAlignmentSpace(noo);
	inited=false;
    }

    public double getMeasureValue( LoadedOntology<?> o1, LoadedOntology<?> o2 ) {
	int v = getValue( o1, o2 );
	if ( v == size ) return 1.;
	else return (double)v / (double)norm;
    };

    public double getSim( LoadedOntology<?> o1, LoadedOntology<?> o2 ) {
	return 1.-getMeasureValue( o1, o2 );
    };

    public double getDissim( LoadedOntology<?> o1, LoadedOntology<?> o2 ) {
	return getMeasureValue( o1, o2 );
    };

    /**
     * This function does compute the values for all 
     */
    private void init() throws OntoSimException {
	// create index
	index = new Hashtable<URI,Integer>();
	int max = 0;
	size = 0;
	for ( URI ont : network.getOntologies() ) {
	    index.put( ont, new Integer( size ) );
	    size++;
	}
	// create matrix
	matrix = new int[size][size];
	// initialise with all 
	for ( int i = 0; i < size ; i++ ) {
	    for ( int j = 0; j < size; j++ ) {
		if ( i == j ) matrix[i][j] = 0;
		else matrix[i][j] = size; // no path
	    }
	}
	// initialise with all alignments
	for ( Alignment al : network.getAlignments() ) {
	    max = 1;
	    try {
		int i = index.get( al.getOntology1URI() ).intValue();
		int j = index.get( al.getOntology2URI() ).intValue();
		matrix[i][j] = 1; // direct alignment
	    } catch (AlignmentException aex) {
		throw new OntoSimException( "Cannot find ontology URI", aex );
	    }
	}
	// compute closure
	boolean modified = true;
	int it = 1; // the number of iterations so far
	while ( modified ) {
	    it++;
	    modified = false;
	    for ( int i = 0; i < size ; i++ ) {
		for ( int j = 0; j < size; j++ ) {
		    if ( i != j && matrix[i][j] > it ) { // won't do better
			for ( int k = 0; k < size; k++ ) {
			    if ( matrix[i][k] + matrix[k][j] < matrix[i][j] ) {
				modified = true;
				matrix[i][j] = matrix[i][k] + matrix[k][j];
				if ( matrix[i][j] > max ) max = matrix[i][j];
			    }
			}
		    }
		}
	    }
	}
	// The length of the longest path
	diameter = max;
	if ( modality == NORM.diameter ) norm = diameter+1;
	else norm = size;
	// no need to redo
	inited = true;
    }
}
