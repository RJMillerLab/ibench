package tresc.benchmark.schemaGen;

import org.vagabond.xmlmodel.RelationType;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import tresc.benchmark.utils.Utils;

public class SourceFDGenerator implements ScenarioGenerator 
{	
	@Override
	public void generateScenario(MappingScenario scenario, Configuration configuration) throws Exception 
	{
		Random _generator = configuration.getRandomGenerator();
		
		// create PK FDs R(A,B,C) if A is key then add A -> B,C
		for(RelationType r: scenario.getDoc().getSchema(true).getRelationArray()) 
		{
			if (r.isSetPrimaryKey()) 
			{
				String[] pkAttrs = scenario.getDoc().getPK(r.getName(), false);
				String[] nonKeyAttrs = getNonKeyAttributes(r, scenario);
				
				scenario.getDocFac().addFD(r.getName(), pkAttrs, nonKeyAttrs);
			}
		}
		
		// create random FDs by randomly selecting non primary key attributes and linking them
		for(RelationType r: scenario.getDoc().getSchema(true).getRelationArray()) 
		{
			// determine how many FDs we should generate for the relation based on the number of attributes it has
			int percentage = configuration.getParam(Constants.ParameterName.SourceFDPerc)/100;
			int numAtts = r.getAttrArray().length;
			int numFDs = (int) Math.floor(percentage * numAtts);
			
			String[] nonKeyAttrs;
			
			if (r.isSetPrimaryKey()) 
				nonKeyAttrs = getNonKeyAttributes(r, scenario);
			else
			{
				// get positions for all of the attributes
				int[] attrPos = new int[r.getAttrArray().length];
				for (int i = 0; i < r.getAttrArray().length; i++)
					attrPos[i] = i;
				
				nonKeyAttrs = scenario.getDoc().getAttrNames(r.getName(), attrPos, true);
			}
			
			// randomly select attributes for each run of FD generation
			for (int i = 0; i < numFDs; i++)
			{
				int numLHSAtts = Utils.getRandomNumberAroundSomething(_generator, numAtts/2, numAtts/2);
				numLHSAtts = (numLHSAtts >= numAtts) ? numAtts/2 : numLHSAtts;
				
				System.out.println("#LHS: " + numLHSAtts);
				
				String[] LHSAtts = new String[numLHSAtts];
				String RHSAtt = "";
				
				Boolean add = true;
				int j;
				
				// pick the attributes to go on the left hand side
				for (j = 0; j < numLHSAtts; j++)
				{
					int position = Utils.getRandomNumberAroundSomething(_generator, numAtts/2, numAtts/2);
					position = (position > numAtts) ? numAtts : position;
					
					// make sure that we haven't already added this to our LHS FDs to avoid redundancy 
					// ex. ABA -> C
					for (int k = 0; k < LHSAtts.length; k++)
						if (nonKeyAttrs[position] == LHSAtts[k])
							add = false;
					
					if (add)	
						LHSAtts[j] = nonKeyAttrs[position];
				}
			
				// ensure that we keep trying to find a RHS attribute until we have added one
				add = false;
				while (!add)
				{
					// pick the attribute to go on the right hand side
					int position = Utils.getRandomNumberAroundSomething(_generator, numAtts/2, numAtts/2);
					position = (position > numAtts) ? numAtts : position;
					
					// make sure it hasn't been added to the LHS attributes to avoid nonsensical FDs
					// ex. AB -> A
					for (int k = 0; k < LHSAtts.length; k++)
						if (nonKeyAttrs[position] == LHSAtts[k])
							add = false;
					
					if (add)	
						RHSAtt = nonKeyAttrs[position];
				}
				
				scenario.getDocFac().addFD(r.getName(), LHSAtts, new String[] { RHSAtt });
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
	 */
	static String[] getNonKeyAttributes(RelationType r, MappingScenario scenario) throws Exception
	{
		// get the positions of all the primary keys
		int[] pkPos = scenario.getDoc().getPKPos(r.getName(), true);
		
		// get positions for all of the attributes
		int[] attrPos = new int[r.getAttrArray().length];
		for (int i = 0; i <= r.getAttrArray().length; i++)
			attrPos[i] = i;
					
		// strip out the positions of the keys
		List attrPosList = Arrays.asList(attrPos);
		for (int i = 0; i < pkPos.length; i++)
			if (attrPosList.contains(pkPos[i]))
				attrPosList.remove(pkPos[i]);
		
		// attrpos is now an array of the positions of all the nonkey attributes
		attrPos = toIntArray(attrPosList);
		
		String[] nonKeyAttrs = scenario.getDoc().getAttrNames(r.getName(), attrPos, true); 
		
		return nonKeyAttrs;
	}

	/**
	 * Converts a list to an array of integers
	 * 
	 * @param	list	 	The list to be converted
	 * @return				An integer array with the contents of the list
	 */
	static int[] toIntArray(List list) 
	{
		int[] intArray = new int[list.size()];
		
		for (int i = 0; i < list.size(); i++) 
			intArray[i] = ((Number)list.get(i)).intValue();
		
		return intArray;
	}
	
}
