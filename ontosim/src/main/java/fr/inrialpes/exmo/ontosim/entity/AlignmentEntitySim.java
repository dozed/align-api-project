/**
 *   $Id: AlignmentEntitySim.java 111 2011-05-27 09:18:55Z jdavid $
 *
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   AlignmentEntitySim.java is part of OntoSim.
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

import java.util.Enumeration;

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.Cell;

import fr.inrialpes.exmo.ontosim.align.AbstractAlignmentSpaceMeasure;
import fr.inrialpes.exmo.ontosim.entity.model.Entity;
import fr.inrialpes.exmo.ontowrap.Ontology;

public class AlignmentEntitySim extends AbstractAlignmentSpaceMeasure<Entity<?>> {

	public AlignmentEntitySim() {
		super();
	}

	public double getDissim(Entity<?> e1, Entity<?> e2) {
		return 1-getMeasureValue(e1,e2);
	}

	public double getMeasureValue(Entity<?> e1, Entity<?> e2) {
		Ontology<?> o1 = e1.getOntology();
		Ontology<?> o2 = e2.getOntology();

		if (getAlignments(o1, o2)==null)
			return 0;
		Alignment a = getAlignments(o1, o2).iterator().next();
		Enumeration<Cell> enumA = a.getElements();
		while (enumA.hasMoreElements()) {
			Cell rel = enumA.nextElement();
			if (((rel.getObject1().equals(e1.getURI())&&rel.getObject2().equals(e2.getURI()))||
					(rel.getObject2().equals(e1.getURI())&&rel.getObject1().equals(e2.getURI()))) ) {// &&
				return rel.getStrength();
			}
		}
		return 0;
	}

	public double getSim(Entity<?> e1, Entity<?> e2) {
		return getMeasureValue(e1,e2);
	}

	public TYPES getMType() {
		return TYPES.similarity;
	}



}
