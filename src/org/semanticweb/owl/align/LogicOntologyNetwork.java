/*
 * $Id: LogicOntologyNetwork.java 1828 2013-03-09 18:18:05Z euzenat $
 *
 * Copyright (C) INRIA, 2009, 2013
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

import java.lang.Cloneable;
import java.lang.Iterable;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Set;
import java.net.URI;

/**
 * Represents a distributed system of aligned ontologies or network of ontologies.
 *
 * @author Jérôme Euzenat
 * @version $Id: LogicOntologyNetwork.java 1828 2013-03-09 18:18:05Z euzenat $ 
 */


public interface LogicOntologyNetwork extends OntologyNetwork {

    public void setSemantics( String s );
    public String getSemantics();
    public boolean isConsistent() throws AlignmentException; 
    public boolean isEntailed( Alignment al ) throws AlignmentException;
    //public boolean isEntailed( URI ontology, Object axiom ) throws AlignmentException;

}
