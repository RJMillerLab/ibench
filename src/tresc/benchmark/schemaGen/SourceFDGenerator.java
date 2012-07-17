package tresc.benchmark.schemaGen;

import java.util.Arrays;
import java.util.Random;
import java.util.Vector;

import org.vagabond.xmlmodel.FDType;
import org.vagabond.xmlmodel.RelationType;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;

/**
 * Randomly generates functional dependencies after all scenarios have been generated.
 * 
 * @author mdangelo
 */
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
				String[] pkAttrs = scenario.getDoc().getPK(r.getName(), true);
				String[] nonKeyAttrs = getNonKeyAttributes(r, scenario);
				
				scenario.getDocFac().addFD(r.getName(), pkAttrs, nonKeyAttrs);
			}
		}
		
		// create random FDs by randomly selecting non primary key attributes and linking them
		for(RelationType r: scenario.getDoc().getSchema(true).getRelationArray()) 
		{
			// determine how many FDs we should generate for the relation based on the number of attributes it has
			double percentage = ((double) configuration.getParam(Constants.ParameterName.SourceFDPerc))/(double) 100;
			int numAtts = r.getAttrArray().length;
			int numFDs = (int) Math.floor(percentage * numAtts);
			
			// get positions for all of the attributes
			int[] attrPos = new int[r.getAttrArray().length];
			for (int i = 0; i < r.getAttrArray().length; i++)
				attrPos[i] = i;
				
			// if there is a primary key then we must strip out the attributes associated with it, otherwise we just grab all the attributes
			String[] nonKeyAttrs = (r.isSetPrimaryKey()) ? getNonKeyAttributes(r, scenario) : scenario.getDoc().getAttrNames(r.getName(), attrPos, true);
			
			int max_tries = 20;
			// randomly select attributes for each run of FD generation
			for (int i = 0; i < numFDs; i++)
			{	
				int numLHSAtts = _generator.nextInt(nonKeyAttrs.length/2) + 1;
				
				Vector<String> LHSAtts = new Vector<String> ();
				String RHSAtt = "";
				
				Boolean add = true;
				int j = 0;
				int tries = 0;
				// pick the attributes to go on the left hand side
				while (j < numLHSAtts && tries < max_tries)
				{
					int position = _generator.nextInt(nonKeyAttrs.length);
					
					// make sure that we haven't already added this to our LHS FDs to avoid redundancy 
					// ex. ABA -> C
					if (LHSAtts.indexOf(nonKeyAttrs[position]) != -1)
						add = false;
					
					if (add)	
					{
						LHSAtts.add(nonKeyAttrs[position]);
						j++;
					}
					else
						tries++;
				}
				
				//String[] LHSAtts = convertVectorToStringArray(LHSAtts);
				Boolean done;
				
				// keep trying to find a RHS attribute until we have added one
				do 
				{
					done = true;
					
					// pick the attribute to go on the right hand side
					int position = _generator.nextInt(nonKeyAttrs.length);
					
					// make sure it hasn't been added to the LHS attributes to avoid nonsensical FDs
					// ex. AB -> A
					if (LHSAtts.indexOf(nonKeyAttrs[position]) != -1)
						done = false;
					
					if (done)	
						RHSAtt = nonKeyAttrs[position];
				} while (!done);
				
				// look through all of the existing FDs and check if we would be adding a duplicates
				FDType[] functionalDep = scenario.getDocFac().getRelFDs(r.getName());
				Boolean duplicate = false;
				for (FDType fd : functionalDep)
					if (fd.getTo().getAttrArray(0).equals(RHSAtt) && Arrays.equals(fd.getFrom().getAttrArray(), convertVectorToStringArray(LHSAtts)))
						duplicate = true;
				
				// if the FD was a duplicate then we must create a new FD in its place so decrement the counter, otherwise we add the FD to the relation
				if (duplicate)
					i--;
				else
					scenario.getDocFac().addFD(r.getName(), convertVectorToStringArray(LHSAtts), new String[] { RHSAtt });
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
