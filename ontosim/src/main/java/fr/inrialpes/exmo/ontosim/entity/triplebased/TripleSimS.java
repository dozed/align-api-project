/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   TripleSimS.java is part of OntoSim.
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
package fr.inrialpes.exmo.ontosim.entity.triplebased;

import com.hp.hpl.jena.graph.Node;
import com.hp.hpl.jena.graph.Triple;

import fr.inrialpes.exmo.ontosim.Measure;

public class TripleSimS implements Measure<Triple> {

    //public Measure<String> mSim;

    private Measure<Node> intialSim;

    public TripleSimS(Measure<Node> intialSim) {
	this.intialSim=intialSim;
    }

    public double getDissim(Triple o1, Triple o2) {
	return 1-getMeasureValue(o1,o2);
    }

    public TYPES getMType() {
	return TYPES.similarity;
    }

    public double getMeasureValue(Triple o1, Triple o2) {
	double simP=0;
	if (o1.getPredicate().equals(o2.getPredicate())) {
		simP =1;
	}
	else {
	    double iSim = intialSim.getSim(o1.getPredicate(),o2.getPredicate());
	    if (!Double.isNaN(iSim)) {
		simP = iSim;
	    }
	}

	if (simP==0) return 0;

	double simS=0;
	if (o1.getSubject().equals(o2.getSubject())) {
	    simS =1;
	}
	else {
	    double iSim = intialSim.getSim(o1.getSubject(),o2.getSubject());
	    if (!Double.isNaN(iSim))
		simS = iSim;
	}


	double simO=0;
	if (o1.getObject().equals(o2.getObject())) {
	    simO =1;
	}
	else {
	    double iSim = intialSim.getSim(o1.getObject(),o2.getObject());
	    if (!Double.isNaN(iSim))
		simO = iSim;
	}
	//if (simP==1)
	//return simP*(simS+simP+simO)/3;
	return (simS+simP+simO)/3;
    }	

    public double getSim(Triple o1, Triple o2) {
	return getMeasureValue(o1,o2);
    }

}
