package fr.inrialpes.exmo.align.impl;/*
 * $Id: EDOALParserTest.java 1570 2011-04-23 15:20:27Z euzenat $
 *
 * Copyright (C) INRIA, 2009-2011
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Alignment;

import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.util.NullStream;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.OutputStream;
import java.io.PrintStream;


/**
 * These tests corresponds to the tests presented in the examples/omwg directory
 */

public class EDOALParserTest {

    private AlignmentParser aparser1 = null;

    @Test(groups = { "full", "omwg", "raw" })
    public void setUp() throws Exception {
	aparser1 = new AlignmentParser( 0 );
	assertNotNull( aparser1 );
    }

    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods={ "setUp" })
    public void typedParsingTest() throws Exception {
	AlignmentParser aparser2 = new AlignmentParser( 2 );
	aparser2.initAlignment( null );
	// Would be good to close System.err at that point...
	OutputStream serr = System.err;
	System.setErr( new PrintStream( new NullStream() ) );
	Alignment al = aparser2.parse( "file:examples/omwg/total.xml" );
	System.setErr( new PrintStream( serr ) );
	assertNotNull( al );
    }

    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods={ "typedParsingTest" })
    public void roundTripTest() throws Exception {
	// Load the full test
	aparser1.initAlignment( null );
	Alignment alignment = aparser1.parse( "file:examples/omwg/total.xml" );
	assertNotNull( alignment );
	// Print it in a string
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new RDFRendererVisitor( writer );
	alignment.render( renderer );
	writer.flush();
	writer.close();
	String str1 = stream.toString();
	// Read it again
	aparser1 = new AlignmentParser( 0 );
	aparser1.initAlignment( null );
	//System.err.println( str1 );
	Alignment al = aparser1.parseString( str1 );
	assertEquals( alignment.nbCells(), al.nbCells() );
	// Print it in another string
	stream = new ByteArrayOutputStream(); 
	writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	renderer = new RDFRendererVisitor( writer );
	al.render( renderer );
	writer.flush();
	writer.close();
	String str2 = stream.toString();
	// They should be the same... (no because of invertion...)
	//assertEquals( str1, str2 );
	// But have the same length
	assertEquals( str1.length(), str2.length() );
    }
}
