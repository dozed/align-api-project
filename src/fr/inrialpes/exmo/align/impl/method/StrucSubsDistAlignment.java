/*
 * $Id: StrucSubsDistAlignment.java 1502 2010-08-16 08:13:55Z euzenat $
 *
 * Copyright (C) INRIA, 2003-2004, 2007-2010
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


package fr.inrialpes.exmo.align.impl.method; 

import java.util.Vector;
import java.util.Set;
import java.util.Properties;
import java.lang.Integer;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

import fr.inrialpes.exmo.ontosim.string.StringDistances;

/** This class has been built for ISWC experiments with bibliography.
 * It implements a non iterative (one step) OLA algorithms based on
 * the name of classes and properties. It could be made iterative by
 *  just adding range/domain on properties...
 *  The parameters are:
 *  - threshold: above what do we select for the alignment;
 *  - epsillon [ignored]: for convergence
 *  - pic1: weigth for class name
 *  - pic2: weight for class attributes
 *  - pia1 [ignored=1]: weigth for property name
 *  - pia3 [ignored=0]: weigth for property domain
 *  - pia4 [ignored=0]: weigth for property range
 *
 * @author J�r�me Euzenat
 * @version $Id: StrucSubsDistAlignment.java 1502 2010-08-16 08:13:55Z euzenat $ 
 */

public class StrucSubsDistAlignment extends DistanceAlignment implements AlignmentProcess {

    private HeavyLoadedOntology<Object> honto1 = null;
    private HeavyLoadedOntology<Object> honto2 = null;

    /** Creation **/
    public StrucSubsDistAlignment() {
	setType("**");
    };

    /**
     * Initialisation
     * The class requires HeavyLoadedOntologies
     */
    public void init(Object o1, Object o2, Object ontologies) throws AlignmentException {
	super.init( o1, o2, ontologies );
	if ( !( getOntologyObject1() instanceof HeavyLoadedOntology
		&& getOntologyObject1() instanceof HeavyLoadedOntology ))
	    throw new AlignmentException( "StrucSubsDistAlignment requires HeavyLoadedOntology ontology loader" );
    }

    /** Processing **/
    // Could better use similarity
    public void align( Alignment alignment, Properties params ) throws AlignmentException {
	loadInit( alignment );
	honto1 = (HeavyLoadedOntology<Object>)getOntologyObject1();
	honto2 = (HeavyLoadedOntology<Object>)getOntologyObject2();
	double threshold = 1.; // threshold above which distances are too high
	int i, j = 0;     // index for onto1 and onto2 classes
	int nbclass1 = 0; // number of classes in onto1
	int nbclass2 = 0; // number of classes in onto2
	Vector<Object> classlist2 = new Vector<Object>(10); // onto2 classes
	Vector<Object> classlist1 = new Vector<Object>(10); // onto1 classes
	double classmatrix[][];   // class distance matrix
	int nbprop1 = 0; // number of properties in onto1
	int nbprop2 = 0; // number of properties in onto2
	Vector<Object> proplist2 = new Vector<Object>(10); // onto2 properties
	Vector<Object> proplist1 = new Vector<Object>(10); // onto1 properties
	double propmatrix[][];   // properties distance matrix
	double pic1 = 0.5; // class weigth for name
	double pic2 = 0.5; // class weight for properties
	double pia1 = 1.; // relation weight for name
	double epsillon = 0.05; // stoping condition

	if ( params.getProperty("debug") != null )
	    debug = Integer.parseInt( params.getProperty("debug") );

	try {
	    // Create property lists and matrix
	    for ( Object prop : honto1.getObjectProperties() ){
		nbprop1++;
		proplist1.add( prop );
	    }
	    for ( Object prop : honto1.getDataProperties() ){
		nbprop1++;
		proplist1.add( prop );
	    }
	    for ( Object prop : honto2.getObjectProperties() ){
		nbprop2++;
		proplist2.add( prop );
	    }
	    for ( Object prop : honto2.getDataProperties() ){
		nbprop2++;
		proplist2.add( prop );
	    }
	    propmatrix = new double[nbprop1+1][nbprop2+1];
	    
	    // Create class lists
	    for ( Object cl : honto1.getClasses() ){
		nbclass1++;
		classlist1.add( cl );
	    }
	    for ( Object cl : honto2.getClasses() ){
		nbclass2++;
		classlist2.add( cl );
	    }
	    classmatrix = new double[nbclass1+1][nbclass2+1];
	    
	    if (debug > 0) System.err.println("Initializing property distances");
	    
	    for ( i=0; i<nbprop1; i++ ){
		Object cl1 = proplist1.get(i);
		String st1 = honto1.getEntityName( cl1 );
		if ( st1 != null) st1 = st1.toLowerCase();
		for ( j=0; j<nbprop2; j++ ){
		    Object cl2 = proplist2.get(j);
		    String st2 = honto2.getEntityName( cl2 );
		    if( st2 != null ) st2 = st2.toLowerCase();
		    if ( st1 != null || st2 != null ) {
			propmatrix[i][j] = pia1 * StringDistances.subStringDistance( st1, st2 );
		    } else {
			propmatrix[i][j] = pia1;
		    }
		}
	    }
	    
	    // Initialize class distances
	    if (debug > 0) System.err.println("Initializing class distances");
	    for ( i=0; i<nbclass1; i++ ){
		Object cl1 = classlist1.get(i);
		for ( j=0; j<nbclass2; j++ ){
		    classmatrix[i][j] = pic1*StringDistances.subStringDistance( honto1.getEntityName( cl1 ).toLowerCase(), honto2.getEntityName( classlist2.get(j) ).toLowerCase());
		}
	    }
	} catch ( OntowrapException owex ) {
	    throw new AlignmentException( "Error accessing ontology", owex );
	}

	// Iterate until completion
	double factor = 1.0;
	while ( factor > epsillon ){
	    // Compute property distances
	    // -- FirstExp: nothing to be done: one pass
	    // Here create the best matches for property distance already
	    // -- FirstExp: goes directly in the alignment structure
	    //    since it will never be refined anymore...
	    if (debug > 0) System.err.print("Storing property alignment\n");
	    for ( i=0; i<nbprop1; i++ ){
		boolean found = false;
		int best = 0;
		double max = threshold;
		for ( j=0; j<nbprop2; j++ ){
		    if ( propmatrix[i][j] < max) {
			found = true;
			best = j;
			max = propmatrix[i][j];
		    }
		}
		if ( found ) { addAlignCell( proplist1.get(i), proplist2.get(best), "=", 1.-max ); }
	    }
	    
	    if (debug > 0) System.err.print("Computing class distances\n");
	    // Compute classes distances
	    // -- for all of its attribute, find the best match if possible... easy
	    // -- simply replace in the matrix the value by the value plus the 
	    // classmatrix[i][j] =
	    // pic1 * classmatrix[i][j]
	    // + pic2 * 2 *
	    //  (sigma (att in c[i]) getAllignCell... )
	    //  / nbatts of c[i] + nbatts of c[j]
	    try {
		for ( i=0; i<nbclass1; i++ ){
		    Set<? extends Object> properties1 = honto1.getProperties( classlist1.get(i), OntologyFactory.ANY, OntologyFactory.ANY, OntologyFactory.ANY );
		    int nba1 = properties1.size();
		    if ( nba1 > 0 ) { // if not, keep old values...
			//Set correspondences = new HashSet();
			for ( j=0; j<nbclass2; j++ ){
			    Set<? extends Object> properties2 = honto2.getProperties( classlist2.get(j), OntologyFactory.ANY, OntologyFactory.ANY, OntologyFactory.ANY );
			    int nba2 = properties2.size();
			    double attsum = 0.;
			    // check that there is a correspondance
			    // in list of class2 atts and add their weights
			    for ( Object prp : properties1 ){
				Set<Cell> s2 = getAlignCells1( prp );
				// Find the property with the higest similarity
				// that is matched here
				double currentValue = 0.;
				for( Cell c2 : s2 ){
				    if ( properties2.contains( c2.getObject2() ) ) {
					double val = c2.getStrength();
					if ( val > currentValue )
					    currentValue = val;
				    }
				}
				attsum = attsum + 1 - currentValue;
			    }
			    classmatrix[i][j] = classmatrix[i][j]
				+ pic2 * (2 * attsum / (nba1 + nba2));
			}
		    }
		}
	    } catch ( OntowrapException owex ) {
		throw new AlignmentException( "Error accessing ontology", owex );
	    }

	    // Assess factor
	    // -- FirstExp: nothing to be done: one pass
	    factor = 0.;
	}

	// This mechanism should be parametric!
	// Select the best match
	// There can be many algorithm for these:
	// n:m: get all of those above a threshold
	// 1:1: get the best discard lines and columns and iterate
	// Here we basically implement ?:* because the algorithm
	// picks up the best matching object above threshold for i.
	if (debug > 0) System.err.print("Storing class alignment\n");
	
	for ( i=0; i<nbclass1; i++ ){
	    boolean found = false;
	    int best = 0;
	    double max = threshold;
	    for ( j=0; j<nbclass2; j++ ){
		if ( classmatrix[i][j] < max) {
		    found = true;
		    best = j;
		    max = classmatrix[i][j];
		}
	    }
	    if ( found ) { addAlignCell( classlist1.get(i), classlist2.get(best), "=", 1.-max ); }
	}
    }
}
