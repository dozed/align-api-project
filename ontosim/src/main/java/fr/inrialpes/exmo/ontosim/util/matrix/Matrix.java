/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   Matrix.java is part of OntoSim.
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

import java.util.Set;

public interface Matrix<R,C> {

    public void put(R r, C c, double value);
    public double get(R r, C c);
    public Set<R> getDimR();
    public Set<C> getDimC();
    public Set<?> keySet();
    
    public boolean containsRdim(R r);
    public boolean containsCdim(C c);

    public MatrixDoubleArray<R, C> toArray();
    public MatrixDoubleArray<C, R> toArrayT();
    
    public void putAll(Matrix<R, C> m);
}
