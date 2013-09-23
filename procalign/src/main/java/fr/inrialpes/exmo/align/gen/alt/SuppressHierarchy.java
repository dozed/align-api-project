/*
 * $Id: SuppressHierarchy.java 1842 2013-03-24 17:42:41Z euzenat $
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
import java.util.ArrayList;

import fr.inrialpes.exmo.align.gen.Alterator;
import fr.inrialpes.exmo.align.gen.ParametersIds;

public class SuppressHierarchy extends BasicAlterator {

    public SuppressHierarchy( Alterator om ) {
	initModel( om );
    };

    public Alterator modify( Properties params ) {
	String p = params.getProperty( ParametersIds.NO_HIERARCHY );
	if ( p == null ) return null;
        buildClassHierarchy();                                                  //builds the class hierarchy if necessary
        for( int level = classHierarchy.getMaxLevel(); level > 1; level-- ) {
            noHierarchy ( level );
        }
	return this; // useless
    };

    // This suppress attach all classes of a level to the level above
    // JE: why not directly attach all classes to Thing (or the root) and set childs to all these classes but root to null? (JE2012: corrected)
    public void noHierarchy ( int level ) {
        if ( level == 1 ) return;
        ArrayList<OntClass> levelClasses = new ArrayList<OntClass>();		//the list of classes from that level
        ArrayList<OntClass> parentLevelClasses = new ArrayList<OntClass>();	//the list of parent of the child classes from that level
        ArrayList<OntClass> superLevelClasses = new ArrayList<OntClass>();	//the list of parent of the parent classes from that level
        buildClassHierarchy();                                                  //check if the class hierarchy is built
        classHierarchy.flattenClassHierarchy( modifiedModel, level, levelClasses, parentLevelClasses, superLevelClasses);
        int size = levelClasses.size();

        for ( int i=0; i<size; i++ ) {
            OntClass childClass = levelClasses.get( i );			//child class
            OntClass parentClass = parentLevelClasses.get( i );                 //parent class
            //all the classes are subclasses of owl: Thing
	    OntClass superClass = superLevelClasses.get( i );                //parent class of the child class parents
            //if ( superClass != null ) childClass.addSuperClass( superClass );
	    if ( !parentClass.getURI().equals( "Thing" ) ) {
		childClass.removeSuperClass( parentClass );
            }
	    parentClass.removeSubClass( childClass );
        }
    }
}
