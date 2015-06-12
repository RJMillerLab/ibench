package tresc.benchmark.schemaGen;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.benchmark.model.TrampModelFactory;
import org.vagabond.util.CollectionUtils;
import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.RelationType;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Constants.ParameterName;
import vtools.dataModel.expression.Rule;


//MN this class generates random source inclusion dependencies - 3 April 2014
//MN how to output random regular inclusion dependencies? - 12 April 2014
//MN only supports one attribute - one attribute inclusion dependencies, checks only two ways cyclic paths, does not allow self-referring inclusion dependencies
//MN added isCircularInclusionDependencyFK - 28 May 2014
//MN FIXED random foreign key generator to correctly set foreign keys when primaryKeySize>1 - 24 June 2014
//MN ToDo: fix random regular inclusion dependency generator to support primaryKeySize>1 - 24 June 2014
//MN       at the moment, we don't have the feature - 24 June 2014

public class SourceInclusionDependencyGenerator implements ScenarioGenerator {
	
	//MN this attribute has been considered so that we could inject random source inclusion dependencies into mappings - 14 April 2014
	ArrayList<String> sids;
	
	private class InclusionDependency{
		public String fromRelName;
		public String[] fromRelAttr;
		public String toRelName;
		public String[] toRelAttr;
		public String toString = null;
		
		public InclusionDependency(String fromRelName, String[] fromRelAttr, String toRelName, String[] toRelAttr){
			this.fromRelName = fromRelName;
			this.fromRelAttr = new String [fromRelAttr.length];
			for(int i=0; i<fromRelAttr.length; i++)
				this.fromRelAttr[i] = fromRelAttr[i];
			this.toRelName = toRelName;
			this.toRelAttr = new String[toRelAttr.length];
			for(int i=0; i<toRelAttr.length; i++)
				this.toRelAttr[i] = toRelAttr[i];
		}
		
		@Override
		public String toString () {
			if (toString == null) {
				String temp1 = this.fromRelAttr[0];
				for(int indx=1; indx<this.fromRelAttr.length; indx++)
					temp1 += this.fromRelAttr[indx];
				
				String temp2 = this.toRelAttr[0];
				for(int indx=1; indx<this.toRelAttr.length; indx++)
					temp2 += this.toRelAttr[indx];
				
				toString = fromRelName + "|" +  temp1 + "|" + toRelName +
						   "|" + temp2;
			}
			return toString;
		}
	}

	static Logger log = Logger.getLogger(SourceInclusionDependencyGenerator.class);
	
	//MN attempts to generate random source inclusion dependencies
	@Override
	public void generateScenario(MappingScenario scenario,
			Configuration configuration) throws Exception {
		// TODO Auto-generated method stub
		Random _generator = configuration.getRandomGenerator();
		Map<String, Map<String,InclusionDependency>> ids = new HashMap<String, Map<String,InclusionDependency>> ();
		
		if (log.isDebugEnabled()) {log.debug("Attempting to Generate Random Source Inclusion Dependencies");};

		//MN new ArrayList<String> - 14 April 2014
		sids = new ArrayList<String> ();
		
		if (configuration.getParam(ParameterName.SourceInclusionDependencyPerc) >0) {
			generateRandomIDs(scenario, configuration, _generator, ids);
		}
		
	}
	
	//MN this method returns random source inclusion dependencies - 14 April 2014
	public ArrayList<String> getRandomSourceIDs(){
		return sids;
	}
	
	//MN generates random source regular inclusion dependencies
	public void generateRandomIDs (MappingScenario scenario, Configuration configuration, Random _generator,
	Map<String, Map<String, InclusionDependency>> ids) throws Exception{
		
		double sourceIDPerc = (double) configuration.getParam(Constants.ParameterName.SourceInclusionDependencyPerc);
		double sourceIDFK = (double) configuration.getParam(Constants.ParameterName.SourceInclusionDependencyFKPerc);
		
		//we calculate number of inclusion dependencies and number of foreign keys that need to be generated
		//example: suppose there are 10 source relations, 80% sourceInclusionDependenciesPerc, 30% sourceForeignKeysPerc
		//numIDs (number of random source inclusion dependencies that should be generated: 8
		//numIDFKs (number of random source foreign keys that should be generated) : 2
		double percentage = ((double) sourceIDPerc)/(double) 100;
		int numSourceRels = scenario.getDoc().getSchema(true).getRelationArray().length;
		int numIDs = (int) Math.floor(percentage * numSourceRels);
		
		double percentagee = ((double) sourceIDFK)/(double) 100;
		int numIDFKs = (int) Math.floor(percentagee * numIDs);
		
		//first, we generate required number of regular inclusion dependencies
		if(numIDs-numIDFKs>0){
			if (log.isDebugEnabled()) {log.debug("Generating Random Source Regular Inclusion Dependencies: ");};
			generateRandomRegularInclusionDependency(numIDs, numIDFKs, scenario, configuration, _generator, ids);
		}
		
		//second, we generate required number of foreign keys
		if(numIDFKs > 0){
			if (log.isDebugEnabled()) {log.debug("Generating Random Source Foreign Keys: ");};
			generateRandomForeignKey(numIDFKs, scenario, configuration, _generator, ids);
		}
	}
	
	//MN generates random source foreign keys
	private void generateRandomForeignKey(int numIDFKs, MappingScenario scenario, Configuration configuration, Random _generator,
			Map<String, Map<String, InclusionDependency>> ids) throws Exception {
		Set<String[]> notPossible = new HashSet<String[]> ();
		boolean sourceCircluarFKs = configuration.getParam(Constants.ParameterName.SourceCircularFK) == 0;
		
		for(int i=0; i<numIDFKs; i++) {
			RelationType[] rels = scenario.getDoc().getSchema(true).getRelationArray();
			
			boolean done = false;
			
			int max_tries = 10;
			while(max_tries-- > 0 && !done) {
				int fromRelIndex = _generator.nextInt(rels.length-1);
				int toRelIndex = _generator.nextInt(rels.length-1);
				
				// we already know it is not possible to add FKs between these two relation then continue
				if (notPossible.contains(new String[] { rels[fromRelIndex].getName(), rels[toRelIndex].getName()}))
					continue;
				
				// not same relations
				if (fromRelIndex == toRelIndex)
					continue;
				
				// to relation has to have a PK
				if (!rels[toRelIndex].isSetPrimaryKey())
					continue;
				
				// from relation has to have more attributes than to relation PK
				if (rels[toRelIndex].getPrimaryKey().sizeOfAttrArray() > rels[fromRelIndex].getAttrArray().length)
					continue;

				// get to PK attribute information
				String[] toPKAttrs = scenario.getDoc().getPK(rels[toRelIndex].getName(), true);
				AttrDefType[] toAttrs = rels[toRelIndex].getAttrArray();
				String[] toRelAttrType = new String[toPKAttrs.length];
				for(int j=0; j<toPKAttrs.length; j++)
					for(int index=0; index<toAttrs.length; index++)
						if(toAttrs[index].getName().equals(toPKAttrs[j])){
							toRelAttrType[j] = toAttrs[index].getDataType();
							break;
						}
				
				// determine from relation attributes that could be used for FK
				String[] fromAttrs = new String [toPKAttrs.length];
				int[] fromAttrsPos = new int [toPKAttrs.length];
				String[] fromRelAttrType = new String [toPKAttrs.length];
				int max_triesAttr = 10;
				boolean doneAttr = false;
				AttrDefType[] allFromAttrs = rels[fromRelIndex].getAttrArray();
				
				while(max_triesAttr-- > 0 && !doneAttr) {
					// create list of attr pos to pick from
					ArrayList<Integer> allAttrPos = new ArrayList<Integer> ();
					for(int j = 0; j < allFromAttrs.length; j++)
						allAttrPos.add(j);
					
					// determine random set of from attributes 
					for(int j=0; j<toPKAttrs.length; j++){
						int fromRelAttrIndex = _generator.nextInt(allAttrPos.size());
						allAttrPos.remove(fromRelAttrIndex);						
						
						fromAttrs[j] = allFromAttrs[fromRelAttrIndex].getName();
						fromAttrsPos[j] = fromRelAttrIndex;
						fromRelAttrType[j] = allFromAttrs[fromRelAttrIndex].getDataType();
					}

					// check that attribute types are the same for the picked from attributes and the to PK attributes
					boolean failed = false;
					for(int typeIndx=0; typeIndx<toPKAttrs.length; typeIndx++)
						if(!fromRelAttrType[typeIndx].equals(toRelAttrType[typeIndx])) {
							failed = true;
							break;
						}
				
					// if we reach here we found matching attributes
					doneAttr = !failed;
				}

				// if FK already exists then fail
				if(existsID(ids, rels[fromRelIndex], rels[toRelIndex], fromAttrs, toPKAttrs))
					continue;
				
				// if it would create a circular inclusion dependency and the user has requested that no such dependencies should exist then fail
				if(sourceCircluarFKs &&
						isCircularInclusionDependencyFK(scenario, ids, rels[fromRelIndex].getName(), 
								rels[toRelIndex].getName(), fromAttrs, toPKAttrs))
					continue;
					
				// add inclusion dependency
				if (addInclusionDependency (ids, rels[fromRelIndex].getName(), rels[toRelIndex].getName(), 
						fromAttrs, toPKAttrs, true)) {
					TrampModelFactory fac = scenario.getDocFac();
					fac.addForeignKey(rels[fromRelIndex].getName(), fromAttrs, 
							rels[toRelIndex].getName(), toPKAttrs, true);

					if (log.isDebugEnabled()) {
						log.debug("--------- GENERATING NEW RANDOM SOURCE FOREIGN KEY---------");
						log.debug("fromRelName: " + rels[fromRelIndex].getName());
						log.debug("toRelName: " + rels[toRelIndex].getName());
						String temp1 = fromAttrs[0];
						for(int k=1; k<fromAttrs.length; k++)
							temp1 += fromAttrs[k];
						log.debug("fromRelAttrName: " + temp1);
						String temp2 = toPKAttrs[0];
						for(int k=1; k<toPKAttrs.length; k++)
							temp2 += toPKAttrs[k];
						log.debug("toRelAttrName: " + temp2);
					}
					done = true;
				}
			}
			
			log.debug("attempt to create FK - " + ((done) ? "SUCCESS" : "FAILED"));
		}	
			
//			
//			while(!done && (max_tries>0)){
//				//we roll dice to choose from and to relations for generating foreign keys
//				int fromRelIndex = _generator.nextInt(rels.length-1);
//				int toRelIndex = -1;
//				
//				int max_triesIn = 10;
//				boolean doneIn = false;
//				while(max_triesIn>0 && ! doneIn){
//					toRelIndex = _generator.nextInt(rels.length-1);
//					//self-referring inclusion dependencies are not allowed - 
//					//note that I considered isSetPrimaryKey for generating foreign key
//					//MN size of foreign key should be equal to size of primary key; 
//					//MN so, I added the new condition to if clause - 24 June 2014
//					if((toRelIndex != fromRelIndex) && (rels[toRelIndex].isSetPrimaryKey()) && 
//							rels[toRelIndex].getPrimaryKey().sizeOfAttrArray()<=rels[fromRelIndex].getAttrArray().length)
//						doneIn=true;
//					else
//						max_triesIn--;
//				}
//		
//				//it would be better to print it in log that we could not generate random regular inclusion dependencies
//				if(!doneIn)
//					break;
//				
//				//we roll dice to choose from rel attr and to rel attr for foreign key
//			
//				//MN toRelAttrs - 24 June 2014
//				String[] toPKAttrs = scenario.getDoc().getPK(rels[toRelIndex].getName(), true);
//				
//				//int toRelAttrIndex = 0;
//				//if(toPKAttrs.length > 1)
//					//toRelAttrIndex = _generator.nextInt(toPKAttrs.length-1);
//				
//				//int fromRelAttrIndex = -1;
//				//MN checking that both from and to attributes have the same type
//				
//				//MN get toRelAttrsTypes - 24 June 2014
//				AttrDefType[] toAttrs = rels[toRelIndex].getAttrArray();
//				String[] toRelAttrType = new String[toPKAttrs.length];
//				for(int j=0; j<toPKAttrs.length; j++)
//					for(int index=0; index<toAttrs.length; index++)
//						if(toAttrs[index].getName().equals(toPKAttrs[j])){
//							toRelAttrType[j] = toAttrs[index].getDataType();
//							break;
//						}
//				
//				//MN *****I have toRelAttrs and their types - 24 June 2014
//				//int[] fromAttrPos = CollectionUtils.createSequence(0, rels[fromRelIndex].sizeOfAttrArray()); 
//				//String[] fromNonKeyAttrs = (rels[fromRelIndex].isSetPrimaryKey()) ? getNonKeyAttributes(rels[fromRelIndex], scenario) 
//				//		: scenario.getDoc().getAttrNames(rels[fromRelIndex].getName(),fromAttrPos, true);
//				
//				String[] fromAttrs = new String [toPKAttrs.length];
//				int[] fromAttrsPos = new int [toPKAttrs.length];
//				
//				int max_triesAttr = 10;
//				boolean doneAttr = false;
//				while(max_triesAttr>0 && !doneAttr){
//					
//					for(int j=0; j<toPKAttrs.length; j++){
//						int fromRelAttrIndex = _generator.nextInt(rels[fromRelIndex].sizeOfAttrArray()-1);
//						boolean same =false;
//						for(int g=0; g<j; g++)
//							if(fromAttrsPos[g] == fromRelAttrIndex)
//								same=true;
//						if(!same){
//							fromAttrs[j] = rels[fromRelIndex].getAttrArray(fromRelAttrIndex).getName();
//							fromAttrsPos[j] = fromRelAttrIndex;
//						}
//						else{
//							j--;
//						}
//					}
//
//					//MN get fromRelAttrsTypes - 24 June 2014
//					AttrDefType[] fromAttrsDef = rels[fromRelIndex].getAttrArray();
//					String[] fromRelAttrType = new String [toPKAttrs.length];
//					for(int j=0; j<toPKAttrs.length; j++) {
//						for(int index=0; index<fromAttrsDef.length; index++)
//							if(fromAttrsDef[index].toString().substring(fromAttrsDef[index].toString().indexOf("<Name>") + 6, 
//								fromAttrsDef[index].toString().indexOf("</Name>")).equals(fromAttrs[j])) {
//								fromRelAttrType[j] = fromAttrsDef[index].toString().substring(fromAttrsDef[index].toString().indexOf("<DataType>") + 10, 
//										fromAttrsDef[index].toString().indexOf("</DataType>"));
//								break;
//							}
//					}
//					//String type = scenario.getSource().getSubElement(fromRelIndex).getSubElement(fromRelAttrIndex).getType().toString();
//					doneAttr=true;
//					for(int typeIndx=0; typeIndx<toPKAttrs.length; typeIndx++)
//						if(!fromRelAttrType[typeIndx].equals(toRelAttrType[typeIndx]))
//							doneAttr = false;
//					//if(fromRelAttrType.equals(toRelAttrType))
//						//doneAttr = true;
//					//else
//					if(!doneAttr)
//						max_triesAttr --;
//				}
//				
//				if(!doneAttr)
//					break;
//		
//				//TODO fail early, check before
//				if(existsID(ids, rels[fromRelIndex], rels[toRelIndex], fromAttrs, toPKAttrs)){
//					max_tries--;
//				}
//				else{
//					if(sourceCircluarFKs &&
//							isCircularInclusionDependencyFK(scenario, ids, rels[fromRelIndex].getName(), 
//									rels[toRelIndex].getName(), fromAttrs, toPKAttrs)){
//						max_tries--;
//					}
//					else{
//						//add foreign key
//						if (addInclusionDependency (ids, rels[fromRelIndex].getName(), rels[toRelIndex].getName(), 
//								fromAttrs, toPKAttrs, true)) {
//
//							//MN BEGIN 24 August 2014 - corrected the way foreign keys are stored 
//							//for(int count=0; count<fromAttrs.length; count++){
//							TrampModelFactory fac = scenario.getDocFac();
//							fac.addForeignKey(rels[fromRelIndex].getName(), fromAttrs, 
//									rels[toRelIndex].getName(), toPKAttrs, true);
//							//}
//							//MN END
//							if (log.isDebugEnabled()) {
//								log.debug("--------- GENERATING NEW RANDOM SOURCE FOREIGN KEY---------");
//								log.debug("fromRelName: " + rels[fromRelIndex].getName());
//								log.debug("toRelName: " + rels[toRelIndex].getName());
//								String temp1 = fromAttrs[0];
//								for(int k=1; k<fromAttrs.length; k++)
//									temp1 += fromAttrs[k];
//								log.debug("fromRelAttrName: " + temp1);
//								String temp2 = toPKAttrs[0];
//								for(int k=1; k<toPKAttrs.length; k++)
//									temp2 += toPKAttrs[k];
//								log.debug("toRelAttrName: " + temp2);
//							}
//							done = true;
//						}
//						else
//							max_tries --;
//					}
//				}
//			}
		
	}
	
	private void generateRandomRegularInclusionDependency(int numIDs, int numIDFKs, MappingScenario scenario, 
			Configuration configuration, Random _generator,
			Map<String, Map<String, InclusionDependency>> ids) throws Exception{
		//for(int i=0; i<numIDs-numIDFKs; i++){
			//RelationType[] rels = scenario.getDoc().getSchema(true).getRelationArray();
		
			//boolean done = false;
			
			//int max_tries = 10;
			//while (!done && (max_tries > 0)) {
				//we roll dice to choose from and to relations for regular inclusion dependencies
				//int fromRelIndex = _generator.nextInt(rels.length-1);
				//int toRelIndex = -1;
				
				//int max_triesIn = 10;
				//boolean doneIn = false;
				//while(max_triesIn>0 && ! doneIn){
					//toRelIndex = _generator.nextInt(rels.length-1);
					//self-referring inclusion dependencies are not allowed
					//if((toRelIndex != fromRelIndex))
						//doneIn=true;
					//else
						//max_triesIn--;
				//}
		
				//it would be better to print it in log that we could not generate random regular inclusion dependencies
				//if(!doneIn)
					//break;
				
				//we roll dice to choose from rel attr and to rel attr for regular inclusion dependencies
				//int[] fromAttrPos = CollectionUtils.createSequence(0, rels[fromRelIndex].sizeOfAttrArray()); 
				//String[] fromNonKeyAttrs = (rels[fromRelIndex].isSetPrimaryKey()) ? getNonKeyAttributes(rels[fromRelIndex], scenario) : scenario.getDoc().getAttrNames(rels[fromRelIndex].getName(),fromAttrPos, true);
		
				//int[] toAttrPos = CollectionUtils.createSequence(0, rels[toRelIndex].sizeOfAttrArray()); 
				//String[] toNonKeyAttrs = (rels[toRelIndex].isSetPrimaryKey()) ? getNonKeyAttributes(rels[toRelIndex], scenario) : scenario.getDoc().getAttrNames(rels[toRelIndex].getName(),toAttrPos, true);
		
				//int fromRelAttrIndex = 0;
				//if(fromNonKeyAttrs.length>1)
					//fromRelAttrIndex = _generator.nextInt(fromNonKeyAttrs.length-1);
				
				//int toRelAttrIndex = -1;
				//MN checking that both from and to attributes have the same type
				//int max_triesAttr = 10;
				//boolean doneAttr = false;
				
				//MN compute fromRelAttrType - 11 April 2014
				//AttrDefType[] fromAttrs = rels[fromRelIndex].getAttrArray();
				//String fromRelAttrType = null;
				//for(int index=0; index<fromAttrs.length; index++)
					//if(fromAttrs[index].toString().substring(fromAttrs[index].toString().indexOf("<Name>") + 6, fromAttrs[index].toString().indexOf("</Name>")).equals(fromNonKeyAttrs[fromRelAttrIndex])){
						//fromRelAttrType = fromAttrs[index].toString().substring(fromAttrs[index].toString().indexOf("<DataType>") + 10, fromAttrs[index].toString().indexOf("</DataType>"));
						//break;}
				
				//while(max_triesAttr>0 && !doneAttr){
					//if(toNonKeyAttrs.length>1)
						//toRelAttrIndex = _generator.nextInt(toNonKeyAttrs.length-1);
					//else
						//toRelAttrIndex = 0;
					
					//MN the way to compute attr type has been changed - 11 April 2014
					
					//MN get toRelAttrType - 11 April 2014
					//AttrDefType[] toAttrs = rels[toRelIndex].getAttrArray();
					//String toRelAttrType = null;
					//for(int index=0; index<toAttrs.length; index++)
						//if(toAttrs[index].toString().substring(toAttrs[index].toString().indexOf("<Name>") + 6, toAttrs[index].toString().indexOf("</Name>")).equals(toNonKeyAttrs[toRelAttrIndex]))
							//{toRelAttrType = toAttrs[index].toString().substring(toAttrs[index].toString().indexOf("<DataType>") + 10, toAttrs[index].toString().indexOf("</DataType>"));
							 //break;}
					
					//if(fromRelAttrType.equals(toRelAttrType))
						//doneAttr = true;
					//else
						//max_triesAttr --;
					
				//}
				
				//if(!doneAttr)
					//break;
		
				//if(existsID(ids, rels[fromRelIndex], rels[toRelIndex], fromNonKeyAttrs[fromRelAttrIndex], toNonKeyAttrs[toRelAttrIndex])){
					//max_tries--;
				//}
				//else{
					//if(((int)(configuration.getParam(Constants.ParameterName.SourceCircularInclusionDependency)) == 0) &&
							//isCircularInclusionDependency(ids, rels[fromRelIndex].getName(), rels[toRelIndex].getName(), fromNonKeyAttrs[fromRelAttrIndex], toNonKeyAttrs[toRelAttrIndex])){
						//max_tries--;
					//}
					//else{
						//add inclusion dependency
						//if (addInclusionDependency (ids, rels[fromRelIndex].getName(), rels[toRelIndex].getName(), fromNonKeyAttrs[fromRelAttrIndex], toNonKeyAttrs[toRelAttrIndex], false)) {

							//I need to implement something like below
							//scenario.getDocFac().addFD(r.getName(), arrayLHS, new String[] { RHSAtt });
							
						
							//if (log.isDebugEnabled()) {
								//log.debug("--------- GENERATING NEW RANDOM SOURCE REGULAR INCLUSION DEPENDENCY---------");
								//log.debug("fromRelName: " + rels[fromRelIndex].getName());
								//log.debug("toRelName: " + rels[toRelIndex].getName());
								//log.debug("fromRelAttrName: " + fromNonKeyAttrs[fromRelAttrIndex]);
								//log.debug("toRelAttrName: " + toNonKeyAttrs[toRelAttrIndex]);
							//}
							//done = true;
						//}
						//else
							//max_tries --;
					//}
				//}
				
			//}
		//}
	}
	
	////checks if there exists an inclusion dependency with reverse relations in randomly generated fks and fks generated by mapping primitives
	//MN 28 May 2014
	//MN fixed the method to support primaryKeySize>1 - 24 June 2014
	private boolean isCircularInclusionDependencyFK (MappingScenario scenario, Map<String, Map<String, 
			InclusionDependency>> ids, String from, String to, String[] fromAttr, String[] toAttr)
	{
// BG: retarded loop that does the same check in every loop iteration		
//		for(int i=0; i<ids.size(); i++)
//		{
		String temp1 = fromAttr[0];
		for(int indx=1; indx<fromAttr.length; indx++)
			temp1 += fromAttr[indx];
		
		String temp2 = toAttr[0];
		for(int indx=1; indx<toAttr.length; indx++)
			temp2 += toAttr[indx];
		
		if (ids.containsKey(to + temp2 + from + temp1))
			return true;
//		}
		//MN - 28 May 2014 - wrote code to check circularity with fks that have been generated by mapping primitives 
		ArrayList<Rule> fks = scenario.getSource().getForeignKeyConstraints();
		if(fks.size() == 0)
			return false;
		
		//TODO before starting index the FKs created by the primitives in ids
		for(int i=0; i<fks.size(); i++)
			if(fks.get(i).getLeftTerms().toString().contains(to) && fks.get(i).getRightTerms().toString().contains(from)){
				boolean[] circular = new boolean [fromAttr.length];
				for(int f=0; f<fromAttr.length; f++)
					circular[f] = false;
				
				for(int g=0; g<fromAttr.length; g++){
					if(fks.get(i).getRightConditions().toString().contains(fromAttr[g]) &&
						 fks.get(i).getRightConditions().toString().contains(toAttr[g]))
							circular[g] = true;
				}
				
				for(int f=0; f<fromAttr.length; f++)
					if(!circular[f])
						return false;
			}
		return false;
	}
	
//	private boolean isCircularInclusionDependency (Map<String, Map<String, InclusionDependency>> ids, String from, String to, String fromAttr, String toAttr)
//	{
//		for(int i=0; i<ids.size(); i++)
//		{
//			if (ids.containsKey(to + toAttr + from + fromAttr))
//				return true;
//		}
//
//		return false;
//	}
	
	////checks if there exists an inclusion dependency with the same from, from attr, to, to attr
	//MN modified the code to support primaryKeySize>1 
	private boolean existsID (Map<String, Map<String, InclusionDependency>> ids, RelationType from, RelationType to, 
			String[] fromAttr, String[] toAttr)
	{
//		for (int i=0; i< ids.size(); i++)
//		{
			String temp1 = fromAttr[0];
			for(int indx=1; indx<fromAttr.length; indx++)
				temp1 += fromAttr[indx];
			
			String temp2 = toAttr[0];
			for(int indx=1; indx<toAttr.length; indx++)
				temp2 += toAttr[indx];
			
			if (ids.containsKey(from.getName() + temp1 + to.getName() + temp2))
				return true;
//		}
		return false;
	}
	
	//MN fixed the method to support primaryKeySize>1 - 24 June 2014
	private boolean addInclusionDependency (Map<String, Map<String,InclusionDependency>> ids, String fromRelName, String toRelName,
			String[] fromRelAttrName, String[] toRelAttrName, boolean foreignKey){
		InclusionDependency id = new InclusionDependency (fromRelName, fromRelAttrName, 
				toRelName, toRelAttrName);
		
		Map<String, InclusionDependency> relMap;
		
		String temp1 = fromRelAttrName[0];
		for(int indx=1; indx<fromRelAttrName.length; indx++)
			temp1 += fromRelAttrName[indx];
		
		String temp2 = toRelAttrName[0];
		for(int indx=1; indx<toRelAttrName.length; indx++)
			temp2 += toRelAttrName[indx];
		
		if (!ids.containsKey(fromRelName + temp1 + toRelName + temp2)) {
			relMap = new HashMap<String, InclusionDependency> ();
			
			ids.put(fromRelName + temp1 + toRelName + temp2, relMap);
			
			//MN add random source regular inclusion dependency to sids - 14 April 2014
			if(!foreignKey)
				sids.add(id.toString());
		}
		else {
			relMap = ids.get(fromRelName + temp1 + toRelName + temp2);
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
