/**
 *   Copyright 2010-2011 INRIA, Université Pierre Mendès France
 *   
 *   HLIndividualImpl.java is part of OntoSim.
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
import java.util.List;
import java.util.Set;
import java.util.Vector;

import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public class HLIndividualImpl<E> extends HLEntityImpl<E> implements HLIndividual<E> {

    protected HLIndividualImpl(HeavyLoadedOntology<E> ont, E e) {
	super(ont, e);
	// TODO Auto-generated constructor stub
    }

    private List<Reference<Set<HLClass<E>>>> classes;
    
    /* Individual methods */
    @SuppressWarnings("unchecked")
    public Set<HLClass<E>> getClasses(int local, int asserted, int named ) {
	if (classes==null) classes=new Vector<Reference<Set<HLClass<E>>>>();
	int idx = getIdx(local,asserted,named);
	while (classes.size()<=idx) classes.add(null);
	if (classes.get(idx)==null  || classes.get(idx).get()==null) 
	    try {
		classes.set(idx, new SoftReference<Set<HLClass<E>>>((Set<HLClass<E>>)fact.getFrom((Set<E>)onto.getClasses(getObject(), local, asserted, named))));
	    } catch ( OntowrapException owex ) { owex.printStackTrace(); }
	return classes.get(idx).get();
    }
}
