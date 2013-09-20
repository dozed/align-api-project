/*
 * $Id: XMLMetadataRendererVisitor.java 1699 2012-03-09 23:07:32Z euzenat $
 *
 * Copyright (C) INRIA, 2007, 2009-2010, 2012
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

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Properties;
import java.io.PrintWriter;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.BasicAlignment;

/**
 * Renders an alignment in its RDF format
 *
 * @author Jérôme Euzenat
 * @version $Id: XMLMetadataRendererVisitor.java 1699 2012-03-09 23:07:32Z euzenat $ 
 */

public class XMLMetadataRendererVisitor extends GenericReflectiveVisitor implements AlignmentVisitor {
    
    PrintWriter writer = null;
    Alignment alignment = null;
    boolean embedded = false; // if the output is XML embeded in a structure
    Hashtable<String,String> nslist = null;
    boolean newstyle = false;

    public XMLMetadataRendererVisitor( PrintWriter writer ){
	this.writer = writer;
    }

    public void init( Properties p ) {
	if ( p.getProperty( "embedded" ) != null 
	     && !p.getProperty( "embedded" ).equals("") ) embedded = true;
    };

    public void visit( Alignment align ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, align, Alignment.class ) ) return;
	// default behaviour
	String extensionString = "";
	alignment = align;
	nslist = new Hashtable<String,String>();
	nslist.put(Namespace.ALIGNMENT.uri,"align");
	nslist.put("http://www.w3.org/1999/02/22-rdf-syntax-ns#","rdf");
	nslist.put("http://www.w3.org/2001/XMLSchema#","xsd");
	//nslist.put("http://www.omwg.org/TR/d7/ontology/alignment","omwg");
	// Get the keys of the parameter
	int gen = 0;
	for ( String[] ext : align.getExtensions() ){
	    String prefix = ext[0];
	    String name = ext[1];
	    String tag = nslist.get(prefix);
	    if ( tag == null ) {
		tag = "ns"+gen++;
		nslist.put( prefix, tag );
	    }
	    if ( tag.equals("align") ) { tag = name; }
	    else { tag += ":"+name; }
	    extensionString += "  <"+tag+">"+ext[2]+"</"+tag+">\n";
	}
	if ( embedded == false ) {
	    writer.print("<?xml version='1.0' encoding='utf-8");
	    writer.print("' standalone='no'?>\n");
	}
	writer.print("<rdf:RDF xmlns='"+Namespace.ALIGNMENT.uri+"'");
	for ( Enumeration e = nslist.keys() ; e.hasMoreElements(); ) {
	    String k = (String)e.nextElement();
	    writer.print("\n         xmlns:"+nslist.get(k)+"='"+k+"'");
	}
	if ( align instanceof BasicAlignment ) {
	    for ( String label : ((BasicAlignment)align).getXNamespaces().stringPropertyNames() ) {
		if ( !label.equals("rdf") && !label.equals("xsd")
		     && !label.equals("<default>") )
		    writer.print("\n         xmlns:"+label+"='"+((BasicAlignment)align).getXNamespace( label )+"'");
	    }
	}
	writer.print(">\n");
	writer.print("<Alignment");
	String idext = align.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID );
	if ( idext != null ) {
	    writer.print(" rdf:about=\""+idext+"\"");
	}
	writer.print(">\n  <xml>yes</xml>\n");
	writer.print("  <level>");
	writer.print( align.getLevel() );
	writer.print("</level>\n  <type>");
	writer.print( align.getType() );
	writer.print("</type>\n");
	// Get the keys of the parameter
	if ( align.getFile1() != null )
	    writer.print("  <onto1>"+align.getFile1().toString()+"</onto1>\n");
	if ( align.getFile2() != null )
	    writer.print("  <onto2>"+align.getFile2().toString()+"</onto2>\n");
	writer.print("  <uri1>");
	writer.print( align.getOntology1URI().toString() );
	writer.print("</uri1>\n");
	writer.print("  <uri2>");
	writer.print( align.getOntology2URI().toString() );
	writer.print("</uri2>\n");
	writer.print(extensionString);
	if ( newstyle ){
	    writer.print("  <onto1>\n    <Ontology");
	    if ( align.getOntology1URI() != null ) {
		writer.print(" rdf:about=\""+align.getOntology1URI()+"\"");
	    }
	    writer.print(">\n      <location>"+align.getFile1()+"</location>");
	    if ( align instanceof BasicAlignment && ((BasicAlignment)align).getOntologyObject1().getFormalism() != null ) {
		writer.print("\n      <formalism>\n        <Formalism align:name=\""+((BasicAlignment)align).getOntologyObject1().getFormalism()+"\" align:uri=\""+((BasicAlignment)align).getOntologyObject1().getFormURI()+"\"/>\n      </formalism>");
	    }
	    writer.print("\n    </Ontology>\n  </onto1>\n");
	    writer.print("  <onto2>\n    <Ontology");
	    if ( align.getOntology2URI() != null ) {
		writer.print(" rdf:about=\""+align.getOntology2URI()+"\"");
	    }
	    writer.print(">\n      <location>"+align.getFile2()+"</location>");
	    if ( align instanceof BasicAlignment && ((BasicAlignment)align).getOntologyObject2().getFormalism() != null ) {
		writer.print("\n      <formalism>\n        <Formalism align:name=\""+((BasicAlignment)align).getOntologyObject2().getFormalism()+"\" align:uri=\""+((BasicAlignment)align).getOntologyObject2().getFormURI()+"\"/>\n      </formalism>");
	    }
	    writer.print("\n    </Ontology>\n  </onto2>\n");
	}
	writer.print("</Alignment>\n");
	writer.print("</rdf:RDF>\n");
    }

    public void visit( Cell c ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, c, Cell.class ) ) return;
	// default behaviour
    };

    public void visit( Relation r ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, r, Relation.class ) ) return;
	// default behaviour
    };
    
}
