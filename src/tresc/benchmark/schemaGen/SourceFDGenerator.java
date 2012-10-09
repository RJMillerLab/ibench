package tresc.benchmark.schemaGen;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.util.CollectionUtils;
import org.vagabond.xmlmodel.FDType;
import org.vagabond.xmlmodel.RelationType;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Constants.ParameterName;
import tresc.benchmark.utils.Utils;

/**
 * Randomly generates functional dependencies after all scenarios have been
 * generated.
 * 
 * @author mdangelo
 */
public class SourceFDGenerator implements ScenarioGenerator 
{
	
	private class FD {
		
		public String relName;
		public String[] lhs;
		public String[] rhs;
		public String toString = null;
		
		public FD (String relName, String[] lhs, String[] rhs) {
			this.relName = relName;
			this.lhs = lhs;
			this.rhs = rhs;
		}
		
		@Override
		public String toString () {
			if (toString == null) {
				toString = relName + "|" +  Arrays.toString(lhs) + "|" 
						+ Arrays.toString(rhs);
			}
			return toString;
		}
	}
	
	static Logger log = Logger.getLogger(SourceFDGenerator.class);
	
	@Override
	public void generateScenario(MappingScenario scenario,Configuration configuration) throws Exception 
	{
		Random _generator = configuration.getRandomGenerator();
		Map<String, Map<String,FD>> fds = new HashMap<String, Map<String,FD>> ();
		
		if (log.isDebugEnabled()) {log.debug("Attempting to Generate Source FDs");};

		// create PK FDs R(A,B,C) if A is key then add A -> B,C
		if (configuration.getParam(ParameterName.PrimaryKeyFDs) == 1) {
			if (log.isDebugEnabled()) {log.debug("Generating PK FDs for those Relations with PKs");};
			generatePKFDs(scenario, fds);
		}

		if (configuration.getParam(Constants.ParameterName.SourceFDPerc) > 0) {
			if (log.isDebugEnabled()) {log.debug("Generating Random FDs as SourceFDPerc > 0");};	
			generateRandomFDs(scenario, configuration, _generator, fds);
		}
	
	}

	private void generateRandomFDs(MappingScenario scenario,
			Configuration configuration, Random _generator,
			Map<String, Map<String,FD>> fds) throws Exception {
		// create random FDs by randomly selecting non primary key attributes
		// and linking them
		for (RelationType r : scenario.getDoc().getSchema(true).getRelationArray()) 
		{
			// determine how many FDs we should generate for the relation based
			// on the number of attributes it has
			double percentage = ((double) configuration.getParam(Constants.ParameterName.SourceFDPerc))/(double) 100;
			int numAtts = r.getAttrArray().length;
			int numFDs = (int) Math.floor(percentage * numAtts);
			
			if (log.isDebugEnabled()) {log.debug("Attempting to Generate <" + numFDs + "> Random FDs for Relation " + r.getName());};

			// get positions for all of the attributes
			int[] attrPos = CollectionUtils.createSequence(0, r.sizeOfAttrArray()); 

			// if there is a primary key then we must strip out the attributes
			// associated with it, otherwise we just grab all the attributes
			String[] nonKeyAttrs = (r.isSetPrimaryKey()) ? getNonKeyAttributes(r, scenario) : scenario.getDoc().getAttrNames(r.getName(),attrPos, true);
			String[] allAttrs = scenario.getDoc().getAttrNames(r.getName(), true);
			String[] pkAttrs = scenario.getDoc().getPK(r.getName(), true);
			int pkSize = pkAttrs == null ? 0 : pkAttrs.length;
			
			// randomly select attributes for each run of FD generation
			for (int i = 0; i < numFDs; i++) {
				int numLHSAtts = _generator.nextInt(allAttrs.length / 2) + 1;
				Set<String> noKeySet = CollectionUtils.makeSet(nonKeyAttrs);
				Set<String> pkSet = CollectionUtils.makeSet(pkAttrs);
				Vector<String> LHSAtts;
				String RHSAtt;
				boolean done = false;
			
				int max_tries = 10;
				while (!done && max_tries > 0) {
					// pick LHS and single RHS attr from all attributes
					LHSAtts = Utils.getRandomWithoutReplacementSequence(
							_generator, numLHSAtts + 1, allAttrs);
					RHSAtt = LHSAtts.remove(LHSAtts.size() - 1);

					// check that we haven't choosen the whole key
					// if we are using the whole key in the LHS then remove the key and
					// add another non-key attribute
					if (pkSize != 0) {
						for(String att: LHSAtts) {
							pkSet.remove(att);
							noKeySet.remove(att);
						}
	
						if (pkSet.isEmpty()) {
							String[] noKeyLeft = noKeySet.toArray(new String[noKeySet.size()]);
							LHSAtts.remove(pkAttrs[_generator.nextInt(pkSize)]);
							LHSAtts.add(noKeyLeft[_generator.nextInt(noKeyLeft.length)]);
						}
					}
					
					// sort LHS
					Collections.sort(LHSAtts);
					String[] arrayLHS = Utils.convertVectorToStringArray(LHSAtts);

					log.trace("Potential FDs, must be checked for duplicates");
					log.trace("LHS: " + LHSAtts.toString());
					log.trace("RHS: " + RHSAtt);
					
					// check whether we have found one
					if (addFD(fds, r.getName(), arrayLHS, new String[] {RHSAtt})) {
						scenario.getDocFac().addFD(r.getName(), arrayLHS,
								new String[] { RHSAtt });
						done = true;
						if (log.isDebugEnabled()) {log.debug("---------NEW FD---------");};

						if (log.isDebugEnabled()) {log.debug("relName: " + r.getName());};
						if (log.isDebugEnabled()) {log.debug("LHS: " + LHSAtts.toString());};
						if (log.isDebugEnabled()) {log.debug("RHS: " + RHSAtt);};
					}
					else
						max_tries--;
				}
			}
		}
	}

	private void generatePKFDs(MappingScenario scenario, 
			Map<String, Map<String,FD>> fds) throws Exception {
		for (RelationType r : scenario.getDoc().getSchema(true).getRelationArray())
		{	
			if (r.isSetPrimaryKey()) 
			{
				String[] pkAttrs = scenario.getDoc().getPK(r.getName(), true);
				String[] nonKeyAttrs = getNonKeyAttributes(r, scenario);
				
				scenario.getDocFac().addFD(r.getName(), pkAttrs, nonKeyAttrs);
				addFD (fds, r.getName(), pkAttrs, nonKeyAttrs);
				
				// convert to vector to facilitate printing
		        List<String> pkList = Arrays.asList(pkAttrs);
		        Vector<String> pkVect = new Vector<String>(pkList);
		        List<String> nonkeyList = Arrays.asList(nonKeyAttrs);
		        Vector<String> nonkeyVect = new Vector<String>(nonkeyList);
				
		        if (log.isDebugEnabled()) {log.debug("---------GENERATING PRIMARY KEY FD---------");};
		        
				if (log.isDebugEnabled()) {log.debug("relName: " + r.getName());};
				if (log.isDebugEnabled()) {log.debug("LHS: " + pkVect.toString());};
				if (log.isDebugEnabled()) {log.debug("RHS: " + nonkeyVect.toString());};
			}
		}
	}
	
	private boolean addFD (Map<String, Map<String,FD>> fds, String relName, 
			String[] lhs, String[] rhs) {
		FD fd = new FD (relName, lhs, rhs);
		Map<String, FD> relMap;
		
		if (!fds.containsKey(relName)) {
			relMap = new HashMap<String, FD> ();
			fds.put(relName, relMap);
		}
		else {
			relMap = fds.get(relName);
		}
			
		if (relMap.get(fd.toString) == null) {
			relMap.put(fd.toString, fd);
			return true;
		}
		return false;
	}

	/**
	 * Get all of the non-key attributes of a relation
	 * 
	 * @param r
	 *            The relation to get the attributes from
	 * @param scenario
	 *            The mapping scenario
	 * 
	 * @return An array of the names of all of the non-key attributes
	 * @throws Exception
	 */
	static String[] getNonKeyAttributes(RelationType r, MappingScenario scenario) throws Exception 
	{
		// get the positions of all the primary keys
		int[] pkPos = scenario.getDoc().getPKPos(r.getName(), true);

		// get positions for all of the attributes
		int[] attrPos = new int[r.getAttrArray().length];
		for (int i = 0; i < r.getAttrArray().length; i++)
			attrPos[i] = i;

		int[] nonKeyPos = stripOutPKPositions(r.getAttrArray().length, pkPos, attrPos);

		String[] nonKeyAttrs = scenario.getDoc().getAttrNames(r.getName(), nonKeyPos, true);

		return nonKeyAttrs;
	}

	/**
	 * Takes an array of all attribute positions and removes the ones that are
	 * associated with the primary key
	 * 
	 * @param numAttr
	 *            The amount of attributes
	 * @param pkPos
	 *            The positions associated with the primary key
	 * @param attrPos
	 *            The positions of all attributes in the relation
	 * 
	 * @return An array with all of the positions not associated with the
	 *         primary key
	 */
	static int[] stripOutPKPositions(int numAttr, int[] pkPos, int[] attrPos) 
	{
		int[] nonKeyPos = new int[numAttr - pkPos.length];
		Boolean addPosition = true;
		int lastAdded = 0;

		for (int i = 0; i < attrPos.length; i++) 
		{
			addPosition = true;

			for (int pkPosition : pkPos)
				if (attrPos[i] == pkPosition)
					addPosition = false;

			if (addPosition)
				nonKeyPos[lastAdded++] = attrPos[i];
		}

		return nonKeyPos;
	}
}
