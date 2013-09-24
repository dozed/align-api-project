/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   OntoSimException.java is part of OntoSim.
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

public class OntoSimException extends RuntimeException {

    private static final long serialVersionUID = 1L;

	public OntoSimException(Exception e){
		super(e);
	}
	
	public OntoSimException(String msg, Exception e){
		super(msg,e);
	}
	
	public OntoSimException(String msg){
		super(msg);
	}
}
