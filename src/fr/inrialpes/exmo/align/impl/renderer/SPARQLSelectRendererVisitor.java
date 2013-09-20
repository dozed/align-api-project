/*
 * $Id: SPARQLSelectRendererVisitor.java 1827 2013-03-07 22:44:05Z euzenat $
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
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;

public class SPARQLSelectRendererVisitor extends GraphPatternRendererVisitor implements AlignmentVisitor{

    Alignment alignment = null;
    Cell cell = null;
    Hashtable<String,String> nslist = null;
    boolean embedded = false;
    boolean split = false;
    String splitdir = "";
    private String GP1;
    private String GP2;
    
    public SPARQLSelectRendererVisitor(PrintWriter writer) {
	super(writer);
    }   

    public void init(Properties p) {
	if ( p.getProperty( "embedded" ) != null && !p.getProperty( "embedded" ).equals("") ) 
	    embedded = true;
	if ( p.getProperty( "blanks" ) != null && !p.getProperty( "blanks" ).equals("") ) 
	    blanks = true;
	if ( p.getProperty( "weakens" ) != null && !p.getProperty( "weakens" ).equals("") ) 
	    weakens = true;
	if ( p.getProperty( "ignoreerrors" ) != null && !p.getProperty( "ignoreerrors" ).equals("") ) 
	    ignoreerrors = true;
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
	for( Cell c : align ){ c.accept( this ); };    	
    }	

    public void visit( Cell cell ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, cell, Cell.class ) ) return;
    	String query = "";
    	this.cell = cell;    	
    	
    	URI u1 = cell.getObject1AsURI(alignment);
    	URI u2 = cell.getObject2AsURI(alignment);
    	if ( ( u1 != null && u2 != null) || alignment.getLevel().startsWith("2EDOAL") ) {
	    resetVariables("s", "o");
	    ((Expression)(cell.getObject1())).accept( this );
	    GP1 = getGP();
	    resetVariables("s", "o");
	    ((Expression)(cell.getObject2())).accept( this );
	    GP2 = getGP();
	    for ( Enumeration<String> e = prefixList.keys() ; e.hasMoreElements(); ) {
		String k = e.nextElement();
		query += "PREFIX "+prefixList.get(k)+":<"+k+">"+NL;
	    }
	    query += "SELECT ?s WHERE {"+NL;
	    query += GP1;
	    query += "}"+NL;	    		
	    if ( split ) {
		createQueryFile( splitdir, query );
	    } else {
		writer.println(query);
	    }	    		
	    query="";
	    for ( Enumeration<String> e = prefixList.keys() ; e.hasMoreElements(); ) {
		String k = e.nextElement();
		query += "PREFIX "+prefixList.get(k)+":<"+k+">"+NL;
	    }
	    query += "SELECT ?s WHERE {"+NL;
	    query += GP2;
	    query += "}"+NL;
	    if ( split ) {
		createQueryFile( splitdir, query );
	    } else {
	    	writer.println(query);
	    }
    	}    
    }
    
    public void visit( Relation rel ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, rel, Relation.class ) ) return;
	// default behaviour
	// rel.write( writer );
    }
}
