/*
 * $Id: SKOSLiteThesaurus.java 1681 2012-02-16 10:11:59Z euzenat $
 *
 * Copyright (C) INRIA, 2008-2010, 2012
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

package fr.inrialpes.exmo.ontowrap.skoslite;

import java.net.URI;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.rdf.model.InfModel;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.ResIterator;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;

import fr.inrialpes.exmo.ontowrap.Annotation;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public class SKOSLiteThesaurus implements HeavyLoadedOntology<Model> {
    
    protected final static String SKOS_ONTO = SKOSLiteThesaurus.class.getClassLoader().getResource("fr/inrialpes/exmo/ontowrap/skoslite/skos.rdf").toString();

    protected final static String SKOS_NS="http://www.w3.org/2004/02/skos/core#";
    protected final static String SKOS_CONCEPT=SKOS_NS+"Concept";
    protected final static String SKOS_PREFLABEL=SKOS_NS+"prefLabel";
    protected final static String SKOS_NOTE=SKOS_NS+"note";
    //protected final static String SKOS_SCOPENOTE=SKOS_NS+"scopeNote";
    protected final static String SKOS_NOTATION=SKOS_NS+"notation";
    protected final static String SKOS_BROADERTRANSITIVE=SKOS_NS+"broaderTransitive";
    protected final static String SKOS_NARROWERTRANSITIVE=SKOS_NS+"narrowerTransitive";
    
    protected InfModel ontoInf;
    protected Model onto;
    
    protected String formalism;
    protected URI formUri;
    protected URI file;
    protected URI uri;
    
    public SKOSLiteThesaurus(URI file) {
	setFile(file);
    }
    
    public SKOSLiteThesaurus(Model onto) {
	this.setOntology(onto);
    }

    @Override
    public boolean getCapabilities(int Direct, int Asserted, int Named) throws OntowrapException {
	// TODO Auto-generated method stub
	return true;
    }

    /**
     * returns empty set
     */
    public Set<? extends Object> getClasses(Object i, int local, int asserted, int named) throws OntowrapException {
	return Collections.emptySet();
    }

    /**
     * returns empty set
     */
    public Set<? extends Object> getDataProperties(Object c, int local, int asserted, int named) throws OntowrapException {
	return Collections.emptySet();
    }

    /**
     * returns empty set
     */
    public Set<? extends Object> getDomain(Object p, int asserted) throws OntowrapException {
	return Collections.emptySet();
    }

    /**
     * returns empty set
     */
    public Set<? extends Object> getInstances(Object c, int local, int asserted, int named) throws OntowrapException {
	return Collections.emptySet();
    }

    /**
     * returns empty set
     */
    public Set<? extends Object> getObjectProperties(Object c, int local, int asserted, int named) throws OntowrapException {
	return Collections.emptySet();
    }

    /**
     * returns empty set
     */
    public Set<? extends Object> getProperties(Object c, int local, int asserted, int named) throws OntowrapException {
	return Collections.emptySet();
    }

    /**
     * returns empty set
     */
    public Set<? extends Object> getRange(Object p, int asserted) throws OntowrapException {
	return Collections.emptySet();
    }
    
    /**
     * returns empty set
     */
    public Set<? extends Object> getSubProperties(Object p, int local, int asserted, int named) throws OntowrapException {
	return Collections.emptySet();
    }
    
   
    /**
     * returns empty set
     */
    public Set<? extends Object> getSuperProperties(Object p, int local, int asserted, int named) throws OntowrapException {
	return Collections.emptySet();
    }


    /**
     * returns all sub concepts of given object c.
     * 
     */
    public Set<? extends Object> getSubClasses(Object c, int local, int asserted, int named) {
	HashSet<Object> sub = new HashSet<Object>(); 
	//System.out.println(c);
	StmtIterator it =ontoInf.listStatements(null,ontoInf.getProperty(SKOS_BROADERTRANSITIVE),(Resource) c);
	while ( it.hasNext() ) {
	    Statement st = it.next();
	    sub.add( st.getSubject() );
	}
	it =ontoInf.listStatements((Resource) c,ontoInf.getProperty(SKOS_NARROWERTRANSITIVE),(RDFNode)null);
	while (it.hasNext()) {
	    Statement st = it.next();
	    sub.add( st.getObject() );
	}
	return sub;
    }

    @Override
    public Set<? extends Object> getSuperClasses(Object c, int local, int asserted, int named) throws OntowrapException {
	HashSet<Object> sub = new HashSet<Object>(); 
	StmtIterator it =ontoInf.listStatements(null,ontoInf.getProperty(SKOS_NARROWERTRANSITIVE),(Resource) c);
	while (it.hasNext()) {
	    Statement st = it.next();
	    sub.add(st.getSubject());
	}
	it =ontoInf.listStatements((Resource) c,ontoInf.getProperty(SKOS_BROADERTRANSITIVE),(RDFNode)null);
	while (it.hasNext()) {
	    Statement st = it.next();
	    sub.add(st.getObject());
	}
	return sub;
    }

   

    
    public Set<? extends Object> getClasses() throws OntowrapException {
	ResIterator resIt = ontoInf.listSubjectsWithProperty(RDF.type, ontoInf.getResource(SKOS_CONCEPT));
	return resIt.toSet();
    }

    @Override
    public Set<? extends Object> getDataProperties() throws OntowrapException {
	return Collections.emptySet();
    }

    @Override
    public Set<? extends Object> getEntities() throws OntowrapException {
	return this.getClasses();
    }

    @Override
    public Object getEntity(URI u) throws OntowrapException {
	try {
	    Object o = ontoInf.getRawModel().getResource(u.toString());
	    if (! ontoInf.contains((Resource) o, RDF.type, ontoInf.getResource(SKOS_CONCEPT))) {
		return null;//System.out.println(u+" : "+o);
	    }
	    
	    return o;
	} catch (Exception e) {
	    throw new OntowrapException("No Object for URI "+u);
	}
    }

    public Set<String> getEntityAnnotations(Object o, String lang, String[] types) throws OntowrapException {
	HashSet<String> annots=new HashSet<String>();
	ExtendedIterator<RDFNode> it=null;
	for (String t : types) {
	    if (it==null) 
		it=ontoInf.listObjectsOfProperty((Resource) o,ontoInf.getProperty(t));
	    else 
		it=it.andThen(ontoInf.listObjectsOfProperty((Resource) o,ontoInf.getProperty(t)));
	}
	while (it.hasNext()) {
	    Node n = it.next().asNode();
	    if (n.isLiteral() && (lang==null || lang.equals(n.getLiteralLanguage()))) {
		//System.out.println(n.getLiteralLexicalForm());
		annots.add(n.getLiteralLexicalForm());
	    }
	}
	return annots;
    }
    
    public Set<Annotation> getEntityAnnotationsL(Object o, String[] types) throws OntowrapException {
	Set<Annotation> annots=new HashSet<Annotation>();
	ExtendedIterator<RDFNode> it=null;
	for (String t : types) {
	    if (it==null) 
		it=ontoInf.listObjectsOfProperty((Resource) o,ontoInf.getProperty(t));
	    else 
		it=it.andThen(ontoInf.listObjectsOfProperty((Resource) o,ontoInf.getProperty(t)));
	}
	while (it.hasNext()) {
	    Node n = it.next().asNode();
	    if (n.isLiteral()) {
		//System.out.println(n.getLiteralLexicalForm());
		annots.add(new Annotation(n.getLiteralLexicalForm(),n.getLiteralLanguage()));
	    }
	}
	return annots;
    }
    
    @Override
    public Set<Annotation> getEntityAnnotationsL(Object o) throws OntowrapException {
	return getEntityAnnotationsL(o,new String[]{RDFS.label.toString(),SKOS_NOTE,SKOS_NOTATION});
    }
    
    public Set<String> getEntityAnnotations(Object o) throws OntowrapException {
	return getEntityAnnotations(o,null);
    }

    public Set<String> getEntityAnnotations(Object o, String lang) throws OntowrapException {
	return getEntityAnnotations(o,lang,new String[]{RDFS.label.toString(),SKOS_NOTE,SKOS_NOTATION});
    }

    public Set<String> getEntityComments(Object o, String lang) throws OntowrapException {
	return getEntityAnnotations(o,lang,new String[]{SKOS_NOTE});
    }

    public Set<String> getEntityComments(Object o) throws OntowrapException {
	return getEntityComments(o,null);
    }

    /**
     * returns one of the skos:prefLabel
     * In skos there at most one prefLabel for a given language
     */
    public String getEntityName(Object o) throws OntowrapException {
	return getEntityName(o,null);
    }

    /**
     * returns the skos:prefLabel for the given language.
     * In skos there at most one prefLabel for a given language
     */
    public String getEntityName(Object o, String lang) throws OntowrapException {
	try {
	    return getEntityAnnotations(o,lang,new String[]{SKOS_PREFLABEL}).iterator().next();
	}
	catch (Exception e) {
	    throw new OntowrapException("No skos:prefLabel for "+o);
	}
    }

    /**
     * returns all the labels, i.e. rdf:label subproperties for a given language
     * In skos, the properties skos:prefLabel, skos:altLabel and skos:hiddenLabel are subproperties of rdf:label
     */
    public Set<String> getEntityNames(Object o, String lang) throws OntowrapException {
	return getEntityAnnotations(o,lang,new String[]{RDFS.label.toString()});
    }

    /**
     * returns all the labels, i.e. rdf:label subproperties
     * In skos, the properties skos:prefLabel, skos:altLabel and skos:hiddenLabel are subproperties of rdf:label
     */
    public Set<String> getEntityNames(Object o) throws OntowrapException {
	return getEntityNames(o,null);
    }

   
    
    public URI getEntityURI(Object o) throws OntowrapException {
	try {
	    return URI.create(((Resource) o).getURI());
	}
	catch (Exception e) {
	    throw new OntowrapException("No URI for "+o);
	}
    }

    /**
     * No individuals for skos. returns empty set
     */
    public Set<? extends Object> getIndividuals() throws OntowrapException {
	return Collections.emptySet();
    }

    /**
     * No object property for skos. returns empty set
     */
    public Set<? extends Object> getObjectProperties() throws OntowrapException {
	return Collections.emptySet();
    }

    /**
     * No property for skos. returns empty set
     */
    public Set<? extends Object> getProperties() throws OntowrapException {
	return Collections.emptySet();
    }

    /**
     * returns true if the given object is an instance of skos:Concept
     */
    public boolean isClass(Object o) throws OntowrapException {
	return ontoInf.contains((Resource) o, RDF.type, ontoInf.getResource(SKOS_CONCEPT));
    }

    
    /**
     * returns false
     */
    public boolean isDataProperty(Object o) throws OntowrapException {
	// TODO Auto-generated method stub
	return false;
    }

    /**
     * returns false
     */
    public boolean isEntity(Object o) throws OntowrapException {
	// TODO Auto-generated method stub
	return false;
    }

    /**
     * returns false
     */
    public boolean isIndividual(Object o) throws OntowrapException {
	// TODO Auto-generated method stub
	return false;
    }

    /**
     * returns false
     */
    public boolean isObjectProperty(Object o) throws OntowrapException {
	// TODO Auto-generated method stub
	return false;
    }

    /**
     * returns false
     */
    public boolean isProperty(Object o) throws OntowrapException {
	// TODO Auto-generated method stub
	return false;
    }

    /**
     * returns the number of skos:Concept in the thesaurus
     */
    public int nbClasses() throws OntowrapException {
	return this.getClasses().size();
    }

    /**
     * returns 0
     */
    public int nbDataProperties() throws OntowrapException {
	// TODO Auto-generated method stub
	return 0;
    }

    /**
     * returns the same number than nbClasses()
     */
    public int nbEntities() throws OntowrapException {
	return this.getClasses().size();
    }

    /**
     * returns 0
     */
    public int nbIndividuals() throws OntowrapException {
	// TODO Auto-generated method stub
	return 0;
    }

    /**
     * returns 0
     */
    public int nbObjectProperties() throws OntowrapException {
	// TODO Auto-generated method stub
	return 0;
    }

    /**
     * returns 0
     */
    public int nbProperties() throws OntowrapException {
	// TODO Auto-generated method stub
	return 0;
    }

    public void unload() throws OntowrapException {
	ontoInf=null;
	onto=null;
    }

    public URI getFile() {
	return file;
    }

    public URI getFormURI() {
	return formUri;
    }

    public String getFormalism() {
	return formalism;
    }

    public Model getOntology() {
	return onto;
    }

    @Override
    public URI getURI() {
	return uri;
    }

    public void setFile(URI file) {
	this.file=file;
	onto=ModelFactory.createDefaultModel();
	onto.read(file.toString());
	onto.read(SKOS_ONTO);
	this.ontoInf=ModelFactory.createRDFSModel(onto);
	
    }

    public void setFormURI(URI u) {
	formUri=u;
    }

    public void setFormalism(String name) {
	formalism=name;
    }

    public void setOntology(Model o) {
	onto=o;
	onto.read(SKOS_ONTO);
	this.ontoInf=ModelFactory.createRDFSModel(onto);
	
    }

    public void setURI(URI uri) {
	this.uri=uri;
    }
}
