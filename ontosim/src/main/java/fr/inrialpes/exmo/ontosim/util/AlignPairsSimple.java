/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   AlignPairsSimple.java is part of OntoSim.
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

import java.util.ArrayList;
import java.util.Hashtable;

import org.semanticweb.owl.model.OWLEntity;

import ca.uqam.info.latece.sboa.inter.selection.AlignPairs;
import fr.inrialpes.exmo.ontosim.util.matrix.MatrixDouble;

public class AlignPairsSimple implements AlignPairs {


	protected MatrixDouble<OWLEntity,OWLEntity> entEntSimMap = new MatrixDouble<OWLEntity, OWLEntity>();

	public void addPair(OWLEntity arg0, OWLEntity arg1, double arg2) {
		if (entEntSimMap.containsRdim(arg1) || entEntSimMap.containsCdim(arg0) ) { 
		    entEntSimMap.put(arg1, arg0, arg2);
		    return;
		}
		entEntSimMap.put(arg0, arg1, arg2);
	}

	public void rm(OWLEntity e1) {
	    //entEntSimMap.remove(e1);
	}


	public double getSimPair(OWLEntity e1, OWLEntity e2) {
	    	if (entEntSimMap.containsKey(e2,e1)) {
	    	    return entEntSimMap.get(e2, e1);
		}
	    	if (entEntSimMap.containsKey(e1,e2)) {
	    	    return entEntSimMap.get(e1, e2);
	    	}
	    	return 0;
	}


	public Hashtable<ArrayList<OWLEntity>, Double> hungarianSelect(boolean arg0) {
		// TODO Auto-generated method stub
		return null;
	}

	public Hashtable<ArrayList<OWLEntity>, Double> maxSelect(boolean arg0,
			boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

	public Hashtable<ArrayList<OWLEntity>, Double> thresholdSelect(double arg0,
			boolean arg1) {
		// TODO Auto-generated method stub
		return null;
	}

}
