/*
 * $Id: SKOSOntologyFactory.java 1477 2010-07-12 15:28:07Z euzenat $
 *
 * Copyright (C) INRIA, 2009-2010
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

package fr.inrialpes.exmo.ontowrap.skosapi;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.NoSuchElementException;

import org.semanticweb.skosapibinding.SKOSManager;
import org.semanticweb.skos.SKOSCreationException;
import org.semanticweb.skos.SKOSDataset;
import org.semanticweb.skos.SKOSDataFactory;

import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntologyCache;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public class SKOSOntologyFactory extends OntologyFactory {

    private static URI formalismUri = null;
    private static String formalismId = "SKOS1.0";
    private static OntologyCache<SKOSThesaurus> cache = null;

    private SKOSManager manager;
    private SKOSDataFactory factory;

    public SKOSOntologyFactory() throws OntowrapException {
	cache = new OntologyCache<SKOSThesaurus>();
	try { 
	    formalismUri = new URI("http://www.w3.org/2004/02/skos/core#");
	} catch (URISyntaxException ex) { ex.printStackTrace(); } // should not happen
	try {
	    manager = new SKOSManager();
	} catch (SKOSCreationException sce) {
	    throw new OntowrapException( "Cannot initialise SKOSManager ", sce);
	}
	factory = manager.getSKOSDataFactory();
    }

    @Override
    public SKOSThesaurus newOntology( Object ontology ) throws OntowrapException {
	if ( ontology instanceof SKOSDataset ) {
	    SKOSThesaurus onto = null;
	    onto = new SKOSThesaurus();
	    onto.setFormalism( formalismId );
	    onto.setFormURI( formalismUri );
	    onto.setOntology( (SKOSDataset)ontology );
	    onto.setFactory( factory );
	    // This is the URI of the corresponding OWL API Ontology
	    URI uri = ((SKOSDataset)ontology).getURI();
	    onto.setURI( uri );
	    cache.recordOntology( uri, onto );
	    return onto;
	} else {
	    throw new OntowrapException( "Argument is not an SKOSDataset: "+ontology );
	}
    }

    @Override
    public SKOSThesaurus loadOntology( URI uri ) throws OntowrapException {
	//System.err.println(" Loading "+uri );
	SKOSThesaurus onto = null;
	onto = cache.getOntologyFromURI( uri );
	if ( onto != null ) return onto;
	onto = cache.getOntology( uri );
	if ( onto != null ) return onto;
	SKOSDataset dataset = null;
	try {
	    dataset = manager.loadDataset( uri );
	} catch (SKOSCreationException sce) {
	    throw new OntowrapException( "Cannot load ontology: "+uri, sce);
	}
	onto = new SKOSThesaurus();
	onto.setFormalism( formalismId );
	onto.setFormURI( formalismUri );
	onto.setOntology( dataset );
	onto.setFactory( factory );
	onto.setFile( uri );
	// This is the URI of the corresponding OWL API Ontology
	onto.setURI( dataset.getURI() );
	//cache.recordOntology( uri, onto );
	
	return onto;
    }

    @Override
    public void clearCache() throws OntowrapException {
	cache.clear();
    }

}
