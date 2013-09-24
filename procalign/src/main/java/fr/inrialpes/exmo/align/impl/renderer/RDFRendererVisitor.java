/*
 * $Id: RDFRendererVisitor.java 1806 2013-02-08 14:27:25Z euzenat $
 *
 * Copyright (C) INRIA, 2003-2010, 2012
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
import java.util.Set;
import java.util.Properties;
import java.io.PrintWriter;
import java.net.URI;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.BasicAlignment;

import fr.inrialpes.exmo.ontowrap.Ontology;

import fr.inrialpes.exmo.align.parser.SyntaxElement;
import fr.inrialpes.exmo.align.parser.SyntaxElement.Constructor;

import fr.inrialpes.exmo.align.impl.edoal.PathExpression;
import fr.inrialpes.exmo.align.impl.edoal.Expression;
import fr.inrialpes.exmo.align.impl.edoal.ClassExpression;
import fr.inrialpes.exmo.align.impl.edoal.ClassId;
import fr.inrialpes.exmo.align.impl.edoal.ClassConstruction;
import fr.inrialpes.exmo.align.impl.edoal.ClassTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassOccurenceRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyId;
import fr.inrialpes.exmo.align.impl.edoal.PropertyConstruction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationId;
import fr.inrialpes.exmo.align.impl.edoal.RelationConstruction;
import fr.inrialpes.exmo.align.impl.edoal.RelationDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationCoDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.InstanceId;

import fr.inrialpes.exmo.align.impl.edoal.Transformation;
import fr.inrialpes.exmo.align.impl.edoal.ValueExpression;
import fr.inrialpes.exmo.align.impl.edoal.Value;
import fr.inrialpes.exmo.align.impl.edoal.Apply;
import fr.inrialpes.exmo.align.impl.edoal.Datatype;
import fr.inrialpes.exmo.align.impl.edoal.EDOALCell;
import fr.inrialpes.exmo.align.impl.edoal.EDOALVisitor;

/**
 * Renders an alignment in its RDF format
 *
 * @author Jérôme Euzenat
 * @version $Id: RDFRendererVisitor.java 1806 2013-02-08 14:27:25Z euzenat $
 */

public class RDFRendererVisitor extends IndentedRendererVisitor implements AlignmentVisitor,EDOALVisitor {

    Alignment alignment = null;
    Cell cell = null;
    Hashtable<String,String> nslist = null;
    boolean embedded = false; // if the output is XML embeded in a structure

    private static Namespace DEF = Namespace.ALIGNMENT;
    
    private boolean isPattern = false;
	
    public RDFRendererVisitor( PrintWriter writer ){
	super( writer );
    }

    public void init( Properties p ) {
	if ( p.getProperty( "embedded" ) != null 
	     && !p.getProperty( "embedded" ).equals("") ) embedded = true;
	if ( p.getProperty( "indent" ) != null )
	    INDENT = p.getProperty( "indent" );
	if ( p.getProperty( "newline" ) != null )
	    NL = p.getProperty( "newline" );
    }

    public void visit( Alignment align ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, align, Alignment.class ) ) return;
	// default behaviour
	String extensionString = "";
	alignment = align;
	nslist = new Hashtable<String,String>();
        nslist.put( Namespace.ALIGNMENT.prefix , Namespace.ALIGNMENT.shortCut );
        nslist.put( Namespace.RDF.prefix , Namespace.RDF.shortCut );
        nslist.put( Namespace.XSD.prefix , Namespace.XSD.shortCut );
	// Get the keys of the parameter
	int gen = 0;
	for ( String[] ext : align.getExtensions() ) {
	    String prefix = ext[0];
	    String name = ext[1];
	    String tag = nslist.get(prefix);
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
	    writer.print("' standalone='no'?>"+NL);
	}
	writer.print("<"+SyntaxElement.RDF.print(DEF)+" xmlns='"+Namespace.ALIGNMENT.prefix+"'");
	// JE2009: (1) I must use xml:base
	//writer.print(NL+"         xml:base='"+Namespace.ALIGNMENT.uri+"'");
	for ( Enumeration e = nslist.keys() ; e.hasMoreElements(); ) {
	    String k = (String)e.nextElement();
	    writer.print(NL+INDENT+INDENT+INDENT+INDENT+" xmlns:"+nslist.get(k)+"='"+k+"'");
	}
	if ( align instanceof BasicAlignment ) {
	    for ( String label : ((BasicAlignment)align).getXNamespaces().stringPropertyNames() ) {
		if ( !label.equals("rdf") && !label.equals("xsd")
		     && !label.equals("<default>") )
		    writer.print(NL+INDENT+INDENT+INDENT+INDENT+" xmlns:"+label+"='"+((BasicAlignment)align).getXNamespace( label )+"'");
	    }
	}
	writer.print(">"+NL);
	indentedOutput("<"+SyntaxElement.ALIGNMENT.print(DEF));
	String idext = align.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID );
	if ( idext != null ) {
	    writer.print(" "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+idext+"\"");
	}
	writer.print(">"+NL);
	increaseIndent();
	indentedOutputln( "<"+SyntaxElement.XML.print(DEF)+">yes</"+SyntaxElement.XML.print(DEF)+">" );
	if ( alignment.getLevel().startsWith("2EDOALPattern") ) isPattern = true;
	indentedOutputln( "<"+SyntaxElement.LEVEL.print(DEF)+">"+align.getLevel()+"</"+SyntaxElement.LEVEL.print(DEF)+">" );
	indentedOutputln( "<"+SyntaxElement.TYPE.print(DEF)+">"+align.getType()+"</"+SyntaxElement.TYPE.print(DEF)+">");
	writer.print(extensionString);
	indentedOutputln( "<"+SyntaxElement.MAPPING_SOURCE.print(DEF)+">" );
	increaseIndent();
	if ( align instanceof BasicAlignment ) {
	    printOntology( ((BasicAlignment)align).getOntologyObject1() );
	} else {
	    printBasicOntology( align.getOntology1URI(), align.getFile1() );
	}
	decreaseIndent();
	indentedOutputln( "</"+SyntaxElement.MAPPING_SOURCE.print(DEF)+">" );
	indentedOutputln( "<"+SyntaxElement.MAPPING_TARGET.print(DEF)+">" );
	increaseIndent();
	if ( align instanceof BasicAlignment ) {
	    printOntology( ((BasicAlignment)align).getOntologyObject2() );
	} else {
	    printBasicOntology( align.getOntology2URI(), align.getFile2() );
	}
	decreaseIndent();
	indentedOutputln( "</"+SyntaxElement.MAPPING_TARGET.print(DEF)+">" );
	for( Cell c : align ){ c.accept( this ); };
	decreaseIndent();
	indentedOutputln("</"+SyntaxElement.ALIGNMENT.print(DEF)+">");
	writer.print("</"+SyntaxElement.RDF.print(DEF)+">"+NL);
    }

    private void printBasicOntology ( URI u, URI f ) {
	indentedOutput("<"+SyntaxElement.ONTOLOGY.print(DEF)+" "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+u+"\">"+NL);
	increaseIndent();
	if ( f != null ) {
	    indentedOutputln("<"+SyntaxElement.LOCATION.print(DEF)+">"+f+"</"+SyntaxElement.LOCATION.print(DEF)+">");
	} else {
	    indentedOutputln("<"+SyntaxElement.LOCATION.print(DEF)+">"+u+"</"+SyntaxElement.LOCATION.print(DEF)+">");
	}
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.ONTOLOGY.print(DEF)+">"+NL);
    }

    public void printOntology( Ontology onto ) {
	URI u = onto.getURI();
	URI f = onto.getFile();
	indentedOutput("<"+SyntaxElement.ONTOLOGY.print(DEF)+" "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+u+"\">"+NL);
	increaseIndent();
	if ( f != null ) {
	    indentedOutputln("<"+SyntaxElement.LOCATION.print(DEF)+">"+f+"</"+SyntaxElement.LOCATION.print(DEF)+">");
	} else {
	    indentedOutputln("<"+SyntaxElement.LOCATION.print(DEF)+">"+u+"</"+SyntaxElement.LOCATION.print(DEF)+">");
	}
	if ( onto.getFormalism() != null ) {
	    indentedOutputln("<"+SyntaxElement.FORMATT.print(DEF)+">");
	    increaseIndent();
	    indentedOutputln("<"+SyntaxElement.FORMALISM.print(DEF)+" "+SyntaxElement.NAME.print()+"=\""+onto.getFormalism()+"\" "+SyntaxElement.URI.print()+"=\""+onto.getFormURI()+"\"/>");
	    decreaseIndent();
	    indentedOutputln("</"+SyntaxElement.FORMATT.print(DEF)+">");
	}
	decreaseIndent();
	indentedOutputln("</"+SyntaxElement.ONTOLOGY.print(DEF)+">");
    }

    public void visit( Cell cell ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, cell, Cell.class ) ) return;
	// default behaviour
	this.cell = cell;
	URI u1 = cell.getObject1AsURI(alignment);
	URI u2 = cell.getObject2AsURI(alignment);
	if ( ( u1 != null && u2 != null)
	     || alignment.getLevel().startsWith("2EDOAL") ){ //expensive test
	    indentedOutputln("<"+SyntaxElement.MAP.print(DEF)+">");
	    increaseIndent();
	    indentedOutput("<"+SyntaxElement.CELL.print(DEF));
	    if ( cell.getId() != null && !cell.getId().equals("") ){
		writer.print(" "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+cell.getId()+"\"");
	    }
	    writer.print(">"+NL);
	    increaseIndent();
	    if ( alignment.getLevel().startsWith("2EDOAL") ) {
		indentedOutputln("<"+SyntaxElement.ENTITY1.print(DEF)+">");
		increaseIndent();
		((Expression)(cell.getObject1())).accept( this );
		decreaseIndent();
		writer.print(NL);
		indentedOutputln("</"+SyntaxElement.ENTITY1.print(DEF)+">");
		indentedOutputln("<"+SyntaxElement.ENTITY2.print(DEF)+">");
		increaseIndent();
		((Expression)(cell.getObject2())).accept( this );
		decreaseIndent();
		writer.print(NL);
		indentedOutputln("</"+SyntaxElement.ENTITY2.print(DEF)+">");
		if ( cell instanceof EDOALCell ) { // Here put the transf
		    Set<Transformation> transfs = ((EDOALCell)cell).transformations();
		    if ( transfs != null ) {
			for ( Transformation transf : transfs ){
			    indentedOutputln("<"+SyntaxElement.TRANSFORMATION.print(DEF)+">");
			    increaseIndent();
			    transf.accept( this );
			    decreaseIndent();
			    writer.print(NL);
			    indentedOutputln("</"+SyntaxElement.TRANSFORMATION.print(DEF)+">");
			}
		    }
		}
	    } else {
		indentedOutputln("<"+SyntaxElement.ENTITY1.print(DEF)+" "+SyntaxElement.RDF_RESOURCE.print(DEF)+"='"+u1.toString()+"'/>");
		indentedOutputln("<"+SyntaxElement.ENTITY2.print(DEF)+" "+SyntaxElement.RDF_RESOURCE.print(DEF)+"='"+u2.toString()+"'/>");
	    }
	    indentedOutput("<"+SyntaxElement.RULE_RELATION.print(DEF)+">");
	    cell.getRelation().accept( this );
	    writer.print("</"+SyntaxElement.RULE_RELATION.print(DEF)+">"+NL);
	    indentedOutputln("<"+SyntaxElement.MEASURE.print(DEF)+" "+SyntaxElement.RDF_DATATYPE.print(DEF)+"='"+Namespace.XSD.getUriPrefix()+"float'>"+cell.getStrength()+"</"+SyntaxElement.MEASURE.print(DEF)+">");
	    if ( cell.getSemantics() != null &&
		 !cell.getSemantics().equals("") &&
		 !cell.getSemantics().equals("first-order") )
		indentedOutputln("<"+SyntaxElement.SEMANTICS.print(DEF)+">"+cell.getSemantics()+"</"+SyntaxElement.SEMANTICS.print(DEF)+">");
	    if ( cell.getExtensions() != null ) {
		for ( String[] ext : cell.getExtensions() ){
		    String uri = ext[0];
		    String tag = nslist.get( uri );
		    if ( tag == null ){
			tag = ext[1];
			// That's heavy.
			// Maybe adding an extra: ns extension in the alignment at parsing time
			// would help redisplaying it better...
			indentedOutputln("<alignapilocalns:"+tag+" xmlns:alignapilocalns=\""+uri+"\">"+ext[2]+"</alignapilocalns:"+tag+">");
		    } else {
			tag += ":"+ext[1];
			indentedOutputln("<"+tag+">"+ext[2]+"</"+tag+">");
		    }
		}
	    }
	    decreaseIndent();
	    indentedOutputln("</"+SyntaxElement.CELL.print(DEF)+">");
	    decreaseIndent();
	    indentedOutputln("</"+SyntaxElement.MAP.print(DEF)+">");
	}
    }

    public void visit( Relation rel ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, rel, Relation.class ) ) return;
	// default behaviour
	rel.write( writer );
    };

    // ********** EDOAL

    public void renderVariables( Expression expr ) {
	if ( expr.getVariable() != null ) {
	    writer.print( " "+SyntaxElement.VAR.print(DEF)+"=\""+expr.getVariable().name() );
	}
    }

    public void visit( final ClassId e ) throws AlignmentException {
	indentedOutput("<"+SyntaxElement.CLASS_EXPR.print(DEF));
	if ( e.getURI() != null ) {
	    writer.print(" "+SyntaxElement.RDF_ABOUT.print(DEF));
	    writer.print("=\""+e.getURI()+"\"");
	}
	if ( isPattern ) renderVariables( e );
	writer.print("/>");
    }

    public void visit( final ClassConstruction e ) throws AlignmentException {
	final Constructor op = e.getOperator();
	String sop = SyntaxElement.getElement( op ).print(DEF) ;
	indentedOutput("<"+SyntaxElement.CLASS_EXPR.print(DEF));
	if ( isPattern ) renderVariables( e );
	writer.print(">"+NL);
	increaseIndent();
	indentedOutput("<"+sop);
	if ( (op == Constructor.AND) || (op == Constructor.OR) ) writer.print(" "+SyntaxElement.RDF_PARSETYPE.print(DEF)+"=\"Collection\"");
	writer.print(">"+NL);
	increaseIndent();
	for ( final ClassExpression ce : e.getComponents() ) {
	    writer.print(linePrefix);
	    ce.accept( this );
	    writer.print(NL);
	}
	decreaseIndent();
	indentedOutput("</"+sop+">"+NL);
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.CLASS_EXPR.print(DEF)+">");
    }

    public void visit( final ClassValueRestriction c ) throws AlignmentException {
	indentedOutput("<"+SyntaxElement.VALUE_COND.print(DEF));
	if ( isPattern ) renderVariables( c );
	writer.print(">"+NL);
	increaseIndent();
	indentedOutput("<"+SyntaxElement.ONPROPERTY.print(DEF)+">"+NL);
	increaseIndent();
	c.getRestrictionPath().accept( this );
	decreaseIndent();
	writer.print(NL);
	indentedOutputln("</"+SyntaxElement.ONPROPERTY.print(DEF)+">");
	indentedOutput("<"+SyntaxElement.COMPARATOR.print(DEF));
	writer.print(" "+SyntaxElement.RDF_RESOURCE.print(DEF));
	writer.print("=\""+c.getComparator().getURI());
	writer.print("\"/>"+NL);
	indentedOutput("<"+SyntaxElement.VALUE.print(DEF)+">"+NL);
	increaseIndent();
	c.getValue().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.VALUE.print(DEF)+">"+NL);
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.VALUE_COND.print(DEF)+">");
    }

    public void visit( final ClassTypeRestriction c ) throws AlignmentException {
	indentedOutput("<"+SyntaxElement.TYPE_COND.print(DEF));
	if ( isPattern ) renderVariables( c );
	writer.print(">"+NL);
	increaseIndent();
	indentedOutput("<"+SyntaxElement.ONPROPERTY.print(DEF)+">"+NL);
	increaseIndent();
	c.getRestrictionPath().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.ONPROPERTY.print(DEF)+">"+NL);
	c.getType().accept( this ); // Directly -> to be changed for rendering all/exists
	decreaseIndent();
	writer.print(NL);
	indentedOutput("</"+SyntaxElement.TYPE_COND.print(DEF)+">");
    }

    public void visit( final ClassDomainRestriction c ) throws AlignmentException {
	indentedOutput("<"+SyntaxElement.DOMAIN_RESTRICTION.print(DEF));
	if ( isPattern ) renderVariables( c );
	writer.print(">"+NL);
	increaseIndent();
	indentedOutput("<"+SyntaxElement.ONPROPERTY.print(DEF)+">"+NL);
	increaseIndent();
	c.getRestrictionPath().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.ONPROPERTY.print(DEF)+">"+NL);
	if ( c.isUniversal() ) {
	    indentedOutput("<"+SyntaxElement.ALL.print(DEF)+">"+NL);
	} else {
	    indentedOutput("<"+SyntaxElement.EXISTS.print(DEF)+">"+NL);
	}
	increaseIndent();
	c.getDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	if ( c.isUniversal() ) {
	    indentedOutput("</"+SyntaxElement.ALL.print(DEF)+">"+NL);
	} else {
	    indentedOutput("</"+SyntaxElement.EXISTS.print(DEF)+">"+NL);
	}
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.DOMAIN_RESTRICTION.print(DEF)+">");
    }

    public void visit( final ClassOccurenceRestriction c ) throws AlignmentException {
	indentedOutput("<"+SyntaxElement.OCCURENCE_COND.print(DEF));
	if ( isPattern ) renderVariables( c );
	writer.print(">"+NL);
	increaseIndent();
	indentedOutput("<"+SyntaxElement.ONPROPERTY.print(DEF)+">"+NL);
	increaseIndent();
	c.getRestrictionPath().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.ONPROPERTY.print(DEF)+">"+NL);
	indentedOutput("<"+SyntaxElement.COMPARATOR.print(DEF));
	writer.print(" "+SyntaxElement.RDF_RESOURCE.print(DEF));
	writer.print("=\""+c.getComparator().getURI());
	writer.print("\"/>"+NL);
	indentedOutput("<"+SyntaxElement.VALUE.print(DEF)+">");
	writer.print(c.getOccurence());
	writer.print("</"+SyntaxElement.VALUE.print(DEF)+">"+NL);
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.OCCURENCE_COND.print(DEF)+">");
    }
    
    public void visit(final PropertyId e) throws AlignmentException {
	indentedOutput("<"+SyntaxElement.PROPERTY_EXPR.print(DEF));
	if ( e.getURI() != null ){
	    writer.print(" "+SyntaxElement.RDF_ABOUT.print(DEF));
	    writer.print("=\""+e.getURI()+"\"");
	}
	if ( isPattern ) renderVariables( e );
	writer.print("/>");
    }

    public void visit(final PropertyConstruction e) throws AlignmentException {
	indentedOutput("<"+SyntaxElement.PROPERTY_EXPR.print(DEF));
	if ( isPattern ) renderVariables( e );
	writer.print(">"+NL);
	increaseIndent();
	final Constructor op = e.getOperator();
	String sop = SyntaxElement.getElement( op ).print(DEF) ;
	indentedOutput("<"+sop);
	if ( (op == Constructor.AND) || (op == Constructor.OR) || (op == Constructor.COMP) ) writer.print(" "+SyntaxElement.RDF_PARSETYPE.print(DEF)+"=\"Collection\"");
	writer.print(">"+NL);
	increaseIndent();
	if ( (op == Constructor.AND) || (op == Constructor.OR) || (op == Constructor.COMP) ) {
	    for ( final PathExpression pe : e.getComponents() ) {
		writer.print(linePrefix);
		pe.accept( this );
		writer.print(NL);
	    }
	} else {
	    for (final PathExpression pe : e.getComponents()) {
		pe.accept( this );
		writer.print(NL);
	    }
	}
	decreaseIndent();
	indentedOutput("</"+sop+">"+NL);
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.PROPERTY_EXPR.print(DEF)+">");
    }

    public void visit(final PropertyValueRestriction c) throws AlignmentException {
	indentedOutput("<"+SyntaxElement.PROPERTY_VALUE_COND.print(DEF));
	if ( isPattern ) renderVariables( c );
	writer.print(">"+NL);
	increaseIndent();
	indentedOutput("<"+SyntaxElement.COMPARATOR.print(DEF));
	writer.print(" "+SyntaxElement.RDF_RESOURCE.print(DEF));
	writer.print("=\""+c.getComparator().getURI());
	writer.print("\"/>"+NL);
	indentedOutput("<"+SyntaxElement.VALUE.print(DEF)+">"+NL);
	increaseIndent();
	c.getValue().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.VALUE.print(DEF)+">"+NL);
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.PROPERTY_VALUE_COND.print(DEF)+">");
    }

    public void visit(final PropertyDomainRestriction c) throws AlignmentException {
	indentedOutput("<"+SyntaxElement.PROPERTY_DOMAIN_COND.print(DEF));
	if ( isPattern ) renderVariables( c );
	writer.print(">"+NL);
	increaseIndent();
	indentedOutput("<"+SyntaxElement.TOCLASS.print(DEF)+">"+NL);
	increaseIndent();
	c.getDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.TOCLASS.print(DEF)+">"+NL);
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.PROPERTY_DOMAIN_COND.print(DEF)+">");
    }

    public void visit(final PropertyTypeRestriction c) throws AlignmentException {
	indentedOutput("<"+SyntaxElement.PROPERTY_TYPE_COND.print(DEF));
	if ( isPattern ) renderVariables( c );
	writer.print(">"+NL);
	increaseIndent();
	c.getType().accept( this );
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.PROPERTY_TYPE_COND.print(DEF)+">");
    }
    
    public void visit( final RelationId e ) throws AlignmentException {
	indentedOutput("<"+SyntaxElement.RELATION_EXPR.print(DEF));
	if ( e.getURI() != null ) {
	    writer.print(" "+SyntaxElement.RDF_ABOUT.print(DEF));
	    writer.print("=\""+e.getURI()+"\"");
	}
	if ( isPattern ) renderVariables( e );
	writer.print("/>");
    }

    public void visit( final RelationConstruction e ) throws AlignmentException {
	indentedOutput("<"+SyntaxElement.RELATION_EXPR.print(DEF));
	if ( isPattern ) renderVariables( e );
	writer.print(">"+NL);
	increaseIndent();
	final Constructor op = e.getOperator();
	String sop = SyntaxElement.getElement( op ).print(DEF) ;
	indentedOutput("<"+sop);
	if ( (op == Constructor.OR) || (op == Constructor.AND) || (op == Constructor.COMP) ) writer.print(" "+SyntaxElement.RDF_PARSETYPE.print(DEF)+"=\"Collection\"");
	writer.print(">"+NL);
	increaseIndent();
	if ( (op == Constructor.AND) || (op == Constructor.OR) || (op == Constructor.COMP) ) {
	    for (final PathExpression re : e.getComponents()) {
		writer.print(linePrefix);
		re.accept( this );
		writer.print(NL);
	    }
	} else { // NOT... or else: enumerate them
	    for (final PathExpression re : e.getComponents()) {
		re.accept( this );
		writer.print(NL);
	    }
	}
	decreaseIndent();
	indentedOutput("</"+sop+">"+NL);
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.RELATION_EXPR.print(DEF)+">");
    }
	
    public void visit(final RelationCoDomainRestriction c) throws AlignmentException {
	indentedOutput("<"+SyntaxElement.RELATION_CODOMAIN_COND.print(DEF));
	if ( isPattern ) renderVariables( c );
	writer.print(">"+NL);
	increaseIndent();
	indentedOutput("<"+SyntaxElement.TOCLASS.print(DEF)+">"+NL);
	increaseIndent();
	c.getCoDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.TOCLASS.print(DEF)+">"+NL);
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.RELATION_CODOMAIN_COND.print(DEF)+">");
    }

    public void visit(final RelationDomainRestriction c) throws AlignmentException {
	indentedOutput("<"+SyntaxElement.RELATION_DOMAIN_COND.print(DEF));
	if ( isPattern ) renderVariables( c );
	writer.print(">"+NL);
	increaseIndent();
	indentedOutput("<"+SyntaxElement.TOCLASS.print(DEF)+">"+NL);
	increaseIndent();
	c.getDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.TOCLASS.print(DEF)+">"+NL);
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.RELATION_DOMAIN_COND.print(DEF)+">");
    }

    public void visit( final InstanceId e ) throws AlignmentException {
	indentedOutput("<"+SyntaxElement.INSTANCE_EXPR.print(DEF));
	if ( e.getURI() != null ) {
	    writer.print(" "+SyntaxElement.RDF_ABOUT.print(DEF));
	    writer.print("=\""+e.getURI()+"\"");
	}
	if ( isPattern ) renderVariables( e );
	writer.print("/>");
    }
    
    public void visit( final Value e ) throws AlignmentException {
	indentedOutput("<"+SyntaxElement.LITERAL.print(DEF)+" ");
	if ( e.getType() != null ) {
	    writer.print(SyntaxElement.ETYPE.print(DEF)+"=\""+e.getType()+"\" ");
	}
	writer.print(SyntaxElement.STRING.print(DEF)+"=\""+e.getValue()+"\"/>");
    }
	
    public void visit( final Apply e ) throws AlignmentException {
	indentedOutputln("<"+SyntaxElement.APPLY.print(DEF)+" "+SyntaxElement.OPERATOR.print(DEF)+"=\""+e.getOperation()+"\">");
	increaseIndent();
	indentedOutputln("<"+SyntaxElement.ARGUMENTS.print(DEF)+" "+SyntaxElement.RDF_PARSETYPE.print(DEF)+"=\"Collection\">");
	increaseIndent();
	for ( final ValueExpression ve : e.getArguments() ) {
	    writer.print(linePrefix);
	    ve.accept( this );
	    writer.print(NL);
	}
	decreaseIndent();
	indentedOutputln("</"+SyntaxElement.ARGUMENTS.print(DEF)+">");
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.APPLY.print(DEF)+">");
    }

    public void visit( final Transformation transf ) throws AlignmentException {
	indentedOutput("<"+SyntaxElement.TRANSF.print(DEF)+" "+SyntaxElement.TRDIR.print(DEF)+"=\""+transf.getType()+"\">"+NL);
	increaseIndent();
	indentedOutputln("<"+SyntaxElement.TRENT1.print(DEF)+">");
	increaseIndent();
	transf.getObject1().accept( this );
	decreaseIndent();
	writer.print(NL);
	indentedOutputln("</"+SyntaxElement.TRENT1.print(DEF)+">");
	indentedOutputln("<"+SyntaxElement.TRENT2.print(DEF)+">");
	increaseIndent();
	transf.getObject2().accept( this );
	decreaseIndent();
	writer.print(NL);
	indentedOutputln("</"+SyntaxElement.TRENT2.print(DEF)+">");
	decreaseIndent();
	indentedOutput("</"+SyntaxElement.TRANSF.print(DEF)+">");
    }

    public void visit( final Datatype e ) throws AlignmentException {
	indentedOutput("<"+SyntaxElement.EDATATYPE.print(DEF)+">");
	writer.print("<"+SyntaxElement.DATATYPE.print(DEF)+" "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+e.getType()+"\"/>");
	writer.print("</"+SyntaxElement.EDATATYPE.print(DEF)+">");
    }
	
}
