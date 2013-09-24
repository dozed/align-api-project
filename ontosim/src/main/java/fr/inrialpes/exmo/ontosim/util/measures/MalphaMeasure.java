/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   MalphaMeasure.java is part of OntoSim.
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
package fr.inrialpes.exmo.ontosim.util.measures;

import java.util.Set;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.OntoSimException;

@Deprecated
public class MalphaMeasure<O> implements Measure<Set<? extends O>> {
	double alpha;

	public MalphaMeasure(double alpha) {
		this.alpha=alpha;
	}

	public double getDissim(Set<? extends O> s1, Set<? extends O> s2) {
		throw new OntoSimException(this.getClass()+" is not a dissimilarity");
	}

	public double getMeasureValue(Set<? extends O> s1, Set<? extends O> s2) {
		if (alpha == Double.NEGATIVE_INFINITY)
			return Math.min(s1.size(), s2.size());
		else if (alpha == Double.POSITIVE_INFINITY)
			return Math.max(s1.size(), s2.size());
		else if (alpha == 0)
			return Math.sqrt(s1.size()*s2.size());
		else if (alpha == 1)
			return ((double) (s1.size()+s2.size()))/2;
		return Math.pow((Math.pow(s1.size(), alpha)+Math.pow(s2.size(), alpha))/2.0, 1.0/alpha);
	}

	public double getSim(Set<? extends O> s1, Set<? extends O> s2) {
		throw new OntoSimException(this.getClass()+" is not a similarity");
	}

	public fr.inrialpes.exmo.ontosim.Measure.TYPES getMType() {
		return TYPES.other;
	}



}
