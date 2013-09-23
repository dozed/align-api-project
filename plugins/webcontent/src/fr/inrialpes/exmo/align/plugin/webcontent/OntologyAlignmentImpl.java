/*
 * $Id: OntologyAlignmentImpl.java 1704 2012-03-10 16:24:07Z euzenat $
 *
 * Copyright (C) INRIA, 2007-2010, 2012
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

package fr.inrialpes.exmo.align.plugin.webcontent;

import javax.jws.WebService;
import org.weblab_project.core.model.ComposedUnit;
import org.weblab_project.core.model.Document;
import org.weblab_project.core.model.MediaUnit;
import org.weblab_project.services.exception.WebLabException;
import org.weblab_project.core.model.text.Text;
import org.weblab_project.services.ontologyalignment.types.MatchArgs;
import org.weblab_project.services.ontologyalignment.types.FindArgs;
import org.weblab_project.services.ontologyalignment.types.RetrieveArgs;
import org.weblab_project.services.ontologyalignment.types.LoadArgs;
import org.weblab_project.services.ontologyalignment.types.StoreArgs;
import org.weblab_project.services.ontologyalignment.types.TrimArgs;
import org.weblab_project.services.ontologyalignment.types.AddResourceArgs;
import org.weblab_project.services.ontologyalignment.types.AddResourceReturn;
import org.weblab_project.services.ontologyalignment.types.MatchReturn;
import org.weblab_project.services.ontologyalignment.types.FindReturn;
import org.weblab_project.services.ontologyalignment.types.RetrieveReturn;
import org.weblab_project.services.ontologyalignment.types.LoadReturn;
import org.weblab_project.services.ontologyalignment.types.StoreReturn;
import org.weblab_project.services.ontologyalignment.types.TrimReturn;
import org.weblab_project.services.ontologyalignment.OntologyAlignment;
import org.weblab_project.services.ontologyalignment.MatchException;
import org.weblab_project.services.ontologyalignment.FindException;
import org.weblab_project.services.ontologyalignment.RetrieveException;
import org.weblab_project.services.ontologyalignment.LoadException;
import org.weblab_project.services.ontologyalignment.StoreException;
import org.weblab_project.services.ontologyalignment.TrimException;
import org.weblab_project.services.ontologyalignment.AddResourceException;
import org.weblab_project.core.model.ontology.Ontology;
import org.weblab_project.core.model.Annotation;
 
import org.weblab_project.core.model.user.UsageContext;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
 
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import java.net.URI;
import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;



@WebService(endpointInterface =
"org.weblab_project.services.ontologyalignment.OntologyAlignment")
public class OntologyAlignmentImpl implements OntologyAlignment {

public MatchReturn match(MatchArgs args) throws MatchException  {

String defaultHost = "aserv.inrialpes.fr";
String defaultPort = "80";
 
String defaultMethod = "fr.inrialpes.exmo.align.impl.method.EditDistNameAlignment";

WSInterface onAlign = new WSInterface(defaultPort, defaultHost);

Ontology onto1  = args.getOnto1();
Ontology onto2  = args.getOnto2();
String uri1     = onto1.getUri();
String uri2     = onto2.getUri();
UsageContext uc = args.getUsageContext();

String matchMethod = uc.getUri();
String[] methods   = onAlign.getMethods();

boolean found = false;

for(int i=0; i < methods.length; i++) {
 if ( matchMethod.equals(methods[i]) ) found = true;
}

if(found) defaultMethod = matchMethod;

String alignURI = null;

if (!uri1.startsWith("http://") || !uri2.startsWith("http://") ) {
	WebLabException ex = new WebLabException();
	ex.setErrorMessage("ERROR : Ontology URI.");
	ex.setErrorId("OntologyAlignment");
	throw new MatchException("MatchException : ", ex);
 			   			 
} else {
	 				   		
	alignURI = onAlign.getAlignId( matchMethod, uri1, uri2  );
	if(alignURI==null || alignURI.equals(""))  {
		WebLabException ex = new WebLabException();
		ex.setErrorMessage("ERROR : Alignment URI.");
		ex.setErrorId("OntologyAlignment");
		throw new MatchException("MatchException : ", ex);
	}  
}
						
				 
MatchReturn out = new MatchReturn();
Annotation annot = new Annotation(); 
annot.setUri(alignURI);
out.setAnnotation(annot);
 
return out;

}

public FindReturn find(FindArgs args) throws FindException  {

String defaultHost = "aserv.inrialpes.fr";
String defaultPort = "80";

WSInterface onAlign = new WSInterface(defaultPort, defaultHost);

Ontology onto1  = args.getOnto1();
Ontology onto2  = args.getOnto2();
String uri1     = onto1.getUri();
String uri2     = onto2.getUri();

if (!uri1.startsWith("http://") || !uri2.startsWith("http://") ) {
	WebLabException ex = new WebLabException();
	ex.setErrorMessage("ERROR : Ontology URI.");
	ex.setErrorId("OntologyAlignment");
	throw new FindException("FindException : ", ex);
 			   			 
} else {	 			   		
	String[] alignUris = onAlign.findAlignForOntos( uri1, uri2  );
	if( alignUris==null || alignUris.length == 0)  {
		WebLabException ex = new WebLabException();
		ex.setErrorMessage("ERROR : Alignment not found.");
		ex.setErrorId("OntologyAlignment");
		throw new FindException("FindException : ", ex);
	} else {
		FindReturn out = new FindReturn();
		List<Annotation> ll = out.getAnnotation();
		for(int i=0; i < alignUris.length; i++) {
			Annotation annot = new Annotation(); 
			annot.setUri(alignUris[i]);
			ll.add(annot);
		}	 
		return out;
	}
}
						
}

public RetrieveReturn retrieve(RetrieveArgs args) throws RetrieveException  {

String defaultHost = "aserv.inrialpes.fr";
String defaultPort = "80";

WSInterface onAlign = new WSInterface(defaultPort, defaultHost);

Annotation align  = args.getAlign();
String aUri       = align.getUri();
UsageContext uc   = args.getUsageContext();
String method     = uc.getUri();


if (!aUri.startsWith("http://") ) {
	WebLabException ex = new WebLabException();
	ex.setErrorMessage("ERROR : Ontology URI.");
	ex.setErrorId("OntologyAlignment");
	throw new RetrieveException("RetrieveException : ", ex);
 			   			 
} else {	 			   		
	String alignContent = onAlign.render( aUri , method);
	if( alignContent == null )  {
		WebLabException ex = new WebLabException();
		ex.setErrorMessage("ERROR : No rendered Alignment.");
		ex.setErrorId("OntologyAlignment");
		throw new RetrieveException("RetrieveException : ", ex);
	} else {
	  /*
	  PrintWriter writer = null;
	  ByteArrayOutputStream result = new ByteArrayOutputStream(); 
	  
	  AlignmentParser ap = null;
	  URIAlignment al = null;

	  try {
			ap = new AlignmentParser(0);
			ap.setEmbedded( true );
			al = (URIAlignment) ap.parse( alignContent );
	  } catch (Exception e) {
			WebLabException ex = new WebLabException();
			ex.setErrorMessage(" Parsing problem.");
			ex.setErrorId("OntologyAlignment");
			throw new RetrieveException("RetrieveException : ", ex); 
	  }

		// Render it
		 
	  AlignmentVisitor renderer = null;
		// Redirect the output in a String
	  try {
 	  		writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( result, "UTF-8" )), true);
	  
			Object[] mparams = {(Object) writer };
			java.lang.reflect.Constructor[] rendererConstructors =
		    	Class.forName(method).getConstructors();
			renderer =
		    		(AlignmentVisitor) rendererConstructors[0].newInstance(mparams);
	  } catch (Exception ex) { 
			WebLabException e = new WebLabException();
			e.setErrorMessage("Unknown method.");
			e.setErrorId("OntologyAlignment");
			throw new RetrieveException("RetrieveException : ", e);
       	  }

	  try {
		ObjectAlignment obAl = ObjectAlignment.toObjectAlignment( al );
		obAl.render( renderer );
	    	writer.flush();
	    	writer.close();

	  } catch (Exception e) {
			WebLabException ex = new WebLabException();
			ex.setErrorMessage(" Renderer problem.");
			ex.setErrorId("OntologyAlignment");
			throw new RetrieveException("RetrieveException : ", ex); 
	  }
	  */    
	  RetrieveReturn out = new RetrieveReturn();
	  Annotation annot = new Annotation();
	  //String strRes = result.toString(); 
	  //if(strRes == null || strRes.equals("") ) {
	  if(alignContent == null || alignContent.equals("") ) {
		 annot.setData("No alignment");
	  } else
		 //annot.setData(strRes);
		 annot.setData( alignContent );
	      
	  out.setAnnotation(annot);
 	  return out;
	}
}
						
}


public TrimReturn trim(TrimArgs args) throws TrimException {
       String defaultHost = "aserv.inrialpes.fr";
       String defaultPort = "80";

       WSInterface onAlign = new WSInterface(defaultPort, defaultHost);
       Annotation th     = args.getThreshold();
        
       String threshold  = th.getData();
       Annotation align  = args.getAlign();
       String aUri  	 = align.getUri();
  
       if (!aUri.startsWith("http://") ) {
		WebLabException ex = new WebLabException();
		ex.setErrorMessage("ERROR : Alignment URI.");
		ex.setErrorId("OntologyAlignment");
		throw new TrimException("TrimException : ", ex);		   			 
       }
	
       String resUri = onAlign.trimAlign( aUri, threshold );
	
       if (resUri == null || resUri.equals("") ) {
		WebLabException ex = new WebLabException();
		ex.setErrorMessage("ERROR : Trimmed Alignment .");
		ex.setErrorId("OntologyAlignment");
		throw new TrimException("TrimException : ", ex);		   			 
       }

       TrimReturn out = new TrimReturn();
       Annotation annot = new Annotation();
       annot.setUri( resUri );
       out.setAnnotation(annot);
       return out;
}

public StoreReturn store(StoreArgs args) throws StoreException {
       String defaultHost = "aserv.inrialpes.fr";
       String defaultPort = "80";

       WSInterface onAlign = new WSInterface(defaultPort, defaultHost);
        
       Annotation align  = args.getAlign();
       String aUri  	 = align.getUri();
  
       if (!aUri.startsWith("http://") ) {
		WebLabException ex = new WebLabException();
		ex.setErrorMessage("ERROR : Alignment URI.");
		ex.setErrorId("OntologyAlignment");
		throw new StoreException("StoreException : ", ex);		   			 
       }
	
       String resUri = onAlign.storeAlign( aUri );
	
       if (resUri == null || resUri.equals("") ) {
		WebLabException ex = new WebLabException();
		ex.setErrorMessage("ERROR : Stored Alignment .");
		ex.setErrorId("OntologyAlignment");
		throw new StoreException("StoreException : ", ex);		   			 
       }

       StoreReturn out = new StoreReturn();
       Annotation annot = new Annotation();
       annot.setUri( resUri );
       out.setAnnotation(annot);
       return out;

}


public LoadReturn load(LoadArgs args) throws LoadException  {

String defaultHost = "aserv.inrialpes.fr";
String defaultPort = "80";

WSInterface onAlign = new WSInterface(defaultPort, defaultHost);

Annotation fileContent  = args.getFileContent();
String content          = fileContent.getData();

System.out.println("content="+content);
 
String alignUri = onAlign.loadStringAsAlignment( content );
if( alignUri == null )  {
		WebLabException ex = new WebLabException();
		ex.setErrorMessage("ERROR : No loaded Alignment.");
		ex.setErrorId("OntologyAlignment");
		throw new LoadException("RetrieveException : ", ex);
}  

LoadReturn out = new LoadReturn();
Annotation annot = new Annotation();
annot.setUri(alignUri);
out.setAnnotation(annot);

return out;
}

public AddResourceReturn addResource(AddResourceArgs args) throws AddResourceException {
AddResourceReturn out = new AddResourceReturn();
return out;
}

}
