package fr.inrialpes.exmo.ontosim.extractor.matching;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;




public class BasicMatching<O> implements Matching<O> {

    /*private List<O> src;
    private List<O> trg;*/
    
    private HashMap<O,Set<O>> entries;
    private Collection<Entry<O>> entriesList;
    
    public BasicMatching() {
	entries=new HashMap<O,Set<O>>();
	entriesList=new LinkedList<Entry<O>>();
    }
    
    public BasicMatching(Collection<Entry<O>> s) {
	entriesList=s;
	for (Entry<O> e : s) {
	    Set<O> sTrg = entries.get(e.getSource());
	    if (sTrg==null) {
		sTrg=new HashSet<O>();
		entries.put(e.getSource(), sTrg);
	    }
	    sTrg.add(e.getTarget());
	}
    }
    
    public void addAll(Set<Entry<O>> entries) {
	for (Entry<O> ent : entries)
	    add(ent);
    }
    
    public boolean add(Entry<O> entry) {
	Set<O> sTrg = entries.get(entry.getSource());
	if (sTrg==null) {
	    sTrg=new HashSet<O>();
	    entries.put(entry.getSource(), sTrg);
	}
	
	if (sTrg.add(entry.getTarget())) {
	    entriesList.add(entry);//new Entry<O>(s,t));
	    return true;
	}
	return false;
    }
    
    
    public boolean add(O s, O t) {
	return add(new Entry<O>(s,t));
    }

    public void clear() {
	entries.clear();
	entriesList.clear();
    }

   

    /**
     * To be modified
     */
    public Matching<O> transposeView() {
	return new BasicMatching<O>() {
	    public boolean add(O s, O t) {
		return BasicMatching.this.add(t, s);
	    }
	};
    }

    public int size() {
	return entriesList.size();
    }

    public boolean contains(O s, O t) {
	return entries.containsKey(s) && entries.get(s).contains(t);
    }

    public Iterator<Entry<O>> iterator() {
	return entriesList.iterator();
    }




}
