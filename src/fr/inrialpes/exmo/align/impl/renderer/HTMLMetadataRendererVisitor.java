/*
 * $Id: HTMLMetadataRendererVisitor.java 1832 2013-03-14 20:14:01Z euzenat $
 *
 * Copyright (C) INRIA, 2006-2010, 2012-2013
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
import java.net.URI;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;

/**
 * Renders an alignment in HTML
 *
 * TODO:
 * - add CSS categories
 * - add resource chooser
 *
 * @author Jérôme Euzenat
 * @version $Id: HTMLMetadataRendererVisitor.java 1832 2013-03-14 20:14:01Z euzenat $ 
 */

public class HTMLMetadataRendererVisitor extends GenericReflectiveVisitor implements AlignmentVisitor {
    
    PrintWriter writer = null;
    Alignment alignment = null;
    Hashtable<String,String> nslist = null;
    boolean embedded = false; // if the output is XML embeded in a structure

    public HTMLMetadataRendererVisitor( PrintWriter writer ){
	this.writer = writer;
    }

    public void init( Properties p ) {
	if ( p.getProperty( "embedded" ) != null 
	     && !p.getProperty( "embedded" ).equals("") ) embedded = true;
    };

    public void visit( Alignment align ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, align, Alignment.class ) ) return;
	// default behaviour
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
	    //extensionString += "  <"+tag+">"+((String[])ext)[2]+"</"+tag+">\n";
	}
	if ( embedded == false ) {
	    writer.print("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>\n");
	    writer.print("<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML+RDFa 1.0//EN\" \"http://www.w3.org/MarkUp/DTD/xhtml-rdfa-1.dtd\">\n");
	}
	writer.print("<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en\"");
	for ( Enumeration e = nslist.keys() ; e.hasMoreElements(); ) {
	    String k = (String)e.nextElement();
	    writer.print("\n       xmlns:"+nslist.get(k)+"='"+k+"'");
	}
	writer.print(">\n<head><title>Alignment</title></head>\n<body>\n");
	String alid = align.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID );
	String pid = align.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
	if ( alid == null ) alid = "Anonymous alignment";
	if ( pid == null ) {
	    writer.print("<h1>"+alid+"</h1>\n");
	} else {
	    writer.print("<h1>"+alid+" ("+pid+")</h1>\n");
	}
	writer.print("<div typeof=\"align:Alignment\">\n");
	writer.print("<table border=\"0\">\n");
	writer.print("<tr><td>onto1</td><td><div rel=\"align:onto1\"><div typeof=\"align:Ontology\" about=\""+align.getOntology1URI()+"\">");
	writer.print("<table>\n<tr><td>uri: </td><td>"+align.getOntology1URI()+"</td></tr>\n");
	if ( align.getFile1() != null )
	    writer.print("<tr><td><span property=\"align:location\" content=\""+align.getFile1()+"\"/>file:</td><td><a href=\""+align.getFile1()+"\">"+align.getFile1()+"</a></td></tr>\n" );
	if ( align instanceof BasicAlignment && ((BasicAlignment)align).getOntologyObject1().getFormalism() != null ) {
	    writer.print("<tr><td>type:</td><td><span rel=\"align:formalism\"><span typeof=\"align:Formalism\"><span property=\"align:name\">"+((BasicAlignment)align).getOntologyObject1().getFormalism()+"</span><span property=\"align:uri\" content=\""+((BasicAlignment)align).getOntologyObject1().getFormURI()+"\"/></span></span></td></tr>");
	}
	writer.print("</table>\n</div></div></td></tr>\n");
	writer.print("<tr><td>onto2</td><td><div rel=\"align:onto2\"><div typeof=\"align:Ontology\" about=\""+align.getOntology2URI()+"\">");
	writer.print("<table>\n<tr><td>uri: </td><td>"+align.getOntology2URI()+"</td></tr>\n");
	if ( align.getFile2() != null )
	    writer.print("<tr><td><span property=\"align:location\" content=\""+align.getFile2()+"\"/>file:</td><td><a href=\""+align.getFile2()+"\">"+align.getFile2()+"</a></td></tr>\n" );
	if ( align instanceof BasicAlignment && ((BasicAlignment)align).getOntologyObject2().getFormalism() != null ) {
	    writer.print("<tr><td>type:</td><td><span rel=\"align:formalism\"><span typeof=\"align:Formalism\"><span property=\"align:name\">"+((BasicAlignment)align).getOntologyObject2().getFormalism()+"</span><span property=\"align:uri\" content=\""+((BasicAlignment)align).getOntologyObject2().getFormURI()+"\"/></span></span></td></tr>");
	}
	writer.print("</table>\n</div></div></td></tr>\n");
	writer.print("<tr><td>level</td><td property=\"align:level\">"+align.getLevel()+"</td></tr>\n" );
	writer.print("<tr><td>type</td><td property=\"align:type\">"+align.getType()+"</td></tr>\n" );
	// RDFa: Get the keys of the parameter (to test)
	for ( String[] ext : align.getExtensions() ){
	    writer.print("<tr><td>"+ext[0]+" : "+ext[1]+"</td><td property=\""+nslist.get(ext[0])+":"+ext[1]+"\">"+ext[2]+"</td></tr>\n");
	}
	writer.print("</table>\n");
	writer.print("</div>\n");
	writer.print("</body>\n</html>\n");
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
