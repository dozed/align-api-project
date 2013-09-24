/*
 * $Id: OntologyNetwork.java 1620 2011-05-31 13:49:54Z jdavid $
 *
 * Copyright (C) INRIA, 2009-2010
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

package org.semanticweb.owl.align;

import java.lang.Cloneable;
import java.util.Set;
import java.net.URI;

/**
 * Represents a distributed system of aligned ontologies or network of ontologies.
 *
 * @author Jérôme Euzenat
 * @version $Id: OntologyNetwork.java 1620 2011-05-31 13:49:54Z jdavid $ 
 */
public interface OntologyNetwork extends Cloneable {

    public void addAlignment( Alignment al ) throws AlignmentException; 
    public void addOntology( URI onto );
    public void remAlignment( Alignment al ) throws AlignmentException;
    public void remOntology( URI onto ) throws AlignmentException;
    public Set<Alignment> getAlignments();
    public Set<URI> getOntologies();
    public Set<Alignment> getTargetingAlignments( URI onto );
    public Set<Alignment> getSourceAlignments( URI onto );
    public Set<Alignment> getAlignments(URI srcOnto, URI dstOnto);

    /**
     * close reflexively the network.
     * The network is modified.
     */
    public void invert() throws AlignmentException;
}


