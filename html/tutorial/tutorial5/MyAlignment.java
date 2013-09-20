package example.ws.matcher;

import java.net.URI;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentProcess;

import fr.inrialpes.exmo.align.impl.URIAlignment;

public class MyAlignment extends URIAlignment implements AlignmentProcess {
	
	public MyAlignment() {
    };
     
    public void align( Alignment alignment, Properties params ) throws AlignmentException {

    	       // matcher code
    } 
    	
}
