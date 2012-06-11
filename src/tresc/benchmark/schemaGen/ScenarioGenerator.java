package tresc.benchmark.schemaGen;

import java.util.HashMap;
import java.util.Random;

import org.vagabond.benchmark.model.TrampModelFactory;

import smark.support.MappingScenario;
import smark.support.PartialMapping;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Constants.ScenarioName;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.schema.Schema;
import vtools.utils.structures.AssociativeArray;

/*
 * Each generator of a scenario case subclasses this class.
 */
public abstract class ScenarioGenerator {
	protected final String _attributes = "abcdefghijklmnopqrstuvwxyz"; // Can
																		// only
																		// hold
																		// less
																		// than
																		// 26
																		// attributes
																		// in
																		// one
																		// mapping

	protected HashMap<String, Character> attrMap =
			new HashMap<String, Character>();
	protected PartialMapping m = null;
	protected Random _generator;
	protected MappingScenario scen;
	protected Configuration configuration;

	protected int repetitions;
	protected int numOfElements;
	protected int numOfElementsDeviation;
	protected int nesting;
	protected int nestingDeviation;
	protected int numOfSetElements;
	protected int numOfSetElementsDeviation;
	protected int keyWidth;
	protected int keyWidthDeviation;
	protected int joinKind;	
	protected int numOfParams;
	protected int numOfParamsDeviation;
	
	protected Schema source;
	protected Schema target;
	protected SPJQuery pquery;
	
	protected TrampModelFactory fac;






	public void generateScenario(MappingScenario scenario,
			Configuration configuration) throws Exception {
		init(configuration, scenario);
		initPartialMapping();

		for (int i = 0; i < repetitions; i++) {
			initPartialMapping();
			genSchemas();
			genMapsAndTrans();
			scenario.get_basicScens().put(getScenType() + "_" + i, m);
		}
	}

	protected abstract void genMapsAndTrans();

	protected void genSchemas() {
		// TODO switch on schema reuse
		genSourceRels();
		genTargetRels();
	}

	protected abstract void genSourceRels();

	protected abstract void genTargetRels();

	protected void init(Configuration configuration,
			MappingScenario mappingScenario) {
		this.scen = mappingScenario;
		fac = scen.getDocFac();
		m = null;
		_generator = configuration.getRandomGenerator();

		repetitions =
				configuration.getScenarioRepetitions(getScenType().ordinal());
		numOfElements = configuration
						.getParam(Constants.ParameterName.NumOfSubElements);
		numOfElementsDeviation =
				configuration
						.getDeviation(Constants.ParameterName.NumOfSubElements);
		nesting = configuration.getParam(Constants.ParameterName.NestingDepth);
		nestingDeviation = configuration
						.getDeviation(Constants.ParameterName.NestingDepth);
		numOfSetElements =
				configuration.getParam(Constants.ParameterName.JoinSize);
		numOfSetElementsDeviation =
				configuration.getDeviation(Constants.ParameterName.JoinSize);
        keyWidth = configuration.getParam(Constants.ParameterName.NumOfJoinAttributes);
        keyWidthDeviation = configuration.getDeviation(Constants.ParameterName.NumOfJoinAttributes);
        joinKind = configuration.getParam(Constants.ParameterName.JoinKind);
        numOfParams = configuration.getParam(Constants.ParameterName.NumOfParamsInFunctions);
        numOfParamsDeviation = configuration.getDeviation(Constants.ParameterName.NumOfParamsInFunctions);

		
		source = scen.getSource();
		target = scen.getTarget();
		pquery = scen.getTransformation();

	}

	protected void initPartialMapping() {
		m = new PartialMapping();
	}

	public abstract ScenarioName getScenType();

	public int getNestingDeviation() {
		return nestingDeviation;
	}

	public void setNestingDeviation(int nestingDeviation) {
		this.nestingDeviation = nestingDeviation;
	}

	protected int getRepetitions() {
		return repetitions;
	}

	protected void setRepetitions(int repetitions) {
		this.repetitions = repetitions;
	}

	protected int getNumOfElements() {
		return numOfElements;
	}

	protected void setNumOfElements(int numOfElements) {
		this.numOfElements = numOfElements;
	}

	protected int getNumOfElementsDeviation() {
		return numOfElementsDeviation;
	}

	protected void setNumOfElementsDeviation(int numOfElementsDeviation) {
		this.numOfElementsDeviation = numOfElementsDeviation;
	}

	protected int getNesting() {
		return nesting;
	}

	protected void setNesting(int nesting) {
		this.nesting = nesting;
	}

}
