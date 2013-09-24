/**
 *   Copyright 2010-2011 INRIA, Université Pierre Mendès France
 *   
 *   HLClassImpl.java is part of OntoSim.
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
import java.lang.ref.WeakReference;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public class HLClassImpl<E> extends HLEntityImpl<E> implements HLClass<E> {

    protected HLClassImpl(HeavyLoadedOntology<E> ont, E e) {
	super(ont, e);
	// TODO Auto-generated constructor stub
    }

    private List<Reference<Set<HLClass<E>>>> subclasses;
    private List<Reference<Set<HLClass<E>>>> superclasses;
    private List<Reference<Set<HLProperty<E>>>> properties;
    private List<Reference<Set<HLProperty<E>>>> dataproperties;
    private List<Reference<Set<HLProperty<E>>>> objectproperties;
    private List<Reference<Set<HLIndividual<E>>>> instances;
    
    
    /* Class methods */
    @SuppressWarnings("unchecked")
    public Set<HLClass<E>> getSubClasses(int local, int asserted, int named ) {
	if (subclasses==null) subclasses=new Vector<Reference<Set<HLClass<E>>>>();
	int idx = getIdx(local,asserted,named);
	while (subclasses.size()<=idx) subclasses.add(null);
	if (subclasses.get(idx)==null || subclasses.get(idx).get()==null) {
		Set s = onto.getSubClasses(getObject(), local, asserted, named);
		subclasses.set(idx, new WeakReference<Set<HLClass<E>>>((Set<HLClass<E>>)fact.getFrom(s)));
	}
	return subclasses.get(idx).get();
    }
    
    
    @SuppressWarnings("unchecked")
    public Set<HLClass<E>> getSuperClasses(int local, int asserted, int named ) {
	if (superclasses==null) superclasses=new Vector<Reference<Set<HLClass<E>>>>();
	int idx = getIdx(local,asserted,named);
	while (superclasses.size()<=idx) superclasses.add(null);
	if (superclasses.get(idx)==null || superclasses.get(idx).get()==null) 
	    try {
		superclasses.set(idx, new WeakReference<Set<HLClass<E>>>((Set<HLClass<E>>) fact.getFrom((Set<E>)onto.getSuperClasses(getObject(), local, asserted, named))));
	    } catch ( OntowrapException owex ) { owex.printStackTrace(); }		
	return superclasses.get(idx).get();
    }
    
    @SuppressWarnings("unchecked")
    public Set<HLProperty<E>> getProperties(int local, int asserted, int named ){
	if (properties==null) properties=new Vector<Reference<Set<HLProperty<E>>>>();
	int idx = getIdx(local,asserted,named);
	while (properties.size()<=idx) properties.add(null);
	if (properties.get(idx)==null  || properties.get(idx).get()==null) 
	    try {
		properties.set(idx, new WeakReference<Set<HLProperty<E>>>((Set<HLProperty<E>>)fact.getFrom((Set<E>)onto.getProperties(getObject(), local, asserted, named))));
	    } catch ( OntowrapException owex ) { owex.printStackTrace(); }
	return properties.get(idx).get();
    }
    
    @SuppressWarnings("unchecked")
    public Set<HLProperty<E>> getDataProperties(int local, int asserted, int named ) {
	if (dataproperties==null) dataproperties=new Vector<Reference<Set<HLProperty<E>>>>();
	int idx = getIdx(local,asserted,named);
	while (dataproperties.size()<=idx) dataproperties.add(null);
	if (dataproperties.get(idx)==null  || dataproperties.get(idx).get()==null) 
	    try {
		dataproperties.set(idx, new WeakReference<Set<HLProperty<E>>>((Set<HLProperty<E>>)fact.getFrom((Set<E>)onto.getDataProperties(getObject(), local, asserted, named))));
	    } catch ( OntowrapException owex ) { owex.printStackTrace(); }
	return dataproperties.get(idx).get();
    }
    
    @SuppressWarnings("unchecked")
    public Set<HLProperty<E>> getObjectProperties(int local, int asserted, int named ) {
	if (objectproperties==null) objectproperties=new Vector<Reference<Set<HLProperty<E>>>>();
	int idx = getIdx(local,asserted,named);
	while (objectproperties.size()<=idx) objectproperties.add(null);
	if (objectproperties.get(idx)==null  || objectproperties.get(idx).get()==null) 
	    try{
		objectproperties.set(idx, new WeakReference<Set<HLProperty<E>>>((Set<HLProperty<E>>)fact.getFrom((Set<E>)onto.getObjectProperties(getObject(), local, asserted, named))));
	    } catch ( OntowrapException owex ) { owex.printStackTrace(); }
	return objectproperties.get(idx).get();
    }
    
    
    @SuppressWarnings("unchecked")
    public Set<HLIndividual<E>> getInstances(int local, int asserted, int named) {
	if (instances==null) instances=new Vector<Reference<Set<HLIndividual<E>>>>();
	int idx = getIdx(local,asserted,named);
	while (instances.size()<=idx) instances.add(null);
	if (instances.get(idx)==null  || instances.get(idx).get()==null)
	    try {
		instances.set(idx, new WeakReference<Set<HLIndividual<E>>>((Set<HLIndividual<E>>)fact.getFrom((Set<E>)onto.getInstances(getObject(), local, asserted, named))));
	    } catch ( OntowrapException owex ) { owex.printStackTrace(); }
	return instances.get(idx).get();
    }
}

