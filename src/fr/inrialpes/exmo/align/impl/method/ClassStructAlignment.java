/*
 * $id: ClassStructAlignment.java 1189 2010-01-03 17:57:13Z euzenat $
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

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.impl.DistanceAlignment;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

import fr.inrialpes.exmo.ontosim.string.StringDistances;

/** 
 * Establishes an alignment based on the comparison of the properties
 * that classes have in common. These properties are themselves compared
 * based on correspondences provided by an initial alignment.
 *
 * This class has been built for ISWC experiments with bibliography.
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
 * @version $Id: ClassStructAlignment.java 1502 2010-08-16 08:13:55Z euzenat $ 
 */

public class ClassStructAlignment extends DistanceAlignment implements AlignmentProcess {

    private HeavyLoadedOntology<Object> honto1 = null;
    private HeavyLoadedOntology<Object> honto2 = null;

    /** Creation **/
    public ClassStructAlignment(){};

    /**
     * Initialisation
     * The class requires HeavyLoadedOntologies
     */
    public void init(Object o1, Object o2, Object ontologies) throws AlignmentException {
	super.init( o1, o2, ontologies );
	if ( !( getOntologyObject1() instanceof HeavyLoadedOntology
		&& getOntologyObject1() instanceof HeavyLoadedOntology ))
	    throw new AlignmentException( "ClassStructAlignment requires HeavyLoadedOntology ontology loader" );
    }

    /** Processing **/
    // Could better use similarity
    public void align( Alignment alignment, Properties params ) throws AlignmentException {
	loadInit( alignment );
	honto1 = (HeavyLoadedOntology<Object>)getOntologyObject1();
	honto2 = (HeavyLoadedOntology<Object>)getOntologyObject2();
	int i, j = 0;     // index for onto1 and onto2 classes
	int nbclass1 = 0; // number of classes in onto1
	int nbclass2 = 0; // number of classes in onto2
	Vector<Object> classlist2 = new Vector<Object>(10); // onto2 classes
	Vector<Object> classlist1 = new Vector<Object>(10); // onto1 classes
	double classmatrix[][];   // class distance matrix
	double pic1 = 0.5; // class weigth for name
	double pic2 = 0.5; // class weight for properties

	if ( init != null ) ingest( init );

	// Create class lists
	try {
	    for ( Object cl : honto2.getClasses() ){
		nbclass2++;
		classlist2.add( cl );
	    }
	    for ( Object cl : honto1.getClasses() ){
		nbclass1++;
		classlist1.add( cl );
	    }
	} catch ( OntowrapException owex ) {
	    throw new AlignmentException( "Cannot access class hierarchy", owex );
	}
	classmatrix = new double[nbclass1+1][nbclass2+1];
	
	if (debug > 0) System.err.println("Initializing class distances");

	// Initialize class distances
	// JE: Here AlignmentException is raised if cl or classlist2.get(j)
	// cannot be identified as an Entity, maybe this should be traped here
	try {
	    for ( i=0; i<nbclass1; i++ ){
		Object cl = classlist1.get(i);
		for ( j=0; j<nbclass2; j++ ){
		    classmatrix[i][j] = pic1 * StringDistances.subStringDistance(honto1.getEntityName( cl ).toLowerCase(),
										 honto2.getEntityName( classlist2.get(j) ).toLowerCase());
		}
	    }
	} catch ( OntowrapException owex ) {
	    throw new AlignmentException( "Cannot find entity URI", owex );
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
			    if ( s2 != null ) {
				for( Cell c2 : s2 ){
				    if ( properties2.contains( c2.getObject2() ) ) {
					double val = c2.getStrength();
					if ( val > currentValue )
					    currentValue = val;
				    }
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
	    throw new AlignmentException( "Cannot navigate properties", owex );
	}

    }

}
