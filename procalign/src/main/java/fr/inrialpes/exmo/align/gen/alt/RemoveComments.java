/*
 * $Id: RemoveComments.java 1659 2011-12-28 10:50:46Z euzenat $
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

import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.Individual;

import java.util.Properties;
import java.util.List;
import java.util.ArrayList;
import java.util.Iterator;

import fr.inrialpes.exmo.align.gen.Alterator;
import fr.inrialpes.exmo.align.gen.ParametersIds;


public class RemoveComments extends BasicAlterator {

    public RemoveComments( Alterator om ) {
	initModel( om );
    };

    public Alterator modify( Properties params ) {
	String p = params.getProperty( ParametersIds.REMOVE_COMMENTS );
	if ( p == null ) return null;
	float percentage = Float.parseFloat( p );
        removeClassesComments ( percentage );
        removeIndividualsComments ( percentage );
        removePropertiesComments ( percentage );
        removeOntologiesComments ( percentage );
	return this; // useless
    };

    //remove classes comments
    @SuppressWarnings("unchecked")
    public void removeClassesComments ( float percentage ) {
        ArrayList<Literal> comments = new ArrayList<Literal>();
        List<OntClass> classes = modifiedModel.listNamedClasses().toList();
        ArrayList<OntClass> classesTo = new ArrayList<OntClass>();
        int nbClasses = classes.size();
        int toBeRemoved = Math.round( percentage*nbClasses );                      //number of classes comments to be removed

        int [] n = this.randNumbers(nbClasses, toBeRemoved);
        for ( int i=0; i<toBeRemoved; i++ ) {
            OntClass cls = classes.get(n[i]);
            classesTo.add( cls );
        }

        for ( OntClass c : classesTo ) {
            for (Iterator it2 = c.listComments(null); it2.hasNext();)
                comments.add(((Literal) it2.next()));
            for (Literal lit : comments)                                        // remove comments
                c.removeComment( lit );
            comments.clear();
        }
    }

    //remove properties comments
    @SuppressWarnings("unchecked")
    public void removePropertiesComments ( float percentage ) {
        ArrayList<Literal> comments = new ArrayList<Literal>();                 // an array list to hold all the comments
        List<OntProperty> properties = modifiedModel.listAllOntProperties().toList();
        ArrayList<OntProperty> propertiesTo = new ArrayList<OntProperty>();
        int nbProperties = properties.size();
        int toBeRemoved = Math.round( percentage*nbProperties );                   //the number of properties comments to be removed

        int [] n = this.randNumbers(nbProperties, toBeRemoved);
        for ( int i=0; i<toBeRemoved; i++ ) {
            OntProperty p = properties.get(n[i]);
            propertiesTo.add( p );
        }

        for ( OntProperty prop : propertiesTo ) {
            for (Iterator it2 = prop.listComments(null); it2.hasNext();)        // get all comments
                comments.add(((Literal) it2.next()));
            for (Literal lit : comments) 					//remove comments
                prop.removeComment( lit );
            comments.clear();
        }
    }

    //remove individuals comments
    @SuppressWarnings("unchecked")
    public void removeIndividualsComments ( float percentage ) {
        ArrayList<Literal> comments = new ArrayList<Literal>();                 // an array list to hold all the comments
        List<Individual> individuals = modifiedModel.listIndividuals().toList();
        ArrayList<Individual> individualsTo = new ArrayList<Individual>();
        int nbIndividuals = individuals.size();
        int toBeRemoved = Math.round( percentage*nbIndividuals );                  //number of classes to be removed

        int [] n = this.randNumbers(nbIndividuals, toBeRemoved);
        for ( int i=0; i<toBeRemoved; i++ ) {
            Individual indiv = individuals.get(n[i]);
            individualsTo.add( indiv );
        }
        for ( Individual indiv : individuals ) {
            for (Iterator it2 = indiv.listComments(null); it2.hasNext(); )      //get all comments
                comments.add( ((Literal) it2.next()) );
            for (Literal lit : comments )					//remove comments
                indiv.removeComment( lit );
            comments.clear();
        }
    }

    //remove Ontologies comments
    @SuppressWarnings("unchecked")
    public void removeOntologiesComments ( float percentage ) {
        ArrayList<Literal> comments = new ArrayList<Literal>();                 // an array list to hold all the comments
        List<Ontology> ontologies = modifiedModel.listOntologies().toList();
        ArrayList<Ontology> ontologiesTo = new ArrayList<Ontology>();
        int nbOntologies = ontologies.size();
        int toBeRemoved = Math.round( percentage*nbOntologies );                   //the number of Ontologies comments to be removed

        int [] n = this.randNumbers(nbOntologies, toBeRemoved);
        for ( int i=0; i<toBeRemoved; i++ ) {
            Ontology onto = ontologies.get(n[i]);
            ontologiesTo.add( onto );
        }

        for ( Ontology onto : ontologies ) {
            for (Iterator it2 = onto.listComments(null); it2.hasNext(); )       // get all comments
                comments.add(((Literal) it2.next()));
            for ( Literal lit : comments )					//remove all comments
                onto.removeComment( lit );
            comments.clear();
        }
    }

}
