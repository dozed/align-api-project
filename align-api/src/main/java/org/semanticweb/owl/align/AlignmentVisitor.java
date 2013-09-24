/*
 * $Id: AlignmentVisitor.java 1662 2012-01-14 15:47:39Z euzenat $
 *
 * Copyright (C) INRIA, 2004, 2008-2009, 2012
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

import java.util.Properties;

/**
 *
 *
 * @author Jérôme Euzenat
 * @version $Id: AlignmentVisitor.java 1662 2012-01-14 15:47:39Z euzenat $ 
 */

public interface AlignmentVisitor {
    public void init( Properties p );
    public void visit( Alignment o ) throws AlignmentException;
    public void visit( Cell o ) throws AlignmentException;
    public void visit( Relation o ) throws AlignmentException;
}
