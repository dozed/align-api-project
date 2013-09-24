/*
 * $Id: AlignmentService.java 1841 2013-03-24 17:28:33Z euzenat $
 *
 * Copyright (C) INRIA, 2006-2009, 2010, 2013
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

import fr.inrialpes.exmo.queryprocessor.QueryProcessor;
import fr.inrialpes.exmo.queryprocessor.Result;
import fr.inrialpes.exmo.queryprocessor.Type;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gnu.getopt.LongOpt;
import gnu.getopt.Getopt;

import java.util.Hashtable;
import java.util.Properties;
import java.io.PrintStream;

import java.lang.reflect.InvocationTargetException;

/**
 * AlignmentService
 * 
 * The basic alignment service able to run a store and answer queries...
 * 
    <pre>
    java -jar alignsvc.jar [options]
    </pre>

    where the options are:
    <pre>
        --load=filename -l filename     Load previous image
	--params=filename -p filename   Read the parameters in file
        --help -h                       Print this message
    </pre>

<pre>
$Id: AlignmentService.java 1841 2013-03-24 17:28:33Z euzenat $
</pre>

 * @author Jérôme Euzenat
 */
public class AlignmentService {
    final static Logger logger = LoggerFactory.getLogger( AlignmentService.class );

    public String //DBMS Parameters
	DBHOST = "localhost",
	DBPORT = "3306",
	DBUSER = "adminAServ",
	DBPASS = "aaa345",
	DBBASE = "AServDB",
	DBMS   = "mysql";

    public static final String //Port Strings
	HTML = "8089",
	JADE = "8888",
	WSDL = "7777",
	JXTA = "6666";

    public static final String //IP Strings
	HOST = "localhost";

    private String filename = null;
    private String outfile = null;
    private String paramfile = null;
    private Hashtable<String,AlignmentServiceProfile> services = null;
    private Hashtable<String,Directory> directories = null;

    private AServProtocolManager manager;
    private DBService connection;

    public static void main(String[] args) {
	try { new AlignmentService().run( args ); }
	catch ( Exception ex ) {
	    logger.error( "FATAL error", ex );
	};
    }
    
    public void run(String[] args) throws Exception {
	services = new Hashtable<String,AlignmentServiceProfile>();
	directories = new Hashtable<String,Directory>();
	// Read parameters
	Properties params = readParameters( args );
	if ( outfile != null ) {
	    // This redirects error outout to log file given by -o
	    System.setErr( new PrintStream( outfile ) );
	} 
	logger.debug("Parameter parsed");

	// Shut down hook
	Runtime.getRuntime().addShutdownHook(new Thread(){
		public void run() { close(); } });

	// Connect database
	if( DBMS.equals("postgres") ) {
	    logger.debug("postgres driver");
	    DBPORT = "5432";
	    connection = new DBServiceImpl( "org.postgresql.Driver" ,  "jdbc:postgresql", DBPORT );
	} else {
	    logger.debug("mysql driver");
	    DBPORT = "3306";
	    connection = new DBServiceImpl( "com.mysql.jdbc.Driver" ,  "jdbc:mysql", DBPORT );
	}
	
	connection.init();
	connection.connect( DBHOST, DBPORT, DBUSER, DBPASS, DBBASE );
	logger.debug("Database connected");

	// Create a AServProtocolManager
	manager = new AServProtocolManager( directories );
	manager.init( connection, params );
	logger.debug("Manager created");

	// Launch services
	for ( AlignmentServiceProfile serv : services.values() ) {
	    try {
		serv.init( params, manager );
	    } catch ( AServException ex ) { // This should rather be the job of the caller
		logger.warn( "Cannot start {} server on {}:{}", new Object[] { serv, params.getProperty( "host" ), params.getProperty( "http" ) });
	    }
	}
	// Register to directories
	for ( Directory dir : directories.values() ) {
	    try {
		dir.open( params );
		logger.debug("{} connected.", dir);
	    } catch ( AServException ex ) {
		logger.warn( "Cannot connect to {} directory", dir );
		logger.debug( "IGNORED Connection exception", ex );
		// JE: this has to be done
		//directories.remove( name, dir );
	    }
	}

	// Wait loop
	while ( true ) {
	    // do not exhaust CPU
	    Thread.sleep(1000);
	}
    }

    protected void close(){
	logger.debug("Shuting down server");
	// [Directory]: unregister to directories
	for ( Directory dir : directories.values() ) {
	    try { dir.close(); }
	    catch ( AServException ex ) {
		logger.warn("Cannot unregister from {}", dir);
		logger.debug("IGNORED", ex);
	    }
	}
	// Close services
	for ( AlignmentServiceProfile serv : services.values() ) {
	    try { serv.close(); }
	    catch ( AServException ex ) {
		logger.debug("Cannot close {}", serv);
		logger.trace("IGNORED Exception", ex );
	    }
	}
	
	// Shut down database connection
	manager.close();
	connection.close();
	logger.debug("Database connection closed");
	System.err.close();
    }

    protected void finalize() throws Throwable {
	try { close(); }
	finally { super.finalize(); }
    }

    protected Object loadInstance( String className) throws ClassNotFoundException, NoSuchMethodException, InstantiationException, IllegalAccessException, InvocationTargetException {
	Class<?> cl = Class.forName(className);
	java.lang.reflect.Constructor constructor = cl.getConstructor( (Class[])null );
	return constructor.newInstance( (Object[])null );
    }


    public Properties readParameters( String[] args ) {
	Properties params = new Properties();
	// Default values
	params.setProperty( "host", HOST );

	// Read parameters

	LongOpt[] longopts = new LongOpt[20];
	// General parameters
	longopts[0] = new LongOpt("help", LongOpt.NO_ARGUMENT, null, 'h');
	longopts[1] = new LongOpt("output", LongOpt.REQUIRED_ARGUMENT, null, 'o');
	longopts[2] = new LongOpt("debug", LongOpt.OPTIONAL_ARGUMENT, null, 'd');
	longopts[3] = new LongOpt("impl", LongOpt.REQUIRED_ARGUMENT, null, 'l');
	// Is there a way for that in LongOpt ???
	longopts[4] = new LongOpt("D", LongOpt.REQUIRED_ARGUMENT, null, 'D');
	// Service parameters
	longopts[5] = new LongOpt("html", LongOpt.OPTIONAL_ARGUMENT, null, 'H');
	longopts[6] = new LongOpt("jade", LongOpt.OPTIONAL_ARGUMENT, null, 'A');
	longopts[7] = new LongOpt("wsdl", LongOpt.OPTIONAL_ARGUMENT, null, 'W');
	longopts[8] = new LongOpt("jxta", LongOpt.OPTIONAL_ARGUMENT, null, 'P');
	longopts[9] = new LongOpt("oyster", LongOpt.OPTIONAL_ARGUMENT, null, 'O');
	longopts[10] = new LongOpt("uddi", LongOpt.OPTIONAL_ARGUMENT, null, 'U');
	// DBMS Server parameters
	longopts[11] = new LongOpt("dbmshost", LongOpt.REQUIRED_ARGUMENT, null, 'm');
	longopts[12] = new LongOpt("dbmsport", LongOpt.REQUIRED_ARGUMENT, null, 's');
	longopts[13] = new LongOpt("dbmsuser", LongOpt.REQUIRED_ARGUMENT, null, 'u');
	longopts[14] = new LongOpt("dbmspass", LongOpt.REQUIRED_ARGUMENT, null, 'p');
	longopts[15] = new LongOpt("dbmsbase", LongOpt.REQUIRED_ARGUMENT, null, 'b');
	longopts[16] = new LongOpt("dbms", LongOpt.REQUIRED_ARGUMENT, null, 'B');
	longopts[17] = new LongOpt("host", LongOpt.REQUIRED_ARGUMENT, null, 'S');
	longopts[18] = new LongOpt("serv", LongOpt.REQUIRED_ARGUMENT, null, 'i');
	longopts[19] = new LongOpt("uriprefix", LongOpt.REQUIRED_ARGUMENT, null, 'f');

	Getopt g = new Getopt("", args, "ho:S:l:f:d::D:H::A::W::P::O::U::m:s:u:p:b:B:i:", longopts);
	int c;
	String arg;

	while ((c = g.getopt()) != -1) {
	    switch (c) {
	    case 'h' :
		usage();
		System.exit(0);
		break;
	    case 'o' :
		/* Use filename instead of stdout */
		outfile = g.getOptarg();
		break;
	    case 'l' :
		/* Use the given file as a database image to load */
		filename = g.getOptarg();
		break;
	    case 'd' :
		/* DEPRECATED: Debug level  */
		arg = g.getOptarg();
		System.err.println( "WARNING: debug argument is deprecated, use logging" );
		System.err.println( "See http://alignapi.gforge.inria.fr/logging.html" );
		break;
	    case 'i' :
		/* external service */
		arg = g.getOptarg();
		try {
		    services.put( arg, (AlignmentServiceProfile)loadInstance( arg ) );
		} catch (Exception ex) {
		    logger.warn( "Cannot create service for {}", arg );
		    logger.trace( "IGNORED Exception", ex );
		}
		break;
 	    case 'f' :
 		/* Parameter definition */
 		params.setProperty( "prefix", g.getOptarg() );
 		break;
	    case 'H' :
		/* HTTP Server + port */
		arg = g.getOptarg();
		if ( arg != null ) {
		    params.setProperty( "http", arg );
		} else {
		    params.setProperty( "http", HTML );
		}
		// This shows that it does not work
		try {
		    services.put( "fr.inrialpes.exmo.align.service.HTMLAServProfile", (AlignmentServiceProfile)loadInstance( "fr.inrialpes.exmo.align.service.HTMLAServProfile" ) );
		} catch (Exception ex) {
		    logger.warn( "Cannot create service for HTMLAServProfile", ex );
		}
		break;
	    case 'A' :
		/* JADE Server + port */
		arg = g.getOptarg();
		if ( arg != null ) {
		    params.setProperty( "jade", arg );
		} else {
		    params.setProperty( "jade", JADE );
		}		    
		try {
		    services.put( "fr.inrialpes.exmo.align.service.jade.JadeFIPAAServProfile", (AlignmentServiceProfile)loadInstance( "fr.inrialpes.exmo.align.service.jade.JadeFIPAAServProfile" ) );
		} catch (Exception ex) {
		    logger.warn("Cannot create service for JadeFIPAAServProfile", ex);
		}
		break;
	    case 'W' :
		/* Web service + port */
		arg = g.getOptarg();
		if ( arg != null ) {
		    params.setProperty( "wsdl", arg );
		} else {
		    params.setProperty( "wsdl", WSDL );
		};
		// The WSDL extension requires HTTP server (and the same one).
		// Put the default port, may be overriden
		if ( params.getProperty( "http" ) == null )
		    params.setProperty( "http", HTML );
		try {
		    services.put( "fr.inrialpes.exmo.align.service.HTMLAServProfile", (AlignmentServiceProfile)loadInstance( "fr.inrialpes.exmo.align.service.HTMLAServProfile" ) );
		} catch (Exception ex) {
		    logger.warn("Cannot create service for Web services", ex);
		}
		break;
	    case 'P' :
		/* JXTA Server + port */
		arg = g.getOptarg();
		if ( arg != null ) {
		    params.setProperty( "jxta", arg );
		} else {
		    params.setProperty( "jxta", JXTA );
		}		    
		break;
	    case 'S' :
		/* Server */
		params.setProperty( "host", g.getOptarg() );
		break;
	    case 'O' :
		/* [JE: Currently not working]: Oyster directory + port */
		arg = g.getOptarg();
		if ( arg != null ) {
		    params.setProperty( "oyster", arg );
		} else {
		    params.setProperty( "oyster", JADE );
		}
		try {
		    directories.put( "fr.inrialpes.exmo.align.service.OysterDirectory", (Directory)loadInstance( "fr.inrialpes.exmo.align.service.OysterDirectory" ) );
		} catch (Exception ex) {
		    logger.warn("Cannot create directory for Oyster", ex);
		}
		break;
	    case 'U' :
		/* [JE: Currently not working]: UDDI directory + port */
		arg = g.getOptarg();
		if ( arg != null ) {
		    params.setProperty( "uddi", arg );
		} else {
		    params.setProperty( "uddi", JADE );
		}		    
		try {
		    directories.put( "fr.inrialpes.exmo.align.service.UDDIDirectory", (Directory)loadInstance( "fr.inrialpes.exmo.align.service.UDDIDirectory" ) );
		} catch (Exception ex) {
		    logger.warn("Cannot create directory for UDDI", ex);
		}
		break;
	    case 'm' :
		DBHOST = g.getOptarg();
		break;
	    case 's' :
		DBPORT = g.getOptarg();
		break;
	    case 'u' :
		DBUSER = g.getOptarg();
		break;
	    case 'p' :
		DBPASS = g.getOptarg();
		break;
	    case 'b' :
		DBBASE = g.getOptarg();
		break;
	    case 'B' :
		arg   = g.getOptarg();
		if ( arg != null ) {
		    params.setProperty( "DBMS", arg );
		    DBMS = arg;
		} else {
		    params.setProperty( "DBMS", "mysql" );
		    DBMS = "mysql";
		}
		break;
	    case 'D' :
		/* Parameter definition */
		arg = g.getOptarg();
		int index = arg.indexOf('=');
		if ( index != -1 ) {
		    params.setProperty( arg.substring( 0, index), 
					 arg.substring(index+1));
		} else {
		    logger.warn("Bad parameter syntax: "+g);
		    usage();
		    System.exit(0);
		    
		}
		break;
	    }
	}
	
	return params;
    }

    // Really missing:
    // OUTPUT(o): what for, there is no output (maybe LOGS)
    // LOAD(l): good idea, load from file, but what kind? sql?
    // PARAMS(p is taken, P is taken): yes good as well to read parameters from file
    public void usage() {
	System.err.println("usage: AlignmentService [options]");
	System.err.println("options are:");
	//System.err.println("\t--load=filename -l filename\t\tInitialize the Service with the content of this ");
	System.err.println("\t--html[=port] -H[port]\t\t\tLaunch HTTP service");
	System.err.println("\t--jade[=port] -A[port]\t\t\tLaunch Agent service");
	System.err.println("\t--wsdl[=port] -W[port]\t\t\tLaunch Web service");
	System.err.println("\t--jxta[=port] -P[port]\t\t\tLaunch P2P service");
	System.err.println("\t--oyster -O\t\t\tRegister to Oyster directory");
	//System.err.println("\t--uddi -U\t\t\tRegister to Oyster directory");
	System.err.println("\t--serv=class -i class\t\t\tLaunch service corresponding to fully qualified classname");
	//System.err.println("\t--params=filename -p filename\tReads parameters from filename");
	System.err.println("\t--output=filename -o filename\tRedirect output to filename");
	System.err.println("\t--dbmshost=host -m host\t\t\tUse DBMS host");
	System.err.println("\t--dbmsport=port -s port\t\t\tUse DBMS port");
	System.err.println("\t--dbmsuser=name -u name\t\t\tUse DBMS user name");
	System.err.println("\t--dbmspass=pwd -p pwd\t\t\tUse DBMS password");
	System.err.println("\t--dbmsbase=name -b name\t\t\tUse Database name");
	System.err.println("\t--dbms=name -B name\t\t\tUse Database Management System");
	System.err.println("\t--uriprefix=uri -f uri\t\t\tSet alignment URIs with this prefix");
	System.err.println("\t-Dparam=value\t\t\tSet parameter");
	System.err.println("\t--help -h\t\t\tPrint this message");

	System.err.print("\n"+AlignmentService.class.getPackage().getImplementationTitle()+" "+AlignmentService.class.getPackage().getImplementationVersion());
	System.err.println(" ($Id: AlignmentService.java 1841 2013-03-24 17:28:33Z euzenat $)\n");
    }
    
}
