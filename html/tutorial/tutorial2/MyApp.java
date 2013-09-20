/*
 * $Id: MyApp.java 1404 2010-03-31 08:53:09Z euzenat $
 *
 * Copyright (C) INRIA, 2006-2010
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
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

// Alignment API classes
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Evaluator;

// Alignment API implementation classes
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicParameters;
import fr.inrialpes.exmo.align.impl.method.StringDistAlignment;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

// SAX standard classes
import org.xml.sax.SAXException;

// Java standard classes
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.File;
import java.net.URI;
import java.util.Properties;

/**
 * MyApp
 *
 * Takes two files as arguments and align them.
 * Match them with different matching methods
 * Merge the results
 * Selects the threshold that provide the best F-measure
 * Return the alignment trimmed at that threshold as OWL Axioms
 */

public class MyApp {

    public static void main( String[] args ) {
	URI onto1 = null;
	URI onto2 = null;
	Properties params = new BasicParameters();
	int question = 1;

	try {
	    // Loading ontologies
	    if (args.length >= 2) {
		onto1 = new URI(args[0]);
		onto2 = new URI(args[1]);
	    } else {
		System.err.println("Need two arguments to proceed");
		return ;
	    }


	    // Run two different alignment methods (e.g., ngram distance and smoa)
	    AlignmentProcess a1 = new StringDistAlignment();
	    params.setProperty("stringFunction","smoaDistance");
	    a1.init ( onto1, onto2 );
	    a1.align( (Alignment)null, params );
	    AlignmentProcess a2 = new StringDistAlignment();
	    a2.init ( onto1, onto2 );
	    params = new BasicParameters();
	    params.setProperty("stringFunction","ngramDistance");
	    a2.align( (Alignment)null, params );

	    if ( question == 2 ) {
		// Clone a1
		System.err.println( a1.nbCells() );
		BasicAlignment a3 = (BasicAlignment)(a1.clone());
		System.err.println( a3.nbCells() );
		
		// Merge the two results.
		a3.ingest( a2 );
		System.err.println( a3.nbCells() );

		// Invert the alignement
		Alignment a4 = a3.inverse();
		System.err.println( a4.nbCells() );
		
		// Trim above .5
		a4.cut( .5 );
		System.err.println( a4.nbCells() );
	    } else {
		// Merge the two results.
		((BasicAlignment)a1).ingest(a2);
		
		// Trim at various thresholds
		// Evaluate them against the references
		// and choose the one with the best F-Measure
		AlignmentParser aparser = new AlignmentParser(0);
		// Changed by Angel for Windows
		//Alignment reference = aparser.parse( "file://"+(new File ( "../refalign.rdf" ) . getAbsolutePath()) );
		Alignment reference = aparser.parse( new File( "../refalign.rdf" ).toURI() );
		
		double best = 0.;
		Alignment result = null;
		Properties p = new BasicParameters();
		for ( int i = 0; i <= 10 ; i += 2 ){
		    a1.cut( ((double)i)/10 );
		    // This operation must be repeated because the modifications in a1
		    // are not taken into account otherwise
		    Evaluator evaluator = new PRecEvaluator( reference, a1 );
		    evaluator.eval( p );
		    System.err.println("Threshold "+(((double)i)/10)+" : "+((PRecEvaluator)evaluator).getFmeasure()+" over "+a1.nbCells()+" cells");
		    if ( ((PRecEvaluator)evaluator).getFmeasure() > best ) {
			result = (BasicAlignment)((BasicAlignment)a1).clone();
			best = ((PRecEvaluator)evaluator).getFmeasure();
		    }
		}
		// Displays it as OWL Rules
		PrintWriter writer = new PrintWriter (
				      new BufferedWriter(
		                       new OutputStreamWriter( System.out, "UTF-8" )), true);
		AlignmentVisitor renderer = new OWLAxiomsRendererVisitor(writer);
		result.render(renderer);
		writer.flush();
		writer.close();
	    }
	} catch (Exception e) { e.printStackTrace(); };
    }
}
