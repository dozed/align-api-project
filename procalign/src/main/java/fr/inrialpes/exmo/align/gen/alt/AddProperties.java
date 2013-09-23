/*
 * $Id: AddProperties.java 1659 2011-12-28 10:50:46Z euzenat $
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

package fr.inrialpes.exmo.align.gen.alt;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.ObjectProperty;
import com.hp.hpl.jena.ontology.DatatypeProperty;
import com.hp.hpl.jena.vocabulary.XSD;

import java.util.Properties;
import java.util.List;
import java.util.Random;

import fr.inrialpes.exmo.align.gen.Alterator;
import fr.inrialpes.exmo.align.gen.ParametersIds;


public class AddProperties extends BasicAlterator {

    public AddProperties( Alterator om ) {
	initModel( om );
    };

    public Alterator modify( Properties params ) {
	String p = params.getProperty( ParametersIds.ADD_PROPERTIES );
	if ( p == null ) return null;
	float percentage = Float.parseFloat( p );
        List<OntProperty> properties = this.getOntologyProperties();
        List<OntClass> classes = this.getOntologyClasses();
        ObjectProperty property = null;
        DatatypeProperty d = null;
        Random classRand = new Random();
        int index;
        int nbClasses = classes.size();                                         //the number of classes
        int nbProperties = properties.size();                                   //the number of properties
        int toBeAdd = Math.round( percentage*nbProperties );                         //the number of properties to be add

        for ( int i=0; i<toBeAdd/2; i++ ) {                                     //add object properties
            //p = modifiedModel.createObjectProperty( modifiedOntologyNS + "OBJECT_PROPERTY_" + getRandomString() );
            property = modifiedModel.createObjectProperty( modifiedOntologyNS + getRandomString() );
            index = classRand.nextInt( nbClasses );                             //pick random domain
            property.addDomain( classes.get( index ) );
            index = classRand.nextInt( nbClasses );                             //pick random range
            property.addRange( classes.get( index ) );
        }

        for ( int i=toBeAdd/2; i<toBeAdd; i++ ) {                               //add datatype properties
            //d = modifiedModel.createDatatypeProperty( modifiedOntologyNS + "DATATYPE_PROPERTY_" + getRandomString() );
            d = modifiedModel.createDatatypeProperty( modifiedOntologyNS +  getRandomString() );
            index = classRand.nextInt( nbClasses );                             //add domain
            d.addDomain( classes.get( index ) );
            d.addRange( XSD.xstring );						//add range -> string
        }
	return this; // useless
    };

}
