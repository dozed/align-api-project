/*
 * $Id: Visitable.java 1311 2010-03-07 22:51:10Z euzenat $
 *
 * Copyright (C) INRIA, 2009
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

/**
 * The interface of the objects that can be visited by a renderer
 *
 * @author Jérôme Euzenat
 * @version $Id: Visitable.java 1311 2010-03-07 22:51:10Z euzenat $ 
 */

public interface Visitable {
    public void accept( AlignmentVisitor visitor ) throws AlignmentException;
 }
