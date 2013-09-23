/*
 * $Id: OnlineAlign.java 1234 2010-02-13 11:24:57Z euzenat $
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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.InputStreamReader;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.OutputStream;
import java.io.ByteArrayOutputStream;

import java.lang.StringBuffer;

import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
 
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

public class OnlineAlign {
		
    public  boolean connected = false;
    URL SOAPUrl = null;
    String SOAPAction = null;
		 
    HttpURLConnection globalConn = null;
    HttpURLConnection globalConn2 = null;
		 
    String globalAnswer = null;
			
    private static DocumentBuilder BUILDER = null;
    final DocumentBuilderFactory fac = DocumentBuilderFactory.newInstance();
		
    public OnlineAlign( String htmlPort, String host   )  {
	try {
	    SOAPUrl = new URL( "http://" + host + ":" + htmlPort + "/aserv" );
	} catch ( Exception ex ) { ex.printStackTrace(); };
	fac.setValidating(false);
	fac.setNamespaceAware(false);
	try { BUILDER = fac.newDocumentBuilder(); }
	catch (ParserConfigurationException e) { };
    }
	    
    public String uploadAlign( String alignId ) {
	SOAPAction = "loadRequest";
	String content = fileToString( new File( alignId ) );
	String answer = sendFile( content, alignId );
	String result[] = getResultsFromMessage( answer, "loadResponse" );
	//System.out.println("Loaded Align="+ result[0]);
	return result[0];
    }
	    
    public String trimAlign( String alignId, String thres ) {
	SOAPAction = "trimRequest";
	// JE: method is not implemented
	String message = createMessage( "<id>"+alignId+"</id><threshold>"+thres+"</threshold>" );
	String answer = sendMessageMonoThread( message, false );
	String result[] = getResultsFromMessage( answer, "trimResponse" );
	//System.out.println("Trim Align="+ result[0]);
	return result[0];
    }

    public String[] getMethods() {
	SOAPAction = "listmethodsRequest";
	String message = createMessage( "" );
	String answer = sendMessageMonoThread( message, false );
	String result[] = getResultsFromMessage( answer, "listmethodsResponse/classList/classname" );
	//for(int i=0; i< result.length;i++) //System.out.println("methods=" + result[i]);
	return result;
    }
	    
    public String[] findAlignForOntos( String onto1, String onto2 ) {
	SOAPAction = "findRequest";
	String message = createMessage( "<onto1>"+onto1+"</onto1><onto2>"+onto2+"</onto2>" );
	String answer = sendMessageMonoThread( message, false );
	String result[] = getResultsFromMessage( answer, "findResponse/alignmentList/alid" );
	//for(int i=0; i< result.length;i++) System.out.println("aligns for ontos=" + result[i]);
	return result; 
    }
	    
    public String[] getAllAlign() {
	SOAPAction = "listalignmentsRequest";
	String message = createMessage( "");
	String answer = sendMessageMonoThread( message, false );
	String result[] = getResultsFromMessage( answer, "listalignmentsResponse/alignmentList/alid" );
	return result;
    }
	    
    /**
     * store an alignment on the server
     */
    public String storeAlign( String alignId ) {
	SOAPAction = "storeRequest";
	String message = createMessage( "<id>"+alignId+"</id>" );
	String answer = sendMessageMonoThread( message, false );
	String result[] = getResultsFromMessage( answer, "storeResponse" );
	//System.out.println("Stored Align="+ result[0]); 
	return result[0];
    }

    //Used without ProgressBar
    public String getAlignIdMonoThread( String method, String wserver, String wsmethod, String onto1, String onto2 ) {
	SOAPAction = "matchRequest";
	String messageBody = "	<onto1>"+onto1+"</onto1>\n   <onto2>"+onto2+"</onto2>\n";
	if ( method != null )
	    messageBody += "	<method>"+method+"</method>\n";
	if ( wserver != null )
	    messageBody += "	<wserver>"+ wserver +"</wserver>\n";
	if ( wsmethod != null )
	    messageBody += "	<wsmethod>"+ wsmethod +"</wsmethod>\n";    
	messageBody += "	<force>on</force>";
	String message = createMessage( messageBody );
	String answer = sendMessageMonoThread( message, false );
	String result[] = getResultsFromMessage( answer, "matchResponse" );
	 
	return result[0];
    }
	    
    public void getAlignId( String method, String wserver, String wsmethod, String onto1, String onto2 ) {
	String[] aservArgAlign = new String[6];		
	SOAPAction = "matchRequest";
	String messageBody = "	<onto1>"+onto1+"</onto1>\n   <onto2>"+onto2+"</onto2>\n";
	if ( method != null )
	    messageBody += "	<method>"+method+"</method>\n";
	if ( wserver != null )
	    messageBody += "	<wserver>"+ wserver +"</wserver>\n";
	if ( wsmethod != null )
	    messageBody += "	<wsmethod>"+ wsmethod +"</wsmethod>\n";    
	messageBody += "	<force>on</force>";
	String message = createMessage( messageBody );
	String answer = sendMessageMonoThread( message, false );
    }
	    
    public String getAlignIdParsed( String answer ) {
    	
	String result[] = getResultsFromMessage( answer, "matchResponse" );
	return result[0];	 
    }

    /**
     * retrieve alignment for storing in OWL file
     */
    public String getOWLAlignment( String alignId ) {
	SOAPAction = "retrieveRequest";
	String message = createMessage( "<id>"+alignId+"</id><method>fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor</method>" );
	String answer = sendMessageMonoThread( message, true );
	String result[] = getResultsFromMessage( answer, "retrieveResponse/result/RDF" );
	//System.out.println("OWLAlign="+ result[0]);
	return result[0];
    }
	    
    /**
     * retrieve alignment for storing in OWL file
     */
    public String getRDFAlignment( String alignId ) {
	SOAPAction = "retrieveRequest";
	String message = createMessage( "<id>"+alignId+"</id><method>fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor</method>" );
	String answer = sendMessageMonoThread( message, true );
	String result[] = getResultsFromMessage( answer, "retrieveResponse/result/RDF" );
	return result[0];
    }
	    
    /**
     * retrieve alignment for storing in OWL file
     */
    public void getRDFAlignmentMonoThread( String alignId ) {
	SOAPAction = "retrieveRequest";
	String message = createMessage( "<id>"+alignId+"</id><method>fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor</method>" );
	String answer = sendMessageMonoThread( message, true );
	globalAnswer = answer;
    }
	    
    public String getRDFAlignmentParsed() { 
	String result[] = getResultsFromMessage( globalAnswer, "retrieveResponse/result/RDF" );
	return result[0];
    }
	    
    protected String[] getResultsFromMessage( String answer, String xpath ){
	Document domMessage = null;
	try {
	    domMessage = BUILDER.parse( new ByteArrayInputStream( answer.getBytes()) );
	} catch  ( IOException ioex ) {
	    ioex.printStackTrace();
	} catch  ( SAXException saxex ) {
	    saxex.printStackTrace();
	}
	return getTagFromSOAP( domMessage, xpath );
    }

    protected String[] getTagFromSOAP( Document dom,  String tag ){
	XPath XPATH = XPathFactory.newInstance().newXPath();
	String[] result = null;
	Node n = null;
	NodeList nl = null;
	try {
	    // The two first elements are prefixed by: "SOAP-ENV:"
	    if(tag.equals("listmethodsResponse/classList/classname") || tag.equals("listalignmentsResponse/alignmentList/alid") 
	       || tag.equals("findResponse/alignmentList/alid") ) {
		nl = (NodeList)(XPATH.evaluate("/Envelope/Body/" + tag, dom, XPathConstants.NODESET));
		result = new String[nl.getLength()];
		for (int i=0; i< nl.getLength(); i++) {
		    Node method = nl.item(i);
		    String nm = method.getFirstChild().getNodeValue();
		    if(nm!=null) result[i] = nm; 
		}
	    } else  if (tag.equals("retrieveResponse/result/RDF") ) {
		n =  (Node)(XPATH.evaluate("/Envelope/Body/" + tag, dom, XPathConstants.NODE));
		ByteArrayOutputStream stream = new ByteArrayOutputStream();
		try {
		    Transformer tf = TransformerFactory.newInstance().newTransformer();
		    tf.setOutputProperty(OutputKeys.ENCODING,"utf-8");
		    tf.setOutputProperty(OutputKeys.INDENT,"yes");
		    tf.transform(new DOMSource(n),new StreamResult(stream));
		} catch (Exception e) {}
		//Node firstnode = n.getFirstChild();
		String nm = stream.toString();
		result = new String[1];
		result[0] = nm; 
	    } else {
		Node nn =  (Node)(XPATH.evaluate("/Envelope/Body/" + tag +"/alid", dom, XPathConstants.NODE));
		result = new String[1];
		 
		Node nx  = nn.getFirstChild();
		 
		String nm = nx.getNodeValue();
		result[0] = nm; 
	    }  
	} catch (XPathExpressionException e) {
	} catch (NullPointerException e) {
	}
	return result;  
    }

    public String createMessage( String body ) {
	return "<SOAP-ENV:Envelope xmlns:SOAP-ENV=\'http://schemas.xmlsoap.org/soap/envelope/\' " +
	    "xmlns:xsi=\'http://www.w3.org/1999/XMLSchema-instance\' " + 
	    "xmlns:xsd=\'http://www.w3.org/1999/XMLSchema\'>" +
	    "<SOAP-ENV:Body>" + body + "</SOAP-ENV:Body>"+"</SOAP-ENV:Envelope>";
    }

    public String sendMessageMonoThread( String message, boolean buffered ) {
	// Create the connection
	byte[] b = message.getBytes();
	String answer = "";
	// Create HTTP Request
	try {
	    HttpURLConnection httpConn = (HttpURLConnection)SOAPUrl.openConnection();
	    httpConn.setRequestProperty( "Content-Length",
					 String.valueOf( b.length ) );
	    httpConn.setRequestProperty("Content-Type","text/xml; charset=utf-8");
	    httpConn.setRequestProperty("SOAPAction",SOAPAction);
	    httpConn.setRequestMethod( "POST" );
	    httpConn.setDoOutput(true);
	    httpConn.setDoInput(true);
	    // Send the request through the connection
	    OutputStream out = httpConn.getOutputStream();
	    out.write( b );
	    out.close( );
	    InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
	    BufferedReader in = new BufferedReader(isr);
	    // Buffering is better for large files
	    if( buffered ) {
		StringBuffer lineBuff =  new StringBuffer();
		String line;
		while ( (line = in.readLine()) != null) {
		    lineBuff.append( line + "\n");
		}
		if (in != null) in.close();
		answer = lineBuff.toString();
		//System.out.println("RDF=" + answer );
	    } else {
		// Read the response and set it to answer
		String line;
		while ((line = in.readLine()) != null) {
		    answer += line + "\n";
		}
		if (in != null) in.close();
		if(httpConn.HTTP_REQ_TOO_LONG == httpConn.getResponseCode()) System.err.println("Request too long");
	    }
	} catch  (Exception ex) {
	    //connected= false; 
	    ex.printStackTrace() ; return null;
	}
	return answer;
    }

    public String sendFile( String message, String uploadFile ) {
	// Create HTTP Request
	try {
	    HttpURLConnection httpConn = (HttpURLConnection)SOAPUrl.openConnection();
	    httpConn.setRequestProperty("SOAPAction",SOAPAction);
	    httpConn.setRequestMethod( "POST" );
	    httpConn.setDoOutput( true );
	    httpConn.setDoInput( true );
	    // Don't use a cached version of URL connection.
	    httpConn.setUseCaches ( false );
	    httpConn.setDefaultUseCaches (false);
	    File f = new File( uploadFile );
	    FileInputStream fi = new FileInputStream(f);
	    // set headers and their values.
	    httpConn.setRequestProperty("Content-Type",
					"application/octet-stream");
	    httpConn.setRequestProperty("Content-Length",
					Long.toString(f.length()));
	    // create file stream and write stream to write file data.
	    OutputStream os =  httpConn.getOutputStream();
	    String str ="";
	    try {
		// transfer the file in 4K chunks.
		byte[] buffer = new byte[4096];
		//long byteCnt = 0;
		int bytes=0;
		while (true) {
		    bytes = fi.read(buffer);
		    if (bytes < 0)  break;
		    os.write(buffer, 0, bytes );
		}
		os.flush();
	    } catch (Exception ex) {}
	    os.close();
	    fi.close();
	    //System.out.println("Upload Read done.");

	    // Read the response  
	    InputStreamReader isr = new InputStreamReader(httpConn.getInputStream());
	    BufferedReader in = new BufferedReader(isr);
	    String line;
	    StringBuffer strBuff = new StringBuffer();
	    while ((line = in.readLine()) != null) {
		strBuff.append( line + "\n");
	    }
	    if (in != null) in.close();
	    String answer = strBuff.toString();
	    //connected = true;
	    return answer;
	} catch  (Exception ex) {
	    //connected = false;
	    ex.printStackTrace();
	    return null;
	}
    }

    // With a StringBuffer ??
    public static String fileToString ( File f ) {
	String text = "";
	int i=0;
	try{
	    FileReader rd = new FileReader(f);
	    i = rd.read();
	    while( i != -1 ) {
		text += (char)i;
		i = rd.read();
	    }
	} catch ( IOException e ) { System.err.println(e.getMessage());	}
	return text;
    }
}
