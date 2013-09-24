/*
 * $Id: ConsensusAggregator.java 1608 2011-05-28 20:21:16Z euzenat $
 *
 * Copyright (C) INRIA, 2010
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

package fr.inrialpes.exmo.align.impl.aggr; 

import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.BasicAlignment;

/**
 *
 * @author Jérôme Euzenat
 * @version $Id: ConsensusAggregator.java 1608 2011-05-28 20:21:16Z euzenat $ 
 *
 * This class is a generalisation of a consensus aggregation.
 * It is fed by "ingesting" various alignments, i.e., collecting and counting their correspondences
 * Then the extraction of an alignement is made by extracting an alignment
 * So the aggregator works with the following interface:
 * aggr = new ConsensusAggregator();
 * aggr.ingest( Alignment );
 * aggr.ingest( Alignment );
 * ...
 * aggr.extract( double, boolean )
 * or
 * aggr.extract( int, boolean )
 * for the interpretation of these two primitives, see the definition of extract.
 *
 * There could be also a possibility to introduce a hook depending on the entities to be compared. 
 * For instance, in multilingual matching, if the labels are one word, then n=5, if they are more than one word, n=2
 */

public class ConsensusAggregator extends BasicAlignment {

    int nbAlignments = 0;
    Hashtable<Cell, CountCell> count;

    /** Creation **/
    public ConsensusAggregator() {
	// Initialising the hash table
	count = new Hashtable<Cell, CountCell>();
    }


    /**
     * Extract the alignment from consensus
     */
    public void ingest( Alignment al ) throws AlignmentException {
	nbAlignments++;
	for ( Cell c : al ) {
	    Cell newc = isAlreadyThere( c );
	    if ( newc == null ) {
		newc = addAlignCell( c.getObject1(), c.getObject2(), c.getRelation().toString(), 1. );
		count.put( newc, new CountCell() );
	    }
	    count.get( newc ).incr( c.getStrength() );
	}
    }

    /**
     * Extract the alignment from consensus
     * If absolute, then retain correspondences found in more than n alignments
     * Otherwise, retain those found in more than n% of alignments
     */
    public void extract( int minVal, boolean absolute ) throws AlignmentException {
	// Check that this is between 0. and 1.
	double threshold = (double)minVal;
	if ( !absolute ) threshold = minVal*(double)nbAlignments;
	Set<Cell> todelete = new HashSet<Cell>();
	for ( Cell c : this ) {
	    // if it is not more than X time, then return 0
	    if ( count.get( c ).getOccurences() >= threshold ) {
		c.setStrength( (double)count.get( c ).getOccurences() / (double)nbAlignments );
	    } else {
		todelete.add( c );
	    }
	}
	for ( Cell c : todelete ) {
	    try { remCell( c ); } catch (Exception ex ) {};
	}

    }

    /**
     * Extract the alignment from consensus
     * If absolute, then retain correspondences scoring more than n
     * Otherwise, retain those scoring more than n in average, i.e., n*nbAlignments
     */
    public void extract( double minVal, boolean absolute ) throws AlignmentException {
	double threshold = minVal;
	if ( !absolute ) threshold = minVal*(double)nbAlignments; // Check that this is between 0. and 1.
	Set<Cell> todelete = new HashSet<Cell>();
	for ( Cell c : this ) {
	    if ( count.get( c ).getValue() >= minVal ) {
		c.setStrength( count.get( c ).getValue() / (double)nbAlignments );
	    } else {
		todelete.add( c );
	    }
	}
	for ( Cell c : todelete ) {
	    try { remCell( c ); } catch (Exception ex ) {};
	}

    }

    /**
     * Find the relation if it already exists.
     * 
     * NOTE: It may be worth to consider that the relations do not have to
     * be equal but could be more specific or general than one another.
     * This could typically be made with algebras of relations.
     */
    public Cell isAlreadyThere( Cell c ){
	try {
	    Set<Cell> possible = getAlignCells1( c.getObject1() );
	    Object ob2 = c.getObject2();
	    Relation r = c.getRelation();
	    if ( possible!= null ) {
		for ( Cell c2 : possible ) {
		    if ( ob2.equals( c2.getObject2() ) && r.equals( c2.getRelation() ) ) return c2;
		}
	    }
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
	return null;
    }

    private class CountCell {
	private int occ;
	private double number;
	public CountCell() { number = 0.; occ = 0; }
	public CountCell( double i, int j ) { number = i; occ = j; }
	public void incr( double d ) { number += d; occ++; }
	public double getValue() { return number; }
	public int getOccurences() { return occ; }
    }

}
