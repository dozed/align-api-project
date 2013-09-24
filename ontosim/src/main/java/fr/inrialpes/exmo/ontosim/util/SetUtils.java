/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   SetUtils.java is part of OntoSim.
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

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public abstract class SetUtils {

    public static <S> Set<S> concat(final Set<S> set, final S e) {
	if (set.contains(e)) return set;
	return new AbstractSet<S>() {

	    @Override
	    public Iterator<S> iterator() {
		return new Iterator<S>() {
		    private Iterator<S> it=set.iterator();
		    private S elem=e;
		    private boolean notEnd=true;

		    public boolean hasNext() {
			return it.hasNext() || notEnd;
		    }

		    public S next() {
			try {
			    return it.next();
			}
			catch (NoSuchElementException e) {
        			if (!notEnd) {
        			    throw new NoSuchElementException();
        			}
        			notEnd=false;
        			return elem;
			}
		    }
		    public void remove() {
			throw new UnsupportedOperationException();
		    }};
	    }

	    @Override
	    public int size() {
		return set.size()+1;
	    }

	};
    }
}
