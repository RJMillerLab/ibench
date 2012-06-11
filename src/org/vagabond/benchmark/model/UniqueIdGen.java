package org.vagabond.benchmark.model;

import java.util.HashMap;
import java.util.Map;

import org.vagabond.util.Pair;

public class UniqueIdGen {
	
	private Map<String, Pair<String, Integer>> curIdVals;
	
	public UniqueIdGen () {
		curIdVals = new HashMap<String, Pair<String, Integer>> ();
	}
	
	public void createIdType (String name, String idPrefix) {
		curIdVals.put(name, new Pair<String, Integer> (idPrefix, 0));
	}
	
	public String createId (String name) {
		Pair<String, Integer> cur = curIdVals.get(name);
		cur.setValue(cur.getValue() + 1);
		return cur.getKey() + (cur.getValue() - 1);
	}
	
	public int getNumIds (String name) {
		return curIdVals.get(name).getValue();
	}
	
}
