/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   HalfMatrixDouble.java is part of OntoSim.
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
import java.util.List;
import java.util.Map;
import java.util.Set;


public class HalfMatrixDouble<T> implements Matrix<T,T>{

    public List<double[]> values = new ArrayList<double[]>();
    private Map<T,Integer> keys = new HashMap<T, Integer>();
    private int newIdx=0;


    public HalfMatrixDouble() {

    }

    public void put(T key1, T key2,  double value) {
	int k1idx = getKeyIdx(key1);
	int k2idx = getKeyIdx(key2);

	int maxIdx = Math.max(k1idx, k2idx);
	double[] tabRow = values.get(maxIdx);
	if (tabRow==null) {
	    tabRow = new double[maxIdx+1];
	    Arrays.fill(tabRow, Double.NaN);
	    values.set(maxIdx, tabRow);

	}
	tabRow[Math.min(k1idx,k2idx)]=value;
    }

    public double get(T key1, T key2) {
	Integer k1idx = keys.get(key1);
	Integer k2idx = keys.get(key2);
	if (k1idx != null && k2idx !=null) {
	    double[] tabRow = values.get(Math.max(k1idx.intValue(),k2idx.intValue()));
	    if (tabRow !=null)
		return tabRow[Math.min(k1idx.intValue(),k2idx.intValue())];
	}
	return Double.NaN;
    }

    public Set<T> keySet() {
	return Collections.unmodifiableSet(keys.keySet());
    }


    protected int getKeyIdx(T key) {
	Integer idx = keys.get(key);
	if (idx==null) {
	    idx = new Integer(newIdx);
	    keys.put(key,idx);
	    values.add(null);
	    newIdx++;
	}
	return idx.intValue();
    }

    public Set<T> getDimC() {
	return keySet();
    }

    public Set<T> getDimR() {
	return keySet();
    }

    public MatrixDoubleArray<T, T> toArray() {
	List<T> rowL = new ArrayList<T>(keySet());
	double[][] vals = new double[rowL.size()][rowL.size()];
	int i=0;
	for (T aRow : rowL) {
	    int j=0;
	    for (T aCol : rowL) {
		double v = this.get(aRow, aCol);
		vals[i][j]=Double.isNaN(v)?0:v;
		j++;
	    }
	    i++;
	}
	return new MatrixDoubleArray<T, T>(rowL, rowL, vals);
    }

    public MatrixDoubleArray<T,T> toArrayT() {
	return toArray();
    }

    @Override
    public boolean containsCdim(T c) {
	return keys.containsKey(c);
    }

    @Override
    public boolean containsRdim(T r) {
	return keys.containsKey(r);
    }

    /**
     * Can be better implemented...
     */
    public void putAll(Matrix<T, T> m) {
	for (T r : m.getDimR()/*md.rowVal.keySet()*/){
	    for (T c : m.getDimC()/*md.col.keySet()*/) {
		double newVal = m.get(r, c);
		if ( !Double.isNaN(newVal) )
		    put(r,c,newVal);
	    }
	}
	
    }
}
