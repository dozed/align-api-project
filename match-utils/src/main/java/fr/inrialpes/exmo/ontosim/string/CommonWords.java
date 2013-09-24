/*
 * $Id$
 *
 * Copyright (C) Université Pierre Mendes-France, 2009-2010
 *
 * This program was originaly part of the Alignment API implementation
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307 USA
 */

package fr.inrialpes.exmo.ontosim.string;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.StopAnalyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.snowball.SnowballAnalyzer;
import org.apache.lucene.analysis.tokenattributes.TermAttribute;
import org.apache.lucene.util.Version;

import fr.inrialpes.exmo.ontosim.Measure;


public class CommonWords implements Measure<String> {

    private Analyzer analyzer = new SnowballAnalyzer(Version.LUCENE_30,"English",StopAnalyzer.ENGLISH_STOP_WORDS_SET);
    private Map<String, Set<String>> map = Collections.synchronizedMap(new HashMap<String, Set<String>>());

    
    public double getDissim(String o1, String o2) {
	return 1-getMeasureValue(o1,o2);
    }


    public fr.inrialpes.exmo.ontosim.Measure.TYPES getMType() {
	return Measure.TYPES.similarity;
    }

    public double getMeasureValue(String o1, String o2) {
	if (!map.containsKey(o1))
	    extractTerms(o1);
	if (!map.containsKey(o2))
	    extractTerms(o2);
	
	Set<String> s1 = map.get(o1);
	Set<String> s2 = map.get(o2);
	if (s1.size()==0 || s2.size()== 0)
	    return 0;
	
	int i=0;
	for (String s : s1) {
	    if (s2.contains(s)) i++;
	}

	return ((double) i) / (Math.max(s1.size(), s2.size()));
    }


    public double getSim(String o1, String o2) {
	return getMeasureValue(o1, o2);
    }

    private void extractTerms(String e) {
	Set<String> s = new LinkedHashSet<String>();
	TokenStream ts = analyzer.tokenStream("", new StringReader(e));
	TermAttribute termAtt = ts.addAttribute(TermAttribute.class);
	try {
	    while ( ts.incrementToken() ) {
		s.add( termAtt.term() );
	    }
	} catch (IOException ex) {
		ex.printStackTrace();
	}
	/*
	Token token;
	try {
	    while ((token = ts.next()) != null) {
		s.add(token.termText());
	    }
	} catch (IOException ex) {
		ex.printStackTrace();
	}
	*/
	map.put(e, s);
    }
}
