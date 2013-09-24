/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   Extractor.java is part of OntoSim.
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
package fr.inrialpes.exmo.ontosim.extractor;

import java.util.Set;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.extractor.matching.Matching;
import fr.inrialpes.exmo.ontosim.util.matrix.Matrix;

/**
 * An extractor is an algorithm which extracts a matching according to measure values
 * @author jerome DAVID
 *
 */
public interface Extractor {
    
    //public Cardinality getCardinality();
    
    public <O> Matching<O> extract(Matrix<O, O> m);
    
    public <O> Matching<O> extract(Measure<O> m, Set<? extends O> s, Set<? extends O> t);
}
