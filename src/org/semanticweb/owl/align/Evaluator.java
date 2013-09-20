/*
 * $Id: Evaluator.java 1425 2010-04-06 20:25:39Z euzenat $
 *
 * Copyright (C) INRIA, 2004, 2007-2010
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

import java.io.PrintWriter;
import java.util.Properties;

/**
 * Assess the closeness between two ontology alignments.
 *
 * @author Jérôme Euzenat
 * @version $Id: Evaluator.java 1425 2010-04-06 20:25:39Z euzenat $ 
 */


public interface Evaluator {
    /** Creation **/
    //public Evaluator( OWLOntology onto1, OWLOntology onto2 );

    /**
     * Run the evaluation between the two ontologies.
     * Returns a double (between 0 and 1) providing an idea of the
     * proximity
     */
    public double eval( Properties param ) throws AlignmentException;

    /**
     * Run the evaluation between the two ontologies.
     * Returns a double (between 0 and 1) providing an idea of the
     * proximity
     * The additional argument allows to cache the ontologies if necessary
     * //@deprecated The OntologyCache is now internal, use eval( params ) instead
     **/
    //@Deprecated
    public double eval( Properties param, Object cache ) throws AlignmentException;

    /** Housekeeping **/
    /**
     * Outputs (in XML/RDF) a full report on the proximity of the two
     * ontologies.
     */
    public void write( PrintWriter writer ) throws java.io.IOException ;

    /**
     * Returns the results as a property list not further described but
     * suitable for display.
     */
    public Properties getResults();
}

