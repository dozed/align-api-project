/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   MatrixDoubleArray.java is part of OntoSim.
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
package fr.inrialpes.exmo.ontosim.util.matrix;

import java.util.List;

public class MatrixDoubleArray<R,C> {

    private double[][] values;
    private List<R> rows;
    private List<C> cols;

    public MatrixDoubleArray(List<R> r, List<C> c, double[][] vals) {
	rows=r;
	cols=c;
	values=vals;
    }

    public double[][] getValues() {
	return values;
    }

    public List<R> getRows() {
	return rows;
    }

    public List<C> getCols() {
	return cols;
    }
}
