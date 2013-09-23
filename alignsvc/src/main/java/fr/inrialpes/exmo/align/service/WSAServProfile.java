/*
 * $Id: WSAServProfile.java 1830 2013-03-09 18:20:23Z euzenat $
 *
 * Copyright (C) INRIA, 2007-2013
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

package fr.inrialpes.exmo.align.service;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.service.msg.Message;
import fr.inrialpes.exmo.align.service.msg.ErrorMsg;
import fr.inrialpes.exmo.align.service.msg.NonConformParameters;

import org.semanticweb.owl.align.Alignment;

import java.io.File;
import java.io.InputStream;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.ByteArrayInputStream;

import java.util.Set;
import java.util.HashSet;
import java.util.Properties;
import java.util.Enumeration;
import java.util.StringTokenizer;
import java.util.jar.JarFile;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.Attributes.Name;

import java.lang.NullPointerException;

// For message parsing
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathFactory;
import javax.xml.xpath.XPathExpressionException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * WSAServProfile: a SOAP and REST over HTTP provile for the Alignment server
 * It uses the HTTP server of HTTPAServProfile
 * 
 * Improvements to come:
 * - provide WSDL from that channel as well
 * - implement request_id management (fully missing here)
 * - use XML/Xpath parsers [Make it namespace aware please]
 * - clean up
 */

public class WSAServProfile implements AlignmentServiceProfile {
    final static Logger logger = LoggerFactory.getLogger( WSAServProfile.class );

    private int tcpPort;
    private String tcpHost;
    private AServProtocolManager manager;
    private static String wsdlSpec = "";

    private String myId;
    private String serverURL;
    private int localId = 0;

    private static DocumentBuilder BUILDER = null;

    // ==================================================
    // Socket & server code
    // ==================================================

    public void init( Properties params, AServProtocolManager manager ) throws AServException {
	this.manager = manager;
	// This may register the WSDL file to some directory
	serverURL = manager.serverURL()+"/aserv/";
	myId = "SOAPoverHTTPInterface";
	localId = 0;	

	// New XML parsing stuff
	final DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
	fac.setValidating(false);
	fac.setNamespaceAware(false); // Change this!
	try { BUILDER = fac.newDocumentBuilder(); }
	catch (ParserConfigurationException e) {
	    throw new AServException( "Cannot initialize SOAP message parsing", e );
	}

	// Read the WSDL specification
	try {
	    String classPath = System.getProperty("java.class.path",".");
	    StringTokenizer tk = new StringTokenizer(classPath,File.pathSeparator);
	    Set<String> visited = new HashSet<String>();
	    classPath = "";
	    while ( tk != null && tk.hasMoreTokens() ){
		StringTokenizer tk2 = tk;
		tk = null;
		// Iterate on Classpath
		while ( tk2 != null && tk2.hasMoreTokens() ) {
		    File file = new File( tk2.nextToken() );
		    if ( file.isDirectory() ) { // Do nothing, it is in a jar...
		    } else if ( file.toString().endsWith(".jar") &&
				!visited.contains( file.toString() ) &&
				file.exists() ) {
			visited.add( file.toString() );
			try { 
			    JarFile jar = new JarFile( file );
			    Enumeration enumeration = jar.entries();
			    while( enumeration != null && enumeration.hasMoreElements() ){
				JarEntry entry = (JarEntry)enumeration.nextElement();
				String classname = entry.toString();
				if ( classname.equals("fr/inrialpes/exmo/align/service/aserv.wsdl") ){
				    // Parse it
				    InputStream is = jar.getInputStream( entry );
				    BufferedReader in = new BufferedReader(new InputStreamReader(is));
				    String line  = in.readLine(); // Suppress the first line (<?xml...)
				    while ((line = in.readLine()) != null) {
					wsdlSpec += line + "\n";
				    }
				    if (in != null) in.close();
				    wsdlSpec = wsdlSpec.replace( "%%ASERVADDRESS%%", serverURL );
				    // exit
				    enumeration = null;
				    tk2 = null;
				    tk = null;
				    classPath = "";
				}
			    }
			    if ( wsdlSpec.equals("") ){
				// Iterate on needed Jarfiles
				// JE(caveat): this deals naively with Jar files,
				// in particular it does not deal with section'ed MANISFESTs
				Attributes mainAttributes = jar.getManifest().getMainAttributes();
				String path = mainAttributes.getValue( Name.CLASS_PATH );
				logger.debug("  >CP> "+path);
				if ( path != null && !path.equals("") ) {
				    // JE: Not sure where to find the other Jars:
				    // in the path or at the local place?
				    classPath += File.pathSeparator+file.getParent()+File.separator + path.replaceAll("[ \t]+",File.pathSeparator+file.getParent()+File.separator);
				}
			    }
			} catch (NullPointerException nullexp) { //Raised by JarFile
			    logger.warn( "IGNORED Warning {} unavailable", file );
			}
		    }
		}
		if ( !classPath.equals("") ) {
		    tk =  new StringTokenizer(classPath,File.pathSeparator);
		    classPath = "";
		}
	    }
	} catch (IOException ioex) {
	    logger.debug( "IGNORED Exception", ioex );
	}
    }

    public void close(){
	// This may unregister the WSDL file to some directory
    }
    
    // ==================================================
    // API parts
    // ==================================================

    /**
     * HTTP protocol implementation
     * each call of the protocol is a direct URL
     * and the answer is through the resulting page (RDF? SOAP? HTTP? JSON?)
     * Not implemented yet
     * but reserved if appears useful
     */
    public String protocolAnswer( String uri, String perf, Properties header, Properties param ) {
	String method = null;
	String message = null;
	Properties newparameters = null;
	Message answer = null;
	boolean restful = (param.getProperty("restful")==null)?false:true;
	String svcNS = "\n       xml:base='"+Namespace.ALIGNSVC.prefix+"'"+
	    "\n       xmlns='"+Namespace.ALIGNSVC.prefix+"'";
	String msg = "";

	// Set parameters if necessary
	if ( restful ) {
	    method = perf;
	    newparameters = param;
	} else {
	    method = header.getProperty("SOAPAction");
	    if ( param.getProperty( "filename" ) == null ) {
		// NOTE: we currently pass the file in place of a SOAP message
		// hence there is no message and no parameters to parse
		// However, there is a way to pass SOAP messages with attachments
		// It would be better to implement this. See:
		// http://www.oracle.com/technology/sample_code/tech/java/codesnippet/webservices/attachment/index.html
		message = param.getProperty("content").trim();
		// Create the DOM tree for the SOAP message
		Document domMessage = null;
		try {
		    domMessage = BUILDER.parse( new ByteArrayInputStream( message.getBytes()) );
		} catch  ( IOException ioex ) {
		    logger.debug( "IGNORED Exception", ioex );
		    answer = new NonConformParameters(0,(Message)null,myId,"Cannot Parse SOAP message",message,(Properties)null);
		} catch  ( SAXException saxex ) {
		    logger.debug( "IGNORED Exception", saxex );
		    answer = new NonConformParameters(0,(Message)null,myId,"Cannot Parse SOAP message",message,(Properties)null);
		}
		newparameters = getParameters( domMessage );
	    } else {
		newparameters = new Properties();
	    }
	}

	// Process the action
	if ( perf.equals("WSDL") || method.equals("wsdl") || method.equals("wsdlRequest") ) {
	    msg += wsdlAnswer( !restful );
	} else if ( method.equals("listalignmentsRequest") || method.equals("listalignments") ) {
	    msg += "    <listalignmentsResponse"+svcNS+">\n      <alignmentList>\n";
	    if ( newparameters.getProperty("msgid") != null ) {
		msg += "        <in-reply-to>"+newparameters.getProperty("msgid")+"</in-reply-to>\n";
	    }
	    for( Alignment al: manager.alignments() ){
		String id = al.getExtension(Namespace.ALIGNMENT.uri, Annotations.ID);
		msg += "        <alid>"+id+"</alid>\n";
	    }
	    msg += "      </alignmentList>\n    </listalignmentsResponse>\n";
	    // -> List of URI
	} else if ( method.equals("listmethodsRequest") || method.equals("listmethods") ) { // -> List of String
	    msg += getClasses( "listmethodsResponse", svcNS, manager.listmethods(), newparameters );
	} else if ( method.equals("listrenderersRequest") || method.equals("listrenderers") ) { // -> List of String
	    msg += getClasses( "listrenderersResponse", svcNS, manager.listrenderers(), newparameters );
	} else if ( method.equals("listservicesRequest") || method.equals("listservices") ) { // -> List of String
	    msg += getClasses( "listservicesResponse", svcNS, manager.listservices(), newparameters );
	} else if ( method.equals("listevaluatorsRequest") || method.equals("listevaluators") ) { // -> List of String
	    msg += getClasses( "listevaluatorsResponse", svcNS, manager.listevaluators(), newparameters );
	} else if ( method.equals("storeRequest") || method.equals("store") ) { // URI -> URI
	    if ( newparameters.getProperty( "id" ) == null ) {
		answer = new NonConformParameters(0,(Message)null,myId,"",message,(Properties)null);
	    } else {
		answer = manager.store( new Message(newId(),(Message)null,myId,serverURL,newparameters.getProperty( "id" ), newparameters) );
	    }
	    msg += "    <storeResponse"+svcNS+">\n"+answer.SOAPString()+"    </storeResponse>\n";
	} else if ( method.equals("invertRequest") || method.equals("invert") ) { // URI -> URI
	    if ( newparameters.getProperty( "id" ) == null ) {
		answer = new NonConformParameters(0,(Message)null,myId,"",message,(Properties)null);
	    } else {
		answer = manager.inverse( new Message(newId(),(Message)null,myId,serverURL, newparameters.getProperty( "id" ), newparameters) );
	    }
	    msg += "    <invertResponse"+svcNS+">\n"+answer.SOAPString()+"    </invertResponse>\n";
	} else if ( method.equals("trimRequest") || method.equals("trim") ) { // URI * string * float -> URI
	    if ( newparameters.getProperty( "id" ) == null ) {
		answer = new NonConformParameters(0,(Message)null,myId,"",message,(Properties)null);
	    } else if ( newparameters.getProperty( "threshold" ) == null ) {
		answer = new NonConformParameters(0,(Message)null,myId,"",message,(Properties)null);
	    } else {
		if ( newparameters.getProperty( "type" ) == null ) {
		    newparameters.setProperty( "type", "hard" );
		}
		answer = manager.trim( new Message(newId(),(Message)null,myId,serverURL,newparameters.getProperty( "id" ), newparameters) );
	    }
	    msg += "    <trimResponse"+svcNS+">\n"+answer.SOAPString()+"    </trimResponse>\n";
	} else if ( method.equals("matchRequest") || method.equals("match") ) { // URL * URL * URI * String * boolean * (newparameters) -> URI
	    if ( newparameters.getProperty( "onto1" ) == null ) {
		answer = new NonConformParameters(0,(Message)null,myId,"",message,(Properties)null);
	    } else if ( newparameters.getProperty( "onto2" ) == null ) {
		answer = new NonConformParameters(0,(Message)null,myId,"",message,(Properties)null);
	    } else {
		answer = manager.align( new Message(newId(),(Message)null,myId,serverURL,"", newparameters) );
	    }
	    msg += "    <matchResponse"+svcNS+">\n"+answer.SOAPString()+"</matchResponse>\n";
	} else if ( method.equals("align") ) { // URL * URL * (newparameters) -> URI
	    // This is a dummy method for emulating a WSAlignement service
	    if ( newparameters.getProperty( "onto1" ) == null ) {
		answer = new NonConformParameters(0,(Message)null,myId,"",message,(Properties)null);
	    } else if ( newparameters.getProperty( "onto2" ) == null ) {
		answer = new NonConformParameters(0,(Message)null,myId,"",message,(Properties)null);
	    } else { // Use the required method if it exists
		if ( newparameters.getProperty( "wsmethod" ) == null ) {
		    newparameters.setProperty( "method", "fr.inrialpes.exmo.align.impl.method.StringDistAlignment" );
		} else {
		    newparameters.setProperty( "method", newparameters.getProperty( "wsmethod" ) );
	    	} // Match the two ontologies
		Message result = manager.align( new Message(newId(),(Message)null,myId,serverURL,"", newparameters) );
		if ( result instanceof ErrorMsg ) {
		    answer = result;
		} else {
		    // I got an answer so ask the manager to return it as RDF/XML
		    newparameters = new Properties();
		    newparameters.setProperty( "id",  result.getContent() );
		    if ( newparameters.getProperty( "id" ) == null ) {
			answer = new NonConformParameters(0,(Message)null,myId,"",message,(Properties)null);
		    } else {
			newparameters.setProperty( "method",  "fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor" );
			newparameters.setProperty( "embedded", "true" );
			answer = manager.render( new Message(newId(),(Message)null,myId,serverURL, "", newparameters) );
		    }
		}
	    }
	    msg += "    <alignResponse"+svcNS+">\n";
	    if ( answer instanceof ErrorMsg ) {
		msg += answer.SOAPString();
	    } else {
		// JE: Depending on the type we should change the MIME type
		// This should be returned in answer.getParameters()
		msg += "      <result>\n" + answer.getContent() + "      </result>\n";
	    }
	    msg += "    </alignResponse>\n";
	} else if ( method.equals("correspRequest") || method.equals("corresp") ) { // URI * URI * boolean * (newparameters) -> URI*
	    if ( newparameters.getProperty( "id" ) == null ) {
		answer = new NonConformParameters(0,(Message)null,myId,"",message,(Properties)null);
	    } else if ( newparameters.getProperty( "entity" ) == null ) {
		answer = new NonConformParameters(0,(Message)null,myId,"",message,(Properties)null);
	    } else {
		answer = manager.findCorrespondences( new Message(newId(),(Message)null,myId,serverURL,"", newparameters) );
	    }
	    msg += "    <correspResponse"+svcNS+">\n"+answer.SOAPString()+"    </correspResponse>\n";
	} else if ( method.equals("findRequest") || method.equals("find") ) { // URI * URI -> List of URI
	    if ( newparameters.getProperty( "onto1" ) == null && newparameters.getProperty( "onto2" ) == null ) {
		answer = new NonConformParameters(0,(Message)null,myId,"",message,(Properties)null);
	    } else {
		answer = manager.existingAlignments( new Message(newId(),(Message)null,myId,serverURL,"", newparameters) );
            }
	    msg += "    <findResponse"+svcNS+">\n"+answer.SOAPString()+"    </findResponse>\n";
	} else if ( method.equals("retrieveRequest") || method.equals("retrieve")) { // URI * method -> XML
	    if ( newparameters.getProperty( "id" ) == null ) {
		answer = new NonConformParameters(0,(Message)null,myId,"",message,(Properties)null);
	    } else if ( newparameters.getProperty( "method" ) == null ) {
		answer = new NonConformParameters(0,(Message)null,myId,"",message,(Properties)null);
	    } else {
		newparameters.setProperty( "embedded", "true" );
		answer = manager.render( new Message(newId(),(Message)null,myId,serverURL, "", newparameters) );
	    }
	    msg += "    <retrieveResponse"+svcNS+">\n";		
	    if ( answer instanceof ErrorMsg ) {
		msg += answer.SOAPString();
	    } else {
		// JE: Depending on the type we should change the MIME type
		// This should be returned in answer.getParameters()
		msg += "      <result>\n" + answer.getContent() + "      \n</result>";
	    }
	    msg += "\n    </retrieveResponse>\n";
	} else if ( method.equals("metadataRequest") || method.equals("metadata") ) { // URI -> XML
	    if ( newparameters.getProperty( "id" ) == null ) {
		answer = new NonConformParameters(0,(Message)null,myId,"",message,(Properties)null);
	    } else {
		newparameters.setProperty( "embedded", "true" );
		newparameters.setProperty( "method", "fr.inrialpes.exmo.align.impl.renderer.XMLMetadataRendererVisitor");
		answer = manager.render( new Message(newId(),(Message)null,myId,serverURL, "", newparameters) );
            }
	    msg += "    <metadataResponse"+svcNS+">\n"+answer.SOAPString()+"\n    </metadataResponse>\n";
	} else if ( method.equals("loadRequest") || method.equals("load") ) { // URL -> URI
	    if ( newparameters.getProperty( "url" ) == null &&
		 param.getProperty( "filename" ) != null ) {
		// HTTP Server has stored it in filename (HTMLAServProfile)
		newparameters.setProperty( "url",  "file://"+param.getProperty( "filename" ) );
	    } else if ( newparameters.getProperty( "url" ) == null &&
		 param.getProperty( "filename" ) == null ) {
		answer = new NonConformParameters(0,(Message)null,myId,"",message,(Properties)null);
	    }
	    answer = manager.load( new Message(newId(),(Message)null,myId,serverURL,"", newparameters) );
	    msg += "    <loadResponse"+svcNS+">\n"+answer.SOAPString()+"    </loadResponse>\n";
	    /*
	      // JE2009: This has never been in use.
	} else if ( method.equals("loadfileRequest") ) { // XML -> URI
	    if ( newparameters.getProperty( "url" ) == null ) {
		answer = new NonConformParameters(0,(Message)null,myId,"",message,(Properties)null);
	    } else {
		answer = manager.load( new Message(newId(),(Message)null,myId,serverURL,"", newparameters) );
	    }
	    msg += "    <loadResponse"+svcNS+">\n"+answer.SOAPString()+"    </loadResponse>\n";
	    */
	} else if ( method.equals("translateRequest") ) { // XML * URI -> XML
	    // Not done yet
	    msg += "    <translateResponse "+svcNS+">\n"+"    </translateResponse>\n";
	} else {
	    msg += "    <UnRecognizedAction "+svcNS+"/>\n";
	}

	if ( restful ) {
	    return msg;
	} else {
	    return "<"+Namespace.SOAP_ENV.shortCut+":Envelope\n"  +
		"   xmlns:"+Namespace.SOAP_ENV.shortCut+"='"+Namespace.SOAP_ENV.prefix+"'\n"+
		"   xmlns:"+Namespace.XSI.shortCut+"='"+Namespace.XSI.prefix+"'\n" +
		"   xmlns:"+Namespace.XSD.shortCut+"='"+Namespace.XSD.uri+"'>\n" +
		"  <"+Namespace.SOAP_ENV.shortCut+":Body>\n"+msg+"  </"+Namespace.SOAP_ENV.shortCut+":Body>\n" +
		"</"+Namespace.SOAP_ENV.shortCut+":Envelope>\n";
	}
    }

    public static String wsdlAnswer( boolean embedded ) { 
	if ( embedded )	return wsdlSpec;
	else return "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"+wsdlSpec;
    }

    /**
     * Extract parameters from a DOM document resulting from parsing a SOAP messgae
     */
    private Properties getParameters( Document doc ) {
	Properties params = new Properties();
	XPath path = XPathFactory.newInstance().newXPath();
	try {
	    XPathExpression expr = path.compile("//Envelope/Body/*");
	    NodeList result = (NodeList)expr.evaluate(doc, XPathConstants.NODESET);
	    // Believe it or not, NodeList has no iterator!
	    for (int i = 0; i < result.getLength(); i++) {
		Node item = result.item(i);
		// Check getNodeType() please
		String key = item.getNodeName();
		if ( key != null ) {
		    String val = item.getTextContent().trim();
		    // This is for <param name="k">value</param>
		    if ( key.equals("param") ) {
			key = item.getAttributes().getNamedItem("name").getNodeValue();
		    }
		    params.setProperty( key, val );
		}
	    }
	} catch (XPathExpressionException e) {
	  logger.warn( "[getParameters] XPath exception: should not occur");
	} catch (NullPointerException e) {
	  logger.warn( "[getParameters] NullPointerException: should not occur");
	}
	return params;
    }

    private int newId() { return localId++; }

    private String buildAnswer( String tag, Message answer, Properties param ){
	String res = "    <"+tag+">\n";
	if ( param.getProperty("msgid") != null ) {
	    res += "      <in-reply-to>"+param.getProperty("msgid")+"</in-reply-to>\n";
	}
	res += answer.SOAPString();
	res += "    </"+tag+">\n";
	return res;
    }

    private String getClasses( String tag, String ns, Set<String> classlist, Properties param ){
	String res = "    <"+tag+ns+">\n      <classList>\n";
	if ( param.getProperty("msgid") != null ) {
	    res += "        <in-reply-to>"+param.getProperty("msgid")+"</in-reply-to>\n";
	}
	for( String mt: classlist ) {
	    res += "        <classname>"+mt+"</classname>\n";
	}
	res += "      </classList>\n    </"+tag+">\n";
	return res;
    }

}
