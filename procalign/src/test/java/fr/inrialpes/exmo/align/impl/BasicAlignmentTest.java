package fr.inrialpes.exmo.align.impl;/*
 * $Id: BasicAlignmentTest.java 1605 2011-05-21 10:56:27Z euzenat $
 *
 * Copyright (C) INRIA, 2008-2011
 * Copyright (C) FZI/Juergen Bock, 2010
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
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.assertNull;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.net.URI;

import fr.inrialpes.exmo.align.parser.AlignmentParser;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import org.semanticweb.owl.align.AlignmentException;

public class BasicAlignmentTest {

    private Alignment alignment = null;

    @BeforeClass(groups = { "full", "raw" })
    private void init(){
	alignment = new BasicAlignment();
    }

    @Test(groups = { "full", "raw" })
    public void aFastTest() {
	assertNotNull( alignment, "Alignment was null" );
    }

    @Test(groups = { "full", "raw" })
    public void someCutTest() throws AlignmentException {
	// THIS SHOULD BE REPLACED WITH ALIGNMENT BUILT IN PREVIOUS TESTS
	AlignmentParser aparser = new AlignmentParser( 0 );
	assertNotNull( aparser, "AlignmentParser was null" );
	Alignment result = aparser.parse( "file:examples/rdf/newsample.rdf" );
	assertNotNull( result, "URIAlignment(result) was null" );
	assertTrue( result instanceof URIAlignment );
	assertEquals( result.nbCells(), 2, "Alignment should contain 2 cells" );
	assertEquals( ((BasicAlignment)result).minConfidence(), 0.4666666666666667 );
	assertEquals( ((BasicAlignment)result).maxConfidence(), 1. );
	assertEquals( ((BasicAlignment)result).avgConfidence(), 0.7333333333333334 );
	assertEquals( ((BasicAlignment)result).varianceConfidence(), .07111111111111101 );
	result.cut( "hard", .5 );
	assertEquals( result.nbCells(), 1, "Alignment should contain 1 cell" );
    }

    private static Cell cell1, cell2, cell3, cell4, cell5, cell6;
    private static Cell ucell1, ucell2, ucell3, ucell4, ucell5, ucell6;

    /**
     * @throws java.lang.Exception
     */
    @BeforeClass(groups = { "full", "raw" })
	public static void setUpBeforeClass() throws Exception {
	URI cls1 = URI.create( "http://example.org/test#cls1" );
	URI cls2 = URI.create( "http://example.org/test#cls2" );
	URI cls3 = URI.create( "http://example.org/test#cls3" );
	URI cls4 = URI.create( "http://example.org/test#cls3" );
	Relation rel1 = new BasicRelation( "=" );
	Relation rel2 = new BasicRelation( "<" );
	cell1 = new BasicCell( "1", cls1, cls2, rel1, 0);
	cell2 = new BasicCell( "2", cls1, cls2, rel1, 0);
	cell3 = new BasicCell( "3", cls1, cls3, rel1, 0);
	cell4 = new BasicCell( "4", cls1, cls2, rel2, 0);
	cell5 = new BasicCell( "5", cls1, cls2, rel1, .5);
	cell6 = new BasicCell( "6", cls1, cls4, rel1, 0);
	ucell1 = new URICell( "1", cls1, cls2, rel1, 0);
	ucell2 = new URICell( "2", cls1, cls2, rel1, 0);
	ucell3 = new URICell( "3", cls1, cls3, rel1, 0);
	ucell4 = new URICell( "4", cls1, cls2, rel2, 0);
	ucell5 = new URICell( "5", cls1, cls2, rel1, .5);
	ucell6 = new URICell( "6", cls1, cls4, rel1, 0);
	assertTrue( cls3.equals( cls4 ) ); // Check that URIs are correct!
	assertTrue( cls3.hashCode() == cls4.hashCode() );
    }
    
    @Test(groups = { "full", "raw" })
	public void testEquals() {
	assertTrue( cell1.equals( cell1 ) ); // 1 == 1
	assertTrue( cell1.equals( cell2 ) ); // 1 == 2
	assertTrue( cell2.equals( cell1 ) ); // 2 == 1
	assertTrue( !cell1.equals( null ) ); // 1 != null
	assertTrue( !cell1.equals( cell3 ) ); // 1 != 3
	assertTrue( !cell1.equals( cell4 ) ); // 1 != 4
	assertTrue( !cell1.equals( cell5 ) ); // 1 != 5
	assertTrue( cell6.equals( cell3 ) ); // 6 == 3
    }

    @Test(groups = { "full", "raw" })
	public void testEqualsObject() {
	assertTrue( cell1.equals( (Object) cell1 ) ); // 1 == 1
	assertTrue( cell1.equals( (Object) cell2 ) ); // 1 == 2
	assertTrue( cell2.equals( (Object) cell1 ) ); // 2 == 1
	assertTrue( !cell1.equals( (Object) null ) ); // 1 != null
	assertTrue( !cell1.equals( (Object) cell3 ) ); // 1 != 3
	assertTrue( !cell1.equals( (Object) cell4 ) ); // 1 != 4
	assertTrue( !cell1.equals( (Object) cell5 ) ); // 1 != 5
	assertTrue( cell6.equals( (Object)cell3 ) ); // 6 == 3
    }
	
    // These have been added because equals is not redefined in URICell anymore
    @Test(groups = { "full", "raw" })
	public void testUEquals() {
	assertTrue( ucell1.equals( ucell1 ) ); // 1 == 1
	assertTrue( ucell1.equals( ucell2 ) ); // 1 == 2
	assertTrue( ucell2.equals( ucell1 ) ); // 2 == 1
	assertTrue( !ucell1.equals( null ) ); // 1 != null
	assertTrue( !ucell1.equals( ucell3 ) ); // 1 != 3
	assertTrue( !ucell1.equals( ucell4 ) ); // 1 != 4
	assertTrue( !ucell1.equals( ucell5 ) ); // 1 != 5
	assertTrue( cell6.equals( cell3 ) ); // 6 == 3
    }

    @Test(groups = { "full", "raw" })
	public void testUEqualsObject() {
	assertTrue( ucell1.equals( (Object) ucell1 ) ); // 1 == 1
	assertTrue( ucell1.equals( (Object) ucell2 ) ); // 1 == 2
	assertTrue( ucell2.equals( (Object) ucell1 ) ); // 2 == 1
	assertTrue( !ucell1.equals( (Object) null ) ); // 1 != null
	assertTrue( !ucell1.equals( (Object) ucell3 ) ); // 1 != 3
	assertTrue( !ucell1.equals( (Object) ucell4 ) ); // 1 != 4
	assertTrue( !ucell1.equals( (Object) ucell5 ) ); // 1 != 5
	assertTrue( ucell6.equals( (Object)ucell3 ) ); // 6 == 3
    }
	
    @Test(groups = { "full", "raw" })
	public void testHashCodeEquals() {
	assertTrue( cell1.equals( cell2 ) && cell1.hashCode() == cell2.hashCode() );
	assertTrue( cell1.equals( cell1 ) && cell1.hashCode() == cell1.hashCode() );
	assertTrue( cell2.equals( cell1 ) && cell2.hashCode() == cell1.hashCode() );
	assertTrue( cell3.equals( cell6 ) && cell3.hashCode() == cell6.hashCode() );
    }
	
    /**
     * @throws java.lang.Exception
     */
    @AfterClass(groups = { "raw", "full" }, alwaysRun = true )
	public static void tearDownAfterClass() throws Exception {
	cell1 = cell2 = cell3 = cell4 = cell5 = cell6 = null;
    }

}
