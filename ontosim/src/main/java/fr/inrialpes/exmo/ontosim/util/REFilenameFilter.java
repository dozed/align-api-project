/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   REFilenameFilter.java is part of OntoSim.
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
package fr.inrialpes.exmo.ontosim.util;

import java.io.File;
import java.io.FilenameFilter;

/**
 * A FilenameFilter based on regular expression
 * @author jerome David
 *
 */
public class REFilenameFilter implements FilenameFilter {

    private String regularExp;
    public REFilenameFilter(String regularExp) {
	this.regularExp=regularExp;
    }

    public boolean accept(File dir, String name) {
	return name.matches(regularExp);
    }

}
