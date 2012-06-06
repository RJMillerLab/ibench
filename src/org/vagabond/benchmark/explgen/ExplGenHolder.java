package org.vagabond.benchmark.explgen;


import java.util.ArrayList;
import java.util.List;

import tresc.benchmark.Constants;
import tresc.benchmark.Constants.DESErrorType;

public class ExplGenHolder {

	public static ExplGenHolder instance = new ExplGenHolder();
	private List<ExplanationGenerator> gens;
	
	public ExplGenHolder () {
		gens = new ArrayList<ExplanationGenerator> ();
		
		for(DESErrorType e: Constants.DESErrorType.values()) {
			gens.add(Constants.errorGenerators.get(e));
		}
	}
	
	
	
}
