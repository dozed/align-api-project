package example.ws.matcher;

import javax.xml.ws.Endpoint;
import eu.sealsproject.omt.ws.matcher.AlignmentWS;
import eu.sealsproject.omt.ws.matcher.AlignmentWSImpl;


public class AlignmentWSPublisher {
	
	   public static void main(String args[]) {
	  
		  
		   /* Publish matcher service web service */
		   AlignmentWS serverMatcher = new AlignmentWSImpl();
		   Endpoint endpointMatcher = Endpoint.publish("http://134.155.86.66:8080/matcherWS", serverMatcher);
		   System.out.println("Matcher service published ... ");
	   }
	   
}
