/*
 * $Id: ConcatenatedIterator.java 899 2009-01-11 12:47:29Z euzenat $
 *
 * Copyright (C) INRIA 2003-2005, 2009
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

package fr.inrialpes.exmo.align.impl; 

import java.lang.Iterable;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.lang.UnsupportedOperationException;

/**
 * This class builds a composite iterator from two iterators
 * This helps writing more concise code.
 *
 * This is the naive implementation (can be optimized)
 *
 * @author Jérôme Euzenat
 * @version $Id: ConcatenatedIterator.java 899 2009-01-11 12:47:29Z euzenat $ 
 */

public final class ConcatenatedIterator<O> implements Iterator<O>, Iterable<O> {
    private Iterator<O> it1 = null;
    private Iterator<O> it2 = null;
    public ConcatenatedIterator ( Iterator<O> i1, Iterator<O> i2 ){
	it1 = i1;
	it2 = i2;
    }
    public boolean hasNext() {
	if ( it1.hasNext() || it2.hasNext() ) return true;
	else return false;
    }
    public O next() throws NoSuchElementException {
	if ( it1.hasNext() ) return it1.next();
	else return it2.next();
    }
    public void remove() throws UnsupportedOperationException {
	throw new UnsupportedOperationException();
    }
    public Iterator<O> iterator(){
	return this;
    }
}
