/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   Entity.java is part of OntoSim.
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
package fr.inrialpes.exmo.ontosim.entity.model;

import java.net.URI;
import java.util.Set;

import fr.inrialpes.exmo.ontowrap.LoadedOntology;

public interface Entity<E> {
	public URI getURI();
	public Set<String> getLabels(String lang);
	public Set<String> getComments(String lang);
	public LoadedOntology<E> getOntology();
	public Set<String> getAnnotations(String lang);
	public E getObject();

	public boolean isClass();
	public boolean isProperty();
	public boolean isDataProperty();
	public boolean isObjectProperty();
	public boolean isIndividual();
}
