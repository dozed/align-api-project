/*
 * $Id: BasicParameters.java 1704 2012-03-10 16:24:07Z euzenat $
 *
 * Copyright (C) INRIA, 2004-2005, 2008-2010, 2012
 * Copyright (C) University of Montréal, 2004
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

package fr.inrialpes.exmo.align.impl; 

// import java classes
import java.util.Enumeration;
import java.util.Collection;
import java.util.Properties;
import java.io.PrintStream;
import java.io.File;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.DocumentBuilder;

import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import org.semanticweb.owl.align.Parameters;

/**
  * @deprecated
  * This class implements Parameters for compatibility purposes.
  * Parameter list structure used everywhere at the begining of the API.
  * Parameters has been "morally" deprecated in the Alignment API for long, 
  * it is now (4.4) not used anymore and marked deprecated.
  *
  * By default and for means of communication, parameter names and values 
  * are Strings (even if their type is Object).
  *
  * A note about unchecked warnings
  * java.util.Properties is declared as hashtable<Object,Object>
  * However all its accessors can only put String as key in the hashtable
  * But propertyNames returns Enumeration and not Enumeration<String>
  * Using keySet will not change anything, because it will be Set<Object>
  * Java 6 introduces Set<String> stringPropertyNames() !!
  * 
  * @author Jérôme Euzenat
  * @version $Id: BasicParameters.java 1704 2012-03-10 16:24:07Z euzenat $ 
 */

//@Deprecated
public class BasicParameters extends Properties implements Parameters, Cloneable {
 
    static final long serialVersionUID = 400L;

    public BasicParameters() {}

    @SuppressWarnings( "unchecked" )
    public BasicParameters( Properties prop ) {
	for ( Enumeration<String> e = (Enumeration<String>)prop.propertyNames(); e.hasMoreElements(); ) { //[W:unchecked]
	    String k = e.nextElement();
	    setProperty( k, prop.getProperty(k) );
	}
    }
  
    public void setParameter( String name, String value ){
	setProperty( name, value );
    }

    public void unsetParameter( String name ){
	setProperty( name, (String)null );
    }

    public String getParameter( String name ){
	return getProperty( name );
    }
    
    @SuppressWarnings( "unchecked" )
    public Enumeration<String> getNames(){
	return (Enumeration<String>)propertyNames(); //[W:unchecked]
    }

    public Collection getValues(){
	return values();
    }

    /**
     * This is legacy code.
     * java.lang.Properties offers p.storeToXML( System.out, "" )
     * and p.loadFromXML( new FileInputStream( filename ) )
     * which are natural implementers for this.
     */
    @SuppressWarnings( "unchecked" )
    public void write(){
	System.out.println("<?xml version='1.0' ?>");
	System.out.println("<Parameters>");
	for ( Enumeration<String> e = (Enumeration<String>)propertyNames(); e.hasMoreElements(); ) { //[W:unchecked]
	    String k = e.nextElement();
	    System.out.println("  <param name='"+k+"'>"+getProperty(k)+"</param>");
	}
	System.out.println("</Parameters>");
    }

    /**
     * displays the current parameters (debugging)
     */
    @SuppressWarnings( "unchecked" )
    public void displayParameters( PrintStream stream ){
	stream.println("Parameters:");
	for ( Enumeration<String> e = (Enumeration<String>)propertyNames(); e.hasMoreElements();) { //[W:unchecked]
	    String k = e.nextElement();
	    stream.println("  "+k+" = "+getProperty(k));
	}
    }

    public static BasicParameters read( String filename ){
	return read( new BasicParameters(), filename );
    }

    public static BasicParameters read( BasicParameters p, String filename ){
	try {
	    // open the stream
	    DocumentBuilderFactory docBuilderFactory =
		DocumentBuilderFactory.newInstance();
	    DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
	    Document doc = docBuilder.parse(new File(filename));

	    // normalize text representation
	    doc.getDocumentElement().normalize();

	    // Get the params
	    NodeList paramList = doc.getElementsByTagName("param");
	    int totalParams = paramList.getLength();
	    for (int s = 0; s < totalParams; s++) {
		Element paramElement = (Element)paramList.item(s);
		String paramName = paramElement.getAttribute("name");
		NodeList paramContent = paramElement.getChildNodes();
		String paramValue = paramContent.item(0).getNodeValue().trim();
		p.setParameter(paramName, paramValue); 
	    }
	} catch (SAXParseException err) {
	    System.err.println("** Parsing error: ["+ err.getLineNumber()+"]: "+err.getSystemId()); 
	    System.err.println(" " + err.getMessage());
	} catch (SAXException e) {
	    Exception x = e.getException();
	    ((x == null) ? e : x).printStackTrace();
	} catch (Throwable t) {	t.printStackTrace(); }

	return p;
    }

    public Object clone() {
	return super.clone();
    }    
}
