/*
 * $Id: AlignmentParser.java 1727 2012-05-13 08:53:41Z euzenat $
 *
 * Copyright (C) INRIA, 2003-2005, 2007-2010, 2012
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

//Imported JAVA classes
import java.io.Reader;
import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Hashtable;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.impl.URIAlignment;

/**
 * This class allows the creation of a parser for an Alignment file.
 * The class is called by:
 * AlignmentParser parser = new AlignmentParser( debugLevel );
 * Alignment alignment = parser.parse( input );
 * input can be a URI as a String, an InputStream
 * This new version (January 2004) parses the alignment description in
 * RDF/XML/OWL format and RDF format. It understands the EDOAL format.
 *
 */

public class AlignmentParser {

    /**
     * level of debug/warning information
     */
    protected int debugMode = 0;
    
    /**
     * a URI to a process
     */
    protected String uri = null;
    
    /**
     * the alignment that is parsed
     * We always create a URIAlignment (we could also use a BasicAlignment).
     * This is a pitty but the idea of creating a particular alignment
     * is not in accordance with using an interface.
     */
    protected Alignment alignment = null;

    /**
     * The parsing level, if equal to 3 we are in the Alignment
     * if equal to 5 we are in a cell
     * and can find metadata
     */
    protected int parseLevel = 0;

    /**
     * The parsing level, if equal to 3 we are in the Alignment
     * if equal to 5 we are in a cell
     * and can find metadata
     */
    protected boolean embedded = false;

    /**
     * The level at which we found the Alignment tag.
     * It is -1 outside the alignment.
     */
    protected int alignLevel = -1;

    /** 
     * Creates a Parser.
     * @param debugMode The value of the debug mode
     */
    public AlignmentParser( int debugMode ) {
	this.debugMode = debugMode;
    }

    public void setEmbedded( boolean b ){
	embedded = b;
    }

    /** 
     * Parses the document corresponding to the URI given in parameter
     * If the current process has links (import or include) to others documents then they are 
     * parsed.
     * @param uri URI of the document to parse
     * @param loaded (cached ontologies)
     * @deprecated use parse( URI ) instead
     */
    @Deprecated
    public Alignment parse( String uri, Hashtable loaded ) throws AlignmentException {
	return parse( uri );
    }

    /** 
     * Parses the document given in parameter
     * If the current process has links (import or include) to others documents then they are 
     * parsed.
     * @param o
     *     A URI, InputStream, String or Reader
     */
    private Alignment callParser( Object o ) throws AlignmentException {
	try { 
	    XMLParser parser = new XMLParser( debugMode );
	    if ( embedded ) parser.setEmbedded( embedded );
	    //alignment = parser.parse( o );
	    alignment = callParser( parser, o );
	} catch ( Exception e ) {
	    if ( debugMode > 0 ) {
		System.err.println("XMLParser failed to parse alignment (INFO)");
		e.printStackTrace();
	    }
	    try {
		if ( !embedded ) {
		    RDFParser rparser = new RDFParser( debugMode );
		    alignment = callParser( rparser, o );
		} else {
		    throw new AlignmentException( "Cannot parse "+o+" (use debug>0 for more info)", e );
		}
	    } catch ( Exception ex ) {
		// JE: should contain both ex and e
		throw new AlignmentException( "Cannot parse "+o, ex );
	    }
	}
	return alignment;
    }

    /**
     * This dispatch is ridiculous, but that's life
     */
    private Alignment callParser( XMLParser p, Object o ) throws AlignmentException {
	if ( o instanceof URI ) return p.parse( ((URI)o).toString() );
	if ( o instanceof String ) return p.parse( new ByteArrayInputStream( ((String)o).getBytes() ) );
	if ( o instanceof Reader ) return p.parse((Reader)o);
	if ( o instanceof InputStream ) return p.parse((InputStream)o);
	throw new AlignmentException( "AlignmentParser: XMLParser cannot parse :"+o );
    }

    private Alignment callParser( RDFParser p, Object o ) throws AlignmentException {
	if ( o instanceof URI ) return p.parse( ((URI)o).toString() );
	if ( o instanceof String ) return p.parse( new ByteArrayInputStream( ((String)o).getBytes() ) );
	if ( o instanceof Reader ) return p.parse((Reader)o);
	if ( o instanceof InputStream ) return p.parse((InputStream)o);
	throw new AlignmentException( "AlignmentParser: RDFParser cannot parse :"+o );
    }

    /** 
     * Parses the content of a string
     * @param s String the string to parse
     */
    public Alignment parseString( String s ) throws AlignmentException {
	// The problem here is that InputStream are consumed by parsers
	// So they must be opened again! Like Readers...
	callParser( s );
	return alignment;
    }

    /** 
     * Parses a the content of a reader
     * @param r the reader to parse
     */
    public Alignment parse( Reader r ) throws AlignmentException {
	callParser( r );
	return alignment;
    }

    /** 
     * Parses a URI expressed as a String
     * @param uri the URI as a String
     * This is only here for compatibility purposes
     */
    public Alignment parse( String uri ) throws AlignmentException {
	this.uri = uri; // should be obsoloted
	try {
	    callParser( new URI( uri ) );
	} catch (URISyntaxException urisex) {
	    throw new AlignmentException( "Invalid URI : "+uri, urisex );
	}
	return alignment;
    }

    /** 
     * Parses a URI
     * @param uri the URI
     */
    public Alignment parse( URI uri ) throws AlignmentException {
	this.uri = uri.toString();
	callParser( uri );
	return alignment;
    }

    /** 
     * Parses an inputStream
     * @param s the Stream to parse
     */
    public Alignment parse( InputStream s ) throws AlignmentException {
	callParser( s );
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
    
 
}
    
