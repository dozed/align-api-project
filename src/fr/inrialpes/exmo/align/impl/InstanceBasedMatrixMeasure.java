/*
 * $Id: InstanceBasedMatrixMeasure.java 1681 2012-02-16 10:11:59Z euzenat $
 *
 * Copyright (C) INRIA, 2010
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
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

package fr.inrialpes.exmo.align.impl; 

// Alignment API classes
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;

// Alignment API implementation classes
import fr.inrialpes.exmo.align.impl.MatrixMeasure;

import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

// Java standard classes
import java.util.Set;
import java.util.Properties;
import java.util.Vector;

/**
 * InstanceBasedMatrixMeasure
 *
 * This is a generic distance store in which
 * - a distance between instances is computed and stored in the table
 * - the classical distance between classes is computed from the distance between
 *   instances through classical measure (linkage, etc.).
 * - the rest is as usual
 * 
 * The only method to implement is computeInstanceDistance( params ).
 * For additional flexibility, initialize( LoadedOntology onto1, LoadedOntology onto2, Alignment align ) can be refined.
 * Note that it uses HeavyLoadedOntology.
 *
 */

public abstract class InstanceBasedMatrixMeasure extends MatrixMeasure {

    Set<Object>[] classinst1 = null;
    Set<Object>[] classinst2 = null;

    public InstanceBasedMatrixMeasure() {
	similarity = false; // This is a distance
    };

    
    @SuppressWarnings({"unchecked"})
	    public void initialize( LoadedOntology onto1, LoadedOntology onto2, Alignment align ) {
	// create the matrices and all structures
	super.initialize( onto1, onto2, align );
	try {
	    if ( !(onto1 instanceof HeavyLoadedOntology) 
		 || !(onto2 instanceof HeavyLoadedOntology) )
		throw new AlignmentException( "InstanceBasedMatrixMeasure requires HeavyLoadedOntology");
	    HeavyLoadedOntology ontology1 = (HeavyLoadedOntology)onto1;
	    HeavyLoadedOntology ontology2 = (HeavyLoadedOntology)onto2;

	    // Normalise class comparators (which instance belongs to which class)
	    classinst1 = new Set[nbclass1];
	    for( Object cl1 : ontology1.getClasses() ) {
		classinst1[ classlist1.get( cl1 ).intValue() ] = ontology1.getInstances( cl1, OntologyFactory.LOCAL, OntologyFactory.FULL, OntologyFactory.NAMED );
	    }
	    classinst2 = new Set[nbclass2];
	    for( Object cl2 : ontology2.getClasses() ) {
		classinst2[ classlist2.get( cl2 ).intValue() ] = ontology2.getInstances( cl2, OntologyFactory.LOCAL, OntologyFactory.FULL, OntologyFactory.NAMED );
	    }
	} catch (OntowrapException owex) {
	    owex.printStackTrace();
	} catch (AlignmentException alex) {
	    alex.printStackTrace();
	}

    }

    public void compute( Properties params ) {
	// First compute the distance on instances
	computeInstanceDistance( params );
	// Then compute class distances wrt parameters
	computeClassDistance( params );
    }

    /**
     * This is the empty method of this abstract class
     * it must compute the instance distance
     * and fill the adequate indmatrix with these distances.
     */
    public abstract void computeInstanceDistance( Properties params );

    public void computeClassDistance( Properties params ) {
	String cmeasure = params.getProperty( "cmeasure" );
	if ( cmeasure != null && !cmeasure.equals( "singlel" ) ) {
	    if ( cmeasure.equals( "fulll" ) ) {
		computeFullLinkage();
	    } else if ( cmeasure.equals( "averagel" ) ) {
		computeAverageLinkage();
	    } else if ( cmeasure.equals( "hausdorff" ) ) {
		computeHausdorffDistance();
	    }
	} else {
	    computeSingleLinkage();
	}
    }
	
    private void computeSingleLinkage() {
	for ( int i=0; i < nbclass1; i++ ) {
	    for ( int j=0; j < nbclass2; j++ ) {
		double min = 1.;
		for ( Object in1 : classinst1[i] ) {
		    for ( Object in2 : classinst2[j] ) {
			double val = indmatrix[indlist1.get( in1 ).intValue()][indlist2.get( in2 ).intValue()];
			if ( val < min ) { min = val; }
		    }
		}
		clmatrix[i][j] = min;
	    }
	}
    }

    private void computeFullLinkage() {
	for ( int i=0; i < nbclass1; i++ ) {
	    for ( int j=0; j < nbclass2; j++ ) {
		double max = 0.;
		for ( Object in1 : classinst1[i] ) {
		    for ( Object in2 : classinst2[j] ) {
			double val = indmatrix[indlist1.get( in1 ).intValue()][indlist2.get( in2 ).intValue()];
			if ( val > max ) { max = val; }
		    }
		}
		clmatrix[i][j] = max;
	    }
	}
    }
    private void computeAverageLinkage() {
	for ( int i=0; i < nbclass1; i++ ) {
	    for ( int j=0; j < nbclass2; j++ ) {
		int nbval = 0;
		double dist = 0.;
		for ( Object in1 : classinst1[i] ) {
		    for ( Object in2 : classinst2[j] ) {
			nbval++;
			dist += indmatrix[indlist1.get( in1 ).intValue()][indlist2.get( in2 ).intValue()];
		    }
		}
		if ( nbval == 0 ) clmatrix[i][j] = 1.0;
		else clmatrix[i][j] = dist/(double)nbval;
	    }
	}
    }

    private void computeHausdorffDistance() {
	for ( int i=0; i < nbclass1; i++ ) {
	    for ( int j=0; j < nbclass2; j++ ) {
		if ( classinst1[i].size() == 0 && classinst2[j].size() == 0 ) clmatrix[i][j] = 1.;
		double max = 0.;
		for ( Object in1 : classinst1[i] ) {
		    double min = 1.;
		    for ( Object in2 : classinst2[j] ) {
			double val = indmatrix[indlist1.get( in1 ).intValue()][indlist2.get( in2 ).intValue()];
			if ( val < min ) min = val;
		    }
		    if ( min > max ) max = min;
		}
		for ( Object in2 : classinst2[j] ) {
		    double min = 1.;
		    for ( Object in1 : classinst1[i] ) {
			double val = indmatrix[indlist1.get( in1 ).intValue()][indlist2.get( in2 ).intValue()];
			if ( val < min ) min = val;
		    }
		    if ( min > max ) max = min;
		}
		clmatrix[i][j] = max;
	    }
	}
    }

    // These are useless because "compute" is overridden
    public double measure( Object cl1, Object cl2 ) throws Exception {
	return 0.;
    }
    public double classMeasure( Object cl1, Object cl2 ) throws Exception {
	return 0.;
    }
    public double propertyMeasure( Object pr1, Object pr2 ) throws Exception {
	return 0.;
    }
    public double individualMeasure( Object id1, Object id2 ) throws Exception {
	//if ( debug > 4 ) System.err.println( "ID:"+id1+" -- "+id2);
	// compute edit distance between both norms
	//norm1[indlist1.get(ob1).intValue()], norm2[indlist2.get(ob2).intValue()]
	return 0.;
    }

}

