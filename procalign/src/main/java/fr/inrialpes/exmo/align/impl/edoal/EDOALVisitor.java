/*
 * $Id: EDOALVisitor.java 1713 2012-03-26 17:48:12Z euzenat $
 *
 * Copyright (C) INRIA, 2012
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation; either version 2.1 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.align.impl.edoal;

import org.semanticweb.owl.align.AlignmentException;

public interface EDOALVisitor {

    public void visit( ClassId o ) throws AlignmentException;
    public void visit( ClassConstruction o ) throws AlignmentException;
    public void visit( ClassTypeRestriction o ) throws AlignmentException;
    public void visit( ClassDomainRestriction o ) throws AlignmentException;
    public void visit( ClassValueRestriction o ) throws AlignmentException;
    public void visit( ClassOccurenceRestriction o ) throws AlignmentException;
    public void visit( PropertyId o ) throws AlignmentException;
    public void visit( PropertyConstruction o ) throws AlignmentException;
    public void visit( PropertyDomainRestriction o ) throws AlignmentException;
    public void visit( PropertyTypeRestriction o ) throws AlignmentException;
    public void visit( PropertyValueRestriction o ) throws AlignmentException;
    public void visit( RelationId o ) throws AlignmentException;
    public void visit( RelationConstruction o ) throws AlignmentException;
    public void visit( RelationDomainRestriction o ) throws AlignmentException;
    public void visit( RelationCoDomainRestriction o ) throws AlignmentException;
    public void visit( InstanceId o ) throws AlignmentException;

    public void visit( Transformation o ) throws AlignmentException;
    public void visit( Value o ) throws AlignmentException;
    public void visit( Apply o ) throws AlignmentException;
    public void visit( Datatype o ) throws AlignmentException;
    //public void visit( Comparator o ) throws AlignmentException;
    //public void visit( EDOALCell o ) throws AlignmentException;
}
