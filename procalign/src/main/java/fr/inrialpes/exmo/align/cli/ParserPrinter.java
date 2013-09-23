/*
 * $Id: ParserPrinter.java 1827 2013-03-07 22:44:05Z euzenat $
 *
 * Copyright (C) INRIA, 2003-2004, 2007-2008, 2011-2013
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

/* 
*/
package fr.inrialpes.exmo.align.cli;

//Imported JAVA classes
import java.lang.Integer;
import java.lang.Double;
import java.util.Properties;

import java.io.File;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

/** A really simple utility that loads and alignment and prints it.
    A basic class for ontology alignment processing.
    Command synopsis is as follows:
    
    <pre>
    java fr.inrialpes.exmo.align.util.ParserPrinter [options] input [output]
    </pre>

    where the options are:
    <pre>
	--renderer=className -r className  Use the given class for output.
	--parser=className -p className  Use the given class for input.
        --inverse -i              Inverse first and second ontology
	--threshold=threshold -t threshold      Trim the alugnment with regard to threshold
	--cutmethod=hard|perc|prop|best|span -T hard|perc|prop|best|span      Method to use for triming
        --debug[=n] -d [n]              Report debug info at level n,
        --output=filename -o filename Output the alignment in filename
        --help -h                       Print this message
    </pre>

    The <CODE>input</CODE> is a filename. If output is
    requested (<CODE>-o</CODE> flags), then output will be written to
    <CODE>output</CODE> if present, stdout by default.

<pre>
$Id: ParserPrinter.java 1827 2013-03-07 22:44:05Z euzenat $
</pre>

    */

public class ParserPrinter {

    public static void main(String[] args) {
	try { new ParserPrinter().run( args ); }
	catch (Exception ex) { ex.printStackTrace(); };
    }

    public void run(String[] args) throws Exception {
	Alignment result = null;
	String initName = null;
	String filename = null;
	String dirName = null;
	PrintWriter writer = null;
	AlignmentVisitor renderer = null;
	LongOpt[] longopts = new LongOpt[11];
	int debug = 0;
	String rendererClass = null;
	String parserClass = null;
	boolean inverse = false;	
	boolean embedded = false;	
	double threshold = 0;
	String cutMethod = "hard";
	Properties params = new Properties();

	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	longopts[2] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	longopts[3] = new LongOpt("renderer", LongOpt.REQUIRED_ARGUMENT, null, 'r');
	longopts[4] = new LongOpt("parser", LongOpt.REQUIRED_ARGUMENT, null, 'p');
	longopts[5] = new LongOpt("inverse", LongOpt.NO_ARGUMENT, null, 'i');
	longopts[6] = new LongOpt("threshold", LongOpt.REQUIRED_ARGUMENT, null, 't');
	longopts[7] = new LongOpt("cutmethod", LongOpt.REQUIRED_ARGUMENT, null, 'T');
	longopts[8] = new LongOpt("embedded", LongOpt.NO_ARGUMENT, null, 'e');
	longopts[9] = new LongOpt("dirName", LongOpt.REQUIRED_ARGUMENT, null, 'c');
	// Is there a way for that in LongOpt ???
	longopts[10] = new LongOpt("D", LongOpt.REQUIRED_ARGUMENT, null, 'D');
	
	Getopt g = new Getopt("", args, "ehio:t:T:d::r:p:c:D:", longopts);
	int c;
	String arg;

	while ((c = g.getopt()) != -1) {
	    switch(c) {
	    case 'h':
		usage();
		return;
	    case 'i':
		inverse = true;
		break;
	    case 'e':
		embedded = true;
		break;
	    case 'o':
		/* Write warnings to stdout rather than stderr */
		filename = g.getOptarg();
		break;
	    case 'c':
		dirName = g.getOptarg();
		break;
	    case 'r':
		/* Use the given class for rendernig */
		rendererClass = g.getOptarg();
		break;
	    case 'p':
		/* Use the given class for rendernig */
		parserClass = g.getOptarg();
		break;
	    case 't' :
		/* Threshold */
		threshold = Double.parseDouble(g.getOptarg());
		break;
	    case 'T' :
		/* Cut method */
		cutMethod = g.getOptarg();
		break;
	    case 'd':
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
	
	int i = g.getOptind();
	
	if (args.length > i ) {
	    initName = args[i];
	} else {
	    System.out.println("Require the alignement filename");
	    usage();
	    return;
	}

	if ( debug > 1 ) System.err.println(" Filename"+initName);

	try {
	    // Create parser
	    AlignmentParser aparser = null;
	    if ( parserClass == null ) aparser = new AlignmentParser( debug );
	    else {
		try {
		    Object[] mparams = { (Object)debug };
		    java.lang.reflect.Constructor[] parserConstructors =
			Class.forName(parserClass).getConstructors();
		    aparser = (AlignmentParser) parserConstructors[0].newInstance(mparams);
		} catch (Exception ex) {
		    System.err.println("Cannot create parser " + 
				       parserClass + "\n" + ex.getMessage() );
		    usage();
		    return;
		}
	    }

	    aparser.setEmbedded( embedded );
	    result = aparser.parse( initName );
	    if ( debug > 0 ) System.err.println(" Alignment structure parsed");
	    // Set output file
	    OutputStream stream;
	    if (filename == null) {
		//writer = (PrintStream) System.out;
		stream = System.out;
	    }
	    else {
		//writer = new PrintStream(new FileOutputStream(filename));
		stream = new FileOutputStream(filename);
	    }
	    if ( dirName != null ) {
	    	 File f = new File(dirName);
		 f.mkdir();
		 params.setProperty( "dir", dirName );
		 params.setProperty( "split", "true" );
	    }
	    writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);

	    if ( inverse ) result = result.inverse();
	    
	    // Thresholding
	    if (threshold != 0) result.cut( cutMethod, threshold );

	    // Create renderer
	    if ( rendererClass == null ) renderer = new RDFRendererVisitor( writer );
	    else {
		try {
		    Object[] mparams = {(Object) writer };
		    java.lang.reflect.Constructor[] rendererConstructors =
			Class.forName(rendererClass).getConstructors();
		    renderer =
			(AlignmentVisitor) rendererConstructors[0].newInstance(mparams);
		} catch (Exception ex) {
		    System.err.println("Cannot create renderer " + 
				       rendererClass + "\n" + ex.getMessage() );
		    usage();
		    return;
		}
	    }

	    renderer.init( params );

	    // Render the alignment
	    try {
		result.render( renderer );
	    } catch ( AlignmentException aex ) {
		throw aex;
	    } finally {
		writer.flush();
		writer.close();
	    }	    
	    
	} catch (Exception ex) {
	    ex.printStackTrace();
	}
    }

    public void usage() {
	System.out.println("usage: ParserPrinter [options] URI");
	System.out.println("options are:");
	//System.out.println("\t--alignment=filename -a filename Start from an XML alignment file");
	System.out.println("\t--debug[=n] -d [n]\t\tReport debug info at level ,");
	System.out.println("\t--renderer=className -r\t\tUse the given class for output.");
	System.out.println("\t--parser=className -p\t\tUse the given class for input.");
	System.out.println("\t--embedded -e\t\tRead the alignment as embedded in XML file");
	System.out.println("\t--inverse -i\t\tInverse first and second ontology");
	System.out.println("\t--threshold=threshold -t threshold\t\tTrim the alugnment with regard to threshold");
	System.out.println("\t--cutmethod=hard|perc|prop|best|span -T hard|perc|prop|best|span\t\tMethod to use for triming");
	System.out.println("\t--output=filename -o filename\tOutput the alignment in filename");
	System.out.println("\t--outputDir=dirName -c dirName\tSplit the output in a directory (SPARQL)");
	System.out.println("\t--help -h\t\t\tPrint this message");
	System.err.println("\t-Dparam=value\t\t\tSet parameter");
	System.err.print("\n"+ParserPrinter.class.getPackage().getImplementationTitle()+" "+ParserPrinter.class.getPackage().getImplementationVersion());
	System.err.println(" ($Id: ParserPrinter.java 1827 2013-03-07 22:44:05Z euzenat $)\n");

    }
}
