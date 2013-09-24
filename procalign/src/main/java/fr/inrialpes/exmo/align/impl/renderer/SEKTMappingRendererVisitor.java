/*
 * $Id: SEKTMappingRendererVisitor.java 1827 2013-03-07 22:44:05Z euzenat $
 *
 * Copyright (C) INRIA, 2003-2005, 2007-2010, 2012-2013
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

import java.util.Properties;
import java.util.Random;
import java.io.PrintWriter;

import fr.inrialpes.exmo.align.impl.AlignmentTransformer;
import fr.inrialpes.exmo.align.impl.rel.EquivRelation;
import fr.inrialpes.exmo.align.impl.rel.IncompatRelation;
import fr.inrialpes.exmo.align.impl.rel.SubsumeRelation;
import fr.inrialpes.exmo.align.impl.rel.SubsumedRelation;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;

import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

/**
 * Renders an alignment as a new ontology merging these.
 *
 * @author Jérôme Euzenat
 * @version $Id: SEKTMappingRendererVisitor.java 1827 2013-03-07 22:44:05Z euzenat $ 
 */

public class SEKTMappingRendererVisitor extends GenericReflectiveVisitor implements AlignmentVisitor {
    PrintWriter writer = null;
    Alignment alignment = null;
    LoadedOntology onto1 = null;
    LoadedOntology onto2 = null;
    Cell cell = null;
    // I hate using random generator for generating symbols (address would be better)
    Random generator = null;

    public SEKTMappingRendererVisitor( PrintWriter writer ){
	this.writer = writer;
	generator = new Random();
    }

    public void init( Properties p ) {};

    public void visit( Alignment align ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, align, Alignment.class ) ) return;
	// default behaviour
	if ( align instanceof ObjectAlignment ) {
	    alignment = align;
	} else {
	    try {
		alignment = AlignmentTransformer.toObjectAlignment((URIAlignment) align);
	    } catch ( AlignmentException alex ) {
		throw new AlignmentException("SEKTMappingRenderer: cannot render simple alignment. Need an ObjectAlignment", alex );
	    }
	}
	onto1 = (LoadedOntology)((ObjectAlignment)alignment).getOntologyObject1();
	onto2 = (LoadedOntology)((ObjectAlignment)alignment).getOntologyObject2();
	writer.print("MappingDocument(<\""+"\">\n");
	writer.print("  source(<\""+onto1.getURI()+"\">)\n");
	writer.print("  target(<\""+onto2.getURI()+"\">)\n");

	for( Cell c : align ){
	    c.accept( this );
	} //end for
	writer.print(")\n");
    }
    public void visit( Cell cell ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, cell, Cell.class ) ) return;
	// default behaviour
	this.cell = cell;
	String id = String.format( "s%06d", generator.nextInt(100000) );
	Object ob1 = cell.getObject1();
	Object ob2 = cell.getObject2();
	try {
	    if ( onto1.isClass( ob1 ) ) {
		writer.print("  classMapping( <\"#"+id+"\">\n");
		cell.getRelation().accept( this );
		writer.print("    <\""+onto1.getEntityURI( ob1 )+"\">\n");
		writer.print("    <\""+onto2.getEntityURI( ob2 )+"\">\n");
		writer.print("  )\n");
	    } else if ( onto1.isDataProperty( ob1 ) ) {
		writer.print("  relationMapping( <\"#"+id+"\">\n");
		cell.getRelation().accept( this );
		writer.print("    <\""+onto1.getEntityURI( ob1 )+"\">\n");
		writer.print("    <\""+onto2.getEntityURI( ob2 )+"\">\n");
		writer.print("  )\n");
	    } else if ( onto1.isObjectProperty( ob1 ) ) {
		writer.print("  attributeMapping( <\"#"+id+"\">\n");
		cell.getRelation().accept( this );
		writer.print("    <\""+onto1.getEntityURI( ob1 )+"\">\n");
		writer.print("    <\""+onto2.getEntityURI( ob2 )+"\">\n");
		writer.print("  )\n");
	    } else if ( onto1.isIndividual( ob1 ) ) {
		writer.print("  instanceMapping( <\"#"+id+"\">\n");
		cell.getRelation().accept( this );
		writer.print("    <\""+onto1.getEntityURI( ob1 )+"\">\n");
		writer.print("    <\""+onto2.getEntityURI( ob2 )+"\">\n");
		writer.print("  )\n");
	    }
	    writer.print("\n");
	} catch ( OntowrapException owex ) {
	    throw new AlignmentException( "Cannot find entity URI", owex );
	}
    }

    public void visit( EquivRelation rel ) throws AlignmentException {
	writer.print("    bidirectional\n");
    }
    public void visit( SubsumeRelation rel ) throws AlignmentException {
	writer.print("    unidirectional\n");
    }
    public void visit( SubsumedRelation rel ) throws AlignmentException {
	writer.print("    unidirectional\n");
    }
    public void visit( IncompatRelation rel ) throws AlignmentException {
	writer.print("    unidirectional\n");
    }
    public void visit( Relation rel ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, rel, Relation.class ) ) return;
	// default behaviour
	throw new AlignmentException( "Cannot render generic Relation" );
    }

}
