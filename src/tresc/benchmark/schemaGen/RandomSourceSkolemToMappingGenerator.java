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
import tresc.benchmark.utils.Utils;
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
		sk = SkolemKind.values()[configuration.getParam(Constants.ParameterName.SkolemKind)];
				
		for(RelationType r: scenario.getDoc().getSchema(true).getRelationArray()) 
		{
			double percentage = ((double) configuration.getParam(Constants.ParameterName.SourceSkolemPerc))/(double) 100;
			int numAtts = r.getAttrArray().length;
			int numSKs = (int) Math.floor(percentage * numAtts);
			
			System.out.println("numSKs: " + numSKs);
			String[] addedSKs = new String[numSKs];
			
			// get positions for all of the attributes
			int[] attrPos = new int[r.getAttrArray().length];
			for (int i = 0; i < r.getAttrArray().length; i++)
				attrPos[i] = i;
			
			// if there is a primary key then we must strip out the attributes associated with it, otherwise we just grab all the attributes
			String[] nonKeyAttrs = (r.isSetPrimaryKey()) ? getNonKeyAttributes(r, scenario) : scenario.getDoc().getAttrNames(r.getName(), attrPos, true);
			
			RandSrcSkolem[] randSK = new RandSrcSkolem[numSKs];
			
			int skCount = 0;
			for (RandSrcSkolem rsk : randSK)
			{
				int position;
				Boolean alreadyAdded;
				
				do
				{
					alreadyAdded = false;
					
					// pick a nonkey attribute at random to be the skolem
					position = Utils.getRandomNumberAroundSomething(_generator, nonKeyAttrs.length/2, nonKeyAttrs.length/2);
					position = (position >= nonKeyAttrs.length) ? nonKeyAttrs.length-1 : position;
					rsk = new RandSrcSkolem();
					rsk.setAttr(nonKeyAttrs[position]);
					
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
				
				// TODO remove the attrVar from the list of skolemVars
				// TODO roll the dice to pick the skolem mode

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
						sk = SkolemKind.values()[Constants.SkolemKind.ALL.ordinal()];
					}
				}
				
				if (sk != SkolemKind.KEY)
				{
					System.out.println("Skolem not dependent on key");
					
					int numArgsForSkolem = allAttrs.length;
					
					// pick random number of attributes 
					if (sk == SkolemKind.RANDOM)
					{
						numArgsForSkolem = Utils.getRandomNumberAroundSomething(_generator, allAttrs.length/2, allAttrs.length/2);
						// ensure that we are still within bounds
						numArgsForSkolem = (numArgsForSkolem > allAttrs.length) ? allAttrs.length : numArgsForSkolem;
					}

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
					
					// convert the vectors to strings
					String[] skAttrs = new String[skAtts.size()];
					int j = 0;
					for (String str: skAtts)
						skAttrs[j++] = str;
					
					j = 0;
					String[] skVars = new String[vars.size()];
					for (String var: vars)
						skVars[j++] = var;
					
					// add them to the RandSrcSkolem
					rsk.setSkolemArgs(skAttrs);
					rsk.setSkolemVars(skVars);
				}
				
				MappingType[] mappings = model.getMappings(r.getName());
				
				// go through all relations within each mapping and further go through all of the variables
				// if the variable we are seeking is the same as the one we have turned into a skolem remove the var 
				// and add a skolem function in its place with the arguments determined above
				for (MappingType m : mappings)
					for (RelAtomType a : m.getExists().getAtomArray())
					{
						for (int i=0; i < a.getVarArray().length; i++)
						{
							// store all of the vars and then delete them from the relation
							String[] vars = a.getVarArray();
							Vector<String> varVect = new Vector<String> ();
							
							for (String str: vars)
								varVect.add(str);
							
							if (a.getVarArray(i).equals(rsk.getAttrVar()))
							{
								int numVars = a.sizeOfVarArray();
								for (int j=i; j < numVars; j++)
									a.removeVar(i);
								
								SKFunction sk = a.insertNewSKFunction(skCount);
								sk.setSkname(fac.getNextId("SK"));
								sk.setVarArray(rsk.getSkolemVars());
								
								System.out.println("skName: " + sk.getSkname());
								
								System.out.println("skAttVar: " + rsk.getAttrVar());
								System.out.println("skArgVar: ");
								for (String str : rsk.getSkolemVars())
									System.out.println(str);
								
								// restore the deleted vars
								for (int k=i; k < numVars; k++)
								{
									if (!(varVect.elementAt(i).equals(rsk.getAttrVar())))
										a.addVar(varVect.elementAt(i));
									varVect.remove(i);
								}
								
								// move the old sks down
								for (int l=++skCount; l < a.sizeOfSKFunctionArray(); l++)
								{
									SKFunction sk1 = a.addNewSKFunction();
									sk1.setSkname(a.getSKFunctionArray(l).getSkname());
									sk1.setVarArray(a.getSKFunctionArray(l).getVarArray());
									a.removeSKFunction(l);
								}
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
}
