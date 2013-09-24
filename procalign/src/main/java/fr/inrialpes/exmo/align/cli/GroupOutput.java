/*
 * $Id: GroupOutput.java 1790 2012-11-05 22:48:48Z euzenat $
 *
 * Copyright (C) 2003 The University of Manchester
 * Copyright (C) 2003 The University of Karlsruhe
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

/* This program evaluates the results of several ontology aligners
   and generates a LaTeX diagram for each of these
*/
package fr.inrialpes.exmo.align.cli;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Evaluator;

import fr.inrialpes.exmo.align.impl.eval.PRecEvaluator;

import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.lang.Integer;
import java.util.Vector;
import java.util.Properties;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

import fr.inrialpes.exmo.align.parser.AlignmentParser;

/** A basic class for synthesizing the alignment results of an algorithm with
 * regard to the ontology characteristics as a colored module.
 *
 * These modules are however computed on averaging the precision recall/graphs
 * on test directories instead of recording the actual precision recall graphs
 * which would amount at recoding all the valid and invalid alignment cells and
 * their level.
 *  
 *  <pre>
 *  java -cp procalign.jar fr.inrialpes.exmo.align.util.GroupOutput [options]
 *  </pre>
 *
 *  where the options are:
 *  <pre>
 *  -o filename --output=filename
 *  -c --color
 *  -v --value
 *  -e --labels
 *  -m --measure
 *  -d debug --debug=level
 *  -l list of compared algorithms
 *  -t output --type=output: xml/tex/html/ascii
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
 *
 * <pre>
 * $Id: GroupOutput.java 1790 2012-11-05 22:48:48Z euzenat $
 * </pre>
 *
 * @author Jérôme Euzenat
 */

/*
Generating Spider/Radar view in TikZ is very easy.
The code below works.
But it is less informative than the above presentation.

\begin{tikzpicture}

% JE: experiment to draw radar/spider plots in TikZ
% n= number of dimensions (here 5)
% w=radius if the graph (here 2)
% p=number of isovalue lines (here 2)

% grid 
% (for i=1 to n do \draw[black!50] (0,0) -- (i*360/n:w);
\draw[black!50] (0,0) -- (0:2) node {instances};
\draw[black!50] (0,0) -- (72:2) node {properties};
\draw[black!50] (0,0) -- (144:2) node {label};
\draw[black!50] (0,0) -- (216:2) node {hierarchy};
\draw[black!50] (0,0) -- (288:2);
% (for i=1 to p do \draw[black!50] (0,0) -- (i*360/n:w/i);
\draw[black!50] (0:2) -- (72:2) -- (144:2) -- (216:2) -- (288:2) -- (360:2);
\draw[black!50] (0:1) -- (72:1) -- (144:1) -- (216:1) -- (288:1) -- (360:1);

% any curve can be displayed in the same way
\draw (0:1) -- (72:1.22) -- (144:1.56) -- (216:.78) -- (288:.6) -- (360:1);

\end{tikzpicture} 
*/

public class GroupOutput {

    static int SIZE = 16;// Nb of cells = 2^ = 16
    static int cellSpec[][] = { {101}, //liph=0
			    {201, 202, 203, 204, 205, 206, 207, 208, 209, 210}, //phi=1
			    {221},//lip=2
			    {224},//lph=3
			    {225,228,230,231},//hil=4
			    {232,237,238},//lp=5
			    {233,239,240},//li=6
			    {236},//lh=7
			    {249},//ph=8
			    {248,251,252},//ip=9
			    {250},//hi=10
			    {241,246,247},//l=11
			    {253,258,259},//p=12
			    {257},//h=13
			    {254,260,261},//i=14
			    {262,265,266} };//emptyset=15
    Properties params = null;
    Vector<String> listAlgo;
    String fileNames = "";
    String outFile = null;
    String type = "tex";
    String color = null;
    boolean labels = false;
    boolean value = false;
    int debug = 0;
    int measure = 0;
    PrintWriter output = null;

    public static void main(String[] args) {
	try { new GroupOutput().run( args ); }
	catch (Exception ex) { ex.printStackTrace(); };
    }

    public void run(String[] args) throws Exception {
	LongOpt[] longopts = new LongOpt[10];

 	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	longopts[2] = new LongOpt("color", LongOpt.OPTIONAL_ARGUMENT, null, 'c');
	longopts[3] = new LongOpt("type", LongOpt.REQUIRED_ARGUMENT, null, 't');
	longopts[4] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	longopts[6] = new LongOpt("list", LongOpt.REQUIRED_ARGUMENT, null, 'l');
	longopts[7] = new LongOpt("value", LongOpt.NO_ARGUMENT, null, 'v');
	longopts[8] = new LongOpt("labels", LongOpt.NO_ARGUMENT, null, 'e');
	longopts[9] = new LongOpt("measure", LongOpt.REQUIRED_ARGUMENT, null, 'm');

	Getopt g = new Getopt("", args, "hveo:c::d::l:t:m:", longopts);
	int c;
	String arg;

	while ((c = g.getopt()) != -1) {
	    switch (c) {
	    case 'h' :
		usage();
		return;
	    case 'v' :
		value = true;
		break;
	    case 'e' :
		labels = true;
		break;
	    case 'o' :
		/* Write output here */
		outFile = g.getOptarg();
		break;
	    case 'c' :
		/* Cell color */
		arg = g.getOptarg();
		if ( arg != null )  {
		    color = arg.trim();
		} else color = "blue";
		break;
	    case 't' :
		/* Type of output (tex/tsv(/html/xml/ascii)) */
		type = g.getOptarg();
		break;
	    case 'l' :
		/* List of filename */
		fileNames = g.getOptarg();
		break;
	    case 'm' :
		/* measure to be used */
		String s = g.getOptarg();
		if ( s.equals("p") ) measure = 1;
		else if ( s.equals("r") ) measure = 2;
		else if ( s.equals("o") ) measure = 3;
		break;
	    case 'd' :
		/* Debug level  */
		arg = g.getOptarg();
		if ( arg != null ) debug = Integer.parseInt(arg.trim());
		else debug = 4;
		break;
	    }
	}

	listAlgo = new Vector<String>();
	for ( String s : fileNames.split(",") ) {
	    listAlgo.add( s );	    
	}

	params = new Properties();
	if (debug > 0) params.setProperty( "debug", Integer.toString(debug-1) );

	params.setProperty( "step", Integer.toString(SIZE) );

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

	// Header
	if ( type.equals("tex") ) {
	    output.println("\\documentclass[11pt]{book}");
	    output.println();
	    output.println("\\usepackage{pgf}");
	    output.println("\\usepackage{tikz}");
	    output.println("\\usepackage{pgflibraryshapes}");
	    output.println();
	    output.println("\\begin{document}");
	    output.println("\\date{today}");
	    output.println("");
	}
	// Process
	iterateAlgorithm( );
	// Trailer
	if ( type.equals("tex") ) {
	    output.println("\\end{document}");
	    output.println();
	}
	
    }

    public void iterateAlgorithm(){
	// for all alignments there,
	for ( String algo : listAlgo ) {
	    if ( debug > 0 ) System.err.println("Algorithm: "+algo);
	    // type
	    if ( type.equals("tex") ) {
		printPGFTeX( algo, iterateCells( algo ) );
	    } else System.err.println("Flag -t "+type+" : not implemented yet");
	}
    }

    public double[] iterateCells( String algo ){
	double[] cells = new double[SIZE];
	for ( int i = 0; i < SIZE; i++ ){
	    cells[i] = iterateTests( algo, cellSpec[i] );
	}
	return cells;
    }

    public double iterateTests( String algo, int[] tests ){
	File dir = (new File(System.getProperty("user.dir")));
	double result = 0.0;
	int nbtests = 0;
	for ( int i=0; i<tests.length; i++ ){//size() or length
	    if ( debug > 1 ) System.err.println("    tests: "+tests[i]);
	    String prefix = dir.toURI().toString()+"/"+tests[i]+"/";
	    try {
		PRecEvaluator evaluator = (PRecEvaluator)eval( prefix+"refalign.rdf", prefix+algo+".rdf");
		result += getMeasure( evaluator );
		nbtests++; // Only the tests that succeed
	    } catch ( AlignmentException aex ) { // simple error message
		if ( aex.getCause() != null ) 
		    System.err.println( aex.getCause().getMessage() );
		else System.err.println( aex );
	    }
	}
	// Unload the ontologies.
	try {
	    OntologyFactory.clear();
	} catch ( OntowrapException owex ) { // only report
	    owex.printStackTrace();
	}
	return result/(double)nbtests;
    }

    public Evaluator eval( String alignName1, String alignName2 ) throws AlignmentException {
	Evaluator eval = null;
	int nextdebug;
	if ( debug < 3 ) nextdebug = 0;
	else nextdebug = debug - 3;
	// Load alignments
	Alignment align1=null, align2=null;
	try {
	    AlignmentParser aparser = new AlignmentParser( nextdebug );
	    align1 = aparser.parse( alignName1 );
	    if ( debug > 2 ) System.err.println(" Alignment structure1 parsed");
	    aparser.initAlignment( null );
	    align2 = aparser.parse( alignName2 );
	    if ( debug > 2 ) System.err.println(" Alignment structure2 parsed");
	} catch (Exception ex) {
	    throw new AlignmentException( "Cannot parse ", ex );
	}
	// Create evaluator object
	eval = new PRecEvaluator( align1, align2 );
	// Compare
	params.setProperty( "debug", Integer.toString( nextdebug ) );
	eval.eval( params ) ;
	return eval;
    }

    public void printPGFTeX( String algo, double[] cells ){
	output.println("\n%% Plot generated by GroupOutput of alignapi");
	output.println("\\begin{figure}[!h]");
	output.println("\\begin{center}");
	output.println("\\begin{tikzpicture}");

	output.println("\\begin{scope}[minimum size=1cm]");
	if ( labels ) {
	    output.println("\\draw (-.5,2.) node[anchor=east] {labels};");
	    output.println("\\draw (2.5,0.) node[anchor=west] {hierarchy};");
	    output.println("\\draw (-.5,0.) node[anchor=east] {properties};");
	    output.println("\\draw (2.5,2.0) node[anchor=west] {instances};");
	}

	output.println("\\draw (0,2.) node[diamond"+colorFormat(cells[11])+"] {}; % l");
	if (value) output.println("\\draw (0,2.) node {"+stringFormat(cells[11])+"}; % l");
	output.println("\\draw (0,1.) node[diamond"+colorFormat(cells[5])+"] {}; % lp");
	if (value) output.println("\\draw (0,1.) node {"+stringFormat(cells[5])+"}; % lp");
	output.println("\\draw (0.5,1.5) node[diamond"+colorFormat(cells[2])+"] {}; % lip");
	if (value) output.println("\\draw (0.5,1.5) node {"+stringFormat(cells[2])+"}; % lip");
	output.println("\\draw (1.,2.) node[diamond"+colorFormat(cells[6])+"] {}; %li");
	if (value) output.println("\\draw (1.,2.) node {"+stringFormat(cells[6])+"}; %li");
	output.println("\\draw (-0.5,-0.5) node[diamond"+colorFormat(cells[9])+"] {}; % ip");
	if (value) output.println("\\draw (-0.5,-0.5) node {"+stringFormat(cells[9])+"}; % ip");
	output.println("\\draw (0,0) node[diamond"+colorFormat(cells[12])+"] {}; %p");
	if (value) output.println("\\draw (0,0) node {"+stringFormat(cells[12])+"}; %p");
	output.println("\\draw (.5,.5) node[diamond"+colorFormat(cells[3])+"] {}; % lph");
	if (value) output.println("\\draw (.5,.5) node {"+stringFormat(cells[3])+"}; % lph");
	output.println("\\draw (1.,1.) node[diamond"+colorFormat(cells[0])+"] {}; % liph");
	if (value) output.println("\\draw (1.,1.) node {"+stringFormat(cells[0])+"}; % liph");
	output.println("\\draw (1.5,1.5) node[diamond"+colorFormat(cells[4])+"] {}; %hil");
	if (value) output.println("\\draw (1.5,1.5) node {"+stringFormat(cells[4])+"}; %hil");
	output.println("\\draw (2.,2.) node[diamond"+colorFormat(cells[14])+"] {}; %i");
	if (value) output.println("\\draw (2.,2.) node {"+stringFormat(cells[14])+"}; %i");
	output.println("\\draw (2.,1.) node[diamond"+colorFormat(cells[10])+"] {}; % hi");
	if (value) output.println("\\draw (2.,1.) node {"+stringFormat(cells[10])+"}; % hi");
	output.println("\\draw (1.5,0.5) node[diamond"+colorFormat(cells[1])+"] {}; % phi");
	if (value) output.println("\\draw (1.5,0.5) node {"+stringFormat(cells[1])+"}; % phi");
	output.println("\\draw (1.,0) node[diamond"+colorFormat(cells[8])+"] {}; % ph");
	if (value) output.println("\\draw (1.,0) node {"+stringFormat(cells[8])+"}; % ph");
	output.println("\\draw (2.,0) node[diamond"+colorFormat(cells[13])+"] {}; % h");
	if (value) output.println("\\draw (2.,0) node {"+stringFormat(cells[13])+"}; % h");
	output.println("\\draw (2.5,-0.5) node[diamond"+colorFormat(cells[7])+"] {}; % hl");
	if (value) output.println("\\draw (2.5,-0.5) node {"+stringFormat(cells[7])+"}; % hl");

	output.println("\\end{scope}");

	output.println("\\end{tikzpicture} ");
	output.println("\\caption{"+algo+" evaluation on F-measure (the darkest the best).}\\label{fig:diag"+algo+"}");
	output.println("\\end{center}");
	output.println("\\end{figure}");
    }

    public void printTSV( double[][] result ) {
    }

    public String stringFormat(double f){
	String result;
	// JE: Must add the test is the value is Not a number, print NaN.
	if ( f != f ) result = "N";
         else {
	     int tmp = (int)(f*100);
	     int dec = tmp%100;
	     if( (int)(f*1000)%10 >= 5 ) dec++;
	     if ( tmp >= 100 || dec >= 100 ) result = "1.0";
	     else {
		 result = ".";
		 if (dec < 10) result = result+"0";
		 result = result+dec;
	     }
	}
	return result;
    }

    public double getMeasure( PRecEvaluator evaluator ) {
	if ( measure == 1 ) return evaluator.getPrecision();
	if ( measure == 2 ) return evaluator.getRecall();
	if ( measure == 3 ) return evaluator.getOverall();
	return evaluator.getFmeasure();
    }

    public String colorFormat(double f){
	if ( color == null ) return "";
	else return ",fill="+color+"!"+(int)(f*100);
    }

    public void usage() {
	System.out.println("usage: GenPlot [options]");
	System.out.println("options are:");
	System.out.println("\t--values -v\tSpecifies if the values should be displayed");
	System.out.println("\t--label -e\tSpecifies if the labels should be displayed");
	System.out.println("\t--measure=[prf] -c [prf]\tThe measure to display [F-measure]");
	System.out.println("\t--color=color -c color\tSpecifies the color to use [blue]");
	System.out.println("\t--list=algo1,...,algon -l algo1,...,algon\tSequence of the filenames to consider");
	System.out.println("\t--debug[=n] -d [n]\t\tReport debug info at level n");
	System.out.println("\t--help -h\t\t\tPrint this message");
    }
}
