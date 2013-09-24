/**
 *   Copyright 2008-2011 INRIA, Université Pierre Mendès France
 *   
 *   OntologySpaceMeasure.java is part of OntoSim.
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

import java.lang.reflect.ParameterizedType;
import java.util.HashSet;
import java.util.Set;

import fr.inrialpes.exmo.ontosim.entity.model.Entity;
import fr.inrialpes.exmo.ontosim.entity.model.EntityImpl;
import fr.inrialpes.exmo.ontosim.entity.model.HLEntityFactory;
import fr.inrialpes.exmo.ontosim.set.SetMeasure;
import fr.inrialpes.exmo.ontowrap.HeavyLoadedOntology;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntologyFactory;
import fr.inrialpes.exmo.ontowrap.OntowrapException;
import fr.inrialpes.exmo.ontowrap.jena25.JENAOntologyFactory;


public class  OntologySpaceMeasure implements Measure<LoadedOntology<?>> {

	private SetMeasure<Entity<?>> globalMeasure;

	public OntologySpaceMeasure(SetMeasure<Entity<?>> globalMeasure) {
		this.globalMeasure = globalMeasure;
	}

	public Measure<Entity<?>> getLocalMeasure() {
	    return globalMeasure.getLocalMeasure();
	}

	@SuppressWarnings("unchecked")
	private Set<Entity<?>> getEntities(LoadedOntology<?> o1) {
	    // Check if the localMeasure used depends on Jena.
	    // if the case, use JenaFactory instead of default factory
	    // it should be enhanced or removed after Ondrej experiments
	    Set<Entity<?>> entities = new HashSet<Entity<?>>();
	    for (java.lang.reflect.Type t : globalMeasure.getLocalMeasure().getClass().getGenericInterfaces()) {
		if (t instanceof ParameterizedType) {
		    ParameterizedType pt = (ParameterizedType) t;
		    if (pt.getRawType() == Measure.class) {
			if ( pt.getActualTypeArguments()[0] instanceof ParameterizedType && ((ParameterizedType)pt.getActualTypeArguments()[0]).getActualTypeArguments()[0] == com.hp.hpl.jena.ontology.OntResource.class) {
			    OntologyFactory f = new JENAOntologyFactory();
			    try {
				o1 = f.loadOntology(o1.getFile());
			    } catch (OntowrapException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			    }
			}
			   
		    }
		}
	    }
	    //System.out.println(((ParameterizedType) [0]).getActualTypeArguments()[0] );
	    if (o1 instanceof HeavyLoadedOntology) {
		try {
		    for ( Object e1 : o1.getEntities() )
			entities.add(HLEntityFactory.getInstance((HeavyLoadedOntology)o1).createHLEntity(e1));
		} catch ( OntowrapException owex ) { owex.printStackTrace(); }
	    }
	    else {
		try {
		    for ( Object e1 : o1.getEntities() ) entities.add(new EntityImpl(o1,e1));
		} catch ( OntowrapException owex ) { owex.printStackTrace(); }
	    }
	    return entities;
	}

	public double getDissim(LoadedOntology<?> o1, LoadedOntology<?> o2) {
		Set<Entity<?>> o1Entities = getEntities(o1);
		Set<Entity<?>> o2Entities = getEntities(o2);
		return globalMeasure.getDissim(o1Entities, o2Entities);
	}

	public double getMeasureValue(LoadedOntology<?> o1, LoadedOntology<?> o2) {
		Set<Entity<?>> o1Entities = getEntities(o1);
		Set<Entity<?>> o2Entities = getEntities(o2);
		return globalMeasure.getMeasureValue(o1Entities, o2Entities);
	}

	public double getSim(LoadedOntology<?> o1, LoadedOntology<?> o2) {
		Set<Entity<?>> o1Entities = getEntities(o1);
		Set<Entity<?>> o2Entities = getEntities(o2);
		return globalMeasure.getSim(o1Entities, o2Entities);
	}


	public TYPES getMType() {
		return globalMeasure.getMType();
	}

	public SetMeasure<Entity<?>> getGlobalMeasure() {
	    return this.globalMeasure;
	}


}
