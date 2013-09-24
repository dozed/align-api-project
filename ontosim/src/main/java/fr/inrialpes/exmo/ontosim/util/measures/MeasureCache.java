/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   MeasureCache.java is part of OntoSim.
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

import java.lang.ref.Reference;
import java.lang.ref.WeakReference;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.util.matrix.HalfMatrixDouble;
import fr.inrialpes.exmo.ontosim.util.matrix.Matrix;
import fr.inrialpes.exmo.ontosim.util.matrix.MatrixDouble;

public class MeasureCache<O> implements Measure<O>{

    protected Reference<Matrix<O,O>> mValues;
    protected Measure<O> m;

    protected boolean fullMatrix;

    public MeasureCache(Measure<O> m) {
	this(m,true);
    }

    public MeasureCache(Measure<O> m, boolean fullMatrix) {
	this.m=m;
	this.fullMatrix=fullMatrix;
    }

    protected void createMatrix() {
	Matrix<O,O> m=null;
	if (fullMatrix)
	    m=new MatrixDouble<O,O>();
	else
	    m=new HalfMatrixDouble<O>();
	
	mValues=new WeakReference<Matrix<O,O>>(m);
	
    }

    public double getDissim(O o1, O o2) {
	if (getMType() == TYPES.dissimilarity)
	    return getMeasureValue(o1,o2);
	else
	    return m.getDissim(o1, o2);
    }

    public fr.inrialpes.exmo.ontosim.Measure.TYPES getMType() {
	return m.getMType();
    }

    public double getMeasureValue(O o1, O o2) {
	Matrix<O,O> values =null;
        if (mValues==null || (values=mValues.get())==null) {
            createMatrix();
            values=mValues.get();
        }
        double val = values.get(o1, o2);
        if (Double.isNaN(val)) {
            val = m.getMeasureValue(o1, o2);
            values.put(o1, o2, val);
        }
        values=null;
        return val;
    }

    public double getSim(O o1, O o2) {
	if (getMType() == TYPES.similarity)
	    return getMeasureValue(o1,o2);
	else
	    return m.getSim(o1, o2);
    }
}
