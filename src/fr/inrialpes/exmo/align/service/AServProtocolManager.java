/*
 * $Id: AServProtocolManager.java 1841 2013-03-24 17:28:33Z euzenat $
 *
 * Copyright (C) INRIA, 2006-2013
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 */

package fr.inrialpes.exmo.align.service;

import fr.inrialpes.exmo.align.parser.AlignmentParser;
import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.ObjectAlignment;
import fr.inrialpes.exmo.align.impl.eval.DiffEvaluator;
import fr.inrialpes.exmo.align.impl.rel.EquivRelation;

import fr.inrialpes.exmo.align.service.msg.Message;
import fr.inrialpes.exmo.align.service.msg.AlignmentId;
import fr.inrialpes.exmo.align.service.msg.AlignmentIds;
import fr.inrialpes.exmo.align.service.msg.AlignmentMetadata;
import fr.inrialpes.exmo.align.service.msg.EntityList;
import fr.inrialpes.exmo.align.service.msg.EvalResult;
import fr.inrialpes.exmo.align.service.msg.OntologyURI;
import fr.inrialpes.exmo.align.service.msg.RenderedAlignment;
import fr.inrialpes.exmo.align.service.msg.TranslatedMessage;
import fr.inrialpes.exmo.align.service.msg.ErrorMsg;
import fr.inrialpes.exmo.align.service.msg.NonConformParameters;
import fr.inrialpes.exmo.align.service.msg.RunTimeError;
import fr.inrialpes.exmo.align.service.msg.UnknownAlignment;
import fr.inrialpes.exmo.align.service.msg.UnknownMethod;
import fr.inrialpes.exmo.align.service.msg.UnreachableAlignment;
import fr.inrialpes.exmo.align.service.msg.UnreachableOntology;
import fr.inrialpes.exmo.align.service.msg.CannotRenderAlignment;

import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.Ontology;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;
import org.semanticweb.owl.align.AlignmentProcess;
import org.semanticweb.owl.align.AlignmentVisitor;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Evaluator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.SQLException;

import java.lang.ClassNotFoundException;
import java.lang.InstantiationException;
import java.lang.NoSuchMethodException;
import java.lang.IllegalAccessException;
import java.lang.NullPointerException;
import java.lang.UnsatisfiedLinkError;
import java.lang.ExceptionInInitializerError;
import java.lang.reflect.InvocationTargetException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.BufferedWriter;
import java.io.OutputStreamWriter;
import java.io.IOException;
import java.io.File;
import java.net.URI;
import java.net.URL;
import java.net.JarURLConnection;
import java.net.URISyntaxException;
import java.util.Hashtable;
import java.util.Set;
import java.util.HashSet;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.jar.Attributes.Name;
import java.util.jar.Attributes;
import java.util.jar.Manifest;
import java.util.jar.JarFile;
import java.util.jar.JarEntry;
import java.util.zip.ZipEntry;

/**
 * This is the main class that control the behaviour of the Alignment Server
 * It is as independent from the OWL API as possible.
 * However, it is still necessary to test for the reachability of an ontology and moreover to resolve its URI for that of its source.
 * For these reasons we still need a parser of OWL files here.
 */

public class AServProtocolManager {
    final static Logger logger = LoggerFactory.getLogger( AServProtocolManager.class );

    CacheImpl alignmentCache = null;
    Properties commandLineParams = null;
    Set<String> renderers = null;
    Set<String> methods = null;
    Set<String> services = null;
    Set<String> evaluators = null;

    Hashtable<String,Directory> directories = null;

    // This should be stored somewhere
    int localId = 0; // surrogate of emitted messages
    String serverId = null; // id of this alignment server

    /*********************************************************************
     * Initialization and constructor
     *********************************************************************/

    public AServProtocolManager ( Hashtable<String,Directory> dir ) {
	directories = dir;
    }

    public void init( DBService connection, Properties prop ) throws SQLException, AlignmentException {
	commandLineParams = prop;
	serverId = prop.getProperty("prefix");
	if ( serverId == null || serverId.equals("") )
	    serverId = "http://"+prop.getProperty("host")+":"+prop.getProperty("http");
	alignmentCache = new CacheImpl( connection );
	alignmentCache.init( prop, serverId );
	renderers = implementations( "org.semanticweb.owl.align.AlignmentVisitor" );
	methods = implementations( "org.semanticweb.owl.align.AlignmentProcess" );
	methods.remove("fr.inrialpes.exmo.align.impl.DistanceAlignment"); // this one is generic, but not abstract
	services = implementations( "fr.inrialpes.exmo.align.service.AlignmentServiceProfile" );
	evaluators = implementations( "org.semanticweb.owl.align.Evaluator" );
    }

    public void close() {
	try { alignmentCache.close(); }
	catch (SQLException sqle) { 
	    logger.trace( "IGNORED SQL Exception", sqle );
	}
    }

    public void reset() {
	try {
	    alignmentCache.reset();
	} catch (SQLException sqle) {
	    logger.trace( "IGNORED SQL Exception", sqle );
	}
    }

    public void flush() {
	alignmentCache.flushCache();
    }

    public void shutdown() {
	try { 
	    alignmentCache.close();
	    System.exit(0);
	} catch (SQLException sqle) {
	    logger.trace( "IGNORED SQL Exception", sqle );
	}
    }

    private int newId() { return localId++; }

    /*********************************************************************
     * Extra administration primitives
     *********************************************************************/

    public Set<String> listmethods (){
	return methods;
    }

    public Set<String> listrenderers(){
	return renderers;
    }

    public Set<String> listservices(){
	return services;
    }

    public Set<String> listevaluators(){
	return evaluators;
    }

    /*
    public Enumeration alignments(){
	return alignmentCache.listAlignments();
    }
    */
    public Collection<Alignment> alignments() {
	return alignmentCache.alignments();
    }

    public Collection<URI> ontologies() {
	return alignmentCache.ontologies();
    }

    public Collection<Alignment> alignments( URI uri1, URI uri2 ) {
	return alignmentCache.alignments( uri1, uri2 );
    }

    public String query( String query ){
	//return alignmentCache.query( query );
	return "Not available yet";
    }

    public String serverURL(){
	return serverId;
    }

    public String argline(){
	return commandLineParams.getProperty( "argline" );
    }

   /*********************************************************************
     * Basic protocol primitives
     *********************************************************************/

    // DONE
    // Implements: store (different from store below)
    public Message load( Message mess ) {
	boolean todiscard = false;
	Properties params = mess.getParameters();
	// load the alignment
	String name = params.getProperty("url");
	String file = null;
	if ( name == null || name.equals("") ){
	    file  = params.getProperty("filename");
	    if ( file != null && !file.equals("") ) name = "file://"+file;
	}
	//logger.trace("Preparing for loading {}", name);
	Alignment al = null;
	try {
	    //logger.trace(" Parsing alignment");
	    AlignmentParser aparser = new AlignmentParser(0);
	    al = aparser.parse( name );
	    //logger.trace(" Alignment parsed");
	} catch (Exception e) {
	    return new UnreachableAlignment(newId(),mess,serverId,mess.getSender(),name,(Properties)null);
	}
	// We preserve the pretty tag within the loaded ontology
	String pretty = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
	if ( pretty == null ) pretty = params.getProperty("pretty");
	if ( pretty != null && !pretty.equals("") ) {
	    al.setExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY, pretty );
	}
	// register it
	String id = alignmentCache.recordNewAlignment( al, true );
	// if the file has been uploaded: discard it
	if ( al != null && al != null ) {
	    // try unlink
	}
	return new AlignmentId(newId(),mess,serverId,mess.getSender(),id,(Properties)null,pretty);
    }

    // Implements: align
    @SuppressWarnings( "unchecked" )
    public Message align( Message mess ){
	Message result = null;
	Properties p = mess.getParameters();
	// These are added to the parameters wich are in the message
	//for ( String key : commandLineParams ) {
	// Unfortunately non iterable
	for ( Enumeration<String> e = (Enumeration<String>)commandLineParams.propertyNames(); e.hasMoreElements();) { //[W:unchecked]
	    String key = e.nextElement();
	    if ( p.getProperty( key ) == null ){
		p.setProperty( key , commandLineParams.getProperty( key ) );
	    }
	}
	// Do the fast part (retrieve)
	result = retrieveAlignment( mess );
	if ( result != null ) return result;
	// [JE2013:ID]
	String uri = alignmentCache.generateAlignmentUri();

	// [JE2013:ID]
	Aligner althread = new Aligner( mess, uri );
	Thread th = new Thread(althread);
	// Do the slow part (align)
	if ( mess.getParameters().getProperty("async") != null ) {
	    th.start();
	    // Parameters are used
	    // [JE2013:ID]
	    return new AlignmentId(newId(),mess,serverId,mess.getSender(),uri,mess.getParameters());
	} else {
	    th.start();
	    try{ th.join(); }
	    catch ( InterruptedException is ) {
		return new ErrorMsg(newId(),mess,serverId,mess.getSender(),"Interrupted exception",(Properties)null);
	    };
	    return althread.getResult();
	}
    }

    /**
     * returns null if alignment not retrieved
     * Otherwise returns AlignmentId or an ErrorMsg
     */
    private Message retrieveAlignment( Message mess ){
	Properties params = mess.getParameters();
	String method = params.getProperty("method");
	// find and access o, o'
	URI uri1 = null;
	URI uri2 = null;
	try {
	    uri1 = new URI(params.getProperty("onto1"));
	    uri2 = new URI(params.getProperty("onto2"));
	} catch (Exception e) {
	    return new NonConformParameters(newId(),mess,serverId,mess.getSender(),"nonconform/params/onto",(Properties)null);
	};
	Set<Alignment> alignments = alignmentCache.getAlignments( uri1, uri2 );
	if ( alignments != null && params.getProperty("force") == null ) {
	    for ( Alignment al: alignments ){
		String meth2 = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.METHOD );
		if ( meth2 != null && meth2.equals(method) ) {
		    return new AlignmentId(newId(),mess,serverId,mess.getSender(),
					   al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID ),(Properties)null,
					   al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ) );
		}
	    }
	}
	return (Message)null;
    }

    // DONE
    // Implements: query-aligned
    public Message existingAlignments( Message mess ){
	Properties params = mess.getParameters();
	// find and access o, o'
	String onto1 = params.getProperty("onto1");
	String onto2 = params.getProperty("onto2");
	URI uri1 = null;
	URI uri2 = null;
	Set<Alignment> alignments = new HashSet<Alignment>();
	try {
	    if( onto1 != null && !onto1.equals("") ) {
		uri1 = new URI( onto1 );
	    }
	    if ( onto2 != null && !onto2.equals("") ) {
		uri2 = new URI( onto2 );
	    }
	    alignments = alignmentCache.getAlignments( uri1, uri2 );
	} catch (Exception e) {
	    return new ErrorMsg(newId(),mess,serverId,mess.getSender(),"MalformedURI problem",(Properties)null);
	}; //done below
	String msg = "";
	String prettys = "";
	for ( Alignment al : alignments ) {
	    msg += al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID )+" ";
	    prettys += al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY )+ ":";
	}
	return new AlignmentIds(newId(),mess,serverId,mess.getSender(),msg,(Properties)null,prettys);
    }

    public Message findCorrespondences( Message mess ) {
	Properties params = mess.getParameters();
	// Retrieve the alignment
	Alignment al = null;
	String id = params.getProperty("id");
	try {
	    al = alignmentCache.getAlignment( id );
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,serverId,mess.getSender(),id,(Properties)null);
	}
	// Find matched
	URI uri = null;
	try {
	    uri = new URI( params.getProperty("entity") );
	} catch (Exception e) {
	    return new ErrorMsg(newId(),mess,serverId,mess.getSender(),"MalformedURI problem",(Properties)null);
	};
	// Retrieve correspondences
	String msg = params.getProperty("strict");
	boolean strict = ( msg != null && !msg.equals("0") && !msg.equals("false") && !msg.equals("no") );
	msg = "";
	try {
	    Set<Cell> cells = al.getAlignCells1( uri );
	    if ( cells != null ) {
		for ( Cell c : cells ) {
		    if ( !strict || c.getRelation() instanceof EquivRelation ) {
			msg += c.getObject2AsURI( al )+" ";
		    }
		}
	    }
	} catch ( AlignmentException alex ) { // should never happen
	    return new ErrorMsg(newId(),mess,serverId,mess.getSender(),"Unexpected Alignment API Error",(Properties)null);
	}
	return new EntityList( newId(), mess, serverId, mess.getSender(), msg, (Properties)null );
    }

    // ABSOLUTELY NOT IMPLEMENTED
    // But look at existingAlignments
    // Implements: find
    // This may be useful when calling WATSON
    public Message find(Message mess){
    //\prul{search-success}{a --request ( find (O, T) )--> S}{O' <= Match(O,T); S --inform (O')--> a}{reachable(O) & Match(O,T)!=null}
    //\prul{search-void}{a - request ( find (O, T) ) \rightarrow S}{S - failure (nomatch) \rightarrow a}{reachable(O)\wedge Match(O,T)=\emptyset}
    //\prul{search-unreachable}{a - request ( find (O, T) ) \rightarrow S}{S - failure ( unreachable (O) ) \rightarrow a}{\neg reachable(O)}
	return new OntologyURI(newId(),mess,serverId,mess.getSender(),"Find not implemented",(Properties)null);
    }

    // Implements: translate
    // This should be applied to many more kind of messages with different kind of translation
    public Message translate(Message mess){
	Properties params = mess.getParameters();
	// Retrieve the alignment
	String id = params.getProperty("id");
	Alignment al = null;
	try {
	    al = alignmentCache.getAlignment( id );
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,serverId,mess.getSender(),id,(Properties)null);
	}
	// Translate the query
	try {
	    String translation = QueryMediator.rewriteSPARQLQuery( params.getProperty("query"), al );
	    return new TranslatedMessage(newId(),mess,serverId,mess.getSender(),translation,(Properties)null);
	} catch (AlignmentException e) {
	    return new ErrorMsg(newId(),mess,serverId,mess.getSender(),e.toString(),(Properties)null);
	}
    }

    // DONE
    // Implements: render
    public Message render( Message mess ){
	Properties params = mess.getParameters();
	// Retrieve the alignment
	String id = params.getProperty( "id" );
	Alignment al = null;
	try {
	    logger.trace("Alignment sought for {}", id);
	    al = alignmentCache.getAlignment( id );
	    logger.trace("Alignment found");
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,serverId,mess.getSender(),id,(Properties)null);
	}
	// Render it
	String method = params.getProperty("method");
	PrintWriter writer = null;
	// Redirect the output in a String
	ByteArrayOutputStream result = new ByteArrayOutputStream(); 
	try { 
	    writer = new PrintWriter (
			  new BufferedWriter(
			       new OutputStreamWriter( result, "UTF-8" )), true);
	    AlignmentVisitor renderer = null;
	    try {
		Object[] mparams = {(Object) writer };
		java.lang.reflect.Constructor[] rendererConstructors =
		    Class.forName(method).getConstructors();
		renderer =
		    (AlignmentVisitor) rendererConstructors[0].newInstance(mparams);
	    } catch ( ClassNotFoundException cnfex ) {
		// should return the message
		logger.error( "Unknown method", cnfex );
		return new UnknownMethod(newId(),mess,serverId,mess.getSender(),method,(Properties)null);
	    }
	    renderer.init( params );
	    al.render( renderer );
	} catch ( AlignmentException e ) {
	    return new CannotRenderAlignment(newId(),mess,serverId,mess.getSender(),id,(Properties)null);
	} catch ( Exception e ) { // These are exceptions related to I/O
	    writer.flush();
	    //logger.trace( "Resulting rendering : {}", result.toString() );
	    logger.error( "Cannot render alignment", e );
	    return new Message(newId(),mess,serverId,mess.getSender(),"Failed to render alignment",(Properties)null);

	} finally {
	    writer.flush();
	    writer.close();
	}

	return new RenderedAlignment(newId(),mess,serverId,mess.getSender(),result.toString(),(Properties)null);
    }


    /*********************************************************************
     * Extended protocol primitives
     *********************************************************************/

    // Implementation specific
    public Message store( Message mess ) {
	String id = mess.getContent();
	Alignment al = null;
	 
	try {
	    try {
	    	al = alignmentCache.getAlignment( id );
	    } catch(Exception ex) {
	    	logger.warn( "Unknown Id {} in Store", id );
	    }
	    // Be sure it is not already stored
	    if ( !alignmentCache.isAlignmentStored( al ) ) {

		alignmentCache.storeAlignment( id );
		 
		// Retrieve the alignment again
		al = alignmentCache.getAlignment( id );
		// for all directories...
		for ( Directory d : directories.values() ){
		    // Declare the alignment in the directory
		    try { d.register( al ); }
		    catch ( AServException e ) {
			logger.debug( "IGNORED Exception in alignment registering", e );
		    }
		}
	    }
	    // register by them
	    // Could also be an AlreadyStoredAlignment error
	    return new AlignmentId(newId(),mess,serverId,mess.getSender(),id,(Properties)null,
				   al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ));
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,serverId,mess.getSender(),id,(Properties)null);
	}
    }

    // Implementation specific
    public Message erase( Message mess ) {
	String id = mess.getContent();
	Alignment al = null;
	try {
	    al = alignmentCache.getAlignment( id );
	    // Erase it from directories
	    for ( Directory d : directories.values() ){
		try { d.register( al ); }
		catch ( AServException e ) { 
		    logger.debug( "IGNORED Cannot register alignment", e );
		}
	    }
	    // Erase it from storage
	    try {
		alignmentCache.eraseAlignment( id, true );
	    } catch ( Exception ex ) {
		logger.debug( "IGNORED Cannot erase alignment", ex );
	    }
	    // Should be a SuppressedAlignment
	    return new AlignmentId(newId(),mess,serverId,mess.getSender(),id,(Properties)null,
				   al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ));
	} catch ( Exception ex ) {
	    return new UnknownAlignment(newId(),mess,serverId,mess.getSender(),id,(Properties)null);
	}
    }

    /*
     * Returns only the metadata of an alignment and returns it in 
     * parameters
     */
    public Message metadata( Message mess ){
	// Retrieve the alignment
	String id = mess.getParameters().getProperty("id");
	Alignment al = null;
	try {
	    al = alignmentCache.getMetadata( id );
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,serverId,mess.getSender(),id,(Properties)null);
	}
	// JE: Other possibility is to render the metadata through XMLMetadataRendererVisitor into content...
	// Put all the local metadata in parameters
	Properties params = new Properties();
	params.setProperty( "file1", al.getFile1().toString() );
	params.setProperty( "file2", al.getFile2().toString() );
	params.setProperty( Namespace.ALIGNMENT.uri+"#level", al.getLevel() );
	params.setProperty( Namespace.ALIGNMENT.uri+"#type", al.getType() );
	for ( String[] ext : al.getExtensions() ){
	    params.setProperty( ext[0]+ext[1], ext[2] );
	}
	return new AlignmentMetadata(newId(),mess,serverId,mess.getSender(),id,params);
    }

    /*********************************************************************
     * Extra alignment primitives
     *
     * All these primitives must create a new alignment and return its Id
     * There is no way an alignment server could modify an alignment
     *********************************************************************/

    public Message trim( Message mess ) {
	// Retrieve the alignment
	String id = mess.getParameters().getProperty("id");
	Alignment al = null;
	try {
	    al = alignmentCache.getAlignment( id );
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,serverId,mess.getSender(),id,(Properties)null);
	}
	// get the trim parameters
	String type = mess.getParameters().getProperty("type");
	if ( type == null ) type = "hard";
	double threshold = Double.parseDouble(mess.getParameters().getProperty("threshold"));
	al = (BasicAlignment)((BasicAlignment)al).clone();
	try { al.cut( type, threshold );}
	catch (AlignmentException e) {
	    return new ErrorMsg(newId(),mess,serverId,mess.getSender(),e.toString(),(Properties)null);
	}
	String pretty = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
	if ( pretty != null ){
	    al.setExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY, pretty+"/trimmed "+threshold );
	};
	String newId = alignmentCache.recordNewAlignment( al, true );
	return new AlignmentId(newId(),mess,serverId,mess.getSender(),newId,(Properties)null,
			       al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ));
    }

    public Message harden( Message mess ){
	return new AlignmentId(newId(),mess,serverId,mess.getSender(),"Harden not implemented",(Properties)null);
    }

    public Message inverse( Message mess ){
	Properties params = mess.getParameters();
	// Retrieve the alignment
	String id = params.getProperty("id");
	Alignment al = null;
	try {
	    al = alignmentCache.getAlignment( id );
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,serverId,mess.getSender(),"unknown/Alignment/"+id,(Properties)null);
	}

	// Invert it
	try { al = al.inverse(); }
	catch (AlignmentException e) {
	    return new ErrorMsg(newId(),mess,serverId,mess.getSender(),e.toString(),(Properties)null);
	}
	String pretty = al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY );
	if ( pretty != null ){
	    al.setExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY, pretty+"/inverted" );
	};
	String newId = alignmentCache.recordNewAlignment( al, true );
	return new AlignmentId(newId(),mess,serverId,mess.getSender(),newId,(Properties)null,
			       al.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ));
    }

    public Message meet( Message mess ){
	// Retrieve alignments
	return new AlignmentId(newId(),mess,serverId,mess.getSender(),"Meet not available",(Properties)null);
    }

    public Message join( Message mess ){
	// Retrieve alignments
	return new AlignmentId(newId(),mess,serverId,mess.getSender(),"Join not available",(Properties)null);
    }

    public Message compose( Message mess ){
	// Retrieve alignments
	return new AlignmentId(newId(),mess,serverId,mess.getSender(),"Compose not available",(Properties)null);
    }

    public Message eval( Message mess ){
	Properties params = mess.getParameters();
	// Retrieve the alignment
	String id = params.getProperty("id");
	Alignment al = null;
	try {
	    al = alignmentCache.getAlignment( id );
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,serverId,mess.getSender(),"unknown/Alignment/"+id,(Properties)null);
	}
	// Retrieve the reference alignment
	String rid = params.getProperty("ref");
	Alignment ref = null;
	try {
	    ref = alignmentCache.getAlignment( rid );
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,serverId,mess.getSender(),"unknown/Alignment/"+rid,(Properties)null);
	}
	// Set the comparison method
	String classname = params.getProperty("method");
	if ( classname == null ) classname = "fr.inrialpes.exmo.align.impl.eval.PRecEvaluator";
	Evaluator eval = null;
	try {
	    Object [] mparams = {(Object)ref, (Object)al};
	    Class<?> oClass = Class.forName("org.semanticweb.owl.align.Alignment");
	    Class[] cparams = { oClass, oClass };
	    Class<?> evaluatorClass = Class.forName(classname);
	    java.lang.reflect.Constructor evaluatorConstructor = evaluatorClass.getConstructor(cparams);
	    eval = (Evaluator)evaluatorConstructor.newInstance(mparams);
	} catch ( ClassNotFoundException cnfex ) {
	    logger.error( "Unknown method", cnfex );
	    return new UnknownMethod(newId(),mess,serverId,mess.getSender(),classname,(Properties)null);
	} catch ( InvocationTargetException itex ) {
	    String msg = itex.toString();
	    if ( itex.getCause() != null ) msg = itex.getCause().toString();
	    return new ErrorMsg(newId(),mess,serverId,mess.getSender(),msg,(Properties)null);
	} catch ( Exception ex ) {
	    return new ErrorMsg(newId(),mess,serverId,mess.getSender(),ex.toString(),(Properties)null);
	}
	// Compare it
	try { eval.eval(params); }
	catch ( AlignmentException e ) {
	    return new ErrorMsg(newId(),mess,serverId,mess.getSender(),e.toString(),(Properties)null);
	}
	// Could also be EvaluationId if we develop a more elaborate evaluation description
	return new EvalResult(newId(),mess,serverId,mess.getSender(),classname,eval.getResults());
    }

    public Message diff( Message mess ){
	Properties params = mess.getParameters();
	// Retrieve the alignment
	String id1 = params.getProperty("id1");
	Alignment al1 = null;
	try {
	    al1 = alignmentCache.getAlignment( id1 );
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,serverId,mess.getSender(),"unknown/Alignment/"+id1,(Properties)null);
	}
	// Retrieve the reference alignment
	String id2 = params.getProperty("id2");
	Alignment al2 = null;
	try {
	    al2 = alignmentCache.getAlignment( id2 );
	} catch (Exception e) {
	    return new UnknownAlignment(newId(),mess,serverId,mess.getSender(),"unknown/Alignment/"+id2,(Properties)null);
	}
	try { 
	    DiffEvaluator diff = new DiffEvaluator( al1, al2 );
	    diff.eval( params ); 
	    // This will only work with HTML
	    return new EvalResult(newId(),mess,serverId,mess.getSender(),diff.HTMLString(),(Properties)null);
	} catch (AlignmentException e) {
	    return new ErrorMsg(newId(),mess,serverId,mess.getSender(),e.toString(),(Properties)null);
	}
    }

    /**
     * Store evaluation result from its URI
     */
    public Message storeEval( Message mess ){
	return new ErrorMsg(newId(),mess,serverId,mess.getSender(),"Not yet implemented",(Properties)null);
    }

    /**
     * Evaluate a track: a set of results
     */
    // It is also possible to try a groupeval ~> with a zipfile containing results
    //            ~~> But it is more difficult to know where is the reference (non public)
    // There should also be options for selecting the result display
    //            ~~> PRGraph (but this may be a Evaluator)
    //            ~~> Triangle
    //            ~~> Cross
    public Message groupEval( Message mess ){
	return new ErrorMsg(newId(),mess,serverId,mess.getSender(),"Not yet implemented",(Properties)null);
    }

    /**
     * Store the result
     */
    public Message storeGroupEval( Message mess ){
	return new ErrorMsg(newId(),mess,serverId,mess.getSender(),"Not yet implemented",(Properties)null);
    }

    /**
     * Retrieve the results (all registered result) of a particular test
     */
    public Message getResults( Message mess ){
	return new ErrorMsg(newId(),mess,serverId,mess.getSender(),"Not yet implemented",(Properties)null);
    }

    public boolean storedAlignment( Message mess ) {
	// Retrieve the alignment
	String id = mess.getParameters().getProperty("id");
	Alignment al = null;
	try {
	    al = alignmentCache.getAlignment( id );
	} catch (Exception e) {
	    return false;
	}
	return alignmentCache.isAlignmentStored( al );
    }

    /*********************************************************************
     * Network of alignment server implementation
     *********************************************************************/

    /**
     * Ideal network implementation protocol:
     *
     * - publication (to some directory)
     * registerID
     * publishServices
     * unregisterID
     * (publishRenderer)
     * (publishMethods) : can be retrieved through the classical interface.
     *  requires a direcory
     *
     * - subscribe style
     * subscribe() : ask to receive new metadata
     * notify( metadata ) : send new metadata to subscriber
     * unsubscribe() :
     * update( metadata ) : update some modification
     *   requires to store the subscribers
     *
     * - query style: this is the classical protocol that can be done through WSDL
     * getMetadata()
     * getAlignment()
     *   requires to store the node that can be 
     */

    // Implements: reply-with
    public Message replywith(Message mess){

    //\prul{redirect}{a - request ( q(x)~reply-with:~i) \rightarrow S}{
    //Q \Leftarrow Q\cup\{\langle a, i, !i', q(x), S'\rangle\}\		\
    //S - request( q( R(x) )~reply-with:~i')\rightarrow S'}{S'\in C(q)}
	return new Message(newId(),mess,serverId,mess.getSender(),"dummy//",(Properties)null);
    }

    // Implements: reply-to
    public Message replyto(Message mess){

    //\prul{handle-return}{S' - inform ( y~reply-to:~i') \rightarrow S}{
    //Q \Leftarrow Q-\{\langle a, i, i', _, S'\rangle\}\		\
    //S - inform( R^{-1}(y)~reply-to:~i)\rightarrow a}{\langle a, i, i', _, S'\rangle \in Q, \neg surr(y)}

    //\prul{handle-return}{S' - inform ( y~reply-to:~i') \rightarrow S}{
    //Q \Leftarrow Q-\{\langle a, i, i', _, S'\rangle\}\	\
    //R \Leftarrow R\cup\{\langle a, !y', y, S'\rangle\}\		\
    //S - inform( R^{-1}(y)~reply-to:~i)\rightarrow a}{\langle a, i, i', _, S'\rangle \in Q, surr(y)}
	return new Message(newId(),mess,serverId,mess.getSender(),"dummy//",(Properties)null);
    }

    // Implements: failure
    public Message failure(Message mess){

    //\prul{failure-return}{S' - failure ( y~reply-to:~i') \rightarrow S}{
    //Q \Leftarrow Q-\{\langle a, i, i', _, S'\rangle\}\		\
    //S - failure( R^{-1}(y)~reply-to:~i)\rightarrow a}{\langle a, i, i', _, S'\rangle \in Q}
	return new Message(newId(),mess,serverId,mess.getSender(),"dummy//",(Properties)null);
    }

    /*********************************************************************
     * Utilities: reaching and loading ontologies
     *********************************************************************/

    public LoadedOntology reachable( URI uri ){
	try { 
	    OntologyFactory factory = OntologyFactory.getFactory();
	    return factory.loadOntology( uri );
	} catch (Exception e) { return null; }
    }

    /*********************************************************************
     * Utilities: Finding the implementation of an interface
     *********************************************************************/

    public static void implementations( Class tosubclass, Set<String> list ){
	Set<String> visited = new HashSet<String>();
	String classPath = System.getProperty("java.class.path",".");
	// Hack: this is not necessary
	//classPath = classPath.substring(0,classPath.lastIndexOf(File.pathSeparatorChar));
	//logger.trace( "CLASSPATH = {}", classPath );
	StringTokenizer tk = new StringTokenizer(classPath,File.pathSeparator);
	classPath = "";
	while ( tk != null && tk.hasMoreTokens() ){
	    StringTokenizer tk2 = tk;
	    tk = null;
	    // Iterate on Classpath
	    while ( tk2.hasMoreTokens() ) {
		try {
		    File file = new File( tk2.nextToken() );
		    if ( file.isDirectory() ) {
			//logger.trace("DIR {}", file);
			String subs[] = file.list();
			for( int index = 0 ; index < subs.length ; index ++ ){
			    //logger.trace("    {}", subs[index]);
			    // IF class
			    if ( subs[index].endsWith(".class") ) {
				String classname = subs[index].substring(0,subs[index].length()-6);
				if (classname.startsWith(File.separator)) 
				    classname = classname.substring(1);
				classname = classname.replace(File.separatorChar,'.');
				if ( implementsInterface( classname, tosubclass ) ) {
				    list.add( classname );
				}
			    }
			}
		    } else if ( file.toString().endsWith(".jar") &&
				!visited.contains( file.toString() ) &&
				file.exists() ) {
			//logger.trace("JAR {}", file);
			visited.add( file.toString() );
			JarFile jar = null;
			try {
			    jar = new JarFile( file );
			    exploreJar( list, visited, tosubclass, jar );
			    // Iterate on needed Jarfiles
			    // JE(caveat): this deals naively with Jar files,
			    // in particular it does not deal with section'ed MANISFESTs
			    Attributes mainAttributes = jar.getManifest().getMainAttributes();
			    String path = mainAttributes.getValue( Name.CLASS_PATH );
			    //logger.trace("  >CP> {}", path);
			    if ( path != null && !path.equals("") ) {
				// JE: Not sure where to find the other Jars:
				// in the path or at the local place?
				//classPath += File.pathSeparator+file.getParent()+File.separator + path.replaceAll("[ \t]+",File.pathSeparator+file.getParent()+File.separator);
				// This replaces the replaceAll which is not tolerant on Windows in having "\" as a separator
				// Is there a way to make it iterable???
				for( StringTokenizer token = new StringTokenizer(path," \t"); token.hasMoreTokens(); )
				    classPath += File.pathSeparator+file.getParent()+File.separator+token.nextToken();
			    }
			} catch (NullPointerException nullexp) { //Raised by JarFile
			    //logger.trace( "JarFile, file {} unavailable", file );
			}
		    }
		} catch( IOException e ) {
		    continue;
		}
	    }
	    if ( !classPath.equals("") ) {
		tk =  new StringTokenizer(classPath,File.pathSeparator);
		classPath = "";
	    }
	}
    }
    
    public static void exploreJar( Set<String> list, Set<String> visited, Class tosubclass, JarFile jar ) {
	Enumeration enumeration = jar.entries();
	while( enumeration != null && enumeration.hasMoreElements() ){
	    JarEntry entry = (JarEntry)enumeration.nextElement();
	    String entryName = entry.toString();
	    //logger.trace("    {}", entryName);
	    int len = entryName.length()-6;
	    if( len > 0 && entryName.substring(len).compareTo(".class") == 0) {
		entryName = entryName.substring(0,len);
		// Beware, in a Jarfile the separator is always "/"
		// and it would not be dependent on the current system anyway.
		//entryName = entryName.replaceAll(File.separator,".");
		entryName = entryName.replaceAll("/",".");
		if ( implementsInterface( entryName, tosubclass ) ) {
			    list.add( entryName );
		}
	    } else if( entryName.endsWith(".jar") &&
		       !visited.contains( entryName ) ) { // a jar in a jar
		//logger.trace("JAR {}", entryName);
		visited.add( entryName );
		//logger.trace(  "jarEntry is a jarfile={}", je.getName() );
		InputStream jarSt = null;
		OutputStream out = null;
		File f = null;
		try {
		    jarSt = jar.getInputStream( (ZipEntry)entry );
		    f = File.createTempFile( "aservTmpFile"+visited.size(), "jar" );
		    out = new FileOutputStream( f );
		    byte buf[]=new byte[1024];
		    int len1 ;
		    while( (len1 = jarSt.read(buf))>0 )
			out.write(buf,0,len1);
		    JarFile inJar = new JarFile( f );
		    exploreJar( list, visited, tosubclass, inJar );
		} catch (IOException ioex) {
		    logger.warn( "IGNORED Cannot read embedded jar", ioex );
		} finally {
		    try {
			jarSt.close();
			out.close();
			f.delete();
		    } catch (Exception ex) {};
		}
	    } 
	}
    }

    public static boolean implementsInterface( String classname, Class tosubclass ) {
	try {
	    if ( classname.equals("org.apache.xalan.extensions.ExtensionHandlerGeneral") || 
		 classname.equals("org.apache.log4j.net.ZeroConfSupport") 
	    ) throw new ClassNotFoundException( "Classes breaking this work" );
	    // JE: Here there is a bug that is that it is not possible
	    // to have ALL interfaces with this function!!!
	    // This is really stupid but that's life
	    // So it is compulsory that AlignmentProcess be declared 
	    // as implemented
	    Class cl = Class.forName(classname);
	    // It is possible to suppress here abstract classes by:
	    if ( java.lang.reflect.Modifier.isAbstract( cl.getModifiers() ) ) return false;
	    Class[] interfaces = cl.getInterfaces();
	    for ( int i=interfaces.length-1; i >= 0  ; i-- ){
		if ( interfaces[i] == tosubclass ) {
		    //logger.trace(" -j-> {}", classname);
		    return true;
		}
		//logger.trace("       I> {}", interfaces[i] );
	    }
	    // Not one of our classes
	} catch ( ExceptionInInitializerError eiie ) {
	} catch ( NoClassDefFoundError ncdex ) {
	} catch ( ClassNotFoundException cnfex ) {
	} catch ( UnsatisfiedLinkError ule ) {
	    //logger.trace("   ******** {}", classname);
	}
	return false;
    }

    /**
     * Display all the classes inheriting or implementing a given
     * interface in the currently loaded packages.
     * @param interfaceName the name of the interface to implement
     */
    public static Set<String> implementations( String interfaceName ) {
	Set<String> list = new HashSet<String>();
	try {
	    Class toclass = Class.forName(interfaceName);
	    //Package [] pcks = Package.getPackages();
	    //for (int i=0;i<pcks.length;i++) {
		//logger.trace(interfaceName+ ">> "+pcks[i].getName() );
		//implementations( pcks[i].getName(), toclass, list );
		//}
	    implementations( toclass, list );
	} catch (ClassNotFoundException ex) {
	    logger.debug( "IGNORED Class {} not found!", interfaceName );
	}
	return list;
    }

    protected class Aligner implements Runnable {
	private Message mess = null;
	private Message result = null;
	private String id = null;

	public Aligner( Message m, String id ) {
	    mess = m;
	    this.id = id;
	}

	public Message getResult() {
	    return result;
	}

	public void run() {
	    Properties params = mess.getParameters();
	    String method = params.getProperty("method");
	    // find and access o, o'
	    URI uri1 = null;
	    URI uri2 = null;

	    try {
		uri1 = new URI(params.getProperty("onto1"));
		uri2 = new URI(params.getProperty("onto2"));
	    } catch (Exception e) {
		result = new NonConformParameters(newId(),mess,serverId,mess.getSender(),"nonconform/params/onto",(Properties)null);
		return;
	    };

	    // find initial alignment
	    Alignment init = null;
	    if ( params.getProperty("init") != null && !params.getProperty("init").equals("") ) {
		try {
		    //logger.trace(" Retrieving init");
		    try {
			init = alignmentCache.getAlignment( params.getProperty("init") );
		} catch (Exception e) {
			result = new UnknownAlignment(newId(),mess,serverId,mess.getSender(),params.getProperty("init"),(Properties)null);
			return;
		    }
		} catch (Exception e) {
		    result = new UnknownAlignment(newId(),mess,serverId,mess.getSender(),params.getProperty("init"),(Properties)null);
		    return;
		}
	    }
	    
	    // Create alignment object
	    try {
		Object[] mparams = {};
		if ( method == null )
		    method = "fr.inrialpes.exmo.align.impl.method.StringDistAlignment";
		Class<?> alignmentClass = Class.forName(method);
		Class[] cparams = {};
		java.lang.reflect.Constructor alignmentConstructor = alignmentClass.getConstructor(cparams);
		AlignmentProcess aresult = (AlignmentProcess)alignmentConstructor.newInstance(mparams);
		try {
		    aresult.init( uri1, uri2 );
		    long time = System.currentTimeMillis();
		    aresult.align( init, params ); // add opts
		    long newTime = System.currentTimeMillis();
		    aresult.setExtension( Namespace.ALIGNMENT.uri, Annotations.TIME, Long.toString(newTime - time) );
		    aresult.setExtension( Namespace.ALIGNMENT.uri, Annotations.TIME, Long.toString(newTime - time) );
		    String pretty = params.getProperty( "pretty" );
		    if ( pretty != null && !pretty.equals("") )
			aresult.setExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY, pretty );
		} catch (AlignmentException e) {
		    // The unreachability test has already been done
		    // JE 15/1/2009: commented the unreachability test
		    if ( reachable( uri1 ) == null ){
			result = new UnreachableOntology(newId(),mess,serverId,mess.getSender(),params.getProperty("onto1"),(Properties)null);
		    } else if ( reachable( uri2 ) == null ){
			result = new UnreachableOntology(newId(),mess,serverId,mess.getSender(),params.getProperty("onto2"),(Properties)null);
		    } else {
			result = new NonConformParameters(newId(),mess,serverId,mess.getSender(),"nonconform/params/"+e.getMessage(),(Properties)null);
		    }
		    return;
		}
		// ask to store A'
		alignmentCache.recordNewAlignment( id, aresult, true );
		result = new AlignmentId(newId(),mess,serverId,mess.getSender(),id,(Properties)null,
			       aresult.getExtension( Namespace.ALIGNMENT.uri, Annotations.PRETTY ));
	    } catch ( ClassNotFoundException cnfex ) {
		logger.error( "Unknown method", cnfex );
		result = new UnknownMethod(newId(),mess,serverId,mess.getSender(),method,(Properties)null);
	    } catch (NoSuchMethodException e) {
		result = new RunTimeError(newId(),mess,serverId,mess.getSender(),"No such method: "+method+"(Object, Object)",(Properties)null);
	    } catch (InstantiationException e) {
		result = new RunTimeError(newId(),mess,serverId,mess.getSender(),"Instantiation",(Properties)null);
	    } catch (IllegalAccessException e) {
		result = new RunTimeError(newId(),mess,serverId,mess.getSender(),"Cannot access",(Properties)null);
	    } catch (InvocationTargetException e) {
		result = new RunTimeError(newId(),mess,serverId,mess.getSender(),"Invocation target",(Properties)null);
	    } catch (AlignmentException e) {
		result = new NonConformParameters(newId(),mess,serverId,mess.getSender(),"nonconform/params/",(Properties)null);
	    } catch (Exception e) {
		result = new RunTimeError(newId(),mess,serverId,mess.getSender(),"Unexpected exception :"+e,(Properties)null);
	    }
	}
    }

    
}
