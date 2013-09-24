/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   Hausdorff.java is part of OntoSim.
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

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.aggregation.AggregationScheme;
import fr.inrialpes.exmo.ontosim.aggregation.DummyAS;

public class Hausdorff<S> extends SetMeasure<S>{



	public Hausdorff(Measure<S> m) {
		super(m, new fr.inrialpes.exmo.ontosim.extractor.Hausdorff(),AggregationScheme.getInstance(DummyAS.class));
	}

	/*private <T extends S> double[] getMins(Set<? extends S> o1, Set<T> o2) {
	    double[] res = new double[o1.size()+o2.size()];
	    int i=0;
	    Arrays.fill(res,Double.POSITIVE_INFINITY);
	    Collection<T> o2ItFix;
	    if (!(o2 instanceof LinkedHashSet<?>)) {
		o2ItFix = new ArrayList<T>(o2.size());
		o2ItFix.addAll(o2);
	    }
	    else {
		o2ItFix=o2;
	    }
	    for (S e1 : o1) {
		int j=o1.size();
		for (S e2 : o2ItFix) {
		    double dissim=localMeasure.getDissim(e1, e2);
		    if (dissim < res[i]) res[i]=dissim;
		    if (dissim < res[j]) res[j]=dissim;
		    j++;

		}
		i++;
	    }
	    return res;
	}

	private double getMaxMin(double[] vals){
	    double res=Double.NEGATIVE_INFINITY;
	    for (double val : vals) {
		if (val>res) res=val;
	    }
	    return res;
	}

	public double getMeasureValue(Set<? extends S> o1, Set<? extends S> o2) {
	    return getMaxMin(getMins(o1,o2));
	}

	public double getDissim(Set<? extends S> o1, Set<? extends S> o2) {
		return getMeasureValue(o1,o2);
	}

	public double getSim(Set<? extends S> o1, Set<? extends S> o2) {
		return 1-getMeasureValue(o1,o2);
	}

	public TYPES getMType() {
		return TYPES.dissimilarity;
	}*/
}
