/*
 * $Id: ObjectAlignment.java 1820 2013-03-06 10:13:00Z euzenat $
 *
 * Copyright (C) INRIA, 2003-2011, 2013
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

import java.util.Enumeration;
import java.net.URI;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

/**
 * Represents an OWL ontology alignment. An ontology comprises a number of
 * collections. Each ontology has a number of classes, properties and
 * individuals, along with a number of axioms asserting information about those
 * objects.
 *
 * @author Jérôme Euzenat
 * @version $Id: ObjectAlignment.java 1820 2013-03-06 10:13:00Z euzenat $
 */

public class ObjectAlignment extends BasicAlignment {

    protected ObjectAlignment init = null;

    public ObjectAlignment() {}

    public void init(Object onto1, Object onto2) throws AlignmentException {
	if ( (onto1 instanceof LoadedOntology && onto2 instanceof LoadedOntology) ){
	    super.init( onto1, onto2 );
	} else if ( onto1 instanceof URI && onto2 instanceof URI ) {
		super.init( loadOntology( (URI)onto1 ),
			    loadOntology( (URI)onto2 ) );
	} else {
	    throw new AlignmentException("Arguments must be LoadedOntology or URI");
	};
    }

    public LoadedOntology<Object> ontology1(){
	return (LoadedOntology<Object>)onto1;
    }

    public LoadedOntology<Object> ontology2(){
	return (LoadedOntology<Object>)onto2;
    }

    public void loadInit( Alignment al ) throws AlignmentException {
	if ( al instanceof URIAlignment ) {
	    init = AlignmentTransformer.toObjectAlignment((URIAlignment) al);
	} else if ( al instanceof ObjectAlignment ) {
	    init = (ObjectAlignment)al;
	}
    }

    public URI getOntology1URI() { return onto1.getURI(); };

    public URI getOntology2URI() { return onto2.getURI(); };

    public Cell createCell(String id, Object ob1, Object ob2, Relation relation, double measure) throws AlignmentException {
	return new ObjectCell( id, ob1, ob2, relation, measure);
    }

    /**
     * Generate a copy of this alignment object
     */
    public ObjectAlignment createNewAlignment( Object onto1, Object onto2 ) throws AlignmentException {
	ObjectAlignment align = new ObjectAlignment();
	align.init( onto1, onto2 );
	return align;
    }

    /**
     * This is a clone with the URI instead of Object objects
     */
    public URIAlignment toURIAlignment() throws AlignmentException {
	return toURIAlignment( false );
    }

    public URIAlignment toURIAlignment( boolean strict ) throws AlignmentException {
	URIAlignment align = new URIAlignment();
	align.init( getOntology1URI(), getOntology2URI() );
	align.setType( getType() );
	align.setLevel( getLevel() );
	align.setFile1( getFile1() );
	align.setFile2( getFile2() );
	align.setExtensions( convertExtension( "EDOALURIConverted", this.getClass().getName()+"#toURI" ) );
	for (Enumeration e = getElements(); e.hasMoreElements();) {
	    Cell c = (Cell)e.nextElement();
	    try {
		align.addAlignCell( c.getId(), c.getObject1AsURI(this), c.getObject2AsURI(this), c.getRelation(), c.getStrength() );
	    } catch (AlignmentException aex) {
		// Sometimes URIs are null, this is ignored
		if ( strict ) {
		    throw new AlignmentException( "Cannot convert to URIAlignment" );
		}
	    }
	};
	return align;
    }

    static LoadedOntology loadOntology( URI ref ) throws AlignmentException {
	OntologyFactory factory = OntologyFactory.getFactory();
	try {
	    return factory.loadOntology( ref );
	} catch ( OntowrapException owex ) {
	    throw new AlignmentException( "Cannot load ontology "+ref, owex );
	}
    }
}

