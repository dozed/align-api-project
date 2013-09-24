/**
 * $Id: AlignmentSpaceMeasure.java 66 2009-09-17 07:01:14Z euzenat $
 *
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *    
 *   AlignmentSpaceMeasure.java is part of OntoSim.
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

import org.semanticweb.owl.align.Alignment;
import org.semanticweb.owl.align.AlignmentException;
import org.semanticweb.owl.align.OntologyNetwork;

public interface AlignmentSpaceMeasure<S> extends Measure<S> {
    public boolean addAlignment(Alignment al) throws AlignmentException;
    public void setAlignmentSpace(OntologyNetwork on);
}
