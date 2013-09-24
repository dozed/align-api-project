/**
 *   Copyright 2008, 2009 INRIA, Université Pierre Mendès France
 *   
 *   MeasureFactory.java is part of OntoSim.
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

import java.lang.reflect.Constructor;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.OntoSimException;
import fr.inrialpes.exmo.ontowrap.LoadedOntology;


/**
 * Factory of measures which can instanciate a measure by giving its name and its parameters
 * The factory can use a cache for storing instances of measures
 * @author jerome D
 *
 */
public class MeasureFactory {

    private Map<Node,Object> mCache;
    
    public MeasureFactory(boolean useCache) {
	if (useCache)
	    mCache = new HashMap<Node,Object>();
    }
    

    public static class Node {
	public static final Node ROOT= new Node();
	public String name;
	public Node parent;
	public List<Node> children;
	
	public Class<?> classe;
	public Object instance;
	
	public void addChild(Node c) {
	    if (children==null) children = new Vector<Node>();
	    children.add(c);
	    c.parent=this;
	}
	
	public boolean equals(Object o) {
	    if (o instanceof Node && ((Node)o).name.equals(this.name)) {
		Iterator<Node> thisChildren = children.iterator();
		Iterator<Node> otherChildren = ((Node)o).children.iterator();
		while (thisChildren.hasNext() && otherChildren.hasNext()) {
		   if (!thisChildren.next().equals(otherChildren.next()))
		       return false;
		}
		if (otherChildren.hasNext()||thisChildren.hasNext()) return false;
		return true;
	    }
	    return false;
	}
    }
    
    @SuppressWarnings("unchecked")
    public Object getInstance(String name, Node current, String basePackage) {
	int idx=0;
	while (idx<name.length() && name.charAt(idx)!='(' && name.charAt(idx)!=')' && name.charAt(idx)!=',') idx++;
	
	Node n=null;
	if (idx>0) {
	    n = new Node();
	    n.name=basePackage+name.substring(0, idx);
	    current.addChild(n);
	    String[] args = n.name.split("=");
	    try {
		n.classe = Class.forName(args[0]);
	    } catch (ClassNotFoundException e) {
		throw new OntoSimException(args[0]+" does not exist",e);
	    }
	    if (args.length>1)
		if (n.classe==String.class)
		    n.instance=args[1];
	    	else
	    	    n.instance=Enum.valueOf( ((Class<? extends Enum>) n.classe), args[1]);
	}
	

	if (idx<name.length()) {
	    if (name.charAt(idx)=='(') current=n;
	    else if (name.charAt(idx)==')') current=current.parent;
	    getInstance(name.substring(idx+1),current, basePackage);
	}
	if (n!= null && n.classe !=null && n.instance==null)
    		newInstance(n);
	if (current==Node.ROOT&&n!=null) return n.instance;
	//System.out.println(current.instance);
	return current.instance;
	
	
    }
    
    
    private Object newInstance(Node current)  {
	if (mCache !=null && mCache.containsKey(current))
	    return mCache.get(current);
	int nbParam = current.children==null?0:current.children.size();
	//System.out.println("Instancie : "+current.name+" - "+nbParam);
	if (nbParam==0) {
	    try {
		current.instance=current.classe.newInstance();
	    } catch (Exception e) {
		throw new OntoSimException("Cannot instanciate measure "+current.name,e);
	    }
	}
	Class<?>[] cs = new Class[nbParam];
	Object[] params = new Object[nbParam];
	for (int i=0 ; i<nbParam ; i++) {
	    cs[i]=current.children.get(i).classe;
	    params[i]=current.children.get(i).instance;
	}
	for (Constructor<?> cst : current.classe.getConstructors()) {
	    Class<?>[] types = cst.getParameterTypes();
	    if (types.length==nbParam) {
		try {
		    for (int i=0 ; i<nbParam ; i++) {
			cs[i].asSubclass(types[i]);
		    }
		    current.instance=cst.newInstance(params);

		}
		catch (ClassCastException e) {} 
		catch (Exception e) {
		    throw new OntoSimException("Cannot instanciate measure "+current.name,e);
		} 
	    }
	}
	if (current.instance==null) {
	    StringBuffer sb = new StringBuffer();
	    sb.append('(');
	    for (Class<?> c : cs) {
		sb.append(c.getCanonicalName());
		sb.append(',');
	    }
	    sb.deleteCharAt(sb.length()-1);
	    sb.append(')');
	    throw new OntoSimException("No Constructor "+current.classe.getSimpleName()+sb.toString()+" for class "+current.classe.getCanonicalName());
	}
	if (mCache !=null)
	    mCache.put(current, current.instance);
	//System.out.println(current.instance);
	return current.instance;
    }
    
    /**
     * Build an ontology measure by parsing name argument
     * name must have the following forms :
     * OntologySpaceMeasure(GlobalMeasure(LocalMeasure))
     * VectorSpaceMeasure(VectorMeasure,VectorType)
     * examples :
     * fr.inrialpes.exmo.ontosim.VectorSpaceMeasure(fr.inrialpes.exmo.ontosim.vector.CosineVM,fr.inrialpes.exmo.ontosim.vector.model.DocumentCollection$WEIGHT=TFIDF)
     * fr.inrialpes.exmo.ontosim.OntologySpaceMeasure(fr.inrialpes.exmo.ontosim.set.MaxCoupling(fr.inrialpes.exmo.ontosim.entity.EntityLexicalMeasure)) 
     * @param measureName
     */
    @SuppressWarnings("unchecked")
    public Measure<LoadedOntology<?>> getOntologyMeasure(String measureName) {
	   return (Measure<LoadedOntology<?>>) getInstance(measureName,Node.ROOT,"");
    }
    
    @SuppressWarnings("unchecked")
    public Measure<LoadedOntology<?>> getOntologyMeasure(String measureName,String basePackage) {

	    return (Measure<LoadedOntology<?>>) getInstance(measureName,Node.ROOT, basePackage);
    }
    
    
    public void clearCache() {
	if (mCache==null) return;
	mCache.clear();
    }
    
    private void remove(Node n, boolean recursive) {
	mCache.remove(n);
	if (recursive) {
	    for (Node c : n.children) {
		remove(c,recursive);
	    }
	}
    }
    
    public void clearCache(Object o, boolean recursive) {
	if (mCache==null) return;
	Node node=null;
	for (Map.Entry<Node,Object> e : mCache.entrySet()) {
	    if (e.getValue()==o) {
		node = e.getKey();
		break;
	    }
	}
	if (node!=null) remove(node,recursive); 
    }

    
}
