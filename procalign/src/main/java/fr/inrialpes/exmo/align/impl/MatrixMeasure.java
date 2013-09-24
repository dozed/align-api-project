/*
 * $Id: MatrixMeasure.java 1502 2010-08-16 08:13:55Z euzenat $
 *
 * Copyright (C) INRIA, 2003-2010
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

package fr.inrialpes.exmo.align.impl; 

import java.util.HashMap;
import java.util.Properties;
import java.util.Set;
import java.text.NumberFormat;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

/**
 * Implements the structure needed for recording class similarity
 * or dissimilarity within a matrix structure.
 *
 * @author Jérôme Euzenat
 * @version $Id: MatrixMeasure.java 1502 2010-08-16 08:13:55Z euzenat $ 
 */


public abstract class MatrixMeasure implements Similarity {

    public boolean similarity = true;

    //Momentaneously public
    public LoadedOntology onto1 = null;
    public LoadedOntology onto2 = null;
    public int nbclass1 = 0; // number of classes in onto1
    public int nbclass2 = 0; // number of classes in onto2
    public int nbprop1 = 0; // number of classes in onto1
    public int nbprop2 = 0; // number of classes in onto2
    public int nbind1 = 0; // number of individuals in onto1
    public int nbind2 = 0; // number of individuals in onto2
    public int i, j = 0;     // index for onto1 and onto2 classes
    public int l1, l2 = 0;   // length of strings (for normalizing)
    public HashMap<Object,Integer> classlist2 = null; // onto2 classes
    public HashMap<Object,Integer> classlist1 = null; // onto1 classes
    public HashMap<Object,Integer> proplist2 = null; // onto2 properties
    public HashMap<Object,Integer> proplist1 = null; // onto1 properties
    public HashMap<Object,Integer> indlist2 = null; // onto2 individuals
    public HashMap<Object,Integer> indlist1 = null; // onto1 individuals

    private NumberFormat numFormat = null; // printing

    public double clmatrix[][];   // distance matrix
    public double prmatrix[][];   // distance matrix
    public double indmatrix[][];   // distance matrix
	
    public void initialize( LoadedOntology onto1, LoadedOntology onto2, Alignment align ){
	initialize( onto1, onto2 );
	// Set the values of the initial alignment in the cells
	try {
	    ObjectAlignment oalign;
	    if ( align instanceof URIAlignment ){
		oalign = AlignmentTransformer.toObjectAlignment((URIAlignment) align);
	    } else if ( align instanceof ObjectAlignment ) {
		oalign = (ObjectAlignment)align;
	    } else {
		throw new AlignmentException(""); 
	    };
	    for ( Cell c : oalign ){
		Object o1 = c.getObject1();
		if ( onto1.isClass( o1 ) ) {
		    Integer i1 = classlist1.get( o1 );
		    Integer i2 = classlist2.get( c.getObject2() );
		    if ( i1 != null && i2 != null ) {
			if ( similarity )
			    clmatrix[i1.intValue()][i2.intValue()] = c.getStrength();
			else clmatrix[i1.intValue()][i2.intValue()] = 1.-c.getStrength();
		    }
		} else if ( onto1.isProperty( o1 ) ) {
		    Integer i1 = proplist1.get( o1 );
		    Integer i2 = proplist2.get( c.getObject2() );
		    if ( i1 != null && i2 != null ) {
			if ( similarity )
			    prmatrix[i1.intValue()][i2.intValue()] = c.getStrength();
			else prmatrix[i1.intValue()][i2.intValue()] = 1.-c.getStrength();
		    }
		} else {
		    Integer i1 = indlist1.get( o1 );
		    Integer i2 = indlist2.get( c.getObject2() );
		    if ( i1 != null && i2 != null ) {
			if ( similarity )
			    indmatrix[i1.intValue()][i2.intValue()] = c.getStrength();
			else indmatrix[i1.intValue()][i2.intValue()] = 1.-c.getStrength();
		    }
		}
	    }
	} catch (Exception ex) {}; // ignore silently
    }

    public void initialize( LoadedOntology o1, LoadedOntology o2 ){
	onto1 = o1;
	onto2 = o2;
	classlist2 = new HashMap<Object,Integer>(); // onto2 classes
	classlist1 = new HashMap<Object,Integer>(); // onto1 classes
	proplist2 = new HashMap<Object,Integer>(); // onto2 properties
	proplist1 = new HashMap<Object,Integer>(); // onto1 properties
	indlist2 = new HashMap<Object,Integer>(); // onto2 instances
	indlist1 = new HashMap<Object,Integer>(); // onto1 instances
	//System.err.println("  >> "+onto1+"/"+onto2 );

	try {
	    // Create class lists
	    for( Object cl : onto2.getClasses() ){
		classlist2.put( cl, new Integer(nbclass2++) );
	    }
	    for( Object cl : onto1.getClasses() ){
		classlist1.put( cl, new Integer(nbclass1++) );
	    }
	    //System.err.println("  >> NbClasses: "+nbclass1+"/"+nbclass2 );
	    clmatrix = new double[nbclass1+1][nbclass2+1];

	    // Create property lists
	    for( Object pr : onto2.getObjectProperties() ){
		proplist2.put( pr, new Integer(nbprop2++) );
	    }
	    for( Object pr : onto2.getDataProperties() ){
		proplist2.put( pr, new Integer(nbprop2++) );
	    }
	    for( Object pr : onto1.getObjectProperties() ){
		proplist1.put( pr, new Integer(nbprop1++) );
	    }
	    for( Object pr : onto1.getDataProperties() ){
		proplist1.put( pr, new Integer(nbprop1++) );
	    }
	    //System.err.println("  >> NbProp: "+nbprop1+"/"+nbprop2 );
	    prmatrix = new double[nbprop1+1][nbprop2+1];

	    // Create individual lists
	    for( Object ind : onto2.getIndividuals() ){
		// We suppress anonymous individuals... this is not legitimate
		if ( onto2.getEntityURI( ind ) != null ) {
		    indlist2.put( ind, new Integer(nbind2++) );
		}
	    }
	    for( Object ind : onto1.getIndividuals() ){
		// We suppress anonymous individuals... this is not legitimate
		if ( onto1.getEntityURI( ind ) != null ) {
		    indlist1.put( ind, new Integer(nbind1++) );
		}
	    }
	    //System.err.println("  >> NbInd: "+nbind1+"/"+nbind2 );
	    indmatrix = new double[nbind1+1][nbind2+1];
	} catch (OntowrapException e) { e.printStackTrace(); };
    }

    @SuppressWarnings("unchecked") //ConcatenatedIterator
    public void compute( Properties params ){
	try {
	    // Compute distances on classes
	    for ( Object cl2: onto2.getClasses() ){
		for ( Object cl1: onto1.getClasses() ){
		    clmatrix[classlist1.get(cl1).intValue()][classlist2.get(cl2).intValue()] = classMeasure( cl1, cl2 );
		}
	    }
	    // Compute distances on individuals
	    // (this comes first because otherwise, it2 is defined)
	    for ( Object ind2: onto2.getIndividuals() ){
		if ( indlist2.get(ind2) != null ) {
		    for ( Object ind1: onto1.getIndividuals() ){
			if ( indlist1.get(ind1) != null ) {
			    indmatrix[indlist1.get(ind1).intValue()][indlist2.get(ind2).intValue()] = individualMeasure( ind1, ind2 );
			}
		    }
		}
	    }
	    // Compute distances on properties
	    ConcatenatedIterator it2 = new
		ConcatenatedIterator(onto2.getObjectProperties().iterator(),
				     onto2.getDataProperties().iterator());
	    for ( Object pr2: it2 ){
		ConcatenatedIterator it1 = new
		    ConcatenatedIterator(onto1.getObjectProperties().iterator(),
					 onto1.getDataProperties().iterator());
		for ( Object pr1: it1 ){
		    prmatrix[proplist1.get(pr1).intValue()][proplist2.get(pr2).intValue()] = propertyMeasure( pr1, pr2 );
		}
	    }
	    // What is caught is really Exceptions
	} catch (Exception e) { e.printStackTrace(); }
    }

    public double getIndividualSimilarity( Object i1, Object i2 ){
	return indmatrix[indlist1.get(i1).intValue()][indlist2.get(i2).intValue()];
    }
    public double getClassSimilarity( Object c1, Object c2 ){
	return clmatrix[classlist1.get(c1).intValue()][classlist2.get(c2).intValue()];
    }
    public double getPropertySimilarity( Object p1, Object p2 ){
	return prmatrix[proplist1.get(p1).intValue()][proplist2.get(p2).intValue()];
    }

    // Not an efficient access...
    private void printMatrix( int nb1, HashMap<Object,Integer> ent1, HashMap<Object,Integer> ent2, double matrix[][] ) {
	// Number format class to format the values
	numFormat = NumberFormat.getInstance();
	numFormat.setMinimumFractionDigits( 2 );
	numFormat.setMaximumFractionDigits( 2 );
	System.out.print("\\begin{tabular}{r|");
	for ( int i = 0; i < nb1 ; i++ ) System.out.print("c");
	System.out.println("}");
	try {
	    Set<Object> key1 = ent1.keySet();
	    for( Object ob1 : key1 ){
		System.out.print(" & \\rotatebox{90}{"+onto1.getEntityName( ob1 )+"}");
	    }
	    System.out.println(" \\\\ \\hline");
	    for ( Object ob2 : ent2.keySet() ){
		System.out.print( onto2.getEntityName( ob2 ) );
		for ( Object ob1 : key1 ){
		    System.out.print(" & "+numFormat.format(matrix[ent1.get(ob1).intValue()][ent2.get(ob2).intValue()]));
		}
		System.out.println("\\\\");
	    }
	} catch (OntowrapException e) { e.printStackTrace(); };
	System.out.println("\n\\end{tabular}");
    }

    public boolean getSimilarity() {
	return similarity;
    }
    public void printClassSimilarityMatrix( String type ){
	printMatrix( nbclass1, classlist1, classlist2, clmatrix ); 
    }
    public void printPropertySimilarityMatrix( String type ){
	printMatrix( nbprop1, proplist1, proplist2, prmatrix ); 
    };
    public void printIndividualSimilarityMatrix( String type ){
	printMatrix( nbind1, indlist1, indlist2, indmatrix ); 
    };

}
