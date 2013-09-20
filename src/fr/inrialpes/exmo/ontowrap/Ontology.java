/*
 * $Id: Ontology.java 1409 2010-03-31 10:17:05Z euzenat $
 *
 * Copyright (C) INRIA, 2008, 2010
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

package fr.inrialpes.exmo.ontowrap;

import java.net.URI;

public interface Ontology<O> {

    public URI getURI();
    public URI getFile();
    public URI getFormURI(); // Can be null
    public String getFormalism(); // Can be null
    public O getOntology();

    public void setURI( URI uri );
    public void setFile( URI file );
    public void setFormURI( URI u );
    public void setFormalism( String name );
    public void setOntology( O o );
}
