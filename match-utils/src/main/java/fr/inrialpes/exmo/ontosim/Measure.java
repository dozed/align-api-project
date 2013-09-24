/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   Measure.java is part of OntoSim.
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
package fr.inrialpes.exmo.ontosim;

public interface Measure<O> {
	static enum TYPES {similarity, dissimilarity, distance, other};
	

	public TYPES getMType();
	public double getMeasureValue(O o1, O o2);
	public double getSim(O o1, O o2);
	public double getDissim(O o1, O o2);
	
	
	// TO DO for best use in SetMeasures
	// add some methods like
	// public HalfMatrixDouble<O,O> getMeasureValue(Set<O> s1, Set<O> s2);
	// Could Be done in a new Interface MatrixMeasure or something like that
	
	// Then create Measure abstract class implementing these methods
	// And special methods in CachedMeasure

	// After change the calls in setMeasure.
}
