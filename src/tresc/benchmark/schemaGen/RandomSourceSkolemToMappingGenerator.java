package tresc.benchmark.schemaGen;

import java.util.Random;
import java.util.Vector;

import org.vagabond.benchmark.model.TrampModelFactory;
import org.vagabond.benchmark.model.TrampXMLModel;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SKFunction;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Constants.SkolemKind;
import vtools.dataModel.types.RandSrcSkolem;

/**
 * Randomly picks source attributes to turn into skolem terms after all scenarios have been generated.
 * 
 * @author mdangelo
 */

// PRG FIXED Compilation Error on July 16th, 2012 - Replaced rsk.getSKId() with rsk.getSkId() (lines 223 and 260 in previous version)

public class RandomSourceSkolemToMappingGenerator implements ScenarioGenerator 
{
	protected TrampModelFactory fac;
	protected TrampXMLModel model;
	private SkolemKind sk;
	
	/**
	 * Randomly picks source attributes to turn into skolem terms after all scenarios have been generated.
	 * 
	 * @param	scenario		The mapping scenario to add random skolems to
	 * @param	configuration	Configuration parameters - used to determine the number of skolems to generate
	 * 
	 * @throws 	Exception
	 * 
	 * @author mdangelo
	 */
	@Override
	public void generateScenario(MappingScenario scenario, Configuration configuration) throws Exception 
	{
		Random _generator = configuration.getRandomGenerator();
		fac = scenario.getDocFac();
		model = scenario.getDoc();
		
		Vector<String> SKVars = new Vector<String> ();
		Vector<RandSrcSkolem> RandomSkolems = new Vector<RandSrcSkolem> ();
				
		for(RelationType r: scenario.getDoc().getSchema(true).getRelationArray()) 
		{
			double percentage = ((double) configuration.getParam(Constants.ParameterName.SourceSkolemPerc))/(double) 100;
			int numAtts = r.getAttrArray().length;
			int numSKs = (int) Math.ceil((percentage/2) * numAtts);
			
			Vector<String> addedSKs = new Vector<String> ();
			
			// get positions for all of the attributes
			int[] attrPos = new int[r.getAttrArray().length];
			for (int i = 0; i < r.getAttrArray().length; i++)
				attrPos[i] = i;
			
			// if there is a primary key then we must strip out the attributes associated with it, otherwise we just grab all the attributes
			String[] nonKeyAttrs = (r.isSetPrimaryKey()) ? getNonKeyAttributes(r, scenario) : scenario.getDoc().getAttrNames(r.getName(), attrPos, true);
			
			RandSrcSkolem[] randSK = new RandSrcSkolem[numSKs];
			
			for (RandSrcSkolem rsk : randSK)
			{
				int position;
				Boolean alreadyAdded;
				
				do
				{
					alreadyAdded = false;
					
					// pick a nonkey attribute at random to be the skolem
					position = _generator.nextInt(nonKeyAttrs.length);
					rsk = new RandSrcSkolem();
					rsk.setAttr(nonKeyAttrs[position]);
					
					// check if we already picked this one to be a skolem
					if(addedSKs.indexOf(rsk.getAttr()) == -1)
						addedSKs.add(rsk.getAttr());
					else
						alreadyAdded = true;
					
				} while (alreadyAdded);
					
				
				// retrieve the variable associated with the skolem attribute
				String[] allAttrs = scenario.getDoc().getAttrNames(r.getName(), attrPos, true);
				for (int i = 0; i < allAttrs.length; i++)
					if(allAttrs[i].equals(rsk.getAttr()))
						rsk.setPosition(position);
				rsk.setAttrVar(fac.getFreshVars(position, 1)[0]);
				
				// roll the dice to pick the skolem mode (there are 4 modes with ordinals from 0 to 3)
				int mode = position = _generator.nextInt(4);
				
				sk = SkolemKind.values()[mode];
				
				SKVars.add(rsk.getAttrVar());

				// if we are using a key in the original relation then we will base the skolem on only the primary key
				if (sk == SkolemKind.KEY)
				{	
					System.out.println("mode: KEY");
					
					// make sure there is a key for the relation, if there is not then we will switch to using all attributes for the skolem
					if (r.isSetPrimaryKey()) 
					{
						String pkVar[] = new String [scenario.getDoc().getPK(r.getName(), true).length];
						int pos = 0;
						
						String[] pkAtts = scenario.getDoc().getPK(r.getName(), true);
						rsk.setSkolemArgs(pkAtts);
						for (int i = 0; i < allAttrs.length; i++)
							for (String str : pkAtts)
								if(allAttrs[i].equals(str))
									pkVar[pos++] = fac.getFreshVars(i, 1)[0];

						rsk.setSkolemVars(pkVar);
					}
					else
					{
						sk = SkolemKind.values()[Constants.SkolemKind.RANDOM.ordinal()];
					}
				}
				
				if (sk == SkolemKind.EXCHANGED)
				{					
					System.out.println("mode: EXCHANGED");
					
					// get the relation(s) for the target and store the vars used in them
					MappingType[] maps = model.getMappings(r.getName());
					Vector<String> tgtVars = new Vector<String> ();
					
					for (MappingType m : maps)
						for (RelAtomType a : m.getExists().getAtomArray())
							for (int i=0; i < a.getVarArray().length; i++)
								tgtVars.add(a.getVarArray(i));
					
					
					// get the intersection of the set of vars used in the source and the set of vars used in the target
					Vector<String> exchangedVars = new Vector<String> ();
					String[] allVars = fac.getFreshVars(0, allAttrs.length);
					
					for (String v : tgtVars)
						for (String av : allVars)
							if (v.equals(av) && (exchangedVars.indexOf(av) == -1) && !v.equals(rsk.getAttrVar()))
								exchangedVars.add(v);
					
					String[] exchVars = convertVectorToStringArray(exchangedVars);
					java.util.Arrays.sort(exchVars);
					
					rsk.setSkolemVars(exchVars);
				}
				
				if (sk == SkolemKind.RANDOM)
				{
					System.out.println("mode: RANDOM");
					
					int numArgsForSkolem = _generator.nextInt(allAttrs.length);
					
					int max_tries = 20;
					
					// generate the random vars to be arguments for the skolem
					Vector<String> randomVars = new Vector<String> ();
					for (int i=0; i < numArgsForSkolem; i++)
					{		
						// try 20 times per argument slot to find an argument
						int tries = 0;
						
						while (tries < max_tries)
						{
							int pos = _generator.nextInt(allAttrs.length);
						
							// initially check that we are not trying to add a var as an argument to its own skolem function
							// then make sure we're not adding duplicate vars
							if (!(allAttrs[pos].equals(rsk.getAttr())) && randomVars.indexOf(fac.getFreshVars(pos, 1)[0]) == -1)
							{
								randomVars.add(fac.getFreshVars(pos, 1)[0]);
								break;
							}
							
							tries++;
						}
					}
					
					String[] randVars = convertVectorToStringArray(randomVars);
					java.util.Arrays.sort(randVars);
					
					// make sure the random selection yielded at least one argument, switch modes if it didn't
					if(randVars.length == 0)
						sk = SkolemKind.ALL;
					
					rsk.setSkolemVars(randVars);
				}
				
				if (sk == SkolemKind.ALL)
				{
					System.out.println("mode: ALL");
					
					// ensure that we are not adding the attribute itself as an argument to the skolem
					Vector<String> skAtts = new Vector<String>();
					Vector<String> vars = new Vector<String>();
					for (int i=0; i < allAttrs.length; i++)
					{
						if(allAttrs[i].equals(rsk.getAttr()))
							;
						else
						{
							skAtts.add(allAttrs[i]);
							vars.add(fac.getFreshVars(i, 1)[0]); 
						}
					}
					
					rsk.setSkolemArgs(convertVectorToStringArray(skAtts));
					rsk.setSkolemVars(convertVectorToStringArray(vars));
				}
				
				rsk.setSkId(fac.getNextId("SK"));
				
				RandomSkolems.add(rsk);
			}
			
			System.out.println("---------NEW SKOLEMS---------");
			
			for (RandSrcSkolem rsk : RandomSkolems)
			{
				System.out.println("relName: " + r.getName());
				System.out.println("skID: " + rsk.getSkId());
				System.out.println("SkolemVar: " + rsk.getAttrVar());
				
				System.out.print("Args: ");
				for (String s : rsk.getSkolemVars())
					System.out.print(s + " ");
				
				System.out.println();
			}
			
			// get all the mappings associated with the relation in question and go through them
			MappingType[] mappings = model.getMappings(r.getName());

			// create a new mapping to temporarily store the SKFunctions (because we need to destroy the ones in the atom before we can add new ones)
			// the old ones would be deallocated (and all information lost) if we did not clone them
			MappingType map = model.getScenario().getMappings().addNewMapping();
			RelAtomType tmp = map.addNewExists().addNewAtom();
			
			for (MappingType m : mappings)
			{
				// for each relation we go through all the objects and check if they are skolem functions
				for(RelAtomType a : m.getExists().getAtomArray())
				{
					// retrieve all the objects associated with the exists clause of the mapping (aka. vars and skolems in one array)
					Object[] mappingObjects = scenario.getDoc().getAtomParameters(m, false, scenario.getDoc().getAtomPos(m, a.getTableref())); 
					
					for(int j = 0; j < mappingObjects.length; j++)
					{
						if(!(mappingObjects[j] instanceof SKFunction))
						{
							// if it's a var we must check if it should be swapped out with a skolem so we go through all the skolems we 
							// must replace and insert it into the mapping object array
							for (RandSrcSkolem rsk : RandomSkolems)
								if(mappingObjects[j].equals(rsk.getAttrVar()))
								{						
									// create the skolem function object
									SKFunction sk = tmp.addNewSKFunction();
									sk.setSkname(rsk.getSkId());
									sk.setVarArray(rsk.getSkolemVars());

									// switch out the var for the new SKFunction
									mappingObjects[j] = sk;
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
			model.getScenario().getMappings().removeMapping(model.getScenario().getMappings().sizeOfMappingArray()-1);
			
			// clear the vector of random skolems
			RandomSkolems.removeAllElements();
		}
	}
	
	/**
	 * Get all of the non-key attributes of a relation
	 * 
	 * @param	r	 		The relation to get the attributes from
	 * @param	scenario	The mapping scenario
	 * 	
	 * @return				An array of the names of all of the non-key attributes
	 * @throws 	Exception 
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
	 * Takes an array of all attribute positions and removes the ones that are associated with the primary key
	 * 
	 * @param	numAttr		The amount of attributes
	 * @param	pkPos	 	The positions associated with the primary key
	 * @param	attrPos	 	The positions of all attributes in the relation
	 * 
	 * @return				An array with all of the positions not associated with the primary key
	 * 
	 * @author mdangelo
	 */
	static int[] stripOutPKPositions(int numAttr, int[] pkPos, int[] attrPos) 
	{
		int[] nonKeyPos = new int[numAttr - pkPos.length];
		Boolean addPosition = true;
		int lastAdded = 0;
		
		for(int i = 0; i < attrPos.length; i++)
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
	 * Converts a string vector to an array of strings
	 * 
	 * @param	vStr		A string vector
	 * @return				An array of strings
	 * 
	 * @author mdangelo
	 */
	static String[] convertVectorToStringArray(Vector<String> vStr)
	{
		String[] ret = new String[vStr.size()];
		
		int j = 0;
		for (String str: vStr)
			ret[j++] = str;
		
		return ret;
	}
}
