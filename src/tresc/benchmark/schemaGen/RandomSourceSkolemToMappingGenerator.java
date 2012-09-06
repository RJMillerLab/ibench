package tresc.benchmark.schemaGen;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.benchmark.model.TrampModelFactory;
import org.vagabond.benchmark.model.TrampXMLModel;
import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SKFunction;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Constants.SkolemKind;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.types.RandSrcSkolem;

/**
 * Randomly picks source attributes to turn into skolem terms after all
 * scenarios have been generated.
 * 
 * @author mdangelo
 */
public class RandomSourceSkolemToMappingGenerator implements ScenarioGenerator 
{
	static Logger log = Logger.getLogger(RandomSourceSkolemToMappingGenerator.class);
	
	protected static TrampModelFactory fac;
	protected static TrampXMLModel model;
	private static SkolemKind sk;
	private static Random _generator;
	private static Vector<RandSrcSkolem> RandomSkolems;

	/**
	 * Randomly picks source attributes to turn into skolem terms after all
	 * scenarios have been generated.
	 * 
	 * @param scenario
	 *            The mapping scenario to add random skolems to
	 * @param configuration
	 *            Configuration parameters - used to determine the number of
	 *            skolems to generate
	 * 
	 * @throws Exception
	 * 
	 * @author mdangelo
	 */
	@Override
	public void generateScenario(MappingScenario scenario, Configuration configuration) throws Exception 
	{
		_generator = configuration.getRandomGenerator();
		fac = scenario.getDocFac();
		model = scenario.getDoc();

		RandomSkolems = new Vector<RandSrcSkolem>();

		for (RelationType r : scenario.getDoc().getSchema(true).getRelationArray()) 
		{
			log.debug("---------GENERATING SKOLEMS---------");
			log.debug("Relation: " + r.getName());
			
	        log.debug("Attributes: ");
	        for (AttrDefType ra : r.getAttrArray())
	        	log.debug(ra.getName() + " ");
	        
			double percentage = ((double) configuration.getParam(Constants.ParameterName.SourceSkolemPerc)) / (double) 100;
			int numAtts = r.getAttrArray().length;
			int numSKs = (int) Math.ceil((percentage / 2) * numAtts);

			Vector<String> addedSKs = new Vector<String>();
			
			generateVictims(r, scenario, RandomSkolems, addedSKs, numSKs);
			generateSkolemArguments(r, scenario, RandomSkolems, numSKs);
	        
			log.debug("---------NEW SKOLEMS---------");
			
			for (RandSrcSkolem rsk : RandomSkolems)
			{
				log.debug("Victim: " + rsk.getAttr());
				log.debug("Victim Position: " + rsk.getAttrPosition());
				log.debug("Victim Variable: " + rsk.getAttrVar());
				log.debug("Identifier: " + rsk.getSkId());
				
				if(rsk.getArgAttrs().length != 0)
				{
					// convert to vector to facilitate printing
			        List<String> argList = Arrays.asList(rsk.getArgAttrs());
			        Vector<String> argVect = new Vector<String>(argList);
			        log.debug("Arguments: " + argVect.toString());
			        
			        log.debug("Positions: ");
			        for (int pos : rsk.getArgPositions())
						log.debug(pos + " ");
			        log.debug("");
				}
			}
			
			addSkolemsToMappings(r, scenario);
		}
	}
	
	/**
	 * Generates victims to be skolemized for a given relation. 
	 *		Checks:
	 * 			a) That an attribute isn't being skolemized more than once 
	 * 			b) That the attribute in question isn't deleted in the target mappings to avoid gaps in the skolem IDs
	 * 
	 * @param r	
	 * 			The relation for which we are generating random skolems
	 * @param scenario	
	 * 			The mapping scenario in question
	 * @param randomSkolems
	 * 			A vector of RandSrcSkolems which keep track of victim names and positions
	 * @param addedSKs
	 * 			A vector of Strings to keep track of which attributes have already been picked as victims
	 * @param numSKs
	 * 			The number of random skolems to generate
	 * 
	 * @throws Exception
	 * 
	 * @author mdangelo
	 */
	private static void generateVictims (RelationType r, MappingScenario scenario, Vector<RandSrcSkolem> randomSkolems, Vector<String> addedSKs, int numSKs) throws Exception
	{
		for(int i = 0; i < numSKs; i++)
		{
			RandSrcSkolem rsk = new RandSrcSkolem ();
	
			int position;
			Boolean successful = false;
			
			// get all vars and attributes
			String[] allVars = scenario.getDoc().getAttrVars(r.getName());
			String[] allAttrs = scenario.getDoc().getAttrNames(r.getName(),getAttrPositions(r.getAttrArray().length), true);
	
			// if there is a primary key then we must strip out the attributes associated with it, otherwise we just grab all the attributes
			String[] nonKeyAttrs = (r.isSetPrimaryKey()) ? getNonKeyAttributes(r, scenario) : scenario.getDoc().getAttrNames(r.getName(),getAttrPositions(r.getAttrArray().length), true);
	
			int max_tries = 50;
			int tries = 0;
		
			while (tries < max_tries) 
			{
				// pick a nonkey attribute at random to be the skolem
				position = _generator.nextInt(nonKeyAttrs.length);
				
				rsk = new RandSrcSkolem();
				rsk.setAttr(nonKeyAttrs[position]);
	
				for (int j = 0; j < allAttrs.length; j++)
					if (allAttrs[j].equals(rsk.getAttr()))
						rsk.setAttrPosition(j);
				
				rsk.setAttrVar(allVars[rsk.getAttrPosition()]);

				// check if the variable exists in any target mappings associated with the source relation
				// this prevents "gaps" in SK IDs which are created when we generate a skolem that will never appear in a mapping
				MappingType[] tgtMaps = model.getMappings(r.getName());
				Vector<String> tgtVars = new Vector<String>();
	
				for (MappingType m : tgtMaps)
					for (RelAtomType a : m.getExists().getAtomArray())
						for (int k = 0; k < a.getVarArray().length; k++)
							if (tgtVars.indexOf(a.getVarArray(k)) == -1)
								tgtVars.add(a.getVarArray(k));
	
				// if the list of target variables includes the variable then we can add it in
				if(tgtVars.indexOf(rsk.getAttrVar()) != -1)
					// check if we already picked this one to be a skolem (prevent duplicates)
					if (addedSKs.indexOf(rsk.getAttr()) == -1)
					{
						addedSKs.add(rsk.getAttr());
						successful = true;
						break;
					}
				
				tries++;
			}
			
			if(successful)
			{
				rsk.setSkId(fac.getNextId("SK"));
				randomSkolems.add(rsk);
			}
		}
	}

	/**
	 * Generates arguments for the skolems using different modes (which call on different methods to do the actual work).
	 * 
	 * @param r	
	 * 			The relation for which we are generating random skolems
	 * @param scenario	
	 * 			The mapping scenario in question
	 * @param randomSkolems
	 * 			A vector of RandSrcSkolems which keep track of victim names and positions
	 * @param numSKs
	 * 			The number of random skolems to generate
	 * 
	 * @throws Exception
	 * 
	 * @author mdangelo
	 */
	private static void generateSkolemArguments (RelationType r, MappingScenario scenario, Vector<RandSrcSkolem> randomSkolems, int numSKs) throws Exception
	{
		for (RandSrcSkolem rsk : randomSkolems)
		{
			// roll the dice to pick the skolem mode (only use modes 0 - KEY, 1 - ALL, 2 - RANDOM)
			// we are not using the 4th mode (EXCHANGED) because we would need to look at all of the mappings associated with it
			// and try to find some common ground (intersection) among the vars, which is an unnecessary complication
			int mode = _generator.nextInt(3);
			sk = SkolemKind.values()[mode];
			
			String[] allAttrs = scenario.getDoc().getAttrNames(r.getName(),getAttrPositions(r.getAttrArray().length), true);
			
			// KEY and RANDOM cases include and if statement to check if the mode has been changed in the case of 
			// a) a primary key not being set for KEY mode which causes it to switch to RANDOM, and 
			// b) no arguments being chosen in RANDOM (because of the limit on the amount of tries) which causes it to switch to ALL
			switch(sk)
			{
				case KEY:
					useKeyAsArgument(r, scenario, rsk, allAttrs);
					if (sk == SkolemKind.KEY)
						break;
				case RANDOM:
					useRandomAsArgument(r, scenario, rsk, allAttrs);
					if (sk == SkolemKind.RANDOM)
						break;
				case ALL:
					useAllAsArgument(r, rsk, allAttrs);
					break;
				default:
					break;
			}
		}
	}

	/**
	 * Pick the primary key attributes as the arguments of the skolem adds their names and positions to the RandSrcSkolem object.
	 * 
	 * @param r
	 * 			The relation which we are adding the skolem to
	 * @param scenario
	 * 			The scenario which contains the relation
	 * @param rsk
	 * 			The RandSrcSkolem object which we wish to add the arguments to
	 * @param allAttrs
	 * 			All attributes in the source relation we are looking at
	 * 
	 * @throws Exception
	 * 
	 * @author mdangelo
	 */
	private static void useKeyAsArgument(RelationType r, MappingScenario scenario, RandSrcSkolem rsk, String[] allAttrs) throws Exception 
	{
		log.debug("mode: KEY");
	
		// make sure there is a key for the relation, if there is not then we will switch to using all attributes for the skolem
		if (r.isSetPrimaryKey()) 
		{
			String pkVar[] = new String[scenario.getDoc().getPK(r.getName(), true).length];
			String[] pkAtts = scenario.getDoc().getPK(r.getName(),true);
			
			rsk.setArgAttrs(pkAtts);
			
			int[] pkPos = getAttrPositions(allAttrs, pkAtts);
			
			int pos = 0;
			for(int p : pkPos)
				pkVar[pos++] = fac.getFreshVars(p, 1)[0];
			
			for (int i = 0; i < allAttrs.length; i++)
				for (String str : pkAtts)
					if (allAttrs[i].equals(str))
						
			rsk.setArgAttrs(pkAtts);
			rsk.setArgPositions(pkPos);
		} 
		
		else 
			sk = SkolemKind.values()[Constants.SkolemKind.RANDOM.ordinal()];
	}

	/**
	 * Picks random attributes to be the arguments of the skolem and adds their names and positions to the RandSrcSkolem object. 
	 * Works by randomly generating a position and checking if that attribute was a) already an argument for the skolem, or 
	 * b) was the victim of the skolemization. To avoid infinite looping, this method tries 20 times per argument slot to pick an
	 * attribute meeting that criteria and if it cannot find one it moves on to the next "slot". Note that there is a final check 
	 * to ensure that we didn't end up with 0 arguments which then changes the mode to ALL. 
	 * 
	 * @param r
	 * 			The relation which we are adding the skolem to
	 * @param scenario
	 * 			The scenario which contains the relation
	 * @param rsk
	 * 			The RandSrcSkolem object which we wish to add the arguments to
	 * @param allAttrs
	 * 			All attributes in the source relation we are looking at
	 * 
	 * @throws Exception
	 * 
	 * @author mdangelo
	 */
	private static void useRandomAsArgument(RelationType r, MappingScenario scenario, RandSrcSkolem rsk, String[] allAttrs) throws Exception 
	{
		log.debug("mode: RANDOM");
	
		int numArgsForSkolem = _generator.nextInt(allAttrs.length);
	
		int max_tries = 20;
	
		// generate the random vars to be arguments for the skolem
		Vector<String> randomAttrs = new Vector<String>();
		for (int i = 0; i < numArgsForSkolem; i++) 
		{
			// try 20 times per argument slot to find an argument
			int tries = 0;
	
			while (tries < max_tries) 
			{
				int pos = _generator.nextInt(allAttrs.length);
	
				// initially check that we are not trying to add a var as an argument to its own skolem function
				// then make sure we're not adding duplicate vars
				if (!(allAttrs[pos].equals(rsk.getAttr())) && randomAttrs.indexOf(allAttrs[pos]) == -1) 
				{
					randomAttrs.add(allAttrs[pos]);
					break;
				}
	
				tries++;
			}
		}
	
		String[] randAtts = Utils.convertVectorToStringArray(randomAttrs);
		int[] attrPos = getAttrPositions(allAttrs, randAtts);
		java.util.Arrays.sort(attrPos);
	
		// make sure the random selection yielded at least one argument, switch modes if it didn't
		if (randAtts.length == 0)
			sk = SkolemKind.ALL;
	
		rsk.setArgAttrs(randAtts);
		rsk.setArgPositions(attrPos);
	}

	/**
	 * Picks all attributes to be the arguments of the skolem and adds their names and positions to the RandSrcSkolem object.
	 * 
	 * @param r
	 * 			The relation which we are adding the skolem to
	 * @param scenario
	 * 			The scenario which contains the relation
	 * @param rsk
	 * 			The RandSrcSkolem object which we wish to add the arguments to
	 * @param allAttrs
	 * 			All attributes in the source relation we are looking at
	 * 
	 * @throws Exception
	 * 
	 * @author mdangelo
	 */
	private static void useAllAsArgument(RelationType r, RandSrcSkolem rsk, String[] allAttrs) throws Exception 
	{
		log.debug("mode: ALL");
	
		Vector<String> skAtts = new Vector<String>();
		
		for (String att : allAttrs)
			skAtts.add(att);
		
		// ensure that we are not adding the attribute itself as an argument to the skolem
		for (int i = 0; i < allAttrs.length; i++) 
			if (skAtts.indexOf(rsk.getAttr()) != -1)
				skAtts.remove(skAtts.indexOf(rsk.getAttr()));
		
		rsk.setArgAttrs(Utils.convertVectorToStringArray(skAtts));
		rsk.setArgPositions(getAttrPositions(allAttrs, rsk.getArgAttrs()));
	}

	/**
	 * Replaces the victims in mappings associated with the relation with the skolem function generated previously.
	 * 
	 * @param r	
	 * 			The relation for which we generated the random skolems
	 * @param scenario	
	 * 			The mapping scenario in question
	 * 
	 * @throws Exception
	 * 
	 * @author mdangelo
	 */
	private static void addSkolemsToMappings(RelationType r, MappingScenario scenario) 
	{
		// get all the mappings associated with the relation in question and go through them
		MappingType[] mappings = model.getMappings(r.getName());

		// create a new mapping to temporarily store the SKFunctions (because we need to destroy the ones in the atom before 
		// we can add new ones) the old ones would be deallocated (and all information lost) if we did not clone them
		MappingType map = model.getScenario().getMappings().addNewMapping();
		RelAtomType tmp = map.addNewExists().addNewAtom();

		for (MappingType m : mappings) 
		{		
			// for each relation we go through all the objects and check if they are skolem functions
			for (RelAtomType a : m.getExists().getAtomArray()) 
			{
				// retrieve all the objects associated with the exists clause of the mapping (aka. vars and skolems in one array)
				Object[] mappingObjects = scenario.getDoc().getAtomParameters(m, false, scenario.getDoc().getAtomPos(m, a.getTableref()));

				for (int j = 0; j < mappingObjects.length; j++) 
				{
					if (!(mappingObjects[j] instanceof SKFunction)) 
					{
						// if it's a var we must check if it should be swapped out with a skolem so we go through all the skolems we
						// must replace and insert it into the mapping object array
						for (RandSrcSkolem rsk : RandomSkolems)
						{
							for (RelAtomType b : m.getForeach().getAtomArray()) 
							{
								// translate the attribute positions into variables relevant for this mapping
								if(b.getTableref().equals(r.getName()))
								{
									String[] allVars = b.getVarArray();
								
									Vector<String> argVars = new Vector<String> ();
									for (int p : rsk.getArgPositions())
										argVars.add(allVars[p]);
									
									rsk.setArgVars(Utils.convertVectorToStringArray(argVars));
									rsk.setAttrVar(allVars[rsk.getAttrPosition()]);
								}
								
								// mdangelo BUG FIX August 24, 2012
								// check if the mapping object we are looking at is supposed to be replaced and perform the replacement
								if (mappingObjects[j].equals(rsk.getAttrVar())) 
								{
									// create the skolem function object
									SKFunction sk = tmp.addNewSKFunction();
									sk.setSkname(rsk.getSkId());
									sk.setVarArray(rsk.getArgVars());

									// switch out the var for the new SKFunction
									mappingObjects[j] = sk;
								}
							}
						}
					} 
					else 
					{
						// if it's not a var, then we have to clone the existing skolem object so that when we
						// use it later we are not referencing an object that has already been destroyed
						SKFunction sk = tmp.addNewSKFunction();
						sk.setSkname(((SKFunction) mappingObjects[j]).getSkname());
						sk.setVarArray(((SKFunction) mappingObjects[j]).getVarArray());

						mappingObjects[j] = sk;
					}
				}

				// now replace the atom parameters we retrieved earlier with the modified version
				scenario.getDoc().setAtomParameters(mappingObjects, a);
			}
		}

		// delete the mapping we temporarily created
		model.getScenario().getMappings().removeMapping(model.getScenario().getMappings().sizeOfMappingArray() - 1);

		// clear the vector of random skolems
		RandomSkolems.removeAllElements();
	}

	/**
	 * Get "positions" associated with a relation - essentially generates an array of numbers from 0 to size.
	 * 
	 * @param size
	 *            The number of attributes for which to get positions.
	 * 
	 * @return An array of the positions of all the attributes
	 * 
	 * @author mdangelo
	 */
	private static int[] getAttrPositions (int size)
	{
		// get positions for all of the attributes
		int[] attrPos = new int[size];
		for (int i = 0; i < size; i++)
			attrPos[i] = i;
	
		return attrPos;
	}

	/**
	 * Get "positions" for specific of a relation
	 * 
	 * @param allAttrs
	 * 			All attributes of the relation
	 * @param attrs
	 * 			The attributes for which we want to retrieve the positions
	 * 
	 * @return An array of the positions of all the attributes
	 * 
	 * @author mdangelo
	 */
	private static int[] getAttrPositions (String[] allAttrs, String[] attrs)
	{
		Vector<Integer> positions = new Vector<Integer> ();
		
		for (int i = 0; i < allAttrs.length; i++)
			for(String a : attrs)
				if (allAttrs[i].equals(a))
					positions.add(i);

		return convertVectorToIntegerArray(positions);
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
	 * 
	 * @author mdangelo
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
	 * 
	 * @author mdangelo
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
	
	/**
	 * Converts an Integer vector to an array of integers
	 * 
	 * @param vInt
	 *            An integer vector
	 *            
	 * @return An array of integers
	 * 
	 * @author mdangelo
	 */
	static int[] convertVectorToIntegerArray(Vector<Integer> vInt) 
	{
		int[] ret = new int[vInt.size()];

		int j = 0;
		for (int i : vInt)
			ret[j++] = i;

		return ret;
	}
}
