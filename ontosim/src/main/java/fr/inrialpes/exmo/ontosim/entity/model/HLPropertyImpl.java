/**
 *   Copyright 2010-2011 INRIA, Université Pierre Mendès France
 *   
 *   HLPropertyImpl.java is part of OntoSim.
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

public class HLPropertyImpl<E> extends HLEntityImpl<E> implements HLProperty<E>{

    protected HLPropertyImpl(HeavyLoadedOntology<E> ont, E e) {
	super(ont, e);
	// TODO Auto-generated constructor stub
    }
    private List<Reference<Set<HLProperty<E>>>> subproperties;
    private List<Reference<Set<HLProperty<E>>>> superproperties;
    private List<Reference<Set<HLClass<E>>>> ranges;
    private List<Reference<Set<HLClass<E>>>> domains;
    
    /* Property methods */
    @SuppressWarnings("unchecked")
    public Set<HLProperty<E>> getSubProperties(int local, int asserted, int named ) {
	if (subproperties==null) subproperties=new Vector<Reference<Set<HLProperty<E>>>>();
	int idx = getIdx(local,asserted,named);
	while (subproperties.size()<=idx) subproperties.add(null);
	if (subproperties.get(idx)==null  || subproperties.get(idx).get()==null) 
	    try {
		subproperties.set(idx, new SoftReference<Set<HLProperty<E>>>((Set<HLProperty<E>>)fact.getFrom((Set<E>)onto.getSubProperties(getObject(), local, asserted, named))));
	    } catch ( OntowrapException owex ) { owex.printStackTrace(); }
	return subproperties.get(idx).get();
    }
    
    @SuppressWarnings("unchecked")
    public Set<HLProperty<E>> getSuperProperties(int local, int asserted, int named ) {
	if (superproperties==null) superproperties=new Vector<Reference<Set<HLProperty<E>>>>();
	int idx = getIdx(local,asserted,named);
	while (superproperties.size()<=idx) superproperties.add(null);
	if (superproperties.get(idx)==null  || superproperties.get(idx).get()==null)
	    try {
		superproperties.set(idx, new SoftReference<Set<HLProperty<E>>>((Set<HLProperty<E>>)fact.getFrom((Set<E>)onto.getSuperProperties(getObject(), local, asserted, named))));
	    } catch ( OntowrapException owex ) { owex.printStackTrace(); }
	return superproperties.get(idx).get();
    }
    
    @SuppressWarnings("unchecked")
    public Set<HLClass<E>> getRange(int asserted) {
	if (ranges==null) ranges=new Vector<Reference<Set<HLClass<E>>>>();
	int idx = getIdx(0,asserted,0);
	while (ranges.size()<=idx) ranges.add(null);
	if (ranges.get(idx)==null  || ranges.get(idx).get()==null) 
	    try {
		ranges.set(idx, new SoftReference<Set<HLClass<E>>>((Set<HLClass<E>>)fact.getFrom((Set<E>)onto.getRange(getObject(), asserted))));
	    } catch ( OntowrapException owex ) { owex.printStackTrace(); }
	return ranges.get(idx).get();
    }
    
    @SuppressWarnings("unchecked")
    public Set<HLClass<E>> getDomain(int asserted) {
	if (domains==null) domains=new Vector<Reference<Set<HLClass<E>>>>();
	int idx = getIdx(0,asserted,0);
	while (domains.size()<=idx) domains.add(null);
	if (domains.get(idx)==null  || domains.get(idx).get()==null) 
	    try {
		domains.set(idx, new SoftReference<Set<HLClass<E>>>((Set<HLClass<E>>)fact.getFrom((Set<E>)onto.getDomain(getObject(), asserted))));
	    } catch ( OntowrapException owex ) { owex.printStackTrace(); }
	return domains.get(idx).get();
    }
}
