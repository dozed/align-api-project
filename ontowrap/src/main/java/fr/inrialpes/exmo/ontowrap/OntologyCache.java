/*
 * $Id: OntologyCache.java 1459 2010-06-17 08:31:04Z euzenat $
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

package fr.inrialpes.exmo.ontowrap; 

// import java classes
import java.util.Hashtable;
import java.util.Enumeration;
import java.net.URI;

/**
 * This caches the loaded ontologies so that it is possible
 * to share them between alignments
 * as well as to unload them if necessary.
 * 
 * @author Jérôme Euzenat
 * @version $Id: OntologyCache.java 1459 2010-06-17 08:31:04Z euzenat $ 
 * 
 * This class should be parameterized by O subClassOf LoadedOntology 
 */

public class OntologyCache <O extends LoadedOntology> {
 
  /** The list of currently loaded ontologies as a function:
   * URI --> Ontology
   * This is the ontology URI, NOT its filename
   */
    Hashtable<URI,O> ontologies = null;
    Hashtable<URI,O> ontologyUris = null;
    
    public OntologyCache() {
	ontologies = new Hashtable<URI,O>();
	ontologyUris = new Hashtable<URI,O>();
    }
  
    public void recordOntology( URI uri, O ontology ){
	ontologies.put( uri, ontology );
	ontologyUris.put( ontology.getURI(), ontology );
    }

    public O getOntology( URI uri ){
	return ontologies.get( uri );
    }

    public O getOntologyFromURI( URI uri ){
	return ontologyUris.get( uri );
    }

    public void unloadOntology( URI uri, O ontology ) throws OntowrapException {
	O o = ontologyUris.get(uri);
	o.unload();
	ontologyUris.remove( uri );
	ontologies.remove( uri );
    }

    /* debugging utility */
    public void displayCache(){
	System.err.println("CACHE: "+ontologies.size()+"/"+ontologyUris.size()+" elements cached");
	// No way to make this iterable??
	//for ( URI u : ontologies.keys() ){
	for( Enumeration<URI> e = ontologies.keys(); e.hasMoreElements(); ){
	URI u = e.nextElement();
	    LoadedOntology o = ontologies.get( u );
	    System.err.println( "      "+u );
	    System.err.println( "      "+o.getURI() );
	    System.err.println( "  --> "+o+" ("+o.getOntology()+")" );
	}
    };

    public void clear() throws OntowrapException {
	for ( LoadedOntology o : ontologies.values() ){
	    o.unload();
	}
	ontologyUris.clear();
	ontologies.clear();
    }

}
