/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   URI2Triples.java is part of OntoSim.
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
package fr.inrialpes.exmo.ontosim.util;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Set;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

public class URI2Triples {

    public LinkedHashMap<Node,Set<Triple>[]> uri2triples = new LinkedHashMap<Node, Set<Triple>[]>();

    public URI2Triples() {
    }

    // Cannot create an array of generic
    @SuppressWarnings("unchecked")
    public Set<Triple>[] getTripleLists(Node obj) {
	Set<Triple>[] tripleLists = uri2triples.get(obj);
	if (tripleLists==null) {
	    tripleLists = new Set[3];
	    tripleLists[0]=new HashSet<Triple>();
	    tripleLists[1]=new HashSet<Triple>();
	    tripleLists[2]=new HashSet<Triple>();
	    uri2triples.put(obj, tripleLists);
	}
	return tripleLists;

    }

    public void addTripleSubject(Node obj, Triple t) {
	getTripleLists(obj)[0].add(t);
    }

    public void addTriplePredicate(Node obj, Triple t) {
	getTripleLists(obj)[1].add(t);
    }

    public void addTripleObject(Node obj, Triple t) {
	getTripleLists(obj)[2].add(t);
    }


}
