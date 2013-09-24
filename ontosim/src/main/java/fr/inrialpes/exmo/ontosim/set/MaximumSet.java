/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   MaximumSet.java is part of OntoSim.
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

@Deprecated
public class MaximumSet<S> extends FullLinkage<S> {

    public MaximumSet(Measure<S> lm) {
	super(lm);
	// TODO Auto-generated constructor stub
    }

	/*public MaximumSet(Measure<S> m) {
		super(m,new Max(),);
		// TODO Auto-generated constructor stub
	}

	public double getDissim(Set<? extends S> o1, Set<? extends S> o2) {
		double max=0;
		for (S e1 : o1) {
			for (S e2 : o2) {
				max = Math.max(max,this.localMeasure.getDissim(e1, e2));
			}
		}
		return max;
	}

	public double getMeasureValue(Set<? extends S> o1, Set<? extends S> o2) {
		double max=0;
		for (S e1 : o1) {
			for (S e2 : o2) {
				max = Math.max(max,this.localMeasure.getMeasureValue(e1, e2));
			}
		}
		return max;
	}

	public double getSim(Set<? extends S> o1, Set<? extends S> o2) {
		double max=0;
		for (S e1 : o1) {
			for (S e2 : o2) {
				max = Math.max(max,this.localMeasure.getSim(e1, e2));
			}
		}
		return max;
	}*/

}
