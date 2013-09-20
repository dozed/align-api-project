/*
 * $Id: InstanceId.java 1710 2012-03-23 19:53:25Z euzenat $
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.4 - 2006 -- then InstanceExpr
 * Copyright (C) INRIA, 2009-2010, 2012
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.impl.edoal;

import java.net.URI;
import java.net.URISyntaxException;

import org.semanticweb.owl.align.AlignmentException;

import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor;
import fr.inrialpes.exmo.align.parser.TypeCheckingVisitor.TYPE;

/**
 * A simple Id to represent a Instance.
 * 
 */

public class InstanceId extends InstanceExpression implements Id {

    private URI uri;

    public URI getURI(){
	return uri;
    }

    public void setURI( URI u ){
	uri = u;
    }

    /**
     * Creates an anonymous instance pattern
     */
    public InstanceId() {}

    /**
     * Constructs an InstanceId.
     * 
     * @param u
     *            an URI of this instance
     * @throws NullPointerException
     *             u is null
     */
    public InstanceId( final URI u ) {
	if ( u == null ) {
	    throw new NullPointerException("The URI must not be null");
	}
	this.uri = u;
    }
    
    public void accept( EDOALVisitor visitor ) throws AlignmentException {
	visitor.visit( this );
    }
    public TYPE accept( TypeCheckingVisitor visitor  ) throws AlignmentException {
	return visitor.visit(this);
    }

    /**
     * Returns the Id.
     * 
     * @return the id.
     */
    public String plainText() {
	return toString();
    }
    
    
    /**
     * <p>
     * Returns a simple description of this object. <b>The format of the
     * returned String is undocumented and subject to change.</b>
     * <p>
     * <p>
     * An expamle return String could be:
     * <code>instanceId: http://my/super/instance</code>
     * </p>
     */
    public String toString() {
	return "instanceId: " + uri;
    }

    /*    
    public Object clone() {
	return super.clone();
    }
    */
}
