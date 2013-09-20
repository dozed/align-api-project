/*
 * $Id: DiscriminantGenerator.java 1695 2012-03-07 16:07:46Z euzenat $
 *
 * Copyright (C) 2012, INRIA
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

/*
 * Generates a "discriminant" set of tests by systematically covering the
 * space at a particular resolution (STEP)
 * This only applies to three different dimensions but could be parameterized
 */

package fr.inrialpes.exmo.align.gen;

import java.util.Properties;

/**
 * This class serves as an example for systematically generating the
 * alteration space at a particular resolution.
 * It could be generalised by:
 * - using an arbitrary number of dimensions (instead of 3)
 * - having a different step on each dimention
 * All this could be achieved by using the parameters which could be Alterator/Step
 */
public class DiscriminantGenerator extends TestSet {

    public void initTestCases( Properties params ) {
	// Process params
	debug = ( params.getProperty( "debug" ) != null );

	// JE: ugly 
	secondOntoFile = params.getProperty( "outdir" )+"/000/onto.rdf";

	// Test configuration parameters
	int STEP = 5;
	String stepval = params.getProperty( "step" );
	try {
	    if ( stepval != null && !stepval.equals("") ) STEP = Integer.parseInt( stepval );
	} catch ( Exception ex ) {
	    ex.printStackTrace(); // continue with the default
	}
	final float INCR = 1.0f/(STEP-1);
	if ( debug ) System.err.println( " STEP: "+STEP+" / INCR: "+INCR );

        /* Test 000 Generate the initial situation */
	initTests( "000" );

	for ( int i = 0; i < STEP; i++ ) {
	    String label1 = i+"00";
	    if ( i != 0 ) {
		addTestChild( "000", label1, 
			      newProperties( ParametersIds.REMOVE_PROPERTIES, ""+i*INCR ) );
	    }
	    for ( int j = 0; j < STEP; j++ ) {
		String label2 = ""+i+j+"0";
		if ( i != 0 || j != 0 ) {
		    addTestChild( label1, label2, 
				  newProperties( ParametersIds.REMOVE_COMMENTS, ""+j*INCR ) );
		}
		for ( int k = 0; k < STEP; k++ ) {
		    String label3 = ""+i+j+k;
		    if ( i != 0 || j != 0 || k != 0 ) {
			addTestChild( label2, ""+i+j+k,
				      newProperties( ParametersIds.RENAME_CLASSES, ""+k*INCR,
						     ParametersIds.RENAME_PROPERTIES, ""+k*INCR ) );
		    }
		}
	    }
	}
    }
}
