/*
 * $Id: RemoveClasses.java 1676 2012-02-15 12:16:50Z euzenat $
 *
 * Copyright (C) 2011-2012, INRIA
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

package fr.inrialpes.exmo.align.gen.alt;

import com.hp.hpl.jena.ontology.OntClass;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

import fr.inrialpes.exmo.align.gen.Alterator;
import fr.inrialpes.exmo.align.gen.ParametersIds;

public class RemoveClasses extends BasicAlterator {

    public RemoveClasses( Alterator om ) {
	initModel( om );
    };

    public Alterator modify( Properties params ) {
	String p = params.getProperty( ParametersIds.REMOVE_CLASSES );
	if ( p == null ) return null;
	float percentage = Float.parseFloat( p );
        List<OntClass> classes = this.getOntologyClasses();			//the list of classes from Ontologu
        List<OntClass> removedClasses = new ArrayList<OntClass>();
        List<String> cl = new ArrayList<String>();
        HashMap<String, String> uris = new HashMap<String, String>();           //the HashMap of strings
        int nbClasses = classes.size();						//number of classes
        buildClassHierarchy();							//build the class hierarchy if necessary
        int toBeRemoved =  Math.round(percentage*nbClasses);			//the number of classes to be removed

        //build the list of classes to be removed
        int [] n = this.randNumbers( nbClasses, toBeRemoved );
        for ( int i=0; i<toBeRemoved; i++ ) {
            OntClass cls = classes.get(n[i]);
            removedClasses.add( cls );
            cl.add( getLocalName( cls.getURI() ) );                             //builds the list of labels of classes to be removed
        }

        for ( OntClass cls : removedClasses ) {					//remove the classes from the list
            String parentURI = removeClass( cls );
            uris.put( cls.getURI(), parentURI );
        }

        //checks if the class appears like unionOf.. and replaces its appearence with the superclass
        modifiedModel = changeDomainRange( uris );
        
        //remove the URI of the class from the reference alignment
        for ( String key : alignment.stringPropertyNames() ) {
            if ( cl.contains( alignment.getProperty( key ) ) )
                alignment.remove( key );
        }
	return this; // useless
    };


}
