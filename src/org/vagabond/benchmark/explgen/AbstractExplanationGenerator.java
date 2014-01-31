package org.vagabond.benchmark.explgen;

import java.sql.Connection;

import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.xmlmodel.explanderror.ExplanationAndErrorsDocument;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants.DESErrorType;
import tresc.benchmark.utils.Utils;

public abstract class AbstractExplanationGenerator implements
		ExplanationGenerator {

	protected IExplanationSet e;
	protected int numExpl;

	@Override
	public void generateExpl(MappingScenario scenario, Connection dbCon,
			ExplanationAndErrorsDocument eDoc, Configuration conf) throws Exception {
		init(scenario, conf);
	}

	protected void init(MappingScenario scen, Configuration conf) {
		e = ExplanationFactory.newExplanationSet();
		scen.getErr().addSet(getType(), e);

		// determine number of explanations
		int paramNumExpls = conf.getGroundTruthValues(getType());
		int explDev = conf.getGroundTruthDev(getType());

		numExpl = Utils.getRandomNumberAroundSomething(conf
				.getRandomGenerator(), paramNumExpls, explDev);
	}

	protected abstract DESErrorType getType();

}
