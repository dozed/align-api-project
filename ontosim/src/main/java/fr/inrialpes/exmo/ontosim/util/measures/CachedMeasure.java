/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   CachedMeasure.java is part of OntoSim.
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

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.OntoSimException;
import fr.inrialpes.exmo.ontosim.util.matrix.Matrix;
import fr.inrialpes.exmo.ontosim.util.matrix.MatrixDouble;

public class CachedMeasure<O> implements Measure<O> {

    protected Matrix<O,O> mValues;

    protected Measure.TYPES type;

    public CachedMeasure(Matrix<O,O> mValues, Measure.TYPES typeM) {
	this.mValues=mValues;
	type = typeM;
    }

    protected CachedMeasure(Measure.TYPES typeM) {
	this.mValues=new MatrixDouble<O, O>();
	type = typeM;
    }

    public double getDissim(O o1, O o2) {
	if (type == TYPES.dissimilarity)
	    return getMeasureValue(o1,o2);
	else if (type == TYPES.similarity)
	    return 1-getMeasureValue(o1,o2);
	throw new OntoSimException("Cannot be convert into dissimilarity value");
    }

    public Measure.TYPES getMType() {
	return type;
    }

    public double getMeasureValue(O o1, O o2) {
	if (mValues.containsRdim(o1))
	    return mValues.get(o1, o2);
	else if (mValues.containsCdim(o1))
	    return mValues.get(o2, o1);
	return Double.NaN;
    }

    public double getSim(O o1, O o2) {
	if (type == TYPES.similarity)
	    return getMeasureValue(o1,o2);
	else if (type == TYPES.dissimilarity)
	    return 1-getMeasureValue(o1,o2);
	throw new OntoSimException("Cannot be convert into similarity value");
    }
}
