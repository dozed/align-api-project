package fr.inrialpes.exmo.ontosim.extractor;

import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import fr.inrialpes.exmo.ontosim.Measure;
import fr.inrialpes.exmo.ontosim.extractor.matching.BasicMatching;
import fr.inrialpes.exmo.ontosim.extractor.matching.Matching;
import fr.inrialpes.exmo.ontosim.extractor.matching.Matching.Entry;

/**
 * To be revised : it doesn't work
 * @author jerome
 *
 */
public class GreedyExtractor extends AbstractExtractor {

    private static class EntryComp<T> implements Comparator<Entry<T>> {

	Measure<T> m;
	public EntryComp(Measure<T> m) {
	    this.m=m;
	}
	@Override
	public int compare(Entry<T> o1, Entry<T> o2) {
	    return Double.compare(	m.getMeasureValue(o1.getSource(), o2.getTarget()), 
		    			m.getMeasureValue(o2.getSource(), o2.getTarget()));
	}
	
    }
    
    private static <T> void add( Map<T,Set<Entry<T>>> m, T e , Entry<T> ent) {
	Set<Entry<T>> matchings = m.get(e);
	if (matchings==null) {
	    matchings = new HashSet<Entry<T>>();
	    m.put(e, matchings);
	}
	matchings.add(ent);
	
    }
    
    @Override
    public <O> Matching<O> extract(Measure<O> m, Set<? extends O> s, Set<? extends O> t) {
	Map<O,Set<Entry<O>>> map = new HashMap<O,Set<Entry<O>>>();
	EntryComp<O> comp = new EntryComp<O>(m);
	TreeSet<Entry<O>> ts = new TreeSet<Entry<O>>(comp);
	for (O src : s)
	    for (O trg : t) { 
		Entry<O> ent = new Entry<O>(src, trg);
		ts.add(ent);
		add(map,src,ent);
		add(map,trg,ent);
		Set<Entry<O>> lowers = ts.headSet(ent);
		Set<Entry<O>> toRem = new HashSet<Entry<O>>(map.get(src));
		toRem.retainAll(lowers);
		map.get(src).removeAll(lowers);
		lowers.removeAll(toRem);
		
		toRem = new HashSet<Entry<O>>(map.get(trg));
		toRem.retainAll(lowers);
		map.get(trg).removeAll(lowers);
		lowers.removeAll(toRem);
	    }
	Matching<O> matching = new BasicMatching<O>(ts);
	return matching;
	
    }

}
