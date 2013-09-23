package fr.inrialpes.exmo.ontowrap.util;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Set;

public abstract class FilteredSet<T> extends AbstractSet<T> {
	
	private Set<T> s;
	private int size=-1;
	
	public FilteredSet(Set<T> s) {
	    this.s=s;
	}
	
	protected abstract boolean isFiltered(T obj);
	
	@Override
	public final Iterator<T> iterator() {
	    return new Iterator<T>() {
		Iterator<T> i=s.iterator();
		T current=null;
		
		public boolean hasNext() {
		   while (current==null || isFiltered(current)) {
		       if (!i.hasNext()) return false;
		       current=i.next();
		   }
		   return true;
		}

		@Override
		public T next() {
		    T r = current;
		    if (hasNext()) {
			current=null;
			return r;
		    }
		    throw new NoSuchElementException();
		}

		@Override
		public void remove() {
		    throw new UnsupportedOperationException();
		    
		}
	    };
	}

	public final int size() {
	    if (size==-1) {
		Iterator<T> i=this.iterator();
		size=0;
		while (i.hasNext()) {
		    i.next();
		    size++;
		}
	    }
	    return size;
	}
	
}
