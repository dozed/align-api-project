/*
 * $Id: Alterator.java 1659 2011-12-28 10:50:46Z euzenat $
 *
 * Copyright (C) 2011, INRIA
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

package fr.inrialpes.exmo.align.gen;

import java.util.Properties;

import org.semanticweb.owl.align.Alignment;

import com.hp.hpl.jena.ontology.OntModel;

/**
 * An abstract test generator which takes as input an ontology and an
 * alignment between this ontology and another one and transform the
 * ontology and the alignment accordingly to a type of alteration.
 *
 * It follows a particular lifecycle
 */
public interface Alterator {

    /**
     * It is created either:
     * - from a seed ontology and generate the alignment between this
     *   ontology itself
     * - from a previous alterator from which it will take the output
     *   ontology and alignment as input.
     */
    //public Alterator( Alterator om );

    /**
     * the namespace of the input ontology
     */
    public String getNamespace();
    /**
     * the namespace of the source ontology in the input alignment
     */
    public String getBase();

    /**
     * modify applies the alteration to the input (the results are kept in 
     * internal structures.
     */
    public Alterator modify( Properties params );

    // Temporary
    /**
     * getProtoAlignment, getProtoOntology, getHierarchy
     * are used for accessing these internal structure at creation time.
     */
    public Properties getProtoAlignment();
    public OntModel getProtoOntology();
    public ClassHierarchy getHierarchy();

    /**
     * Modifies the namespaces of source and target ontologies
     * (for the main purpose of outputing them)
     */
    public void relocateTest( String namespace1, String namespace2 );
    public void relocateTest( String namespace2 );

    /**
     * Returns the altered Alignment and Ontology in output form
     */
    public Alignment getAlignment();
    public OntModel getModifiedOntology();

}
