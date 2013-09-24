/*
 * $Id: ExtGroupEval.java 1701 2012-03-10 15:54:01Z euzenat $
 *
 * Copyright (C) 2003 The University of Manchester
 * Copyright (C) 2003 The University of Karlsruhe
 * Copyright (C) 2003-2005, 2007-2012 INRIA
 * Copyright (C) 2004, Université de Montréal
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

/* This program evaluates the results of several ontology aligners in a row.
   It uses the generalisations of precision and recall described in
   [Ehrig & Euzenat 2005].
*/
package fr.inrialpes.exmo.align.cli;

import fr.inrialpes.exmo.align.impl.AlignmentTransformer;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.eval.ExtPREvaluator;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

import java.io.File;
import java.io.PrintStream;
import java.io.FileOutputStream;
import java.lang.Integer;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Arrays;
import java.util.Formatter;
import java.util.Properties;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

/** A basic class for synthesizing the results of a set of alignments provided
    by different algorithms. The output is a table showing various generalisations
    of precision and recall for each test and for each algorithm.
    Average is also computed as Harmonic means.
    
    <pre>
    java -cp procalign.jar fr.inrialpes.exmo.align.util.ExtGroupEval [options]
    </pre>

    where the options are:
    <pre>
    -o filename --output=filename
    -f format = sepr (symetric/effort-based/precision-oriented/recall-oriented) --format=sepr
    -d debug --debug=level
    -r filename --reference=filename
    -s algo/measure
    -l list of compared algorithms
    -t output --type=output: xml/tex/html/ascii
   </pre>

   The input is taken in the current directory in a set of subdirectories
   (one per test which will be rendered by a line) each directory contains
   a number of alignment files (one per algorithms which will be renderer
   as a column).

    If output is requested (<CODE>-o</CODE> flags), then output will be
    written to <CODE>output</CODE> if present, stdout by default.

<pre>
$Id: ExtGroupEval.java 1701 2012-03-10 15:54:01Z euzenat $
</pre>

@author Sean K. Bechhofer
@author Jérôme Euzenat
    */

public class ExtGroupEval {

    Properties params = null;
    String filename = null;
    String reference = "refalign.rdf";
    String format = "s";
    int fsize = 2;
    String type = "html";
    boolean embedded = false;
    String dominant = "s";
    Vector<String> listAlgo = null;
    int debug = 0;
    String color = null;
    String ontoDir = null;

    public static void main(String[] args) {
	try { new ExtGroupEval().run( args ); }
	catch (Exception ex) { ex.printStackTrace(); };
    }

    public void run(String[] args) throws Exception {
	String listFile = "";
	LongOpt[] longopts = new LongOpt[10];

 	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	longopts[2] = new LongOpt("format", LongOpt.REQUIRED_ARGUMENT, null, 'f');
	longopts[3] = new LongOpt("type", LongOpt.REQUIRED_ARGUMENT, null, 't');
	longopts[4] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	longopts[5] = new LongOpt("sup", LongOpt.REQUIRED_ARGUMENT, null, 's');
	longopts[6] = new LongOpt("list", LongOpt.REQUIRED_ARGUMENT, null, 'l');
	longopts[7] = new LongOpt("color", LongOpt.OPTIONAL_ARGUMENT, null, 'c');
	longopts[8] = new LongOpt("reference", LongOpt.REQUIRED_ARGUMENT, null, 'r');
	longopts[9] = new LongOpt("directory", LongOpt.REQUIRED_ARGUMENT, null, 'w');

	Getopt g = new Getopt("", args, "ho:a:d::l:f:t:r:w:c::", longopts);
	int c;
	String arg;

	while ((c = g.getopt()) != -1) {
	    switch (c) {
	    case 'h' :
		usage();
		return;
	    case 'o' :
		/* Write output here */
		filename = g.getOptarg();
		break;
	    case 'r' :
		/* File name for the reference alignment */
		reference = g.getOptarg();
		break;
	    case 'f' :
		/* Sequence of results to print */
		format = g.getOptarg();
		break;
	    case 't' :
		/* Type of output (tex/html/xml/ascii) */
		type = g.getOptarg();
		break;
	    case 's' :
		/* Print per type or per algo */
		dominant = g.getOptarg();
		break;
	    case 'c' :
		/* Print colored lines */
		color = "lightblue";
		    //dominant = g.getOptarg();
		break;
	    case 'l' :
		/* List of filename */
		listFile = g.getOptarg();
		break;
	    case 'd' :
		/* Debug level  */
		arg = g.getOptarg();
		if ( arg != null ) debug = Integer.parseInt(arg.trim());
		else debug = 4;
		break;
	    case 'w' :
		/* Use the given ontology directory */
	    arg = g.getOptarg();
	    if ( arg != null ) ontoDir = g.getOptarg();
	    else ontoDir = null;
		break;
	    }
	}

	listAlgo = new Vector<String>();
	for ( String s : listFile.split(",") ) {
	    listAlgo.add( s );	    
	}

	params = new Properties();
	if (debug > 0) params.setProperty( "debug", Integer.toString( debug-1 ) );

	print( iterateDirectories() );
    }

    public Vector<Vector> iterateDirectories (){
	Vector<Vector> result = null;
	File [] subdir = null;
	try {
	    if (ontoDir == null) {
		subdir = (new File(System.getProperty("user.dir"))).listFiles(); 
	    } else {
		subdir = (new File(ontoDir)).listFiles();
	    }
	} catch (Exception e) {
	    System.err.println("Cannot stat dir "+ e.getMessage());
	    usage();
	}
	int size = subdir.length;
        Arrays.sort(subdir);
	result = new Vector<Vector>(size);
	int i = 0;
	for ( int j=0 ; j < size; j++ ) {
	    if( subdir[j].isDirectory() ) {
		if ( debug > 0 ) System.err.println("\nEntering directory "+subdir[j]);
		// eval the alignments in a subdirectory
		// store the result
		Vector vect = (Vector)iterateAlignments( subdir[j] );
		if ( vect != null ){
		    result.add(i, vect);
		    i++;
		}
	    }
	}
	return result;
    }

    public Vector<Object> iterateAlignments ( File dir ) {
	String prefix = dir.toURI().toString()+"/";
	Vector<Object> result = new Vector<Object>();
	boolean ok = false;
	result.add(0,(Object)dir.getName().toString());
	int i = 0;
	// for all alignments there,
	for ( String m : listAlgo ) {
	    i++;
	    // call eval
	    // store the result in a record
	    // return the record.
	    if ( debug > 1) System.err.println("  Considering result "+i);
	    Evaluator evaluator = eval( prefix+reference, prefix+m+".rdf");
	    if ( evaluator != null ) ok = true;
	    result.add( i, evaluator );
	}
	// Unload the ontologies.
	try {
	    OntologyFactory.clear();
	} catch ( OntowrapException owex ) { // only report
	    owex.printStackTrace();
	}

	if ( ok == true ) return result;
	else return null;
    }

    public Evaluator eval( String alignName1, String alignName2 ) {
	Evaluator eval = null;
	try {
	    int nextdebug;
	    if ( debug < 2 ) nextdebug = 0;
	    else nextdebug = debug - 2;
	    // Load alignments
	    AlignmentParser aparser = new AlignmentParser( nextdebug );
	    Alignment align1 = aparser.parse( alignName1 );
	    if ( debug > 1 ) System.err.println(" Alignment structure1 parsed");
	    aparser.initAlignment( null );
	    Alignment align2 = aparser.parse( alignName2 );
	    if ( debug > 1 ) System.err.println(" Alignment structure2 parsed");
	    // Create evaluator object
	    eval = new ExtPREvaluator(AlignmentTransformer.toObjectAlignment((URIAlignment) align1),
				      AlignmentTransformer.toObjectAlignment((URIAlignment) align2) );
	    // Compare
	    params.setProperty( "debug", Integer.toString( nextdebug ) );
	    eval.eval( params ) ;
	} catch (Exception ex) {
	    if ( debug > 1 ) {
		ex.printStackTrace();
	    } else {
		System.err.println("ExtGroupEval: "+ex);
		System.err.println(alignName1+ " - "+alignName2 );
	    }
	};
	return eval;
    }

    /**
     * This does not only print the results but compute the average as well
     */
    public void print( Vector<Vector> result ) {
	// variables for computing iterative harmonic means
	int expected = 0; // expected so far
	int foundVect[]; // found so far
	double symVect[]; // symmetric similarity
	double effVect[]; // effort-based similarity
	double precOrVect[]; // precision-oriented similarity
	double recOrVect[]; // recall-oriented similarity
	PrintStream writer = null;

	fsize = format.length();
	try {
	    // Print result
	    if ( filename == null ) {
		writer = System.out;
	    } else {
		writer = new PrintStream(new FileOutputStream( filename ));
	    }
	    Formatter formatter = new Formatter(writer);
	    // Print the header
	    writer.println("<html><head></head><body>");
	    writer.println("<table border='2' frame='sides' rules='groups'>");
	    writer.println("<colgroup align='center' />");
	    // for each algo <td spancol='2'>name</td>
	    for ( String m : listAlgo ) {
		writer.println("<colgroup align='center' span='"+2*fsize+"' />");
	    }
	    // For each file do a
	    writer.println("<thead valign='top'><tr><th>algo</th>");
	    // for each algo <td spancol='2'>name</td>
	    for ( String m : listAlgo ) {
		writer.println("<th colspan='"+((2*fsize))+"'>"+m+"</th>");
	    }
	    writer.println("</tr></thead><tbody><tr><td>test</td>");
	    // for each algo <td>Prec.</td><td>Rec.</td>
	    for ( String m : listAlgo ) {
		for ( int i = 0; i < fsize; i++){
		    if ( format.charAt(i) == 's' ) {
			writer.println("<td colspan='2'><center>Symmetric</center></td>");
		    } else if ( format.charAt(i) == 'e' ) {
			writer.println("<td colspan='2'><center>Effort</center></td>");
		    } else if ( format.charAt(i) == 'p' ) {
			writer.println("<td colspan='2'><center>Prec. orient.</center></td>");
		    } else if ( format.charAt(i) == 'r' ) {
			writer.println("<td colspan='2'><center>Rec. orient.</center></td>");
		    }
		}
		//writer.println("<td>Prec.</td><td>Rec.</td>");
	    }
	    writer.println("</tr></tbody><tbody>");
	    foundVect = new int[ listAlgo.size() ];
	    symVect = new double[ listAlgo.size() ];
	    effVect = new double[ listAlgo.size() ];
	    precOrVect = new double[ listAlgo.size() ];
	    recOrVect = new double[ listAlgo.size() ];
	    for( int k = listAlgo.size()-1; k >= 0; k-- ) {
		foundVect[k] = 0;
		symVect[k] = 0.;
		effVect[k] = 0.;
		precOrVect[k] = 0.;
		recOrVect[k] = 0.;
	    }
	    // </tr>
	    // For each directory <tr>
	    boolean colored = false;
	    for ( Vector test : result ) {
		int nexpected = -1;
		if ( colored == true && color != null ){
		    colored = false;
		    writer.println("<tr bgcolor=\""+color+"\">");
		} else {
		    colored = true;
		    writer.println("<tr>");
		};
		// Print the directory <td>bla</td>
		writer.println("<td>"+(String)test.get(0)+"</td>");
		// For each record print the values <td>bla</td>
		Enumeration f = test.elements();
		f.nextElement();
		for( int k = 0 ; f.hasMoreElements() ; k++) {
		    ExtPREvaluator eval = (ExtPREvaluator)f.nextElement();
		    if ( eval != null ){
			// iterative H-means computation
			if ( nexpected == -1 ){
			    nexpected = eval.getExpected();
			    expected += nexpected;
			}
			// If foundVect is -1 then results are invalid
			if ( foundVect[k] != -1 ) foundVect[k] += eval.getFound();
			for ( int i = 0 ; i < fsize; i++){
			    writer.print("<td>");
			    if ( format.charAt(i) == 's' ) {
				formatter.format("%1.2f", eval.getSymPrecision());
				System.out.print("</td><td>");
				formatter.format("%1.2f", eval.getSymRecall());
				symVect[k] += eval.getSymSimilarity();
			    } else if ( format.charAt(i) == 'e' ) {
				formatter.format("%1.2f", eval.getEffPrecision());
				System.out.print("</td><td>");
				formatter.format("%1.2f", eval.getEffRecall());
				effVect[k] += eval.getEffSimilarity();
			    } else if ( format.charAt(i) == 'p' ) {
				formatter.format("%1.2f", eval.getPrecisionOrientedPrecision());
				System.out.print("</td><td>");
				formatter.format("%1.2f", eval.getPrecisionOrientedRecall());
				precOrVect[k] += eval.getPrecisionOrientedSimilarity();
			    } else if ( format.charAt(i) == 'r' ) {
				formatter.format("%1.2f", eval.getRecallOrientedPrecision());
				System.out.print("</td><td>");
				formatter.format("%1.2f", eval.getRecallOrientedRecall());
				recOrVect[k] += eval.getRecallOrientedSimilarity();
			    }
			    writer.println("</td>");
			}
		    } else {
			writer.println("<td>n/a</td><td>n/a</td>");
		    }
		}
		writer.println("</tr>");
	    }
	    writer.print("<tr bgcolor=\"yellow\"><td>H-mean</td>");
	    int k = 0;
	    for ( String m : listAlgo ) {
		if ( foundVect[k] != -1 ){
		    for ( int i = 0 ; i < fsize; i++){
			writer.print("<td>");
			if ( format.charAt(i) == 's' ) {
			    formatter.format("%1.2f", symVect[k]/foundVect[k]);
			    System.out.print("</td><td>");
			    formatter.format("%1.2f", symVect[k]/expected);
			} else if ( format.charAt(i) == 'e' ) {
			    formatter.format("%1.2f", effVect[k]/foundVect[k]);
			    System.out.print("</td><td>");
			    formatter.format("%1.2f", effVect[k]/expected);
			} else if ( format.charAt(i) == 'p' ) {
			    formatter.format("%1.2f", precOrVect[k]/foundVect[k]);
			    System.out.print("</td><td>");
			    formatter.format("%1.2f", precOrVect[k]/expected);
			} else if ( format.charAt(i) == 'r' ) {
			    formatter.format("%1.2f", recOrVect[k]/foundVect[k]);
			    System.out.print("</td><td>");
			    formatter.format("%1.2f", recOrVect[k]/expected);
			}
			writer.println("</td>");
		    }
		} else {
		    writer.println("<td colspan='2'><center>Error</center></td>");
		}
		//};
		k++;
	    }
	    writer.println("</tr>");
	    writer.println("</tbody></table>");
	    writer.println("<p><small>n/a: result alignment not provided or not readable<br />");
	    writer.println("NaN: division per zero, likely due to empty alignent.</small></p>");
	    writer.println("</body></html>");
	} catch (Exception ex) {
	    ex.printStackTrace();
	} finally {
	    writer.flush();
	    writer.close();
	}
    }

    public void usage() {
	System.out.println("usage: ExtGroupEval [options]");
	System.out.println("options are:");
	System.out.println("\t--format=sepr -f sepr\tSpecifies the extended measures used (symetric/effort-based/precision-oriented/recall-oriented)");
	System.out.println("\t--reference=filename -r filename\tSpecifies the name of the reference alignment file (default: refalign.rdf)");
	System.out.println("\t--output=filename -o filename\tSpecifies a file to which the output will go");
	// Apparently not implemented
	//System.out.println("\t--dominant=algo -s algo\tSpecifies if dominant columns are algorithms or measure");
	System.out.println("\t--type=html|xml|tex|ascii -t html|xml|tex|ascii\tSpecifies the output format");
	System.out.println("\t--list=algo1,...,algon -l algo1,...,algon\tSequence of the filenames to consider");
	System.out.println("\t--color=color -c color\tSpecifies if the output must color even lines of the output");
	System.out.println("\t--debug[=n] -d [n]\t\tReport debug info at level n");
	System.out.println("\t--help -h\t\t\tPrint this message");
	System.err.print("\n"+ExtGroupEval.class.getPackage().getImplementationTitle()+" "+ExtGroupEval.class.getPackage().getImplementationVersion());
	System.err.println(" ($Id: ExtGroupEval.java 1701 2012-03-10 15:54:01Z euzenat $)\n");
    }
}
