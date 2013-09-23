package fr.inrialpes.exmo.align.impl;/*
 * $Id: IOTest.java 1726 2012-05-09 23:22:05Z euzenat $
 *
 * Copyright (C) INRIA, 2008-2011
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import fr.inrialpes.exmo.align.impl.URIAlignment;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.parser.AlignmentParser;

import java.io.File;
import java.io.BufferedInputStream;
import java.io.FileInputStream;

/**
 * These tests corresponds to the tests presented in the examples/omwg directory
 */

public class IOTest {

    private Alignment alignment = null;
    private AlignmentParser aparser = null;

    @Test(groups = { "full", "io", "raw" }, expectedExceptions = AlignmentException.class)
    public void loadSOAPErrorTest() throws Exception {
	aparser = new AlignmentParser( 0 );
	assertNotNull( aparser );
	try { 	// shut-up log4j
	    com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler.silent = true;
	    alignment = aparser.parse( "test/input/soap.xml" );
	    // error (we forgot to tell the parser that the alignment is embedded)
	} catch (Exception ex) {
	    com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler.silent = false;
	    throw ex;
	}
    }

    @Test(groups = { "full", "io", "raw" }, dependsOnMethods = {"loadSOAPErrorTest"})
    public void loadSOAPTest() throws Exception {
	aparser.initAlignment( null );
	aparser.setEmbedded( true );
	alignment = aparser.parse( "test/input/soap.xml" );
	assertNotNull( alignment );
	assertTrue( alignment instanceof URIAlignment);
	assertEquals( alignment.getOntology2URI().toString(), "http://alignapi.gforge.inria.fr/tutorial/edu.mit.visus.bibtex.owl" );
	assertEquals( alignment.nbCells(), 57 );
    }

    private static String readFileAsString(String filePath) throws java.io.IOException {
	byte[] buffer = new byte[(int) new File(filePath).length()];
	BufferedInputStream f = new BufferedInputStream(new FileInputStream(filePath));
	f.read(buffer);
	return new String(buffer);
    }

    /**
     * The same tests as above (and elsewhere) using parseString instead of parse
     */
    @Test(groups = { "full", "io", "raw" }, expectedExceptions = AlignmentException.class)
    public void loadSOAPStringErrorTest() throws Exception {
	aparser = new AlignmentParser( 0 );
	assertNotNull( aparser );
	try { 	// shut-up log4j
	    com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler.silent = true;
	    alignment = aparser.parseString( readFileAsString( "test/input/soap.xml" ) );
	    // error (we forgot to tell the parser that the alignment is embedded)
	} catch (Exception ex) {
	    com.hp.hpl.jena.rdf.model.impl.RDFDefaultErrorHandler.silent = false;
	    throw ex;
	}
    }

    @Test(groups = { "full", "io", "raw" }, dependsOnMethods = {"loadSOAPStringErrorTest"})
    public void loadStringTest() throws Exception {
	aparser.initAlignment( null );
	// a regular alignment, out of a SOAP message
	alignment = aparser.parseString( readFileAsString( "examples/rdf/newsample.rdf" ) );
	assertNotNull( alignment );
	assertTrue( alignment instanceof URIAlignment );
	assertEquals( alignment.nbCells(), 2 );
	double min = 1.;
	double max = 0.;
	for ( Cell c : alignment ) {
	    double v = c.getStrength();
	    if ( v < min ) min = v;
	    if ( v > max ) max = v;
	}
	assertEquals( min, 0.4666666666666667 );
	assertEquals( max, 1. );
    }

    @Test(groups = { "full", "io", "raw" }, dependsOnMethods = {"loadSOAPStringErrorTest"})
    public void loadSOAPStringTest() throws Exception {
	aparser.initAlignment( null );
	aparser.setEmbedded( true );
	alignment = aparser.parseString( readFileAsString( "test/input/soap.xml" ) );
	assertNotNull( alignment );
	assertTrue( alignment instanceof URIAlignment );
	assertEquals( alignment.getOntology2URI().toString(), "http://alignapi.gforge.inria.fr/tutorial/edu.mit.visus.bibtex.owl" );
	assertEquals( alignment.nbCells(), 57 );
    }
}
