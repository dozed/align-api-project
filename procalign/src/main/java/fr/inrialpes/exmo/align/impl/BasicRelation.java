/*
 * $Id: BasicRelation.java 1681 2012-02-16 10:11:59Z euzenat $
 *
 * Copyright (C) INRIA, 2003-2005, 2007, 2009-2012
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

package fr.inrialpes.exmo.align.impl;

import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor;

import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Relation;

import org.xml.sax.ContentHandler;

import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import java.lang.ClassNotFoundException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 * Represents an ontology alignment relation.
 *
 * @author Jérôme Euzenat
 * @version $Id: BasicRelation.java 1681 2012-02-16 10:11:59Z euzenat $
 */

public class BasicRelation implements Relation {

    private static Map<String, Class<?>> classIndex = null;

    static Class<?> getClass( String label ) {
	if ( label == null ) 
	    throw new NullPointerException("The string to search must not be null");
	if ( classIndex == null ){
	    classIndex = new HashMap<String, Class<?>>();
	    try {
		Class<?> eq = Class.forName("fr.inrialpes.exmo.align.impl.rel.EquivRelation");
		Class<?> sub = Class.forName("fr.inrialpes.exmo.align.impl.rel.SubsumedRelation");
		Class<?> sup = Class.forName("fr.inrialpes.exmo.align.impl.rel.SubsumeRelation");
		Class<?> dis = Class.forName("fr.inrialpes.exmo.align.impl.rel.IncompatRelation");
		Class<?> ins = Class.forName("fr.inrialpes.exmo.align.impl.rel.InstanceOfRelation");
		Class<?> has = Class.forName("fr.inrialpes.exmo.align.impl.rel.HasInstanceRelation");
		Class<?> nti = Class.forName("fr.inrialpes.exmo.align.impl.rel.NonTransitiveImplicationRelation");
		
		classIndex.put( "Equivalence", eq );
		classIndex.put( "=", eq );
		classIndex.put( "equivalence", eq );
		classIndex.put( "ClassMapping", eq );
		
		classIndex.put( "Subsumes", sup );
		classIndex.put( ">", sup );
		classIndex.put( "&gt;", sup );
		
		classIndex.put( "SubsumedBy", sub );
		classIndex.put( "<", sub );
		classIndex.put( "&lt;", sub );
		
		classIndex.put("><", dis );
		classIndex.put("%", dis );
		classIndex.put("DisjointFrom",dis);
		classIndex.put("Disjoint",dis);
		classIndex.put("disjointFrom",dis);
		classIndex.put("disjoint",dis);
	    
		classIndex.put( "InstanceOf", ins );
	    
		classIndex.put( "HasInstance", has );

		classIndex.put( "~>", nti );
		classIndex.put( "~&gt;", nti );
	    } catch ( ClassNotFoundException cnfe ) {
		cnfe.printStackTrace(); // should never occur
	    }
	}
	return classIndex.get(label);
    }

    public void accept( TypeCheckingVisitor visitor ) throws AlignmentException {
	visitor.visit(this);
    }

    public void accept( AlignmentVisitor visitor) throws AlignmentException {
        visitor.visit( this );
    }

    /**
     * The initial relation given by the user (through parser for instance)
     * This is never used in subclass relations (because they share one relation)
     */
    protected String relation = null;

    /** printable format **/
    public String getRelation() {
	return relation;
    }

    /**
     * The pretty relation attached to the relation type
     * This is overriden as static in subclasses
     */
    protected String prettyLabel = null;

    public String getPrettyLabel() {
	return prettyLabel;
    }

    /**
     * The name to use if no other information is available
     */
    public String getClassName() {
	return getClass().toString();
    }

    /** Creation **/
    public BasicRelation( String rel ){
	relation = rel;
    }

    /**
     * The constructor to use
     */
    public static Relation createRelation( String rel ) {
	Class<?> relationClass = getClass( rel );
	if ( relationClass != null ) {
	    try { // Get existing relation... 
		Method m = relationClass.getMethod("getInstance");
		return (Relation)m.invoke(null);
	    } catch ( Exception ex ) {}; // should not happen
	}
	try { // Create a relation from classname
	    relationClass = Class.forName(rel);
	    Constructor relationConstructor = relationClass.getConstructor((Class[])null);
	    return (Relation)relationConstructor.newInstance((Object[])null);
	} catch ( Exception ex ) {
	    //ex.printStackTrace();
	    //Otherwise, just create a Basic relation
	    return new BasicRelation( rel );
	}
    }

    /** By default the inverse is the relation itself **/
    public Relation inverse() {
	return this;
    }

    /** By default... no composition possible **/
    public Relation compose( Relation r ) {
    	return null;
    }

    /** Are the two relations equal **/
    public boolean equals( Relation r ) {
	if ( r instanceof BasicRelation ){
	    return ( relation.equals( ((BasicRelation)r).getRelation() ) );
	} else {
	    return false;
	}
    }
    public int hashCode() {
	return 19+relation.hashCode();
    }

    /** Housekeeping **/
    public void dump( ContentHandler h ){};

    /** This is kept for displayig more correctly the result **/
    public void write( PrintWriter writer ) {
	if ( relation != null ) {
	    writer.print( relation );
	} else if ( getPrettyLabel() != null ) {
	    writer.print( getPrettyLabel() );
	} else {
	    writer.print( getClassName() );
	}
    }
}


