/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   StringMeasureSS.java is part of OntoSim.
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
package fr.inrialpes.exmo.ontosim.string;

import com.wcohen.ss.AbstractStringDistance;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.OntoSimException;

/**
 * A string measure using secondString package. The secondString measure must be a similarity !
 * @author jerome david
 *
 */
public class StringMeasureSS implements Measure<String> {
	private AbstractStringDistance sd;


	public StringMeasureSS(AbstractStringDistance d) {
		sd = d;
	}


	public double getMeasureValue(String o1, String o2) {
	    if (o1.equals(o2)) return 1;
	    double sim=sd.score(o1,o2);
		if (Double.isNaN(sim))
			return 0;
		return sim;
	}

	public double getDissim(String o1, String o2) {
		return 1-getMeasureValue(o1,o2);
	}

	public double getSim(String o1, String o2) {
	    double val = getMeasureValue(o1,o2);
	    if (val>1) throw new OntoSimException("Similarity value greater than 1, "+
		    sd.getClass().getName()+" seems not to be a similarity measure");
	    return val; 
	}

	public fr.inrialpes.exmo.ontosim.Measure.TYPES getMType() {
		return TYPES.similarity;
	}
}
