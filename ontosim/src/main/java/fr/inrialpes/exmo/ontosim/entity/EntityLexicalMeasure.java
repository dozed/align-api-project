/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   EntityLexicalMeasure.java is part of OntoSim.
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
package fr.inrialpes.exmo.ontosim.entity;


import java.net.URI;
import java.util.LinkedHashSet;
import java.util.Set;

import com.wcohen.ss.JaroWinkler;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.OntoSimException;
import fr.inrialpes.exmo.ontosim.entity.model.Entity;
import fr.inrialpes.exmo.ontosim.set.MaxCoupling;
import fr.inrialpes.exmo.ontosim.set.SetMeasure;
import fr.inrialpes.exmo.ontosim.string.StringMeasureSS;

public class EntityLexicalMeasure<E> implements Measure<Entity<E>> {

	private final Measure<String> stringMeasure;
	private final SetMeasure<String> setMeasure;
	private final String language;
	
	


	public EntityLexicalMeasure() {
	    this((String)null);
	}

	public EntityLexicalMeasure(String language) {
	    this(new MaxCoupling<String>(new StringMeasureSS(new JaroWinkler()),Double.NEGATIVE_INFINITY),language);
	}
	
	public EntityLexicalMeasure(SetMeasure<String> measure) {
		this(measure,null);
	}

	public EntityLexicalMeasure(Measure<String> measure) {
	    this(new MaxCoupling<String>(measure,Double.NEGATIVE_INFINITY),null);
	}
	
	public EntityLexicalMeasure(SetMeasure<String> measure, String language) {
	    stringMeasure = measure.getLocalMeasure();
	    setMeasure = measure;
	    this.language=language;
	}


	public double getMeasureValue(Entity<E> e1, Entity<E> e2) {
	    	if (e1.getURI()!=null && e1.getURI().equals(e2.getURI()))
			return 1.0;

		URI e1URI = e1.getURI();
		URI e2URI = e2.getURI();

		Set<String> e1Labels = e1.getAnnotations(language);
		Set<String> e2Labels = e2.getAnnotations(language);
		
		double sim=0;

		Set<String> temp = new LinkedHashSet<String>(e1Labels.size()+1);
		temp.addAll(e1Labels);
		if (e1URI!=null && e1URI.getFragment() !=null) temp.add(e1URI.getFragment());
		e1Labels=temp;

		temp = new LinkedHashSet<String>(e2Labels.size()+1);
		temp.addAll(e2Labels);
    		if (e2URI!=null && e2URI.getFragment() !=null)  temp.add(e2URI.getFragment());
    		e2Labels=temp;
    		
		if ((e1Labels.size() ==1) && (e2Labels.size() == 1)) {
			sim = stringMeasure.getSim(e1Labels.iterator().next(), e2Labels.iterator().next());
		}
		else if ((e1Labels.size() > 0) && (e2Labels.size() > 0)) {
			sim=setMeasure.getSim(e1Labels, e2Labels);
		}
		if (sim>1)
			throw new OntoSimException("Similarity value greater than 1");

		return sim;
	}

	public double getDissim(Entity<E> e1, Entity<E> e2) {
		return 1-getMeasureValue(e1,e2);
	}

	public double getSim(Entity<E> e1, Entity<E> e2) {
		return getMeasureValue(e1,e2);
	}

	public fr.inrialpes.exmo.ontosim.Measure.TYPES getMType() {
		return TYPES.similarity;
	}

}
