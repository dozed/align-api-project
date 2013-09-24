/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   WeightedMaxSum.java is part of OntoSim.
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

/**
 * The weighted sum of the maximum similarities (from the most little set to the biggest one)
 * The weights are the the similarities themselves
 * @author jerome DAVID
 *
 * @param <S>
 */
public class WeightedMaxSum<S> extends SetMeasure<S> {


    public WeightedMaxSum(Measure<S> m) {
	super(m,null,null);
    }

    
    public double getDissim(Set<? extends S> o1, Set<? extends S> o2) {
	return 1.0-getMeasureValue(o1,o2);
    }

    public double getMeasureValue(Set<? extends S> o1, Set<? extends S> o2) {
	if (o1.size()>o2.size()) return getMeasureValue(o2,o1);
	double sum=0;
	double squares=0;
	for (S x : o1) {
	    double max=0;
	    for (S y : o2) {
		double v = this.localMeasure.getSim(x, y);
		if (v>max) max=v;
	    }
	    sum+=max;
	    squares+=max*max;
	}
	if (sum==0) return 0;
	return squares/sum;
    }

    public double getSim(Set<? extends S> o1, Set<? extends S> o2) {
	return getMeasureValue(o1,o2);
    }
    
    

}
