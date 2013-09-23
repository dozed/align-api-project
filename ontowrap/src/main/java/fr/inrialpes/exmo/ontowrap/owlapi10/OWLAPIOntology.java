/*
 * $Id: OWLAPIOntology.java 896 2008-11-25 14:45:46Z jdavid $
 *
 * Copyright (C) INRIA, 2007-2008, 2010
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

/*
 * This should be turned into an HeavyLoadedOntology.
 * Some primitives are already avalible below
 *
 * The point is that these primitives may concern:
 * - Named entities / All entities
 * - Asserted information / Deduced information
 * - Inherited information... ()
 * In fact the OWL API does only provide asserted.
 *
 * This is not very well implemented: the OWL API mandates to implement this as visitors...
 */

package fr.inrialpes.exmo.ontowrap.owlapi10;

import java.net.URI;
import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.HashSet;

import fr.inrialpes.exmo.ontowrap.Annotation;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.BasicOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import fr.inrialpes.exmo.ontowrap.util.EntityFilter;

import org.semanticweb.owl.io.vocabulary.RDFSVocabularyAdapter;
import org.semanticweb.owl.model.OWLDataType;
import org.semanticweb.owl.model.OWLOntology;
import org.semanticweb.owl.model.OWLProperty;
import org.semanticweb.owl.model.OWLClass;
import org.semanticweb.owl.model.OWLObjectProperty;
import org.semanticweb.owl.model.OWLDataProperty;
import org.semanticweb.owl.model.OWLIndividual;
import org.semanticweb.owl.model.OWLEntity;
import org.semanticweb.owl.model.OWLRestriction;
import org.semanticweb.owl.model.OWLDescription;
import org.semanticweb.owl.model.OWLNaryBooleanDescription;
import org.semanticweb.owl.model.OWLCardinalityRestriction;
import org.semanticweb.owl.model.OWLException;
import org.semanticweb.owl.model.helper.OWLEntityCollector;

/**
 * Store the information regarding ontologies in a specific structure
 * Acts as an interface with regard to an ontology APY
 */

public class OWLAPIOntology extends BasicOntology<OWLOntology> implements HeavyLoadedOntology<OWLOntology> {

    /* For caching sizes */
    private int nbentities = -1;
    private int nbclasses = -1;
    private int nbproperties = -1;
    private int nbobjectproperties = -1;
    private int nbdataproperties = -1;
    private int nbindividuals = -1;

    public OWLAPIOntology() {
	setFormalism( "OWL1.0" );
	try {
	    setFormURI( new URI("http://www.w3.org/2002/07/owl#") );
	} catch (Exception e) {}; // does not happen
    };

    // -----------------------------------------------------------------
    // Ontology interface [//DONE]

    public OWLOntology getOntology() { return onto; }

    public void setOntology( OWLOntology o ) { this.onto = o; }

    // -----------------------------------------------------------------
    // LoadedOntology interface [//DONE]

    public Object getEntity( URI uri ) throws OntowrapException {
	try {
	    OWLEntity result = onto.getClass( uri );
	    if ( result == null ) result = onto.getDataProperty( uri );
	    if ( result == null ) result = onto.getObjectProperty( uri );
	    if ( result == null ) result = onto.getIndividual( uri );
	    return result;
	} catch (OWLException ex) {
	    throw new OntowrapException( "Cannot dereference URI : "+uri );
	}
    }

    public URI getEntityURI( Object o ) throws OntowrapException {
	try {
	    return ((OWLEntity)o).getURI();
	} catch (OWLException oex) {
	    throw new OntowrapException( "Cannot get URI ", oex );
	}
    }

    public String getEntityName( Object o ) throws OntowrapException {
	try {
	    // Try to get labels first...
	    URI u = ((OWLEntity)o).getURI();
	    if ( u != null ) return u.getFragment();
	    else return "";
	} catch (OWLException oex) {
	    return null;
	}
    };

    public String getEntityName( Object o, String lang ) throws OntowrapException {
	// Should first get the label in the language
	return getEntityName( o );
    }

    public Set<String> getEntityNames( Object o , String lang ) throws OntowrapException {
	try {
	    OWLEntity e = ((OWLEntity) o);
	    return getAnnotations(e,lang,RDFSVocabularyAdapter.INSTANCE.getLabel());
	} catch (OWLException oex) {
	    return null;
	}
    }
    public Set<String> getEntityNames( Object o ) throws OntowrapException {
	try {
	    OWLEntity e = ((OWLEntity) o);
	    return getAnnotations(e,null,RDFSVocabularyAdapter.INSTANCE.getLabel());
	} catch (OWLException oex) {
	    return null;
	}
    };


    public Set<String> getEntityComments( Object o , String lang ) throws OntowrapException {
	try {
	    OWLEntity e = ((OWLEntity) o);
	    return getAnnotations(e,lang,RDFSVocabularyAdapter.INSTANCE.getComment());
	} catch (OWLException oex) {
	    return null;
	}
    }

    public Set<String> getEntityComments( Object o ) throws OntowrapException {
	try {
	    OWLEntity e = ((OWLEntity) o);
	    return getAnnotations(e,null,RDFSVocabularyAdapter.INSTANCE.getComment());
	} catch (OWLException oex) {
	    return null;
	}
    }
    
    public Set<Annotation> getEntityAnnotationsL(Object o) throws OntowrapException {
	throw new UnsupportedOperationException();
    }

    protected Set<String> getAnnotations(final OWLEntity e , final String lang , final String typeAnnot ) throws OWLException {
	final OWLOntology o = this.onto;
	return new AbstractSet<String>() {
	    int size=-1;
	    public Iterator<String> iterator() {
		try {
		    return new OWLAPIAnnotIt(o,e,lang,typeAnnot);
		} catch (OWLException e) {
		    e.printStackTrace();
		    return null;
		}
	    }
	    public int size() {
		if (size==-1) {
		    for (String s : this) {
			size++;
		    }
		    size++;
		}
		return size;
	    }

	};
	// OLD IMPLEMENTATION
	/*Set<String> annots = new HashSet<String>();
	for (Object objAnnot :  e.getAnnotations(onto)) {
	    OWLAnnotationInstance annot = (OWLAnnotationInstance) objAnnot;
		String annotUri = annot.getProperty().getURI().toString();
		if (annotUri.equals(typeAnnot) || typeAnnot==null) {
        		if ( annot.getContent() instanceof OWLDataValue &&
        			( lang==null || ((OWLDataValue) annot.getContent()).getLang().equals(lang)) ) {
        		    annots.add(((OWLDataValue) annot.getContent()).getValue().toString());
        		}
		}
	}
	return annots;*/
    }

    public Set<String> getEntityAnnotations( Object o ) throws OntowrapException {
	try {
	    return getAnnotations(((OWLEntity) o),null,null);
	} catch (OWLException oex) {
	    return null;
	}
    }
    
    public Set<String> getEntityAnnotations(Object o, String lang) throws OntowrapException {
	try {
	    return getAnnotations(((OWLEntity) o),lang,null);
	} catch (OWLException oex) {
	    return null;
	}
    }

    public boolean isEntity( Object o ){
	return ( o instanceof OWLEntity );
    };
    public boolean isClass( Object o ){
	return ( o instanceof OWLClass );
    };
    public boolean isProperty( Object o ){
	return ( o instanceof OWLProperty );
    };
    public boolean isDataProperty( Object o ){
	return ( o instanceof OWLDataProperty );
    };
    public boolean isObjectProperty( Object o ){
	return ( o instanceof OWLObjectProperty );
    };
    public boolean isIndividual( Object o ){
	return ( o instanceof OWLIndividual );
    };

    // ***JE:
    // We should solve this issue, is it better to go this way or tu use the OWL API?
    // JD: allows to retrieve some specific entities by giving their class
    // This is not part of the interface...
    @SuppressWarnings("unchecked") // OWL API 1 untyped
    protected Set<?> getEntities(Class<? extends OWLEntity> c) throws OWLException{
        OWLEntityCollector ec = new OWLEntityCollector();
    	onto.accept(ec);
    	return new EntityFilter<OWLEntity>((Set<OWLEntity>) ec.entities(),this);
    	/*Set<Object> entities = new HashSet<Object>();
	for (Object obj : ec.entities()) {
	    // JD: OWLEntitytCollector seems to return anonymous entities :&& ((OWLEntity)obj).getURI()!=null
	    if (c.isInstance(obj) && ((OWLEntity) obj).getURI()!=null) { // &&((OWLEntity) obj).getURI().toString().startsWith(onto.getURI().toString()) ){
		entities.add(obj);
	    }
	}
	return entities;*/
    }

    // Here it shoud be better to report exception
    // JE: Onto this does not work at all, of course...!!!!
    public Set<?> getEntities() {
	try {
	    return getEntities(OWLEntity.class);
	} catch (OWLException ex) {
	    return null;
	}
    }

    public Set<?> getClasses() {
	try {
	    return getEntities(OWLClass.class);
	} catch (OWLException ex) {
	    return null;
	}
    }

    public Set<?> getProperties() {
	try {
	    return getEntities(OWLProperty.class);
	} catch (OWLException ex) {
	    return null;
	}
    }

    public Set<?> getDataProperties() {
	try {
	    // This first method returns also Properties not defined in the Onto
	    // i.e. properties having an namespace different from the ontology uri
	    return getEntities(OWLDataProperty.class);
	} catch (OWLException ex) {
	    return null;
	}
    }

    public Set<?> getObjectProperties() {
	try {
	    return getEntities(OWLObjectProperty.class);
	} catch (OWLException ex) {
	    return null;
	}
    }

    public Set<?> getIndividuals() {
	try {
	    return getEntities(OWLIndividual.class);
	} catch (OWLException ex) {
	    return null;
	}
    }

    public int nbEntities() {
	if ( nbentities != -1 ) return nbentities;
	nbentities = getEntities().size();
	return nbentities;
    }

    public int nbClasses() {
	if ( nbclasses != -1 ) return nbclasses;
	nbclasses = getClasses().size();
	return nbclasses;
    }

    public int nbProperties() {
	if ( nbproperties != -1 ) return nbproperties;
	nbproperties = nbObjectProperties()+nbDataProperties();
	return nbproperties;
    }

    public int nbObjectProperties() {
	if ( nbobjectproperties != -1 ) return nbobjectproperties;
	nbobjectproperties = getObjectProperties().size();
	return nbobjectproperties;

    }

    public int nbDataProperties() {
	if ( nbdataproperties != -1 ) return nbdataproperties;
	nbdataproperties = getDataProperties().size();
	return nbdataproperties;

    }

    public int nbIndividuals() {
	if ( nbindividuals != -1 ) return nbindividuals;
	nbindividuals = getIndividuals().size();
	return nbindividuals;

    }

    public void unload() {
	try {
	    onto.getOWLConnection().notifyOntologyDeleted( onto );
	} catch (OWLException ex) { System.err.println(ex); };
    }

    // -----------------------------------------------------------------
    // HeavyLoadedOntology interface [//TOCHECK]

    // [//TODO]
    public boolean getCapabilities( int Direct, int Asserted, int Named ){
	return true;
    }

    // Pretty inefficient but nothing seems to be stored
    public Set<Object> getSubClasses( Object cl, int local, int asserted, int named ){
	Set<Object> sbcl = new HashSet<Object>();
	for( Object c : getClasses() ) {
	    if ( getSuperClasses( (OWLClass)c, local, asserted, named ).contains( cl ) ) sbcl.add( c );
	}
	return sbcl;
    }
    public Set<Object> getSuperClasses( Object cl, int local, int asserted, int named ){
	Set<Object> spcl = new HashSet<Object>();
	if ( asserted == OntologyFactory.ASSERTED ){
	    try {
		for( Object rest : ((OWLClass)cl).getSuperClasses( getOntology() ) ){
		    if (rest instanceof OWLClass) spcl.add( rest );
		}
	    } catch (OWLException ex) {
	    }
	} else {
	    try {
		// JE: I do not feel that this is really correct
		Set<Object> sup = new HashSet<Object>();
		for( Object rest : ((OWLClass)cl).getSuperClasses( getOntology() ) ){
		    if (rest instanceof OWLClass) {
			spcl.add( rest );
			sup.add( rest );
		    }
		}
	    } catch (OWLException ex) {
	    };
	}
	return spcl;
    }

    /*
     * In the OWL API, there is no properties: there are SuperClasses which are restrictions
     */
    public Set<Object> getProperties( Object cl, int local, int asserted, int named ){
	Set<Object> prop = new HashSet<Object>();
	if ( asserted == OntologyFactory.ASSERTED && local == OntologyFactory.LOCAL ) {
	    try {
		for ( Object ent : ((OWLClass)cl).getSuperClasses( getOntology() ) ){
		    if ( ent instanceof OWLRestriction ) 
			prop.add( ((OWLRestriction)ent).getProperty() );
		}
	    } catch (OWLException e) { e.printStackTrace(); }
	} else {
	    prop = getInheritedProperties( (OWLClass)cl );
	}
	return prop;
    }
    // Not very efficient: 2n instead of n
    public Set<Object> getDataProperties( Object c, int local, int asserted, int named ){
	Set<Object> props = new HashSet<Object>();
	for ( Object p : getProperties( c, local, asserted, named )  ){
	    if ( p instanceof OWLDataProperty ) props.add( p );
	}
	return props;
    }
    // Not very efficient: 2n instead of n
    public Set<Object> getObjectProperties( Object c, int local, int asserted, int named ){
	Set<Object> props = new HashSet<Object>();
	for ( Object p : getProperties( c, local, asserted, named )  ){
	    if ( p instanceof OWLObjectProperty ) props.add( p );
	}
	return props;
    }
    // Pretty inefficient but nothing seems to be stored
    public Set<Object> getInstances( Object cl, int local, int asserted, int named  ){
	Set<Object> sbcl = new HashSet<Object>();
	try {
	    for( Object i : getIndividuals() ) {
		//if ( getClasses( (OWLIndividual)i, local, asserted, named ).contains( cl ) ) sbcl.add( i );
		if ( ((OWLIndividual)i).getTypes( getOntology() ).contains( cl ) ) sbcl.add( i );
	    }
	} catch (OWLException ex) {};
	return sbcl;
    }
    // Pretty inefficient but nothing seems to be stored
    public Set<Object> getSubProperties( Object pr, int local, int asserted, int named ){
	Set<Object> sbpr = new HashSet<Object>();
	for( Object p : getProperties() ) {
	    if ( getSuperProperties( (OWLProperty)p, local, asserted, named ).contains( pr ) ) sbpr.add( p );
	}
	return sbpr;
    }
    public Set<Object> getSuperProperties( Object pr, int local, int asserted, int named ){
	Set<Object> supers = new HashSet<Object>();
	if ( asserted == OntologyFactory.ASSERTED ){
	    try {
		for( Object rest : ((OWLProperty)pr).getSuperProperties( getOntology() ) ){
		    if (rest instanceof OWLProperty) supers.add( rest );
		}
	    } catch (OWLException ex) {
	    }
	} else {
	    try {
		Set<Object> sup = new HashSet<Object>();
		for( Object rest : ((OWLProperty)pr).getSuperProperties( getOntology() ) ){
		    if (rest instanceof OWLProperty) {
			sup.add( rest );
			supers.add( rest );
		    }
		}
	    } catch (OWLException ex) {
	    };
	}
	return supers;
    }
    public Set<Object> getRange( Object p, int asserted ){
	Set<Object> resultSet = new HashSet<Object>(); 
	try {
	    for ( Object ent : ((OWLProperty)p).getRanges( getOntology() ) ){
		// Not correct
		// Could be something else than class
		if ( ent instanceof OWLClass || ent instanceof OWLDataType ) {
		    resultSet.add( ent );
		}
	    }
	} catch (OWLException ex) {};
	return resultSet;
    }

    public Set<Object> getDomain( Object p, int asserted ){
	Set<Object> resultSet = new HashSet<Object>(); 
	try {
	    for ( Object ent : ((OWLProperty)p).getDomains( getOntology() ) ){
		// Not correct
		// Could be something else than class
		if ( ent instanceof OWLClass ) {
		    resultSet.add( ent );
		}
	    }
	} catch (OWLException ex) {};
	return resultSet;
    }

    @SuppressWarnings("unchecked") // OWL API 1 untyped
    public Set<Object> getClasses( Object i, int local, int asserted, int named ){
	Set<Object> supers = null;
	try {
	    supers = ((OWLIndividual)i).getTypes( getOntology() );
	} catch (OWLException ex) {};
	if ( local == OntologyFactory.LOCAL ) {
	    return supers; 
	} else {
	    // inherits the superclasses (unless we have to reduce them...)
	    return supers;
	}
    }

    /**
     * returns emptyset in case of error (e.g., if p is a property)
     */
    public Set<Object> getCardinalityRestrictions( Object p ){
	Set<Object> spcl = new HashSet<Object>();
	try {
	    for( Object rest : ((OWLClass)p).getSuperClasses( getOntology() ) ){
		// This should be filtered
		if (rest instanceof OWLCardinalityRestriction) spcl.add( rest );
	    }
	} catch (OWLException ex) {
	};
	return spcl;
    }

    /**
     * Inherits all properties of a class
     */
    private Set<Object> getInheritedProperties( OWLClass cl ) {
	Set<Object> resultSet = new HashSet<Object>(); 
	try { getProperties( cl, resultSet ); }
	catch (OWLException ex) {};
	return resultSet;
    }

    /* This traverse properties */
    public void getProperties( OWLDescription desc, Set<Object> list) throws OWLException {
	if ( desc instanceof OWLRestriction ){
	    //getProperties( (OWLRestriction)desc, list );
	    list.add( ((OWLRestriction)desc).getProperty() );
	} else if ( desc instanceof OWLClass ) {
	    getProperties( (OWLClass)desc, list );
	} else if ( desc instanceof OWLNaryBooleanDescription ) {
	    for ( Object d : ((OWLNaryBooleanDescription)desc).getOperands() ){
		getProperties( (OWLDescription)d, list );
	    }
	    //getProperties( (OWLNaryBooleanDescription)desc, list );
	}
    }
    public void getProperties( OWLRestriction rest, Set<Object> list) throws OWLException {
	list.add( (Object)rest.getProperty() );
    }
    public void getProperties( OWLNaryBooleanDescription d, Set<Object> list) throws OWLException {
	for ( Iterator it = d.getOperands().iterator(); it.hasNext() ;){
	    getProperties( (OWLDescription)it.next(), list );
	}
    }
    public void getProperties( OWLClass cl, Set<Object> list) throws OWLException {
	for ( Object desc : cl.getSuperClasses( getOntology() ) ){
	    getProperties( (OWLDescription)desc, list );
	}
	// JE: I suspect that this can be a cause for looping!!
	for ( Object desc : cl.getEquivalentClasses( getOntology() ) ){
	    getProperties( (OWLDescription)desc, list );
	}
    }



}
