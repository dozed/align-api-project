/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   SetMeasure.java is part of OntoSim.
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
import fr.inrialpes.exmo.ontosim.aggregation.GenericMean;
import fr.inrialpes.exmo.ontosim.extractor.Extractor;
import fr.inrialpes.exmo.ontosim.util.measures.CachedMeasure;
import fr.inrialpes.exmo.ontosim.util.measures.DissimilarityUtility;
import fr.inrialpes.exmo.ontosim.util.measures.MeasureCache;
import fr.inrialpes.exmo.ontosim.util.measures.SimilarityUtility;

public class SetMeasure<S> implements Measure<Set<? extends S>>{

	protected Measure<S> localMeasure;
	protected Measure<S> lmAsSim;
	protected Measure<S> lmAsDissim;
	protected Extractor extractor;
	protected AggregationScheme as;

	public SetMeasure(Measure<S> lm, Extractor e) {
	    this(lm,e,AggregationScheme.getInstance(GenericMean.class));
	}

	public SetMeasure(Measure<S> lm, Extractor e, AggregationScheme as) {
	    //localMeasure=lm;
	    if (lm instanceof CachedMeasure<?> || lm instanceof MeasureCache<?>)
		localMeasure=lm;
	    else
		localMeasure = new MeasureCache<S>(lm);
	    
	    lmAsSim=SimilarityUtility.convert(localMeasure);
	    lmAsDissim=DissimilarityUtility.convert(localMeasure);
	    this.as=as;
	    this.extractor=e;
	    
	}

	/*public SetMeasure(Measure<S> m) {
		localMeasure = m;
	}*/

	public Measure<S> getLocalMeasure() {
		return localMeasure;
	}

	public TYPES getMType() {
		return localMeasure.getMType();
	}

	public double getDissim(Set<? extends S> o1, Set<? extends S> o2) {
	    return as.getValue(lmAsDissim, extractor.extract(localMeasure, o1, o2));
	}

	public double getMeasureValue(Set<? extends S> o1, Set<? extends S> o2) {
	    return as.getValue(localMeasure, extractor.extract(localMeasure, o1, o2));
	}

	public double getSim(Set<? extends S> o1, Set<? extends S> o2) {
	    return as.getValue(lmAsSim, extractor.extract(localMeasure, o1, o2));
	}
	
}