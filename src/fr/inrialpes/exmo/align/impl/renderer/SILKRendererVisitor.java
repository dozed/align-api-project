/*
 * $Id: SILKRendererVisitor.java 1778 2012-09-03 07:23:21Z dnnthinh $
 *
 * Copyright (C) INRIA, 2012
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

import java.io.PrintWriter;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.ontowrap.Ontology;

import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.edoal.Expression;

import java.net.URI;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.util.Random;

public class SILKRendererVisitor extends GraphPatternRendererVisitor implements AlignmentVisitor{

    Alignment alignment = null;
    Cell cell = null;
    Hashtable<String,String> nslist = null;
    private boolean embedded = false;
    private String threshold = "";
    private String limit = "";
    private Random rand;
    
    public SILKRendererVisitor(PrintWriter writer) {
	super(writer);
    }   

    public void init( Properties p ) {
	if ( p.getProperty( "embedded" ) != null && !p.getProperty( "embedded" ).equals("") ) 
	    embedded = true;
	if ( p.getProperty( "blanks" ) != null && !p.getProperty( "blanks" ).equals("") ) 
	    blanks = true;
	if ( p.getProperty( "weakens" ) != null && !p.getProperty( "weakens" ).equals("") ) 
	    weakens = true;
	if ( p.getProperty( "ignoreerrors" ) != null && !p.getProperty( "ignoreerrors" ).equals("") ) 
	    ignoreerrors = true;
	if ( p.getProperty( "silkthreshold" ) != null && !p.getProperty( "silkthreshold" ).equals("") ) {
	    threshold = " threshold=\""+p.getProperty( "silkthreshold" )+"\"";
	}
	if ( p.getProperty( "silklimit" ) != null && !p.getProperty( "silklimit" ).equals("") ) {
	    limit = " limit=\""+p.getProperty( "silklimit" )+"\"";
	}
	if ( p.getProperty( "indent" ) != null )
	    INDENT = p.getProperty( "indent" );
	if ( p.getProperty( "newline" ) != null )
	    NL = p.getProperty( "newline" );
	rand = new Random( System.currentTimeMillis() );
    }

    public void visit(Alignment align) throws AlignmentException {

    	if ( subsumedInvocableMethod( this, align, Alignment.class ) ) return;
    	// default behaviour
    	String extensionString = "";
    	alignment = align;
    	nslist = new Hashtable<String,String>();
	nslist.put( Namespace.RDF.prefix , Namespace.RDF.shortCut );
	nslist.put( Namespace.XSD.prefix , Namespace.XSD.shortCut );
    	// Get the keys of the parameter
    	int gen = 0;
    	for ( String[] ext : align.getExtensions() ) {
    	    String prefix = ext[0];
    	    String name = ext[1];
    	    String tag = nslist.get(prefix);
    	    //if ( tag.equals("align") ) { tag = name; }
    	    if ( prefix.equals( Namespace.ALIGNMENT.uri ) ) { tag = name; }
    	    else {
    		if ( tag == null ) {
    		    tag = "ns"+gen++;
    		    nslist.put( prefix, tag );
    		}
    		tag += ":"+name;
    	    }
    	    extensionString += INDENT+"<"+tag+">"+ext[2]+"</"+tag+">"+NL;
    	}
    	if ( embedded == false ) {
    	    writer.print("<?xml version='1.0' encoding='utf-8");
    	    writer.print("' standalone='no'?>"+NL+NL);
    	}
    	indentedOutputln("<SILK>");
	increaseIndent();
    	indentedOutputln("<Prefixes>");
	increaseIndent();
    	indentedOutputln("<Prefix id=\"rdf\" namespace=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\" />");
    	indentedOutputln("<Prefix id=\"rdfs\" namespace=\"http://www.w3.org/2000/01/rdf-schema#\" />");
    	indentedOutputln("<Prefix id=\"owl\" namespace=\"http://www.w3.org/2002/07/owl#\" />");
    	for ( Enumeration e = prefixList.keys() ; e.hasMoreElements(); ) {
		    String k = (String)e.nextElement();
		    indentedOutputln("<Prefix id=\""+k+" namespace=\""+prefixList.get(k)+"\" />");
	}
	decreaseIndent();
    	indentedOutputln("</Prefixes>"+NL);
    	indentedOutputln("<DataSources>");
	increaseIndent();
	indentedOutputln("<!-- These may have to be edited to proper data sources -->");
	if ( align instanceof BasicAlignment ) {
	    printOntology( ((BasicAlignment)align).getOntologyObject1(), "source" );
	} else {
	    printBasicOntology( align.getOntology1URI(), align.getFile1(), "source" );
	}
	if ( align instanceof BasicAlignment ) {
	    printOntology( ((BasicAlignment)align).getOntologyObject2(), "target" );
	} else {
	    printBasicOntology( align.getOntology2URI(), align.getFile2(), "target" );
	}
	decreaseIndent();
    	indentedOutputln("</DataSources>"+NL);
    	indentedOutputln("<Interlinks>");
	increaseIndent();
    	for( Cell c : align ){ c.accept( this ); };
    	decreaseIndent();
    	indentedOutputln("</Interlinks>");
    	decreaseIndent();
    	writer.print("</SILK>"+NL);
    }

    private void printBasicOntology ( URI u, URI f, String function ) {
	indentedOutput("<DataSource id=\""+function+"\" type=\"file\">"+NL);
	increaseIndent();
	if ( f != null ) {
	    indentedOutputln("<Param name=\"file\" value=\""+f+"\" />");
	} else {
	    indentedOutputln("<Param name=\"file\" value=\""+u+"\" />");
	}
	indentedOutputln("<Param name=\"format\" value=\"RDF/XML\" />");
	decreaseIndent();
    	indentedOutputln("</DataSource>");
    }

    public void printOntology( Ontology onto, String function ) {
	URI u = onto.getURI();
	URI f = onto.getFile();
	printBasicOntology( u, f, function );
    }

    public void visit( Cell cell ) throws AlignmentException {
    	if ( subsumedInvocableMethod( this, cell, Cell.class ) ) return;
    	// default behaviour
    	this.cell = cell;      	

	// JE: cannot use Cell id because it is an URI and not an id
	String id = "RandomId"+Math.abs( rand.nextInt(100000) );
    	
    	URI u1 = cell.getObject1AsURI(alignment);
    	URI u2 = cell.getObject2AsURI(alignment);
    	if ( ( u1 != null && u2 != null)
    	     || alignment.getLevel().startsWith("2EDOAL") ){ //expensive test    		   		
		
		indentedOutputln("<Interlink id=\""+id+"\">");
		increaseIndent();
		indentedOutputln("<LinkType>owl:sameAs</LinkType>");
		indentedOutputln("<SourceDataSet dataSource=\"source\"" + " var=\"s\">");
		increaseIndent();
		indentedOutputln("<RestrictTo>");
		increaseIndent();
		resetVariables("s", "o");
		((Expression)(cell.getObject1())).accept( this );
		indentedOutput(getGP());
		decreaseIndent();
		indentedOutputln("</RestrictTo>");
		decreaseIndent();
		indentedOutputln("</SourceDataSet>");

		indentedOutputln("<TargetDataSet dataSource=\"target\"" + " var=\"x\">");
		increaseIndent();
		indentedOutputln("<RestrictTo>");
		increaseIndent();
		resetVariables("x", "y");	    		
		((Expression)(cell.getObject2())).accept( this );
		indentedOutput(getGP());
		decreaseIndent();
		indentedOutputln("</RestrictTo>");
		decreaseIndent();
		indentedOutputln("</TargetDataSet>");

		// This should certainly be specified in the EDOAL
		indentedOutputln("<LinkageRule>");
		increaseIndent();
		indentedOutputln("<Compare metric=\"levenshtein\" threshold=\".5\">");
		increaseIndent();
		indentedOutputln("<TransformInput function=\"stripUriPrefix\">");
		increaseIndent();
		indentedOutputln("<Input path=\"?s\" />");
		decreaseIndent();
		indentedOutputln("</TransformInput>");
		indentedOutputln("<TransformInput function=\"stripUriPrefix\">");
		increaseIndent();
		indentedOutputln("<Input path=\"?x\" />");
		decreaseIndent();
		indentedOutputln("</TransformInput>");
		decreaseIndent();
		indentedOutputln("</Compare>");
		decreaseIndent();
		indentedOutputln("</LinkageRule>");
		indentedOutputln("<Filter"+threshold+limit+" />");
		indentedOutputln("<Outputs>");	    		
		increaseIndent();
		indentedOutputln("<Output minConfidence=\".7\" type=\"file\">");
		increaseIndent();
		indentedOutputln("<Param name=\"file\" value=\""+id+"-accepted.nt\"/>");
		indentedOutputln("<Param name=\"format\" value=\"ntriples\"/>");
		decreaseIndent();
		indentedOutputln("</Output>");
		indentedOutputln("<Output maxConfidence=\".7\" minConfidence=\".2\" type=\"file\">");
		increaseIndent();
		indentedOutputln("<Param name=\"file\" value=\""+id+"-tocheck.nt\"/>");
		indentedOutputln("<Param name=\"format\" value=\"ntriples\"/>");
		decreaseIndent();
		indentedOutputln("</Output>");
		decreaseIndent();
		indentedOutputln("</Outputs>");
		decreaseIndent();
		indentedOutputln("</Interlink>"+NL);		    		
    	}
    }

    public void visit( Relation rel ) throws AlignmentException {
		if ( subsumedInvocableMethod( this, rel, Relation.class ) ) return;
		// default behaviour
		// rel.write( writer );
    }
	
}
