/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   NoAlignmentException.java is part of OntoSim.
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

import fr.inrialpes.exmo.ontowrap.Ontology;


public class NoAlignmentException extends OntoSimException {

    private static final long serialVersionUID = 1L;
	protected Ontology<?> o1;
	protected Ontology<?> o2;

	public NoAlignmentException(Ontology<?> o1, Ontology<?> o2, Exception cause){
		super("No alignment between "+o1.getURI()+" and "+o2.getURI(),cause);
		this.o1=o1;
		this.o2=o2;
	}

	public NoAlignmentException(Ontology<?> o1, Ontology<?> o2){
		super("No alignment between "+o1.getURI()+" and "+o2.getURI());
		this.o1=o1;
		this.o2=o2;
	}


}
