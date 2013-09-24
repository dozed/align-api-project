/**
 * $Id: AlignmentGlobalSum.java 113 2011-05-27 09:26:12Z jdavid $
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   AlignmentGlobalSum.java is part of OntoSim.
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

package fr.inrialpes.exmo.ontosim.align;

import java.util.Enumeration;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.ontosim.NoAlignmentException;
import fr.inrialpes.exmo.ontosim.OntoSimException;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;

public class AlignmentGlobalSum extends AbstractAlignmentSpaceMeasure<LoadedOntology<?>> {

	protected double getSumAlign(Alignment a) {
		Enumeration<?> enumA = a.getElements();
		double sum=0;
		while (enumA.hasMoreElements())
			sum += ((Cell) enumA.nextElement()).getStrength();
		return sum;
	}

	/**
	 * Similarity version of delta_gm issued from the j.euzenat paper
	 * @param o1
	 * @param o2
	 * @param al
	 * @return the measure value
	 */
	public double getMeasureValue( LoadedOntology<?> o1, LoadedOntology<?> o2, Alignment al ) throws OntoSimException {
	    try {
		addAlignment( al );
	    } catch (AlignmentException aex) {
		throw new OntoSimException( "Alignment error", aex );
	    }
	    if (! getAlignments( o1, o2 ).contains( al ) ) return 0;
	    return getSumAlign( al );
	}

	public double getDissim(LoadedOntology<?> o1, LoadedOntology<?> o2) {
	    throw new OntoSimException(this.getClass()+" is not a dissimilarity");
	}

	public double getMeasureValue(LoadedOntology<?> o1, LoadedOntology<?> o2) {
	    double max=Double.NEGATIVE_INFINITY;
	    try {
		for (Alignment al : getAlignments( o1, o2 ) ) {
		    max = Math.max(max, getMeasureValue(o1,o2,al));
		}
		if (max == Double.NEGATIVE_INFINITY)
		    return Double.NaN;
		return max;
	    }
	    catch (NullPointerException e) {
		throw new NoAlignmentException(o1,o2,e);
	    }
	}

	public double getSim(LoadedOntology<?> o1, LoadedOntology<?> o2) {
		throw new OntoSimException(this.getClass()+" is not a similarity");
	}

	public fr.inrialpes.exmo.ontosim.Measure.TYPES getMType() {
		return TYPES.other;
	}

	public fr.inrialpes.exmo.ontosim.Measure.TYPES getSubMeasureType() {
		return TYPES.other;
	}


}
