/*
 * $Id: RelationRestriction.java 1710 2012-03-23 19:53:25Z euzenat $
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.5 - 2006
 * Copyright (C) INRIA, 2009, 2012
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

/**
 * <p>
 * Superclass for all RelationConditions.
 * </p>
 * <p>
 * To successfully subclass this class the <code>clone</code> and
 * <code>equals</code> methods must be overwritten. And if new fields were
 * introduced, the <code>hashCode</code> and <code>toString</code> methods,
 * too.
 * </p>
 * <p>
 * Created on 23-Mar-2005 Committed by $Author: poettler_ric $
 * </p>
 * 
 * @version $Id: RelationRestriction.java 1710 2012-03-23 19:53:25Z euzenat $
 */
public abstract class RelationRestriction extends RelationExpression {

    protected RelationRestriction() {}

}
