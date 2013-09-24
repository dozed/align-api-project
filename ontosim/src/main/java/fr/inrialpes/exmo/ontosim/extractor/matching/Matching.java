/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   Matching.java is part of OntoSim.
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
package fr.inrialpes.exmo.ontosim.extractor.matching;

/**
 * represents a matching between two sets
 * could extends Collection ?
 * @author jerome David
 *
 * @param <O>
 */
public interface Matching<O> extends Iterable<Matching.Entry<O>>{
    
    public class Entry<T> {
	T src;
	T trg;
	public Entry(T s, T t) {src=s;trg=t;}
	public T getSource() {return src;}
	public T getTarget() {return trg;}
    }

    public boolean add(O s, O t);
    
    public void clear();
    
    public int size();
    
    public boolean contains(O s, O t);
    
    public Matching<O> transposeView();

}
