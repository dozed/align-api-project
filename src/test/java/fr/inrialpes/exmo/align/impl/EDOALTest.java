package fr.inrialpes.exmo.align.impl;/*
 * $Id: EDOALTest.java 1724 2012-05-09 20:35:49Z euzenat $
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
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Alignment;

import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;


// JE2010: THIS SHOULD TEST ALL ERRORS RAISED BY CONSTRUCTORS

/**
 * These tests corresponds to the tests presented in the examples/omwg directory
 */

public class EDOALTest {

    private Alignment alignment = null;
    private AlignmentParser aparser1 = null;

    @Test(groups = { "full", "omwg", "raw" })
    public void loadPrintTest() throws Exception {
	/*
java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.cli.ParserPrinter wine.xml > wine2.xml
	*/
	aparser1 = new AlignmentParser( 0 );
	assertNotNull( aparser1 );
	alignment = aparser1.parse( "file:examples/omwg/wine.xml" );
	assertNotNull( alignment );
	assertTrue( alignment instanceof EDOALAlignment );
	FileOutputStream stream = new FileOutputStream("test/output/wine2.xml");
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new RDFRendererVisitor( writer );
	alignment.render( renderer );
	writer.flush();
	writer.close();
    }

    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"loadPrintTest"})
    public void roundTripTest() throws Exception {
	/*
java -cp ../../lib/procalign.jar fr.inrialpes.exmo.align.cli.ParserPrinter wine2.xml > wine3.xml
	*/
	aparser1.initAlignment( null );
	alignment = aparser1.parse( "file:test/output/wine2.xml" );
	assertNotNull( alignment );
	FileOutputStream stream = new FileOutputStream("test/output/wine3.xml");
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new RDFRendererVisitor( writer );
	alignment.render( renderer );
	writer.flush();
	writer.close();
    }

    /* diff wine2.xml wine3.xml */
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"roundTripTest"})
    public void diffTest() throws Exception {
	aparser1.initAlignment( null );
	Alignment oldal = aparser1.parse( "file:test/output/wine2.xml" );
	aparser1.initAlignment( null );
	alignment = aparser1.parse( "file:test/output/wine3.xml" );
	assertNotNull( alignment );
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new RDFRendererVisitor( writer );
	oldal.render( renderer );
	writer.flush();
	writer.close();
	String wine2 = stream.toString();
	stream = new ByteArrayOutputStream(); 
	writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	renderer = new RDFRendererVisitor( writer );
	alignment.render( renderer );
	writer.flush();
	writer.close();
	assertEquals( "".equals( wine2 ), false );
	// This tests the round triping
	assertEquals( wine2.length(), stream.toString().length() );
	// This provides an absolute value
	assertEquals( wine2.length(), 4669 );
	// This does not work because (at least) the order of correspondences is never the same...
	//assertEquals( wine2, stream.toString() );
    }

    /* This is round triping wrt converting to URIALignment... */
    @Test(expectedExceptions = AlignmentException.class, groups = { "full", "omwg", "raw" }, dependsOnMethods = {"roundTripTest"})
    public void anotherRoundTripTest() throws Exception {
	aparser1.initAlignment( null );
	EDOALAlignment eal = (EDOALAlignment)aparser1.parse( "file:test/output/wine2.xml" );
	assertNotNull( eal );
	assertEquals( eal.nbCells(), 5 ); 
	URIAlignment al = eal.toURIAlignment();
	assertNotNull( al );
	assertEquals( al.nbCells(), 3 );
	eal = EDOALAlignment.toEDOALAlignment( al ); // does not work because the ontology cannot be loaded!
	assertNotNull( eal );
	assertEquals( eal.nbCells(), 3 );
   }

}
