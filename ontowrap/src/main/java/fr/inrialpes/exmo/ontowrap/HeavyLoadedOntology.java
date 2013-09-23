/*
 * $Id: HeavyLoadedOntology.java 1681 2012-02-16 10:11:59Z euzenat $
 *
 * Copyright (C) INRIA, 2008, 2010, 2012
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

package fr.inrialpes.exmo.ontowrap;

import java.util.Set;

/**
 * Encapsulate deep access to an ontology through some Ontology API
 *
 * Asserted methods corresponds to the information explicitely given or stored about the entity.
 * Non-asserted corresponds to the information that can be deduced from it.
 * So asserted methods are related to a syntactic view while the others are related to the semantics
 */
public interface HeavyLoadedOntology<O> extends LoadedOntology<O> {

    /* Capability methods */
    public boolean getCapabilities( int Direct, int Asserted, int Named ) throws OntowrapException;

    /* Class methods */
    public Set<? extends Object> getSubClasses( Object c, int local, int asserted, int named );
    public Set<? extends Object> getSuperClasses( Object c, int local, int asserted, int named ) throws OntowrapException;
    public Set<? extends Object> getProperties( Object c, int local, int asserted, int named ) throws OntowrapException;
    public Set<? extends Object> getDataProperties( Object c, int local, int asserted, int named ) throws OntowrapException;
    public Set<? extends Object> getObjectProperties( Object c, int local, int asserted, int named ) throws OntowrapException;
    public Set<? extends Object> getInstances( Object c, int local, int asserted, int named  ) throws OntowrapException;

    /* Property methods */
    public Set<? extends Object> getSubProperties( Object p, int local, int asserted, int named ) throws OntowrapException;
    public Set<? extends Object> getSuperProperties( Object p, int local, int asserted, int named ) throws OntowrapException;
    public Set<? extends Object> getRange( Object p, int asserted ) throws OntowrapException;
    public Set<? extends Object> getDomain( Object p, int asserted ) throws OntowrapException;

    /* Individual methods */
    public Set<? extends Object> getClasses( Object i, int local, int asserted, int named ) throws OntowrapException;

}
