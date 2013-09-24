/**
 *   Copyright 2010-2011 INRIA, Université Pierre Mendès France
 *   
 *   HLEntityFactory.java is part of OntoSim.
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

import java.lang.ref.Reference;
import java.lang.ref.SoftReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.WeakHashMap;

import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public final class HLEntityFactory<E> {
    
    public final static Map<HeavyLoadedOntology<?>,HLEntityFactory<?>> FACTORIES=new HashMap<HeavyLoadedOntology<?>,HLEntityFactory<?>>();
    
    private final Map<E,Reference<HLEntity<E>>> cache=new WeakHashMap<E,Reference<HLEntity<E>>>();
    private HeavyLoadedOntology<E> onto;
    
    private HLEntityFactory(HeavyLoadedOntology<E> onto){
	this.onto=onto;
    }
    
    @SuppressWarnings("unchecked")
    public static <T> HLEntityFactory<T> getInstance(HeavyLoadedOntology<T> ont) {
	HLEntityFactory<?> fact = FACTORIES.get(ont);
	if (fact==null) {
	    fact=new HLEntityFactory<T>(ont);
	    FACTORIES.put(ont, fact);
	}
	return (HLEntityFactory<T>) fact;
    }
    
    public final HLEntity<E> createHLEntity(E obj) {
	HLEntity<E> e=null;
	try {
	    if (onto.isClass(obj))
		e= new HLClassImpl<E>(onto,obj);
	    else if (onto.isProperty(obj))
		e= new HLPropertyImpl<E>(onto,obj);
	    else if (onto.isIndividual(obj))
		e= new HLIndividualImpl<E>(onto,obj);
	    if (e!=null) cache.put(obj, new SoftReference<HLEntity<E>>(e));
	} catch ( OntowrapException owex ) { owex.printStackTrace(); }
	return e;
    }
    
    protected Set<? extends HLEntity<E>> getFrom(Set<E> entities) {
	Set<HLEntity<E>> s = new HashSet<HLEntity<E>>();
	for (E e : entities) {
	    Reference<HLEntity<E>> hle = cache.get(e);
	    if (hle == null || hle.get()==null)
		s.add(createHLEntity(e));
	    else {
		s.add(hle.get());
	    }
	}
	return s;
    }

}
