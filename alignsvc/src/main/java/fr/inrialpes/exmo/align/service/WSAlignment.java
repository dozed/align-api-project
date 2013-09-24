/*
 * $Id: WSAlignment.java 1831 2013-03-09 18:58:49Z euzenat $
 *
 * Copyright (C) INRIA, 2008-2011, 2013
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

package fr.inrialpes.exmo.align.service;

import java.util.Enumeration;
import java.util.Properties;
import java.io.OutputStream;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.net.HttpURLConnection;
import java.net.ProtocolException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.parser.AlignmentParser;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

 /**
  * This is an AlignmentProcess which perform matching by connecting
  * to an AlignmentServer and retrieving or computing an alignment.
  * It uses the SOAP web service interface
  * TODO: Implement the REST interface (see examples/wservice/AlignmentClient)
  *
  * @author Jérôme Euzenat
  * @version $Id: WSAlignment.java 1831 2013-03-09 18:58:49Z euzenat $
  */

public class WSAlignment extends URIAlignment implements AlignmentProcess {
    final static Logger logger = LoggerFactory.getLogger( WSAlignment.class );

     private URL SOAPUrl = null;
     /**
      * The address of the web service (URL).
      * This can be overridden in subclasses or in parameters
      */
     private String serviceAddress = null;

     /**
      * Process matching
      * This does not work with regular AServ web service because it returns an URL
      **/
     public void align( Alignment alignment, Properties params ) throws AlignmentException {
	 // Create the invokation message
	 if ( params.getProperty("wserver") != null ) {
	     serviceAddress = params.getProperty("wserver");
	 } else {
	     throw new AlignmentException( "WSAlignment: required parameter : wserver" );
	 }
	 try {
	     SOAPUrl = new URL( serviceAddress );
	 } catch (IOException ioex) {
	     throw new AlignmentException("Malformed service address");
	 }
	 String message = "<"+Namespace.SOAP_ENV.shortCut+":Envelope\n" +
	     "   xmlns='"+Namespace.ALIGNSVC.prefix+"'\n" +
	     "   xml:base='"+Namespace.ALIGNSVC.prefix+"'\n" +
	     "   xmlns:"+Namespace.SOAP_ENV.shortCut+"='"+Namespace.SOAP_ENV.prefix+"'\n" + 
	     "   xmlns:"+Namespace.XSI.shortCut+"='"+Namespace.XSI.prefix+"'\n" +
	     "   xmlns:"+Namespace.XSD.shortCut+"='"+Namespace.XSD.uri+"'>\n" +
	     "  <"+Namespace.SOAP_ENV.shortCut+":Body>\n";
	 // URI encoding
	 String uri1 = ((URI)getOntology1()).toString();
	 String uri2 = ((URI)getOntology2()).toString();
	 if ( uri1 == null || uri2 == null ){
	     throw new AlignmentException("Missing URIs");
	 }
	 message += "    <url1>"+uri1+"</url1>\n    <url2>"+uri2+"</url2>\n";
	 // Parameter encoding
	 for (Enumeration e = params.propertyNames(); e.hasMoreElements();) {
	     String k = (String)e.nextElement();
             if ( k != null && !k.equals("") )
	        message += "    <param name=\""+k+"\">"+params.getProperty(k)+"</param>\n";
	 }

	 message += "  </"+Namespace.SOAP_ENV.shortCut+":Body>\n"+
	     "</"+Namespace.SOAP_ENV.shortCut+":Envelope>\n";
	 byte[] byteMess = message.getBytes();

	 //logger.trace("SOAP for sending={}", message);

	 // Connect with the web service (in parameter)
	 HttpURLConnection httpConn = null;
	 try {
	     httpConn = (HttpURLConnection)SOAPUrl.openConnection();
	     
	     // Create HTTP Request
	     httpConn.setRequestProperty( "Content-Length",
					  String.valueOf( byteMess.length ) );
	     httpConn.setRequestProperty("Content-Type","text/xml; charset=utf-8");
	     //httpConn.setRequestProperty("SOAPAction","http://kameleon.ijs.si/ontolight/align");
	     httpConn.setRequestProperty("SOAPAction","align");
	     httpConn.setRequestMethod( "POST" );
	     httpConn.setDoOutput(true);
	     httpConn.setDoInput(true);
	 } catch (ProtocolException pex) {
	     throw new AlignmentException("Cannot connect");
	 } catch (IOException ioex) {
	     throw new AlignmentException("Cannot connect");
	 }

	 // Send the request through the connection
	 try {
	     OutputStream out = httpConn.getOutputStream();
	     out.write( byteMess );    
	     out.close();
	 } catch (IOException ex) {
	     throw new AlignmentException("Cannot write");
	 }

	 // Get the result
	 // Parse the result in this alignment
	 try {
	     
             //logger.trace("  response code ={}", httpConn.getResponseCode() );
             //logger.trace("  response mess ={}", httpConn.getResponseMessage() );
	     //InputStream  inSt = httpConn.getInputStream(); 
 	     //InputStreamReader isr = new InputStreamReader( inSt );
	     //BufferedReader in = new BufferedReader(isr);
	      
	     //String line;
	     //String res= "";
	     
	      
             //while( (line = in.readLine()) !=null ) {
		//res += line + "\n";
                
		//}
 
	     AlignmentParser parser = new AlignmentParser( 0 );
	     parser.initAlignment( this );
	     parser.setEmbedded( true );
	     parser.parse( httpConn.getInputStream() );
	     //parser.parseString( res ); 
	 } catch (IOException ioex) {
	     throw new AlignmentException( "XML/SOAP parsing error", ioex );
	 } catch (Exception ex) { // JE2009: To suppress in Version 4 (??)
	     throw new AlignmentException( "XML/SOAP parsing error", ex );
	 }
     }

    /**
     * Generate a copy of this alignment object
     */
    // JE: this is a mere copy of the method in BasicAlignement...
    // Should be usefull to have a better way to do it [28/7/2008: DO IT]
    public Object clone() {
	WSAlignment align = new WSAlignment();
	try { align.init( (URI)getOntology1(), (URI)getOntology2() ); }
	catch ( AlignmentException e ) {};
	align.setType( getType() );
	align.setLevel( getLevel() );
	align.setFile1( getFile1() );
	align.setFile2( getFile2() );
	for ( String[] ext : extensions.getValues() ){
	    align.setExtension( ext[0], ext[1], ext[2] );
	    }
	String oldid = align.getExtension( Namespace.ALIGNMENT.uri, "id" );
	if ( oldid != null && !oldid.equals("") ) {
	    align.setExtension( Namespace.ALIGNMENT.uri, "derivedFrom", oldid );
	    align.setExtension( Namespace.ALIGNMENT.uri, "id", (String)null );
	}
	align.setExtension( Namespace.ALIGNMENT.uri, "method", "http://exmo.inrialpes.fr/align/impl/URIAlignment#clone" );
	try {
	    align.ingest( this );
	} catch (AlignmentException ex) { 
	    logger.debug( "IGNORED Exception", ex );
	}
	return align;
    }

}

