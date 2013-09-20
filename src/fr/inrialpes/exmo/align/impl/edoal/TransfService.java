/*
 * $Id: TransfService.java 1710 2012-03-23 19:53:25Z euzenat $
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.2 - 2006
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

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.net.URI;

/**
 * <p>
 * Represents a tranformation service to transform the values of attributes.
 * </p>
 * <p>
 * In the specification a {@code transf} is a local transformaion
 * function/service. The {@code pov} specifies the parameters needed for the
 * function to compute the transformaion.
 * </p>
 * <p>
 * In the specification a {@code service} is a remote transformaion
 * function/service. The {@code id} distiguishes between multiple transformation
 * function at the given address. The {@code pov} specifies the parameters
 * needed for the function to compute the transformaion.
 * </p>
 * 
 * @version $Id: TransfService.java 1710 2012-03-23 19:53:25Z euzenat $
 */

public class TransfService implements Cloneable {
    /** resource (uri) to the service */
    private URI res;

    /** id of the transformation function (only used if it is a service) */
    private URI id;

    /** paramteters for the transformation */
    private Set<Id> params;
    
    /**
     * <p>
     * Constructs a transf.
     * </p>
     * 
     * @param res
     *            uri to the service
     * @param params
     *            parameters for the transformation
     * @throws NullPointerException
     *             if the res is {@code null}
     */
    public TransfService(final URI res, final Collection<Id> params) {
	this(res, null, params);
    }

    /**
     * <p>
     * Constructs a service.
     * </p>
     * 
     * @param res
     *            uri to the service
     * @param id
     *            id of the transformation function
     * @param params
     *            parameters for the transformation
     * @throws NullPointerException
     *             if the res is {@code null}
     */
    @SuppressWarnings( "unchecked" )
	public TransfService(final URI res, final URI id,
			     final Collection<Id> params) {
	if (res == null) {
	    throw new NullPointerException("The resource must not be null");
	}
	this.res = res;
	this.id = id;
	if (params == null) {
	    // The unchecked would be solved by creating an empty hashset
	    this.params = (Set<Id>)Collections.EMPTY_SET;//[W:uncheked]
	} else {
	    this.params = new HashSet<Id>(params);
	    this.params.remove(null);
	}
    }

    /**
     * Returns the resource / uri to the transformation.
     * 
     * @return the uri to the transformator
     */
    public URI getRes() {
	return res;
    }

    /**
     * Returns the id of the transformation function. This function only returns
     * a usable value (another value than {@code null}) if it is a service.
     * 
     * @return the id of the function
     * @see #hasId()
     */
    public URI getId() {
	return id;
    }

    /**
     * Returns an unmodifiable set of parameters needed for the transformation.
     * 
     * @return the set of parameters
     */
    public Set<Id> getParameters() {
	return Collections.unmodifiableSet(params);
    }

    /**
     * Returns whether there is a id.
     * 
     * @return {@code true} if there is a usable id, otherwise {@code false}
     * @see #getId()
     */
    public boolean hasId() {
	return id != null;
    }

    /**
     * <p>
     * Returns a short string description of this object. <b>The format of the
     * returned string is undocumented and subject to change.</b>
     * </p>
     * <p>
     * An example string could be:
     * {@code transf: http://my/super/transf params: [dollar]}
     * </p>
     */
    public String toString() {
	return "transf: " + res + ((id != null) ? " id: " + id : "")
	    + " params: " + params;
    }
    
    public boolean equals(final Object o) {
	if (o == this) {
	    return true;
	}
	if (!(o instanceof TransfService)) {
	    return false;
	}
	TransfService t = (TransfService) o;
	return res.equals(t.res)
	    && ((id == t.id) || ((id != null) && id.equals(t.id)))
	    && (params.size() == t.params.size())
	    && params.containsAll(t.params);
    }
    
    public int hashCode() {
	int hash = 17;
	hash = hash * 37 + res.hashCode();
	hash = hash * 37 + ((id != null) ? id.hashCode() : 0);
	hash = hash * 37 + params.hashCode();
	return hash;
    }

    @SuppressWarnings( "unchecked" )
	public Object clone() {
	try {
	    TransfService clone = (TransfService) super.clone();
	    // JE: noclone on URI
	    //clone.res = (URI) res.clone();
	    clone.res = res;
	    clone.params = (params.isEmpty()) ? Collections.EMPTY_SET //[W:unchecked]
		: new HashSet<Id>(params);
	    // JE: noclone on URI
	    //clone.id = (id == null) ? null : (URI) id.clone();
	    clone.id = (id == null) ? null : id;
	    return clone;
	} catch (CloneNotSupportedException e) {
	    assert true : "Object is always cloneable";
	}
	return null;
    }
}
