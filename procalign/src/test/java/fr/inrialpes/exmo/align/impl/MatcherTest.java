package fr.inrialpes.exmo.align.impl;/*
 * $Id: MatcherTest.java 1843 2013-03-25 11:10:54Z euzenat $
 *
 * Copyright (C) INRIA, 2008-2011, 2013
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

//package 

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import fr.inrialpes.exmo.align.impl.MatrixMeasure;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
//import org.testng.annotations.*;

import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Evaluator;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;

import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.net.URI;
import java.util.Properties;

/**
 * These tests corresponds to the README file in the main directory
 */

public class MatcherTest {

    private AlignmentProcess alignment = null;

    // Add one test with instances in the ontology (the same ontology)

    @Test(groups = {"full", "impl", "raw"})
    public void routineTest8() throws Exception {
    /*
$ java -jar lib/Procalign.jar file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -o examples/rdf/bibref.rdf
	*/
        Properties params = new Properties();
        params.setProperty("stringFunction", "levenshteinDistance");
        params.setProperty("noinst", "1");
        alignment = new StringDistAlignment();
        assertNotNull(alignment, "ObjectAlignment should not be null");
        assertEquals(alignment.nbCells(), 0);
        alignment.init(new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl"), new URI("file:examples/rdf/edu.mit.visus.bibtex.owl"));
        alignment.align((Alignment) null, params);
        assertEquals(alignment.nbCells(), 42/*44*/);
        FileOutputStream stream = new FileOutputStream("test/output/bibref.rdf");
        PrintWriter writer = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(stream, "UTF-8")), true);
        AlignmentVisitor renderer = new RDFRendererVisitor(writer);
        alignment.render(renderer);
        writer.flush();
        writer.close();
        //assertEquals( stream.toString().length(), 1740, "Rendered differently" );
	/*
$ java -jar lib/Procalign.jar file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=subStringDistance -t .4 -o examples/rdf/bibref2.rdf
	*/
        alignment.cut("hard", 0.55);
        assertEquals(alignment.nbCells(), 32); /* With  .4, I have either 36 or 35! */
        stream = new FileOutputStream("test/output/bibref2.rdf");
        writer = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(stream, "UTF-8")), true);
        alignment.render(new RDFRendererVisitor(writer));
        writer.flush();
        writer.close();
        //assertEquals( stream.toString().length(), 1740, "Rendered differently" );
    }

    /* This tests an error when the distance name is incorrect */
    @Test(groups = {"full", "impl", "raw"}, expectedExceptions = AlignmentException.class)
    public void routineTest9() throws Exception {
        Properties params = new Properties();
        params.setProperty("stringFunction", "teinDistance");
        params.setProperty("noinst", "1");
        alignment = new StringDistAlignment();
        assertNotNull(alignment, "ObjectAlignment should not be null");
        assertEquals(alignment.nbCells(), 0);
        alignment.init(new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl"), new URI("file:examples/rdf/edu.mit.visus.bibtex.owl"));
        alignment.align((Alignment) null, params);
    }

    /* This tests an errors in extraction methods (Hungarian method) */
    @Test(groups = {"full", "impl", "raw"})
    public void hungarianExtractionTest() throws Exception {
        StringDistAlignment dal = new StringDistAlignment();
        dal.init(new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl"), new URI("file:examples/rdf/edu.mit.visus.bibtex.owl"));
        assertEquals(dal.nbCells(), 0);
        // Extract with nothing
        Properties params = new Properties();
        params.setProperty("noinst", "1");
        dal.align((Alignment) null, params); // This initialises the matrix
        assertEquals(dal.nbCells(), 10);
        // ****CLASSICAL EXTRACTIONS****
        // Test ?* extraction
        dal.deleteAllCells();
        dal.extractqs(0., params);
        assertEquals(dal.nbCells(), 10);
        // Test ** extraction
        dal.deleteAllCells();
        dal.extractss(0., params);
        assertEquals(dal.nbCells(), 10); // Because all is 0.
        // Test 11 extraction
        dal.deleteAllCells();
        dal.extractqq(0., params);
        assertEquals(dal.nbCells(), 10);
        dal.deleteAllCells();
        dal.extractqqgreedy(0., params);
        assertEquals(dal.nbCells(), 10);
        // ****ZERO'ED ALIGNMENT****
        // Do it with only one cell... (all 0. but 1)
        MatrixMeasure mm = (MatrixMeasure) dal.getSimilarity();
        for (int i = mm.nbclass1 - 1; i >= 0; i--) {
            for (int j = mm.nbclass2 - 1; j >= 0; j--) {
                mm.clmatrix[i][j] = 1.; // this is a distance...
            }
        }
        // Useless
        for (int i = mm.nbprop1 - 1; i >= 0; i--) {
            for (int j = mm.nbprop2 - 1; j >= 0; j--) {
                mm.prmatrix[i][j] = 1.; // distance...
            }
        }
        //dal.printDistanceMatrix( params );
        // Test ** extraction
        dal.deleteAllCells();
        //printAlignment( dal );
        dal.extractss(0., params);
        assertEquals(dal.nbCells(), 0);
        dal.deleteAllCells();
        dal.extractqs(0., params);
        assertEquals(dal.nbCells(), 0);
        dal.deleteAllCells();
        dal.extractqs(1., params);
        assertEquals(dal.nbCells(), 0);
        // Test 11 extraction
        dal.deleteAllCells();
        dal.extractqq(0., params);
        assertEquals(dal.nbCells(), 0);
        dal.deleteAllCells();
        dal.extractqqgreedy(0., params);
        assertEquals(dal.nbCells(), 0);
        // ****ADDED ONE EXPECTED CORRESPONDENCE****
        // Do it with only one cell... (all 0. but 1)
        mm.clmatrix[5][5] = .5;
        // Test ** extraction
        dal.deleteAllCells();
        //printAlignment( dal );
        dal.extractqs(0., params);
        assertEquals(dal.nbCells(), 1);
        // ********  TEST THIS *******
        dal.deleteAllCells();
        dal.extractss(0., params);
        assertEquals(dal.nbCells(), 1);
        dal.deleteAllCells();
        dal.extractqs(.6, params);
        assertEquals(dal.nbCells(), 0);
        // Test 11 extraction
        dal.deleteAllCells();
        dal.extractqq(0., params);
        assertEquals(dal.nbCells(), 1);
        dal.deleteAllCells();
        dal.extractqqgreedy(0., params);
        assertEquals(dal.nbCells(), 1);
        // ****ADDED ONE EXPECTED CORRESPONDENCE****
        // Do it with only one cell... (all 0. but 1)
        mm.clmatrix[5][8] = .5;
        mm.clmatrix[8][8] = .5;
        mm.clmatrix[8][5] = .5;
        // Test ** extraction
        dal.deleteAllCells();
        dal.extractss(0., params);
        assertEquals(dal.nbCells(), 4);
        dal.deleteAllCells();
        //printAlignment( dal );
        dal.extractqs(0., params);
        assertEquals(dal.nbCells(), 2);
        // ********  TEST THIS *******
        dal.deleteAllCells();
        dal.extractqs(.6, params);
        assertEquals(dal.nbCells(), 0);
        // Test 11 extraction
        dal.deleteAllCells();
        dal.extractqq(0., params);
        assertEquals(dal.nbCells(), 2);
        dal.deleteAllCells();
        dal.extractqqgreedy(0., params);
        assertEquals(dal.nbCells(), 2);
    }

    /* This identifies a very specific bug in the Hungarian algorithm when:
     * class2 < class1
     * a subset of class2 is matched with a subset of class1
     * In a simple 0/1 alignment
     */
    @Test(groups = {"full", "impl", "raw"}, dependsOnMethods = {"hungarianExtractionTest"})
    public void naughtyHungarianExtractionTest() throws Exception {
        StringDistAlignment dal = new StringDistAlignment();
        dal.init(new URI("file:examples/rdf/edu.mit.visus.bibtex.owl"), new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl"));
        assertEquals(dal.nbCells(), 0);
        // Extract with nothing
        Properties params = new Properties();
        params.setProperty("noinst", "1");
        dal.align((Alignment) null, params); // This initialises the matrix
        assertEquals(dal.nbCells(), 10);
        // Test 11 extraction
        dal.deleteAllCells();
        dal.extractqq(0., params);
        assertEquals(dal.nbCells(), 10); // **** java.lang.ArrayIndexOutOfBoundsException: 0
    }

    public void printAlignment(Alignment dal) {
        try {
            for (Cell c : dal) {
                System.err.println("< " + c.getObject1AsURI(dal).getFragment() + " " + c.getRelation() + " " + c.getObject1AsURI(dal).getFragment() + " / " + c.getStrength());
            }
        } catch (Exception e) {
        }
    }

}
