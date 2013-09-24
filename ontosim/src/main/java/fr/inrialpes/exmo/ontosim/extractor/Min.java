/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   Min.java is part of OntoSim.
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
import fr.inrialpes.exmo.ontosim.extractor.matching.BasicMatching;
import fr.inrialpes.exmo.ontosim.extractor.matching.Matching;

public class Min extends AbstractExtractor {
    

    @Override
    public <O> Matching<O> extract(Measure<O> m, Set<? extends O> src, Set<? extends O> trg) {
	double min = Double.POSITIVE_INFINITY;
	Matching <O> matching = new BasicMatching<O>();
	for (O s : src) 
	    for (O t : trg) {
		double v = m.getMeasureValue(s, t);
		if (v<min) {
		    matching.clear();
		    matching.add(s,t);//,v);
		    min=v;
		}
		else if (v==min) 
		    matching.add(s,t);//,v);
	    }
	return matching;
    }

}
