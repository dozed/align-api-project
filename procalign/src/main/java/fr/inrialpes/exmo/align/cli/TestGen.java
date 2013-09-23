/*
 * $Id: TestGen.java 1695 2012-03-07 16:07:46Z euzenat $
 *
 * Copyright (C) 2011-2012, INRIA
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

package fr.inrialpes.exmo.align.cli;

import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

import java.util.Properties;

import fr.inrialpes.exmo.align.gen.TestGenerator;
import fr.inrialpes.exmo.align.gen.TestSet;
import fr.inrialpes.exmo.align.gen.ParametersIds;

/** 
    An utility application for generating tests from command line.
    It can either generate a single test or a whole test suite from a single ontology.
    
    <pre>
    java -cp procalign.jar fr.inrialpes.exmo.align.gen.TestGen [options] filename
    </pre>

    where filename is the seed ontology,
    and the options are:
    <pre>
        --debug[=n] -d [n]          --> Report debug info at level n,
   </pre>

*/

public class TestGen {
    private Properties params = null;
    private String methodName = null;                                           //the name of the method
    private String fileName   = null;                                           //the name of the input file
    private String dir        = ".";                                           //
    private String url;                                                        //
    private int debug         = 0;

    public TestGen() {
	fileName = "onto.rdf";
    }

    public static void main(String[] args) {
        try { new TestGen().run( args ); }
        catch ( Exception ex ) { ex.printStackTrace(); };
    }

    public void run(String[] args) throws Exception {
	LongOpt[] longopts = new LongOpt[10];
	params = new Properties();
	
	longopts[0] = new LongOpt("testset", LongOpt.REQUIRED_ARGUMENT, null, 't');
	longopts[1] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[2] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	longopts[3] = new LongOpt("outdir", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	longopts[4] = new LongOpt("urlprefix", LongOpt.REQUIRED_ARGUMENT, null, 'u');
	longopts[5] = new LongOpt("ontoname", LongOpt.REQUIRED_ARGUMENT, null, 'n');
	longopts[6] = new LongOpt("alignname", LongOpt.REQUIRED_ARGUMENT, null, 'a');
	longopts[7] = new LongOpt("D", LongOpt.REQUIRED_ARGUMENT, null, 'D');
          
	Getopt g = new Getopt("", args, "d::o:u:m:n:a:D:t:h", longopts);
	int c;
	String arg;

	while ((c = g.getopt()) != -1) {
	    switch (c) {
	    case 'h':
		usage();
		return;
	    case 't':
		methodName = g.getOptarg();
		break;
	    case 'n':
		params.setProperty( "ontoname", g.getOptarg() );
		break;
	    case 'a':
		params.setProperty( "alignname", g.getOptarg() );
		break;
	    case 'o' : /* Use output directory */
		dir = g.getOptarg();
		params.setProperty( "outdir", dir );
		break;
	    case 'u' : /* URLPrefix */
		url = g.getOptarg();
		params.setProperty( "urlprefix", url );
		break;
	    case 'd' : /* Debug level  */
		arg = g.getOptarg();
		if ( arg != null ) params.setProperty( "debug", arg.trim() );
		else 		  params.setProperty( "debug", "4" );
		break;
	    case 'D' : /* Parameter definition: could be used for all parameters */
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

	// We need an ontology
	int i = g.getOptind();
	
	if ( args.length > i ) {
	    fileName = args[i];
	    params.setProperty( "filename", fileName );
	} else {
	    System.out.println("Require the seed ontology filename");
	    usage();
	    return;
	}

	if ( debug > 0 ) System.err.println( " >>>> "+methodName+" from "+fileName );

	if ( methodName == null ) { // generate one test
	    TestGenerator tg = new TestGenerator();
	    tg.setDirPrefix( dir );
	    tg.setURLPrefix( url );
	    tg.modifyOntology( fileName, (Properties)null, (String)null, params );
	} else { // generate a test set
	    TestSet tset = null;
	    try {
		Object[] mparams = {};
		Class<?> testSetClass = Class.forName( methodName );
		Class[] cparams = {};
		java.lang.reflect.Constructor testSetConstructor = testSetClass.getConstructor(cparams);
		tset = (TestSet)testSetConstructor.newInstance(mparams);
	    } catch (Exception ex) {
		System.err.println("Cannot create TestSet "+methodName+"\n"+ex.getMessage());
		usage();
		throw ex;
	    }
	    tset.generate( params );
	}
    }

    public void usage() {
	System.out.println("TestGen [options] filename");
	System.out.println("such that filename is the filename of the seed ontology\n");
	System.out.println("options are:");
	System.out.println("\t--urlprefix=url");
	System.out.println("\t--testset=classname, where classname is the name of an implementation of TestSet");
	System.out.println("\t--alignname=filename [default: refalign.rdf]");
	System.out.println("\t--ontoname=filename [default: onto.rdf]");
	System.out.println("\t--outdir=directory [default: .]");
	System.out.println("\t--help");
	System.out.println("\t--debug=number [default: 0]");
	System.out.println("\t-Dparameter=value");
	System.out.println("where the parameters are");
	System.out.println( "\tRemove percentage subclasses       \""+ParametersIds.REMOVE_CLASSES+"\"" );
	System.out.println( "\tRemove percentage properties       \""+ParametersIds.REMOVE_PROPERTIES+"\"" );
	System.out.println( "\tRemove percentage comments         \""+ParametersIds.REMOVE_COMMENTS+"\"" );
	System.out.println( "\tRemove percentage restrictions     \""+ParametersIds.REMOVE_RESTRICTIONS+"\"" );
	System.out.println( "\tRemove individuals                 \""+ParametersIds.REMOVE_INDIVIDUALS+"\"" );
	System.out.println( "\tAdd percentage subclasses          \""+ParametersIds.ADD_CLASSES+"\"" );
	System.out.println( "\tAdd percentage properties          \""+ParametersIds.ADD_PROPERTIES+"\"" );
	System.out.println( "\tRename percentage classes          \""+ParametersIds.RENAME_CLASSES+"\"" );
	System.out.println( "\tRename percentage properties       \""+ParametersIds.RENAME_PROPERTIES+"\"" );
	System.out.println( "\tnoHierarchy                        \""+ParametersIds.NO_HIERARCHY+"\"" );
	System.out.println( "\tLevel flattened                    \""+ParametersIds.LEVEL_FLATTENED+"\"" );
	System.out.println( "\tAdd nbClasses to a specific level  \""+ParametersIds.ADD_CLASSESLEVEL+"\"" );
    }
}
