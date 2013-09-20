/*
 * $Id: ThresholdGraphEvaluator.java 1608 2011-05-28 20:21:16Z euzenat $
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
 * Compute the F-measure/precision/recall at various thresholds
 *
 * @author Jerome Euzenat
 * @version $Id: ThresholdGraphEvaluator.java 1608 2011-05-28 20:21:16Z euzenat $ 
 * 
 */

public class ThresholdGraphEvaluator extends GraphEvaluator {

    private int STEP = 50; 

    private double opt = 0.; // Optimum recorded value

    public ThresholdGraphEvaluator() {
	super();
    }

    /**
     * Compute threshold graph
     */
    public Vector<Pair> eval() { // throws AlignmentException
	return eval( (Properties)null );
    }

    /**
     * Returns a list of Measure at threshold points (Pairs)
     * From an ordered vector of cells with their correctness status
     *
     * The basic strategy would be:
     * Take the alignment/(Compute P/R/Apply threshold)+
     * But it is better to: take the cells in reverse order
     * Compute the measures on the fly
     * 
     */
    public Vector<Pair> eval( Properties params ) { // throws AlignmentException
	points = new Vector<Pair>(STEP+1); 
	opt = 0.;
	int nbcorrect = 0;
	int nbfound = 0;
	double precision = 1.;
	double recall = 0.;
	double fmeasure = 0.;
	double prevt = cellSet.first().cell().getStrength(); // threshold for previous fellow
	double prevm = fmeasure;
	points.add( new Pair( 1., prevm ) ); // [T=100%]
	/* 
	// This is the continuous version
	for ( EvalCell c : cellSet ) {
	    nbfound++;
	    if ( c.correct() ) nbcorrect++;
	    if ( c.cell().getStrength() != prevt ) { // may record a new point
		fmeasure = 2*(double)nbcorrect/(double)(nbfound+nbexpected); // alternate formula
		if ( fmeasure != prevm ) {
		    points.add( new Pair( prevt, prevm ) );
		    points.add( new Pair( c.cell().getStrength(), fmeasure ) );
		    prevm = fmeasure;
		    if ( fmeasure > opt ) opt = fmeasure; // for real optimal
		}
		prevt = c.cell().getStrength();
	    }
	}
	fmeasure = 2*(double)nbcorrect/(double)(nbfound+nbexpected);
	*/
	// This is the version with increment
	// Determine what the increment is
	double increment = 1./(double)STEP;
	//System.err.println(" INCREMENT SET "+increment );
	double next = 1.;
	next -= increment;
	for ( EvalCell c : cellSet ) {
	    fmeasure = 2*(double)nbcorrect/(double)(nbfound+nbexpected); // alternate formula
	    if ( fmeasure > opt && c.cell().getStrength() < prevt ) {
		opt = fmeasure; // for real optimal
	    } else { // but only when all correspondences with same strength have been processed
		prevt = c.cell().getStrength();
	    }
	    while ( next >= 0.001 && c.cell().getStrength() <= next ) { // increment achieved
		points.add( new Pair( next, fmeasure ) );
		next -= increment;
	    }
	    nbfound++;
	    if ( c.correct() ) {
		nbcorrect++;
	    }
	}
	fmeasure = 2*(double)nbcorrect/(double)(nbfound+nbexpected);
	if ( fmeasure > opt ) opt = fmeasure; // for real optimal
	// In the end, it should exhaust all the thresholds
	while ( next >= 0.001 ) { // The bound is necessary for avoiding tikz problem
	    points.add( new Pair( next, fmeasure ) ); // same measure
	    next -= increment;
	}
	points.add( new Pair( 0., fmeasure ) ); // [T=0%]
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
	writer.print("    <OPTIMUM>"+opt+"</OPTIMIM>\n");
	writer.print("  </output>\n</"+SyntaxElement.RDF.print()+">\n");
    }

    public double getGlobalResult(){ // can only be the full measure
	return opt;
    }

    public String xlabel() { return "threshold"; }
    public String ylabel() { return "fmeasure"; }
}

