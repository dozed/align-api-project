/*
 * $Id: Cell.java 1189 2010-01-03 17:57:13Z euzenat $
 *
 * Copyright (C) INRIA, 2003-2005, 2007-2009
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

package org.semanticweb.owl.align;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;

import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;

/**
 * Represents an correspondence between ontology entities.
 * An ontology comprises a number of
 * collections. Each ontology has a number of classes, properties and
 * individuals, along with a number of axioms asserting information
 * about those objects.
 *
 * @author Jérôme Euzenat
 * @version $Id: Cell.java 1189 2010-01-03 17:57:13Z euzenat $
 */


public interface Cell extends Comparable<Cell>, Visitable {

    /** Creation **/

    public String getId();
    public void setId( String id );
    public String getSemantics();
    public void setSemantics( String s );
    public Object getObject1();
    public Object getObject2();
    /**
     * //@deprecated use getObject1AsURI( null ) instead
     */
    //@Deprecated
    public URI getObject1AsURI() throws AlignmentException;
    /**
     * //@deprecated use getObject2AsURI( null ) instead
     */
    //@Deprecated
    public URI getObject2AsURI() throws AlignmentException;
    public URI getObject1AsURI( Alignment al ) throws AlignmentException;
    public URI getObject2AsURI( Alignment al ) throws AlignmentException;
    public void setObject1( Object ob ) throws AlignmentException;
    public void setObject2( Object ob ) throws AlignmentException;
    public Relation getRelation();
    public void setRelation( Relation r );
    public double getStrength();
    public void setStrength( double m );
    public boolean equals( Cell c );

    /**
     * Extensions are a way to read and add other information (metadata)
     * to the Cell structure itself.
     * getExtensions returns a set of tripes: uri*label*value
     * all three being String
     */
    public Collection<String[]> getExtensions();
    //public Parameters getExtensions();
    //public void setExtensions( Parameters param );
    public String getExtension( String uri, String label );
    public void setExtension( String uri, String label, String value );

    public Cell inverse() throws AlignmentException;
    public Cell compose(Cell c) throws AlignmentException;

    /** Housekeeping **/
    public void dump(ContentHandler h);
    //    public void write( PrintStream writer ) throws IOException, AlignmentException;
    //    public void write( PrintWriter writer ) throws IOException, AlignmentException;

}


