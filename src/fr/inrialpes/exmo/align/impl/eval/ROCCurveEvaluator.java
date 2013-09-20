/*
 * $Id: ROCCurveEvaluator.java 1506 2010-08-21 08:45:52Z euzenat $
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

import java.util.Enumeration;
import java.util.Properties;
import java.util.Iterator;
import java.util.TreeSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.Comparator;
import java.util.Vector;
import java.io.PrintWriter;
import java.net.URI;

/**
 * Compute ROCCurves
 *
 * @author Jerome Euzenat
 * @version $Id: ROCCurveEvaluator.java 1506 2010-08-21 08:45:52Z euzenat $ 
 * 
 * ROCCurves traverse the ranked list of correspondences
 * Hence, like PRGraphs, it is only relevant to this case.
 * X-axis is the number of incorrect correspondences
 * Y-axis is the number of correct correspondences
 * It is expected that the curve grows fast first and then much slower
 * This indicates the accuracy of the matcher.
 *
 * The "Surface Under Curve" (AUC) is returned as a result.
 * AUC is in fact the percentage of surface under the curve (given by N the size of the reference alignment and P the size of all pairs of ontologies, N*P is the full size), Area/N*P.
 * It is ususally interpreted as:
 * 0.9 - 1.0 excellent
 * 0.8 - 0.9 good
 * 0.7 - 0.8 fair
 * 0.6 - 0.7 poor
 * 0.0 - 0.6 bad
 *
 * The problem with these measures is that they assume that the provided alignment
 * is a complete alignment: it contains all pairs of entities hence auc are comparable
 * because they depend on the same sizes... This is not the case when alignment are
 * incomplete. Hence, this should be normalised.
 *
 * There are two ways of doing this:
 * - simply Area/N*P, but this would advantage
 * - considering the current subpart Area/N*|
 *   => both would advantage matchers with high precision
 * penalising them Area/N*P
 * - interpolating the curve: 
 * NOT EASY, TO BE IMPLEMENTED
 */

public class ROCCurveEvaluator extends GraphEvaluator {

    private double auc = 0.0;

    public ROCCurveEvaluator() {
	super();
    }

    /**
     * Compute ROCCurve points
     * From an ordered vector of cells with their correctness status
     */
    public Vector<Pair> eval( Properties param ) {
	// Local variables
	int nbfound = 0;
	int area = 0;
	int x = 0;
	int y = 0;
	
	//int scale = align2.nbCells();
	int scale = 0;
	if ( param != null && param.getProperty( "scale" ) != null ) {
	    scale = Integer.parseInt( param.getProperty( "scale" ) );
	}

	points = new Vector<Pair>();

	// Collect the points in the curve
	Pair last = new Pair( 0., 0. ); // [Origin]
	for( EvalCell c : cellSet ) {
	    nbfound++;
	    if ( c.correct() ) {
		y++; 
		if ( last.getX() != x ) { 
		    points.add( last );
		    last = new Pair( x, y-1 );
		}
	    } else {
		x++;  area += y;
		if ( last.getY() != y ) {
		    points.add( last );
		    last = new Pair( x-1, y );
		}
	    }
	}
	points.add( last );
	points.add( new Pair( x, y ) );

	/*
	 * This is not ideal because the measure is given curve by curve
	 * only as far as the curves continues. Adding the Max between all curves
	 * would be better
	 */
	if ( nbfound != 0 ) { // or x != 0
	    //auc = (double)area / (double)(nbexpected * x );
	    auc = (double)(area + y*y ) / (double)(nbexpected * nbfound );
	    if ( scale != 0 ){
		auc = (double)(area + y*(scale-x) ) / (double)(nbexpected * scale );
	    }
	} else {
	    auc = 0.00;
	}

	// Scale
	for ( Pair p : points ){
	    if ( scale != 0 ){
		p.setX( p.getX() / scale );
	    } else if ( x != 0 ) p.setX( p.getX() / x );
	    p.setY( p.getY() / nbexpected );
	}
	//points.add( new Pair( 1., y/nbexpected ) );

	return points;
    }

    /**
     * For the moment
     */
    public Vector<Pair> eval() {
	return eval( (Properties)null );
    }

    /**
     * This output the result
     */
    public void write(PrintWriter writer) throws java.io.IOException {
	writer.println("<?xml version='1.0' encoding='utf-8' standalone='yes'?>");
	writer.println("<"+SyntaxElement.RDF.print()+" xmlns:"+Namespace.RDF.shortCut+"='"+Namespace.RDF.prefix+"'>");
	writer.println("  <output rdf:about=''>");
	writeXMLMap( writer );
	writer.print("    <AUC>"+auc+"</AUC>\n");
	writer.print("  </output>\n</"+SyntaxElement.RDF.print()+">\n");
    }
    
    public void writePlot(PrintWriter writer) {
	for( Pair p : points ){
            writer.println( p.getX()/10 + "\t" + p.getY() );
	}
    }
    
    public double getPlotResult( int i ){
	return 0.0;
    }

    public double getGlobalResult(){
	return auc;
    }
    public double getAUC(){
	return auc;
    }
    public String xlabel() { return "noise"; }
    public String ylabel() { return "recall"; };
}

