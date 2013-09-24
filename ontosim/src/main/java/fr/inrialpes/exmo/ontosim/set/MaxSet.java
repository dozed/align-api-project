/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   MaxSet.java is part of OntoSim.
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
package fr.inrialpes.exmo.ontosim.set;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.Set;

import fr.inrialpes.exmo.ontosim.Measure;

/**
 * Measure which return the max similarity (min dissimilarity) between two sets of objects of
 * type O according to a local measure m.
 * @author jerome
 *
 * @param <S>
 */
@Deprecated
public class MaxSet<S> extends SetMeasure<S> {

	public MaxSet(Measure<S> m) {
		super(m,null,null);
	}

	private double getSumMax(Set<? extends S> o1, Set<? extends S> o2, Method m) {

		double sum=0;
		Iterator<? extends S> o1It = o1.iterator();
		while (o1It.hasNext()) {
			S o1Elem = o1It.next();
			double max=0;
			Iterator<? extends S> o2It = o2.iterator();
			while (o2It.hasNext()) {
				double res=0;
				try {
					res = ((Double) m.invoke(localMeasure, new Object[]{o1Elem, o2It.next()})).doubleValue();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				max = Math.max(max, res);
			}
			sum += max;
		}

		return sum;
	}


	public double getMeasureValue(Set<? extends S> o1, Set<? extends S> o2) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		String methodName= trace[2].getMethodName();
		Method method;
		try {
			method = localMeasure.getClass().getMethod(methodName, new Class[]{Object.class,Object.class});
			double sum12 = getSumMax(o1,o2,method);
			double sum21 = getSumMax(o1,o2,method);
			return (sum12+sum21)/(o1.size()+o2.size());
		} catch (SecurityException e) {
			// TODO Auto-generated catch
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}

	public double getDissim(Set<? extends S> o1, Set<? extends S> o2) {
		double min=Double.MAX_VALUE;
		Iterator<? extends S> o1It = o1.iterator();
		while (o1It.hasNext()) {
			S o1Elem = o1It.next();
			Iterator<? extends S> o2It = o2.iterator();
			while (o2It.hasNext())
				min = Math.min(min, this.localMeasure.getDissim(o1Elem, o2It.next()));
		}
		return min;
	}

	public double getSim(Set<? extends S> o1, Set<? extends S> o2) {
		StackTraceElement[] trace = Thread.currentThread().getStackTrace();
		String methodName= trace[2].getMethodName();
		Method method;
		try {
			method = localMeasure.getClass().getMethod(methodName, new Class[]{Object.class,Object.class});
			double sum12 = getSumMax(o1,o2,method);
			double sum21 = getSumMax(o1,o2,method);
			return (sum12+sum21)/(o1.size()+o2.size());
		} catch (SecurityException e) {
			// TODO Auto-generated catch
			e.printStackTrace();
		} catch (NoSuchMethodException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return 0;
	}



}
