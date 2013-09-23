/*
 * $Id: OfflineAlign.java 1266 2010-02-16 16:27:40Z euzenat $
 *
 * Copyright (C) INRIA, 2007-2010
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

package fr.inrialpes.exmo.align.plugin.neontk;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.PrintStream;// Used for debugging
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URI;
import java.util.Vector;
import java.util.Enumeration;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;

import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;

import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;

public class OfflineAlign {
	
    File  alignFolder = null;
    File  ontoFolder  = null;
	
    public OfflineAlign( File al, File on ) {
	ontoFolder  = on; 
	alignFolder = al;
    }

    String matchAndExportAlign (String method, String proj1, String selectedNeOnOnto1, String proj2, String selectedNeOnOnto2) throws Exception {	 
	//export ontologies
	//ImportExportControl ieControl = new ImportExportControl();
	//Integer name1 = new Integer(AlignView.alignId++);  
	//Integer name2 = new Integer(AlignView.alignId++);
	//File f1 = new File( selectedNeOnOnto1.replace("file:","") );
       //File f2 = new File( selectedNeOnOnto2.replace("file:","") );
	Properties p = new Properties();
	AlignmentProcess A1 = null;
	//String htmlString = null;
	//Vector corrList = new Vector();
	Integer name = new Integer(AlignView.getNewAlignId());

	//Vector<URI> uris = new Vector<URI>();

	//try {
	//uris.add( new URI(selectedNeOnOnto1) );
	//uris.add( new URI(selectedNeOnOnto2 ) );
	    Object[] mparams = {};
	    Class<?> alignmentClass = Class.forName(method);
	    Class[] cparams = {};
	    java.lang.reflect.Constructor alignmentConstructor = alignmentClass.getConstructor( cparams );
	    A1 = (AlignmentProcess)alignmentConstructor.newInstance(mparams);
	    OntologyFactory factory = null; 
	    // This should also be a static getInstance!
	    factory = OntologyFactory.getFactory();
	    fr.inrialpes.exmo.ontowrap.LoadedOntology onto1 = loadOntology( factory, proj1, selectedNeOnOnto1 );
	    fr.inrialpes.exmo.ontowrap.LoadedOntology onto2 = loadOntology( factory, proj2, selectedNeOnOnto2 );
	    A1.init( onto1, onto2 );
	    //	    A1.init( (URI)uris.get(0), (URI)uris.get(1) );
	    A1.align( (Alignment)null, p );
	    AlignView.alignmentTable.put( alignFolder.getAbsolutePath() + File.separator + name.toString(), A1 );
	    
	    // For saving locally
	    FileWriter rdfF = new FileWriter(new File( alignFolder.getAbsolutePath() + File.separator + name.toString()+ ".rdf" ));
	    AlignmentVisitor rdfV = new RDFRendererVisitor(  new PrintWriter ( rdfF )  );
	    A1.render(rdfV);
	    rdfF.flush();
	    rdfF.close();
	  	
	   // For exporting to NeonToolkit
	   FileWriter owlF    = new FileWriter(new File( ontoFolder.getAbsolutePath() + File.separator + name.toString()+ ".owl" ));
	  	
	   AlignmentVisitor V = new OWLAxiomsRendererVisitor(  new PrintWriter ( owlF )  );
	   
	   //ObjectAlignment al = ObjectAlignment.toObjectAlignment( (URIAlignment)A1 );
	   //al.render( V );
	   A1.render(V);
	   owlF.flush();
	   owlF.close();
	   //} catch ( Exception ex ) {};
       //System.out.println("match=" +name.toString());
       return alignFolder.getAbsolutePath() + File.separator + name.toString();
    }
   

    public LoadedOntology loadOntology( OntologyFactory factory, String project, String ontoURI ) throws org.semanticweb.owlapi.model.OWLOntologyCreationException {
	fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3Ontology onto = null;
	org.semanticweb.owlapi.model.OWLOntology ontology = null;

	try { // Try to get the local ontology object
	    com.ontoprise.ontostudio.owl.model.OWLModel model = com.ontoprise.ontostudio.owl.model.OWLModelFactory.getOWLModel( ontoURI, project );
	    ontology = model.getOntology();
	} catch ( org.neontoolkit.core.exception.NeOnCoreException e ) {
	    e.printStackTrace();
	    //throw new org.semanticweb.owl.align.AlignmentException("Cannot load " + uri, e);
	    // Let's try to load it from the web...
	}
	if ( ontology == null ) { // try to upload
	    ontology = ((fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3OntologyFactory)factory).getManager().loadOntology( org.semanticweb.owlapi.model.IRI.create( ontoURI ) );
	}
	onto = new fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3Ontology();
	onto.setFormalism( "OWL 2.0" );
	try {
	    onto.setFormURI( new URI("http://www.w3.org/2002/07/owl#") );
	    onto.setFile( new URI( ontoURI ) );
	} catch ( Exception ex ) {}; // never happens
	onto.setOntology( ontology );
	//onto.setURI( ontology.getURI() );
	onto.setURI( ontology.getOntologyID().getOntologyIRI().toURI() );

    return onto;
}
   
    String trimAndExportAlign (Double thres, String id) {	 
	Integer name = new Integer(AlignView.getNewAlignId());
	Alignment A1 = AlignView.alignmentTable.get( id );
	//BasicAlignment clonedA1 = (BasicAlignment)((BasicAlignment)A1).clone();
	BasicAlignment clonedA1 = null;
	      
	try {
	    File exFile = new File(id + ".rdf");
	    AlignmentParser ap = new AlignmentParser(0);
	    ap.setEmbedded(true);
	    clonedA1 = (BasicAlignment) ap.parse(exFile.toURI().toString());
				
	    File fnRdf = new File( alignFolder.getAbsolutePath() + File.separator + name.toString()+ ".rdf" );
	    if (fnRdf.exists()) fnRdf.delete();
		  
	    FileWriter rdfF = new FileWriter( fnRdf );
	    AlignmentVisitor rdfV = new RDFRendererVisitor(  new PrintWriter ( rdfF )  );
		 
	    clonedA1.render(rdfV);
	    rdfF.flush();
	    rdfF.close();
		  
	    clonedA1.cut(thres);
	    AlignView.alignmentTable.put( alignFolder.getAbsolutePath() + File.separator + name.toString(), clonedA1 );
	         
	    File owlFile = new File( ontoFolder.getAbsolutePath() + File.separator + name.toString()+ ".owl");
	    if (owlFile.exists()) owlFile.delete();
		  
	    FileWriter owlF = new FileWriter( owlFile );
		  
	    AlignmentVisitor owlV = new OWLAxiomsRendererVisitor(  new PrintWriter ( owlF )  );
	    ObjectAlignment al = ObjectAlignment.toObjectAlignment( (URIAlignment)clonedA1 );
	    al.render( owlV );		     		  
	    //clonedA1.render(owlV);
	    owlF.flush();
	    owlF.close();	  
	} catch ( Exception ex ) { ex.printStackTrace();};
	//System.out.println("trim=" +name.toString());
	return alignFolder.getAbsolutePath() + File.separator + name.toString();
    }
   
   public String[] getAllAlign() {
       if (AlignView.alignmentTable.keys() == null) return null;
       Vector<String> v = new Vector<String>();
	   
       for (Enumeration e = AlignView.alignmentTable.keys() ; e.hasMoreElements() ;) {
	   v.add((String)e.nextElement()); 
       }
	   
       String[] ls = new String[v.size()];
       for(int i=0; i< v.size(); i++) ls[i] = v.get(i);
	   
       return ls;	  
   }
   
   public void getAllAlignFromFiles() {
       String[] nameL = alignFolder.list();
       Vector<String> v = new Vector<String>();
	   
       for(int i=0; i< nameL.length; i++) 
    	   if(nameL[i].contains(".rdf"))  v.add(nameL[i]);
       
       try {
	   AlignmentParser parser = new AlignmentParser( 0 );
	   parser.setEmbedded( true );
    	   	
	   for(int i=0; i< v.size(); i++) {
    	   		
	       String key = v.get(i).replace(".rdf", "");
	       //System.out.println("Path ="+   alignFolder.getAbsolutePath() + File.separator  + v.get(i) );
	       AlignView.alignmentTable.put( alignFolder.getAbsolutePath() + File.separator + key , 
					     parser.parse( alignFolder.getAbsolutePath() + File.separator  + v.get(i)) );
	   }
       } catch ( Exception ex ) { ex.printStackTrace();};
   }
   
   public static String fileToString(File f){
       String texto = "";
       int i=0;
       try{
	   FileReader rd = new FileReader(f);
	   i = rd.read();
	   
	   while(i!=-1){
	       texto = texto+(char)i;
	       i = rd.read();
	   }
       } catch(IOException e){
	   System.err.println(e.getMessage());
       }
       return texto;
   }

}
