/*
 * $Id: XMLParser.java 1726 2012-05-09 23:22:05Z euzenat $
 *
 * Copyright (C) INRIA, 2003-2005, 2007-2012
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

package fr.inrialpes.exmo.align.parser;

//Imported SAX classes
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;

//Imported JAXP Classes
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.parsers.ParserConfigurationException;

//Imported JAVA classes
import java.io.IOException;
import java.io.Reader;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.lang.Double;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.ontowrap.Ontology;

import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.BasicCell;
import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.Extensions;

/**
 * This class allows the creation of a parser for an Alignment file.
 * The class is called by:
 * AlignmentParser parser = new AlignmentParser( debugLevel );
 * Alignment alignment = parser.parse( input );
 * input can be a URI as a String, an InputStream
 * This new version (January 2004) parses the alignment description in
 * RDF/XML/OWL format
 *
 */

public class XMLParser extends DefaultHandler {

    /**
     * level of debug/warning information
     */
    protected int debugMode = 0;
    
    /**
     * a URI to a process
     */
    protected String uri = null;
    
    /**
     * the first Ontology 
     */
    Ontology onto1 = null;
    Ontology curronto = null;
    
    /**
     * the second Ontology 
     */
    Ontology onto2 = null;

    /**
     * the alignment that is parsed
     * We always create a URIAlignment (we could also use a BasicAlignment).
     * This is a pitty but the idea of creating a particular alignment
     * is not in accordance with using an interface.
     */
    protected Alignment alignment = null;

    /**
     * the content found as text...
     */
    protected String content = null;
    
    /**
     * the first entity of a cell
     */
    protected Object cl1 = null;
    
    /**
     * the second entity of a cell
     */
    protected Object cl2 = null;
    
    /**
     * the relation content as text...
     */
    protected Cell cell = null;
    
    /**
     * the relation content as text...
     */
    protected String relation = null;
    
    /**
     * the cell id as text...
     */
    protected String id = null;

    /**
     * the semantics of the cell (default first-order)...
     */
    protected String sem = null;

    /**
     * Cell extensions (default null)
     */
    protected Extensions extensions = null;

    /**
     * the measure content as text...
     */
    protected String measure = null;
    
    /**
     * XML Parser
     */
    protected SAXParser parser = null;

    /**
     * The parsing level, if equal to 3 we are in the Alignment
     * if equal to 5 we are in a cell
     * and can find metadata
     */
    protected int parseLevel = 0;

    /**
     * Is the Alignment RDF for embedded in a larger XML structure to be parsed.
     */
    protected boolean embedded = false;

    /**
     * The level at which we found the Alignment tag.
     * It is -1 outside the alignment.
     */
    protected int alignLevel = -1;

    /** 
     * Creates an XML Parser.
     */
    public XMLParser() throws ParserConfigurationException, SAXException {
	this(0);
    }

    /** 
     * Creates an XML Parser.
     * @param debugMode The value of the debug mode
     */
    public XMLParser( int debugMode ) throws ParserConfigurationException, SAXException {
	this.debugMode = debugMode;
	SAXParserFactory parserFactory = SAXParserFactory.newInstance();
	if (debugMode > 0) {
	    parserFactory.setValidating(true);
	} else {
	    parserFactory.setValidating(false);
	}
	parserFactory.setNamespaceAware(true);
	parser = parserFactory.newSAXParser();
    }

    public void setEmbedded( boolean b ){
	embedded = b;
    }
    
    /** 
     * Parses the document corresponding to the URI given in parameter
     * If the current process has links (import or include) to others documents then they are 
     * parsed.
     * @param uri URI of the document to parse
     */
    public Alignment parse( String uri ) throws AlignmentException {
	try {
	    parser.parse( uri, this );
	} catch ( SAXException sex ) {
	    throw new AlignmentException( "Parsing error", sex );
	} catch ( IOException ioex ) {
	    throw new AlignmentException( "I/O error", ioex );
	}
	return alignment;
    }

    /** 
     * Parses a reader, used for reading from a string
     * @param r the reader from which to parse
     */
    public Alignment parse( Reader r ) throws AlignmentException {
	try {
	    parser.parse( new InputSource( r ), this );
	} catch ( SAXException sex ) {
	    throw new AlignmentException( "Parsing error", sex );
	} catch ( IOException ioex ) {
	    throw new AlignmentException( "I/O error", ioex );
	}
	return alignment;
    }

    /** 
     * Parses a string instead of a URI
     * @param s String the string to parse
     */
    public Alignment parse( InputStream s ) throws AlignmentException {
	try {
	    parser.parse( s, this );
	} catch ( SAXException sex ) {
	    throw new AlignmentException( "Parsing error", sex );
	} catch ( IOException ioex ) {
	    throw new AlignmentException( "I/O error", ioex );
	}
	return alignment;
    }

    /** 
     * Allows to have the parser filling an existing alignment instead
     * of creating a new one
     * @param al URIAlignment the alignment to be returned by the parser
     *
     * Note that this function is also useful for reseting the parser 
     * and using it once again by parser.initAlignment( null )
     * Otherwise, this may lead to errors.
     */
    public void initAlignment( URIAlignment al ) {
	alignment = al;
    }
    
  /** 
   * Called by the XML parser at the begining of an element.
   * The corresponing graph component is create for each element.
   *
   * @param namespaceURI 	The namespace of the current element
   * @param pName 			The local name of the current element
   * @param qname					The name of the current element 
   * @param atts 					The attributes name of the current element 
   */
    public void startElement(String namespaceURI, String pName, String qname, Attributes atts) throws SAXException {
	if(debugMode > 2) System.err.println("startElement XMLParser : " + pName);
	parseLevel++;
	if( namespaceURI.equals( Namespace.ALIGNMENT.uri+"#" )
	    || namespaceURI.equals( Namespace.ALIGNMENT.uri ) )  {
	    if (pName.equals( SyntaxElement.RULE_RELATION.name )) {
	    } else if (pName.equals( SyntaxElement.SEMANTICS.name )) {
	    } else if (pName.equals( SyntaxElement.MEASURE.name )) {
	    } else if (pName.equals( SyntaxElement.ENTITY2.name )) {
		if(debugMode > 2) 
		    System.err.println(" resource = " + atts.getValue(SyntaxElement.RDF_RESOURCE.print()));
		try {
		    cl2 = new URI( atts.getValue(SyntaxElement.RDF_RESOURCE.print()) );
		} catch (URISyntaxException e) {
		    throw new SAXException("Malformed URI: "+atts.getValue(SyntaxElement.RDF_RESOURCE.print()));
		}
	    } else if (pName.equals( SyntaxElement.ENTITY1.name )) {
		if(debugMode > 2) 
		    System.err.println(" resource = " + atts.getValue(SyntaxElement.RDF_RESOURCE.print()));
		try {
		    cl1 = new URI( atts.getValue( SyntaxElement.RDF_RESOURCE.print() ) );
		} catch (URISyntaxException e) {
		    throw new SAXException("Malformed URI: "+atts.getValue(SyntaxElement.RDF_RESOURCE.print()));
		}
	    } else if (pName.equals( SyntaxElement.CELL.name )) {
		if ( alignment == null )
		    { throw new SAXException("No alignment provided"); };
		if ( atts.getValue( SyntaxElement.RDF_ID.print() ) != null ){
		    id = atts.getValue( SyntaxElement.RDF_ID.print() );
		} else if ( atts.getValue( SyntaxElement.RDF_ABOUT.print() ) != null ){
		    id = atts.getValue( SyntaxElement.RDF_ABOUT.print() );
		}
		sem = null;
		measure = null;
		relation = null;
		extensions = null;
		cl1 = null;
		cl2 = null;
	    } else if (pName.equals( SyntaxElement.MAP.name )) {//"map"
		try {
		    alignment.init( onto1, onto2 );
		} catch ( AlignmentException e ) {
		    throw new SAXException("Catched alignment exception", e );
		}
	    } else if (pName.equals( SyntaxElement.FORMALISM.name )) {
		if ( atts.getValue( SyntaxElement.URI.name ) != null )
		    try {
			curronto.setFormURI( new URI(atts.getValue( SyntaxElement.URI.name )) );
		    } catch ( URISyntaxException e ) {
			throw new SAXException("Malformed URI"+atts.getValue( SyntaxElement.URI.name ), e );
		    };
		if ( atts.getValue( SyntaxElement.NAME.name ) != null )
		    curronto.setFormalism( atts.getValue( SyntaxElement.NAME.name ) );
	    } else if (pName.equals( SyntaxElement.FORMATT.name )) {
	    } else if (pName.equals( SyntaxElement.LOCATION.name )) {
	    } else if (pName.equals( SyntaxElement.ONTOLOGY.name )) {
		String about = atts.getValue( SyntaxElement.RDF_ABOUT.print() );
		if ( about != null && !about.equals("") ) {
		    try {
			curronto.setURI( new URI( about ) );
		    } catch (URISyntaxException e) {
			throw new SAXException("onto2: malformed URI");
		    }
		}
	    } else if (pName.equals( SyntaxElement.MAPPING_TARGET.name )) {
		curronto = onto2;
	    } else if (pName.equals( SyntaxElement.MAPPING_SOURCE.name )) {
		curronto = onto1;
	    } else if (pName.equals("uri2")) { // Legacy
	    } else if (pName.equals("uri1")) { // Legacy
	    } else if (pName.equals( SyntaxElement.TYPE.name )) {
	    } else if (pName.equals( SyntaxElement.LEVEL.name )) {
	    } else if (pName.equals( SyntaxElement.XML.name )) {
	    } else if (pName.equals( SyntaxElement.ALIGNMENT.name )) {
		alignLevel = parseLevel;
		parseLevel = 2; // for embeded (RDF is usually 1)
		if ( alignment == null ) alignment = new URIAlignment();
		onto1 = ((URIAlignment)alignment).getOntologyObject1();
		onto2 = ((URIAlignment)alignment).getOntologyObject2();
		String about = atts.getValue( SyntaxElement.RDF_ABOUT.print() );
		if ( about != null && !about.equals("") ) {
		    alignment.setExtension( Namespace.ALIGNMENT.uri, Annotations.ID, about );
		};
	    } else {
		if ( debugMode > 0 ) System.err.println("[XMLParser] Unknown element name : "+pName);
	    };
	} else if ( namespaceURI.equals( Namespace.SOAP_ENV.prefix )) { //"http://schemas.xmlsoap.org/soap/envelope/"))  {
	    // Ignore SOAP namespace
	    if ( !pName.equals("Envelope") && !pName.equals("Body") ) {
		throw new SAXException("[XMLParser] unknown element name: "+pName); };
	} else if (namespaceURI.equals( Namespace.RDF.prefix )) { //"http://www.w3.org/1999/02/22-rdf-syntax-ns#"
	    if ( !pName.equals("RDF") ) {
		throw new SAXException("[XMLParser] unknown element name: "+pName); };
	} else if (namespaceURI.equals( Namespace.EDOAL.prefix )) { 
	    throw new SAXException("[XMLParser] EDOAL alignment must have type EDOAL: "+pName);
	} else {
	    if ( alignLevel != -1 && parseLevel != 3 && parseLevel != 5 && !embedded ) throw new SAXException("[XMLParser("+parseLevel+")] Unknown namespace : "+namespaceURI);
	}
    }

    private Object getEntity( Object ontology, String name ) throws SAXException {
	try { return new URI( name ); }
	catch (URISyntaxException e) {
	    throw new SAXException("[XMLParser] bad URI syntax : "+name);}
    }

    /**
     * Put the content in a variable
    public void characters(char ch[], int start, int length) {
	content = new String( ch, start, length );
	if(debugMode > 2) 
	    System.err.println("content XMLParser : " + content);
    }
    */

    /* From a patch proposed by Sabine Massmann
     * Get around some nasty double parsing bug. Sometimes it parses
     * 6.925955630686735E-4 as
     * content XMLParser : 6.925955630686735
     * content XMLParser : E-4
     */
    public void characters( char ch[], int start, int length ) {
	String newContent = new String( ch, start, length );
	if ( content != null && content.indexOf('.',0) != -1 // a float
	     && newContent != null && !newContent.startsWith("\n ") 
	     ) {
	    content += newContent;
	} else {
	    content = newContent; 
	}
	if ( debugMode > 2 ) System.err.println("content XMLParser : " + content);
    }

    /** 
     * Called by the XML parser at the end of an element.
     *
     * @param namespaceURI 	The namespace of the current element
     * @param pName 			The local name of the current element
     * @param qName					The name of the current element 
     */
    public  void endElement(String namespaceURI, String pName, String qName ) throws SAXException {
	if(debugMode > 2) 
	    System.err.println("endElement XMLParser : " + pName);
	if( namespaceURI.equals( Namespace.ALIGNMENT.uri+"#" )
	    || namespaceURI.equals( Namespace.ALIGNMENT.uri ) )  {
	    try {
		if (pName.equals( SyntaxElement.RULE_RELATION.name )) {
		    relation = content;
		} else if (pName.equals( SyntaxElement.MEASURE.name )) {
		    measure = content;
		} else if (pName.equals( SyntaxElement.SEMANTICS.name )) {
		    sem = content;
		} else if (pName.equals( SyntaxElement.ENTITY2.name )) {
		} else if (pName.equals( SyntaxElement.ENTITY1.name )) {
		} else if (pName.equals( SyntaxElement.CELL.name )) {
		    if(debugMode > 1) {
			System.err.print(" " + cl1);
			System.err.print(" " + cl2);
			System.err.print(" " + relation);
			System.err.println(" " + Double.parseDouble(measure));
		    }
		    if ( cl1 == null || cl2 == null ) {
			// Maybe we could just print this out and fail in the end.
			//throw new SAXException( "Missing entity "+cl1+" "+cl2 );
			// The cell is void
			System.err.println("Warning (cell voided), missing entity "+cl1+" "+cl2 );
		    } else if ( measure == null || relation == null ){
			cell = alignment.addAlignCell( cl1, cl2);
		    } else {
			// This test must be revised for more generic confidence (which should then be strings)
			double conf = Double.parseDouble( measure );
			if ( conf > 1. || conf < 0. )
			    throw new SAXException( "Bad confidence value : "+conf+" (should belong to [0. 1.])" );
			cell = alignment.addAlignCell( cl1, cl2, relation, conf );}
		    if ( id != null ) cell.setId( id );
		    if ( sem != null ) cell.setSemantics( sem );
		    if ( extensions != null ) ((BasicCell)cell).setExtensions( extensions );
		} else if (pName.equals( SyntaxElement.MAP.name )) {
		} else if (pName.equals("uri1")) { // Legacy
		    try {
			URI u = new URI( content );
			onto1.setURI( u );
			if ( onto1.getFile() == null ) onto1.setFile( u );
		    } catch (URISyntaxException e) {
//			throw new SAXException("uri1: malformed URI : "+content);
		    }
		} else if (pName.equals("uri2")) { // Legacy
		    try {
			URI u = new URI( content );
			onto2.setURI( u );
			if ( onto2.getFile() == null ) onto2.setFile( u );
		    } catch (URISyntaxException e) {
//			throw new SAXException("uri1: malformed URI : "+content);
		    }
		} else if (pName.equals( SyntaxElement.ONTOLOGY.name )) {
		} else if (pName.equals( SyntaxElement.LOCATION.name )) {
		    try { curronto.setFile( new URI( content ) );
		    } catch (URISyntaxException e) {
			//throw new SAXException("Malformed URI : "+content );
		    }
		} else if (pName.equals( SyntaxElement.FORMALISM.name )) {
		} else if (pName.equals( SyntaxElement.FORMATT.name )) {
		} else if (pName.equals( SyntaxElement.MAPPING_SOURCE.name ) || pName.equals( SyntaxElement.MAPPING_TARGET.name )) {
		    if ( curronto.getFile() == null && 
			 content != null && !content.equals("") ) {
			try {
			    URI u = new URI( content );
			    curronto.setFile( u );
			    if ( curronto.getURI() == null ) curronto.setURI( u );
			} catch (URISyntaxException e) {
//			    throw new SAXException(pName+": malformed URI : "+content );
			}
		    };
		    curronto = null;
		} else if (pName.equals( SyntaxElement.TYPE.name )) {
		    alignment.setType( content );
		} else if (pName.equals( SyntaxElement.LEVEL.name )) {
		    if ( content.startsWith("2") ) { // Maybe !startsWith("0") would be better
			throw new SAXException("Cannot parse Level 2 alignments (so far)");
		    } else {
			alignment.setLevel( content );
		    }
		} else if (pName.equals( SyntaxElement.XML.name )) {
		    //if ( content.equals("no") )
		    //	{ throw new SAXException("Cannot parse non XML alignments"); }
		} else if (pName.equals( SyntaxElement.ALIGNMENT.name )) {
		    parseLevel = alignLevel; // restore level<
		    alignLevel = -1;
		} else {
		    if ( namespaceURI.equals( Namespace.ALIGNMENT.uri+"#" ) ) namespaceURI = Namespace.ALIGNMENT.uri;
		    if ( parseLevel == 3 ){
			alignment.setExtension( namespaceURI, pName, content );
		    } else if ( parseLevel == 5 ) {
			extensions.setExtension( namespaceURI, pName, content );
		    } else //if ( debugMode > 0 )
			System.err.println("[XMLParser("+parseLevel+")] Unknown element name : "+pName);
		    //throw new SAXException("[XMLParser] Unknown element name : "+pName);
		};
	    } catch ( AlignmentException e ) { throw new SAXException("[XMLParser] Exception raised", e); };
	} else if(namespaceURI.equals( Namespace.SOAP_ENV.prefix ) ) {//"http://schemas.xmlsoap.org/soap/envelope/"))  {
	    // Ignore SOAP namespace
	    if ( !pName.equals("Envelope") && !pName.equals("Body") ) {
		throw new SAXException("[XMLParser] unknown element name: "+pName); };
	} else if(namespaceURI.equals( Namespace.RDF.prefix ) ) {//"http://www.w3.org/1999/02/22-rdf-syntax-ns#"))  {
	    if ( !pName.equals("RDF") ) {
		throw new SAXException("[XMLParser] unknown element name: "+pName); };
	} else if (namespaceURI.equals( Namespace.EDOAL.prefix )) { 
	    throw new SAXException("[XMLParser] EDOAL alignment must have type EDOAL: "+pName);
	} else {
	    if ( parseLevel == 3 && alignLevel != -1 ){
		alignment.setExtension( namespaceURI, pName, content );
	    } else if ( parseLevel == 5 && alignLevel != -1 ) {
		if ( extensions == null ) extensions = new Extensions();
		extensions.setExtension( namespaceURI, pName, content );
	    } else if (  !embedded ) throw new SAXException("[XMLParser] Unknown namespace : "+namespaceURI);
	}
	content = null; // JE2012: set it for the character patch
	parseLevel--;
    } //end endElement
    
    /** Can be used for loading the ontology if it is not available **/
 
}//end class
    
