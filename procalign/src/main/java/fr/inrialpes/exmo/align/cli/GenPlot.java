/*
 * $Id: GenPlot.java 1701 2012-03-10 15:54:01Z euzenat $
 *
 * Copyright (C) 2003-2012, INRIA
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

/*
 * This program evaluates the results of several ontology aligners and plot
 * these results
*/
package fr.inrialpes.exmo.align.cli;

import org.semanticweb.owl.align.Alignment;

import fr.inrialpes.exmo.align.impl.eval.GraphEvaluator;
import fr.inrialpes.exmo.align.impl.eval.Pair;

import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.lang.Integer;
import java.util.Properties;
import java.util.Vector;
import java.lang.reflect.Constructor;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

import fr.inrialpes.exmo.align.parser.AlignmentParser;

/**
 * A basic class for ploting the results of an evaluation.
 *
 * These graphs are however computed on averaging the precision recall/graphs
 * on test directories instead of recording the actual precision recall graphs
 * which would amount at recoding all the valid and invalid alignment cells and
 * their level.
 *  
 *  <pre>
 *  java -cp procalign.jar fr.inrialpes.exmo.align.util.GenPlot [options]
 *  </pre>
 *
 *  where the options are:
 *  <pre>
 *  -o filename --output=filename
 *  -d debug --debug=level
 *  -l list of compared algorithms
 *  -t output --type=output: xml/tex/html/ascii
 *  -e classname --evaluator=classname
 *  -g classname --grapher=classname
 * </pre>
 *
 * The input is taken in the current directory in a set of subdirectories (one per
 * test) each directory contains a the alignment files (one per algorithm) for that test and the
 * reference alignment file.
 *
 * If output is
 * requested (<CODE>-o</CODE> flags), then output will be written to
 *  <CODE>output</CODE> if present, stdout by default. In case of the Latex output, there are numerous files generated (regardless the <CODE>-o</CODE> flag).
 *
 * <pre>
 * $Id: GenPlot.java 1701 2012-03-10 15:54:01Z euzenat $
 * </pre>
 *
 * @author Jérôme Euzenat
 */

public class GenPlot {

    int STEP = 10;
    Properties params = new Properties();
    Vector<String> listAlgo;
    Vector<GraphEvaluator> listEvaluators;
    String fileNames = "";
    String outFile = null;
    Constructor evalConstructor = null;
    Constructor graphConstructor = null;
    String xlabel;
    String ylabel;
    String type = "tsv";
    int debug = 0;
    int size = 0; // the set of algo to compare
    PrintWriter output = null;

    public static void main(String[] args) {
	try { new GenPlot().run( args ); }
	catch (Exception ex) { ex.printStackTrace(); };
    }

    public void run(String[] args) throws Exception {
	LongOpt[] longopts = new LongOpt[10];

 	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	longopts[3] = new LongOpt("type", LongOpt.REQUIRED_ARGUMENT, null, 't');
	longopts[4] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	longopts[5] = new LongOpt("evaluator", LongOpt.REQUIRED_ARGUMENT, null, 'e');
	longopts[6] = new LongOpt("grapher", LongOpt.REQUIRED_ARGUMENT, null, 'g');
	longopts[7] = new LongOpt("list", LongOpt.REQUIRED_ARGUMENT, null, 'l');
	longopts[8] = new LongOpt("step", LongOpt.REQUIRED_ARGUMENT, null, 's');
	longopts[9] = new LongOpt("D", LongOpt.REQUIRED_ARGUMENT, null, 'D');

	Getopt g = new Getopt("", args, "ho:d::l:D:e:g:s:t:", longopts);
	int step = 10;
	int c;
	String arg;
	String evalCN = "fr.inrialpes.exmo.align.impl.eval.PRecEvaluator";
	String graphCN = "fr.inrialpes.exmo.align.impl.eval.PRGraphEvaluator";

	while ((c = g.getopt()) != -1) {
	    switch (c) {
	    case 'h' :
		usage();
		return;
	    case 'o' :
		/* Write output here */
		outFile = g.getOptarg();
		break;
	    case 'e' :
		/* Name of the evaluator to use */
		evalCN = g.getOptarg();
		break;
	    case 'g' :
		/* Name of the graph display to use */
		graphCN = g.getOptarg();
		break;
	    case 't' :
		/* Type of output (tex/tsv(/html/xml/ascii)) */
		type = g.getOptarg();
		break;
	    case 'l' :
		/* List of filename */
		fileNames = g.getOptarg();
		break;
		//case 's' :
		/* Step */
		//fileNames = g.getOptarg();
		//break;
	    case 'd' :
		/* Debug level  */
		arg = g.getOptarg();
		if ( arg != null ) debug = Integer.parseInt(arg.trim());
		else debug = 4;
		break;
	    case 'D' :
		/* Parameter definition */
		arg = g.getOptarg();
		int index = arg.indexOf('=');
		if ( index != -1 ) {
		    params.setProperty( arg.substring( 0, index), 
					 arg.substring(index+1));
		} else {
		    System.err.println("Bad parameter syntax: "+g);
		    usage();
		    System.exit(0);
		}
		break;
	    }
	}

	Class<?> graphClass = Class.forName(graphCN);
	Class[] cparams = {};
	graphConstructor = graphClass.getConstructor( cparams );

	//Class<?> evalClass = Class.forName(evalCN);
	//evalConstructor = evalClass.getConstructor( cparams );

	listAlgo = new Vector<String>();
	for ( String s : fileNames.split(",") ) {
	    size++;
	    listAlgo.add( s );	    
	}

	if (debug > 0) params.setProperty( "debug", Integer.toString( debug-1 ) );

	// Collect correspondences from alignments in all directories
	// . -> Vector<EvalCell>
	listEvaluators = iterateDirectories();

	// Find the largest value
	int max = 0;
	for( GraphEvaluator e : listEvaluators ) {
	    int n = e.nbCells();
	    if ( n > max ) max = n;
	}
	params.setProperty( "scale", Integer.toString( max ) );

	xlabel = listEvaluators.get(0).xlabel();
	ylabel = listEvaluators.get(0).ylabel();

	// Vector<EvalCell> -> Vector<Pair>
	// Convert the set of alignments into the list of required point pairs
	// We must convert the 
	Vector<Vector<Pair>> toplot = new Vector<Vector<Pair>>();
	for( int i = 0; i < size ; i++ ) {
	    // Convert it with the adequate GraphPlotter
	    // Scale the point pairs to the current display (local)
	    toplot.add( i, listEvaluators.get(i).eval( params ) );
	    //scaleResults( STEP, 
	}

	// Set output file
	OutputStream stream;
	if (outFile == null) {
	    stream = System.out;
	} else {
	    stream = new FileOutputStream(outFile);
	}
	output = new PrintWriter (
		   new BufferedWriter(
		     new OutputStreamWriter( stream, "UTF-8" )), true);

	//System.err.println ( toplot.get(0));
	// Display the required type of output
	// Vector<Pair> -> .
	if ( type.equals("tsv") ){
	    printTSV( toplot );
	} else if ( type.equals("html") ) {
	    printHTMLGGraph( toplot );
	} else if ( type.equals("tex") ) {
	    printPGFTex( toplot );
	} else System.err.println("Flag -t "+type+" : not implemented yet");
    }

    /**
     * Iterate on each subdirectory
     * Returns a vector[ each algo ] of vector [ each point ]
     * The points are computed by aggregating the values
     *  (and in the end computing the average)
     */
    public Vector<GraphEvaluator> iterateDirectories() {
	Vector<GraphEvaluator> evaluators = new Vector<GraphEvaluator>( size );
	Object[] mparams = {};
	try {
	    for( int i = 0; i < size; i++ ) {
		GraphEvaluator ev = (GraphEvaluator)graphConstructor.newInstance(mparams);
		ev.setStep( STEP );
		evaluators.add( i, ev );
	    }
	} catch (Exception ex) { //InstantiationException, IllegalAccessException
	    ex.printStackTrace();
	    System.exit(-1);
	}

	File [] subdir = null;
	try {
	    subdir = (new File(System.getProperty("user.dir"))).listFiles();
	} catch (Exception e) {
	    System.err.println("Cannot stat dir "+ e.getMessage());
	    usage();
	}

	// Evaluate the results in each directory
	for ( int k = subdir.length-1 ; k >= 0; k-- ) {
	    if( subdir[k].isDirectory() ) {
		// eval the alignments in a subdirectory
		iterateAlignments( subdir[k], evaluators );//, result );
	    }
	}
	return evaluators;
    }

    public void iterateAlignments ( File dir, Vector<GraphEvaluator> evaluators ) {
	if ( debug > 0 ) System.err.println("Directory : "+dir);
	String prefix = dir.toURI().toString()+"/";

	int nextdebug;
	if ( debug < 2 ) nextdebug = 0;
	else nextdebug = debug - 2;
	AlignmentParser aparser = new AlignmentParser( nextdebug );
	Alignment refalign = null;

	try { // Load the reference alignment...
	    refalign = aparser.parse( prefix+"refalign.rdf" );
	    if ( debug > 1 ) System.err.println(" Reference alignment parsed");
	} catch ( Exception aex ) {
	    if ( debug > 1 ) {
		aex.printStackTrace();
	    } else {
		System.err.println("GenPlot cannot parse refalign : "+aex);
	    };
	    return;
	}

	// for all alignments there,
	for( int i = 0; i < size; i++ ) {
	    String algo = listAlgo.get(i);
	    Alignment al = null;
	    if ( debug > 0 ) System.err.println("  Considering result "+algo+" ("+i+")");
	    try {
		aparser.initAlignment( null );
		al = aparser.parse( prefix+algo+".rdf" );
		if ( debug > 1 ) System.err.println(" Alignment "+algo+" parsed");
	    } catch (Exception ex) { 
		if ( debug > 1 ) {
		    ex.printStackTrace();
		} else {
		    System.err.println("GenPlot: "+ex);
		};
	    }
	    // even if empty, declare refalign
	    evaluators.get(i).ingest( al, refalign );
	}
	// Unload the ontologies.
	try {
	    OntologyFactory.clear();
	} catch ( OntowrapException owex ) { // only report
	    owex.printStackTrace();
	}
    }
    
    // should be OK for changing granularity
    // This is not really scalling...
    // This is unused
    public Vector<Pair> scaleResults( int STEP, Vector<Pair> input ) {
	int j = 0;
	Vector<Pair> output = new Vector<Pair>(); // Set the size!
	Pair last = null;
	double next = 0.;//is it a double??
	for ( Pair npair : input ) {
	    if ( npair.getX() == next ) {
		output.add( npair );
		next += STEP;
	    } else if ( npair.getX() >= next ) { // interpolate
		double val;
		if ( last.getY() >= npair.getY() ) {
		    val = npair.getY() + ( ( last.getY() - npair.getY() ) / ( last.getX()-npair.getX() ) );
		} else {
		    val = last.getY() + ( (npair.getY() - last.getY() ) / ( last.getX()-npair.getX() ) );
		}
		//System.err.println( "Scaling: "+next+" / "+val );
		output.add( new Pair( next, val )  );
		next += STEP;
	    }
	    last = npair;
	}
	output.add( last );
	return( output );
    }

    /**
     * This does average plus plot
     *
     */
    public void printPGFTex( Vector<Vector<Pair>> result ){
	int i = 0;
	String marktable[] = { "+", "*", "x", "-", "|", "o", "asterisk", "star", "oplus", "oplus*", "otimes", "otimes*", "square", "square*", "triangle", "triangle*", "diamond", "diamond*", "pentagon", "pentagon*"};
	String colortable[] = { "black", "red", "green!50!black", "blue", "cyan", "magenta" }	;
	output.println("\\documentclass[11pt]{book}");
	output.println();
	output.println("\\usepackage{pgf}");
	output.println("\\usepackage{tikz}");
	output.println("\\usetikzlibrary{plotmarks}");
	output.println();
	output.println("\\begin{document}");
	output.println("\\date{today}");
	output.println("");
	output.println("\n%% Plot generated by GenPlot of alignapi");
	output.println("\\begin{tikzpicture}[cap=round]");
	output.println("% Draw grid");
	output.println("\\draw[step="+(STEP/10)+"cm,very thin,color=gray] (-0.2,-0.2) grid ("+STEP+","+STEP+");");
	output.println("\\draw[->] (-0.2,0) -- (10.2,0);");
	output.println("\\draw (5,-0.3) node {$"+xlabel+"$}; ");
	output.println("\\draw (0,-0.3) node {0.}; ");
	output.println("\\draw (10,-0.3) node {1.}; ");
	output.println("\\draw[->] (0,-0.2) -- (0,10.2);");
	output.println("\\draw (-0.3,0) node {0.}; ");
	output.println("\\draw (-0.3,5) node[rotate=90] {$"+ylabel+"$}; ");
	output.println("\\draw (-0.3,10) node {1.}; ");
	output.println("% Plots");
	i = 0;
	for ( String m : listAlgo ) {
	    output.print("\\draw["+colortable[i%6] );
	    if ( !listEvaluators.get(i).isValid() ) output.print(",dotted");
	    output.println("] plot[mark="+marktable[i%19]+"] file {"+m+".table};");
	    //,smooth
	    i++;
	}
	// And a legend
	output.println("% Legend");
	i = 0;
	for ( String m : listAlgo ) {
	    output.print("\\draw["+colortable[i%6] );
	    if ( !listEvaluators.get(i).isValid() ) output.print(",dotted");
	    output.println("] plot[mark="+marktable[i%19]+"] coordinates {("+((i%3)*3+1)+","+(-(i/3)*.8-1)+") ("+((i%3)*3+3)+","+(-(i/3)*.8-1)+")};");
	    //,smooth
	    output.println("\\draw["+colortable[i%6]+"] ("+((i%3)*3+2)+","+(-(i/3)*.8-.8)+") node {"+m+"};");
	    output.printf("\\draw["+colortable[i%6]+"] ("+((i%3)*3+2)+","+(-(i/3)*.8-1.2)+") node {%1.2f};\n", listEvaluators.get(i).getGlobalResult() );
	    i++;
	}
	output.println("\\end{tikzpicture}");
	output.println();
	output.println("\\end{document}");

	i = 0;
	for( Vector<Pair> table : result ) {
	    String algo = listAlgo.get(i);
	    // Open one file
	    PrintWriter writer = null;
	    try {
		writer = new PrintWriter (
				    new BufferedWriter(
                                       new OutputStreamWriter(
                                            new FileOutputStream(algo+".table"), "UTF-8" )), true);
		// Print header
		writer.println("#Curve 0, "+(STEP+1)+" points");
		writer.println("#x y type");
		writer.println("%% Plot generated by GenPlot of alignapi");
		writer.println("%% Include in PGF tex by:\n");
		writer.println("%% \\begin{tikzpicture}[cap=round]");
		writer.println("%% \\draw[step="+(STEP/10)+"cm,very thin,color=gray] (-0.2,-0.2) grid ("+STEP+","+STEP+");");
		writer.println("%% \\draw[->] (-0.2,0) -- (10.2,0) node[right] {$"+xlabel+"$}; ");
		writer.println("%% \\draw[->] (0,-0.2) -- (0,10.2) node[above] {$"+ylabel+"$}; ");
		writer.println("%% \\draw plot[mark=+,smooth] file {"+algo+".table};");
		writer.println("%% \\end{tikzpicture}");
		writer.println();
		for( Pair p : table ) {
		    if ( debug > 1 ) System.err.println( " >> "+p.getX()+" - "+p.getY() );
		    writer.println( p.getX()*10+" "+ p.getY()*10 );
		}
	    } catch (Exception ex) {
		ex.printStackTrace(); 
	    } finally {
		if ( writer != null ) writer.close();
	    }
	    // UnsupportedEncodingException + FileNotFoundException
	    i++;
	}
    }

    /**
     * This does average plus generate the call for Google Chart API
     *
     */
    public void printHTMLGGraph( Vector<Vector<Pair>> result ){
	output.print("<img src=\"http://chart.apis.google.com/chart?");
	output.print("chs=600x500&cht=lxy&chg=10,10&chof=png");
	output.print("&chxt=x,x,y,y&chxr=0,0.0,1.0,0.1|2,0.0,1.0,0.1&chxl=1:|"+xlabel+"|3:|"+ylabel+"&chma=b&chxp=1,50|3,50&chxs=0N*sz1*|2N*sz1*");
	output.print("&chd=t:"); // data
	boolean firstalg = true;
	for( Vector<Pair> table : result ) {
	    if ( !firstalg ) output.print("|");
	    firstalg = false;
	    boolean firstpoint = true;
	    String Yval = "|";
	    for( Pair p : table ) {
		if ( !firstpoint ) {
		    output.print(",");
		    Yval += ",";
		}
		firstpoint = false;
		Yval += String.format("%1.2f", p.getY()*10);
		if ( debug > 1 ) System.err.println( " >> "+p.getX()+" - "+p.getY() );
		output.printf( "%1.2f", p.getX()*10 );
	    }
	    output.print( Yval );
	}
	output.print("&chdl="); // labels
	int i = 0;
	//String marktable[] = { "+", "*", "x", "-", "|", "o", "asterisk", "star", "oplus", "oplus*", "otimes", "otimes*", "square", "square*", "triangle", "triangle*", "diamond", "diamond*", "pentagon", "pentagon*"};
	//String colortable[] = { "black", "red", "green!50!black", "blue", "cyan", "magenta" };
	String colortable[] = { "000000", "ffff00", "ff00ff", "00ffff", "ff0000", "00ff00", "0000ff", "888888", "8888ff", "88ff88", "ff8888", "8800ff", "88ff00", "008800", "ff8800", "0088ff", "000088","ff0088","00ff88", "888800", "880088", "008888", "880000", "008800", "000088", "88ffff", "ff88ff", "ffff88" };
	String style = "";
	String color = "";
	for ( String m : listAlgo ) {
	    if ( i > 0 ) {
		output.print( "|" );
		color += ",";
		style += "|";
	    }
	    output.print( m );
	    color += colortable[i%28];
	    if ( !listEvaluators.get(i).isValid() ) {
		style += "2,6,3";
	    } else {
		style += "2";
	    }
	    i++;
	}
	//output.print("&chdlp=b"); // legend position (but ugly)
	output.print("&chco="+color); // colors
	output.print("&chls="+style); // linestyle
	output.println("&chds=0,10\"/>");
    }

    // 2010: TSV output is not finished
    // It is supposed to provide
    // List of algo
    // List of STEP + points
    public void printTSV( Vector<Vector<Pair>> points ) {
	// Print first line
	for ( String m : listAlgo ) {
	    output.print("\t"+m );
	}
	// Print others
	for ( int i= 0; i < 100 ; i += STEP ) {
	    for( int j = 0; j < size; j++ ){
		Pair precrec = points.get(j).get(i);
		output.println( precrec.getX()+" "+precrec.getY() );
	    }
	}
	output.println();
    }

    public void usage() {
	System.out.println("usage: GenPlot [options]");
	System.out.println("options are:");
	System.out.println("\t--type=tsv|tex|html(|xml) -t tsv|tex|html(|xml)\tSpecifies the output format");
	System.out.println("\t--graph=class -g class\tSpecifies the class of Evaluator to be used");
	System.out.println("\t--evaluator=class -e class\tSpecifies the class of GraphEvaluator (plotter) to be used");
	System.out.println("\t--list=algo1,...,algon -l algo1,...,algon\tSequence of the filenames to consider");
	System.out.println("\t--debug[=n] -d [n]\t\tReport debug info at level n");
	System.out.println("\t--help -h\t\t\tPrint this message");
    }
}
