/*
 * $Id: PRGraphEvaluator.java 1529 2010-10-12 10:03:49Z euzenat $
 *
 * Copyright (C) INRIA, 2004-2005, 2007-2010
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
 *
 */

package fr.inrialpes.exmo.align.impl.eval;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.parser.SyntaxElement;

import java.util.Properties;
import java.util.Vector;
import java.io.PrintWriter;

/**
 * Compute the precision recall graph on 11 points
 *
 * @author Jerome Euzenat
 * @version $Id: PRGraphEvaluator.java 1529 2010-10-12 10:03:49Z euzenat $ 
 * 
 * This works perfectly correctly. I mention below the point which are
 * mentionned as design points in a forecoming Exmotto entry:
 * [R=0%] What should be P when R is 0% (obviously 100%)
 * [R=100%] What should be P when R=100% is unreachable
 * [Interp.] How is a chaotic curve interpolated
 *
 * Note: a very interesting measure is the MAP (mean average precision)
 * which is figuratively the area under the curve and more precisely
 * the average precision obtained for each correspondence in the reference
 * alignment.
 * The problem is that it can only be valid if the compared alignment has 
 * provided all the correspondences in the reference.
 * Otherwise, it would basically be:
 * SUM_c\in correct( P( c ) ) / nbexpected
 *
 * NOTE: This should be adapated for other notions of Precision and Recall
 *
 */

public class PRGraphEvaluator extends GraphEvaluator {

    private int STEP = 10;

    private double map = 0.0; // For MAP
    private double sumprecisions = 0.; // For MAP

    public PRGraphEvaluator() {
	super();
    }

    /**
     * Compute precision and recall graphs.
     */
    public Vector<Pair> eval() { // throws AlignmentException
	return eval( (Properties)null );
    }

    /**
     * Compute precision and recall graphs.
     */
    public Vector<Pair> eval( Properties params ) { // throws AlignmentException
	points = new Vector<Pair>(STEP+1); 

	if ( params != null && params.getProperty( "oldstyle" ) != null ) {
	    return evalOldStyle();
	} else {
	    return evalOpenEnded();
	}
    }

    /**
     * Returns a list of Precision at recall points (Pairs)
     * From an ordered vector of cells with their correctness status
     */
    public Vector<Pair> evalOpenEnded() {
	// Determine what the increment is
	// Get the increment
	int nbcorrect = 0;
	int nbfound = 0;
	int increment = (STEP*nbexpected)/100; // 2010 should be computed with the total expected (in negrapheval)
	//System.err.println(" INCREMENT SET "+increment );
	int next = 0;
	points.add( new Pair( 0., 1. ) ); // [R=0%]
	next += increment;
	for ( EvalCell c : cellSet ) {
	    nbfound++;
	    if ( c.correct() ) {
		nbcorrect++;
		double precision = (double)nbcorrect / (double)nbfound;
		sumprecisions += precision; // For MAP
		if ( nbcorrect == next ) { // increment achieved
		    //record value
		    double recall = (double)nbcorrect / (double)nbexpected;
		    // Here the recall could be computed more directly
		    points.add( new Pair( recall, precision ) );
		    next += increment;
		}
	    }
	}
	// In the end, the last point on the curve should be the global precision?
	// No, in the end, it should be the real-final-precision and recall, i.e.,
	points.add( new Pair( (double)nbcorrect / (double)nbexpected, (double)nbcorrect / (double)nbfound ) );

	// [Interp.] No interpolation is made there...

	// MAP is provided with regard to the expected number of inflexion points
	map = sumprecisions / nbexpected; 

	return points;
    }

    /**
     * Returns a list of Precision at recall points (Pairs)
     * From an ordered vector of cells with their correctness status
     */
    public Vector<Pair> evalOldStyle() {
	double[] precisions = new double[STEP+1];
	// Determine what the increment is
	// Get the increment
	int nbcorrect = 0;
	int nbfound = 0;
	int increment = (STEP*nbexpected)/100; // 2010 should be computed with the total expected (in negrapheval)
	Vector<Pair> inflexion = new Vector<Pair>();
	//System.err.println(" INCREMENT SET "+increment );
	int next = 0;
	// Collect the points that change recall
	// (the other provide lower precision from the same recall and are not considered)
	inflexion.add( new Pair( 0., 1. ) ); // [R=0%]
	for( EvalCell c2 : cellSet ){
	    nbfound++;
	    if ( c2.correct() ) { //correctCell( c2, align2, align1 ) > 0.
		nbcorrect++;
		double recall = (double)nbcorrect / (double)nbexpected;
		double precision = (double)nbcorrect / (double)nbfound;
		sumprecisions += precision; // For MAP
		// Create a new pair to put in the list
		// It records real precision and recall at that point
		inflexion.add( new Pair( recall, precision ) );
		c2 = null; // out of the loop.
	    }
	}

	// Now if we want to have a regular curve we must penalize those system
	// that do not reach 100% recall.
	// for that purpose, and for each other bound we add a point with the worse
	// precision which is the required recall level divided with the maximum
	// cardinality possible (i.e., the multiplication of the ontology sizes).
	// JE[R=100%]: that's a fine idea! Unfortunately SIZEOFO1 and SIZEOFO2 are undefined values
	//inflexion.add( new Pair( 1., (double)nbexpected/(double)(SIZEOFO1*SIZEOFA2) ) );
	inflexion.add( new Pair( 1.0, 0. ) ); // useless because 

	// [Interp.] Interpolate curve points at each n-recall level
	// This is inspired form Ray Mooney's program
	// It works backward in the vector,
	//  (in the same spirit as before, the maximum value so far -best- is retained)
	int j = inflexion.size()-1; // index in recall-ordered vector of points
	//System.err.println( "Inflexion: "+j);
	int i = STEP; // index of the current recall interval
	double level = (double)i/STEP; // max level of that interval
	double best = 0.; // best value found for that interval
	while( j >= 0 ){
	    Pair precrec = inflexion.get(j);
	    while ( precrec.getX() < level ){
		precisions[i] = best; //??
		i--;
		level = (double)i/STEP;
	    };
	    if ( precrec.getY() > best ) best = precrec.getY();
	    j--;
	}
	precisions[0] = best; // It should be 1. that's why it is now added in points. [R=0%]

	for( i = 0; i <= STEP; i++ ) {
	    // JE: better with j/10
	    //System.err.println( "  prec at "+j+" : "+precisions[j] );
	    points.add( new Pair( ((double)i)/10, precisions[i] ) );
	}

	// MAP is provided with regard to the expected number of inflexion points
	map = sumprecisions / nbexpected; 

	return points;
    }

    /**
     * This output the result
     */
    public void write(PrintWriter writer) throws java.io.IOException {
	writer.println("<?xml version='1.0' encoding='utf-8' standalone='yes'?>");
	writer.println("<"+SyntaxElement.RDF.print()+" xmlns:"+Namespace.RDF.shortCut+"='"+Namespace.RDF.prefix+"'>");
	writer.println("  <output "+SyntaxElement.RDF_ABOUT.print()+"=''>");
	writeXMLMap( writer );
	writer.print("    <MAP>"+map+"</MAP>\n");
	writer.print("  </output>\n</"+SyntaxElement.RDF.print()+">\n");
    }

    public double getMap(){
	return map;
    }
    public double getGlobalResult(){
	return map;
    }

    public String xlabel() { return "recall"; }
    public String ylabel() { return "precision"; };
}

