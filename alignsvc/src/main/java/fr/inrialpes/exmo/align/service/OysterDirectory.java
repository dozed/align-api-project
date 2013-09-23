/*
 * $Id: OysterDirectory.java 1831 2013-03-09 18:58:49Z euzenat $
 *
 * Copyright (C) INRIA, 2007-2011, 2013
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

import java.lang.Double;
import java.util.Properties;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.impl.Annotations;
import fr.inrialpes.exmo.align.impl.Namespace;

import fr.inrialpes.exmo.ontowrap.Ontology;

import org.neon_toolkit.registry.api.Oyster2Manager;
import org.neon_toolkit.registry.api.Oyster2Connection;

import org.neon_toolkit.omv.api.core.OMVOntology;
import org.neon_toolkit.omv.api.extensions.mapping.OMVMapping;
import org.neon_toolkit.omv.api.extensions.mapping.OMVMappingMethod;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class OysterDirectory implements Directory {
    final static Logger logger = LoggerFactory.getLogger( OysterDirectory.class );

    private Oyster2Connection oyster2Conn = null;

    /**
     * Create a connection and/or registration to a directory
     * Parameters can contain, e.g.:
     * - the directory address
     * - the declaration of the current service
     */
    public void open( Properties p ) throws AServException {
	logger.debug("Attempt to connect to Oyster" );
	//oyster2Conn = Oyster2Manager.newConnection("new store","lib/kaon2.jar","totokaon","");
	oyster2Conn = Oyster2Manager.newConnection(false);
	logger.info("Successfully connectered to "+ oyster2Conn );
    }

    /**
     * Register an alignment to the directory (if necessary)
     */
    public void register( Alignment al ) throws AServException {
	OMVOntology onto1 = new OMVOntology();
	OMVOntology onto2 = new OMVOntology();
	try {
	    onto1.setURI( al.getOntology1URI().toString() );
	    onto2.setURI( al.getOntology2URI().toString() );
	} catch (AlignmentException e) { 
	    throw new AServException( "Cannot get ontology URI", e );
	}
	oyster2Conn.replace( onto1 ); // or use submit?
	oyster2Conn.replace( onto2 );
	OMVMapping align = new OMVMapping();
	align.setURI( al.getExtension( Namespace.ALIGNMENT.uri, Annotations.ID ) );
	align.setLevel( al.getLevel() );
	align.setType( al.getType() );
	if ( al.getExtension( Namespace.ALIGNMENT.uri, Annotations.TIME ) != null ) 
	    align.setProcessingTime( new Double( al.getExtension( Namespace.ALIGNMENT.uri, Annotations.TIME ) ) );
	align.setHasSourceOntology( onto1 );
	align.setHasTargetOntology( onto2 );
	if ( al.getExtension( Namespace.ALIGNMENT.uri, Annotations.METHOD ) != null ) {
	    OMVMappingMethod meth = new OMVMappingMethod();
	    meth.setID( al.getExtension( Namespace.ALIGNMENT.uri, Annotations.METHOD ) );
	    align.setUsedMethod( meth );
	    oyster2Conn.replace( meth );
	}
	oyster2Conn.replace( align );
    }

    // Certainly return 
    public void getAlignmentReferences( Ontology o1, Ontology o2 ){
    }

    /**
     * unregister an alignment from the directory (if necessary)
     * This is not implemented
     */
    public void unregister( Alignment al ) {
    }

    /**
     * Shutdown the connection and/or registration to the directory
     */
    public void close() throws AServException {
	Oyster2Manager.closeConnection();
	logger.info("Successfully disconnected to Oyster");
    }
}
