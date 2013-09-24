/*
 * $Id: TypeCheckingVisitor.java 1713 2012-03-26 17:48:12Z euzenat $
 *
 * Copyright (C) INRIA, 2010-2012
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

import java.util.Hashtable;
import java.net.URI;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;
import org.semanticweb.owl.align.Visitable;
import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.parser.SyntaxElement.Constructor;

import fr.inrialpes.exmo.align.impl.edoal.EDOALAlignment;
import fr.inrialpes.exmo.align.impl.edoal.EDOALCell;

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
import fr.inrialpes.exmo.align.impl.edoal.Value;
import fr.inrialpes.exmo.align.impl.edoal.ValueExpression;
import fr.inrialpes.exmo.align.impl.edoal.Apply;
import fr.inrialpes.exmo.align.impl.edoal.Datatype;

/**
 * Checks if an EDOALAlignment is well-typed
 * This is called by AlignmentParser (with debug>0)
 *
 * @author Jérôme Euzenat
 * @version $Id: TypeCheckingVisitor.java 1713 2012-03-26 17:48:12Z euzenat $
 */

public class TypeCheckingVisitor {

    public enum TYPE { CLASS, PROPERTY, RELATION, INSTANCE, VALUE, DATATYPE, ANY, ERROR };

    EDOALAlignment alignment = null;
    EDOALCell cell = null;
    Hashtable<URI,TYPE> nslist = null;
    boolean embedded = false; // if the output is XML embeded in a structure
    boolean error = false; // has an error been encountered
    boolean print = false; // do we print error messages
    private boolean isPattern = false;

    private static Namespace DEF = Namespace.ALIGNMENT;
    
    public TypeCheckingVisitor() {
	nslist = new Hashtable<URI,TYPE>();
    }
	
    /*
      public TYPE visit( Visitable o ) throws AlignmentException {
	throw new AlignmentException( "Cannot type check all" );
    }

    public TYPE visit( Expression o ) throws AlignmentException {
	throw new AlignmentException("Cannot export abstract Expression: "+o );
	}*/

    public TYPE visit( EDOALAlignment align ) throws AlignmentException {
	alignment = align;
	if ( alignment.getLevel().startsWith("2EDOALPattern") ) isPattern = true;
	align.getOntology1URI();
	for( Cell c : align ){ ((EDOALCell)c).accept( this ); };
	return TYPE.ANY;
    }

    public TYPE visit( EDOALCell cell ) throws AlignmentException {
	this.cell = cell;
	// Could be useful when not parsing EDOAL
	if ( alignment.getLevel().startsWith("2EDOAL") ) {
	    TYPE t1 = ((Expression)(cell.getObject1())).accept( this );
	    TYPE t2 = ((Expression)(cell.getObject2())).accept( this );
	    // JE2011: This should be dependent on the Relation type (e.g., instanceOf)
	    // See below
	    if ( !compatible( t1, t2 ) ) return TYPE.ERROR;
	}
	return TYPE.ANY;
    }

    public TYPE visit( Relation o ) throws AlignmentException {
	throw new AlignmentException("This is not used yet and not implemented: "+o );
    }

    public boolean compatible( TYPE t1, TYPE t2 ) {
	if ( t1 == TYPE.ERROR || t2 == TYPE.ERROR ) return true; //nomore
	if ( t1 == TYPE.ANY || t2 == TYPE.ANY || t1 == t2 ) return true;
	return false;
    }

    public boolean pcompatible( TYPE ptype, TYPE tp ) {
	if ( ptype == TYPE.ERROR || tp == TYPE.ERROR ) return true; //nomore
	if ( ( ptype == TYPE.RELATION && tp == TYPE.INSTANCE ) ||
	     ( ptype == TYPE.PROPERTY && tp == TYPE.VALUE ) ||
	     tp == TYPE.ANY ) return true;
	return false;
    }

    // JE2011
    // This should not be related to the Relation class
    // and it can implement a compatibility check from the given types!
    // depending on the 
    //public TYPE visit( EDOALRelation rel ) {
    //	return TYPE.ANY;
    //};

    public TYPE visit( final Transformation trsf ) throws AlignmentException {
	// getType() could allow to do better typing
	TYPE tp1 = trsf.getObject1().accept( this );
	TYPE tp2 = trsf.getObject2().accept( this );
	if ( !compatible( tp1, TYPE.VALUE ) ) return raiseError( null, tp1, TYPE.VALUE );
	if ( !compatible( tp2, TYPE.VALUE ) ) return raiseError( null, tp2, TYPE.VALUE );
	return TYPE.ANY;
    }
    /*
    public TYPE visit( final PathExpression p ) throws AlignmentException {
	throw new AlignmentException( "Cannot dispatch PathExpression "+p );
    }

    public TYPE visit( final ClassExpression e ) throws AlignmentException {
	throw new AlignmentException( "Cannot dispatch ClassExpression "+e );
    }
    */
    public TYPE visit( final ClassId e ) throws AlignmentException {
	TYPE type = nslist.get( e.getURI() );
	if ( type == null ) nslist.put( e.getURI(), TYPE.CLASS );
	else if ( !compatible( type, TYPE.CLASS ) ) return raiseError( e.getURI(), type, TYPE.CLASS );
	return TYPE.CLASS;
    }

    public TYPE visit( final ClassConstruction e ) throws AlignmentException {
	final Constructor op = e.getOperator(); // do we test the operator?
	boolean allright = true;
	for (final ClassExpression ce : e.getComponents()) {
	    TYPE tp = ce.accept( this );
	    if ( !compatible( tp, TYPE.CLASS ) ) {
		raiseError( null, tp, TYPE.CLASS );
		allright = false;
	    }
	}
	if ( allright ) return TYPE.CLASS;
	else return TYPE.ERROR;
    }
    /*
    public TYPE visit(final ClassRestriction e) throws AlignmentException {
	throw new AlignmentException( "Cannot dispatch ClassRestriction "+e );
    }
    */
    public TYPE visit( final ClassValueRestriction c ) throws AlignmentException {
	TYPE ptype = c.getRestrictionPath().accept( this );
	TYPE tp = c.getValue().accept( this );
	if ( !pcompatible( ptype, tp ) ) return raiseError( null, ptype, tp );
	return TYPE.CLASS;
    }

    public TYPE visit( final ClassTypeRestriction c ) throws AlignmentException {
	TYPE ptype = c.getRestrictionPath().accept( this );
	TYPE tp = c.getType().accept( this );
	if ( !compatible( ptype, TYPE.PROPERTY ) ) return raiseError( null, ptype, TYPE.PROPERTY );
	if ( !compatible( tp, TYPE.DATATYPE ) ) return raiseError( null, tp, TYPE.DATATYPE );
	return TYPE.CLASS;
    }

    public TYPE visit( final ClassDomainRestriction c ) throws AlignmentException {
	TYPE ptype = c.getRestrictionPath().accept( this );
	TYPE tp = c.getDomain().accept( this );
	if ( !compatible( ptype, TYPE.RELATION ) ) return raiseError( null, ptype, TYPE.RELATION );
	if ( !compatible( tp, TYPE.CLASS ) ) return raiseError( null, tp, TYPE.CLASS );
	return TYPE.CLASS;
    }

    public TYPE visit( final ClassOccurenceRestriction c ) throws AlignmentException {
	//c.getComparator().getURI();
	TYPE ptype = c.getRestrictionPath().accept( this );
	// c.getOccurence() is an integer
	if ( !compatible( ptype, TYPE.RELATION ) && 
	     !compatible( ptype, TYPE.PROPERTY ) ) return raiseError( null, ptype, TYPE.RELATION );
	return TYPE.CLASS;
    }
    /*
    public TYPE visit(final PropertyExpression e) throws AlignmentException {
	throw new AlignmentException( "Cannot dispatch PropertyExpression "+e );
    }
    */
    public TYPE visit(final PropertyId e) throws AlignmentException {
	TYPE type = nslist.get( e.getURI() );
	if ( type == null ) nslist.put( e.getURI(), TYPE.PROPERTY );
	else if ( !compatible( type, TYPE.PROPERTY ) ) return raiseError( e.getURI(), type, TYPE.PROPERTY );
	return TYPE.PROPERTY;
    }

    public TYPE visit(final PropertyConstruction e) throws AlignmentException {
	//final Constructor op = e.getOperator(); // do we test the operator?
	boolean allright = true;
	for ( final PathExpression pe : e.getComponents() ) {
	    TYPE tp = pe.accept( this );
	    if ( !compatible( tp, TYPE.PROPERTY ) ) {
		raiseError( null, tp, TYPE.PROPERTY );
		allright = false;
	    }
	}
	if ( allright ) return TYPE.PROPERTY;
	else return TYPE.ERROR;
    }
    /*
    public TYPE visit(final PropertyRestriction e) throws AlignmentException {
	throw new AlignmentException( "Cannot dispatch PropertyRestriction "+e );
    }
    */
    public TYPE visit(final PropertyValueRestriction c) throws AlignmentException {
	//c.getComparator().getURI(); // do we test the operator?
	TYPE type = c.getValue().accept( this );
	if ( !compatible( type, TYPE.VALUE ) ) return raiseError( null, type, TYPE.VALUE );
	return TYPE.PROPERTY;
    }

    public TYPE visit(final PropertyDomainRestriction c) throws AlignmentException {
	TYPE type = c.getDomain().accept( this );
	if ( !compatible( type, TYPE.DATATYPE ) ) return raiseError( null, type, TYPE.DATATYPE );
	return TYPE.PROPERTY;
    }

    public TYPE visit(final PropertyTypeRestriction c) throws AlignmentException {
	TYPE type = c.getType().accept( this );
	if ( !compatible( type, TYPE.DATATYPE ) ) return raiseError( null, type, TYPE.DATATYPE );
	return TYPE.PROPERTY;
    }
    /*
    public TYPE visit( final RelationExpression e ) throws AlignmentException {
	throw new AlignmentException( "Cannot dispatch RelationExpression "+e );
    }
    */
    public TYPE visit( final RelationId e ) throws AlignmentException {
	TYPE type = nslist.get( e.getURI() );
	if ( type == null ) nslist.put( e.getURI(), TYPE.RELATION );
	else if ( !compatible( type, TYPE.RELATION ) ) return raiseError( e.getURI(), type, TYPE.RELATION );
	return TYPE.RELATION;
    }

    public TYPE visit( final RelationConstruction e ) throws AlignmentException {
	final Constructor op = e.getOperator(); // do we test the operator?
	boolean allright = true;
	for (final PathExpression re : e.getComponents()) {
	    TYPE tp = re.accept( this );
	    if ( !compatible( tp, TYPE.RELATION ) ) {
		raiseError( null, tp, TYPE.RELATION );
		allright = false;
	    }
	}
	if ( allright ) return TYPE.RELATION;
	else return TYPE.ERROR;
    }
    /*
    public TYPE visit( final RelationRestriction e ) throws AlignmentException {
	throw new AlignmentException( "Cannot dispatch RelationRestriction "+e );
    }
    */
    public TYPE visit(final RelationCoDomainRestriction c) throws AlignmentException {
	TYPE type = c.getCoDomain().accept( this );
	if ( !compatible( type, TYPE.CLASS ) ) return raiseError( null, type, TYPE.CLASS );
	return TYPE.RELATION;
    }

    public TYPE visit(final RelationDomainRestriction c) throws AlignmentException {
	TYPE type = c.getDomain().accept( this );
	if ( !compatible( type, TYPE.CLASS ) ) return raiseError( null, type, TYPE.CLASS );
	return TYPE.RELATION;
    }
    /*
    public TYPE visit( final InstanceExpression e ) throws AlignmentException {
	throw new AlignmentException( "Cannot handle InstanceExpression "+e );
	}*/

    public TYPE visit( final InstanceId e ) throws AlignmentException {
	TYPE type = nslist.get( e.getURI() );
	if ( type == null ) nslist.put( e.getURI(), TYPE.INSTANCE );
	else if ( type != TYPE.INSTANCE ) return raiseError( e.getURI(), type, TYPE.INSTANCE );
	return TYPE.INSTANCE;
    }
    
    public TYPE visit( final ValueExpression e ) throws AlignmentException {
	if ( e instanceof PathExpression ) {
	    TYPE type = e.accept( this );
	    if ( !compatible( type, TYPE.PROPERTY ) ) return raiseError( null, type, TYPE.PROPERTY );
	    return TYPE.VALUE;
	} else throw new AlignmentException( "Cannot handle ValueExpression "+e );
    }
	
    public TYPE visit( final Value e ) throws AlignmentException {
	return TYPE.VALUE;
    }
	
    public TYPE visit( final Apply e ) throws AlignmentException {
	// e.getOperation()
	boolean allright = true;
	for ( ValueExpression ve : e.getArguments() ) {
	    TYPE tp = ve.accept( this );
	    if ( !compatible( tp, TYPE.VALUE ) ) {
		raiseError( null, tp, TYPE.VALUE );
		allright = false;
	    }
	}
	if ( !allright ) return TYPE.ERROR;
	else return TYPE.VALUE; // but is it necessarily VALUE? not INSTANCE?
    }
	
    public TYPE visit( final Datatype e ) throws AlignmentException {
	return TYPE.DATATYPE;
    }
	
    public TYPE raiseError( final URI u, TYPE expT, TYPE foundT ) {
	error = true;
	if ( print ) System.err.println( "Incorrectly typed expression "+u+": Type "+foundT+" ("+expT+" expected)");
	return TYPE.ERROR;
    }

}
