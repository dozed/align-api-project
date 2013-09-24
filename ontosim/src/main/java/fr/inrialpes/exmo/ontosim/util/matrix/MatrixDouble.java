/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   MatrixDouble.java is part of OntoSim.
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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MatrixDouble<R,C> implements Matrix<R,C> {

    //private WeakReference<MatrixDoubleArray<R,C>> matrix; 
    
    private Map<C,Integer> col = new HashMap<C,Integer>();
    private Map<R,List<double[]>> rowVal = new HashMap<R, List<double[]>>();

    private int currentCidx=-1;
    private int cVsize=128;


    public synchronized void put(R r,C c, double value) {
	List<double[]> vals = rowVal.get(r);
	if (vals==null) {
	    vals = new ArrayList<double[]>();
	    rowVal.put(r, vals);
	}
	Integer colIdx = col.get(c);

	if (colIdx == null) {
	    currentCidx++;
	    colIdx = Integer.valueOf(currentCidx);
	    col.put(c, colIdx);
	}

	int divCidx = colIdx.intValue()/cVsize;
	while (vals.size() <= divCidx)
	    vals.add(null);
	double[] valTab = vals.get(divCidx);
	if (valTab==null) {
	    valTab = new double[cVsize];
	    Arrays.fill(valTab, Double.NaN);
	    vals.set(divCidx, valTab);
	}
	valTab[colIdx.intValue()%cVsize]=value;
    }

    public double get(R r,C c){
	try {
	    if (rowVal.containsKey(r) && col.containsKey(c)) {
		int cIdx = col.get(c);
		double[] tabRes = rowVal.get(r).get(cIdx/cVsize);
		if (tabRes!=null) return tabRes[cIdx%cVsize];
	    }
	}
	catch (IndexOutOfBoundsException ie) {};
	return Double.NaN;
    }

    public synchronized void putAll(Matrix<R,C> m) {
	for (R r : m.getDimR()/*md.rowVal.keySet()*/){
	    for (C c : m.getDimC()/*md.col.keySet()*/) {
		double newVal = m.get(r, c);
		if ( !Double.isNaN(newVal) )
		    put(r,c,newVal);
	    }
	}
    }

    public Set<R> getDimR() {
	return Collections.unmodifiableSet(rowVal.keySet());
    }

    public Set<C> getDimC() {
	return Collections.unmodifiableSet(col.keySet());
    }

    public Set<?> keySet() {
	Set<Object> keySet = new HashSet<Object>();
	keySet.addAll(col.keySet());
	keySet.addAll(rowVal.keySet());
	return keySet;
    }

    public boolean containsRdim(R r) {
	return rowVal.containsKey(r);
    }

    public boolean containsCdim(C c) {
	return col.containsKey(c);
    }

    public boolean containsKey(R r, C c) {
	return rowVal.containsKey(r) && col.containsKey(c) && !Double.isNaN(get(r,c));
    }

    public MatrixDoubleArray<C,R> toArrayT() {
	List<R> rowL= new ArrayList<R>(rowVal.keySet());
	List<C> colL= new ArrayList<C>(col.keySet());

	double[][] vals = new double[colL.size()][rowL.size()];
	int i=0;
	for (C aCol : colL) {
	    int j=0;
	    for (R aRow : rowL) {
		double v = this.get(aRow, aCol);
		vals[i][j]=Double.isNaN(v)?0:v;
		j++;
	    }
	    i++;
	}
	return new MatrixDoubleArray<C, R>(colL, rowL, vals);
    }
    
    public MatrixDoubleArray<R,C> toArray() {
	List<R> rowL= new ArrayList<R>(rowVal.keySet());
	List<C> colL= new ArrayList<C>(col.keySet());

	double[][] vals = new double[rowL.size()][colL.size()];
	int i=0;
	for (R aRow : rowL) {
	    int j=0;
	    for (C aCol : colL) {
		double v = this.get(aRow, aCol);
		vals[i][j]=Double.isNaN(v)?0:v;
		j++;
	    }
	    i++;
	}
	return new MatrixDoubleArray<R, C>(rowL, colL, vals);
    }


}
