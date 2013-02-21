package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.benchmark.model.TrampModelFactory;
import org.vagabond.benchmark.model.TrampXMLModel;
import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelationType;

import smark.support.MappingScenario;
import smark.support.PartialMapping;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Constants.MappingLanguageType;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Constants.TrampXMLOutputSwitch;
import tresc.benchmark.utils.Utils;
import tresc.benchmark.Modules;
import vtools.dataModel.expression.Query;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.Union;
import vtools.dataModel.schema.Schema;

/*
 * Each generator of a scenario case subclasses this class.
 */

// PRG RESTORED Reading Deviation Parameter (numOfElementsDeviation) from config file - August 28, 2012
// PRG MOVED method getRandomSourceVars(int numArgsForSkolem, MappingType m1) to tresc.benchmark.utils.Utils.java to facilitate code reuse - Sep 21, 2012

public abstract class AbstractScenarioGenerator implements ScenarioGenerator {
	
	static Logger log = Logger.getLogger(AbstractScenarioGenerator.class);
	
//	protected final String _attributes = "abcdefghijklmnopqrstuvwxyz"; // Can
																		// only
																		// hold
																		// less
																		// than
																		// 26
																		// attributes
																		// in
																		// one
																		// mapping

//	protected HashMap<String, Character> attrMap =
//			new HashMap<String, Character>();
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
	protected int numNewAttr;
	protected int numNewAttrDeviation;
	protected int typeOfSkolem;
	protected int numRemovedAttr;
	protected int numRemovedAttrDeviation;
	protected int srcReusePerc;
	protected int trgReusePerc;
	protected int srcFDPerc;
	protected int primaryKeySize;
	protected int primaryKeySizeDeviation;
	
	protected MappingLanguageType mapLang;
	protected int curRep;
	protected boolean doSchemaElReuse = false;
	protected Schema source;
	protected Schema target;
	protected SPJQuery pquery;
	
	protected TrampModelFactory fac;
	protected TrampXMLModel model;
	protected static String[] stamps;
	
	static {
		stamps = new String[ScenarioName.values().length];
		Set<String> s = new HashSet<String> ();
		
		for(int i = 0; i < ScenarioName.values().length; i++) {
			ScenarioName n = ScenarioName.values()[i];
			String stamp = Constants.nameForScenarios.get(n);
					
			if (stamp == null && s.contains(stamp)) {
				stamp =	"_" + n.toString().substring(0, 1).toUpperCase();
				int pos = 1;
				
				while (s.contains(stamp)) {
					stamp = "_" + n.toString().substring(0, pos++).toUpperCase();
				}
			}
			s.add(stamp);
			stamps[i] = stamp;
		}
	}
	
	
	/**
	 * Creates one instance of this basic scenario or returns if
	 * there have already been created "repetitions" number of instances.
	 * @param scenario
	 * @param configuration
	 * @throws Exception 
	 */
	public void generateNextScenario(MappingScenario scenario, 
			Configuration configuration) throws Exception {
		if (curRep++ < repetitions) {
			if (log.isDebugEnabled()) {log.debug("CREATE " + curRep + "th scenario of type <" 
					+ getScenType() + ">");};
			// already created enough basic scenarios to start reusing?
			doSchemaElReuse = scenario.getNumBasicScen() 
					>= configuration.getReuseThreshold();
			createOneInstanceOfScenario(scenario, configuration);
		}
	}
	
	/**
	 *  call to generate "repetitions" number of instances of this basic
	 *  scenario at once
	 *  
	 * @param scenario
	 * @param configuration
	 * @throws Exception 
	 */
	@Override
	public void generateScenario(MappingScenario scenario,
			Configuration configuration) throws Exception {
		init(configuration, scenario);
		if (log.isDebugEnabled()) {log.debug("CREATE " + repetitions + " scenarios of type <" + getScenType() + ">");};
		
		for (curRep = 0; curRep < repetitions; curRep++)
			createOneInstanceOfScenario(scenario, configuration);
	}

	private void createOneInstanceOfScenario(MappingScenario scenario,
			Configuration configuration) throws Exception {
		initPartialMapping();
		genSchemas();
		if (log.isDebugEnabled()) {log.debug("Repetition <" + curRep +">");};
		if (log.isDebugEnabled()) {log.debug("\n\nGENERATED SCHEMAS: \nSOURCE:\n" + m.getSourceRels().toString() 
				+ "\n\nTARGET:\n" + m.getTargetRels().toString());};
		if (configuration.getTrampXMLOutputOption(TrampXMLOutputSwitch.Correspondences)) {
			genCorrespondences();
			if (log.isDebugEnabled()) {log.debug("\n\nGENERATED CORRS: \n" + m.getCorrs().toString());};
		}
		genMappings();
		if (log.isDebugEnabled()) {log.debug("\n\nGENERATED MAPS: \n" + m.getMaps().toString());};
		if (configuration.getTrampXMLOutputOption(TrampXMLOutputSwitch.Transformations)) {
			genTransformations();
			if (log.isDebugEnabled()) {log.debug("\n\nGENERATED TRANS: \n" + m.getTrans().toString());};
		}
		scenario.get_basicScens().put(getScenType() + "_" + curRep, m);
		//if (log.isDebugEnabled()) {log.debug("Repetition <" + curRep +"> is " + m.toString());};
	}

	protected abstract void genCorrespondences();
	
	protected abstract void genMappings() throws Exception;
	
	protected abstract void genTransformations() throws Exception;
	
	protected void genSchemas() throws Exception {
		// create new schema elements for the scenario instance
		if (!doSchemaElReuse) {
			genSourceRels();
			genTargetRels();
		}
		// roll dice to determine whether source or target are reused
		else {
			boolean reuseSrc = _generator.nextInt(100) <= srcReusePerc;
			boolean reuseTrg = _generator.nextInt(100) <= trgReusePerc;
			
			// check whether at least one target or source relation exists when reusing
			if (reuseSrc && model.getSchema(true).sizeOfRelationArray() == 0)
				reuseSrc = false;
			if (reuseTrg && model.getSchema(false).sizeOfRelationArray() == 0)
				reuseTrg = false;
			
			// do not both reuse source and target
			if (reuseSrc && reuseTrg) {
				if (_generator.nextBoolean())
					reuseSrc = false;
				else
					reuseTrg = false;
			}
			
			// first choose a source relations and then generate
			// the target accordingly
			if (reuseSrc) {
				if (!chooseSourceRels())
					genSourceRels();
				genTargetRels();
			}
			// first choose a target relation and then generate
			// the source accordingly
			else if (reuseTrg) {
				boolean success = chooseTargetRels();
				genSourceRels();
				if (!success)
					genTargetRels();
			}
			// generate source then target
			else {
				genSourceRels();
				genTargetRels();
			}	
		}
	}

	/**
	 * Pick the source relations for the scenario from the 
	 * source schema created so far.
	 * 
	 * @throws Exception
	 */
	protected boolean chooseSourceRels() throws Exception {
		RelationType rel = getRandomRel(true);
		m.addSourceRel(rel);
		return true;
	}
	
	/**
	 * Pick the target relations for the scenario from the
	 * target schema created so far.
	 * @throws Exception
	 */
	protected boolean chooseTargetRels() throws Exception {
		RelationType rel = getRandomRel(false);
		m.addTargetRel(rel);
		return true;
	}
	
	protected RelationType getRandomRel (boolean source, int minAttrs) {
		return getRandomRel(source, minAttrs, false);//TODO use xpath over XBeans?		
	}
	
	protected RelationType getRandomRel (boolean source, int minAttrs, int maxAttrs) {
		return getRandomRel(source, minAttrs, maxAttrs, false);		
	}
	
	
	protected RelationType getRandomRel (boolean source, int minAttrs, boolean key) {
		return getRandomRel(source, minAttrs, Integer.MAX_VALUE, key);
	}
	
	protected RelationType getRandomRel (boolean source, int minAttrs, int maxAttr, boolean key) {
		List<RelationType> cand = new ArrayList<RelationType> ();
		
		for(RelationType r: model.getSchema(source).getRelationArray()) {
			boolean ok = !key || r.isSetPrimaryKey();
			ok &= r.sizeOfAttrArray() >= minAttrs;
			ok &= r.sizeOfAttrArray() <= maxAttr;
			if (ok)
				cand.add(r);
		}
		
		return pickRel(cand);//TODO use xpath over XBeans?
	}
	
	protected RelationType getRandomRelWithNumAttr (boolean source, int numAttr, 
			Collection<RelationType> notThese) {
		List<RelationType> cand = new ArrayList<RelationType> ();
		
		for(RelationType r: model.getSchema(source).getRelationArray()) {
			if (r.sizeOfAttrArray() == numAttr)
				cand.add(r);
		}
		// remove the ones not allowed
		cand.removeAll(notThese);
		
		return pickRel(cand);
	}
	
	protected RelationType getRandomRelWithNumAttr (boolean source, int numAttr) {
		List<RelationType> cand = new ArrayList<RelationType> ();
		
		for(RelationType r: model.getSchema(source).getRelationArray()) {
			if (r.sizeOfAttrArray() == numAttr)
				cand.add(r);
		}
		
		return pickRel(cand);
	}
	
	private RelationType pickRel (List<RelationType> rels) {
		int numRels = rels.size();
		if (numRels == 0)
			return null;
		int pos = _generator.nextInt(numRels);
		return rels.get(pos);
	}
	
	protected RelationType getRandomRel (boolean source) {
		int numRels = model.getNumRels(source);
		if (numRels == 0)
			return null;
		int pos = _generator.nextInt(numRels);
		RelationType rel = model.getRel(pos, source);
		return rel;
	}
	
	protected abstract void genSourceRels() throws Exception;

	protected abstract void genTargetRels() throws Exception;

	public void init(Configuration configuration,
			MappingScenario mappingScenario) {
		this.scen = mappingScenario;
		model = scen.getDoc();
		fac = scen.getDocFac();
		m = null;
		_generator = configuration.getRandomGenerator();
		this.configuration = configuration;
		
		// get parameters from configuration
		repetitions = configuration.getScenarioRepetitions(getScenType().ordinal());
		numOfElements = configuration.getParam(Constants.ParameterName.NumOfSubElements);
		// PRG Restored reading deviation parameter from config file - August 28, 2012
		// numOfElementsDeviation = 0;
		numOfElementsDeviation = configuration.getDeviation(Constants.ParameterName.NumOfSubElements);
		nesting = configuration.getParam(Constants.ParameterName.NestingDepth);
		nestingDeviation = configuration.getDeviation(Constants.ParameterName.NestingDepth);
		
		numOfSetElements = configuration.getParam(Constants.ParameterName.JoinSize);
		numOfSetElementsDeviation = configuration.getDeviation(Constants.ParameterName.JoinSize);
		
		keyWidth = configuration.getParam(Constants.ParameterName.NumOfJoinAttributes);
		keyWidthDeviation = configuration.getDeviation(Constants.ParameterName.NumOfJoinAttributes);
		
		joinKind = configuration.getParam(Constants.ParameterName.JoinKind);
		
		numOfParams = configuration.getParam(Constants.ParameterName.NumOfParamsInFunctions);
		numOfParamsDeviation = configuration.getDeviation(Constants.ParameterName.NumOfParamsInFunctions);
		
		numNewAttr = configuration.getParam(Constants.ParameterName.NumOfNewAttributes);
		numNewAttrDeviation = configuration.getDeviation(Constants.ParameterName.NumOfNewAttributes);
		
		typeOfSkolem = configuration.getParam(Constants.ParameterName.SkolemKind);
		
		numRemovedAttr = configuration.getParam(Constants.ParameterName.NumOfAttributesToDelete);
		numRemovedAttrDeviation = configuration.getDeviation(Constants.ParameterName.NumOfAttributesToDelete);
		
		srcReusePerc = configuration.getParam(Constants.ParameterName.ReuseSourcePerc);
		trgReusePerc = configuration.getParam(Constants.ParameterName.ReuseTargetPerc);
		
		primaryKeySize = configuration.getParam(Constants.ParameterName.PrimaryKeySize);
		primaryKeySizeDeviation = configuration.getDeviation(Constants.ParameterName.PrimaryKeySize);
        
		
        mapLang = configuration.getMapType();

		source = scen.getSource();
		target = scen.getTarget();
		pquery = scen.getTransformation();

		curRep = 0;
		
		scenarioSanityCheck();
	}

	protected void scenarioSanityCheck() {
		
	}

	protected void initPartialMapping() {
		m = new PartialMapping();
		fac.setPartialMapping(m);
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
		return stamps[getScenType().ordinal()];
	}
	
	// PRG MOVED method getRandomSourceVars(int numArgsForSkolem, MappingType m1) to tresc.benchmark.utils.Utils.java to facilitate code reuse - Sep 21, 2012
	// (renamed it as getRandomWithoutReplacementSequence() so that we can invoked in other occasions as well)
	
	/*
	protected Vector<String> getRandomSourceVars(int numArgsForSkolem, MappingType m1) {
		Vector<String> randomArgs = new Vector<String> ();
		Vector<String> allVars = new Vector<String> (); 
		HashSet<String> varSet = new HashSet<String> ();
		
		model.getAllVarsInMapping(m1, true, allVars, varSet);
		
		// not enough source vars?
		if (numArgsForSkolem >= varSet.size()) {
			randomArgs = new Vector<String> (varSet);
			Collections.sort(randomArgs);
			return randomArgs;
		}
		
		// randomly select source vars until we have enough. Remove chosen vars to guarantee convergence.
		for(int i = 0; i < numArgsForSkolem; i++) {
			int pos = Utils.getRandomUniformNumber(_generator, allVars.size());
			String var = allVars.get(pos);
			randomArgs.add(var);
			
			// remove all occurances of var
			varSet.remove(var);
			allVars.removeAll(Collections.singleton(var));
		}
		
		Collections.sort(randomArgs);
//		for (int i = 0; i < numArgsForSkolem; i++) {
//
//			while (!ok & attempts++ < MaxRandomTries) {
//				
//				// Get random position 
//				int pos = Utils.getRandomNumberAroundSomething(_generator, numOfSrcTblAttr / 2, numOfSrcTblAttr / 2);
//				// Adjust random position value just in case it falls outside limits
//				pos = (pos >= numOfSrcTblAttr) ? numOfSrcTblAttr - 1 : pos;
//				
//				// Make sure we have not already added this variable before
//				// If so, attempt to get another random position up to a max of 30 tries
//				if (randomArgs.indexOf(fac.getFreshVars(pos, 1)[0]) == -1) {
//					randomArgs.add(fac.getFreshVars(pos, 1)[0]);
//					ok = true;
//				    break;
//				}
//				
//			}
//			// Plainly give up after 30 tries. If so, we may end up with an argument set with fewer variables.
//		
//		}
		
		return randomArgs;
	}
	*/
		
	protected RelationType createFreeRandomRel (int relId, int numAttr) {
		RelationType r = RelationType.Factory.newInstance();
		r.setName(randomRelName(relId));
		for(int i = 0; i < numAttr; i++) {
			AttrDefType a = r.addNewAttr();
			a.setName(randomAttrName(relId, i));
			a.setDataType("TEXT");
		}
		
		return r;
	}
	
	protected String randomRelName(int relNum) {
		String randomName = Modules.nameFactory.getSafeRandomName();
		String name =
				randomName  + getStamp() + "_" + curRep +  "_NL"
						+ 0 + "_CE" + relNum;
		name = name.toLowerCase();
		
		while(model.hasRelName(name))
			name += "m";
		
		return name;
	}
	
	protected String randomAttrName(int relNum, int attrNum) {
		String randomName = Modules.nameFactory.getSafeRandomName();
		String name = randomName + getStamp() + "_" + curRep + "_NL"
						+ relNum + "_AE" + attrNum;
		return name.toLowerCase();
	}
	
	protected String getAttrHook (int relNum, int attrNum) {
		return getStamp() + curRep + "_NL" + relNum + "_AE" + attrNum;
	}
	
	protected String getRelHook (int relNum) {
		return getStamp() + curRep + "NL" + 0 + "CE" + relNum;
	}
	
	protected void addCorr (int sRel, int sAttr, int tRel, int tAttr) {
		String toRel = m.getTargetRels().get(tRel).getName();
		String fromRel = m.getSourceRels().get(sRel).getName();
		String fromAttr = m.getAttrId(sRel, sAttr, true);
		String toAttr = m.getAttrId(tRel, tAttr, false);
		fac.addCorrespondence(fromRel, fromAttr, toRel, toAttr);
	}
	
	protected void addFK (int fRel, String[] fAttr, int tRel, String[] tAttr, boolean source) {
		String fromRel = m.getRelName(fRel, source);
		String toRel = m.getRelName(tRel, source);
		fac.addForeignKey(fromRel, fAttr, toRel, tAttr, source);
	}
	
	protected void addFK (int fRel, int fAttr, int tRel, int tAttr, boolean source) {
		String fromRel = m.getRelName(fRel, source);
		String fromAttr = m.getAttrId(fRel, fAttr, source);
		String toRel = m.getRelName(tRel, source);
		String toAttr = m.getAttrId(tRel, tAttr, source);
		fac.addForeignKey(fromRel, fromAttr, toRel, toAttr, source);
	}
	
	protected Query addQueryOrUnion (String tName, Query q) {
		SelectClauseList pselect = pquery.getSelect();
		String tblTrgName = tName;
		m.addQuery(q);
		
		// if exists merge into one query //TODO outsource this to provide different types of merging?
		if (pselect.getTerm(tName) != null) {
			Union u;
			Query other = (Query) pselect.getValue(tName);
			if (other instanceof Union) {
				u = (Union) other;
				u.add(q);
			}
			else {
				u = new Union();
				u.add(other);
				u.add(q);
				pselect.setValueOf(tName, u);
			}
			return u;
		}
		else {
			pselect.add(tblTrgName, q);
			pquery.setSelect(pselect);
			return q;
		}
	}
}
