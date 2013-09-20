/*
 * $Id: URICell.java 1325 2010-03-10 11:47:07Z euzenat $
 *
 * Copyright (C) INRIA, 2007-2008, 2010
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 */

package fr.inrialpes.exmo.align.impl; 

import java.io.PrintStream;
import java.io.IOException;
import java.util.Comparator;
import java.lang.ClassNotFoundException;
import java.net.URI;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.Relation;

import fr.inrialpes.exmo.align.impl.rel.*;

/**
 * Represents an ontology alignment correspondence between two URIs
 *
 * @author Jérôme Euzenat
 * @version $Id: URICell.java 1325 2010-03-10 11:47:07Z euzenat $ 
 */

public class URICell extends BasicCell {
    public void accept( AlignmentVisitor visitor) throws AlignmentException {
        visitor.visit( this );
    }

    /** Creation **/
    //    public URICell( Object ob1, Object ob2 ) throws AlignmentException {
    //	super( ob1, ob2 );
    //    };

    public URICell( String id, URI ob1, URI ob2, Relation rel, double m ) throws AlignmentException {
    	super( id, ob1, ob2, rel, m );
    }

    //    public URICell( Object ob1, Object ob2, String rel, double m ) throws AlignmentException {
    //	super( ob1, ob2, rel, m );
    //    }

    //public URICell( Object ob1, Object ob2, Relation rel, double m ) throws AlignmentException {
    //	super( ob1, ob2, rel, m );
    //};

    public URI getObject1AsURI( Alignment al ) throws AlignmentException { 
	return (URI)object1; 
    };
    public URI getObject2AsURI( Alignment al ) throws AlignmentException { 
	return (URI)object2; 
    };
    //public Object getObject1(){ return object1; };
    //public Object getObject2(){ return object2; };
    // We could check that the given values are URIs
    //public void setObject1( Object ob ) throws AlignmentException {
    //	object1 = ob;
    //}
    //public void setObject2( Object ob ) throws AlignmentException {
    //	object2 = ob;
    //}

    public Cell inverse() throws AlignmentException {
	return (Cell)new URICell( (String)null, (URI)object2, (URI)object1, relation.inverse(), strength );
	// The same should be done for the measure
    }

}

