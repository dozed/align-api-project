/*
 * $Id: SPARQLConstructRendererVisitor.java 1827 2013-03-07 22:44:05Z euzenat $
 *
 * Copyright (C) INRIA, 2012-2013
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

package fr.inrialpes.exmo.align.impl.renderer;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.impl.edoal.Expression;

import java.io.PrintWriter;
import java.net.URI;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;

public class SPARQLConstructRendererVisitor extends GraphPatternRendererVisitor implements AlignmentVisitor{

    Alignment alignment = null;
    Cell cell = null;
    Hashtable<String,String> nslist = null;
    boolean embedded = false;
    boolean split = false;
    String splitdir = "";
    private String content_Corese = "";
    private String GP1;
    private String GP2;
    private List<String> listGP1 = new ArrayList<String>();
    private List<String> listGP2 = new ArrayList<String>();
    
    public SPARQLConstructRendererVisitor(PrintWriter writer) {
	super(writer);
    }   
    
    public void init(Properties p) {
	if ( p.getProperty( "embedded" ) != null 
	     && !p.getProperty( "embedded" ).equals("") ) embedded = true;
	if ( p.getProperty( "blanks" ) != null && !p.getProperty( "blanks" ).equals("") ) 
	    blanks = true;
	if ( p.getProperty( "weakens" ) != null && !p.getProperty( "weakens" ).equals("") ) 
	    weakens = true;
	if ( p.getProperty( "ignoreerrors" ) != null && !p.getProperty( "ignoreerrors" ).equals("") ) 
	    ignoreerrors = true;
	if ( p.getProperty( "corese" ) != null && !p.getProperty( "corese" ).equals("") ) 
	    corese = true;
	split = ( p.getProperty( "split" ) != null && !p.getProperty( "split" ).equals("") );
	if ( p.getProperty( "dir" ) != null && !p.getProperty( "dir" ).equals("") )
	    splitdir = p.getProperty( "dir" )+"/";
	if ( p.getProperty( "indent" ) != null )
	    INDENT = p.getProperty( "indent" );
	if ( p.getProperty( "newline" ) != null )
	    NL = p.getProperty( "newline" );
    }
    
    public void visit( Alignment align ) throws AlignmentException {
    	if ( subsumedInvocableMethod( this, align, Alignment.class ) ) return;
	if ( align instanceof EDOALAlignment ) {
	    alignment = align;
	} else {
	    try {
		alignment = EDOALAlignment.toEDOALAlignment( (BasicAlignment)align );
	    } catch ( AlignmentException alex ) {
		throw new AlignmentException("SPARQLSELECTRenderer: cannot render simple alignment. Need an EDOALAlignment", alex );
	    }
	}
    	content_Corese = "";
    	content_Corese += "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" + NL;
    	content_Corese += "<!DOCTYPE rdf:RDF [" + NL;
    	content_Corese += "<!ENTITY rdf \"http://www.w3.org/1999/02/22-rdf-syntax-ns#\">" + NL;
    	content_Corese += "<!ENTITY rdfs \"http://www.w3.org/2000/01/rdf-schema#\">" + NL;
    	content_Corese += "<!ENTITY rul \"http://ns.inria.fr/edelweiss/2011/rule#\">" + NL;
    	content_Corese += "]>" + NL;
    	content_Corese += "<rdf:RDF xmlns:rdfs=\"&rdfs;\" xmlns:rdf=\"&rdf;\" xmlns = \'&rul;\' >" + NL + NL + NL;
    	for( Cell c : align ){ c.accept( this ); };
    	content_Corese += "</rdf:RDF>" + NL;
    	if ( corese ) {
	    if( split ) {			
		createQueryFile( splitdir, content_Corese );
	    } else {
		writer.println( content_Corese );
	    }    	
    	}
    }	
    
    public void visit( Cell cell ) throws AlignmentException {
    	if ( subsumedInvocableMethod( this, cell, Cell.class ) ) return;
    	// default behaviour
    	this.cell = cell;      	
    	String query="";
    	String query_IgnoreErrors="";
    	String query_weaken="";
    	String tmp="";
    	URI u1 = cell.getObject1AsURI(alignment);
    	URI u2 = cell.getObject2AsURI(alignment);
    	if ( ( u1 != null && u2 != null)
    	     || alignment.getLevel().startsWith("2EDOAL") ){ //expensive test
	    resetVariables("s", "o");
	    ((Expression)(cell.getObject1())).accept( this );
	    GP1 = getGP();    		
	    listGP1 = new ArrayList<String>(getBGP());    		
	    resetVariables("s", "o");	    		
	    ((Expression)(cell.getObject2())).accept( this );
	    GP2 = getGP();
	    listGP2 = new ArrayList<String>(getBGP());
	    
	    if( !GP1.contains("UNION") && !GP1.contains("FILTER") ){
		for ( Enumeration<String> e = prefixList.keys() ; e.hasMoreElements(); ) {
		    String k = e.nextElement();
		    query += "PREFIX "+prefixList.get(k)+":<"+k+">"+NL;
		}
		query += "CONSTRUCT {"+NL;
		query += GP1;		    
		query += "}"+NL;		    
		query += "WHERE {"+NL;
		query += GP2;    	    		
		query += "}"+NL;
		query_weaken = query;
		content_Corese += "<rule>" + NL;
		content_Corese += "<body>" + NL;
		content_Corese += "<![CDATA[" + NL;
		content_Corese += query;
		content_Corese += "]]>" + NL;
		content_Corese += "</body>" + NL;
		content_Corese += "</rule>" + NL + NL;
	    } else {    			
		Iterator<String> list = listGP1.iterator();
		while ( list.hasNext() ) {
		    String str = list.next();
		    if ( !str.contains("UNION") && !str.contains("FILTER") ) {
			tmp += str;
		    }
		}
		if ( !tmp.equals("") ) {
		    for ( Enumeration<String> e = prefixList.keys() ; e.hasMoreElements(); ) {
			String k = e.nextElement();
			query_weaken += "PREFIX "+prefixList.get(k)+":<"+k+">"+NL;
		    }
		    query_weaken += "CONSTRUCT {"+NL;
		    query_weaken += tmp;		    
		    query_weaken += "}"+NL;		    
		    query_weaken += "WHERE {"+NL;
		    query_weaken += GP2;    	    		
		    query_weaken += "}"+NL;
		}
	    }
	    for ( Enumeration<String> e = prefixList.keys() ; e.hasMoreElements(); ) {
		String k = e.nextElement();    		
		query_IgnoreErrors += "PREFIX "+prefixList.get(k)+":<"+k+">"+NL;    		
	    }
	    query_IgnoreErrors += "CONSTRUCT {"+NL;
	    query_IgnoreErrors += GP1;		    
	    query_IgnoreErrors += "}"+NL;		    
	    query_IgnoreErrors += "WHERE {"+NL;
	    query_IgnoreErrors += GP2;    	    		
	    query_IgnoreErrors += "}"+NL;		    
	    if ( corese ) return;
	    if( split ) {
		if ( ignoreerrors ) {
		    createQueryFile( splitdir, query_IgnoreErrors );
		} else if ( weakens ) {
		    createQueryFile( splitdir, query_weaken );
		} else {
		    createQueryFile( splitdir, query );
		}
	    } else if ( ignoreerrors ) {
		writer.println( query_IgnoreErrors );
	    } else if ( weakens ) {
		writer.println( query_weaken );
	    } else {
		writer.println( query );
	    }
	    query="";
	    query_weaken = "";
	    query_IgnoreErrors = "";
	    tmp = "";
	    
	    if( !GP2.contains("UNION") && !GP2.contains("FILTER") ){
		for ( Enumeration<String> e = prefixList.keys() ; e.hasMoreElements(); ) {
		    String k = e.nextElement();
		    query += "PREFIX "+prefixList.get(k)+":<"+k+">"+NL;
		}
		query += "CONSTRUCT {"+NL;
		query += GP2;		    
		query += "}"+NL;		    
		query += "WHERE {"+NL;
		query += GP1;    	    		
		query += "}"+NL;
		query_weaken = query;
		content_Corese += "<rule>" + NL;
		content_Corese += "<body>" + NL;
		content_Corese += "<![CDATA[" + NL;
		content_Corese += query;
		content_Corese += "]]>" + NL;
		content_Corese += "</body>" + NL;
		content_Corese += "</rule>" + NL + NL;
	    } else {    			
		Iterator<String> list = listGP2.iterator();
		while ( list.hasNext() ) {
		    String str = list.next();
		    if ( !str.contains("UNION") && !str.contains("FILTER") ) {
			tmp += str;
		    }
		}
		if ( !tmp.equals("") ) {
		    for ( Enumeration<String> e = prefixList.keys() ; e.hasMoreElements(); ) {
			String k = e.nextElement();
			query_weaken += "PREFIX "+prefixList.get(k)+":<"+k+">"+NL;
		    }
		    query_weaken += "CONSTRUCT {"+NL;
		    query_weaken += tmp;		    
		    query_weaken += "}"+NL;		    
		    query_weaken += "WHERE {"+NL;
		    query_weaken += GP1;    	    		
		    query_weaken += "}"+NL;
		}
	    }
	    for ( Enumeration<String> e = prefixList.keys() ; e.hasMoreElements(); ) {
		String k = e.nextElement();    		
		query_IgnoreErrors += "PREFIX "+prefixList.get(k)+":<"+k+">"+NL;    		
	    }
	    query_IgnoreErrors += "CONSTRUCT {"+NL;
	    query_IgnoreErrors += GP2;		    
	    query_IgnoreErrors += "}"+NL;		    
	    query_IgnoreErrors += "WHERE {"+NL;
	    query_IgnoreErrors += GP1;    	    		
	    query_IgnoreErrors += "}"+NL;		    
	    if ( corese ) return;
	    if( split ) {
		if ( ignoreerrors ) {
		    createQueryFile( splitdir, query_IgnoreErrors );
		} else if ( weakens ) {
		    createQueryFile( splitdir, query_weaken );
		} else {
		    createQueryFile( splitdir, query );
		}
	    } else if ( ignoreerrors ) {
		writer.println( query_IgnoreErrors );
	    } else if ( weakens ) {
		writer.println( query_weaken );
	    } else { writer.println( query ); }			   	
    	}
    }
    
    public void visit( Relation rel ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, rel, Relation.class ) ) return;
	// default behaviour
	// rel.write( writer );
    }
}
