/**
 * $Id: BasicAlterator.java 1842 2013-03-24 17:42:41Z euzenat $
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

/* This program receives as input two ontologies (the original ontology, the ontology that must be modified),
  an alignment and a parameter with the modification that must be applied to the input ontology.
  After the modification of the initial ontology the alignment must be computed
  The file in which we store the alignment is "referenceAlignment.rdf"
*/

/* This program receives as input two ontologies (the original ontology, the ontology that must be modified),
  an alignment and a parameter with the modification that must be applied to the input ontology.
  After the modification of the initial ontology the alignment must be computed
  The file in which we store the alignment is "referenceAlignment.rdf"
*/

package fr.inrialpes.exmo.align.gen.alt;

//Java classes
import java.net.URI;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Random;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import com.hp.hpl.jena.rdf.model.RDFWriter;

//Alignment API classes
import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.impl.URIAlignment;

//JENA classes
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.util.ResourceUtils;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.ontology.AllValuesFromRestriction;
import com.hp.hpl.jena.ontology.SomeValuesFromRestriction;


//gen
import fr.inrialpes.exmo.align.gen.Alterator;
import fr.inrialpes.exmo.align.gen.ClassHierarchy;

public abstract class BasicAlterator implements Alterator {
    protected boolean debug = false;

    protected ClassHierarchy classHierarchy;    // the class hierarchy
    protected OntModel modifiedModel;		// the modified Ontology
    protected Properties alignment;		// the alignment
    protected Alignment extractedAlignment;     // the alignment corresponding to alignment

    // Policy with respect to namespacess.
    // See the doc in html... it explains well what an alterator should be...
    // All this should go in the Parameters...

    // Currently (as loaded in empty):
    // 
    // modifiedOntologyNS -> the one from the loaded ontology (e.g, 201-2)
    //   modifiedOntologyNS is to be used in the loaded ontology
    // initOntologyNS -> the one from the loaded alignment (e.g., 101) [it is 101->201-2]
    //   initOntologyNS is to be used in the alignment

    // There are two possible path to improvements which would improve legibility and speed
    // (1) do not put URIs in parameters!
    // (2) replace alignment by real Alignments

    protected String modifiedOntologyNS;			// the namespace of the loaded OntModel
    protected String initOntologyNS;                      // the namespace of the source ontology
                                                // (the OntModel may already be a modified ontology wrt the source)

    // -------------------------
    // Constructors
    // Ontology init, Ontology modified, Alignment align
    // This could be a legitimate constructor in which the
    // Alignment is transformed in properties
    // At the moment it is useless and unused
    //public BasicAlterator ( OntModel model, Alignment al ) {
    //  modifiedModel = model;
    //  // get the default namespace of the model
    //	modifiedOntologyNS = model.getNsPrefixURI("");
    //	alignment = new Properties();
    //}

    public BasicAlterator() {}

    public BasicAlterator( Alterator om ) {
	initModel( om );
    }

    protected void initModel( Alterator om ) {
	// cloning
	modifiedModel = om.getProtoOntology();
	alignment = om.getProtoAlignment();
	classHierarchy = om.getHierarchy();
	modifiedOntologyNS = om.getNamespace();
	initOntologyNS = om.getBase();
    }

    // abstract
    public abstract Alterator modify( Properties params );

    // -------------------------
    // Accessors

    public void setDebug( boolean d ) { debug = d; }

    //returns the modified ontology after changing the namespace
    public OntModel getModifiedOntology () { return modifiedModel; }

    public void setModifiedModel( OntModel model ) { modifiedModel = model; }

    public OntModel getModifiedModel() { return modifiedModel; }

    public ClassHierarchy getHierarchy() { return classHierarchy; }

    //get properties
    public Properties getProtoAlignment() { return alignment; }

    public OntModel getProtoOntology() { return modifiedModel; }

    public String getNamespace() { return modifiedOntologyNS; }

    public String getBase() { return initOntologyNS; }

    // -------------------------
    // Utility (URI) functions
    // This is a roundabout for a Jena but which is not able to 
    // correctly get the local name if it contains non alphabetical characters

    public static String getLocalName( String uri ) {
	if ( uri == null ) return null;
	int index = uri.lastIndexOf("#");
	if ( index == -1 ) {
	    index = uri.lastIndexOf("/");
	    return uri.substring( index+1 );
	} else {
	    return uri.substring( index+1 );
	}
    }

    public static String getNameSpace( String uri ) {
	int index = uri.lastIndexOf("#");
	if ( index == -1 ) {
	    index = uri.lastIndexOf("/");
		return uri.substring( 0, index+1 );
	} else {
	    return uri.substring( 0, index+1 );
	}
    }

    // -------------------------
    // Utility (string) functions

    //generates a random string with the length "length"
    // JE: the name if these strings may be unusually too long...
    // Think about some variation (based on average length and standard variation?)
    // Length and upper casing may be an argument
    public String getRandomString() {
        Random generator = new Random();
        String characters = "abcdefghijklmnopqrstuvwxyz";
	int length = characters.length();
	char[] text = new char[length];
	for (int i = 0; i < length; i++) {
	    text[i] = characters.charAt( generator.nextInt(length) );
	}
	return new String(text); // JE suppressed toUpperCase()
    }

    // -------------------------
    // Utility (randomizing) functions

    //count - the number of elements from the vector
    //the random numElems that must be selected
    // numElems <= count
    //uses the Fisher and Yates method to shuffle integers from an array
    public int [] randNumbers ( int count, int numElems ) {
        int [] vect = new int[count];
        int [] n    = new int[numElems];
        int aux, rand;
        Random generator = new Random();

        for ( int i=0; i<count; i++ ) vect[i] = i;                               //fill the array with sorted elements
        for ( int j=0; j<numElems; j++ ) {
            rand = generator.nextInt( count-j );                                //choose a random number from the interval
            n[j] = vect[rand];                                                  //build the new vector
            aux = vect[rand];                                                   //swap
            vect[rand] = vect[count-j-1];
            vect[count-j-1] = aux;
        }
        return n;
    }

    // -------------------------
    // Class hierarchy utilities

    //if we add / remove a class we need to keep track of the class hierarchy
    public void buildClassHierarchy () {
        if ( classHierarchy == null ) {
	    classHierarchy = new ClassHierarchy();
	    classHierarchy.buildClassHierarchy( modifiedModel );
	}
        //classHierarchy.printClassHierarchy();
    }

    // JE: this is usually not efficient, since these lists will likely be used for iterating

    //gets the Ontology classes
    @SuppressWarnings("unchecked")
    public List<OntClass> getOntologyClasses() {
        List<OntClass> classes = new ArrayList<OntClass>();
        for ( Iterator it = modifiedModel.listNamedClasses(); it.hasNext(); ) {
            OntClass aux = (OntClass)it.next();
	    // JE: why not startsWith ?
            if ( getNameSpace( aux.getURI() ).equals( modifiedOntologyNS ) ) {
                classes.add( aux );
            }
        }
        return classes;
    }

    //gets the Ontology properties
    @SuppressWarnings("unchecked")
    public List<OntProperty> getOntologyProperties() {
        List<OntProperty> properties = new ArrayList<OntProperty>();
        for ( Iterator it = modifiedModel.listAllOntProperties(); it.hasNext(); ) {
            OntProperty prop = (OntProperty)it.next();
	    // JE: why not startsWith ?
            if ( getNameSpace( prop.getURI() ).equals( modifiedOntologyNS ) )
                properties.add( prop );
        }
        return properties;
    }

    //adds a class with a random URI to the parent class parentURI
    public OntClass addClass ( OntClass parentClass, String name ) {
	String childURI = modifiedOntologyNS+name;
        OntClass childClass = modifiedModel.createClass( childURI );//create a new class to the model

        classHierarchy.addClass( childURI, parentClass.getURI() );//add the node in the hierarchy of classes
        parentClass.addSubClass( childClass );                                  //add the childClass as subclass of parentClass
        modifiedModel.add( childClass, RDFS.subClassOf, parentClass );     //add the class to the model
        return childClass;
    }

    //changes the unionOf, intersectionOf
    public OntModel changeDomainRange ( HashMap<String, String> uris ) {
        OntModel newModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);//create new Model
        
        //iterate and modify the identifiers
        for ( Statement stm : modifiedModel.listStatements().toList() ) {
            Resource subject   = stm.getSubject();                              //the subject
            Property predicate = stm.getPredicate();                            //the predicate
            RDFNode object     = stm.getObject();                               //the object

            //if the class appears as the subject of a proposition
            if ( ( subject.getURI() != null ) && ( uris.containsKey( subject.getURI() ) ) ) {
                subject = newModel.createResource( uris.get( subject.getURI() ) );
            }
            
            //if appears as the predicate - never
	    if ( ( predicate.getURI() != null ) && ( uris.containsKey( predicate.getURI() ) ) ) {
                predicate = newModel.createProperty( uris.get( predicate.getURI() ) );
            }
            //if appears as the object of the statement
	    if ( ( object.canAs( Resource.class ) ) && ( object.isURIResource() ) 
		 && ( object.asResource().getURI() != null ) && ( uris.containsKey( object.asResource().getURI() ) ) ) {
		object = newModel.createResource( uris.get( object.asResource().getURI() ) );
	    }
            newModel.add( subject, predicate, object );
        }       
        return newModel;
    }

    //check if the removed class appears as AllValueFrom or SomeValueFrom in a restriction
    // JE2012: This should be replaced by all parentclasses
    @SuppressWarnings("unchecked")
    public void checkClassesRestrictions ( OntClass childClass, OntClass parentClass )  {
	String uri = childClass.getURI();
        for ( Iterator it = modifiedModel.listRestrictions(); it.hasNext(); ) {
            Restriction restr = (Restriction)it.next();					//get the restriction
            /* isAllValuesFromRestriction */
            if ( restr.isAllValuesFromRestriction() )  {
                AllValuesFromRestriction av = restr.asAllValuesFromRestriction();
                if ( uri.equals( av.getAllValuesFrom().getURI() ) )                   //if points to the childClass
		    av.setAllValuesFrom( parentClass );                     //change to point to the parentClass
	    }
            /* SomeValueFromRestriction */
            if ( restr.isSomeValuesFromRestriction() ) {
                SomeValuesFromRestriction sv = restr.asSomeValuesFromRestriction();
                if ( uri.equals( sv.getSomeValuesFrom().getURI() ) )                  //if points to the childClass
		    sv.setSomeValuesFrom( parentClass );                    //change to point to the parentClass
            }
        }
    }

    //removes a class, returns the uri of his parent
    @SuppressWarnings("unchecked")
    public String removeClass( OntClass cls ) {
        ArrayList<OntClass> subClasses = new ArrayList<OntClass>();		//the list of all the subclasses of the class
        OntClass thing = modifiedModel.createClass( OWL.Thing.getURI() );	//Thing class
        buildClassHierarchy();							//build the class hierarchy if necessary
        OntClass parentClass = classHierarchy.removeClass( modifiedModel, cls );//get the parent of the class

        for (Iterator it = cls.listSubClasses(); it.hasNext(); ) {            //build the list of subclasses
	    //because we can't change the
            subClasses.add( (OntClass)it.next() );						//model while we are iterating
        }

        if ( parentClass != thing )						//now we change the superclass of classes
            for (OntClass clss : subClasses) 					//new superclass =>
                clss.setSuperClass( parentClass );				//=>the superclass of the node

	checkClassesRestrictions( cls, parentClass );
	cls.remove();							//remove the class from the Ontology
	return parentClass.getURI();
    }

    //compute the alignment after the modifications
    public Alignment getAlignment() {
	if ( extractedAlignment == null ) extractAlignment( initOntologyNS, modifiedOntologyNS );
	return extractedAlignment;
    }

    /**
     * Generates an Alignment from the property list hosting the alignment.
     * On the fly rename the matched entities from the base1 (source) and 
     * base2 (target) namespaces.
     *
     * The property list is also modified for reflecting the renaming of entities.
     * This is useful when the property list is reused when generating TestSets.
     */
    public void relocateTest( String namespaceNew ) {
        relocateTest( initOntologyNS, namespaceNew );
    }

    public void relocateTest( String base1, String base2 ) {
        extractedAlignment = extractAlignment( base1, base2 );
	modifiedModel = changeNamespace( base2 );
	modifiedOntologyNS = base2;
    }

    @SuppressWarnings("unchecked")
    public Alignment extractAlignment( String base1, String base2 ) {
        Alignment extractedAlignment  = new URIAlignment();
        
	//System.err.println( "\n-----> "+initOntologyNS );
	//System.err.println( "-----> "+base1 );
	//System.err.println( "-----> "+base2 );
        try {
            URI onto1 = new URI( getNameSpace( base1 ) );
            URI onto2 = new URI( getNameSpace( base2 ) );

            extractedAlignment.init( onto1, onto2 );
	    // JE: not likely correct
            extractedAlignment.setFile1( onto1 );
            extractedAlignment.setFile2( onto2 );
            
            for ( String key : alignment.stringPropertyNames() ) {
		if ( !key.equals("##") ) {
		    String value = alignment.getProperty(key);
		    //if ( debug ) System.err.println( "[" + source + "][" + target + "]" );
		    extractedAlignment.addAlignCell( URI.create( base1+key ), URI.create( base2+value ) );
		}
            }
	    alignment.setProperty( "##", base1 );
        } catch ( Exception ex ) {  
	    ex.printStackTrace();
        }
	return extractedAlignment;
    }

    // -------------------------
    // Namespace management and change

    /**
     * Modifies the namespace of the generated ontology
     * This is the initial implementation.
     * However... it has some problem with Jena treating fragments (containing numbers and / in them).
     */
    public OntModel changeNamespace ( String ns ) {
        OntModel newModel = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM ); //create new Model

        //iterate through all the statements and change the namespace
        for ( Statement stm : modifiedModel.listStatements().toList() ) {
            Resource subject   = stm.getSubject();                              //the subject
            Property predicate = stm.getPredicate();                            //the predicate
            RDFNode object     = stm.getObject();                               //the object

            if ( !subject.isLiteral() && ( subject.getURI() != null )
		 && ( getNameSpace( subject.getURI() ).equals( modifiedOntologyNS ) ) ) {
		subject = newModel.createResource( ns + getLocalName( subject.getURI() ) );
	    }
            if ( !object.isLiteral() && ( object.canAs( Resource.class ) ) && ( object.isURIResource() )
		 && ( getNameSpace( object.asResource().getURI() ).equals( modifiedOntologyNS ) ) ) {
		object = newModel.createResource( ns + getLocalName( object.asResource().getURI() ) );
	    }
            if ( !predicate.isLiteral() && ( getNameSpace( predicate.getURI() ).equals( modifiedOntologyNS ) ) ) {
		predicate = newModel.createProperty( ns + getLocalName( predicate.getURI() ) );
	    }
	    newModel.add( subject, predicate, object );
	}
	renameOntology( newModel, modifiedOntologyNS, ns );
        return newModel;
    }

    /**
     * JE: starting an attempt to do the relocation through String reading and writing:
     * The ideal case is that in the generated files, the namespace appears exactly twice:
     * in xmlns and in xml:base.
     * But it depends on the actual seed.
     * Two versions would be possible:
     * OntModel.write( OutputStream out ) / OntModel.write( Writer writer ) 
     * new Model().read( InputStream in, String base ) / new Model().read(Reader reader, String base)
     * So far this is less efficient (and less elegant) than the modifications above
     */
    public OntModel changeNamespace2( String base2 ) {
	OntModel model = ModelFactory.createOntologyModel( OntModelSpec.OWL_MEM );
	try {
	    //Charset defaultCharset = Charset.forName("UTF8");
	    ByteArrayOutputStream out = new ByteArrayOutputStream();
	    //StringWriter sout = new StringWriter();
	    RDFWriter writer = modifiedModel.getWriter("RDF/XML-ABBREV");
	    writer.setProperty( "showXmlDeclaration","true" );
	    writer.setProperty( "relativeURIs", "" );//faster 26.6 -> 26.03 (?)
	    // -------- critical section
	    // I am curious: isn't it possible to set new ns here? Apparently NO
	    writer.setProperty( "xmlbase", modifiedOntologyNS );
	    modifiedModel.setNsPrefix( "", modifiedOntologyNS );
	    renameOntology( modifiedModel, modifiedOntologyNS, modifiedOntologyNS );
	    // --------
	    writer.write( modifiedModel.getBaseModel(), out, "");
	    //writer.write( modifiedModel.getBaseModel(), sout, "");
	    String serial = out.toString( "UTF8" );
	    //StringBuffer serial = sout.getBuffer();
	    // Can be printed right away
	    String sout = serial.replace( modifiedOntologyNS, base2 ); // Does not work well with StringBuffer
	    // JE: This part below is unnecessary if we just want to print the result and close the story
	    // But suppressing it makes problems even in random...
	    // Using the read allocate as much as before
	    //InputStream in = null; from the string
	    ByteArrayInputStream in = new ByteArrayInputStream( sout.getBytes("UTF8") );
	    model.read( in, null );
	} catch ( Exception ex ) { //UnsupportedEncodingException;
	    ex.printStackTrace(); 
	}
	return model;
    }

    /**
     * Rename the ontology, just in case it does not have the same URI as its content...
     * This happens
     * oldURI should be useless
     */
    public void renameOntology( OntModel model, String oldURI, String newURI ) {
	Ontology onto = model.getOntology( oldURI );
	if ( onto == null ) { // Ugly but efficient
	    for ( Ontology o : model.listOntologies().toList() ) {
		onto = o; break;
	    }
	}
	if ( onto == null ) {
	    model.createOntology( newURI );
	} else {
	    ResourceUtils.renameResource( onto, newURI );
	}
    }

}
