/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   SalphaMeasure.java is part of OntoSim.
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
import fr.inrialpes.exmo.ontosim.aggregation.AggregationScheme;
import fr.inrialpes.exmo.ontosim.extractor.Extractor;
import fr.inrialpes.exmo.ontosim.util.measures.MalphaMeasure;

// A supprimer
@Deprecated
public class SalphaMeasure<S> extends SetMeasure<S> {

	public SalphaMeasure(Measure<S> lm, Extractor e, AggregationScheme as) {
	super(lm, e, as);
	// TODO Auto-generated constructor stub
    }


	private MalphaMeasure<S> malpha;

	public SalphaMeasure(Measure<S> localMeasure, double alpha) {
		super(localMeasure,null,null);
		malpha=new MalphaMeasure<S>(alpha);
	}


	public double getDissim(Set<? extends S> s1, Set<? extends S> s2) {
		return 1-getMeasureValue(s1,s2);
	}

	public double getMeasureValue(Set<? extends S> s1, Set<? extends S> s2) {
		double sum=0;
		for (S o1 : s1 ) {
			for (S o2 : s2) {
				sum += localMeasure.getSim(o1, o2);
			}
		}
		return sum/malpha.getMeasureValue(s1,s2);
	}


	public double getSim(Set<? extends S> s1, Set<? extends S> s2) {
		return getMeasureValue(s1,s2);
	}

}
