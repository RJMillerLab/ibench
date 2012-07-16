package org.vagabond.benchmark.explgen;

import java.sql.Connection;

import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.xmlbeans.ExplanationAndErrorXMLLoader;
import org.vagabond.xmlmodel.explanderror.ExplanationAndErrorsDocument;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;

public class GlobalExplGen {

	// private ArrayList<ExplanationGenerator> explGens;

	public GlobalExplGen() {
		// explGens = new ArrayList<ExplanationGenerator> ();
		// // stores references to individual gens
		// CorrespondenceGen cg = new CorrespondenceGen();
		// SourceCopyGen scg = new SourceCopyGen();
		// SourceJoinGen sjg = new SourceJoinGen();
		// SourceSkeletonGen skg = new SourceSkeletonGen();
		// SuperfluousMappingGen smg = new SuperfluousMappingGen();
		// TargetSkeletonGen tsg = new TargetSkeletonGen();
		// explGens.add(cg);
		// explGens.add(scg);
		// explGens.add(sjg);
		// explGens.add(skg);
		// explGens.add(smg);
		// explGens.add(tsg);
	}

	public ExplanationAndErrorsDocument genearteExpls(MappingScenario scenario,
			Configuration configuration) throws Exception {

		// load to DB and creates Expl XML docu
		Connection dbCon = ConnectionManager.getInstance().getConnection(
				scenario.getDoc());
		ExplanationAndErrorsDocument eDoc = ExplanationAndErrorsDocument.Factory
				.newInstance();
		DatabaseScenarioLoader.getInstance().loadScenario(dbCon,
				scenario.getDoc());

		// call each gen
		for (ExplanationGenerator gen : Constants.errorGenerators.values())
			gen.generateExpl(scenario, dbCon, eDoc, configuration);
		// output

		eDoc = ExplanationAndErrorXMLLoader.getInstance().translateToXML(
				scenario.getErr().getAll());

		return eDoc;
	}

}
