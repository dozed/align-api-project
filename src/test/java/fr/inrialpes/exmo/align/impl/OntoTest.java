package fr.inrialpes.exmo.align.impl;/*
 * $Id: OntoTest.java 1843 2013-03-25 11:10:54Z euzenat $
 *
 * Copyright (C) INRIA, 2008-2011
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
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import fr.inrialpes.exmo.ontowrap.Ontology;
import fr.inrialpes.exmo.ontowrap.BasicOntology;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

import fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3Ontology;
import fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3OntologyFactory;
import fr.inrialpes.exmo.ontowrap.jena25.JENAOntologyFactory;
import fr.inrialpes.exmo.ontowrap.jena25.JENAOntology;

import org.semanticweb.owl.align.AlignmentException;

import java.net.URI;

import java.util.Set;

/**
 * This test the encapsulation of various ontologies
 */

// The following assertion is fundamental for having the AfterClass method
// in the end run properly (otherwise, we have bugs around)
@Test(sequential = true)
public class OntoTest {

    private OntologyFactory factory = null;
    private Ontology ontology = null;

    @Test(groups = { "full", "onto", "raw" })
    public void factoryTest() throws Exception {
	assertNotNull( OntologyFactory.getDefaultFactory() );
	assertTrue( OntologyFactory.getDefaultFactory().equals("fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3OntologyFactory") );
	OntologyFactory.setDefaultFactory("fr.inrialpes.exmo.ontowrap.jena25.JENAOntologyFactory");
	assertTrue( OntologyFactory.getDefaultFactory().equals("fr.inrialpes.exmo.ontowrap.jena25.JENAOntologyFactory") );
	factory = OntologyFactory.getFactory();
	assertTrue( factory instanceof JENAOntologyFactory );
	OntologyFactory.setDefaultFactory("fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3OntologyFactory");
	factory = OntologyFactory.getFactory();
	assert( factory instanceof OWLAPI3OntologyFactory );
	assertEquals( factory, OntologyFactory.getFactory() );
    }

    @Test(expectedExceptions = OntowrapException.class, groups = { "full", "onto", "raw" }, dependsOnMethods = {"factoryTest"})
    public void concreteFactoryTest() throws Exception {
	// not really useful
	factory.newOntology( null );
    }

    @Test(groups = { "full", "onto", "raw" }, dependsOnMethods = {"factoryTest"})
    public void basicTest() throws Exception {
	// not really useful
	//factory.newOntology( null );
	Ontology<String> onto = new BasicOntology<String>();
	assertNotNull( onto );
	onto.setURI( new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl") );
	onto.setFile( new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl") );
	assertEquals( onto.getURI(), onto.getFile() );
	onto.setFormalism( "OWL1.0" );
	assertEquals( onto.getFormalism(), "OWL1.0" );
	onto.setFormURI( new URI("http://www.w3.org/2002/07/owl#") );
	onto.setOntology( "MyBeautifulOntology" );
	assertEquals( onto.getOntology(), "MyBeautifulOntology" );
    }

    @Test(groups = { "full", "onto", "raw" }, dependsOnMethods = {"basicTest"})
    public void basicServiceTest() throws Exception {
	BasicOntology<String> onto = new BasicOntology<String>();
	assertNotNull( onto );
	assertEquals( onto.getFragmentAsLabel( new URI("http://example.com/#123" ) ), "123" );
	assertEquals( onto.getFragmentAsLabel( new URI("http://example.com#123" ) ), "123" );
	assertEquals( onto.getFragmentAsLabel( new URI("http://example.com/aaaa#123" ) ), "123" );
	assertEquals( onto.getFragmentAsLabel( new URI("http://example.com/aaaa/#123" ) ), "123" );
	assertEquals( onto.getFragmentAsLabel( new URI("http://example.com/aaaa/123" ) ), "123" );
	assertEquals( onto.getFragmentAsLabel( new URI("http://example.com/aaaa/123/" ) ), "123" );
	assertEquals( onto.getFragmentAsLabel( new URI("http://example.com/aaaa/123/#" ) ), "" );
    }

    @Test(groups = { "full", "onto", "raw" }, dependsOnMethods = {"basicTest"})
    public void loadedTest() throws Exception {
	// load ontologies
	OntologyFactory.setDefaultFactory("fr.inrialpes.exmo.ontowrap.jena25.JENAOntologyFactory");
	URI u = new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl");
	ontology = OntologyFactory.getFactory().loadOntology(u);
	assertNotNull( ontology );
	assertTrue( ontology instanceof JENAOntology );
	LoadedOntology onto = (LoadedOntology)ontology;
	// Doing this now prevent from having problems in case of errors
	OntologyFactory.setDefaultFactory("fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3OntologyFactory");
	assertEquals( onto.nbEntities(), 42 );
	assertEquals( onto.nbClasses(), 13 );
	assertEquals( onto.nbProperties(), 29 );
	assertEquals( onto.nbDataProperties(), 25 );
	assertEquals( onto.nbObjectProperties(), 4 );
	assertEquals( onto.nbIndividuals(), 0 );
	// Various void tests
	onto.getEntities();
	onto.getClasses();
	onto.getProperties();
	onto.getDataProperties();
	onto.getObjectProperties();
	onto.getIndividuals();
	// Test class
	URI u2 = new URI("http://ebiquity.umbc.edu/v2.1/ontology/publication.owl#Publication");
	Object o = onto.getEntity( u2 );
	assertNotNull( o );
	assertEquals( onto.getEntityURI( o ), u2 );
	assertEquals( onto.getEntityName( o ), "Publication" );
	assertTrue( onto.isEntity( o ) );
	assertTrue( onto.isClass( o ) );
	assertTrue( !onto.isProperty( o ) );
	assertEquals( onto.getEntityNames( o ).size(), 1 );
	assertEquals( onto.getEntityComments( o ).size(), 0 );
	//assertEquals( onto.getEntityAnnotations( o ).size(), 1 );
	// Test property 
	u2 = new URI("http://ebiquity.umbc.edu/v2.1/ontology/publication.owl#author");
	o = onto.getEntity( u2 );
	assertNotNull( o );
	assertEquals( onto.getEntityURI( o ), u2 );
	assertEquals( onto.getEntityName( o ), "author" );
	assertTrue( onto.isEntity( o ) );
	assertTrue( !onto.isClass( o ) );
	assertTrue( onto.isProperty( o ) );
	assertTrue( !onto.isDataProperty( o ) );
	assertTrue( onto.isObjectProperty( o ) );
	onto.unload();
    }

    @Test(groups = { "full", "onto", "raw" }, dependsOnMethods = {"loadedTest"})
    @SuppressWarnings("unchecked") // see below
    public void heavyLoadedTest() throws Exception {
	// load ontologies
	OntologyFactory.setDefaultFactory("fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3OntologyFactory");
	URI u = new URI("file:examples/rdf/edu.umbc.ebiquity.publication.owl");
	ontology = OntologyFactory.getFactory().loadOntology(u);
	assertNotNull( ontology );
	assertTrue( ontology instanceof OWLAPI3Ontology );
	HeavyLoadedOntology onto = (HeavyLoadedOntology)ontology;
	// Special tests
	assertTrue( onto.getCapabilities( OntologyFactory.LOCAL, OntologyFactory.ASSERTED, OntologyFactory.NAMED ) );
	// Here insert what is supposed to be done with Jena...
	//for (Object o : onto.getEntities()) System.out.println(o);
	assertEquals( onto.nbEntities(), 42/*44*/ ); //44 is with owl:Thing
	assertEquals( onto.nbClasses(), 13/*15*/ );
	assertEquals( onto.nbProperties(), 29 );
	assertEquals( onto.nbDataProperties(), 25 );
	assertEquals( onto.nbObjectProperties(), 4 );
	assertEquals( onto.nbIndividuals(), 0 );
	// verfify that the nb methods return the same number that element in get methods
	assertEquals( onto.nbEntities(), onto.getEntities().size() );
	assertEquals( onto.nbClasses(), onto.getClasses().size() );
	assertEquals( onto.nbProperties(), onto.getProperties().size() );
	assertEquals( onto.nbDataProperties(), onto.getDataProperties().size() );
	assertEquals( onto.nbObjectProperties(), onto.getObjectProperties().size() );
	assertEquals( onto.nbIndividuals(), onto.getIndividuals().size() );	
	
	// Various void tests
	onto.getEntities();
	onto.getClasses();
	onto.getProperties();
	onto.getDataProperties();
	onto.getObjectProperties();
	onto.getIndividuals();
	// Test class
	URI u2 = new URI("http://ebiquity.umbc.edu/v2.1/ontology/publication.owl#Publication");
	Object o = onto.getEntity( u2 );
	assertNotNull( o );
	assertEquals( onto.getEntityURI( o ), u2 );
	assertEquals( onto.getEntityName( o ), "Publication" );
	assertTrue( onto.isEntity( o ) );
	assertTrue( onto.isClass( o ) );
	assertTrue( !onto.isProperty( o ) );
	assertEquals( onto.getEntityNames( o ).size(), 1 );
	assertEquals( onto.getEntityComments( o ).size(), 0 );
	assertEquals( onto.getEntityAnnotations( o ).size(), 1 );
	// Specific HeavyLoadedTests
	assertEquals( onto.getSubClasses( o, OntologyFactory.LOCAL, OntologyFactory.ASSERTED, OntologyFactory.NAMED ).size(), 10 );
	assertEquals( onto.getSubClasses( o, OntologyFactory.LOCAL, OntologyFactory.INHERITED, OntologyFactory.NAMED ).size(), 10 );
	assertEquals( onto.getSuperClasses( o, OntologyFactory.LOCAL, OntologyFactory.INHERITED, OntologyFactory.NAMED ).size(), 1 ); // Try another one
	assertEquals( onto.getProperties( o, OntologyFactory.LOCAL, OntologyFactory.ASSERTED, OntologyFactory.NAMED ).size(), 2 );
	assertEquals( onto.getObjectProperties( o, OntologyFactory.LOCAL, OntologyFactory.ASSERTED, OntologyFactory.NAMED ).size(), 1 );
	assertEquals( onto.getDataProperties( o, OntologyFactory.LOCAL, OntologyFactory.ASSERTED, OntologyFactory.NAMED ).size(), 1 );
	assertEquals( onto.getInstances( o, OntologyFactory.LOCAL, OntologyFactory.ASSERTED, OntologyFactory.NAMED ).size(), 0 );
	// Test property 
	u2 = new URI("http://ebiquity.umbc.edu/v2.1/ontology/publication.owl#author");
	o = onto.getEntity( u2 );
	assertNotNull( o );
	assertEquals( onto.getEntityURI( o ), u2 );
	assertEquals( onto.getEntityName( o ), "author" );
	assertTrue( onto.isEntity( o ) );
	assertTrue( !onto.isClass( o ) );
	assertTrue( onto.isProperty( o ) );
	assertTrue( !onto.isDataProperty( o ) );
	assertTrue( onto.isObjectProperty( o ) );
	// Specific HeavyLoadedTests
	assertEquals( onto.getSubProperties( o, OntologyFactory.LOCAL, OntologyFactory.ASSERTED, OntologyFactory.NAMED ).size(), 0 );
	assertEquals( onto.getSuperProperties( o, OntologyFactory.LOCAL, OntologyFactory.ASSERTED, OntologyFactory.NAMED ).size(), 0 );
	// a simple Set would suppress the warning but I like to understand
	Set<Object> scl = onto.getRange( o, OntologyFactory.ASSERTED );
	assertEquals( scl.size(), 1 );
	Object o2 = onto.getEntity( new URI( "http://ebiquity.umbc.edu/v2.1/ontology/person.owl#Person" ) );
	assertNotNull( o2 );
	assertTrue( scl.contains( o2 ) );
	scl = onto.getDomain( o, OntologyFactory.ASSERTED );
	assertEquals( scl.size(), 1 );
	o2 = onto.getEntity( new URI( "http://ebiquity.umbc.edu/v2.1/ontology/publication.owl#Resource" ) );
	assertNotNull( o2 );
	assertTrue( scl.contains( o2 ) );
	onto.unload();
    }

    @Test(groups = { "full", "onto", "raw" }, dependsOnMethods = {"heavyLoadedTest"})
    public void cleanUpTest() throws Exception {
	OntologyFactory.getFactory().clear();
	// Check if things remain or not...
    }

    @AfterClass(groups = { "onto", "raw", "full" }, alwaysRun = true )
    public void tearDown() throws Exception {
	OntologyFactory.setDefaultFactory("fr.inrialpes.exmo.ontowrap.owlapi30.OWLAPI3OntologyFactory");
    }

}
