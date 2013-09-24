/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   FullLinkage.java is part of OntoSim.
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
import fr.inrialpes.exmo.ontosim.extractor.Max;

public class FullLinkage<S> extends SetMeasure<S> {

    public FullLinkage(Measure<S> lm) {
	super(lm,new Max(),AggregationScheme.getInstance(DummyAS.class));
    }

    /*public double getDissim(Set<? extends S> o1, Set<? extends S> o2) {
	if (localMeasure.getMType() == TYPES.dissimilarity)
	    return getMeasureValue(o1,o2);
	return 1-getMeasureValue(o1,o2);
    }

    public double getMeasureValue(Set<? extends S> o1, Set<? extends S> o2) {
	if (localMeasure.getMType() == TYPES.dissimilarity) {
	    double max = Double.NEGATIVE_INFINITY;
	    for (S m1 : o1) {
		for (S m2 : o2) {
		    double sim = localMeasure.getMeasureValue(m1, m2);
		    if (sim>max) max=sim;
		}
	    }
	    return max;
	}
	else if (localMeasure.getMType() == TYPES.similarity) {
	    double min = Double.POSITIVE_INFINITY;
	    for (S m1 : o1) {
		for (S m2 : o2) {
		    double sim = localMeasure.getMeasureValue(m1, m2);
		    if (sim<min) min=sim;
		}
	    }
	    return min;
	}
	return Double.NaN;

    }

    public double getSim(Set<? extends S> o1, Set<? extends S> o2) {
	if (localMeasure.getMType() == TYPES.similarity)
	    return getMeasureValue(o1,o2);
	return 1-getMeasureValue(o1,o2);
    }*/

}
