/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   CosineVM.java is part of OntoSim.
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
package fr.inrialpes.exmo.ontosim.vector;

public final class CosineVM extends VectorMeasure {


	public CosineVM() {
		super();
	}

	public double getMeasureValue(double[] v1, double[] v2) {
		double sum = 0;
		double normeV1=0;
		double normeV2=0;
		for (int i=0 ; i<v1.length ; i++ ) {
		    if (!(Double.isNaN(v1[i])||Double.isNaN(v2[i]))) {
			sum+=v1[i]*v2[i];
			normeV1 += v1[i]*v1[i];
			normeV2 += v2[i]*v2[i];
		    }
		}
		if (normeV1==0 || normeV2==0)
		    return 0;
		return sum/(Math.sqrt(normeV1)*Math.sqrt(normeV2));
	}

	public double getDissim(double[] o1, double[] o2) {
		return 1-getMeasureValue(o1,o2);
	}

	public double getSim(double[] o1, double[] o2) {
		return getMeasureValue(o1,o2);
	}

	public fr.inrialpes.exmo.ontosim.Measure.TYPES getMType() {
		return TYPES.similarity;
	}

}
