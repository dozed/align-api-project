/**
 *   Copyright 2008-2011 INRIA, Université Pierre Mendès France
 *   
 *   EntityImpl.java is part of OntoSim.
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

import fr.inrialpes.exmo.ontosim.OntoSimException;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;
import fr.inrialpes.exmo.ontowrap.OntowrapException;

public class EntityImpl<E> implements Entity<E> {

    private LoadedOntology<E> onto;
    private E obj;

    public EntityImpl(LoadedOntology<E> ont, E e) {
	onto=ont;
	obj=e;
    }

    public Set<String> getAnnotations(String lang) {
	try {
	    return onto.getEntityAnnotations(obj);
	} catch (OntowrapException e) {
	   throw new OntoSimException(e);
	}
    }

    public Set<String> getComments(String lang) {
	try {
	    return onto.getEntityComments(obj,lang);
	} catch (OntowrapException e) {
	    throw new OntoSimException(e);
	}
    }

    public Set<String> getLabels(String lang) {
	try {
	    return onto.getEntityNames(obj,lang);
	} catch (OntowrapException e) {
	    throw new OntoSimException(e);
	}
    }

    public E getObject() {
	return obj;
    }

    public LoadedOntology<E> getOntology() {
	return onto;
    }

    public URI getURI() {
	try {
	    return onto.getEntityURI(obj);
	} catch (OntowrapException e) {
	    throw new OntoSimException(e);
	}
    }

    public boolean isClass() {
	try {
	    return onto.isClass(obj);
	} catch ( OntowrapException owex ) { return false; }
    }

    public boolean isDataProperty() {
	try {
	    return onto.isDataProperty(obj);
	} catch ( OntowrapException owex ) { return false; }
    }

    public boolean isIndividual() {
	try {
	    return onto.isIndividual(obj);
	} catch ( OntowrapException owex ) { return false; }
    }

    public boolean isObjectProperty() {
	try {
	    return onto.isObjectProperty(obj);
	} catch ( OntowrapException owex ) { return false; }
    }

    public boolean isProperty() {
	try {
	    return onto.isProperty(obj);
	} catch ( OntowrapException owex ) { return false; }
    }
}
