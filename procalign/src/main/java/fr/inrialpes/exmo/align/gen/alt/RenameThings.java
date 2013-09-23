/*
 * $Id: RenameThings.java 1676 2012-02-15 12:16:50Z euzenat $
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

import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntClass;

//Google API classes
import com.google.api.GoogleAPI;
import com.google.api.translate.Language;
import com.google.api.translate.Translate;

/*
//WordNet API classes
import edu.smu.tspell.wordnet.Synset;
import edu.smu.tspell.wordnet.WordNetDatabase;
*/

// JE: reengineer this class... (what have we to rename)

//activeRandomString is true -> we replace the label with a random string
//activeTranslateString is true -> we translate the label

import java.util.List;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * This is only an abstract class gathering the renaming routines
 */
public abstract class RenameThings extends BasicAlterator {

    // -------------------------
    // Label replacement

    //replaces the label of the property
    public void replacePropertyLabel( String uri, String newLabel, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation ) {
        OntProperty prop = modifiedModel.getOntProperty( uri );
        if ( prop.getLabel( "en" ) != null ) {
            if ( activeTranslateString ) {
                prop.setLabel( newLabel, "fr" );
            } else {
                prop.setLabel( newLabel, "en" );
            }
        }
    }

    // get the URIs of the properties and their translation
    public HashMap<String, String> getPropertiesIdentifiers( float percentage, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation) {
        HashMap<String, String> propertiesIdentifiers = new HashMap<String, String>();	//the HashMap of the properties identifiers
	List<String> propertiesName = new ArrayList<String>();                  //the properties identifiers

        List<OntProperty> propertiesTo = new ArrayList<OntProperty>();          //the list of properties to be renamed
	List<OntProperty> notRenamedProperties = new ArrayList<OntProperty>();  //the list of not renamed properties
        List<OntProperty> properties = getOntologyProperties();                 //the list of all the properties

        int nbProperties, toBeRenamed, renamedProperties;

        // build the list of all unrenamed properties from the model
        for ( OntProperty p : properties ) {
	    String local = getLocalName( p.getURI() );
            if ( alignment.containsKey( local ) ) {
                if ( alignment.getProperty( local ).equals( local ) )
                    notRenamedProperties.add( p );      //add the property to not renamed properties  
            }
        }

        nbProperties = properties.size();                                       //the number of renamed properties
        renamedProperties = nbProperties - notRenamedProperties.size();
        toBeRenamed = Math.round(percentage*nbProperties) - renamedProperties;       // -renamedProperties -> for Benchmark

	// toBeRenamed is negative when properties have been added to the model
	if ( toBeRenamed < 0 ) toBeRenamed = 0;
        //builds the list of properties to be renamed
	int [] n = randNumbers(notRenamedProperties.size(), toBeRenamed);
	for ( int i=0; i<toBeRenamed; i++ ) {
		OntProperty p = notRenamedProperties.get(n[i]);
		propertiesTo.add(p);
                if ( getNameSpace( p.getURI() ).equals( modifiedOntologyNS ) ) 
                    propertiesName.add( getLocalName( p.getURI() ) );
	}

	for ( OntProperty prop : propertiesTo ) {
	    String prefix = getNameSpace( prop.getURI() );
		String localName = getLocalName( prop.getURI() );
		//has the same Namespace as the Ontology Namespace
                if ( prefix.equals( modifiedOntologyNS ) ) {
                    if ( !propertiesIdentifiers.containsKey( localName ) ) {
                        if ( activeTranslateString ) {                          //replace the URI with the translated one
                            String translateStrg = parseString ( localName, true, false);
                            propertiesIdentifiers.put( localName , translateStrg );
                            replacePropertyLabel( prop.getURI(), translateStrg, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );

                            if ( alignment.containsKey( localName ) ) {        //alignment.remove( prop.getURI() );
                                alignment.put( localName, translateStrg );//the reference alignment
                            }
                        } else if ( activeRandomString ) {                        //replace the URI with a random string
                            String newStrg = getRandomString();
                            propertiesIdentifiers.put( localName , newStrg );
                            replacePropertyLabel( prop.getURI(), newStrg, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                            if ( alignment.containsKey( localName ) ) {        //alignment.remove( prop.getURI() );
                                alignment.put( localName, newStrg);//the reference alignment
                            }
                        } else if ( activeSynonym ) {
                            String synonym = parseString (localName, false, true);
                            if ( propertiesName.contains( synonym ) )
                                propertiesIdentifiers.put( localName, localName );
                            else  {
                                propertiesIdentifiers.put( localName, synonym );
                                replacePropertyLabel( prop.getURI(), synonym, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                                if ( alignment.containsKey( localName ) ) {    //alignment.remove( prop.getURI() );
                                    alignment.put( localName, synonym );	//the reference alignment
                                }
                            }
                        } else if ( activeStringOperation == 1 ) {                //replace the URI with the UpperCase URI
                            propertiesIdentifiers.put( localName , localName.toUpperCase() );
                            replacePropertyLabel( prop.getURI(), localName.toUpperCase(), activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                            if ( alignment.containsKey( localName ) ) {        //alignment.remove( prop.getURI() );
                                alignment.put( localName, localName.toUpperCase() ); //the reference alignment
                            }
                        } else if ( activeStringOperation == 2 ) {
                            propertiesIdentifiers.put( localName , localName.toLowerCase() );
                            replacePropertyLabel( prop.getURI(), localName.toLowerCase(), activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                            if ( alignment.containsKey( localName ) ) {        // alignment.remove( prop.getURI() );
                                alignment.put( localName, localName.toLowerCase() ); //the reference alignment
                            }
                        } else {
                            propertiesIdentifiers.put( localName,  localName + "PROPERTY" );
                            replacePropertyLabel( prop.getURI(), localName + "PROPERTY", activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                            if ( alignment.containsKey( localName ) ) {        //alignment.remove( prop.getURI() );
                                alignment.put( localName, localName + "PROPERTY" );
                            }
                        }
                    }
                }
        }
        return propertiesIdentifiers;
    }

    //replaces the label of the class
    public void replaceClassLabel( String uri, String newLabel, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation ) {
        OntClass c = modifiedModel.getOntClass( uri );

        if ( c.getLabel( "en" ) != null ) {
            if ( activeTranslateString ) {
                c.setLabel( newLabel, "fr" );
            } else
                c.setLabel( newLabel, "en" );
        }
    }

    //gets the URIs of the classes and their translation
    public HashMap<String, String> getClassesIdentifiers ( float percentage, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation ) {
        HashMap<String, String> classesIdentifiers = new HashMap<String, String>(); //the HashMap of classes identifiers

        int nbClasses, toBeRenamed, renamedClasses;

        List<OntClass> notRenamedClasses = new ArrayList<OntClass>();           //the list of not renamed classes
        List<OntClass> classes = getOntologyClasses();                     //the list of ontology classes
        List<OntClass> classesTo = new ArrayList<OntClass>();                   //the list of classes to be renamed

	// alignment contains those classes which have already been renamed
        //builds the list of all unrenamed classes from the model
        for ( OntClass c : classes ) {
	    String local = getLocalName( c.getURI() );
            if ( alignment.containsKey( local ) ) {
                if ( alignment.getProperty( local ).equals( local ) )
                    notRenamedClasses.add( c ); //add the class to not renamed classes
            }
        }
        
        nbClasses = classes.size();                           
        renamedClasses = nbClasses - notRenamedClasses.size();                  //the number of renamed classes
        toBeRenamed = Math.round(percentage*nbClasses) - renamedClasses;             // -renamedClasses -> for Benchmark

	//System.err.println( "NbClasses = "+nbClasses+ " YetToRename = "+notRenamedClasses.size()+" I will rename = "+toBeRenamed );

	// toBeRenamed is negative when classes have been added to the model
	if ( toBeRenamed < 0 ) toBeRenamed = 0;
        //build the list of classes to be renamed
        int[] n = randNumbers( notRenamedClasses.size(), toBeRenamed );
        for ( int i=0; i<toBeRenamed; i++ ) {
            OntClass cls = notRenamedClasses.get(n[i]);
            classesTo.add(cls);
        }

        for ( OntClass cls : classesTo ) {
            if ( !cls.isRestriction() ) {
                if ( !cls.isAnon() ) {
                    String prefix = getNameSpace( cls.getURI() );
                    String localName = getLocalName( cls.getURI() );

                    //has the same Namespace as the Ontology Namespace
                    if ( prefix.equals( modifiedOntologyNS ) ) {
                        if ( !classesIdentifiers.containsKey( localName ) ) {
                            if ( activeTranslateString ) {			//replace the URI with the translated one
                                String translateStrg = parseString (localName, true, false);
                                classesIdentifiers.put( localName , translateStrg );
                                replaceClassLabel( cls.getURI(), translateStrg, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                                if ( alignment.containsKey( localName ) ) {     //alignment.remove( cls.getURI() );
                                    alignment.put( localName, translateStrg);	//the reference alignment
                                }
                            } else if ( activeRandomString )	{		//replace the URI with a random string
                                String newStrg = getRandomString();
                                classesIdentifiers.put( localName , newStrg );
                                replaceClassLabel( cls.getURI(), newStrg, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                                if ( alignment.containsKey( localName ) ) {     //alignment.remove( cls.getURI() );
                                    alignment.put( localName, newStrg );	//the reference alignment
                                }
                            } else if ( activeSynonym ) {                         //replace the URI with a synonym
                                String synonym = parseString (localName, false, true);
				classesIdentifiers.put( localName, synonym );
				replaceClassLabel( cls.getURI(), synonym, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
				if ( alignment.containsKey( localName ) ) {     //alignment.remove( cls.getURI() );
                                    alignment.put( localName, synonym );//the reference alignment
                                }
                            } else if ( activeStringOperation == 1 ){             //replace the URI with the UpperCase URI
                                classesIdentifiers.put( localName , localName.toUpperCase() );
                                replaceClassLabel( cls.getURI(), localName.toUpperCase(), activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                                if ( alignment.containsKey( localName ) ) {     //alignment.remove( cls.getURI() );
                                    alignment.put( localName, localName.toUpperCase() ); //the reference alignment
                                }
                            } else if ( activeStringOperation == 2 ){             //replace the URI with the LowerCase URI
                                classesIdentifiers.put( localName , localName.toLowerCase() );
                                replaceClassLabel( cls.getURI(), localName.toLowerCase(), activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                                if ( alignment.containsKey( localName ) ) {     //alignment.remove( cls.getURI() );
                                    alignment.put( localName, localName.toLowerCase() );     //the reference alignment
                                }
                            } else {
                                classesIdentifiers.put( localName, localName + "CLASS" );
                                replaceClassLabel( cls.getURI(), localName + "CLASS", activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
                                if ( alignment.containsKey( localName ) ) {     //alignment.remove( cls.getURI() );
                                    alignment.put( localName, localName + "CLASS" );
                                }
                            }
                        }
                    }
                }
            }
        }
        return classesIdentifiers;
    }

    //renames percentage properties and classes
    //activeProperties -> if true, then rename properties
    //activeClasses -> if true, then rename classes
    public OntModel renameResource( boolean activeProperties, boolean activeClasses, float percentage, boolean activeRandomString, boolean activeTranslateString, boolean activeSynonym, int activeStringOperation) {
        List<Statement> statements = null;                                      //the list of all statements
        HashMap<String, String> propertiesIdentifiers = null;                   //the HashMap of the properties identifiers
        HashMap<String, String> classesIdentifiers = null;                      //the HashMap of the classes identifiers

        OntModel newModel = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );//create new Model
        //get properties and classes identifiers
        if ( activeProperties )
            propertiesIdentifiers = getPropertiesIdentifiers( percentage, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation);
        if ( activeClasses )
            classesIdentifiers = getClassesIdentifiers( percentage, activeRandomString, activeTranslateString, activeSynonym, activeStringOperation );
      
        //iterate and modify the identifiers
        for ( Statement stm : modifiedModel.listStatements().toList() ) {

            Resource subject   = stm.getSubject();                              //the subject
            Property predicate = stm.getPredicate();                            //the predicate
            RDFNode object     = stm.getObject();                               //the object

	    boolean isPred, isSubj, isObj;
            isPred = isSubj = isObj = false;

	    String subjuri = subject.getURI();
	    String subjectLocalName = getLocalName( subjuri );
            Resource subj = null;
            //if it is the subject of the statement
            if ( subjectLocalName != null ) {
	        String subjectNameSpace = getNameSpace( subjuri );
                if ( activeProperties ) {
                    if ( propertiesIdentifiers.containsKey( subjectLocalName ) ) {
                        //if the namespace of the subject is the same as the namespace of the property identifier
                        if ( subjectNameSpace.equals( modifiedOntologyNS ) ) {//that we want to remove
                            isSubj = true;
                            subj = newModel.createResource( subjectNameSpace + propertiesIdentifiers.get( subjectLocalName ) );
                        }
                    }
                }

                if ( activeClasses ) {
                    if ( classesIdentifiers.containsKey( subjectLocalName ) ) {
                        //if the namespace of the subject is the same as the namespace of the property identifier
                       //that we want to remove
                        if ( subjectNameSpace.equals( modifiedOntologyNS ) ) {
                            isSubj = true;
                            subj = newModel.createResource( subjectNameSpace + classesIdentifiers.get( subjectLocalName ) );
                        }
                    }
                }
            }

            //if it is the predicate of the statement
	    String preduri =  predicate.getURI();
	    String predicateLocalName = getLocalName( preduri );
	    String predicateNameSpace = getNameSpace( preduri );
            Property pred = null;
            if ( activeProperties ) {
		    if ( propertiesIdentifiers.containsKey( predicateLocalName ) ) {
                    //if the namespace of the predicate is the same as the namespace of the property identifier
                    //that we want to remove
                    if ( predicateNameSpace.equals( modifiedOntologyNS ) ) {
                        isPred = true;
                        pred = newModel.createProperty(predicateNameSpace, propertiesIdentifiers.get( predicateLocalName ) );
                    }
                }
            }

            if ( activeClasses ) {
                if ( classesIdentifiers.containsKey( predicateLocalName ) ) {
                    //if the namespace of the predicate is the same as the namespace of the property identifier
                    //that we want to remove
                    if ( predicateNameSpace.equals( modifiedOntologyNS ) ) {
                        isPred = true;
                        pred = newModel.createProperty(predicateNameSpace, classesIdentifiers.get( predicateLocalName ) );
                    }
                }
            }
            
            Resource obj  = null;
            //if it is the object of the statement
            if ( object.canAs( Resource.class ) )
                if ( object.isURIResource() ) {
		    String uri =  object.asResource().getURI();
		    String objectLocalName = getLocalName( uri );
		    String objectNameSpace = getNameSpace( uri );
                    if ( activeProperties ) {
                        if ( propertiesIdentifiers.containsKey( objectLocalName ) ) {
                            //if the namespace of the object is the same as the namespace of the property identifier
                            //that we want to remove
                            if ( objectNameSpace.equals( modifiedOntologyNS ) ) {
                                isObj = true;
                                obj = newModel.createResource(objectNameSpace + propertiesIdentifiers.get( objectLocalName ) );
                            }
                        }
                    }

                    if ( activeClasses ) {
                        if ( classesIdentifiers.containsKey( objectLocalName ) ) {
                            //if the namespace of the object is the same as the namespace of the property identifier that we want to remove
                            if ( objectNameSpace.equals( modifiedOntologyNS ) ) {
                                isObj = true;
                                obj = newModel.createResource(objectNameSpace + classesIdentifiers.get( objectLocalName ) );
                            }
                        }
                    }
                }

            if ( isSubj ) {
                if ( isPred ) {
                    if ( isObj )
                        newModel.add( subj, pred, obj );
                    else
                        newModel.add( subj, pred, object );
                }
                else {
                    if ( isObj )
                        newModel.add( subj, predicate, obj );
                    else
                        newModel.add( subj, predicate, object );
                }
            } else {
                if ( isPred ) {
                    if ( isObj )
                        newModel.add( subject, pred, obj );
                    else
                        newModel.add( subject, pred, object );
                }
                else {
                    if ( isObj )
                        newModel.add( subject, predicate, obj );
                    else
                        newModel.add( subject, predicate, object );
                }
            }
        }
        if ( activeClasses ) {
            buildClassHierarchy();
            //we update the class hierarchy according to the new modifications
            classHierarchy.updateClassHierarchy( alignment );
	    //classHierarchy.printClassHierarchy();
        }
        return newModel;
    }

    // -------------------------
    // Utility (string) functions

    //removes spaces from a string
    public String removeSpaces ( String str ) {
        //return str.replaceAll("\\s+", "");
        if ( !str.contains( " " ) )
		return str;
	else {
		String aux = "", aux1="";
		int index;

                if ( str.contains( " " ) ) {
                    while ( str.indexOf( " " ) != -1 ) {
                        index = str.indexOf( " " );
                        aux += str.substring( 0, index );
                        aux1 = str.substring(index+2);
                        str = str.substring(index+1, index+2).toUpperCase().concat( aux1 );
                    }
                    aux += str;
                    return aux;
                }
        }
        return str;
    }

    //translates the string from English to French
    public String translateString( String source ) {
        String translatedText = "";
        GoogleAPI.setHttpReferrer("http://code.google.com/p/google-api-translate-java/");
        //Translate.setHttpReferrer("http://code.google.com/p/google-api-translate-java/");
        try {
            translatedText = Translate.execute(source, Language.ENGLISH, Language.FRENCH);
        } catch (Exception e) {
            System.err.println( "Exception " + e.getMessage() );
        }
        return removeSpaces ( translatedText );
    }

    public String getSynonym( String source ) {
        return source;
    }
        /*
	//synonym of the word
	public String getSynonym ( String source ) {
		String synonym = "";
		//set this variable according to your WordNet installation folder
		//see : http://lyle.smu.edu/~tspell/jaws/index.html
		System.setProperty("wordnet.database.dir", "/usr/Wordnet/WordNet-3.0/dict");
		WordNetDatabase database = WordNetDatabase.getFileInstance();
		Synset[] synsets = database.getSynsets( source );
		if (synsets.length > 0) {
			for (int i = 0; i < synsets.length; i++) {
				String[] wordForms = synsets[i].getWordForms();
				for (int j = 0; j < wordForms.length; j++) {
					if ( !wordForms[j].equals( source ) )	{
						synonym = removeSpaces ( wordForms[j] );
						return synonym;
					}
				}
			}
		}
		else
			return source;
		return source;
	}
        */

    public String parseString (String str, boolean activeTranslateString, boolean activeSynonym) {
        // if ( debug ) System.err.println ( "str = [" + str + "]" );
        char [] parsed = str.toCharArray();
        String newString = "";

        for ( int i=1; i<parsed.length; i++ ) {
            if( Character.isUpperCase( parsed[i] ) ) {
                String aux = str.substring(0, i);

                if ( activeTranslateString )
                    newString = newString.concat( translateString( str.substring(0, i) ) );
                if ( activeSynonym )
                    newString = newString.concat( getSynonym( str.substring(0, i) ) );

                str = str.substring(i);
            }
        }

        if ( activeTranslateString )
            newString = newString.concat( translateString(str.substring(0)) );
        if ( activeSynonym )
            newString = newString.concat( getSynonym(str.substring(0)) );
        return newString;
    }

}
