package org.vagabond.benchmark.model;

public class IdGen {

	private int curId;
	
	public IdGen () {
		curId = 0;
	}
	
	public int getNextId() {
		return curId++;
	}
	
	public int getCurrId() {
		return curId;
	}
	
	public void reset () {
		curId = 0;
	}
	
}
