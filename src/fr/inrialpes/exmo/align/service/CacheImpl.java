/*
 * $Id: CacheImpl.java 1846 2013-03-25 15:28:06Z euzenat $
 *
 * Copyright (C) Seungkeun Lee, 2006
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
 */

package fr.inrialpes.exmo.align.service;

import java.util.Enumeration;
import java.util.Iterator;
import java.util.Hashtable;
import java.util.Vector;
import java.util.Collection;
import java.util.Set;
import java.util.HashSet;
import java.util.Date;
import java.util.Random;
import java.util.Properties;
import java.net.URI;
import java.net.URISyntaxException;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.PreparedStatement;
import java.sql.SQLException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import fr.inrialpes.exmo.align.impl.BasicAlignment;
import fr.inrialpes.exmo.align.impl.BasicRelation;
import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;
import fr.inrialpes.exmo.align.impl.URIAlignment;
import fr.inrialpes.exmo.align.impl.URICell;
import fr.inrialpes.exmo.align.impl.Namespace;

import fr.inrialpes.exmo.ontowrap.Ontology;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;
import java.io.PrintStream;
import java.io.EOFException;

/**
 * This class caches the content of the alignment database. I.e.,
 * It loads the metadata in the hash table
 * It stores the alignment when requested
 * It 
 */

public class CacheImpl {
    final static Logger logger = LoggerFactory.getLogger( CacheImpl.class );

    Hashtable<String,Alignment> alignmentTable = null;
    Hashtable<URI,Set<Alignment>> ontologyTable = null;

    String host = null;
    String port = null;
    int rights = 1; // writing rights in the database (default is 1)

    String idprefix = null;

    final int VERSION = 450; // Version of the API to be stored in the database
    /* 300: initial database format
       301: ADDED alignment id as primary key
       302: ALTERd cached/stored/ouri tag forms
       310: ALTERd extension table with added URIs and method -> val 
       340: ALTERd size of relation in cell table (5 -> 25)
       400: ALTERd size of relation in cell table (5 -> 255 because of URIs)
            ALTERd all URI size to 255
	    ALTERd level size to 25
            ADDED cell_id as keys?
       450: ADDED ontology database / reduced alignment database
	    ADDED prefix in server
            ADDED dependency database (no action)
     */

    DBService service = null;
    Connection conn = null;
	
    final int CONNECTION_ERROR = 1;
    final int SUCCESS = 2;
    final int INIT_ERROR = 3;

    static private final String SVCNS = Namespace.ALIGNSVC.getUriPrefix();
    static private final String CACHED = "cached";
    static private final String STORED = "stored";
    static private final String ALID = "alid/";
    static private final String OURI1 = "ouri1";
    static private final String OURI2 = "ouri2";
	
    //**********************************************************************

    public CacheImpl( DBService serv ) {
	service = serv;
	try {
	    conn = service.getConnection();
	} catch( Exception e ) {
	    logger.warn( "Cannot connect to DB", e );
	}
	alignmentTable = new Hashtable<String,Alignment>();
	ontologyTable = new Hashtable<URI,Set<Alignment>>();
    }

    public void reset() throws SQLException {
	alignmentTable = new Hashtable<String,Alignment>();
	ontologyTable = new Hashtable<URI,Set<Alignment>>();
	// reload alignment descriptions
	loadAlignments( true );
    }

    /**
     * loads the alignment descriptions from the database and put them in the
     * alignmentTable hashtable
     */
    public void init( Properties p, String prefix ) throws SQLException, AlignmentException {
	logger.debug( "Initializing Database cache" );
	port = p.getProperty("http"); // bad idea
	host = p.getProperty("host");
	idprefix = prefix;
	Statement st = createStatement();
	// test if a database is here, otherwise create it
	ResultSet rs = conn.getMetaData().getTables(null,null, "server", new String[]{"TABLE"});
	if ( !rs.next() ) {
	    initDatabase();
	} else {
	    updateDatabase(); // in case it is necessary to upgrade
	}
	String pref = p.getProperty("prefix");
	if ( pref == null || pref.equals("") ) {
	    rs = st.executeQuery( "SELECT prefix FROM server WHERE port='port'" );
	    while( rs.next() ) {
		idprefix = rs.getString("prefix");
	    }
	}
	st.close();
	// register by the database
	registerServer( host, port, rights==1, idprefix );
	// load alignment descriptions
	loadAlignments( true );
    }

    public void close() throws SQLException  {
	Statement st = createStatement();
	// unregister by the database
	st.executeUpdate( "DELETE FROM server WHERE host='"+host+"' AND port='"+port+"'" );
	st.close();
	conn.close();
    }

    public Statement createStatement() throws SQLException {
	conn = service.getConnection();
	return conn.createStatement();
    }

    // **********************************************************************
    // LOADING FROM DATABASE
    /**
     * loads the alignment descriptions from the database and put them in the
     * alignmentTable hashtable
     * index them under the ontology URIs
     */
    private void loadAlignments( boolean force ) throws SQLException {
	logger.debug( "Loading alignments..." );
	String id = null;
	Alignment alignment = null;
	Vector<String> idInfo = new Vector<String>();
	Statement st = createStatement();
	
	if (force) {
	    // Retrieve the alignment ids
	    ResultSet rs = st.executeQuery("SELECT id FROM alignment");
	    while(rs.next()) {
		id = rs.getString("id");
		idInfo.add(id);	
	    }
	    
	    // For each alignment id store metadata
	    for( int i = 0; i < idInfo.size(); i ++ ) {
		id = idInfo.get(i);
		alignment = retrieveDescription( id );
		recordAlignment( recoverAlignmentUri( id ), alignment, true );
	    }							
	}
	st.close();
    }

    protected Enumeration<Alignment> listAlignments() {
	return alignmentTable.elements();
    }

    protected Collection<Alignment> alignments() {
	return alignmentTable.values();
    }

    protected Collection<URI> ontologies() {
	return ontologyTable.keySet();
    }

    protected Collection<Alignment> alignments( URI u1, URI u2 ) {
	Collection<Alignment> results = new HashSet<Alignment>();
	if ( u1 != null ) {
	    for ( Alignment al : ontologyTable.get( u1 ) ) {
		try {
		    //    if ( al.getOntology1URI().equals( u1 ) ) {
		    if ( u2 == null ) results.add( al );
		    else if ( al.getOntology2URI().equals( u2 ) 
			      || al.getOntology1URI().equals( u2 )) results.add( al );
		    //    }
		} catch (AlignmentException alex) {
		    logger.debug( "IGNORED Exception", alex );
		}
	    }
	} else if ( u2 != null ) {
	    for ( Alignment al : ontologyTable.get( u2 ) ) {
		results.add( al );
	    }
	} else { results = alignmentTable.values(); }
	return results;
    }

    protected void flushCache() {// throws AlignmentException
	for ( Alignment al : alignmentTable.values() ){
	    if ( al.getExtension(SVCNS, CACHED ) != null && 
		 !al.getExtension( SVCNS, CACHED ).equals("") &&
		 al.getExtension(SVCNS, STORED ) != null && 
		 !al.getExtension( SVCNS, STORED ).equals("") ) flushAlignment( al );
	};
    }

    /**
     * loads the description of alignments from the database and set them
     * in an alignment object
     */
    protected Alignment retrieveDescription( String id ){
	ResultSet rs;
	String tag;
	String value;

	logger.debug( "Loading alignment {}", id );
	URIAlignment result = new URIAlignment();
	Statement st = null;
	try {
	    st = createStatement();
	    // Get basic ontology metadata
	    rs = st.executeQuery( "SELECT * FROM alignment WHERE id = '" + id  +"'" );
	    while( rs.next() ) {
		result.setLevel(rs.getString("level"));
		result.setType(rs.getString("type"));	
	    }

	    // Get ontologies
	    rs = st.executeQuery( "SELECT * FROM ontology WHERE id = '" + id  +"'" );
	    while(rs.next()) {
		if ( rs.getBoolean("source") ) {
		    result.getOntologyObject1().setURI( new URI(rs.getString("uri"))  );
		    if ( rs.getString("file") != null ) 
		       result.setFile1( new URI( rs.getString("file") ) );
		    if ( rs.getString("formuri") != null ) 
			result.getOntologyObject1().setFormURI( new URI(rs.getString("formuri"))  );
		    if ( rs.getString("formname") != null ) 
			result.getOntologyObject1().setFormalism( rs.getString("formname")  );
		    result.setExtension( SVCNS, OURI1, rs.getString("uri") );
		} else {
		    result.getOntologyObject2().setURI( new URI(rs.getString("uri"))  );
		    if ( rs.getString("file") != null ) 
			result.setFile2( new URI( rs.getString("file") ) );
		    if ( rs.getString("formuri") != null ) 
			result.getOntologyObject2().setFormURI( new URI(rs.getString("formuri"))  );
		    if ( rs.getString("formname") != null ) 
			result.getOntologyObject2().setFormalism( rs.getString("formname")  );
		    result.setExtension( SVCNS, OURI2, rs.getString("uri") );
		}
	    }

	    // Get dependencies if necessary

	    // Get extension metadata
	    rs = st.executeQuery( "SELECT * FROM extension WHERE id = '" + id + "'" );
	    while(rs.next()) {
		tag = rs.getString("tag");
		value = rs.getString("val");
		result.setExtension( rs.getString("uri"), tag, value);
	    }
	} catch (Exception e) { // URI exception that should not occur
	    logger.debug( "IGNORED unlikely URI exception", e);
	    return null;
	} finally {
	    try { st.close(); } catch (Exception ex) {};
	}
	// has been extracted from the database
	//result.setExtension( SVCNS, STORED, "DATE");
	// not yet cached (this instruction should be useless)
	result.setExtension( SVCNS, CACHED, (String)null );
	return result;
    }

    /**
     * loads the full alignment from the database and put it in the
     * alignmentTable hastable
     * 
     * should be invoked when:
     * 	( result.getExtension(CACHED) == ""
     * && result.getExtension(STORED) != "") {

     */
    protected Alignment retrieveAlignment( String uri, Alignment alignment ) throws SQLException, AlignmentException, URISyntaxException {
	String id = stripAlignmentUri( uri );
	URI ent1 = null, ent2 = null;

	alignment.setOntology1( new URI( alignment.getExtension( SVCNS, OURI1 ) ) );
	alignment.setOntology2( new URI( alignment.getExtension( SVCNS, OURI2 ) ) );

	// Get cells
	Statement st = createStatement();
	Statement st2 = createStatement();
	ResultSet rs = st.executeQuery( "SELECT * FROM cell WHERE id = '" + id + "'" );
	while( rs.next() ) {
	    ent1 = new URI( rs.getString("uri1") );
	    ent2 = new URI( rs.getString("uri2") );
	    if ( ent1 == null || ent2 == null ) break;
	    Cell cell = alignment.addAlignCell(ent1, ent2, rs.getString("relation"), Double.parseDouble(rs.getString("measure")));
	    String cid = rs.getString( "cell_id" );
	    if ( cid != null && !cid.equals("") ) {
		if ( !cid.startsWith("##") ) {
		    cell.setId( cid );
		}
		ResultSet rse2 = st2.executeQuery("SELECT * FROM extension WHERE id = '" + cid + "'");
		while ( rse2.next() ){
		    cell.setExtension( rse2.getString("uri"), 
				       rse2.getString("tag"), 
				       rse2.getString("val") );
		}
	    }
	    cell.setSemantics( rs.getString( "semantics" ) );
	}

	// reset
	resetCacheStamp(alignment);
	st.close();
	return alignment;
    }
    
    /**
     * unload the cells of an alignment...
     * This should help retrieving some space
     * 
     * should be invoked when:
     * 	( result.getExtension(CACHED) != ""
     *  && obviously result.getExtension(STORED) != ""
     */
    protected void flushAlignment( Alignment alignment ) {// throws AlignmentException
	//alignment.removeAllCells();
	// reset
    	//alignment.setExtension( SVCNS, CACHED, "" );
    }
    
    //**********************************************************************
    // DEALING WITH URIs

    // Public because this is now used by AServProtocolManager
    public String generateAlignmentUri() {
	// Generate an id based on a URI prefix + Date + random number
	return recoverAlignmentUri( generateId() );
    }
    
    public String recoverAlignmentUri( String id ) {
	// Recreate Alignment URI from its id
	return idprefix + "/" + ALID + id;
    }
    
    public String stripAlignmentUri( String alid ) {
	return alid.substring( alid.indexOf( ALID )+5 );
    }

    /*
     * Rules for cell ids:
     * (1) if users set cell_id uses them (check them for URI)
     * (2) if not, generate a *local* cell id if necessary and add ##
     * (3) use these cell-id in the extension part...
     * STORE:
     * if cell has extension && no id, create cell id, store it in db, not in setId
     * if cell has extension && id, us it with getId/setId
     * UNSTORE:
     * suppress those extensions with the cell_id if exists
     * LOAD-FROM-DB: 
     * if there is a cell id, use it for loading extensions
     * At alignment store time, use getCellId -> store it
     * At alignment load-from-db time, get the id and all the 
     */

    private String generateCellId() {
	return "##"+generateId();
    }
    
    private String generateId() {
	// Generate an id based on Date + random number
	return new Date().getTime() + "/" + randomNum();
    }
    
    private int randomNum() {
	Random rand = new Random(System.currentTimeMillis());
	return Math.abs(rand.nextInt(1000)); 
    }

    //**********************************************************************
    // FETCHING FROM CACHE
    /**
     * retrieve alignment metadata from id
     * This is more difficult because we return the alignment we have 
     * disreagarding if it is complete o only metadata
     */
    public Alignment getMetadata( String uri ) throws AlignmentException {
	Alignment result = alignmentTable.get( uri );
	if ( result == null )
	    throw new AlignmentException("getMetadata: Cannot find alignment");
	return result;
    }
	
    /**
     * retrieve full alignment from id (and cache it)
     */
    public Alignment getAlignment( String uri ) throws AlignmentException, SQLException {
	Alignment result = null;
	try {
	    result = alignmentTable.get( uri );
	} catch( Exception ex ) {
	    //logger.trace( "Unknown exception with Id = {}", uri );
	    logger.debug( "IGNORED: Unknown exception", ex );
	}
	
	if ( result == null ) {
	    //logger.trace( "Cache: Id ={} is not found.", uri );
	    throw new AlignmentException( "getAlignment: Cannot find alignment "+uri );
	}

	// If not cached, retrieve it now
	if ( ( result.getExtension( SVCNS, CACHED ) == null || result.getExtension( SVCNS, CACHED ).equals("") )
	     && result.getExtension(SVCNS, STORED ) != null 
	     && !result.getExtension(SVCNS, STORED ).equals("") ) {
	    try { retrieveAlignment( uri, result ); }
	    catch ( URISyntaxException urisex ) {
		logger.trace( "Cache: cannot read from DB", urisex );
		throw new AlignmentException( "getAlignment: Cannot find alignment", urisex );
	    };
	}
	return result;
    }
	
    public Set<Alignment> getAlignments( URI uri ) {
	return ontologyTable.get( uri );
    }

    /**
     * returns the alignments between two ontologies
     * if one of the ontologies is null, then return them all
     */
    public Set<Alignment> getAlignments( URI uri1, URI uri2 ) {
	Set<Alignment> result;
	Set<Alignment> potential = new HashSet<Alignment>();
	if ( uri2 != null ){
	    String uri2String = uri2.toString();
	    Set<Alignment> found = ontologyTable.get( uri2 );
	    if ( found != null ) {
		for( Alignment al : found ) {
		    if ( al.getExtension(SVCNS, OURI2).equals( uri2String ) ) {
			potential.add( al );
		    }
		}
	    }
	} 
	if ( uri1 != null ) {
	    if ( potential.isEmpty() ) {
		Set<Alignment> found = ontologyTable.get( uri1 );
		if ( found != null ) {
		    potential = found;
		} else return potential;
	    }
	    result = new HashSet<Alignment>();
	    String uri1String = uri1.toString();
	    for(  Alignment al : potential ) {
		// This is not the best because URI are not resolved here...
		if ( al.getExtension(SVCNS, OURI1).equals( uri1String ) ) {
		    result.add( al );
		}
	    }
	} else { result = potential; }
	return result;
    }

    //**********************************************************************
    // RECORDING ALIGNMENTS
    /**
     * records newly created alignment
     */
    public String recordNewAlignment( Alignment alignment, boolean force ) {
	try { return recordNewAlignment( generateAlignmentUri(), alignment, force );
	} catch (AlignmentException ae) { return (String)null; }
    }

    /**
     * records alignment identified by id
     */
    public String recordNewAlignment( String uri, Alignment al, boolean force ) throws AlignmentException {
	Alignment alignment = al;
 
	alignment.setExtension(SVCNS, OURI1, alignment.getOntology1URI().toString());
	alignment.setExtension(SVCNS, OURI2, alignment.getOntology2URI().toString());
	// Index
	recordAlignment( uri, alignment, force );
	// Not yet stored
	alignment.setExtension(SVCNS, STORED, (String)null);
	// Cached now
	resetCacheStamp(alignment);
	return uri;
    }

    /**
     * records alignment identified by id
     */
    public String recordAlignment( String uri, Alignment alignment, boolean force ) {
	// record the Alignment at the corresponding Uri in tables!
	alignment.setExtension( Namespace.ALIGNMENT.uri, Annotations.ID, uri );

	// Store it
	try {
	    URI ouri1 = new URI( alignment.getExtension( SVCNS, OURI1) );
	    URI ouri2 = new URI( alignment.getExtension( SVCNS, OURI2) );
	    if ( force || alignmentTable.get( uri ) == null ) {
		Set<Alignment> s1 = ontologyTable.get( ouri1 );
		if ( s1 == null ) {
		    s1 = new HashSet<Alignment>();
		    ontologyTable.put( ouri1, s1 );
		}
		s1.add( alignment );
		Set<Alignment> s2 = ontologyTable.get( ouri2 );
		if ( s2 == null ) {
		    s2 = new HashSet<Alignment>();
		    ontologyTable.put( ouri2, s2 );
		}
		s2.add( alignment );
		alignmentTable.put( uri, alignment );
	    }
	    return uri;
	} catch ( Exception e ) {
	    logger.debug( "IGNORED: Unlikely URI exception", e );
	    return null;
	}
    }

    /**
     * suppresses the record for an alignment
     */
    public void unRecordAlignment( Alignment alignment ) {
	String id = alignment.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID );
	try {
	    Set<Alignment> s1 = ontologyTable.get( new URI( alignment.getExtension( SVCNS, OURI1) ) );
	    if ( s1 != null ) s1.remove( alignment );
	    Set<Alignment> s2 = ontologyTable.get( new URI( alignment.getExtension( SVCNS, OURI2) ) );
	    if ( s2 != null ) s2.remove( alignment );
	} catch ( URISyntaxException uriex ) {
	    logger.debug( "IGNORED: Unlikely URI exception", uriex );
	}
	alignmentTable.remove( id );
    }

    //**********************************************************************
    // STORING IN DATABASE
    /**
     * quote:
     * Prepare a string to be used in SQL queries by preceeding occurences of
     * "'", """, and "\" by a "\".
     * This should be implemented at a lower level within Java itself
     * (or the sql package).
     * This function is used here for protecting everything to be entered in
     * the database
     */
    public String quote( String s ) {
	if ( s == null ) return "NULL";
	String result = "'";
	char[] chars = s.toCharArray();
	int j = 0;
	int i = 0;
	char c;
	for ( ; i < chars.length; i++ ){
	    c = chars[i];
	    if ( c == '\'' || c == '"' || c == '\\' ) {
		result += new String( chars, j, i-j ) + "\\" + c;
		j = i+1;
	    };
	}
	return result + new String( chars, j, i-j ) + "'";
    }

    public boolean isAlignmentStored( Alignment alignment ) {
	return ( alignment.getExtension( SVCNS, STORED ) != null &&
		 !alignment.getExtension( SVCNS, STORED ).equals("") );
    }


    /**
     * Non publicised class
     */
    public void eraseAlignment( String uri, boolean eraseFromDB ) throws SQLException, AlignmentException {
        Alignment alignment = getAlignment( uri );
        if ( alignment != null ) {
            if ( eraseFromDB ) unstoreAlignment( uri, alignment );
            // Suppress it from the cache...
            unRecordAlignment( alignment );
        }
    }

    /**
     * Non publicised class
     */
    public void unstoreAlignment( String uri ) throws SQLException, AlignmentException {
	Alignment alignment = getAlignment( uri );
	if ( alignment != null ) {
	    unstoreAlignment( uri, alignment );
	}
    }

    public void unstoreAlignment( String uri, Alignment alignment ) throws SQLException, AlignmentException {
	Statement st = createStatement();
	String id = stripAlignmentUri( uri );
	try {
	    conn.setAutoCommit( false );
	    // Delete cell's extensions
	    ResultSet rs = st.executeQuery( "SELECT cell_id FROM cell WHERE id='"+id+"'" );
	    while ( rs.next() ){
		String cid = rs.getString("cell_id");
		if ( cid != null && !cid.equals("") ) {
		    st.executeUpdate( "DELETE FROM extension WHERE id='"+cid+"'" );
		}
	    }
	    st.executeUpdate("DELETE FROM cell WHERE id='"+id+"'");
	    st.executeUpdate("DELETE FROM extension WHERE id='"+id+"'");
	    st.executeUpdate("DELETE FROM ontology WHERE id='"+id+"'");
	    st.executeUpdate("DELETE FROM dependency WHERE id='"+id+"'");
	    st.executeUpdate("DELETE FROM alignment WHERE id='"+id+"'");
	    alignment.setExtension( SVCNS, STORED, (String)null);
	} catch ( SQLException sex ) {
	    conn.rollback();
	    logger.warn( "SQLError", sex );
	    throw sex;
	} finally {
	    conn.setAutoCommit( false );
	    st.close();
	}
    }

    public void storeAlignment( String uri ) throws AlignmentException, SQLException {
	String query = null;
	BasicAlignment alignment = (BasicAlignment)getAlignment( uri );
	String id = stripAlignmentUri( uri );
	Statement st = null;
	// We store stored date
	alignment.setExtension( SVCNS, STORED, new Date().toString());
	// We empty cached date
	alignment.setExtension( SVCNS, CACHED, (String)null );

	// Try to store at most 3 times.
	// Otherwise, an exception EOFException will be thrown (relation with Jetty???)
	// [JE2013: Can we check this?]
	for( int i=0; i < 3 ; i++ ) {
	    st = createStatement();
	    try {
		logger.debug( "Storing alignment {} as {}", uri, id );
		conn.setAutoCommit( false );
		query = "INSERT INTO alignment " + 
		    "(id, type, level) " +
		    "VALUES (" +quote(id)+","+quote(alignment.getType())+","+quote(alignment.getLevel()) +")";
		st.executeUpdate(query);
		
		recordOntology( st, id, true,
				alignment.getOntology1URI(),
				alignment.getFile1(), 
				alignment.getOntologyObject1() );
		recordOntology( st, id, false,
				alignment.getOntology2URI(),
				alignment.getFile2(), 
				alignment.getOntologyObject2() );
		
		// store dependencies
		
		for ( String[] ext : alignment.getExtensions() ) {
		    String turi = ext[0];
		    String tag = ext[1];
		    String val = ext[2];
		    query = "INSERT INTO extension " + 
			"(id, uri, tag, val) " +
			"VALUES (" + quote(id) + "," +  quote(turi) + "," +  quote(tag) + "," + quote(val) + ")";
		    st.executeUpdate(query);
		}
		
		for( Cell c : alignment ) {
		    String cellid = null;
		    if ( c.getObject1() != null && c.getObject2() != null ){
			cellid = c.getId();
			if ( cellid != null ){
			    if ( cellid.startsWith("#") ) {
				cellid = alignment.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID ) + cellid;
			    }
			} else if ( c.getExtensions() != null ) {
			    // JE: In case of extensions create an ID
			    cellid = generateCellId();
			}
			else cellid = "";
			String uri1 = c.getObject1AsURI(alignment).toString();
			String uri2 = c.getObject2AsURI(alignment).toString();
			String strength = c.getStrength() + ""; // crazy Java
			String sem;
			if ( !c.getSemantics().equals("first-order") )
			    sem = c.getSemantics();
			else sem = "";
			String rel =  ((BasicRelation)c.getRelation()).getRelation();	
			query = "INSERT INTO cell " + 
			    "(id, cell_id, uri1, uri2, measure, semantics, relation) " +
			    "VALUES (" + quote(id) + "," + quote(cellid) + "," + quote(uri1) + "," + quote(uri2) + "," + quote(strength) + "," + quote(sem) + "," + quote(rel) + ")";
			st.executeUpdate(query);
		    }
		    if ( cellid != null && !cellid.equals("") && c.getExtensions() != null ) {
			// Store extensions
			for ( String[] ext : c.getExtensions() ) {
			    String turi = ext[0];
			    String tag = ext[1];
			    String val = ext[2];
			    query = "INSERT INTO extension " + 
				"(id, uri, tag, val) " +
				"VALUES (" + quote(cellid) + "," +  quote(turi) + "," +  quote(tag) + "," + quote(val) + ")";
			    st.executeUpdate(query);
			}
		    }
		}
	    } catch ( SQLException sex ) {
		logger.warn( "SQLError", sex );
		conn.rollback();
		throw sex;
	    } finally {
		conn.setAutoCommit( true );
	    }
	    break;
	}
	st.close();
	// We reset cached date
	resetCacheStamp(alignment);
    }

    // Do not add transaction here: this is handled by caller
    public void	recordOntology( Statement st, String id, boolean source, URI uri, URI file, Ontology onto ) throws SQLException {
	String sfile = "";
	String suri = "";
	if ( file != null ) sfile = file.toString();
	if ( uri != null ) suri = uri.toString();
	String query = null;
	logger.debug( "Recording ontology {} with file {}", suri, sfile );

	if ( onto != null ) {
	    query = "INSERT INTO ontology " + 
		"(id, uri, file, source, formname, formuri) " +
		"VALUES ("+quote(id)+","+ quote(suri)+","+quote(sfile)+"," +(source?'1':'0')+","+quote(onto.getFormalism())+","+quote(onto.getFormURI().toString())+")";
	} else {
	    query = "INSERT INTO ontology " + 
		"(id, uri, file, source) " +
		"VALUES ("+quote(id)+","+ quote(suri)+","+quote(sfile)+"," +(source?'1':'0')+")";
	    }
	st.executeUpdate(query);
    }

    //**********************************************************************
    // CACHE MANAGEMENT (Not implemented yet)
    public void resetCacheStamp( Alignment result ){
	result.setExtension(SVCNS, CACHED, new Date().toString() );
    }

    public void cleanUpCache() {
	// for each alignment in the table
	// set currentDate = Date();
	// if ( DateFormat.parse( result.getExtension(SVCNS, CACHED) ).before( ) ) {
	// - for each ontology if no other alignment => unload
	// - clean up cells
	// }
    }

    // **********************************************************************
    // DATABASE CREATION AND UPDATING
    /*
      # server info

      create table server (
      host varchar(50),
      port varchar(5),
      prefix varchar(50),
      edit varchar(5)
      );
   

      # alignment info
      
      create table alignment (
      id varchar(100), 
      type varchar(5),
      level varchar(25),
      primary key (id));

      # ontology info

      create table ontology (
      id varchar(255), 
      uri varchar(255),
      file varchar(255),
      source boolean,
      formname varchar(50),
      formuri varchar(255)
      );

      # dependencies info

      create table dependency (
      id varchar(255), 
      dependsOn varchar(255)
      );

      # cell info

      create table cell(
      id varchar(100),
      cell_id varchar(255),
      uri1 varchar(255),
      uri2 varchar(255),
      semantics varchar(30),
      measure varchar(20),
      relation varchar(255));

      # extension info
      
      create table extension(
      id varchar(100),
      uri varchar(200),
      tag varchar(50),
      val varchar(500));

    */

    public void initDatabase() throws SQLException {
	logger.info( "Initialising database" );
	Statement st = createStatement();
	try {
	    conn.setAutoCommit( false );
	    // Create tables
	    st.executeUpdate("CREATE TABLE alignment (id VARCHAR(100), type VARCHAR(5), level VARCHAR(25), primary key (id))");
	    st.executeUpdate("CREATE TABLE ontology (id VARCHAR(255), source BOOLEAN, uri VARCHAR(255), formname VARCHAR(50), formuri VARCHAR(255), file VARCHAR(255), primary key (id, source))");
	    st.executeUpdate("CREATE TABLE dependency (id VARCHAR(255), dependsOn VARCHAR(255))");
	    st.executeUpdate("CREATE TABLE cell(id VARCHAR(100), cell_id VARCHAR(255), uri1 VARCHAR(255), uri2 VARCHAR(255), semantics VARCHAR(30), measure VARCHAR(20), relation VARCHAR(255))");
	    st.executeUpdate("CREATE TABLE extension(id VARCHAR(100), uri VARCHAR(200), tag VARCHAR(50), val VARCHAR(500))");
	    st.executeUpdate("CREATE TABLE server (host VARCHAR(50), port VARCHAR(5), prefix VARCHAR (50), edit BOOLEAN, version VARCHAR(5))");
	    st.close();

	    // Register *DATABASE* Because of the values (that some do not like), this is a special statement
	    registerServer( "dbms", "port", false, idprefix );
	} catch ( SQLException sex ) {
	    logger.warn( "SQLError", sex );
	    conn.rollback();
	    throw sex;
	} finally {
	    conn.setAutoCommit( true );
	}
    }

    public void resetDatabase( boolean force ) throws SQLException, AlignmentException {
	Statement st = createStatement();
	try {
	    conn.setAutoCommit( false );
	    // Check that no one else is connected...
	    if ( force != true ){
		ResultSet rs = st.executeQuery("SELECT COUNT(*) AS rowcount FROM server WHERE edit=1");
		rs.next();
		int count = rs.getInt("rowcount") ;
		rs.close() ;
		if ( count > 1 ) {
		    throw new AlignmentException("Cannot init database: other processes use it");
		}
	    }
	    // Suppress old database if exists
	    st.executeUpdate("DROP TABLE IF EXISTS server");
	    st.executeUpdate("DROP TABLE IF EXISTS alignment");
	    st.executeUpdate("DROP TABLE IF EXISTS ontology");
	    st.executeUpdate("DROP TABLE IF EXISTS dependency");
	    st.executeUpdate("DROP TABLE IF EXISTS cell");
	    st.executeUpdate("DROP TABLE IF EXISTS extension");
	    // Redo it
	    initDatabase();
	  
	    // Register *THIS* server, etc. characteristics (incl. version name)
	    registerServer( host, port, rights==1, idprefix );
	} catch ( SQLException sex ) {
	    logger.warn( "SQLError", sex );
	    conn.rollback();
	    throw sex;
	} finally {
	    st.close();
	    conn.setAutoCommit( true );
	}
    }
    
    private void registerServer( String host, String port, Boolean writeable, String prefix ) throws SQLException {
	// Register *THIS* server, etc. characteristics (incl. version name)
	PreparedStatement pst = conn.prepareStatement("INSERT INTO server (host, port, edit, version, prefix) VALUES (?,?,?,?,?)");
	pst.setString(1,host);
	pst.setString(2,port);
	pst.setBoolean(3,writeable);
	pst.setString(4,VERSION+"");
	pst.setString(5,idprefix);
	pst.executeUpdate();
	pst.close();
    }

    /*
     * A dummy method, since it exists just ALTER TABLE ... DROP and ALTER TABLE ... ADD in SQL Language.
     * each dbms has its own language for manipulating table columns....
     */
    public void renameColumn(Statement st, String tableName, String oldName, String newName, String newType) throws SQLException { 
	try {
	    conn.setAutoCommit( false );
	    st.executeUpdate("ALTER TABLE "+tableName+" ADD "+newName+" "+newType);
	    st.executeUpdate("UPDATE "+tableName+" SET "+newName+"="+oldName);
	    st.executeUpdate("ALTER TABLE "+tableName+" DROP "+oldName);  
	} catch ( SQLException sex ) {
	    logger.warn( "SQLError", sex );
	    conn.rollback();
	    throw sex;
	} finally {
	    conn.setAutoCommit( true );
	}
    }
    
    /*
    * Another dummy method, since it exists just ALTER TABLE ... DROP and ALTER TABLE ... ADD in SQL Language.
    * each dbms has its own language for manipulating table columns....     
    */
    public void changeColumnType(Statement st, String tableName, String columnName, String newType) throws SQLException { 
	try {
	    conn.setAutoCommit( false );
	    String tempName = columnName+"temp";
	    renameColumn(st,tableName,columnName,tempName,newType);
	    renameColumn(st,tableName,tempName,columnName,newType);
	} catch ( SQLException sex ) {
	    logger.warn( "SQLError", sex );
	    conn.rollback();
	    throw sex;
	} finally {
	    conn.setAutoCommit( true );
	}
    }

    public void updateDatabase() throws SQLException, AlignmentException {
	Statement st = createStatement();
	// get the version number (port is the entry which is always here)
	ResultSet rs = st.executeQuery("SELECT version FROM server WHERE port='port'");
	rs.next();
	int version = rs.getInt("version") ;
	if ( version < VERSION ) {
	    if ( version >= 302 ) {
		if ( version < 310 ) {
		    logger.info( "Upgrading to version 3.1" );
		    // ALTER database
		    renameColumn(st,"extension","method","val","VARCHAR(500)");
		    // case mysql
		    //st.executeUpdate("ALTER TABLE extension CHANGE method val VARCHAR(500)");
		   
		    st.executeUpdate("ALTER TABLE extension ADD uri VARCHAR(200);");
		    // Modify extensions
		    ResultSet rse = st.executeQuery("SELECT * FROM extension");
		    Statement st2 = createStatement();
		    while ( rse.next() ){
			String tag = rse.getString("tag");
			//logger.trace(" Treating tag {} of {}", tag, rse.getString("id"));
			if ( !tag.equals("") ){
			    int pos;
			    String ns;
			    String name;
			    if ( (pos = tag.lastIndexOf('#')) != -1 ) {
				ns = tag.substring( 0, pos );
				name = tag.substring( pos+1 );
			    } else if ( (pos = tag.lastIndexOf(':')) != -1 && pos > 5 ) {
				ns = tag.substring( 0, pos )+"#";
				name = tag.substring( pos+1 );
			    } else if ( (pos = tag.lastIndexOf('/')) != -1 ) {
				ns = tag.substring( 0, pos+1 );
				name = tag.substring( pos+1 );
			    } else {
				ns = Namespace.ALIGNMENT.uri;
				name = tag;
			    }
			    //logger.trace("  >> {} : {}", ns, name);
			    st2.executeUpdate("UPDATE extension SET tag='"+name+"', uri='"+ns+"' WHERE id='"+rse.getString("id")+"' AND tag='"+tag+"'");
			}
		    }
		}
		// Nothing to do with 340: subsumed by 400
		if ( version < 400 ) {
		    logger.info("Upgrading to version 4.0");
		    // ALTER database 
		    changeColumnType(st,"cell","relation", "VARCHAR(255)");
		    changeColumnType(st,"cell","uri1", "VARCHAR(255)");
		    changeColumnType(st,"cell","uri2", "VARCHAR(255)");
		    
		    changeColumnType(st,"alignment","level", "VARCHAR(255)");
		    changeColumnType(st,"alignment","uri1", "VARCHAR(255)");
		    changeColumnType(st,"alignment","uri2", "VARCHAR(255)");
		    changeColumnType(st,"alignment","file1", "VARCHAR(255)");
		    changeColumnType(st,"alignment","file2", "VARCHAR(255)");
		    
		    renameColumn(st,"alignment","owlontology1","ontology1", "VARCHAR(255)");
		    renameColumn(st,"alignment","owlontology2","ontology2", "VARCHAR(255)");
		}
		if ( version < 450 ) {
		    logger.info("Upgrading to version 4.5");
		    logger.info("Creating Ontology table");
		    st.executeUpdate("CREATE TABLE ontology (id VARCHAR(255), uri VARCHAR(255), source BOOLEAN, file VARCHAR(255), formname VARCHAR(50), formuri VARCHAR(255), primary key (id, source))");
		    ResultSet rse = st.executeQuery("SELECT * FROM alignment");
		    while ( rse.next() ){
			Statement st2 = createStatement();
			// No Ontology _type_ available then
		    	st2.executeUpdate("INSERT INTO ontology (id, uri, source, file) VALUES ('"+rse.getString("id")+"','"+rse.getString("uri1")+"','1','"+rse.getString("file1")+"')");
		    	st2.executeUpdate("INSERT INTO ontology (id, uri, source, file) VALUES ('"+rse.getString("id")+"','"+rse.getString("uri2")+"','0','"+rse.getString("file2")+"')");
		    }
		    logger.info("Cleaning up Alignment table");
		    st.executeUpdate("ALTER TABLE alignment DROP ontology1");  
		    st.executeUpdate("ALTER TABLE alignment DROP ontology2");  
		    st.executeUpdate("ALTER TABLE alignment DROP uri1");  
		    st.executeUpdate("ALTER TABLE alignment DROP uri2");  
		    st.executeUpdate("ALTER TABLE alignment DROP file1");  
		    st.executeUpdate("ALTER TABLE alignment DROP file2");  
		    logger.debug("Altering server table");
		    st.executeUpdate("ALTER TABLE server ADD prefix VARCHAR(50);");
		    st.executeUpdate("UPDATE server SET prefix='"+idprefix+"'");
		    logger.debug("Updating server with prefix");
		    Statement stmt = null;
		    try { // In all alignment
			conn.setAutoCommit( false );
			stmt = conn.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE,
						    ResultSet.CONCUR_UPDATABLE);
			ResultSet uprs = stmt.executeQuery( "SELECT id FROM alignment" );
			while ( uprs.next() ) {
			    String oldid = uprs.getString("id");
			    String newid = stripAlignmentUri( oldid );
			    //logger.trace("Updating {} to {}", oldid, newid );
			    uprs.updateString( "id", newid );
			    uprs.updateRow();
			    // In all cell (for id and cell_id)
			    st.executeUpdate("UPDATE cell SET id='"+newid+"' WHERE id='"+oldid+"'" );
			    // In all extension
			    st.executeUpdate("UPDATE extension SET id='"+newid+"' WHERE id='"+oldid+"'" );
			    // In all ontology
			    st.executeUpdate("UPDATE ontology SET id='"+newid+"' WHERE id='"+oldid+"'" );
			}
			// Now, for each cell, with an id,
			// either recast the id ... or not
			conn.commit();
		    } catch ( SQLException e ) {
			logger.warn( "IGNORED Failed to update", e );
		    } finally {
			if ( stmt != null ) { stmt.close(); }
			conn.setAutoCommit( true );
		    }
		    logger.info("Creating dependency table");
		    st.executeUpdate("CREATE TABLE dependency (id VARCHAR(255), dependsOn VARCHAR(255))");
		    logger.info("Fixing legacy errors in cached/stored");
		    st.executeUpdate( "UPDATE extension SET val=( SELECT e2.val FROM extension e2 WHERE e2.tag='cached' AND e2.id=extension.id ) WHERE tag='stored' AND val=''" );
		    // We should also implement a clean up (suppress all starting with http://)
		}
		// ALTER version
		st.executeUpdate("UPDATE server SET version='"+VERSION+"'");
	    } else {
		throw new AlignmentException( "Database must be upgraded ("+version+" -> "+VERSION+")" );
	    }
	}
	st.close();
    }

}
