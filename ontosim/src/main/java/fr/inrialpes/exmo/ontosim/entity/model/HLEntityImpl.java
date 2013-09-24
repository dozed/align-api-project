/**
 *   Copyright 2010-2011 INRIA, Université Pierre Mendès France
 *   
 *   HLEntityImpl.java is part of OntoSim.
 *
 *   OntoSim is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU Lesser General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   OntoSim is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU Lesser General Public License for more details.
 *
 *   You should have received a copy of the GNU Lesser General Public License
 *   along with OntoSim; if not, write to the Free Software
 *   Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 */

package fr.inrialpes.exmo.ontosim.entity.model;

import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public abstract class HLEntityImpl<E> extends EntityImpl<E> implements HLEntity<E> {

    //public final static Map<Object,Reference<HLEntity<?>>> ENTITY_CACHE=new WeakHashMap<Object,Reference<HLEntity<?>>>();
    protected HeavyLoadedOntology<E> onto;

    protected HLEntityFactory<E> fact;
    
    protected HLEntityImpl(HeavyLoadedOntology<E> ont, E e) {
	super(ont, e);
	onto=ont;
	fact=HLEntityFactory.getInstance(ont);
	//ENTITY_CACHE.put(e, new SoftReference<HLEntity<?>>(this));
    }
    
    /* Capability methods */
    public boolean getCapabilities( int direct, int asserted, int named ) {
	try {
	    return onto.getCapabilities(direct, asserted, named);
	} catch ( OntowrapException owex ) { return false; }
    }

   /*protected Set<? extends HLEntity<E>> getFrom(Set<E> entities) {
	Set<HLEntity<E>> s = new HashSet<HLEntity<E>>();
	for (E e : entities) {
	    Reference<HLEntity<?>> hle = ENTITY_CACHE.get(e);
	    if (hle == null || hle.get()==null)
		s.add(HLEntityFactory.createHLEntity(onto,e));
	    else {
		s.add((HLEntity<E>)hle.get());
	    }
		
	}
	return s;
    }*/
    
    protected final int getIdx(int local, int asserted, int named ) {
	int idx = 1<<local+1<<asserted+1<<named-1;
	return idx;
    }

    public HeavyLoadedOntology<E> getOntology() {
	return onto;
    }
}
