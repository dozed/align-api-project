/*
 * $Id: OWLAxiomsRendererVisitor.java 1827 2013-03-07 22:44:05Z euzenat $
 *
 * Copyright (C) INRIA, 2003-2004, 2007-2013
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
import java.io.PrintWriter;
import java.net.URI;

import fr.inrialpes.exmo.align.impl.*;
import fr.inrialpes.exmo.align.impl.rel.*;
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

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
import fr.inrialpes.exmo.align.impl.edoal.PropertyExpression;
import fr.inrialpes.exmo.align.impl.edoal.PropertyId;
import fr.inrialpes.exmo.align.impl.edoal.PropertyConstruction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyTypeRestriction;
import fr.inrialpes.exmo.align.impl.edoal.PropertyValueRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationExpression;
import fr.inrialpes.exmo.align.impl.edoal.RelationId;
import fr.inrialpes.exmo.align.impl.edoal.RelationConstruction;
import fr.inrialpes.exmo.align.impl.edoal.RelationDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.RelationCoDomainRestriction;
import fr.inrialpes.exmo.align.impl.edoal.InstanceExpression;
import fr.inrialpes.exmo.align.impl.edoal.InstanceId;

import fr.inrialpes.exmo.align.impl.edoal.Transformation;
import fr.inrialpes.exmo.align.impl.edoal.ValueExpression;
import fr.inrialpes.exmo.align.impl.edoal.Value;
import fr.inrialpes.exmo.align.impl.edoal.Apply;
import fr.inrialpes.exmo.align.impl.edoal.Datatype;
import fr.inrialpes.exmo.align.impl.edoal.Comparator;
import fr.inrialpes.exmo.align.impl.edoal.EDOALCell;
import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.impl.edoal.EDOALVisitor;

/**
 * Renders an alignment as a new ontology merging these.
 *
 * @author Jérôme Euzenat
 * @version $Id: OWLAxiomsRendererVisitor.java 1827 2013-03-07 22:44:05Z euzenat $ 
 */

public class OWLAxiomsRendererVisitor extends IndentedRendererVisitor implements AlignmentVisitor, EDOALVisitor {
    boolean heterogeneous = false;
    boolean edoal = false;
    Alignment alignment = null;
    LoadedOntology onto1 = null;
    LoadedOntology onto2 = null;
    Cell cell = null;
    Relation toProcess = null;

    private static Namespace DEF = Namespace.ALIGNMENT;
    
    public OWLAxiomsRendererVisitor( PrintWriter writer ){
	super( writer );
    }

    public void init( Properties p ) {
	if ( p.getProperty("heterogeneous") != null ) heterogeneous = true;
    };

    public void visit( Alignment align ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, align, Alignment.class ) ) return;
	// default behaviour
	if ( align instanceof ObjectAlignment ) {
	    alignment = align;
	    onto1 = (LoadedOntology)((ObjectAlignment)alignment).getOntologyObject1();
	    onto2 = (LoadedOntology)((ObjectAlignment)alignment).getOntologyObject2();
	} else if ( align instanceof EDOALAlignment ) {
	    edoal = true;
	} else {
	    try {
		alignment = AlignmentTransformer.toObjectAlignment((URIAlignment) align);
		onto1 = (LoadedOntology)((ObjectAlignment)alignment).getOntologyObject1();
		onto2 = (LoadedOntology)((ObjectAlignment)alignment).getOntologyObject2();
	    } catch ( AlignmentException alex ) {
		throw new AlignmentException("OWLAxiomsRenderer: cannot render simple alignment. Need an ObjectAlignment", alex );
	    }
	}
	writer.print("<rdf:RDF"+NL);
	writer.print("    xmlns:owl=\"http://www.w3.org/2002/07/owl#\""+NL);
	writer.print("    xmlns:rdf=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#\""+NL);
	writer.print("    xmlns:rdfs=\"http://www.w3.org/2000/01/rdf-schema#\" "+NL);
	writer.print("    xmlns:xsd=\"http://www.w3.org/2001/XMLSchema#\">"+NL+NL);	
	writer.print("  <owl:Ontology rdf:about=\"\">"+NL);
	writer.print("    <rdfs:comment>Matched ontologies</rdfs:comment>"+NL);
	writer.print("    <rdfs:comment>Generated by fr.inrialpes.exmo.align.renderer.OWLAxiomsRendererVisitor</rdfs:comment>"+NL);
	for ( String[] ext : align.getExtensions() ){
	    writer.print("    <rdfs:comment>"+ext[1]+": "+ext[2]+"</rdfs:comment>"+NL);
	}
	writer.print("    <owl:imports rdf:resource=\""+align.getOntology1URI().toString()+"\"/>"+NL);
	writer.print("    <owl:imports rdf:resource=\""+align.getOntology2URI().toString()+"\"/>"+NL);
	writer.print("  </owl:Ontology>"+NL+NL);
	
	try {
	    for( Cell c : align ){
		Object ob1 = c.getObject1();
		Object ob2 = c.getObject2();
		
		if ( heterogeneous || edoal ||
		     ( onto1.isClass( ob1 ) && onto2.isClass( ob2 ) ) ||
		     ( onto1.isDataProperty( ob1 ) && onto2.isDataProperty( ob2 ) ) ||
		     ( onto1.isObjectProperty( ob1 ) && onto2.isObjectProperty( ob2 ) ) ||
		     ( onto1.isIndividual( ob1 ) && onto2.isIndividual( ob2 ) ) ) {
		    c.accept( this );
		}
	    } //end for
	} catch ( OntowrapException owex ) {
	    throw new AlignmentException( "Error accessing ontology", owex );
	}

	writer.print("</rdf:RDF>"+NL);
    }

    public void visit( Cell cell ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, cell, Cell.class ) ) return;
	// default behaviour
	if ( cell.getId() != null ) writer.print(NL+NL+"<!-- "+cell.getId()+" -->"+NL);
	if ( cell instanceof EDOALCell ) {
	    cell.accept( this ); // useless cast?
	} else {
	    this.cell = cell;
	    Object ob1 = cell.getObject1();
	    Object ob2 = cell.getObject2();
	    URI u1;
	    try {
		Relation rel = cell.getRelation();
		if ( rel instanceof SubsumeRelation || rel instanceof HasInstanceRelation ){
		    u1 = onto2.getEntityURI( ob2 );
		} else {
		    u1 = onto1.getEntityURI( ob1 );
		}
		if ( ob1 instanceof ClassExpression || onto1.isClass( ob1 ) ) {
		    writer.print("  <owl:Class rdf:about=\""+u1+"\">"+NL);
		    rel.accept( this );
		    writer.print("  </owl:Class>"+NL);
		} else if ( ob1 instanceof PropertyExpression || onto1.isDataProperty( ob1 ) ) {
		    writer.print("  <owl:DatatypeProperty rdf:about=\""+u1+"\">"+NL);
		    rel.accept( this );
		    writer.print("  </owl:DatatypeProperty>"+NL);
		} else if ( ob1 instanceof RelationExpression || onto1.isObjectProperty( ob1 ) ) {
		    writer.print("  <owl:ObjectProperty rdf:about=\""+u1+"\">"+NL);
		    rel.accept( this );
		    writer.print("  </owl:ObjectProperty>"+NL);
		} else if ( ob1 instanceof InstanceExpression || onto1.isIndividual( ob1 ) ) {
		    writer.print("  <owl:Thing rdf:about=\""+u1+"\">"+NL);
		    rel.accept( this );
		    writer.print("  </owl:Thing>"+NL);
		}
	    } catch ( OntowrapException owex ) {
		throw new AlignmentException( "Error accessing ontology", owex );
	    }
	}
    }

    public void visit( EDOALCell cell ) throws AlignmentException {
	this.cell = cell;
	toProcess = cell.getRelation();
	increaseIndent();
	if ( toProcess instanceof SubsumeRelation || toProcess instanceof HasInstanceRelation ) {
	    ((Expression)cell.getObject2()).accept( this );
	} else {
	    ((Expression)cell.getObject1()).accept( this );
	}
	decreaseIndent();
	writer.print(NL);
    }

    // Classical dispatch
    // This is the previous code... which is the one which was used.
    // It should be reintroduced in the dispatch!
    public void visit( Relation rel ) throws AlignmentException {
	if ( subsumedInvocableMethod( this, rel, Relation.class ) ) return;
	// default behaviour
	Object ob2 = cell.getObject2();
	if ( edoal ) {
	    String owlrel = getRelationName( rel, ob2 );
	    if ( owlrel == null ) throw new AlignmentException( "Relation "+rel+" cannot apply to "+ob2 );
	    writer.print("  <"+owlrel+">"+NL);
	    increaseIndent();
	    if ( rel instanceof HasInstanceRelation || rel instanceof SubsumeRelation ) {
		((Expression)cell.getObject1()).accept( this );
	    } else {
		((Expression)ob2).accept( this );
	    }
	    decreaseIndent();
	    writer.print(NL+"  </"+owlrel+">");
	} else {
	    String owlrel = getRelationName( onto2, rel, ob2 );
	    if ( owlrel == null ) throw new AlignmentException( "Cannot express relation "+rel );
	    try {
		writer.print("    <"+owlrel+" rdf:resource=\""+onto2.getEntityURI( ob2 )+"\"/>"+NL);
	    } catch ( OntowrapException owex ) {
		throw new AlignmentException( "Error accessing ontology", owex );
	    }
	}
    }

    public void printRel( Object ob, LoadedOntology onto, Relation rel ) throws AlignmentException {
	if ( !edoal ) {
	    String owlrel = getRelationName( onto, rel, ob );
	    if ( owlrel == null ) throw new AlignmentException( "Cannot express relation "+rel );
	    try {
		writer.print("    <"+owlrel+" rdf:resource=\""+onto.getEntityURI( ob )+"\"/>"+NL);
	    } catch ( OntowrapException owex ) {
		throw new AlignmentException( "Error accessing ontology", owex );
	    }
	} else {
	    String owlrel = getRelationName( rel, ob );
	    if ( owlrel == null ) throw new AlignmentException( "Cannot express relation "+rel );
	    if ( ob instanceof InstanceId ) {
		indentedOutput("<"+owlrel+" rdf:resource=\""+((InstanceId)ob).getURI()+"\"/>");
	    } else {
		indentedOutput("<"+owlrel+">");
		writer.print(NL);
		increaseIndent();
		((Expression)ob).accept( this ); // ?? no cast
		decreaseIndent();
		writer.print(NL);
		indentedOutput("</"+owlrel+">");
	    }
	}
    }

    /**
     * For EDOAL relation name depends on type of expressions
     */
    // The two getRelationName may be put as relation methods (this would be more customisable)
    public String getRelationName( Relation rel, Object ob ) {
	if ( rel instanceof EquivRelation ) {
	    if ( ob instanceof ClassExpression ) {
		return "owl:equivalentClass";
	    } else if ( ob instanceof PropertyExpression || ob instanceof RelationExpression ) {
		return "owl:equivalentProperty";
	    } else if ( ob instanceof InstanceExpression ) {
		return "owl:sameAs";
	    }
	} else if ( rel instanceof IncompatRelation ) {
	    if ( ob instanceof ClassExpression ) {
		return "owl:disjointFrom";
	    } else if ( ob instanceof InstanceExpression ) {
		return "owl:differentFrom";
	    }
	} else if ( rel instanceof SubsumeRelation ) {
	    //reversed = true;
	    if ( ob instanceof ClassExpression ) {
		return "owl:subClassOf";
	    } else if ( ob instanceof PropertyExpression || ob instanceof RelationExpression ) {
		return "owl:subPropertyOf";
	    }
	} else if ( rel instanceof SubsumedRelation ) {
	    if ( ob instanceof ClassExpression ) {
		return "owl:subClassOf";
	    } else if ( ob instanceof PropertyExpression || ob instanceof RelationExpression ) {
		return "owl:subPropertyOf";
	    }
	} else if ( rel instanceof InstanceOfRelation ) {
	    if ( ob instanceof InstanceExpression ) {
		return "rdf:type";
	    }
	} else if ( rel instanceof HasInstanceRelation ) {
	    //reversed = true;
	    if ( ob instanceof InstanceExpression ) {
		return "rdf:type";
	    }
	}
	return null;
    }

    /**
     * Regular: relation name depends on loaded ontology
     */
    public String getRelationName( LoadedOntology onto, Relation rel, Object ob ) {
	try {
	    if ( rel instanceof EquivRelation ) {
		if ( onto.isClass( ob ) ) {
		    return "owl:equivalentClass";
		} else if ( onto.isProperty( ob ) ) {
		    return "owl:equivalentProperty";
		} else if ( onto.isIndividual( ob ) ) {
		    return "owl:sameAs";
		}
	    } else if ( rel instanceof SubsumeRelation ) {
		if ( onto.isClass( ob ) ) {
		    return "rdfs:subClassOf";
		} else if ( onto.isProperty( ob ) ) {
		    return "rdfs:subPropertyOf";
		}
	    } else if ( rel instanceof SubsumedRelation ) {
		if ( onto.isClass( ob ) ) {
		    return "rdfs:subClassOf";
		} else if ( onto.isProperty( ob ) ) {
		    return "rdfs:subPropertyOf";
		}
	    } else if ( rel instanceof IncompatRelation ) {
		if ( onto.isClass( ob ) ) {
		    return "rdfs:disjointFrom";
		} else if ( onto.isIndividual( ob ) ) {
		    return "owl:differentFrom";
		}
	    } else if ( rel instanceof InstanceOfRelation ) {
		if ( onto.isClass( ob ) ) {
		    return "rdf:type";
		}
	    } else if ( rel instanceof HasInstanceRelation ) {
		// JE2011: this should be wrong (should be on the other side)
		if ( onto.isClass( ob ) ) {
		    return "rdf:type";
		}
	    }
	} catch ( OntowrapException owex ) {}; // return null anyway
	return null;
    }

    /* This may be genericised
       These methods are not used at the moment
       However they are roughly correct and may be used for more customisation
     */

    public void visit( EquivRelation rel ) throws AlignmentException {
	printRel( cell.getObject2(), onto2, rel );
    }

    public void visit( SubsumeRelation rel ) throws AlignmentException {
	printRel( cell.getObject1(), onto1, rel );
    }

    public void visit( SubsumedRelation rel ) throws AlignmentException {
	printRel( cell.getObject2(), onto2, rel );
    }

    public void visit( IncompatRelation rel ) throws AlignmentException {
	printRel( cell.getObject2(), onto2, rel );
    }

    public void visit( InstanceOfRelation rel ) throws AlignmentException {
	printRel( cell.getObject2(), onto2, rel );
    }

    public void visit( HasInstanceRelation rel ) throws AlignmentException {
	printRel( cell.getObject1(), onto1, rel );
    }

    // ******* EDOAL

    public void visit( final ClassId e ) throws AlignmentException {
	if ( toProcess == null ) {
	    indentedOutput("<owl:Class "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+e.getURI()+"\"/>");
	} else {
	    Relation toProcessNext = toProcess;
	    toProcess = null;
	    indentedOutput("<owl:Class "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+e.getURI()+"\">"+NL);
	    increaseIndent();
	    toProcessNext.accept( this );
	    writer.print(NL);
	    decreaseIndent();
	    indentedOutput("</owl:Class>");
	}
    }

    public void visit( final ClassConstruction e ) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	final Constructor op = e.getOperator();
	String owlop = null;
	// Very special treatment
	if ( toProcessNext != null && e.getComponents().size() == 0 ) {
	    if ( op == Constructor.AND ) owlop = "http://www.w3.org/2002/07/owl#Thing";
	    else if ( op == Constructor.OR ) owlop = "http://www.w3.org/2002/07/owl#Nothing";
	    else if ( op == Constructor.NOT ) throw new AlignmentException( "Complement constructor cannot be empty");
	    indentedOutput("<owl:Class "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+owlop+"\">"+NL);
	    increaseIndent();
	    toProcessNext.accept( this ); 
	    writer.print(NL);
	    decreaseIndent();
	    indentedOutput("</owl:Class>");
	} else {
	    if ( op == Constructor.AND ) owlop = "intersectionOf";
	    else if ( op == Constructor.OR ) owlop = "unionOf";
	    else if ( op == Constructor.NOT ) owlop = "complementOf";
	    else throw new AlignmentException( "Unknown class constructor : "+op );
	    if ( e.getComponents().size() == 0 ) {
		if ( op == Constructor.AND ) indentedOutput("<owl:Thing/>");
		else if ( op == Constructor.OR ) indentedOutput("<owl:Nothing/>");
		else throw new AlignmentException( "Complement constructor cannot be empty");
	    } else {
		indentedOutput("<owl:Class>"+NL);
		increaseIndent();
		indentedOutput("<owl:"+owlop);
		if ( ( (op == Constructor.AND) || (op == Constructor.OR) ) ) 
		    writer.print(" "+SyntaxElement.RDF_PARSETYPE.print(DEF)+"=\"Collection\"");
		writer.print(">"+NL);
		increaseIndent();
		for (final ClassExpression ce : e.getComponents()) {
		    writer.print(linePrefix);
		    ce.accept( this );
		    writer.print(NL);
		}
		decreaseIndent();
		indentedOutput("</owl:"+owlop+">"+NL);
		if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
		decreaseIndent();
		indentedOutput("</owl:Class>");
	    }
	}
    }

    public void visit( final ClassValueRestriction c ) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:Restriction>"+NL);
	increaseIndent();
	indentedOutput("<owl:onProperty>"+NL);
	increaseIndent();
	c.getRestrictionPath().accept( this );
	decreaseIndent();
	writer.print(NL);
	indentedOutputln("</owl:onProperty>");
	ValueExpression ve = c.getValue();
	if ( ve instanceof Value ) {
	    indentedOutput("<owl:hasValue");
	    if ( ((Value)ve).getType() != null ) {
		writer.print( " rdf:datatype=\""+((Value)ve).getType()+"\"" );
	    }
	    writer.print( ">"+((Value)ve).getValue()+"</owl:hasValue>"+NL);
	} else if ( ve instanceof InstanceId ) {
	    indentedOutput("<owl:hasValue>"+NL);
	    increaseIndent();
	    ve.accept( this );
	    decreaseIndent();
	    writer.print(NL);
	    indentedOutput("</owl:hasValue>"+NL);
	} else throw new AlignmentException( "OWL does not support path constraints in hasValue : "+ve );
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:Restriction>");
    }

    public void visit( final ClassTypeRestriction c ) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:Restriction>"+NL);
	increaseIndent();
	indentedOutput("<owl:onProperty>"+NL);
	increaseIndent();
	c.getRestrictionPath().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</owl:onProperty>"+NL);
	indentedOutput("<owl:allValuesFrom>"+NL);
	increaseIndent();
	c.getType().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</owl:allValuesFrom>"+NL);
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:Restriction>");
    }

    public void visit( final ClassDomainRestriction c ) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:Restriction>"+NL);
	increaseIndent();
	indentedOutput("<owl:onProperty>"+NL);
	increaseIndent();
	c.getRestrictionPath().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</owl:onProperty>"+NL);
	if ( c.isUniversal() ) {
	    indentedOutput("<owl:allValuesFrom>"+NL);
	} else {
	    indentedOutput("<owl:someValuesFrom>"+NL);
	}
	increaseIndent();
	c.getDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	if ( c.isUniversal() ) {
	    indentedOutput("</owl:allValuesFrom>"+NL);
	} else {
	    indentedOutput("</owl:someValuesFrom>"+NL);
	}
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:Restriction>");
    }

    // TOTEST
    public void visit( final ClassOccurenceRestriction c ) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:Restriction>"+NL);
	increaseIndent();
	indentedOutput("<owl:onProperty>"+NL);
	increaseIndent();
	c.getRestrictionPath().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</owl:onProperty>"+NL);
	String cardinality = null;
	Comparator comp = c.getComparator();
	if ( comp == Comparator.EQUAL ) cardinality = "cardinality";
	else if ( comp == Comparator.LOWER ) cardinality = "maxCardinality";
	else if ( comp == Comparator.GREATER ) cardinality = "minCardinality";
	else throw new AlignmentException( "Unknown comparator : "+comp.getURI() );
	indentedOutput("<owl:"+cardinality+" rdf:datatype=\"&xsd;nonNegativeInteger\">");
	writer.print(c.getOccurence());
	writer.print("</owl:"+cardinality+">"+NL);
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:Restriction>");
    }
    
    public void visit(final PropertyId e) throws AlignmentException {
	if ( toProcess == null ) {
	    indentedOutput("<owl:DatatypeProperty "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+e.getURI()+"\"/>");
	} else {
	    Relation toProcessNext = toProcess;
	    toProcess = null;
	    indentedOutput("<owl:DatatypeProperty "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+e.getURI()+"\">"+NL);
	    increaseIndent();
	    toProcessNext.accept( this );
	    writer.print(NL);
	    decreaseIndent();
	    indentedOutput("</owl:DatatypeProperty>");
	}
    }

    /**
     * OWL, and in particular OWL 2, does not allow for more Relation (ObjectProperty)
     * and Property (DataProperty) constructor than owl:inverseOf
     * It is thus imposible to transcribe our and, or and not constructors.
     */
    public void visit( final PropertyConstruction e ) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:DatatypePropety>"+NL);
	increaseIndent();
	final Constructor op = e.getOperator();
	String owlop = null;
	if ( op == Constructor.COMP ) owlop = "propertyChainAxiom";
	// JE: FOR TESTING
	//owlop = "FORTESTING("+op.name()+")";
	if ( owlop == null ) throw new AlignmentException( "Cannot translate property construction in OWL : "+op );
	indentedOutput("<owl:"+owlop);
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
	indentedOutput("</owl:"+owlop+">"+NL);
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:DatatypePropety>");
    }
	
    public void visit(final PropertyValueRestriction c) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:DatatypeProperty>"+NL);
	increaseIndent();
	indentedOutput("<rdfs:range>"+NL);
	increaseIndent();
	indentedOutput("<rdfs:Datatype>"+NL);
	increaseIndent();
	indentedOutput("<owl:oneOf>"+NL);
	increaseIndent();
	// In EDOAL, this does only contain one value and is thus rendered as:
	indentedOutput("<rdf:Description>"+NL);
	increaseIndent();
	ValueExpression ve = c.getValue();
	if ( ve instanceof Value ) {
	    indentedOutput("<rdf:first");
	    if ( ((Value)ve).getType() != null ) {
		writer.print( " rdf:datatype=\""+((Value)ve).getType()+"\"" );
	    }
	    writer.print( ">"+((Value)ve).getValue()+"</rdf:first>"+NL);
	} else {
	    indentedOutput("<rdf:first>"+NL);
	    ve.accept( this );
	    writer.print("</rdf:first>"+NL);
	    indentedOutput("<rdf:rest rdf:resource=\"http://www.w3.org/1999/02/22-rdf-syntax-ns#nil\"/>"+NL);
	}
	decreaseIndent();
	indentedOutput("</rdf:Description>"+NL);
	// This is incorrect for more than one value... see the OWL:
	/*
         <rdfs:Datatype>
          <owl:oneOf>
           <rdf:Description>
            <rdf:first rdf:datatype="&xsd;integer">1</rdf:first>
             <rdf:rest>
              <rdf:Description>
               <rdf:first rdf:datatype="&xsd;integer">2</rdf:first>
               <rdf:rest rdf:resource="http://www.w3.org/1999/02/22-rdf-syntax-ns#nil"/>
              </rdf:Description>
             </rdf:rest>
            </rdf:Description>
           </owl:oneOf>
          </rdfs:Datatype>
	*/
	decreaseIndent();
	indentedOutput("</owl:oneOf>"+NL);
	decreaseIndent();
	indentedOutput("</rdfs:Datatype>"+NL);
	decreaseIndent();
	indentedOutput("</rdfs:range>"+NL);
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:DatatypeProperty>");
    }

    public void visit(final PropertyDomainRestriction c) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:DatatypeProperty>"+NL);
	increaseIndent();
	indentedOutput("<rdfs:domain>"+NL);
	increaseIndent();
	c.getDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</rdfs:domain>"+NL);
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:DatatypeProperty>");
    }

    public void visit(final PropertyTypeRestriction c) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:DatatypeProperty>"+NL);
	increaseIndent();
	indentedOutput("<rdfs:range>"+NL);
	increaseIndent();
	c.getType().accept( this );
	decreaseIndent();
	indentedOutput("</rdfs:range>"+NL);
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:DatatypeProperty>");
    }
	
    public void visit( final RelationId e ) throws AlignmentException {
	if ( toProcess == null ) {
	    indentedOutput("<owl:ObjectProperty "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+e.getURI()+"\"/>");
	} else {
	    Relation toProcessNext = toProcess;
	    toProcess = null;
	    indentedOutput("<owl:ObjectProperty "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+e.getURI()+"\">"+NL);
	    increaseIndent();
	    toProcessNext.accept( this );
	    writer.print(NL);
	    decreaseIndent();
	    indentedOutput("</owl:ObjectProperty>");
	}
    }

    /**
     * OWL, and in particular OWL 2, does not allow for more Relation (ObjectProperty)
     * and Property (DataProperty) constructor than owl:inverseOf
     * It is thus imposible to transcribe our and, or and not constructors.
     * Moreover, they have no constructor for the symmetric, transitive and reflexive
     * closure and the compositional closure (or composition) can only be obtained by
     * defining a property subsumed by this closure through an axiom.
     * It is also possible to rewrite the reflexive closures as axioms as well.
     * But the transitive closure can only be obtained through subsumption.
     */
    public void visit( final RelationConstruction e ) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:ObjectProperty>"+NL);
	increaseIndent();
	final Constructor op = e.getOperator();
	String owlop = null;
	if ( op == Constructor.INVERSE ) {
	    owlop = "inverseOf";
	} else if ( op == Constructor.COMP ) {
	    owlop = "propertyChainAxiom";
	}
	// JE: FOR TESTING
	//owlop = "FORTESTING("+op.name()+")";
	if ( owlop == null ) throw new AlignmentException( "Cannot translate relation construction in OWL : "+op );
	indentedOutput("<owl:"+owlop);
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
	indentedOutput("</owl:"+owlop+">"+NL);
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:ObjectProperty>");
    }
	
    public void visit(final RelationCoDomainRestriction c) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:ObjectProperty>"+NL);
	increaseIndent();
	indentedOutput("<rdfs:range>"+NL);
	increaseIndent();
	c.getCoDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</rdfs:range>"+NL);
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:ObjectProperty>");
    }

    public void visit(final RelationDomainRestriction c) throws AlignmentException {
	Relation toProcessNext = toProcess;
	toProcess = null;
	indentedOutput("<owl:ObjectProperty>"+NL);
	increaseIndent();
	indentedOutput("<rdfs:domain>"+NL);
	increaseIndent();
	c.getDomain().accept( this );
	writer.print(NL);
	decreaseIndent();
	indentedOutput("</rdfs:domain>"+NL);
	if ( toProcessNext != null ) { toProcessNext.accept( this ); writer.print(NL); }
	decreaseIndent();
	indentedOutput("</owl:ObjectProperty>");
    }

    public void visit( final InstanceId e ) throws AlignmentException {
	if ( toProcess == null ) {
	    indentedOutput("<owl:Individual "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+e.getURI()+"\"/>");
	} else {
	    Relation toProcessNext = toProcess;
	    toProcess = null;
	    indentedOutput("<owl:Individual "+SyntaxElement.RDF_ABOUT.print(DEF)+"=\""+e.getURI()+"\">"+NL);
	    increaseIndent();
	    toProcessNext.accept( this );
	    writer.print(NL);
	    decreaseIndent();
	    indentedOutput("</owl:Individual>");
	}
    }

    // Unused: see ClassValueRestriction above
    public void visit( final Value e ) throws AlignmentException {
    }

    // OWL does not allow for function calls
    public void visit( final Apply e ) throws AlignmentException {
	throw new AlignmentException( "Cannot render function call in OWL "+e );
    }

    // Not implemented. We only ignore transformations in OWL
    public void visit( final Transformation transf ) throws AlignmentException {
    }

    /**
     * Our Datatypes are only strings identifying datatypes.
     * For OWL, they should be considered as built-in types because we do 
     * not know how to add other types.
     * Hence we could simply have used a rdfs:Datatype="<name>"
     *
     * OWL offers further possiblities, such as additional owl:withRestriction
     * clauses
     */
    public void visit( final Datatype e ) {
	indentedOutput("<owl:Datatype><owl:onDataType rdf:resource=\""+e.getType()+"\"/></owl:Datatype>");
    }

}
