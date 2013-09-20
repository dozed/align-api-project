/*
 * $Id: SKOSThesaurus.java 1681 2012-02-16 10:11:59Z euzenat $
 *
 * Copyright (C) INRIA, 2009-2010, 2012
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

package fr.inrialpes.exmo.ontowrap.skosapi;

import java.net.URI;
import java.util.AbstractSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.semanticweb.skos.SKOSDataFactory;
import org.semanticweb.skos.SKOSDataset;
import org.semanticweb.skos.SKOSAnnotation;
import org.semanticweb.skos.SKOSEntity;
import org.semanticweb.skos.SKOSConcept;
import org.semanticweb.skos.SKOSLiteral;
import org.semanticweb.skos.SKOSUntypedLiteral;
import org.semanticweb.skos.SKOSDataProperty;
import org.semanticweb.skos.SKOSObjectRelationAssertion;
import org.semanticweb.skos.properties .SKOSNarrowerProperty;
import org.semanticweb.skos.properties .SKOSBroaderProperty;

import fr.inrialpes.exmo.ontowrap.Annotation;
import fr.inrialpes.exmo.ontowrap.BasicOntology;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public class SKOSThesaurus extends BasicOntology<SKOSDataset> implements HeavyLoadedOntology<SKOSDataset>{

    SKOSDataFactory factory;

    private static HashSet<Object> NullSet = new HashSet<Object>();

    public SKOSThesaurus () {
	NullSet = new HashSet<Object>();
    }

    public void setFactory( SKOSDataFactory df ){
	factory = df; // new SKOSDataFactoryImpl();
    }

    /**
     * JE: I had not followed for a while but now SKOS is an OWL ontology that 
     * can be interpreted in many ways. Hence a SKOS terminology is an OWL Full
     * ontology (the S is for Simple).
     *
     * Translation from SKOS to OWL (by Antoine Isaac):
     * skos:Concept --> owl:Class
     * skos:broader --> rdfs:subClassOf
     * skos:prefLabel, skos:altLabel, skos:hiddenLabel --> rdfs:label
     * skos:notes, skos:definition, skos:scopeNote --> rdfs:comments
     * skos:related --> rdfs:seeAlso [ignored]
     *
     * OK So what is in the SKOS Data model, since this is the only one that we
     * will take into account...
     * [SPEC: ]: 12/07/2009
     * Object properties: skos:semanticRelation, skos:broader, skos:narrower, skos:related, skos:broaderTransitive and skos:narrowerTransitive (the transitive are obtained by the transitive closure of the "direct") braoder is the inverse of narrower.
     * Data properties (all with lang): skos:notation
     * Annotation properties: skos:hiddenLabel, skos:prefLabel, skos:altLabel
     * skos:note, skos:changeNote, skos:definition, skos:editorialNote, skos:example, skos:historyNote and skos:scopeNote
     * Other object relations: skos:mappingRelation, skos:closeMatch, skos:exactMatch, skos:broadMatch, skos:narrowMatch and skos:relatedMatch
     *
     *
    **/

    /**

onto.getSKOSDataRelationAssertions(concept)
assertion.getSKOSObject();
if (literal.isTyped()) {
SKOSTypedLiteral typedLiteral = literal.getAsSKOSTypedLiteral();
System.out.println("\t\t" + assertion.getSKOSProperty().getURI().getFragment() + " " + literal.getLiteral() + " Type:" + typedLiteral.getDataType().getURI() );
} else {
SKOSUntypedLiteral untypedLiteral = literal.getAsSKOSUntypedLiteral();
if (untypedLiteral.hasLang()) {
lang = untypedLiteral.getLang();
}}
     */


    public void getDataValues( SKOSConcept o, SKOSDataProperty p, Set<String> result ){
	for ( SKOSLiteral lit : onto.getSKOSDataRelationByProperty( o, p ) ){
                result.add( lit.getLiteral() );
	}
    }
		  
    public void getDataValues( SKOSConcept o, SKOSDataProperty p, Set<String> result, String lang ){
	for ( SKOSLiteral lit : onto.getSKOSDataRelationByProperty( o, p ) ){
	    if ( !lit.isTyped() ) {
		SKOSUntypedLiteral l = lit.getAsSKOSUntypedLiteral();
		if ( l.hasLang() && l.getLang().equals( lang ) )
		    result.add( lit.getLiteral() );
	    } // JE: what if typed?
	}
    }
		  
    /**
       We should document:
       name < names 
       annotations
       comments(lang) < comments
     **/

    // NEARLY DONE
    /**
     * returns one of the prefLabel property values for a given SKOS concept.
     * @param o the entity
     * @return a label
     * @throws OntowrapException
     * JE// This is not satisfying because in case of several PrefLabels it will return the first one...
     */
    public String getEntityName( Object o ) throws OntowrapException {
	Set<String> result = new HashSet<String>();
	getDataValues( (SKOSConcept)o, factory.getSKOSPrefLabelProperty(), result );
	if ( result.size() > 0 ) return result.iterator().next();
	else return getFragmentAsLabel( getEntityURI( o ) );
    }

    // NEARLY DONE
    /**
     * returns one of the prefLabel property values for a given SKOS concept in a given language.
     * @param o the entity
     * @param lang the code of the language ("en", "fr", "es", etc.) 
     * @return a label
     * @throws OntowrapException
     * JE// This is not satisfying because in case of several PrefLabels it will return the first one...
     */
    public String getEntityName( Object o, String lang ) throws OntowrapException {
	Set<String> result = new HashSet<String>();
	getDataValues( (SKOSConcept)o, factory.getSKOSPrefLabelProperty(), result, lang );
	if ( result.size() > 0 ) return result.iterator().next();
	else return getEntityName( o );
    }

    /**
     * Returns the values of the prefLabel, hiddenLabel and altLabel properties in a given language.
     * @param o the entity
     * @param lang the code of the language ("en", "fr", "es", etc.) 
     * @return the set of labels
     * @throws OntowrapException
     */
    public Set<String> getEntityNames( Object o, String lang ) throws OntowrapException {
	Set<String> result = new HashSet<String>();
	getDataValues( (SKOSConcept)o, factory.getSKOSPrefLabelProperty(), result, lang );
	getDataValues( (SKOSConcept)o, factory.getSKOSAltLabelProperty(), result, lang );
	getDataValues( (SKOSConcept)o, factory.getSKOSHiddenLabelProperty(), result, lang );
	return result;
    }

    /**
     * Returns the values of the prefLabel, hiddenLabel and altLabel properties.
     * @param o the concept
     * @return the set of labels
     * @throws OntowrapException
     */
    public Set<String> getEntityNames(Object o) throws OntowrapException {
	Set<String> result = new HashSet<String>();
	getDataValues( (SKOSConcept)o, factory.getSKOSPrefLabelProperty(), result );
	getDataValues( (SKOSConcept)o, factory.getSKOSAltLabelProperty(), result );
	getDataValues( (SKOSConcept)o, factory.getSKOSHiddenLabelProperty(), result );
	if ( result.size() == 0 ) result.add( getFragmentAsLabel( getEntityURI( o ) ) );
	return result;
    }

    // TODO
    /**
     * Returns the values of the "rdfs:comment" property for a given entity and for a given natural language (attribute xml:lang).
     * @param o the entity
     * @param lang the code of the language ("en", "fr", "es", etc.) 
     * @return the set of comments
     * @throws OntowrapException
     */
    public Set<String> getEntityComments(Object o, String lang) throws OntowrapException {
	Set<String> comments = new HashSet<String>();
	/*
	OntResource or = (OntResource) o;
	Iterator<?> i = or.listComments(lang);
	while (i.hasNext()) {
	    String comment = ((LiteralImpl) i.next()).getLexicalForm();
	    comments.add(comment);
	}
	*/
	return comments;
    }
    
    public Set<Annotation> getEntityAnnotationsL(Object o) throws OntowrapException {
	throw new UnsupportedOperationException();
    }

    // TODO
    /**
     * Returns all the values of the "rdfs:comment" property for a given entity
     * @param o the entity
     * @return the set of comments
     * @throws OntowrapException
     */
    public Set<String> getEntityComments( Object o ) throws OntowrapException {
	return getEntityComments(o,null);
    }

    /**
     * Returns all the values of the "owl:AnnotationProperty" property for a given entity. 
     * These annotations are those predefined in owl (owl:versionInfo, rdfs:label, rdfs:comment, rdfs:seeAlso and rdfs:isDefinedBy)
     * In SKOS, they also are:  skos:notation
     * Annotation properties: 
     * skos:note, skos:changeNote, skos:definition, skos:editorialNote, 
     * skos:example, skos:historyNote and skos:scopeNote
     * but also all other defined annotation properties which are subClass of "owl:AnnotationProperty"
     * @param o the entity
     * @return the set of annotation values
     * @throws OntowrapException
     */
    public Set<String> getEntityAnnotations( Object o ) throws OntowrapException {
	Set<String> annots = new HashSet<String>();
	for( SKOSAnnotation ann : ((SKOSConcept)o).getSKOSAnnotations( onto ) ){
	    if( ann.isAnnotationByConstant() ){
		SKOSLiteral lit = ann.getAnnotationValueAsConstant();
		if( (lit != null) && !(lit.isTyped()) ){
		    annots.add( lit.getAsSKOSUntypedLiteral().toString() );
		}
	    }
	}
	return annots;
    }

    /**
     * There is no languages on annotations in SKOS API
     * Hence we return all of them
     */
    public Set<String> getEntityAnnotations( Object o, String lang ) throws OntowrapException {
	return getEntityAnnotations( o );
    }

    public Object getEntity( URI u ) throws OntowrapException {
	return factory.getSKOSConcept( u );
    }
    
    public URI getEntityURI( Object o ) throws OntowrapException {
	if ( o instanceof SKOSConcept ) {
	    return ((SKOSConcept)o).getURI();
	} else {
	    throw new OntowrapException( o+" is not a SKOSConcept" );
	}
    }

    public Set<?> getClasses() {
	return onto.getSKOSConcepts();
    }

    public Set<?> getDataProperties() {
	return NullSet;
    }

    public Set<?> getEntities() {
	return getClasses();
    }

    public Set<?> getIndividuals() {
	return NullSet;
    }

    public Set<?> getObjectProperties() {
	return NullSet;
    }

    public Set<?> getProperties() {
	return NullSet;
    }

    public boolean isClass(Object o) {
	return o instanceof SKOSConcept;
    }

    public boolean isDataProperty(Object o) {
	return false;
    }

    public boolean isEntity(Object o) {
	return isClass(o);
    }

    public boolean isIndividual(Object o) {
	return false;
    }

    public boolean isObjectProperty(Object o) {
	return false;
    }

    public boolean isProperty(Object o) {
	return false;
    }

    public int nbEntities() {
	return this.getEntities().size();
    }

    public int nbClasses() {
	return this.getClasses().size();
    }

    public int nbDataProperties() {
	return 0;
    }

    public int nbIndividuals() {
	return 0;
    }

    public int nbObjectProperties() {
	return 0;
    }

    public int nbProperties() {
	return 0;
    }

    public void unload() {
    }

    /** THESE ARE HEAVY LOADED PRIMITIVES
     **/
    /* Capability methods */
    //    TODO
    public boolean getCapabilities( int Direct, int Asserted, int Named ) {
	return false;
    }

    /* Class methods */
    // TOIMPROVE: This is really terrible (JE)
    public Set<Object> getSubClasses( Object c, int local, int asserted, int named ) {
	//c.getSKOSNarrowerTransitiveConcepts( onto )
	//return ((SKOSConcept)c).getSKOSNarrowerConcepts( onto );
	Set<Object> result = new HashSet<Object>();
	for ( SKOSObjectRelationAssertion trp : ((SKOSConcept)c).getObjectRelationAssertions( onto ) ) {
	    if ( trp.getSKOSProperty() instanceof SKOSNarrowerProperty 
		 && trp.getSKOSObject() instanceof SKOSConcept ) result.add( trp.getSKOSObject() );
	}
	return result;
    }
    // TOIMPROVE
    public Set<Object> getSuperClasses( Object c, int local, int asserted, int named ){
	//o.getSKOSBroaderTransitiveConcepts( onto )
	//return ((SKOSConcept)c).getSKOSBroaderConcepts( onto );
	Set<Object> result = new HashSet<Object>();
	for ( SKOSObjectRelationAssertion trp : ((SKOSConcept)c).getObjectRelationAssertions( onto ) ) {
	    if ( trp.getSKOSProperty() instanceof SKOSBroaderProperty 
		 && trp.getSKOSObject() instanceof SKOSConcept ) result.add( trp.getSKOSObject() );
	}
	return result;
    }
    public Set<Object> getProperties( Object c, int local, int asserted, int named ){
	return NullSet;
    }
    public Set<Object> getDataProperties( Object c, int local, int asserted, int named ){
	return NullSet;
    }
    public Set<Object> getObjectProperties( Object c, int local, int asserted, int named ){
	return NullSet;
    }
    public Set<Object> getInstances( Object c, int local, int asserted, int named  ){
	return NullSet;
    }

    /* Property methods */
    public Set<Object> getSubProperties( Object p, int local, int asserted, int named ){
	return NullSet;
    }
    public Set<Object> getSuperProperties( Object p, int local, int asserted, int named ){
	return NullSet;
    }
    public Set<Object> getRange( Object p, int asserted ){
	return NullSet;
    }
    public Set<Object> getDomain( Object p, int asserted ){
	return NullSet;
    }

    /* Individual methods */
    public Set<Object> getClasses( Object i, int local, int asserted, int named ){
	return NullSet;
    }


}
