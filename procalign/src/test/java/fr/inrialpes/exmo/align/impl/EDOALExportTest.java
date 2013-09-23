package fr.inrialpes.exmo.align.impl;/*
 * $Id: EDOALExportTest.java 1804 2013-02-08 14:24:29Z euzenat $
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.3 -- 2007
 * Copyright (C) INRIA, 2009-2012
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

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Alignment;

import fr.inrialpes.exmo.align.impl.renderer.RDFRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.JSONRendererVisitor;
import fr.inrialpes.exmo.align.impl.renderer.OWLAxiomsRendererVisitor;
import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.ontowrap.Ontology;
import fr.inrialpes.exmo.ontowrap.BasicOntology;

import java.io.ByteArrayOutputStream;
import java.io.PrintWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.net.URI;

import fr.inrialpes.exmo.align.parser.SyntaxElement.Constructor;

import fr.inrialpes.exmo.align.impl.edoal.PathExpression;
import fr.inrialpes.exmo.align.impl.edoal.Expression;
import fr.inrialpes.exmo.align.impl.edoal.ClassExpression;
import fr.inrialpes.exmo.align.impl.edoal.ClassId;
import fr.inrialpes.exmo.align.impl.edoal.ClassConstruction;
import fr.inrialpes.exmo.align.impl.edoal.ClassRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.ClassOccurenceRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyExpression;
import fr.inrialpes.exmo.align.impl.edoal.PropertyId;
import fr.inrialpes.exmo.align.impl.edoal.PropertyConstruction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationExpression;
import fr.inrialpes.exmo.align.impl.edoal.RelationId;
import fr.inrialpes.exmo.align.impl.edoal.RelationConstruction;
import fr.inrialpes.exmo.align.impl.edoal.RelationRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationCoDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.InstanceExpression;
import fr.inrialpes.exmo.align.impl.edoal.InstanceId;
import fr.inrialpes.exmo.align.impl.edoal.Value;
import fr.inrialpes.exmo.align.impl.edoal.Datatype;
import fr.inrialpes.exmo.align.impl.edoal.Comparator;

/**
 * These tests corresponds to the tests presented in the examples/omwg directory
 */

public class EDOALExportTest {

    private RDFRendererVisitor renderer;
    private PrintWriter writer;
    private ByteArrayOutputStream stream;

    @Test(groups = { "full", "omwg", "raw" })
    //@BeforeClass(groups = { "full", "omwg", "raw" })
    public void setUp() throws Exception {
    }

    private String render( Expression v ) throws Exception {
	// JE2009: This can only be improved if we can change the stream
	stream = new ByteArrayOutputStream(); 
	writer = new PrintWriter ( new BufferedWriter(
				                 new OutputStreamWriter( stream, "UTF-8" )), true);
	renderer = new RDFRendererVisitor( writer );
	renderer.setIndentString("");	// Indent should be empty
	renderer.setNewLineString("");
	v.accept( renderer );
	writer.flush();
	writer.close();
	stream.close();
	return stream.toString();
    }

    // Load the full test ==> break because not all can be rendered
    @Test(expectedExceptions = AlignmentException.class, groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testOWLRendering0() throws Exception {
	AlignmentParser aparser = new AlignmentParser( 0 );
	aparser.initAlignment( null );
	Alignment alignment = aparser.parse( "file:examples/omwg/total.xml" );
	assertNotNull( alignment );
	// Print it in a string
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new OWLAxiomsRendererVisitor( writer );
	alignment.render( renderer );
    }

    // Load the stripped down test ==> it loads
    // This cannot be passed to an OWL parser because no typechecking has been done
    // This is only a syntactic test
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"testOWLRendering0"})
    public void testOWLRendering1() throws Exception {
	AlignmentParser aparser = new AlignmentParser( 0 );
	aparser.initAlignment( null );
	Alignment alignment = aparser.parse( "file:examples/omwg/total-owlable.xml" );
	assertNotNull( alignment );
	// Print it in a string
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			      new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	/*
	OutputStream stream = new FileOutputStream( "/tmp/total.owl" );
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	*/
	AlignmentVisitor renderer = new OWLAxiomsRendererVisitor( writer );
	alignment.render( renderer );
	writer.flush();
	writer.close();
	String str1 = stream.toString();
	//System.err.println(str1);
	assertEquals( str1.length(), 11623 );
    }

    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"testOWLRendering0"})
    public void testJSONRendering() throws Exception {
	AlignmentParser aparser = new AlignmentParser( 0 );
	aparser.initAlignment( null );
	Alignment alignment = aparser.parse( "file:examples/omwg/total.xml" );
	assertNotNull( alignment );
	// Print it in a string
	ByteArrayOutputStream stream = new ByteArrayOutputStream(); 
	PrintWriter writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( stream, "UTF-8" )), true);
	AlignmentVisitor renderer = new JSONRendererVisitor( writer );
	alignment.render( renderer );
	writer.flush();
	writer.close();
	String str1 = stream.toString();
	//System.err.println(str1);
	assertEquals( str1.length(), 36097 );
    }

    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportPath() throws Exception {
	/*
        assertEquals( render( new Path(new PropertyId(new URI("http://my.beauty.url")))),
		     "<edoal:Property rdf:about=\"http://my.beauty.url\"/>");
	assertEquals( render( Path.EMPTY ),"<edoal:Path rdf:resource=\"http://ns.inria.fr/edoal#emptyPath\"/>");
	*/
	final LinkedHashSet<PathExpression> expressions = new LinkedHashSet<PathExpression>(3);
	expressions.add( new RelationId(new URI("http://my.beauty.url")) );
	expressions.add( new RelationId(new URI("http://my.nasty.url")) );
	expressions.add( new RelationId(new URI("http://my.richi.url")) );
	assertEquals( render( new RelationConstruction( Constructor.COMP, expressions ) ),
		      "<edoal:Relation><edoal:compose rdf:parseType=\"Collection\">" +
		      "<edoal:Relation rdf:about=\"http://my.beauty.url\"/>" +
		      "<edoal:Relation rdf:about=\"http://my.nasty.url\"/>" +
		      "<edoal:Relation rdf:about=\"http://my.richi.url\"/>" +
		      "</edoal:compose></edoal:Relation>" );

	expressions.add( new PropertyId(new URI("http://my.final.url")) );
	assertEquals( render( new PropertyConstruction( Constructor.COMP, expressions ) ),
		      "<edoal:Property><edoal:compose rdf:parseType=\"Collection\">" +
		      "<edoal:Relation rdf:about=\"http://my.beauty.url\"/>" +
		      "<edoal:Relation rdf:about=\"http://my.nasty.url\"/>" +
		      "<edoal:Relation rdf:about=\"http://my.richi.url\"/>" +
		      "<edoal:Property rdf:about=\"http://my.final.url\"/>" +
		      "</edoal:compose></edoal:Property>" );
    }

    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportInstanceExpression() throws Exception {
	final InstanceExpression toExport = new InstanceId(new URI("http://meine.tolle/instance#blah"));
	assertEquals( render( toExport ),
		      "<edoal:Instance rdf:about=\"http://meine.tolle/instance#blah\"/>" );
    }

    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportClassExprSimple() throws Exception {
	final ClassExpression ce = new ClassId("Amertume");
	final String ref = "<edoal:Class rdf:about=\"Amertume\"/>";
	assertEquals( render( ce ), ref );
    }
    
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportClassExprSimpleError() throws Exception {
	// Should raise an error
	final ClassExpression ce = new ClassId("Amertume");
	final String ref = "<edoal:Class rdf:about=\"Amertume\"/>";
	assertEquals( render( ce ), ref );
    }
    
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportClassCond() throws Exception {
	ClassRestriction toExport = null;
	toExport = new ClassValueRestriction(new PropertyId(new URI("http://my.sister#age")),Comparator.GREATER,new Value("18"));
	assertEquals( render( toExport ), "<edoal:AttributeValueRestriction>"
	    + "<edoal:onAttribute><edoal:Property rdf:about=\"http://my.sister#age\"/></edoal:onAttribute>"
	    + "<edoal:comparator rdf:resource=\"http://ns.inria.org/edoal/1.0/#greater-than\"/>"
	    + "<edoal:value><edoal:Literal edoal:string=\"18\"/></edoal:value>"
			  + "</edoal:AttributeValueRestriction>" );
	toExport = new ClassTypeRestriction( new PropertyId(new URI("http://my.sister#age")), new Datatype("integer-under-100"));
	assertEquals( render( toExport ), "<edoal:AttributeTypeRestriction>"
	    + "<edoal:onAttribute><edoal:Property rdf:about=\"http://my.sister#age\"/></edoal:onAttribute>"
	    //+ "<edoal:comparator rdf:resource=\"http://ns.inria.org/edoal/1.0/#equals\"/>"
	    + "<edoal:datatype><edoal:Datatype rdf:about=\"integer-under-100\"/></edoal:datatype>"
		+ "</edoal:AttributeTypeRestriction>" );
	toExport = new ClassOccurenceRestriction( new PropertyId(new URI("http://my.sister#age")), Comparator.GREATER, 18);
	assertEquals( render( toExport ), "<edoal:AttributeOccurenceRestriction>"
	    + "<edoal:onAttribute><edoal:Property rdf:about=\"http://my.sister#age\"/></edoal:onAttribute>"
	    + "<edoal:comparator rdf:resource=\"http://ns.inria.org/edoal/1.0/#greater-than\"/>"
	    + "<edoal:value>18</edoal:value>"
		      + "</edoal:AttributeOccurenceRestriction>" );
    }
    
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportClassExprOr() throws Exception {
	final Set<ClassExpression> expressions = new LinkedHashSet<ClassExpression>(3);
	expressions.add( new ClassId("Acidite") );
	expressions.add( new ClassId("Amertume") );
	expressions.add( new ClassId("Astreinngence") );
	final ClassExpression ce = new ClassConstruction( Constructor.OR, expressions );
	final String ref = "<edoal:Class>" + "<edoal:or rdf:parseType=\"Collection\">"
	    + "<edoal:Class rdf:about=\"Acidite\"/>"
	    + "<edoal:Class rdf:about=\"Amertume\"/>"
	    + "<edoal:Class rdf:about=\"Astreinngence\"/>" 
	    + "</edoal:or>"+"</edoal:Class>";
	assertEquals( render( ce ), ref );
    }
    
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportClassExprAnd() throws Exception {
	final Set<ClassExpression> expressions = new LinkedHashSet<ClassExpression>(3);
	expressions.add(new ClassId("Acidite"));
	expressions.add(new ClassId("Amertume"));
	expressions.add(new ClassId("Astreinngence"));
	final ClassExpression ce = new ClassConstruction( Constructor.AND, expressions );
	final String ref = "<edoal:Class>" + "<edoal:and rdf:parseType=\"Collection\">"
	    + "<edoal:Class rdf:about=\"Acidite\"/>"
	    + "<edoal:Class rdf:about=\"Amertume\"/>"
	    + "<edoal:Class rdf:about=\"Astreinngence\"/>" 
	    + "</edoal:and>"+"</edoal:Class>";
	assertEquals( render( ce ), ref );
    }
    
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportClassExprNot() throws Exception {
	final ClassExpression ce = new ClassConstruction(Constructor.NOT, 
					 Collections.singleton((ClassExpression)new ClassId("Acidite")));
	final String ref = "<edoal:Class>" + "<edoal:not>"
	    + "<edoal:Class rdf:about=\"Acidite\"/>" + "</edoal:not>"
	    + "</edoal:Class>";
	assertEquals( render( ce ), ref );
    }
    
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportClassExprOrCond() throws Exception {
	final Set<ClassExpression> expressions = new LinkedHashSet<ClassExpression>(3);
	expressions.add(new ClassId("Acidite"));
	expressions.add(new ClassId("Amertume"));
	expressions.add(new ClassId("Astreinngence"));
	expressions.add(new ClassValueRestriction( new PropertyId(new URI("http://vinum#age")), Comparator.GREATER, new Value("20")));
	final ClassExpression ce = new ClassConstruction( Constructor.OR, expressions );
	assertEquals( render( ce ), "<edoal:Class>" + "<edoal:or rdf:parseType=\"Collection\">"
	    + "<edoal:Class rdf:about=\"Acidite\"/>"
	    + "<edoal:Class rdf:about=\"Amertume\"/>"
	    + "<edoal:Class rdf:about=\"Astreinngence\"/>"
	    + "<edoal:AttributeValueRestriction>"
	    + "<edoal:onAttribute>"
	    + "<edoal:Property rdf:about=\"http://vinum#age\"/>"
	    + "</edoal:onAttribute>"
	    + "<edoal:comparator rdf:resource=\"http://ns.inria.org/edoal/1.0/#greater-than\"/>"
	    + "<edoal:value><edoal:Literal edoal:string=\"20\"/></edoal:value>"
	    + "</edoal:AttributeValueRestriction>"
	    + "</edoal:or>"+ "</edoal:Class>" );
    }

    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportPropertyCond() throws Exception {
	assertEquals( render( new PropertyDomainRestriction(new ClassId("http://meine/tolle/restriction")) ),
		      "<edoal:PropertyDomainRestriction><edoal:class>"
		      + "<edoal:Class rdf:about=\"http://meine/tolle/restriction\"/>"
		      + "</edoal:class></edoal:PropertyDomainRestriction>" );
	assertEquals( render( new PropertyValueRestriction( Comparator.EQUAL, new Value("18"))),
		      "<edoal:PropertyValueRestriction>"
		      + "<edoal:comparator rdf:resource=\"http://ns.inria.org/edoal/1.0/#equals\"/>"
		      + "<edoal:value><edoal:Literal edoal:string=\"18\"/></edoal:value>"
		      + "</edoal:PropertyValueRestriction>" );
	assertEquals( render( new PropertyTypeRestriction(new Datatype("int"))),
		      "<edoal:PropertyTypeRestriction><edoal:datatype><edoal:Datatype rdf:about=\"int\"/></edoal:datatype></edoal:PropertyTypeRestriction>" );
    }
    
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportPropertyExpr() throws Exception {
	final Set<PathExpression> expressions = new LinkedHashSet<PathExpression>(2);
	expressions.add(new PropertyId(new URI("http://mein/super/property0")));
	expressions.add(new PropertyId(new URI("http://mein/super/property1")));
	final PropertyId single = new PropertyId(new URI("http://mein/super/property"));
	
	PropertyExpression toExport = single;
	assertEquals( render( toExport), "<edoal:Property rdf:about=\"http://mein/super/property\"/>");
	toExport = new PropertyConstruction( Constructor.AND, expressions );
	assertEquals( render( toExport), "<edoal:Property><edoal:and rdf:parseType=\"Collection\">"
		     + "<edoal:Property rdf:about=\"http://mein/super/property0\"/>"
		     + "<edoal:Property rdf:about=\"http://mein/super/property1\"/>"
		     + "</edoal:and></edoal:Property>");

	final Set<PathExpression> expressions2 = new LinkedHashSet<PathExpression>(2);
	expressions2.add( new PropertyConstruction( Constructor.OR, expressions ));
	expressions2.add( new PropertyValueRestriction(Comparator.EQUAL,new Value("5")));
	toExport = new PropertyConstruction( Constructor.AND, expressions2 );
	assertEquals( render( toExport),  "<edoal:Property><edoal:and rdf:parseType=\"Collection\"><edoal:Property><edoal:or rdf:parseType=\"Collection\">"
		      + "<edoal:Property rdf:about=\"http://mein/super/property0\"/>"
		      + "<edoal:Property rdf:about=\"http://mein/super/property1\"/>"
		      + "</edoal:or></edoal:Property>"
		      + "<edoal:PropertyValueRestriction>"
		      + "<edoal:comparator rdf:resource=\"http://ns.inria.org/edoal/1.0/#equals\"/>"
		      + "<edoal:value><edoal:Literal edoal:string=\"5\"/></edoal:value></edoal:PropertyValueRestriction>"
		      + "</edoal:and></edoal:Property>");
	toExport = new PropertyConstruction( Constructor.NOT, Collections.singleton((PathExpression)new PropertyId(new URI("http://mein/super/property"))));
    }

    // ------

    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testExportRelationCondCond() throws Exception {
	RelationRestriction toExport = new RelationDomainRestriction(new ClassId("http://my/super/class"));
	assertEquals( render( toExport), "<edoal:RelationDomainRestriction><edoal:class>"
		      + "<edoal:Class rdf:about=\"http://my/super/class\"/>"
		      + "</edoal:class></edoal:RelationDomainRestriction>");
    toExport = new RelationCoDomainRestriction(new ClassId("http://my/super/class"));
	assertEquals( render( toExport), "<edoal:RelationCoDomainRestriction><edoal:class>"
	    + "<edoal:Class rdf:about=\"http://my/super/class\"/>"
		      + "</edoal:class></edoal:RelationCoDomainRestriction>");
    }

    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
    public void testParseRelationExpr() throws Exception {
	
	RelationExpression toExport = new RelationId("http://my/super/relation");
	assertEquals( render(toExport), 
		      "<edoal:Relation rdf:about=\"http://my/super/relation\"/>");

	// JE 2010: I could export it as well
	RelationExpression relexp = new RelationDomainRestriction(							  new ClassId("http://my/super/class"));

	final Set<PathExpression> expressions = new LinkedHashSet<PathExpression>(2);
	expressions.add(new RelationId("http://my/super/relation0"));
	expressions.add(new RelationId("http://my/super/relation1"));
	expressions.add( relexp );

	toExport = new RelationConstruction( Constructor.AND, expressions );
	assertEquals( render( toExport ), 
	    "<edoal:Relation>"
	    + "<edoal:and rdf:parseType=\"Collection\">"
	    + "<edoal:Relation rdf:about=\"http://my/super/relation0\"/>"
	    + "<edoal:Relation rdf:about=\"http://my/super/relation1\"/>"
	    + "<edoal:RelationDomainRestriction><edoal:class>"
	    + "<edoal:Class rdf:about=\"http://my/super/class\"/>"
	    + "</edoal:class></edoal:RelationDomainRestriction>" 
	    + "</edoal:and>" + "</edoal:Relation>");
	toExport = new RelationConstruction( Constructor.OR, expressions );
	assertEquals( render( toExport ), 
	    "<edoal:Relation>"
	    + "<edoal:or rdf:parseType=\"Collection\">"
	    + "<edoal:Relation rdf:about=\"http://my/super/relation0\"/>"
	    + "<edoal:Relation rdf:about=\"http://my/super/relation1\"/>"
	    + "<edoal:RelationDomainRestriction><edoal:class>"
	    + "<edoal:Class rdf:about=\"http://my/super/class\"/>"
	    + "</edoal:class></edoal:RelationDomainRestriction>" 
		      + "</edoal:or>" + "</edoal:Relation>");

	final Set<PathExpression> expressions2 = new LinkedHashSet<PathExpression>();
	expressions2.add(new RelationConstruction(Constructor.NOT,Collections.singleton((PathExpression)new RelationId("http://my/super/relation"))));
	expressions2.add(new RelationCoDomainRestriction(new ClassId("http://my/super/class")));

	toExport = new RelationConstruction( Constructor.AND, expressions2 );
	assertEquals( render( toExport ), 
	    "<edoal:Relation>"
	    + "<edoal:and rdf:parseType=\"Collection\">"
	    + "<edoal:Relation><edoal:not>"
	    + "<edoal:Relation rdf:about=\"http://my/super/relation\"/>"
	    + "</edoal:not></edoal:Relation>" 
	    + "<edoal:RelationCoDomainRestriction><edoal:class>"
	    + "<edoal:Class rdf:about=\"http://my/super/class\"/>"
	    + "</edoal:class></edoal:RelationCoDomainRestriction>" 
	    + "</edoal:and>" + "</edoal:Relation>");
	toExport = new RelationConstruction( Constructor.INVERSE, Collections.singleton((PathExpression)new RelationId("http://my/super/relation")));
	assertEquals( render( toExport ), 
	    "<edoal:Relation>"
	    + "<edoal:inverse>"
	    + "<edoal:Relation rdf:about=\"http://my/super/relation\"/>"
	    + "</edoal:inverse>" + "</edoal:Relation>");
	toExport = new RelationConstruction(Constructor.SYMMETRIC, Collections.singleton((PathExpression)new RelationId("http://my/super/relation")));
	assertEquals( render( toExport ), 
	    "<edoal:Relation>"
	    + "<edoal:symmetric>"
	    + "<edoal:Relation rdf:about=\"http://my/super/relation\"/>"
			  + "</edoal:symmetric>" + "</edoal:Relation>");
	toExport = new RelationConstruction(Constructor.TRANSITIVE, Collections.singleton((PathExpression)new RelationId("http://my/super/relation")));
	assertEquals( render( toExport ), 
	    "<edoal:Relation>"
	    + "<edoal:transitive>"
	    + "<edoal:Relation rdf:about=\"http://my/super/relation\"/>"
			  + "</edoal:transitive>" + "</edoal:Relation>");
	toExport = new RelationConstruction( Constructor.REFLEXIVE, Collections.singleton((PathExpression)new RelationId("http://my/super/relation")));
	assertEquals( render(toExport), 
	    "<edoal:Relation>"
	    + "<edoal:reflexive>"
	    + "<edoal:Relation rdf:about=\"http://my/super/relation\"/>"
			  + "</edoal:reflexive>" + "</edoal:Relation>" );
	
    }
    
    @Test(groups = { "full", "omwg" }, dependsOnMethods = {"setUp"})
    public void testExportCell() throws Exception {
    }
    
    @Test(groups = { "full", "omwg", "raw" }, dependsOnMethods = {"setUp"})
	public void testExportAlignment() throws Exception {
	
	Ontology o1 = new BasicOntology();
	o1.setURI( new URI("http://source") );
	o1.setFormalism( "wsml" );
	o1.setFormURI( new URI("http://wsml") );
	Ontology o2 = new BasicOntology();
	o2.setURI( new URI("http://target") );
	o2.setFormalism( "wsml" );
	o2.setFormURI( new URI("http://wsml") );
	final EDOALAlignment doc = new EDOALAlignment();
	doc.setExtension( Namespace.ALIGNMENT.uri, Annotations.ID, "http://asdf" );
	doc.init( o1, o2 );

	stream = new ByteArrayOutputStream(); 
	writer = new PrintWriter ( new BufferedWriter(
				                 new OutputStreamWriter( stream, "UTF-8" )), true);
	renderer = new RDFRendererVisitor( writer );
	renderer.setIndentString("");	// Indent should be empty
	renderer.setNewLineString("");
	doc.accept( renderer );//doc.render( renderer );
	writer.flush();
	writer.close();
	stream.close();
	assertEquals( stream.toString(), 
"<?xml version='1.0' encoding='utf-8' standalone='no'?><rdf:RDF xmlns='http://knowledgeweb.semanticweb.org/heterogeneity/alignment#'"+
         " xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"+
         " xmlns:xsd='http://www.w3.org/2001/XMLSchema#'"+
         " xmlns:align='http://knowledgeweb.semanticweb.org/heterogeneity/alignment#'"+
         " xmlns:edoal='http://ns.inria.org/edoal/1.0/#'>"+
		      "<Alignment rdf:about=\"http://asdf\"><xml>yes</xml><level>2EDOAL</level><type>**</type><id>http://asdf</id>"
	    + "<onto1>"
	    + "<Ontology rdf:about=\"http://source\"><location>http://source</location>"
	    + "<formalism><Formalism align:name=\"wsml\" align:uri=\"http://wsml\"/></formalism>"
	    + "</Ontology>" + "</onto1>" + "<onto2>"
	    + "<Ontology rdf:about=\"http://target\"><location>http://target</location>"
	    + "<formalism><Formalism align:name=\"wsml\" align:uri=\"http://wsml\"/></formalism>"
	    + "</Ontology>" + "</onto2>"
		      + "</Alignment>" +"</rdf:RDF>" );
	doc.setType( "1*" );
	stream = new ByteArrayOutputStream(); 
	writer = new PrintWriter ( new BufferedWriter(
				                 new OutputStreamWriter( stream, "UTF-8" )), true);
	renderer = new RDFRendererVisitor( writer );
	renderer.setIndentString("");	// Indent should be empty
	renderer.setNewLineString("");
	doc.accept( renderer );//doc.render( renderer );
	writer.flush();
	writer.close();
	stream.close();
	assertEquals( stream.toString(), 
"<?xml version='1.0' encoding='utf-8' standalone='no'?><rdf:RDF xmlns='http://knowledgeweb.semanticweb.org/heterogeneity/alignment#'"+
         " xmlns:rdf='http://www.w3.org/1999/02/22-rdf-syntax-ns#'"+
         " xmlns:xsd='http://www.w3.org/2001/XMLSchema#'"+
         " xmlns:align='http://knowledgeweb.semanticweb.org/heterogeneity/alignment#'"+
         " xmlns:edoal='http://ns.inria.org/edoal/1.0/#'>"+
		      "<Alignment rdf:about=\"http://asdf\"><xml>yes</xml><level>2EDOAL</level><type>1*</type><id>http://asdf</id>"
	    + "<onto1>"
	    + "<Ontology rdf:about=\"http://source\"><location>http://source</location>"
	    + "<formalism><Formalism align:name=\"wsml\" align:uri=\"http://wsml\"/></formalism>"
	    + "</Ontology>" + "</onto1>" + "<onto2>"
	    + "<Ontology rdf:about=\"http://target\"><location>http://target</location>"
	    + "<formalism><Formalism align:name=\"wsml\" align:uri=\"http://wsml\"/></formalism>"
	    + "</Ontology>" + "</onto2>"
	    + "</Alignment>" +"</rdf:RDF>" );
    }

}
