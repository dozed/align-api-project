/*
 * $Id: Pair.java 1356 2010-03-25 14:19:41Z euzenat $
 *
 * Copyright (C) INRIA, 2004-2005, 2007-2010
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
 *
 */

package fr.inrialpes.exmo.align.impl.eval;

public class Pair {
    private double x;
    private double y;
    public Pair( double x, double y ){
	this.x = x;
	this.y = y;
    }
    public double getX(){ return x; }
    public double getY(){ return y; }
    public void setX( double d ){ x = d; }
    public void setY( double d ){ y = d; }
}
