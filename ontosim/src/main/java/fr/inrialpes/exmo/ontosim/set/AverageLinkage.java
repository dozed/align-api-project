/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   AverageLinkage.java is part of OntoSim.
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
import fr.inrialpes.exmo.ontosim.extractor.DummyExtractor;


public class AverageLinkage<S> extends SetMeasure<S> {


	public AverageLinkage(Measure<S> m) {
		super(m,new DummyExtractor());
	}
	
	/*public double getMeasureValue(Set<? extends S> o1, Set<? extends S> o2) {
		double sum=0;
		Iterator<? extends S> i1 = o1.iterator();
		while (i1.hasNext()) {
			S elem1=i1.next();
			Iterator<? extends S> i2 = o2.iterator();
			while (i2.hasNext()){
				S elem2=i2.next();
				double sim = localMeasure.getMeasureValue(elem1,elem2);
				sum += sim;
			}
		}
		return sum/(o1.size()*o2.size());
	}

	public double getDissim(Set<? extends S> o1, Set<? extends S> o2) {
		return 1-getSim(o1,o2);
	}

	public double getSim(Set<? extends S> o1, Set<? extends S> o2) {
		double sum=0;
		Iterator<? extends S> i1 = o1.iterator();
		while (i1.hasNext()) {
			S elem1=i1.next();
			Iterator<? extends S> i2 = o2.iterator();
			while (i2.hasNext()){
				S elem2=i2.next();
				double sim = localMeasure.getSim(elem1,elem2);
				sum += sim;
			}
		}
		return sum/(o1.size()*o2.size());
	}*/
}
