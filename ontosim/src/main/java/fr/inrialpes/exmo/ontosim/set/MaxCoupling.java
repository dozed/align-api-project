/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   MaxCoupling.java is part of OntoSim.
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
package fr.inrialpes.exmo.ontosim.set;

import java.util.Set;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.util.measures.MalphaMeasure;


public class MaxCoupling<S> extends SetMeasure<S> {

	MalphaMeasure<S> malpha;

	public MaxCoupling(Measure<S> m) {
	    this(m,Double.POSITIVE_INFINITY);
	    
	}

	public MaxCoupling(Measure<S> m, double alpha) {
		super(m,new fr.inrialpes.exmo.ontosim.extractor.MaxCoupling());
		malpha=new MalphaMeasure<S>(alpha);
	}
	
	
	public double getMeasureValue(Set<? extends S> o1, Set<? extends S> o2) {
	    return (super.getMeasureValue(o1, o2)*Math.min(o1.size(), o2.size()))/malpha.getMeasureValue(o1, o2);
	}
	
	public double getDissim(Set<? extends S> o1, Set<? extends S> o2) {
	    return (super.getDissim(o1, o2)*Math.min(o1.size(), o2.size()))/malpha.getMeasureValue(o1, o2);
	}
	
	public double getSim(Set<? extends S> o1, Set<? extends S> o2) {
	    return (super.getSim(o1, o2)*Math.min(o1.size(), o2.size()))/malpha.getMeasureValue(o1, o2);
	}
	
	
	/*

	public  double getMeasureValue(Set<? extends S> o1, Set<? extends S> o2) {
		if (o1.size() > o2.size()) {
			return getMeasureValue(o2,o1);
		}
		if ((o1.size()==0)||(o2.size()==0))
		    return 0;
		double[][] matrix = new double[o1.size()][o2.size()];

		Collection<S> o2Elems;
		if (!(o2 instanceof LinkedHashSet<?>)) {
		    o2Elems = new ArrayList<S>(o2.size());
		    o2Elems.addAll(o2);
		}
		else {
		    o2Elems= (Set<S>) o2;
		}

		Iterator<? extends S> o1It = o1.iterator();
		for (int i=0 ; i<matrix.length ; i++) {
		    Iterator<S> o2It = o2Elems.iterator();
		    S o1Elem = o1It.next();
		    for (int j=0 ; j <matrix[i].length ; j++) {
			matrix[i][j]=localMeasure.getMeasureValue(o1Elem,  o2It.next());
		    }
		}


		int[][] assignment;
		String method;
		if (localMeasure.getMType() == TYPES.similarity)
			method="max";
		else
			method="min";

		assignment = HungarianAlgorithm.hgAlgorithm(matrix,method);	//Call Hungarian algorithm.
		double sum=0;
		for (int i=0; i<assignment.length; i++) {
			sum = sum + matrix[assignment[i][0]][assignment[i][1]];
		}
		return sum/malpha.getMeasureValue(o1, o2);
	}

	public double getDissim(Set<? extends S> o1, Set<? extends S> o2) {
		if (getMType() == TYPES.similarity)
			return 1-getMeasureValue(o1,o2);
		else if (getMType() == TYPES.dissimilarity)
			return getMeasureValue(o1,o2);
		//throw new MeasureNotDefinedException("Local Measure is neither a similarity nor a dissimilarity");
		return Double.NaN;
	}

	public double getSim(Set<? extends S> o1, Set<? extends S> o2) {
		if (getMType() == TYPES.dissimilarity)
			return 1-getMeasureValue(o1,o2);
		else if (getMType() == TYPES.similarity)
			return getMeasureValue(o1,o2);
		return Double.NaN;
	}*/


}
