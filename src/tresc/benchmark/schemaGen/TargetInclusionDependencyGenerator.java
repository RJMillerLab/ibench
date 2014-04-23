package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.vagabond.util.CollectionUtils;
import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.RelationType;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Constants.ParameterName;

//MN this class generates random target inclusion dependencies -3 April 2014
//MN how to output random regular inclusion dependencies? - 12 April 2014
//MN only supports one attribute - one attribute inclusion dependencies, checks only two ways cyclic paths, does not allow self-referring inclusion dependencies
public class TargetInclusionDependencyGenerator implements ScenarioGenerator {
	
	//MN this attribute has been considered so that we could inject random target inclusion dependencies into mappings - 14 April 2014
	//MN do I need to new the List? - 14 April 2014
	ArrayList<String> tids;
	
	private class InclusionDependency{
		public String fromRelName;
		public String fromRelAttr;
		public String toRelName;
		public String toRelAttr;
		public String toString = null;
		
		public InclusionDependency(String fromRelName, String fromRelAttr, String toRelName, String toRelAttr){
			this.fromRelName = fromRelName;
			this.fromRelAttr = fromRelAttr;
			this.toRelName = toRelName;
			this.toRelAttr = toRelAttr;
		}
		
		@Override
		public String toString () {
			if (toString == null) {
				toString = fromRelName + "|" +  fromRelAttr + "|" + toRelName +
						   "|" + toRelAttr;
			}
			return toString;
		}
	}

	static Logger log = Logger.getLogger(SourceInclusionDependencyGenerator.class);
	
	//attempts to generate random target inclusion dependencies
	@Override
	public void generateScenario(MappingScenario scenario,
			Configuration configuration) throws Exception {
		// TODO Auto-generated method stub
		Random _generator = configuration.getRandomGenerator();
		Map<String, Map<String,InclusionDependency>> ids = new HashMap<String, Map<String,InclusionDependency>> ();
		
		if (log.isDebugEnabled()) {log.debug("Attempting to Generate Random Target Inclusion Dependencies");};

		//MN new ArrayList<String> - 14 April 2014
		tids = new ArrayList<String> ();
		
		if (configuration.getParam(ParameterName.TargetInclusionDependencyPerc) >0) {
			generateRandomIDs(scenario, configuration, _generator, ids);
		}
		
	}
	
	//MN this method returns random target inclusion dependencies - 14 April 2014
	public ArrayList<String> getRandomTargetIDs(){
		return tids;
	}
	
	//generates random target regular inclusion dependencies and foreign keys
	public void generateRandomIDs (MappingScenario scenario, Configuration configuration, Random _generator,
	Map<String, Map<String, InclusionDependency>> ids) throws Exception{
		
		double sourceIDPerc = (double) configuration.getParam(Constants.ParameterName.TargetInclusionDependencyPerc);
		double sourceIDFK = (double) configuration.getParam(Constants.ParameterName.TargetInclusionDependencyFKPerc);
		
		//we calculate number of inclusion dependencies and number of foreign keys that need to be generated
		double percentage = ((double) sourceIDPerc)/(double) 100;
		int numSourceRels = scenario.getDoc().getSchema(false).getRelationArray().length;
		int numIDs = (int) Math.floor(percentage * numSourceRels);
		
		double percentagee = ((double) sourceIDFK)/(double) 100;
		int numIDFKs = (int) Math.floor(percentagee * numIDs);
		
		//first, we generate required number of regular inclusion dependencies
		if(numIDs-numIDFKs>0){
			if (log.isDebugEnabled()) {log.debug("Generating Random Target Regular Inclusion Dependencies: ");};
			generateRandomRegularInclusionDependency(numIDs, numIDFKs, scenario, configuration, _generator, ids);}
		
		//second, we generate required number of foreign keys
		if(numIDFKs > 0){
			if (log.isDebugEnabled()) {log.debug("Generating Random Target Foreign Keys: ");};
			generateRandomForeignKey(numIDFKs, scenario, configuration, _generator, ids);
		}
	}
	
	private void generateRandomForeignKey(int numIDFKs, MappingScenario scenario, Configuration configuration, Random _generator,
			Map<String, Map<String, InclusionDependency>> ids) throws Exception{
		for(int i=0; i<numIDFKs; i++){
			RelationType[] rels = scenario.getDoc().getSchema(false).getRelationArray();
			
			boolean done = false;
			
			int max_tries = 10;
			while(!done && (max_tries>0)){
				//we roll dice to choose from and to relations for foreign key
				int fromRelIndex = _generator.nextInt(rels.length-1);
				int toRelIndex = -1;
				
				int max_triesIn = 10;
				boolean doneIn = false;
				while(max_triesIn>0 && ! doneIn){
					toRelIndex = _generator.nextInt(rels.length-1);
					//self-referring inclusion dependencies are not allowed - note that I considered isSetPrimaryKey for generating foreign key
					if((toRelIndex != fromRelIndex) && (rels[toRelIndex].isSetPrimaryKey()))
						doneIn=true;
					else
						max_triesIn--;
				}
		
				//it would be better to print it in log that we could not generate random regular inclusion dependencies
				if(!doneIn)
					break;
				
				//we roll dice to choose from rel attr and to rel attr for foreign key
				int[] fromAttrPos = CollectionUtils.createSequence(0, rels[fromRelIndex].sizeOfAttrArray()); 
				String[] fromNonKeyAttrs = (rels[fromRelIndex].isSetPrimaryKey()) ? getNonKeyAttributes(rels[fromRelIndex], scenario) : scenario.getDoc().getAttrNames(rels[fromRelIndex].getName(),fromAttrPos, false);
			
				String[] toPKAttrs = scenario.getDoc().getPK(rels[toRelIndex].getName(), false);
				
				int toRelAttrIndex = 0;
				if(toPKAttrs.length>1)
					toRelAttrIndex = _generator.nextInt(toPKAttrs.length-1);
				
				int fromRelAttrIndex = -1;
				//MN checking that both from and to attributes have the same type
				
				//MN get toRelAttrType
				AttrDefType[] toAttrs = rels[toRelIndex].getAttrArray();
				String toRelAttrType = null;
				for(int index=0; index<toAttrs.length; index++)
					if(toAttrs[index].toString().substring(toAttrs[index].toString().indexOf("<Name>") + 6, toAttrs[index].toString().indexOf("</Name>")).equals(toPKAttrs[toRelAttrIndex])){
						toRelAttrType = toAttrs[index].toString().substring(toAttrs[index].toString().indexOf("<DataType>") + 10, toAttrs[index].toString().indexOf("</DataType>"));
						break;}
				
				int max_triesAttr = 10;
				boolean doneAttr = false;
				while(max_triesAttr>0 && !doneAttr){
					if(fromNonKeyAttrs.length>1)
						fromRelAttrIndex = _generator.nextInt(fromNonKeyAttrs.length-1);
					else
						fromRelAttrIndex = 0;
					
					//MN the way to compute attr type has been changed - 11 April 2014
					
					//MN get fromRelAttrType - 11 April 2014
					AttrDefType[] fromAttrs = rels[fromRelIndex].getAttrArray();
					String fromRelAttrType = null;
					for(int index=0; index<fromAttrs.length; index++)
						if(fromAttrs[index].toString().substring(fromAttrs[index].toString().indexOf("<Name>") + 6, fromAttrs[index].toString().indexOf("</Name>")).equals(fromNonKeyAttrs[fromRelAttrIndex])){
							fromRelAttrType = fromAttrs[index].toString().substring(fromAttrs[index].toString().indexOf("<DataType>") + 10, fromAttrs[index].toString().indexOf("</DataType>"));
							break;}
					
					//String type = scenario.getSource().getSubElement(fromRelIndex).getSubElement(fromRelAttrIndex).getType().toString();
					if(toRelAttrType.equals(fromRelAttrType))
						doneAttr = true;
					else
						max_triesAttr --;
				}
				
				if(!doneAttr)
					break;
		
				if(existsID(ids, rels[fromRelIndex], rels[toRelIndex], fromNonKeyAttrs[fromRelAttrIndex], toPKAttrs[toRelAttrIndex])){
					max_tries--;
				}
				else{
					if(((int)(configuration.getParam(Constants.ParameterName.TargetCircularFK)) == 0) &&
							isCircularInclusionDependency(ids, rels[fromRelIndex].getName(), rels[toRelIndex].getName(), fromNonKeyAttrs[fromRelAttrIndex], toPKAttrs[toRelAttrIndex])){
						max_tries--;
					}
					else{
						//add foreign key
						if (addInclusionDependency (ids, rels[fromRelIndex].getName(), rels[toRelIndex].getName(), fromNonKeyAttrs[fromRelAttrIndex], toPKAttrs[toRelAttrIndex], true)) {

							scenario.getDocFac().addForeignKey(rels[fromRelIndex].getName(), fromNonKeyAttrs[fromRelAttrIndex], rels[toRelIndex].getName(), toPKAttrs[toRelAttrIndex], false);
							
							if (log.isDebugEnabled()) {
								log.debug("--------- GENERATING NEW RANDOM TARGET FOREIGN KEY---------");
								log.debug("fromRelName: " + rels[fromRelIndex].getName());
								log.debug("toRelName: " + rels[toRelIndex].getName());
								log.debug("fromRelAttrName: " + fromNonKeyAttrs[fromRelAttrIndex]);
								log.debug("toRelAttrName: " + toPKAttrs[toRelAttrIndex]);
							}
							done = true;
						}
						else
							max_tries --;
					}
				}
			}
		}
	}
	
	private void generateRandomRegularInclusionDependency(int numIDs, int numIDFKs, MappingScenario scenario, 
			Configuration configuration, Random _generator,
			Map<String, Map<String, InclusionDependency>> ids) throws Exception{
		for(int i=0; i<numIDs-numIDFKs; i++){
			RelationType[] rels = scenario.getDoc().getSchema(false).getRelationArray();
		
			boolean done = false;
			
			int max_tries = 10;
			while (!done && (max_tries > 0)) {
				//we roll dice to choose from and to relations for regular inclusion dependencies
				int fromRelIndex = _generator.nextInt(rels.length-1);
				int toRelIndex = -1;
				
				int max_triesIn = 10;
				boolean doneIn = false;
				while(max_triesIn>0 && ! doneIn){
					toRelIndex = _generator.nextInt(rels.length-1);
					//self-referring inclusion dependencies are not allowed
					if((toRelIndex != fromRelIndex))
						doneIn=true;
					else
						max_triesIn--;
				}
		
				//it would be better to print it in log that we could not generate random regular inclusion dependencies
				if(!doneIn)
					break;
				
				//we roll dice to choose from rel attr and to rel attr for regular inclusion dependencies
				int[] fromAttrPos = CollectionUtils.createSequence(0, rels[fromRelIndex].sizeOfAttrArray()); 
				String[] fromNonKeyAttrs = (rels[fromRelIndex].isSetPrimaryKey()) ? getNonKeyAttributes(rels[fromRelIndex], scenario) : scenario.getDoc().getAttrNames(rels[fromRelIndex].getName(),fromAttrPos, false);
		
				int[] toAttrPos = CollectionUtils.createSequence(0, rels[toRelIndex].sizeOfAttrArray()); 
				String[] toNonKeyAttrs = (rels[toRelIndex].isSetPrimaryKey()) ? getNonKeyAttributes(rels[toRelIndex], scenario) : scenario.getDoc().getAttrNames(rels[toRelIndex].getName(),toAttrPos, false);
		
				int fromRelAttrIndex = 0;
				if(fromNonKeyAttrs.length>1)
					fromRelAttrIndex = _generator.nextInt(fromNonKeyAttrs.length-1);
				
				int toRelAttrIndex = -1;
				//MN checking that both from and to attributes have the same type
				int max_triesAttr = 10;
				boolean doneAttr = false;
				
				//MN compute fromRelAttrType - 11 April 2014
				AttrDefType[] fromAttrs = rels[fromRelIndex].getAttrArray();
				String fromRelAttrType = null;
				for(int index=0; index<fromAttrs.length; index++)
					if(fromAttrs[index].toString().substring(fromAttrs[index].toString().indexOf("<Name>") + 6, fromAttrs[index].toString().indexOf("</Name>")).equals(fromNonKeyAttrs[fromRelAttrIndex])){
						fromRelAttrType = fromAttrs[index].toString().substring(fromAttrs[index].toString().indexOf("<DataType>") + 10, fromAttrs[index].toString().indexOf("</DataType>"));
						break;}
				
				while(max_triesAttr>0 && !doneAttr){
					if(toNonKeyAttrs.length>1)
						toRelAttrIndex = _generator.nextInt(toNonKeyAttrs.length-1);
					else
						toRelAttrIndex = 0;
					
					//MN the way to compute attr type has been changed - 11 April 2014
					
					//MN get toRelAttrType - 11 April 2014
					AttrDefType[] toAttrs = rels[toRelIndex].getAttrArray();
					String toRelAttrType = null;
					for(int index=0; index<toAttrs.length; index++)
						if(toAttrs[index].toString().substring(toAttrs[index].toString().indexOf("<Name>") + 6, toAttrs[index].toString().indexOf("</Name>")).equals(toNonKeyAttrs[toRelAttrIndex]))
							{toRelAttrType = toAttrs[index].toString().substring(toAttrs[index].toString().indexOf("<DataType>") + 10, toAttrs[index].toString().indexOf("</DataType>"));
							 break;}
					
					if(fromRelAttrType.equals(toRelAttrType))
						doneAttr = true;
					else
						max_triesAttr --;
				}
				
				if(!doneAttr)
					break;
		
		
				if(existsID(ids, rels[fromRelIndex], rels[toRelIndex], fromNonKeyAttrs[fromRelAttrIndex], toNonKeyAttrs[toRelAttrIndex])){
					max_tries--;
				}
				else{
					if(((int)(configuration.getParam(Constants.ParameterName.TargetCircularInclusionDependency)) == 0) &&
							isCircularInclusionDependency(ids, rels[fromRelIndex].getName(), rels[toRelIndex].getName(), fromNonKeyAttrs[fromRelAttrIndex], toNonKeyAttrs[toRelAttrIndex])){
						max_tries--;
					}
					else{
						//add inclusion dependency
						if (addInclusionDependency (ids, rels[fromRelIndex].getName(), rels[toRelIndex].getName(), fromNonKeyAttrs[fromRelAttrIndex], toNonKeyAttrs[toRelAttrIndex], false)) {

							//I need to implement something like below
							//scenario.getDocFac().addFD(r.getName(), arrayLHS, new String[] { RHSAtt });
						
							if (log.isDebugEnabled()) {
								log.debug("--------- GENERATING NEW RANDOM TARGET REGULAR INCLUSION DEPENDENCY---------");
								log.debug("fromRelName: " + rels[fromRelIndex].getName());
								log.debug("toRelName: " + rels[toRelIndex].getName());
								log.debug("fromRelAttrName: " + fromNonKeyAttrs[fromRelAttrIndex]);
								log.debug("toRelAttrName: " + toNonKeyAttrs[toRelAttrIndex]);
							}
							done = true;
						}
						else
							max_tries --;
					}
				}
				
			}
		}
	}
	
	//checks if there exists an inclusion dependency with reverse relations
	private boolean isCircularInclusionDependency (Map<String, Map<String, InclusionDependency>> ids, String from, String to, String fromAttr, String toAttr)
	{
		for(int i=0; i<ids.size(); i++)
		{
			if (ids.containsKey(to + toAttr + from + fromAttr))
				return true;
		}
		return false;
	}
	
	//checks if there exists an inclusion dependency with the same from and to relations
	private boolean existsID (Map<String, Map<String, InclusionDependency>> ids, RelationType from, RelationType to, String fromAttr, String toAttr)
	{
		for (int i=0; i< ids.size(); i++)
		{
			if (ids.containsKey(from.getName() + fromAttr + to.getName() + toAttr))
				return true;
		}
		return false;
	}
	
	private boolean addInclusionDependency (Map<String, Map<String,InclusionDependency>> ids, String fromRelName, String toRelName,
			String fromRelAttrName, String toRelAttrName, boolean foreignKey){
		InclusionDependency id = new InclusionDependency (fromRelName, fromRelAttrName, toRelName, toRelAttrName);
		
		Map<String, InclusionDependency> relMap;
		
		if (!ids.containsKey(fromRelName + fromRelAttrName + toRelName + toRelAttrName)) {
			relMap = new HashMap<String, InclusionDependency> ();
			ids.put(fromRelName + fromRelAttrName + toRelName + toRelAttrName, relMap);
			
			//MN add random target inclusion dependency - 14 April 2014
			if(!foreignKey)
				tids.add(id.toString());
		}
		else {
			relMap = ids.get(fromRelName + fromRelAttrName + toRelName + toRelAttrName);
		}
		
		if (relMap.get(id.toString()) == null) {
			relMap.put(id.toString(), id);
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
		int[] pkPos = scenario.getDoc().getPKPos(r.getName(), false);

		// get positions for all of the attributes
		int[] attrPos = new int[r.getAttrArray().length];
		for (int i = 0; i < r.getAttrArray().length; i++)
			attrPos[i] = i;

		int[] nonKeyPos = stripOutPKPositions(r.getAttrArray().length, pkPos, attrPos);

		String[] nonKeyAttrs = scenario.getDoc().getAttrNames(r.getName(), nonKeyPos, false);

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
