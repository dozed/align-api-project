/*
 * $Id: EmptyModification.java 1676 2012-02-15 12:16:50Z euzenat $
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

import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntProperty;

import java.util.Properties;

import fr.inrialpes.exmo.align.gen.Alterator;

import org.semanticweb.owl.align.Alignment;

public class EmptyModification extends BasicAlterator {

    protected boolean relocateSource = false;

    public EmptyModification( OntModel o ) {
	modifiedModel = o;
	// get the default namespace of the model
	modifiedOntologyNS = modifiedModel.getNsPrefixURI("");
    };

    // Clearly here setDebug, setNamespace are important

    public Alterator modify( Properties params ) {
	//System.err.println( "********************************************************************************************" );
	relocateSource = ( params.getProperty( "copy101" ) != null );

	if ( alignment == null ) {
	    initOntologyNS = modifiedOntologyNS;

	    alignment = new Properties();
	    alignment.setProperty( "##", initOntologyNS );

	    // Jena has a bug when URIs contain non alphabetical characters
	    // in the localName, it does not split correctly ns/localname
	    for ( OntClass cls : modifiedModel.listNamedClasses().toList() ) {
		String uri = cls.getURI();
		if ( uri.startsWith( modifiedOntologyNS ) ) {
		    String ln = uri.substring( uri.lastIndexOf("#")+1 );
		    //add them to the initial alignment
		    if ( ln != null && !ln.equals("") )	alignment.put( ln, ln );
		} 
	    }
	    for ( OntProperty prop : modifiedModel.listAllOntProperties().toList() ) {
		String uri = prop.getURI();
		if ( uri.startsWith( modifiedOntologyNS ) ) {
		    String ln = uri.substring( uri.lastIndexOf("#")+1 );
		    //add them to the initial alignment
		    if ( ln != null && !ln.equals("") )	alignment.put( ln, ln );
		}
	    }
	}
	return this;
    }

    // In case of 101, I want to have the empty test
    public void relocateTest( String base1, String base2 ) {
	super.relocateTest( relocateSource?base2:base1, base2 );
    }    

    //the initial reference alignment
    public void initializeAlignment( Properties al ) {
        alignment = al;
	initOntologyNS = al.getProperty( "##" );
    }

}
