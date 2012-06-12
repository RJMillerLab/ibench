package tresc.benchmark.schemaGen;

import java.util.HashMap;
import java.util.Random;

import org.apache.log4j.Logger;
import org.vagabond.benchmark.model.TrampModelFactory;
import org.vagabond.benchmark.model.TrampXMLModel;
import org.vagabond.xmlmodel.CorrespondenceType;

import smark.support.MappingScenario;
import smark.support.PartialMapping;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Modules;
import tresc.benchmark.Constants.ScenarioName;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.schema.Schema;
import vtools.utils.structures.AssociativeArray;

/*
 * Each generator of a scenario case subclasses this class.
 */
public abstract class ScenarioGenerator {
	
	static Logger log = Logger.getLogger(ScenarioGenerator.class);
	
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
	
	protected int curRep;
	
	protected Schema source;
	protected Schema target;
	protected SPJQuery pquery;
	
	protected TrampModelFactory fac;
	protected TrampXMLModel model;
	
	public void generateScenario(MappingScenario scenario,
			Configuration configuration) throws Exception {
		init(configuration, scenario);
		initPartialMapping();
		log.debug("CREATE " + repetitions + " scenarios of type <" + getScenType() + ">");
		
		for (curRep = 0; curRep < repetitions; curRep++) {
			initPartialMapping();
			genSchemas();
			genCorrespondences();
			genMappings();
			genTransformations();
			scenario.get_basicScens().put(getScenType() + "_" + curRep, m);
			log.debug("Repetition <" + curRep +"> is " + m.toString());
		}
	}

	protected void genCorrespondences() {}
	
	protected void genMappings() throws Exception {}
	
	protected void genTransformations() throws Exception {}
	
	protected void genSchemas() throws Exception {
		// TODO switch on schema reuse
		genSourceRels();
		genTargetRels();
	}

	protected abstract void genSourceRels() throws Exception;

	protected abstract void genTargetRels() throws Exception;

	protected void init(Configuration configuration,
			MappingScenario mappingScenario) {
		this.scen = mappingScenario;
		model = scen.getDoc();
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

	public String getStamp() {
		return Constants.nameForScenarios.get(getScenType()).substring(1);
	}
	
	protected String randomRelName(int relNum) {
		String randomName = Modules.nameFactory.getARandomName();
		String name =
				randomName + "_" + getStamp() + curRep + "NL"
						+ 0 + "CE" + relNum;
		return name.toLowerCase();
	}
	
	protected String randomAttrName(int relNum, int attrNum) {
		String randomName = Modules.nameFactory.getARandomName();
		String name = randomName + "_" + getStamp() + curRep + "NL"
						+ relNum + "AE" + attrNum;
		return name.toLowerCase();
	}
	
	protected String getAttrHook (int relNum, int attrNum) {
		return getStamp() + curRep + "NL" + relNum + "AE" + attrNum;
	}
	
	protected String getRelHook (int relNum) {
		return getStamp() + curRep + "NL" + 0 + "CE" + relNum;
	}
	
	protected void addCorr (int sRel, int sAttr, int tRel, int tAttr) {
		String toRel = m.getTargetRels().get(tRel).getName();
		String fromRel = m.getSourceRels().get(sRel).getName();
		String fromAttr = model.getRelAttr(sRel, sAttr, true);
		String toAttr = model.getRelAttr(tRel, tAttr, false);
		CorrespondenceType c = fac.addCorrespondence(fromRel, fromAttr, toRel, toAttr);
		m.addCorr(c);
	}
}
