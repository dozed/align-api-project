package fr.inrialpes.exmo.align.impl;/*
* $Id: READMETest.java 1828 2013-03-09 18:18:05Z euzenat $
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

import org.testng.annotations.Test;

import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.Alignment;

import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.impl.eval.SemPRecEvaluator;
import fr.inrialpes.exmo.align.impl.eval.WeightedPREvaluator;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.util.NullStream;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.net.URI;
import java.util.Properties;

/**
 * These tests corresponds to the README file in the main directory
 */

public class READMETest {

    private AlignmentProcess alignment = null;

    @Test(groups = {"full", "impl", "raw"})
    public void routineTest1() {
    /*
$ java -jar lib/procalign.jar --help
	*/
    }

    @Test(groups = {"full", "impl", "raw"})
    public void routineTest2() throws Exception {
	/*
$ java -jar lib/procalign.jar file://$CWD/examples/rdf/onto1.owl file://$CWD/examples/rdf/onto2.owl
	*/
        Properties params = new Properties();
        alignment = new StringDistAlignment();
        assertNotNull(alignment, "ObjectAlignment should not be null");
        assertEquals(alignment.nbCells(), 0);
        alignment.init(new URI("file:examples/rdf/onto1.owl"), new URI("file:examples/rdf/onto2.owl"));
        alignment.align((Alignment) null, params);
        assertEquals(alignment.nbCells(), 1);
    }

    @Test(groups = {"full", "impl", "raw"})
    public void routineTest3() throws Exception {
	/*
$ java -jar lib/procalign.jar file://$CWD/examples/rdf/onto1.owl file://$CWD/examples/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -r fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor
	*/
        Properties params = new Properties();
        params.setProperty("stringFunction", "levenshteinDistance");
        alignment = new StringDistAlignment();
        assertNotNull(alignment, "ObjectAlignment should not be null");
        assertEquals(alignment.nbCells(), 0);
        alignment.init(new URI("file:examples/rdf/onto1.owl"), new URI("file:examples/rdf/onto2.owl"));
        alignment.align((Alignment) null, params);
        assertEquals(alignment.nbCells(), 3);
        // Rendering
        // Playing with threshold
	/*
$ java -jar lib/procalign.jar file://$CWD/examples/rdf/onto1.owl file://$CWD/examples/rdf/onto2.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -t 0.4 -o examples/rdf/sample.rdf
	*/
        alignment.cut("hard", 0.5);
        assertEquals(alignment.nbCells(), 1);
    }

    @Test(groups = {"full", "impl", "raw"})
    public void routineTest5() throws Exception {
	/*
$ java -cp lib/procalign.jar fr.inrialpes.exmo.align.cli.ParserPrinter examples/rdf/newsample.rdf
	*/
        AlignmentParser aparser = new AlignmentParser(0);
        assertNotNull(aparser, "AlignmentParser was null");
        Alignment result = aparser.parse("file:examples/rdf/newsample.rdf");
        assertNotNull(result, "URIAlignment(result) was null");
        assertTrue(result instanceof URIAlignment);
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        PrintWriter writer = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(stream, "UTF-8")), true);
        AlignmentVisitor renderer = new RDFRendererVisitor(writer);
        result.render(renderer);
        writer.flush();
        writer.close();
        assertTrue((1973 <= stream.toString().length()) && (stream.toString().length() <= 1974), "Rendered differently: expected " + 1974 + " but was " + stream.toString().length());
    }

    @Test(groups = {"full", "impl", "raw"}, dependsOnMethods = {"routineTest3"})
    public void routineTest6() throws Exception {
	/*
$ java -jar lib/procalign.jar file://$CWD/examples/rdf/onto1.owl file://$CWD/examples/rdf/onto2.owl -a examples/rdf/sample.rdf

	*/
    }

    @Test(groups = {"full", "impl", "raw"}, dependsOnMethods = {"routineTest3"})
    public void routineTest7() throws Exception {
    /*
$ java -jar lib/Procalign.jar file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl
    */
        Properties params = new Properties();
        alignment = new StringDistAlignment();
        assertNotNull(alignment, "ObjectAlignment should not be null");
        assertEquals(alignment.nbCells(), 0);
        alignment.init(new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl"), new URI("file:examples/rdf/edu.mit.visus.bibtex.owl"));
        alignment.align((Alignment) null, params);
        assertEquals(alignment.nbCells(), 10);
        URIAlignment al = ((ObjectAlignment) alignment).toURIAlignment();
        assertNotNull(al, "URIAlignment should not be null");
        assertEquals(al.nbCells(), 10);
        ObjectAlignment al2 = AlignmentTransformer.toObjectAlignment(al);
        assertNotNull(al2, "ObjectAlignment should not be null");
        assertEquals(al2.nbCells(), 10);
    }

    @Test(groups = {"full", "impl", "raw"}, dependsOnMethods = {"routineTest7"})
    public void routineTest8() throws Exception {
	/*
$ java -jar lib/Procalign.jar file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -o examples/rdf/bibref.rdf
	*/
        Properties params = new Properties();
        params.setProperty("stringFunction", "levenshteinDistance");
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
        Alignment al2 = (Alignment) alignment.clone();
        alignment.cut("hard", 0.55);
        assertEquals(alignment.nbCells(), 32); /* checked! */
        stream = new FileOutputStream("test/output/bibref2.rdf");
        writer = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(stream, "UTF-8")), true);
        alignment.render(new RDFRendererVisitor(writer));
        writer.flush();
        writer.close();
        //assertEquals( stream.toString().length(), 1740, "Rendered differently" );

        // Tests of cutting -- all checked
        Alignment al = (Alignment) al2.clone();
        al.cut("hard", 0.55);
        assertEquals(al.nbCells(), 32);
        al = (Alignment) al2.clone();
        al.cut("best", 0.55);
        assertEquals(al.nbCells(), 42/*44*/);
        al = (Alignment) al2.clone();
        al.cut("span", 0.55);
        assertEquals(al.nbCells(), 33);
        al = (Alignment) al2.clone();
        al.cut("prop", 0.55);
        assertEquals(al.nbCells(), 32);
        al = (Alignment) al2.clone();
        al.cut("prop", 0.55);
        assertEquals(al.nbCells(), 32);
        al = (Alignment) al2.clone();
        al.cut("perc", 0.55);
        assertEquals(al.nbCells(), 23/*24*/);
        al = (Alignment) al2.clone();
        al.cut("hardgap", 0.5);
        assertEquals(al.nbCells(), 42/*44*/);
        al.cut("hardgap", 0.05);
        assertEquals(al.nbCells(), 10);
        al = (Alignment) al2.clone();
        al.cut("propgap", 0.55);
        assertEquals(al.nbCells(), 42/*44*/);
        al.cut("propgap", 0.1);
        assertEquals(al.nbCells(), 10);

    }

    @Test(expectedExceptions = AlignmentException.class, groups = {"full", "impl", "raw"}, dependsOnMethods = {"routineTest8"})
    public void routineErrorTest8() throws Exception {
        alignment.cut("prec", 0.55);
    }

    @Test(groups = {"full", "impl", "raw"}, dependsOnMethods = {"routineTest8"})
    public void routineEvalTest() throws Exception {
	/*
$ java -cp lib/procalign.jar fr.inrialpes.exmo.align.cli.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://$CWD/examples/rdf/bibref2.rdf file://$CWD/examples/rdf/bibref.rdf
	*/
        AlignmentParser aparser1 = new AlignmentParser(0);
        assertNotNull(aparser1);
        Alignment align1 = aparser1.parse("test/output/bibref2.rdf");
        assertNotNull(align1);
        aparser1.initAlignment(null);
        Alignment align2 = aparser1.parse("test/output/bibref.rdf");
        assertNotNull(align2);
        Properties params = new Properties();
        assertNotNull(params);
        PRecEvaluator eval = new PRecEvaluator(align1, align2);
        assertNotNull(eval);
        eval.eval(params);

        // This only output the result to check that this is possible
        OutputStream stream = new NullStream();
        PrintWriter writer = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(stream, "UTF-8")), true);
        eval.write(writer);
        writer.flush();
        writer.close();
        assertEquals(eval.getPrecision(), 0.7619047619047619);
        assertEquals(eval.getRecall(), 1.0);
        assertEquals(eval.getNoise(), 0.23809523809523814);
        assertEquals(eval.getFmeasure(), 0.8648648648648648);
        assertEquals(eval.getOverall(), 0.6875);
    }

    @Test(groups = {"full", "impl", "raw"}, dependsOnMethods = {"routineTest8"})
    public void emptyEvalTest() throws Exception {
	/*
$ java -cp lib/procalign.jar fr.inrialpes.exmo.align.cli.EvalAlign -i fr.inrialpes.exmo.align.impl.eval.PRecEvaluator file://$CWD/examples/rdf/bibref2.rdf file://$CWD/examples/rdf/bibref.rdf
	*/
        AlignmentParser aparser1 = new AlignmentParser(0);
        assertNotNull(aparser1);
        Alignment align1 = aparser1.parse("test/output/bibref2.rdf");
        assertNotNull(align1);
        aparser1.initAlignment(null);
        Alignment align2 = new ObjectAlignment();
        assertNotNull(align2);
        align2.init(align1.getOntology1URI(), align1.getOntology2URI());
        Properties params = new Properties();
        assertNotNull(params);
        PRecEvaluator eval = new PRecEvaluator(align1, align2);
        assertNotNull(eval);
        eval.eval(params);

        assertEquals(eval.getPrecision(), 1.);
        assertEquals(eval.getRecall(), 0.0);
        assertEquals(eval.getNoise(), 0.);
        assertEquals(eval.getFmeasure(), 0.);
        assertEquals(eval.getOverall(), 0.);
    }

    @Test(expectedExceptions = AlignmentException.class, groups = {"full", "impl", "raw"}, dependsOnMethods = {"routineEvalTest"})
    public void routineErrorEvalTest() throws Exception {
        AlignmentParser aparser1 = new AlignmentParser(0);
        assertNotNull(aparser1);
        Alignment align1 = aparser1.parse("test/output/bibref2.rdf");
        assertNotNull(align1);
        aparser1.initAlignment(null);
        Alignment align2 = aparser1.parse("test/output/bibref.rdf");
        assertNotNull(align2);
        Properties params = new Properties();
        assertNotNull(params);
        PRecEvaluator eval = new PRecEvaluator(align1, align2);
        assertNotNull(eval);
        eval.eval(params);

        eval.getFallout(); //4.2: Deprecated raises an error
    }

    @Test(groups = {"full", "sem"}, dependsOnMethods = {"routineEvalTest"})
    public void semanticEvalTest() throws Exception {
        AlignmentParser aparser1 = new AlignmentParser(0);
        assertNotNull(aparser1);
        Alignment align1 = aparser1.parse("test/output/bibref2.rdf");
        assertNotNull(align1);
        aparser1.initAlignment(null);
        Alignment align2 = aparser1.parse("test/output/bibref.rdf");
        assertNotNull(align2);
        Properties params = new Properties();
        assertNotNull(params);
        SemPRecEvaluator eval = new SemPRecEvaluator(align1, align2);
        assertNotNull(eval);
        eval.eval(params);

        // This only output the result to check that this is possible
        OutputStream stream = new NullStream();
        PrintWriter writer = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(stream, "UTF-8")), true);
        eval.write(writer);
        writer.flush();
        writer.close();
        // These figures are different than those of classical PR
        // only because some correspondences cannot be transcribed
        // author<->hasAuthor/editor<->hasEditor/firstAuthor/hasAuthor
        // because Object/DataProperties
        assertEquals(eval.getPrecision(), 0.6904761904761905);
        assertEquals(eval.getRecall(), 0.90625);
        assertEquals(eval.getNoise(), 0.30952380952380953);
        assertEquals(eval.getFmeasure(), 0.7837837837837837);
        assertEquals(eval.getOverall(), 0.5);
    }

    @Test(groups = {"full", "sem"}, dependsOnMethods = {"routineEvalTest"}, expectedExceptions = org.semanticweb.owl.align.AlignmentException.class)
    public void semanticIDDLEvalTest() throws Exception {
        AlignmentParser aparser1 = new AlignmentParser(0);
        assertNotNull(aparser1);
        Alignment align1 = aparser1.parse("test/output/bibref2.rdf");
        assertNotNull(align1);
        aparser1.initAlignment(null);
        Alignment align2 = aparser1.parse("test/output/bibref.rdf");
        assertNotNull(align2);
        SemPRecEvaluator eval = new SemPRecEvaluator(align1, align2);
        assertNotNull(eval);
        Properties params = new Properties();
        params.setProperty("semantics", "DL");
        assertNotNull(params);
        eval.eval(params);

        // This only output the result to check that this is possible
        OutputStream stream = new NullStream();
        PrintWriter writer = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(stream, "UTF-8")), true);
        eval.write(writer);
        writer.flush();
        writer.close();
        // These figures are different than those of classical PR
        // only because some correspondences cannot be transcribed
        // author<->hasAuthor/editor<->hasEditor/firstAuthor/hasAuthor
        // because Object/DataProperties
        assertEquals(eval.getPrecision(), 0.6904761904761905);
        assertEquals(eval.getRecall(), 0.90625);
        assertEquals(eval.getNoise(), 0.30952380952380953);
        assertEquals(eval.getFmeasure(), 0.7837837837837837);
        assertEquals(eval.getOverall(), 0.5);
    }


    @Test(groups = {"full", "impl"}, dependsOnMethods = {"routineEvalTest"})
    public void weightedEvalTest() throws Exception {
        AlignmentParser aparser1 = new AlignmentParser(0);
        assertNotNull(aparser1);
        Alignment align1 = aparser1.parse("test/output/bibref2.rdf");
        assertNotNull(align1);
        aparser1.initAlignment(null);
        Alignment align2 = aparser1.parse("test/output/bibref.rdf");
        assertNotNull(align2);
        Properties params = new Properties();
        assertNotNull(params);
        WeightedPREvaluator eval = new WeightedPREvaluator(align1, align2);
        assertNotNull(eval);
        eval.eval(params);

        // This only output the result to check that this is possible
        OutputStream stream = new NullStream();
        PrintWriter writer = new PrintWriter(
                new BufferedWriter(
                        new OutputStreamWriter(stream, "UTF-8")), true);
        eval.write(writer);
        writer.flush();
        writer.close();
        assertEquals(eval.getPrecision(), 0.8732495020298934);
        assertEquals(eval.getRecall(), 1.0);
        assertEquals(eval.getNoise(), 0.12675049797010662);
        assertEquals(eval.getFmeasure(), 0.9323365639052582);
        assertEquals(eval.getOverall(), 0.8548519092476188);
    }

    @Test(groups = {"full", "impl", "raw"})
    public void routineMatrixTest() throws Exception {
	/*
$ java -jar lib/Procalign.jar file://$CWD/examples/rdf/edu.umbc.ebiquity.publication.owl file://$CWD/examples/rdf/edu.mit.visus.bibtex.owl -i fr.inrialpes.exmo.align.impl.method.StringDistAlignment -DstringFunction=levenshteinDistance -DprintMatrix=1 -o /dev/null > examples/rdf/matrix.tex
	*/
    }

}
