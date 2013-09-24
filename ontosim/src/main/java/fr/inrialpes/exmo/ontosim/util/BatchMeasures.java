/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   BatchMeasures.java is part of OntoSim.
 *
 *   OntoSim is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   OntoSim is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with OntoSim; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */
package fr.inrialpes.exmo.ontosim.util;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.OntologyNetwork;

import fr.inrialpes.exmo.align.gen.NetworkAlignmentDropper;
import fr.inrialpes.exmo.align.gen.NetworkAlignmentWeakener;
import fr.inrialpes.exmo.align.gen.OntologyNetworkWeakener;
import fr.inrialpes.exmo.align.impl.BasicOntologyNetwork;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.ontosim.AlignmentSpaceMeasure;
import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.OntoSimException;
import fr.inrialpes.exmo.ontosim.VectorSpaceMeasure;
import fr.inrialpes.exmo.ontosim.vector.model.DocumentCollection;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import gnu.getopt.Getopt;
import gnu.getopt.LongOpt;

/**
<pre>
java fr.inrialpes.exmo.ontosim.util.BatchMeasures [options] ontodir measurefile
</pre>

where the options are:
<pre>
    --aligndir=dirname -a dirname Use alignments contained in this directory (load all .rdf or .owl files)
    --output=filename -o filename Output the results in filename (stdout by default)
    --factory=[OWL|JENA|OntologyFactory subclass] -f [OWL|JENA|OntologyFactory subclass] Use the specified factory for loading ontologies
    --weaken=n -w n Suppress n% of the correspondences at random in all alignments
    --threshold -t Tells if the correspondences are suppressed at random of by suppressing the n% of lower confidence
    --help -h                       Print this message
</pre>

<CODE>ontodir</CODE> is a directory which contains only the ontologies to compare (ontologies filename must finish by extension .owl or .rdf)

<CODE>measurefile</CODE> is a text file where each line is the name of a measure to compute. examples : 
 	VectorSpaceMeasure(fr.inrialpes.exmo.ontosim.vector.CosineVM,vector.model.DocumentCollection$WEIGHT=TFIDF)
	OntologySpaceMeasure(set.MaxCoupling(entity.EntityLexicalMeasure))

@author jerome D
 */

//07-06-10
//for storing pair of ontology and its similarity value to given ontology
class Pair {
	String ontology="";
	double sim=0.0;
	
	public Pair(String ontology, double sim) {
		this.ontology=ontology;
		this.sim=sim;
	}
	
	public String toString() {
		return this.ontology+":"+this.sim+" ";
	}
}

public class BatchMeasures {

    private final static String SEP=" & ";
    public final static REFilenameFilter filter=new REFilenameFilter(".*\\.((rdf)|(owl))");

    /**
     * @param args
     */
    public static void main(String[] args) throws Exception {

	boolean weakenT=false; // is weakening random or by threshold
	int weaken=-1;         // do I weaken the network
	int drop=-1;           // do I drop alignments from the network
	boolean invert = false; // do the invertion closure of the network before
	boolean close_matrix = false; //OZ,18-08-09:do matrix of closest ontologies for all ontologies and for all measures
	boolean robustness = false; //OZ,18-08-09:prepare matrixes for robustness estimation of all measures
	double max_sim = 0.0; //OZ:for getting the highest value for each measure and each ontology
	double current_sim = 0.0; //OZ
	BufferedWriter logAppend=null; //OZ, for printing closeness_matrixes for all measures where degradation is gradullay applied	
	ArrayList<String> closest_ontologies; //OZ:for note which ontologies are the closest to one
	double threshold=0.0;//OZ, for printing purposes
	
	String basePackage="fr.inrialpes.exmo.ontosim.";
	File alignDir=null;
	// output device
	PrintStream out= System.out;

	LongOpt[] longopts = new LongOpt[10];
	
	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("outputfile", LongOpt.REQUIRED_ARGUMENT, null, 'o'); 
	longopts[2] = new LongOpt("aligndir", LongOpt.REQUIRED_ARGUMENT, null, 'a');
	longopts[3] = new LongOpt("factory", LongOpt.REQUIRED_ARGUMENT, null, 'f');
	longopts[4] = new LongOpt("weaken", LongOpt.REQUIRED_ARGUMENT, null, 'w');
	longopts[5] = new LongOpt("threshold", LongOpt.NO_ARGUMENT, null, 't');
	longopts[6] = new LongOpt("invert", LongOpt.NO_ARGUMENT, null, 'i');
	longopts[7] = new LongOpt("drop", LongOpt.REQUIRED_ARGUMENT, null, 'd');
	longopts[8] = new LongOpt("close_matrix", LongOpt.NO_ARGUMENT, null, 'c');
	longopts[9] = new LongOpt("robustness", LongOpt.NO_ARGUMENT, null, 'r');

	Getopt opts = new Getopt(BatchMeasures.class.getCanonicalName(), args, "rcihf:a:o:w:d:t", longopts);
	int c;
	while ((c = opts.getopt()) != -1)
	    switch (c) {
	    case 'h' : 	
		printUsage();
		return;
	    case 'f' : 	
		if (opts.getOptarg().equals("JENA"))
		    OntologyFactory.setDefaultFactory("fr.inrialpes.exmo.ontowrap.jena25.JENAOntologyFactory");
		else if (opts.getOptarg().equals("OWLAPI1"))
		    OntologyFactory.setDefaultFactory("fr.inrialpes.exmo.ontowrap.owlapi10.OWLAPIOntologyFactory");
		else {
		    try {
			OntologyFactory.setDefaultFactory(opts.getOptarg());
		    }
		    catch (Exception e) {
			System.err.println("No such ontology factory available, it will use "+OntologyFactory.getDefaultFactory()+" instead");
		    }
		}
		break;
	    case 'a' :
		alignDir = new File( opts.getOptarg() );
		if (! alignDir.isDirectory()) {
		    System.err.println(opts.getOptarg()+" is not a directory");
		}		
		break;
	    case 'o' :
		out = new PrintStream(new File(opts.getOptarg()));
		break;
	    case 'i' :
		invert = true;
		break;
	    case 'c':
		close_matrix = true;
		break;
	    case 'r':
		robustness = true;
		close_matrix = true;
		break;
	    case 'w' :
		try {
		    weaken=Integer.parseInt(opts.getOptarg());
		    //if (weaken==100) weaken=99; //OZ,21-08-09: workaround, weaken=100 does not work now
		    //out.print(weaken);
		    if (weaken < 0 || weaken >100) {
		    	System.err.println(opts.getOptarg()+" value must be between 0 and 100, it will not weaken alignment network");
		    	weaken=-1;
		    }
		}
		catch (NumberFormatException e) {
		    System.err.println(opts.getOptarg()+" is not a valid number, it will not weaken alignment network");
		}
		break;
	    case 'd' :
		try {
		    drop=Integer.parseInt(opts.getOptarg());
		    //out.print(drop);
		    if (drop < 0 || drop >100) {
				System.err.println(opts.getOptarg()+" value must be between 0 and 100, it will not weaken alignment network");
				drop=-1;
		    }
		}
		catch (NumberFormatException e) {
		    System.err.println(opts.getOptarg()+" is not a valid number, it will not weaken alignment network");
		}
		break;
	    case 't' :
		weakenT=true;
		break;		
	    }

	// test parameters 
	int a = opts.getOptind();
	if (args.length<a+1) {
	    printUsage();

	    System.exit(-1);
	}


	File ontDir = new File(args[a]);
	File mFile= new File(args[a+1]);

	OntologyFactory ontoFactory = OntologyFactory.getFactory();

	File[] ontFiles = ontDir.listFiles(filter);
	File[] alignFiles=null;
	AlignmentParser ap =null;
	OntologyNetwork on=null;
	if (alignDir!=null) {
	    alignFiles = alignDir.listFiles(filter);
	    ap = new AlignmentParser(0);
	    on = new BasicOntologyNetwork();
	    for (File af : alignFiles) {
		ap.initAlignment(null);
		Alignment al = ap.parse(af.toURI().toString());
		on.addAlignment(al);
	    }
	    /*
	    for(Alignment al : on.getAlignments()) {
	    	out.print(al.getOntology1URI()+" "+al.getOntology2URI());
	    }*/
	    // not clear if this action must be done before or after...
	    if ( invert ) on.invert();
	    if (weaken>0) {
			threshold = ((double)weaken)/100;
			OntologyNetworkWeakener weakener = new NetworkAlignmentWeakener(); 
			Properties p = new Properties();
			p.put("threshold", weakenT);
			on = weakener.weaken(on, threshold, p);					
	    }
	    if (drop>0) {
			threshold = ((double)drop)/100;		
			OntologyNetworkWeakener dropper = new NetworkAlignmentDropper(); 
			on = dropper.weaken(on, threshold, null);
	    }
	}



	MeasureFactory mf = new MeasureFactory(true);
	List<Measure<LoadedOntology<?>>> measures = new ArrayList<Measure<LoadedOntology<?>>>();
	BufferedReader br = new BufferedReader(new FileReader(mFile));
	String line;
	if (!close_matrix)
		out.print(SEP);
	while ((line=br.readLine())!=null) {
	    if (line.charAt(0)=='#') continue;
	    try {
		if (!close_matrix) out.print(SEP+line);
		Measure<LoadedOntology<?>> m = mf.getOntologyMeasure(line,basePackage);
		measures.add(m);

		// case VectorSpaceMeasure with TFIDF weights: add all ontologies before
		if (m instanceof VectorSpaceMeasure && ((VectorSpaceMeasure) m).getVectorType()==DocumentCollection.WEIGHT.TFIDF) {
		    for (File of : ontFiles)
			((VectorSpaceMeasure)m).addOntology(ontoFactory.loadOntology(of.toURI()));
		}

		// case of AlignmentSpaceMeasure : add all alignments
		if (m instanceof AlignmentSpaceMeasure && alignFiles!=null) {
		    ((AlignmentSpaceMeasure<?>) m).setAlignmentSpace(on);
		}
	    }
	    catch (OntoSimException e) {
		e.printStackTrace();
	    }

	}
	if (!close_matrix) out.println();
	br.close();

	if (!close_matrix) {
		for (int i=0 ; i<ontFiles.length ; i++) {
		    LoadedOntology<?> o1 = ontoFactory.loadOntology(ontFiles[i].toURI());
		    for (int j=i ; j<ontFiles.length ; j++) {
			LoadedOntology<?> o2 = ontoFactory.loadOntology(ontFiles[j].toURI());
			out.print(o1.getURI()+SEP+o2.getURI());
			for (Measure<LoadedOntology<?>> m : measures) {
			    if (m !=null)
				out.print(SEP+m.getSim(o1, o2));
			    else
				out.print(SEP+"err");
			}
			out.println();
		    }
		}
		out.close();
	}
	else {//OZ,18-08-09, header and rows names	  	  
	  //OZ:header	  
	  /*if (!robustness) { 
		  out.print(SEP);
		  for (int k=0 ; k<ontFiles.length ; k++) {
			  LoadedOntology<?> o1 = ontoFactory.loadOntology(ontFiles[k].toURI());		   
			  if (!robustness) out.print(o1.getURI()+SEP);	  
		  }	  	
		out.println();	  	
	  }*/
	  for (Measure<LoadedOntology<?>> m : measures) {//1st iteration - measures    	      	  		  
		  if (robustness) {
			//out = new PrintStream(new File(m.getClass().toString().substring(m.getClass().toString().lastIndexOf(".")+1)));		
			logAppend = new BufferedWriter(new FileWriter(m.getClass().toString().substring(m.getClass().toString().lastIndexOf(".")+1)+".cls", true));
			//logAppend.write("\n");//1st row emtpy for header
			/*
			if (drop>0) {
				logAppend = new BufferedWriter(new FileWriter(m.getClass().toString().substring(m.getClass().toString().lastIndexOf(".")+1)+"Drop", true));
			}  
			if (weaken>0) {
				logAppend = new BufferedWriter(new FileWriter(m.getClass().toString().substring(m.getClass().toString().lastIndexOf(".")+1)+"Weaken", true));
			}
			if (weaken==-1&&drop==-1) {
				System.err.println("robustness feature must be used with --drop=n or --weaken=n");
				break; //OZ:probably better way how to stop processing here
				//throw NullPointerException;  
			}
			*/
			logAppend.write(new Double(threshold).toString());
			logAppend.write(SEP);
		  }
		  else out.print(m.getClass().toString().substring(m.getClass().toString().lastIndexOf(".")+1)+SEP);		
    	  for (int i=0 ; i<ontFiles.length ; i++) {//2nd iteration - ontologies (columns)
    		  max_sim=0.0;
    		  closest_ontologies = new ArrayList<String>();
    		  
    		  ArrayList<Pair> meas = new ArrayList<Pair>(15);
    		  
    		  LoadedOntology<?> o1 = ontoFactory.loadOntology(ontFiles[i].toURI());	  
	      	  	
	    	  for (int j=0 ; j<ontFiles.length ; j++) {//3rd iteration - similarity of each pair	    		  
	    		  if (i==j) continue;
			      LoadedOntology<?> o2 = ontoFactory.loadOntology(ontFiles[j].toURI());  			      			     
			      if (m !=null) {
			    	current_sim=m.getSim(o1, o2);
			    	meas.add(new Pair(o2.getURI().toString(), current_sim));				    			    
			      }
			      else
			        if (robustness) logAppend.write(SEP+"err");			    	  
			        else out.print(SEP+"err");
	    	  }
			  //07-06-10, make a sort the pairs ontology+sim according to sim
	    	  Collections.sort(meas,new Comparator<Pair>() {
	  			public int compare(Pair p1, Pair p2) {				
	  				return -(Double.compare(p1.sim, p2.sim));
	  		  }});
	    	  for(Pair p : meas)
			    if (robustness) logAppend.write(p+" ");
			    else out.print(p+" ");
			  if (robustness) {
				  if(i!=(ontFiles.length-1)) logAppend.write(SEP);
			  }
			  else if(i!=(ontFiles.length-1)) out.print(SEP);
	      }
	      if (robustness) logAppend.write("\\\\ \n");  
	      else out.println("\\\\");
	      if (robustness) logAppend.close();	  
		  }	  
		  out.close();
	}
	
    }

    public static final void printUsage() {

	System.err.println("java "+BatchMeasures.class.getCanonicalName()+" [options] ontodir measurefile");
	System.err.println("where the options are:");
	System.err.println("\t--aligndir=dirname -a dirname Use alignments contained in this directory (load all .rdf or .owl files)");
	System.err.println("\t--output=filename -o filename Output the results in filename (stdout by default)");
	System.err.println("\t--factory=[OWL|JENA|OntologyFactory subclass] -f [OWL|JENA|OntologyFactory subclass] Use the specified factory for loading ontologies");
	System.err.println("\t--weaken=n -w n Suppress n% of the correspondences at random in all alignments");
	System.err.println("\t--threshold -t Tells if the correspondences are suppressed at random of by suppressing the n% of lower confidence");
	System.err.println("\t--drop=n -d n Suppress n% of the alignments at random in the network");
	System.err.println("\t--close_matrix -c It will generate closeness_matrix for assessing degree of agreement");
	System.err.println("\t--robustness -r It will generate several (degradated) closeness_matrixes for each measure (in combination with -d or -w), robustness");
	System.err.println("\t--invert -i Use the reflexive closure of the network");
	System.err.println("\t--help -h                       Print this message");
	System.err.println("ontodir is a directory which contains only the ontologies to compare (ontologies filename must finish by extension .owl or .rdf)");
	System.err.println("measurefile is a text file where each line is the name of a measure to compute. examples : \n" +
			"\t VectorSpaceMeasure(fr.inrialpes.exmo.ontosim.vector.CosineVM,vector.model.DocumentCollection$WEIGHT=TFIDF) \n" +
			"\t OntologySpaceMeasure(set.MaxCoupling(entity.EntityLexicalMeasure))");
	
    }

}
