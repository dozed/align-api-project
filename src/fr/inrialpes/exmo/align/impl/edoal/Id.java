/*
 * $Id: Id.java 1311 2010-03-07 22:51:10Z euzenat $
 *
 * Copyright (C) 2006 Digital Enterprise Research Insitute (DERI) Innsbruck
 * Sourceforge version 1.3 - 2006
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

package fr.inrialpes.exmo.align.impl.edoal;

import java.net.URI;

/** 
 * Interface or class description
 * 
 * @author FirstName LastName, FirstName LastName
 *
 * Created on 22-Mar-2005
 * Committed by $Author: poettler_ric $
 * 
 * $Source: /cvsroot/mediation/mappingapi/src/fr.inrialpes.exmo.align.impl.edoal/Id.java,v $, 
 * @version $Revision: 1.3 $ $Date: 2010-03-07 20:40:05 +0100 (Sun, 07 Mar 2010) $
 */

public interface Id {

    /*
    public boolean isComplexExpression(){
	return false;
    }
    */
	 
    /**
     * Returns a plain representation of the value of this id.
     * @return the plain text representation.
     */
    public String plainText();
	 
    /**
     * Returns a plain representation of the value of this id.
     * @return the plain text representation.
     */
    public URI getURI();
}

