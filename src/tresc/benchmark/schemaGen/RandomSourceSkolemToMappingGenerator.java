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
public class RandomSourceSkolemToMappingGenerator implements ScenarioGenerator 
{
	protected TrampModelFactory fac;
	protected TrampXMLModel model;
	private SkolemKind sk;
	private int skCount;
	
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
			
			System.out.println("numSKs: " + numSKs);
			String[] addedSKs = new String[numSKs];
			
			// get positions for all of the attributes
			int[] attrPos = new int[r.getAttrArray().length];
			for (int i = 0; i < r.getAttrArray().length; i++)
				attrPos[i] = i;
			
			// if there is a primary key then we must strip out the attributes associated with it, otherwise we just grab all the attributes
			String[] nonKeyAttrs = (r.isSetPrimaryKey()) ? getNonKeyAttributes(r, scenario) : scenario.getDoc().getAttrNames(r.getName(), attrPos, true);
			
			RandSrcSkolem[] randSK = new RandSrcSkolem[numSKs];
			
			skCount = 0;
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
					
					// TODO populate addedSKs?
					for (String str : addedSKs)
						if (rsk.getAttr().equals(str))
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
					System.out.println("Trying key for skolem");
					
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
						System.out.println("No key, switching mode");
						sk = SkolemKind.values()[Constants.SkolemKind.RANDOM.ordinal()];
					}
				}
				if (sk == SkolemKind.EXCHANGED)
				{					
					System.out.println("Skolem depends on exchanged");
					
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
					
					/*@SuppressWarnings("unchecked")
					Vector<String> exchVars = (Vector<String>)exchangedVars.clone();
					
					// make sure that the vars we are adding as arguments to the new skolem are not skolems themselves
					Boolean addVar = true;
					for (String var : exchangedVars)
					{
						for (String skvar : SKVars)
							if(var.equals(skvar))
								addVar = false;
						
						if(!addVar)
						{
							System.out.println("removing var: " + var);
							exchVars.remove(var);
						}
					}
					
					// if all of the exchanged variables have been skolemized then we need to switch modes so we have arguments for our skolem
					if(exchVars.size() == 0)
					{
						System.out.println("switching modes");
						sk = SkolemKind.RANDOM;
					}*/
					
					String[] exchVars = convertVectorToStringArray(exchangedVars);
					java.util.Arrays.sort(exchVars);
					
					rsk.setSkolemVars(exchVars);
				}
				if (sk == SkolemKind.RANDOM)
				{
					System.out.println("Skolem random");
					
					int numArgsForSkolem = _generator.nextInt(allAttrs.length);
					
					// generate the random vars to be arguments for the skolem
					Vector<String> randomVars = new Vector<String> ();
					for (int i=0; i < numArgsForSkolem; i++)
					{						
						int pos = _generator.nextInt(allAttrs.length);
						
						// initially check that we are not trying to add a var as an argument to its own skolem function
						// then make sure we're not adding duplicate vars
						if (allAttrs[pos].equals(rsk.getAttr()))
							i--;
						else
							if(randomVars.indexOf(fac.getFreshVars(pos, 1)[0]) == -1)
							{
								/*Boolean addVar = true;
								
								for (String skvar : SKVars)
									if(fac.getFreshVars(pos, 1)[0].equals(skvar))
										addVar = false;
								
								if(addVar)*/
									randomVars.add(fac.getFreshVars(pos, 1)[0]);
							}
							else
								i--;
					}
					
					/*// if all of the random variables have been skolemized then we need to switch modes so we have arguments for our skolem
					if(randomVars.size() == 0)
					{
						System.out.println("switching modes");
						sk = SkolemKind.ALL;
					}*/
					
					String[] randVars = convertVectorToStringArray(randomVars);
					java.util.Arrays.sort(randVars);
					
					rsk.setSkolemVars(randVars);
				}
				if (sk == SkolemKind.ALL)
				{
					System.out.println("Skolem depends on all");

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
					
					/*Boolean addVar = true;
					for (String var : vars)
					{
						for (String skvar : SKVars)
							if(var.equals(skvar))
								addVar = false;
						
						if(!addVar)
							vars.remove(var);
					}
					
					// if all of the exchanged variables have been skolemized then we need to switch modes so we have arguments for our skolem
					if(vars.size() == 0)
						vars.add("*");*/
					
					rsk.setSkolemArgs(convertVectorToStringArray(skAtts));
					rsk.setSkolemVars(convertVectorToStringArray(vars));
				}
				
				rsk.setSkId(fac.getNextId("SK"));
				
				RandomSkolems.add(rsk);
			}
			
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
			
			for (MappingType m : mappings)
			{
				System.out.println("Retrieving objects associated with mapping");
				
				// retrieve all the objects associated with the exists clause of the mapping (aka. vars and skolems in one array)
				Object[] mappingObjects = scenario.getDoc().getAtomParameters(m, false, scenario.getDoc().getRelPos(r.getName(), false));
				
				// go through the objects and if there is a var that should be replaced with one of the skolem functions we generated 
				// go through all the relations in the exists clause of the mapping
				for(int j = 0; j < mappingObjects.length; j++)
					if(!(mappingObjects[j] instanceof SKFunction))
					{
						System.out.println("Looking at var: " + mappingObjects[j]);
						
						for (RandSrcSkolem rsk : RandomSkolems)
							if(mappingObjects[j].equals(rsk.getAttrVar()))
							{
								System.out.println("Found a match");
								
								for(RelAtomType a : m.getExists().getAtomArray())
								{						
									// create the skolem function object
									SKFunction sk = a.addNewSKFunction();
									sk.setSkname(rsk.getSkId());
									sk.setVarArray(rsk.getSkolemVars());

									// switch out the var for the new SKFunction
									mappingObjects[j] = sk;
									
									System.out.println("sk: " + ((SKFunction) mappingObjects[j]).getSkname());
									
									// now replace the atom parameters we retrieved earlier with the modified version
									//scenario.getDoc().setAtomParameters(mappingObjects, a);
									
									System.out.println("Swapped it out and set the parameters");
								}
							}
					}
			}
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
