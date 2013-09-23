/*
 * $Id: AveragePRGraphEvaluator.java 1196 2010-01-10 19:58:52Z euzenat $
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

import java.util.Properties;
import java.util.Vector;
import java.io.PrintWriter;

/**
 * Compute the precision recall graph on 11 points
 * The first alignment is thus the expected one.
 * This is for legacy reason the first implementation of this function.
 * It does not follow the new standard GraphEvaluator protocol.
 *
 * @author Jerome Euzenat
 * @version $Id: AveragePRGraphEvaluator.java 1196 2010-01-10 19:58:52Z euzenat $ 
 * 
 * The computation is remotely inspired from the sample programme of
 * Raymond J. Mooney
 * available under GPL from http://www.cs.utexas.edu/users/mooney/ir-course/
 * 
 * Mooney also provides the averaging of these graphs over several queries:
 * unfortunatelly, the resulting graph is not anymore a Precision/Recall graph
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
 * NOTE (JE:2010): This should be adapated for other notions of Precision and Recall
 *
 */

public class AveragePRGraphEvaluator extends GraphEvaluator {

    private int size = 0; // for averaging

    // The eleven values of precision and recall
    private double[] precisions = null;

    private double map = 0.0; // For MAP
    private double rawmap = 0.0; // For MAP

    public AveragePRGraphEvaluator() {
	super();
	precisions = new double[ STEP+1 ];
	points = new Vector<Pair>();
    }

    /**
     * Returns the points to display in a graph
     */
    public Vector<Pair> eval(){
	Vector<Pair> result = new Vector<Pair>(STEP+1);
	// Compute the average and build the vector pair
	//System.err.println( " Size: "+size );
	for( int j = 0; j <= STEP; j++ ) {
	    // JE: better with j/10
	    //System.err.println( "  prec at "+j+" : "+precisions[j] );
	    result.add( new Pair( ((double)j)/10, precisions[j] / size ) );
	}
	map = rawmap / size; // average map
	return result;
    }

    /**
     * Compute precision and recall graphs.
     */
    public Vector<Pair> eval( Properties params ) { // throws AlignmentException
	return eval();
    }

    public void ingest( Alignment al, Alignment reference ) {
	size++;
	try {
	    evalAlignment( reference, al );
	} catch ( AlignmentException aex ) {
	    aex.printStackTrace();
	}
    }

    public void evalAlignment( Alignment align1, Alignment align2 ) throws AlignmentException {
	// Local variables
	int nbexpected = align1.nbCells();
	int nbfound = 0;
	int nbcorrect = 0;
	double sumprecisions = 0.; // For MAP
	Vector<Pair> inflexion = new Vector<Pair>();

	// Create a sorted structure in which putting the cells
	initCellSet( true );
	if ( align2 == null ) return; //no increase of precisions
	for ( Cell c : align2 ) {
    	    if ( invalid && c.getStrength() != 1. && c.getStrength() != 0. ) invalid = false;
	    cellSet.add( new EvalCell( c, isCorrect( c, align1 ) ) );
	}

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
		precisions[i] += best; //??
		i--;
		level = (double)i/STEP;
	    };
	    if ( precrec.getY() > best ) best = precrec.getY();
	    j--;
	}
	precisions[0] += best; // It should be 1. that's why it is now added in points. [R=0%]
	
	rawmap += sumprecisions / nbexpected; // For MAP (average MAP)
    }

    /**
     * This output the result
     */
    public void write(PrintWriter writer) throws java.io.IOException {
	writer.println("<?xml version='1.0' encoding='utf-8' standalone='yes'?>");
	writer.println("<rdf:RDF xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'>");
	writer.println("  <output rdf:about=''>");
	for( int i=0; i <= STEP; i++ ){
	    writer.print("    <step>\n      <recall>");
	    writer.print((double)i/STEP);
	    writer.print("</recall>\n      <precision>");
	    writer.print(precisions[i]);
	    writer.print("</precision>\n    </step>\n");
	}
	writer.print("    <MAP>"+map+"</MAP>\n");
	writer.print("  </output>\n</rdf:RDF>\n");
    }

    /* Write out the final interpolated recall/precision graph data.
     * One line for each recall/precision point in the form: 'R-value P-value'.
     * This is the format needed for GNUPLOT.
     */
    public void writePlot( PrintWriter writer ) {
        for(int i = 0; i < STEP+1; i++){
            writer.println( (double)i/10 + "\t" + precisions[i]);
	}
    }

    public double getPrecision( int i ){
	return precisions[i];
    }

    /*
     * Only valid once eval() has been called 
     */
    public double getMAP(){
	return map;
    }

    /**
     * Retuns a simple global evaluation measure if any
     */
    public double getGlobalResult(){
	return map;
    }

    public String xlabel() { return "recall"; }
    public String ylabel() { return "precision"; };
}

