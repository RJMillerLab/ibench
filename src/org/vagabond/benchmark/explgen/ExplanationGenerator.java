package org.vagabond.benchmark.explgen;

import java.sql.Connection;

import org.vagabond.xmlmodel.explanderror.ExplanationAndErrorsDocument;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;

public interface ExplanationGenerator {

	void generateExpl(MappingScenario scenario, Connection dbCon,
			ExplanationAndErrorsDocument eDoc, Configuration conf);

//	public boolean requiredDBAccess ();
//	public void generate(Configuration conf, MappingScenario map);
	
}
