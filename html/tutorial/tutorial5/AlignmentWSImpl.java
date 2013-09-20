package example.ws.matcher;

import eu.sealsproject.omt.ws.matcher.AlignmentWS;

import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.net.URI;
import java.util.Properties;

import javax.jws.WebService;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;

import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;

@WebService(endpointInterface="eu.sealsproject.omt.ws.matcher.AlignmentWS")
public class AlignmentWSImpl implements AlignmentWS {

	   public String align(URI source, URI target) {
		  		   
		   // your implementation
                   return alignment; 
	}
}
