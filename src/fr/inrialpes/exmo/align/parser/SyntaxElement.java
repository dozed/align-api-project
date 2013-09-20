/*
 * $Id: SyntaxElement.java 1750 2012-07-16 21:00:01Z euzenat $
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Copyright (C) 2005 Digital Enterprise Research Insitute (DERI) Galway
 * Sourceforge version 1.6 - 2008 - then OmwgElement
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

package fr.inrialpes.exmo.align.parser;

import java.util.HashMap;
import java.util.Map;

import fr.inrialpes.exmo.align.impl.Namespace;

import org.semanticweb.owl.align.AlignmentException;

/**
 * <p>
 * Defines all the elements which might show up in a mapping document. Here are
 * also the string representations for the different elements defined.
 * </p>
 * <p>
 * $Id: SyntaxElement.java 1750 2012-07-16 21:00:01Z euzenat $
 * </p>
 * 
 * @author richi
 * @version $Revision: 1.6 $
 */

public enum SyntaxElement {

    // RDF NAMESPACE
    RDF(                Namespace.RDF, "RDF", true), // This is not a resource, nor a property
	RDF_ID(         Namespace.RDF, "ID"),
	RDF_ABOUT(      Namespace.RDF, "about"),
	RDF_DATATYPE(   Namespace.RDF, "datatype"),
	RDF_PARSETYPE(  Namespace.RDF, "parseType"),
	RDF_RESOURCE(   Namespace.RDF, "resource"),

    // DUBLINCORE NAMESPACE
	CREATOR(        Namespace.DUBLIN_CORE, "creator"),
	DATE(           Namespace.DUBLIN_CORE, "date"),
	IDENTIFIER(     Namespace.DUBLIN_CORE, "identifier"),

    // ALIGNMENT NAMESPACE
        ALIGNMENT(      Namespace.ALIGNMENT, "Alignment", true),
	ALID(           Namespace.ALIGNMENT, "id"),
	CELL(           Namespace.ALIGNMENT, "Cell", true),
	CERTIFICATE(    Namespace.ALIGNMENT, "certificate"),
	FORMALISM(      Namespace.ALIGNMENT, "Formalism", true),
	FORMATT(        Namespace.ALIGNMENT, "formalism"),
	CELLID(         Namespace.ALIGNMENT, "id"),
	LEVEL(          Namespace.ALIGNMENT, "level"),
	LIMITATIONS(    Namespace.ALIGNMENT, "limitations"),
	MAP(            Namespace.ALIGNMENT, "map"),
	MAPPING_SOURCE( Namespace.ALIGNMENT, "onto1"),
	MAPPING_TARGET( Namespace.ALIGNMENT, "onto2"),
	MEASURE(        Namespace.ALIGNMENT, "measure"),
	METHOD(         Namespace.ALIGNMENT, "method"),
	NAME(           Namespace.ALIGNMENT, "name"),
	PURPOSE(        Namespace.ALIGNMENT, "purpose"),
	RULE_RELATION(  Namespace.ALIGNMENT, "relation"),
	SEMANTICS(      Namespace.ALIGNMENT, "semantics"),
	ENTITY1(        Namespace.ALIGNMENT, "entity1"),
	ENTITY2(        Namespace.ALIGNMENT, "entity2"),
	TIME(           Namespace.ALIGNMENT, "time"),
	TYPE(           Namespace.ALIGNMENT, "type"),
	URI(            Namespace.ALIGNMENT, "uri"),
 	XML(            Namespace.ALIGNMENT, "xml"),
	ONTOLOGY(       Namespace.ALIGNMENT, "Ontology", true ),
	LOCATION(       Namespace.ALIGNMENT, "location"),

    // EDOAL NAMESPACE
	AND(            Namespace.EDOAL, "and", Constructor.AND),
	APPLY(          Namespace.EDOAL, "Apply", true),
	ARGUMENTS(      Namespace.EDOAL, "arguments"),
	ATTR_TRANSF(    Namespace.EDOAL, "transf"),
	CLASS_EXPR(     Namespace.EDOAL, "Class", true),//ALIGNMENT
	COMPARATOR(     Namespace.EDOAL, "comparator"),
	COMPOSE(        Namespace.EDOAL, "compose", Constructor.COMP),
	EDATATYPE(      Namespace.EDOAL, "datatype"),
	DATATYPE(       Namespace.EDOAL, "Datatype"),
	DOMAIN_RESTRICTION(Namespace.EDOAL, "AttributeDomainRestriction", true),
	//ID(             Namespace.EDOAL, "Id", true), // Useless
	INSTANCE_EXPR(  Namespace.EDOAL, "Instance", true),
	INVERSE(        Namespace.EDOAL, "inverse", Constructor.INVERSE),
	LITERAL(        Namespace.EDOAL, "Literal", true),
	NOT(            Namespace.EDOAL, "not", Constructor.NOT),
	ONPROPERTY(     Namespace.EDOAL, "onAttribute"),
	OPERATOR(       Namespace.EDOAL, "operator"),
	OR(             Namespace.EDOAL, "or", Constructor.OR),
	//PARAMETERS(     Namespace.EDOAL, "parameters"),
	//PROPERTY(       Namespace.EDOAL, "Property", true), // ??
	PROPERTY_EXPR(  Namespace.EDOAL, "Property"),
	OCCURENCE_COND(Namespace.EDOAL, "AttributeOccurenceRestriction", true),
	PROPERTY_TYPE_COND(Namespace.EDOAL, "PropertyTypeRestriction", true),
	PROPERTY_DOMAIN_COND(Namespace.EDOAL, "PropertyDomainRestriction", true),
	PROPERTY_VALUE_COND(     Namespace.EDOAL, "PropertyValueRestriction", true),
	RELATION_DOMAIN_COND(Namespace.EDOAL, "RelationDomainRestriction", true),
	RELATION_CODOMAIN_COND(Namespace.EDOAL, "RelationCoDomainRestriction", true),
	REFLEXIVE(      Namespace.EDOAL, "reflexive", Constructor.REFLEXIVE),
	RELATION_EXPR(  Namespace.EDOAL, "Relation", true),
	//SERVICE(        Namespace.EDOAL, "service"),
	STRING(          Namespace.EDOAL, "string"),
	ETYPE(           Namespace.EDOAL, "type"),
	SYMMETRIC(      Namespace.EDOAL, "symmetric", Constructor.SYMMETRIC),
	TOCLASS(        Namespace.EDOAL, "class"),
	ALL(            Namespace.EDOAL, "all"),
	EXISTS(         Namespace.EDOAL, "exists"),
	TRENT1(         Namespace.EDOAL, "entity1"),
	TRENT2(         Namespace.EDOAL, "entity2"),
	TRANSF(         Namespace.EDOAL, "Transformation", true),
	TRDIR(          Namespace.EDOAL, "direction"),
	TRANSFORMATION( Namespace.EDOAL, "transformation"),
	TRANSITIVE(     Namespace.EDOAL, "transitive", Constructor.TRANSITIVE),
	TYPE_COND(Namespace.EDOAL, "AttributeTypeRestriction", true), // undocumented
	VALUE(          Namespace.EDOAL, "value"),
	VAR(            Namespace.EDOAL, "var"),
	VALUE_COND(Namespace.EDOAL, "AttributeValueRestriction", true),
	    ;

    /** Operator to determine how to combine the expressions */
    public enum Constructor {
	AND, OR, NOT, COMP, JOIN, SYMMETRIC, REFLEXIVE, TRANSITIVE, INVERSE
	    };

    private static Map<String, SyntaxElement> register;

    /** Holds the xml name for the element. */
    public final String name;

    /** Holds the namespace for the element. */
    public final Namespace namespace;
	
    /** Holds the corresponding operator in the . */
    public final Constructor operator;
	
    /** Holds the RDF Resource or property for the entity
     * This is to be used by the RDF parser. 
     */
    public Object resource;

    /** If the resource is a property. 
     */
    public final boolean isProperty;

    private static void recordElement( final String name, final SyntaxElement el ) {
	if ( register == null ) register = new HashMap<String, SyntaxElement>();
	register.put( name, el );
    }

    /**
     * Constructor which takes the name and the namespace of the element.
     * 
     * @param namespace
     *            for the element
     * @param name
     *            for the element
     * @throws NullPointerException
     *             if the name or the namespace is <code>null</code>
     */
    private SyntaxElement( final Namespace ns, final String name, final Constructor op, final boolean prp ) { 
	if ((name == null) || (ns == null)) {
	    throw new NullPointerException("The name and the namespace must not be null");
	}
	namespace = ns;
	this.name = name;
	isProperty = prp;
	operator = op;
		recordElement( name, this );
    }

    private SyntaxElement(final Namespace namespace, final String name) {
	this( namespace, name, (Constructor)null, true );
    }
	
    private SyntaxElement(final Namespace namespace, final String name, final boolean prp) {
	this( namespace, name, (Constructor)null, prp );
    }
	
    private SyntaxElement(final Namespace namespace, final String name, final Constructor op ) {
	// constructors are properties...
	this( namespace, name, op, true );
    }

    /**
     * Determines an element given on a name string. The search will be
     * case-sensitive.
     * Good morning efficiency...
     * 
     * @param s
     *            the string to search for
     * @return the element with the given string as name, or null, if any could
     *         be found
     * @throws NullPointerException
     *             if the string is null
     */
    public static SyntaxElement getElementByName(final String s) {
	if (s == null) {
	    throw new NullPointerException("The string must not be null");
	}
	for ( SyntaxElement e : SyntaxElement.values() ) {
	    if ( s.equals( e.getName() ) ) {
		return e;
	    }
	}
	return null;
    }
    
    /**
     * Returns the xml name of the element.
     * 
     * @return the name
     */
    public String getName() { return name; }

    /**
     * Returns the namespace of the element.
     * 
     * @return the namespace
     */
    public Namespace getNamespace() { return namespace; }

    /**
     * Returns the resource of the element.
     * 
     * @return the resource
     */
    public static Object getResource( String name ) throws AlignmentException {
	if ( register == null ) throw new AlignmentException( "Non initialized Syntax resources" );
	return register.get( name ).resource;
    }

    /**
     * Returns the namespace of the element.
     * 
     * @return the namespace
     */
    public Constructor getOperator() { return operator; }

    public static SyntaxElement getElement( Constructor op ){
	for ( SyntaxElement e : SyntaxElement.values() ) {
	    if ( op.equals( e.getOperator() ) ) {
		return e;
	    }
	}
	return null;
    }

    /**
     * Formats the element to a printable string. The resulting string will
     * consist of the sort form of the namespace and the name of the element
     * separated by a ':' e.g. <code>rdf:resource</code>
     * 
     * @return the formated string
     */
    public String print() {
	return namespace.getShortCut() + ":" + name;
    }

    public String id() {
	return namespace.getUriPrefix()+name;
    }

    /**
     * Formats the element to a printable string. Formats the string in the same
     * way with the difference, that if the given namespace matches the
     * namespace of the element, the formated string will not be prefixed bye
     * the namespace and ':'.
     * 
     * @param namespace
     *            the namespace for which not to prefix the resulting string
     * @return teh formated string
     * @see #print()
     */
    public String print( final Namespace namespace ) {
	return (this.namespace == namespace) ? name : print();
    }
    
    /**
     * <p>
     * Prints a short description of the element. The format will be the
     * shortcut of the namespace, and the name of the element separated by ':'.
     * </p>
     * <p>
     * A example string could be: <code>rdf:resource</code>
     * </p>
     * @return the description of the element
     */
    public String toString() {
	return print();
    }
}

