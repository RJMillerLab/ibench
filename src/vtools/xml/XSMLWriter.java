package vtools.xml;

//MN This class prints the output of iBench as XSML file - 3 April 2014


//MN I changed the expressions in "where" clause so that it prints the skolems in left-side of the expression - 11 April 2014
//MN if error in craeting .xsml refer to skolem parts of the mappings - 12 April 2014
//MN modifying the code to support injection of random regular source and target inclusion dependencies into mappings - 14 April 2014
//MN assumption of two "a", only the second a is considered - what about skolem terms? - 16 April 2014

//MN Fixed Foreign keys in print logical mappings - 1 June 2014
//MN Removed naming of correspondences in .xsml file - 6 August 2014
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.apache.xmlbeans.XmlString;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.ForeignKeyType;
import org.vagabond.xmlmodel.FunctionType;
import org.vagabond.xmlmodel.MapExprType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SKFunction;

import smark.support.MappingScenario;
import vtools.dataModel.schema.Element;
import vtools.dataModel.schema.Schema;

public class XSMLWriter {

	private final String _tab = "	";
	
	private static final Comparator<String> ID_COMP = new Comparator<String>() {

		@Override
		public int compare(String l, String r) {
			char[] lChars = l.toCharArray();
			char[] rChars = r.toCharArray();
			int pos = 0;

			if (l.length() != r.length())
				return l.length() < r.length() ? -1 : 1;

			// skip same char
			while (lChars[pos] == rChars[pos++] && pos < l.length())
				;
			pos--;

			// both have number
			if (Character.isDigit(lChars[pos])
					&& Character.isDigit(rChars[pos])) {
				// assume the rest is a number
				int lId = Integer.parseInt(l.substring(pos));
				int rId = Integer.parseInt(r.substring(pos));
				if (lId == rId)
					return 0;
				return lId < rId ? -1 : 1;
			}
			// no order one that is digit first
			else
				return Character.isDigit(lChars[pos]) ? -1 : 1;
		}

	};

	public void printAll (StringBuffer buf, MappingScenario scenario, String name,
			ArrayList<String> randomSourceInclusionDependencies, 
			ArrayList<String> randomTargetInclusionDependencies) throws Exception {
		buf.append("<?xml version=\"1.0\" encoding=\"ASCII\"?>\n");
		buf.append("<xsml:schemaMapping xmlns:xsml=\"http://com.ibm.clio.model/xsml/2.0\">\n");
		
		//////print schemas
		print(buf, name);
		/////print correspondences
		print(buf, name, scenario);
		////print logical mappings (for first experiment)
		print(buf, scenario, name, randomSourceInclusionDependencies, randomTargetInclusionDependencies);

		buf.append("</xsml:schemaMapping>");
	}
	
	
	//MN prints the schemas (source and target)
	public void print (StringBuffer buf, String name){
		buf.append("  <schemas>\n");
		buf.append("    <source name=\"" + name + "_Src0\"" + " rootName=\"" + name + "_Src\"" + " schemaLocation=\"" + name + "_Src.xsd\"" + "/>\n");
		buf.append("    <target name=\"" + name + "_Trg0\"" + " rootName=\"" + name + "_Trg\"" + " schemaLocation=\"" + name + "_Trg.xsd\"" + "/>\n");
		buf.append("  </schemas>\n");
	}
	
	/**
	 * Print correspondences as "componentMappings"
	 * @param buf
	 * @param name
	 * @param scenario
	 * @throws Exception
	 */
	public void print (StringBuffer buf, String name, MappingScenario scenario) throws Exception{
		if (!scenario.getDoc().getDocument().getMappingScenario()
				.isSetCorrespondences())
			return;
		if (scenario.getDoc().getDocument().getMappingScenario()
				.getCorrespondences().sizeOfCorrespondenceArray() == 0)
			return;
		buf.append("  <componentMappings>\n");

		if (scenario.getDoc().getDocument().getMappingScenario().isSetCorrespondences())
		{
			for(CorrespondenceType c: scenario.getDoc().getDocument().
					getMappingScenario().getCorrespondences().
					getCorrespondenceArray())
			{
				buf.append("    <valueMapping>\n");
			
				String fromRel = c.getFrom().getTableref();
				String toRel = c.getTo().getTableref();
				String fromRelAttr = c.getFrom().getAttrArray(0);
				String toRelAttr = c.getTo().getAttrArray(0);
				
				buf.append("      <source value=\"" + "$" + name + "_Src0" + "/" + fromRel + "/" + fromRelAttr + "\"/>\n");
				buf.append("      <target value=\"" + "$" + name + "_Trg0" + "/" + toRel   + "/" + toRelAttr   + "\"/>\n");
							
				buf.append("    </valueMapping>\n");
			}
		}
		buf.append("  </componentMappings>\n");
	}
	
	//MN checks to see whether both from and to relations of the source FK exist in the mapping
	//MN modified - 1 June 2014
	private boolean existsSourceFKFromTo (String mapping, ForeignKeyType sourceFK){
		String fromRelFK = sourceFK.getFrom().getTableref().toString();
		String toRelFK = sourceFK.getTo().getTableref().toString();
		
		boolean e1 = false;
		boolean e2 = false;
		//existence of fromRelFK in mapping
		String sourceE1 = mapping.substring(mapping.indexOf("<Foreach>"), mapping.indexOf("</Foreach>")+10);
		while (true){
			String fromRel = ((sourceE1.split("="))[1].split(">"))[0];
		
			if(fromRelFK.equals(fromRel.substring(1, fromRel.length()-1))){
				e1 = true;
				break;
			}

			if (!(sourceE1.lastIndexOf("</Atom>") == sourceE1.indexOf("</Atom>")))
				sourceE1 = sourceE1.substring(sourceE1.indexOf("</Atom>")+8);
			else
				break;
		}
		//existence of toRelFK in mapping
		String sourceE2 = mapping.substring(mapping.indexOf("<Foreach>"), mapping.indexOf("</Foreach>")+10);
		while (true){
			String toRel = ((sourceE2.split("="))[1].split(">"))[0];
		
			if(toRelFK.equals(toRel.substring(1, toRel.length()-1))){
				e2 = true;
				break;
			}

			if (!(sourceE2.lastIndexOf("</Atom>") == sourceE2.indexOf("</Atom>")))
				sourceE2 = sourceE2.substring(sourceE2.indexOf("</Atom>")+8);
			else
				break;
		}
		
		if(e1 & e2)
			return true;
		return false;
	}
	
	//MN checks to see whether both from and to relations of the target FK are in the mapping
	//MN modified - 1 June 2014
	private boolean existsTargetFKFromTo(String mapping, ForeignKeyType targetFK){
		String fromRelFK = targetFK.getFrom().getTableref().toString();
		String toRelFK = targetFK.getTo().getTableref().toString();
				
		boolean e1 = false;
		boolean e2 = false;
		//existence of fromRelFK in mapping
		String targetE1 = mapping.substring(mapping.indexOf("<Exists>"), mapping.indexOf("</Exists>")+9);
		while (true){
			String fromRel = ((targetE1.split("="))[1].split(">"))[0];
		
			if(fromRelFK.equals(fromRel.substring(1, fromRel.length()-1))){
				e1 = true;
				break;
			}

			if (!(targetE1.lastIndexOf("</Atom>") == targetE1.indexOf("</Atom>")))
				targetE1 = targetE1.substring(targetE1.indexOf("</Atom>")+8);
			else
				break;
		}
	
		//existence of toRelFK in mapping
		String targetE2 = mapping.substring(mapping.indexOf("<Exists>"), mapping.indexOf("</Exists>")+9);
		while (true){
			String toRel = ((targetE2.split("="))[1].split(">"))[0];
		
			if(toRelFK.equals(toRel.substring(1, toRel.length()-1))){
				e2 = true;
				break;
			}

			if (!(targetE2.lastIndexOf("</Atom>") == targetE2.indexOf("</Atom>")))
				targetE2 = targetE2.substring(targetE2.indexOf("</Atom>")+8);
			else
				break;
		}
		
		if(e1 & e2)
			return true;
		return false;
	}
	
	//MN prints the logical mappings (from source to target)
	//MN modifying the method to support injection of random source and target inclusion dependencies into mappings - 14 April 2014
	public void print (StringBuffer buf, MappingScenario scenario, String name,
			ArrayList<String> randomSourceInclusionDependencies, ArrayList<String> randomTargetInclusionDependencies) throws Exception {
		if (scenario.getDoc().getDocument().getMappingScenario().getMappings().sizeOfMappingArray() == 0)
			return;
		
		
		printLogicalMappings(buf, scenario, name);
		if (1 + 1 >= 2)
			return;
		// ********************************************************************************
		// Here be dragons
		//TODO logical mappings section required?
		buf.append("  <logicalMappings>\n");
		
		String sScenario = scenario.getDoc().getDocument().toString();
		int mapsIndexBegin = sScenario.indexOf("<Mappings");
		int mapsIndexEnd = sScenario.indexOf("</Mappings");
		String mappings = sScenario.substring(mapsIndexBegin+20, mapsIndexEnd-3);
		
		while (!mappings.equals("")){
			String nameMap = mappings.split("=")[1].split(">")[0];
			buf.append("    <logicalMapping "+ "name=" + nameMap + ">\n");
			
			int mapIndexEnd = mappings.indexOf("</Mapping>");
			String mapping = mappings.substring(0, mapIndexEnd+10);
			
			//MN we need to consider a boolean data structure to determine which regular inclusion dependencies can be injected into mapping - 14 April 2014
			int sourceIDsSize = randomSourceInclusionDependencies.size();
			boolean[] sourceFromIDs = new boolean[sourceIDsSize];
			boolean[] sourceToIDs = new boolean[sourceIDsSize];
			//initialization
			for(int i=0; i<sourceIDsSize; i++){
				sourceFromIDs[i] = false;
				sourceToIDs[i] = false;
 			}
			
			int targetIDsSize = randomTargetInclusionDependencies.size();
			boolean[] targetFromIDs = new boolean[targetIDsSize];
			boolean[] targetToIDs = new boolean [targetIDsSize];
			//initialization
			for(int i=0; i<targetIDsSize; i++){
				targetFromIDs[i] = false;
				targetToIDs[i] = false;
 			}
			//MN
			
			//source entities
			buf.append("      <source>\n");
			String source = mapping.substring(mapping.indexOf("<Foreach>"), mapping.indexOf("</Foreach>")+10);
			while (true){
				String fromRel = ((source.split("="))[1].split(">"))[0];
				buf.append("        <entity " + "value=\"$" + name + "_Src0/" +  (fromRel.substring(1, fromRel.length()-1)) + "\" " + "name=\"sm" + (fromRel.substring(1, fromRel.length()-1)) + "\"/>\n");
				
				//MN checks for source regular inclusion dependencies - 14 April 2014
				for(int i=0; i<randomSourceInclusionDependencies.size(); i++){
					String id = randomSourceInclusionDependencies.get(i);
					String fromRel1 = id.substring(0, id.indexOf("|"));
					
					id = id.substring(id.indexOf("|"));
					id = id.substring(id.indexOf("|")+1);
					String fromRel2 = id.substring(id.indexOf("|")+1, id.lastIndexOf("|"));
					//from equals to fromRel
					//MN 28 May 2014
					////if(fromRel1.equals(fromRel.substring(1, fromRel.length()-1)))
						////sourceFromIDs[i] = true;
					//to equals to fromRel
					////if(fromRel2.equals(fromRel.substring(1, fromRel.length()-1)))
						////sourceToIDs[i] = true;
				}
				
				if (!(source.lastIndexOf("</Atom>") == source.indexOf("</Atom>")))
				   source = source.substring(source.indexOf("</Atom>")+8);
				else
					break;
			}
			//source FK
			
			boolean andSrcFK = false;
			ForeignKeyType[] srcfkAttrs = scenario.getDoc().getSchema(true).getForeignKeyArray();
			
			//perhaps the source relation does not have any foreign keys
			String sourceFK = null;
			if(srcfkAttrs.length >0){
				//for each source FK
				for (int ii=0; ii<srcfkAttrs.length; ii++){
					String fromRelFK = srcfkAttrs[ii].getFrom().getTableref().toString();
					String toRelFK = srcfkAttrs[ii].getTo().getTableref().toString();
					
					if(existsSourceFKFromTo(mapping, srcfkAttrs[ii])){
						for(int iii=0; iii<srcfkAttrs[ii].getFrom().getAttrArray().length; iii++){
							if(andSrcFK)
								buf.append(" AND ");
							else
								buf.append("      <predicate>");
							
							buf.append("$sm" + fromRelFK + "/" + srcfkAttrs[ii].getFrom().getAttrArray(iii) + 
									" = " + "$sm" + toRelFK + "/" + srcfkAttrs[ii].getTo().getAttrArray(iii));
							
							andSrcFK = true;
						}
					}
			  }
			}
			if(andSrcFK)
				buf.append("</predicate>\n");
			buf.append("      </source>\n");
			//end source entities
			
			//target entities
			buf.append("      <target>\n");
			String target = mapping.substring(mapping.indexOf("<Exists>"), mapping.indexOf("</Exists>")+9);
			while (true){
				String toRel = ((target.split("="))[1].split(">"))[0];
				buf.append("        <entity " + "value=\"$" + name + "_Trg0/" +  (toRel.substring(1, toRel.length()-1)) + "\" " + "name=\"tm" + (toRel.substring(1, toRel.length()-1)) + "\"/>\n");
				
				//MN checks for target regular inclusion dependencies - 14 April 2014
				for(int i=0; i<randomTargetInclusionDependencies.size(); i++){
					String id = randomTargetInclusionDependencies.get(i);
					String toRel1 = id.substring(0, id.indexOf("|"));
					
					id = id.substring(id.indexOf("|"));
					id = id.substring(id.indexOf("|")+1);
					String toRel2 = id.substring(id.indexOf("|")+1, id.lastIndexOf("|"));
					//from equals to toRel
					//MN 28 May 2014
					////if(toRel1.equals(toRel.substring(1, toRel.length()-1)))
						////targetFromIDs[i] = true;
					//to equals to fromRel
					////if(toRel2.equals(toRel.substring(1, toRel.length()-1)))
						////targetToIDs[i] = true;
				}
				
				if (!(target.lastIndexOf("</Atom>") == target.indexOf("</Atom>")))
				   target = target.substring(target.indexOf("</Atom>")+8);
				else
					break;
			}
			//target FK
			
			boolean andTrgFK = false;
			ForeignKeyType[] trgfkAttrs = scenario.getDoc().getSchema(false).getForeignKeyArray();
			String targetFK = null;
			
			if(trgfkAttrs.length >0){
				//for each source FK
				for (int ii=0; ii<trgfkAttrs.length; ii++){
					String fromRelFK = trgfkAttrs[ii].getFrom().getTableref().toString();
					String toRelFK = trgfkAttrs[ii].getTo().getTableref().toString();
					
					if(existsTargetFKFromTo(mapping, trgfkAttrs[ii])){	
						for(int iii=0; iii<trgfkAttrs[ii].getFrom().getAttrArray().length; iii++){
							if(andTrgFK)
								buf.append(" AND ");
							else
								buf.append("      <predicate>");
							
							buf.append("$tm" + fromRelFK + "/" + trgfkAttrs[ii].getFrom().getAttrArray(iii) + 
									" = " + "$tm" + toRelFK + "/" + trgfkAttrs[ii].getTo().getAttrArray(iii));
							
							andTrgFK = true;
						}
					}
			  }
			}
			
			if(andTrgFK)
				buf.append("</predicate>\n");
			//end target FK
			buf.append("      </target>\n");
			//end target entities
			
			buf.append("      <mapping>");
			
			boolean and = false;
			
			///SK and Target Variables
			String targetMapping = mapping.substring(mapping.indexOf("<Exists>"), mapping.indexOf("</Exists>")+9);
			//for each target atom
			while(true){
				String toRel = ((targetMapping.split("="))[1].split(">"))[0];
				int trgVarIndex=-1;
				
				//target vars and skolem terms
				while (true){
					//for each target var
					if((targetMapping.indexOf("<Var>")<targetMapping.indexOf("<SKFunction")) || (targetMapping.indexOf("<SKFunction") == -1))
					{
						while(true){
							String trgVar = targetMapping.split("<Var>")[1].split("</")[0];
							trgVarIndex++;
							boolean found = false;
							String sourceMapping = mapping.substring(mapping.indexOf("<Foreach>"), mapping.indexOf("</Foreach>")+10);
							//for each source atom
							while (true){
								String fromRel = ((sourceMapping.split("="))[1].split(">"))[0];
								int index = -1;
								//for each source variable
								while (true){
									String[] splitSourceVar = sourceMapping.split("<Var>");
									String srcVar = splitSourceVar[1].split("<")[0];
									index++;
									if(trgVar.equals(srcVar)){
										//MN I added this method to modularize the code - 12 April 2014
										boolean[] b = printMapExpr(scenario, fromRel, toRel, buf, index, trgVarIndex, found, and); 
										found = b[0];
										and = b[1];
									}
									String checkSourceMapping = sourceMapping.substring(sourceMapping.indexOf("</Var>")+7);
									if (!(sourceMapping.indexOf("</Var>") == sourceMapping.lastIndexOf("</Var>")) && (checkSourceMapping.indexOf("</Atom>") >= checkSourceMapping.indexOf("<Var>")))
										sourceMapping = sourceMapping.substring(sourceMapping.indexOf("</Var>")+7);
									else
										break;
								}
								//for each source variable	 
								if (!(sourceMapping.indexOf("</Atom>") == sourceMapping.lastIndexOf("</Atom>")))
									sourceMapping = sourceMapping.substring(sourceMapping.indexOf("</Atom>")+8);
								else
									break;
							}
							//for each source atom
							//MN change in if condition to handle OF - 12 April 2014
							String checkTargetMapping = targetMapping.substring(targetMapping.indexOf("</Var>")+7);
							if((targetMapping.indexOf("<Var>") == targetMapping.lastIndexOf("<Var>")) || (checkTargetMapping.indexOf("<Var>") >= checkTargetMapping.indexOf("</Atom>"))
									|| ((checkTargetMapping.indexOf("<SKFunction") != -1) && (checkTargetMapping.indexOf("<Var>")>checkTargetMapping.indexOf("<SKFunction"))))
								{ targetMapping = targetMapping.substring(targetMapping.indexOf("</Var>")+7);	
								  break;}
							else
								targetMapping = targetMapping.substring(targetMapping.indexOf("</Var>")+7);
						}
					}
					//for each target var
					
					
					//for each skolem term
					while(true){
						int skIndex = targetMapping.indexOf("<SKFunction");
						int nextVarIndex = targetMapping.indexOf("<Var>");
				
						if((skIndex != -1) && (nextVarIndex != -1) && (nextVarIndex>skIndex)){
							//MN I added this method to modularize the code - 12 April 2014
							trgVarIndex++;
							targetMapping = dealSK(scenario, buf, mapping, targetMapping, toRel, trgVarIndex, skIndex, and);
							and = true;
						}
						else
							break;
						//MN added to support OF - 16 April 2014
						
						//**MN perhaps we need to add something for other special cases
						int indexNextVar = targetMapping.indexOf("<Var>");
						int indexNextSKFunc = targetMapping.indexOf("<SKFunction");
						int indexNextAtom = targetMapping.indexOf("<Atom>");
						int indexNextEndAtom = targetMapping.indexOf("</Atom>");
						
						
						if((indexNextSKFunc == -1) && (indexNextVar != -1) && (indexNextVar > targetMapping.indexOf("</SKFunction>"))){
							targetMapping = targetMapping.substring(targetMapping.indexOf("<Var>"));
							break;
						}
						//MN next Atom -17 April 2014
						if((indexNextVar != -1) && (indexNextVar>indexNextAtom) && (indexNextVar>indexNextEndAtom)){
							//targetMapping = targetMapping.substring(targetMapping.indexOf("<Var>"));
							break;
						}
						
						if((indexNextSKFunc != -1) && (indexNextVar != -1) && (indexNextVar>targetMapping.indexOf("</SKFunction>"))
								&& (indexNextVar< indexNextSKFunc)){
							targetMapping = targetMapping.substring(targetMapping.indexOf("<Var>"));
							break;
						}
						
						if(!(targetMapping.indexOf("</SKFunction>") == targetMapping.lastIndexOf("</SKFunction>")) && (targetMapping.indexOf("<SKFunction") < targetMapping.indexOf("</Atom>")))
							{targetMapping = targetMapping.substring(targetMapping.indexOf("<SKFunction"));}
						else
							break;
					}
					//for each skolem term
					
					//MN modification to support Object Fusion and Vertical Partitioning ISA - 16 April 2014
					int indexNextAtom = targetMapping.indexOf("<Atom");
					int indexNextVar = targetMapping.indexOf("<Var>");
					
					if((indexNextVar == -1) || ((indexNextAtom != -1) && (indexNextVar != -1) && (indexNextVar>indexNextAtom)))
						break;
				}
				//for each target vars and skolem terms
				
				
				//MN modification to support OF - 12 April 2014
				if (!(targetMapping.lastIndexOf("</Atom>") == targetMapping.indexOf("</Atom>")))
	    			{targetMapping = targetMapping.substring(targetMapping.indexOf("</Atom>")+8);}
				else{
					//MN inject related target regular inclusion dependencies into mappings - 14 April 2014
					//MN if we inject them as predicates into mappings, they will be treated as foreign keys - 14 April 2014
					printTargetRegularInclusionDependencies(buf, targetFromIDs, targetToIDs, randomTargetInclusionDependencies, and);
					
					and = true;
					
					//MN inject related source regular inclusion dependencies into mappings - 14 April 2014
					//MN if we inject them as predicates into mappings, they will be treated as foreign keys - 14 April 2014
					printSourceRegularInclusionDependencies(buf, sourceFromIDs, sourceToIDs, randomSourceInclusionDependencies, and);
					
					buf.append("</mapping>\n");
					buf.append("      </logicalMapping>\n");
					break;
				}
			}
			//for each target atom
			if(mappings.length()>mapIndexEnd+10)
			    mappings = mappings.substring(mapIndexEnd+11);
			else{
			    break;}
		}
		buf.append("  </logicalMappings>\n");
	}


	private void printLogicalMappings(StringBuffer buf,
			MappingScenario scenario, String name) throws Exception {
		buf.append("  <logicalMappings>\n");
		
		for(MappingType m: scenario.getDoc().getDocument().getMappingScenario().getMappings().getMappingArray()) {
			MapExprType source = m.getForeach();
			MapExprType target = m.getExists();
			
			buf.append("    <logicalMapping "+ "name=\"" + m.getId() + "\">\n");
			
			// deal with LHS
			buf.append("      <source>\n");
			for(RelAtomType a: source.getAtomArray())
				printAtom(buf, a, true, name);
			printPredicates(scenario, buf, source, true);
			buf.append("      </source>\n");
			
			// deal wiht RHS
			buf.append("      <target>\n");
			for(RelAtomType a: target.getAtomArray())
				printAtom(buf, a, false, name);
			printPredicates(scenario, buf, target, false);
			buf.append("      </target>\n");

			// deal with exchanged variables
			printExchangedVarsMapping(scenario, buf, source, target);
			buf.append("      </logicalMapping>\n");
		}
		
		buf.append("  </logicalMappings>\n");
	}
	
	/**
	 * 
	 * @param buf
	 * @param source
	 * @param source
	 * @throws Exception 
	 */
	private void printPredicates(MappingScenario s, StringBuffer buf, MapExprType conj, boolean source) throws Exception {
		String relPrefix = source ? "sm" : "tm";
		Map<String,List<String>> argToAttr;
		List<String> comparisons = new LinkedList<String> ();
		
		argToAttr = createVarToRelAttrMap(s,conj, source);
		
		// create comparison expressions
		for(String v: argToAttr.keySet()) {
			List<String> args = argToAttr.get(v);
			
			// add pairwise comparisons 0 = 1, 1 = 2, ...
			for (int i = 0; i < args.size() - 1; i++) {
				String lAttr, rAttr;
				lAttr = "$" + relPrefix + args.get(i);
				rAttr = "$" + relPrefix + args.get(i + 1);
				comparisons.add(lAttr + " = " + rAttr);
			}
		}
		
		if (comparisons.size() == 0)
			return;
		
		// output expressions
		buf.append("      <predicate>");
		
		appendListSepByAnd(buf, comparisons);
		
		buf.append("</predicate>\n");
	}


	private void appendListSepByAnd(StringBuffer buf, List<String> stringList) {
		for(int i = 0; i < stringList.size(); i++) {
			buf.append(stringList.get(i));
			if (i != stringList.size() - 1)
				buf.append(" AND ");
		}
	}
	
	private Map<String,List<String>> createVarToRelAttrMap (MappingScenario s, MapExprType conj, boolean source) throws Exception {
		HashMap<String, List<String>> varToAttr = new HashMap<String, List<String>>();
		
		for(RelAtomType r: conj.getAtomArray()) {
			String relName = r.getTableref();
			RelationType rel = s.getDoc().getRelForName(relName, !source);
			XmlObject[] args = s.getDoc().getAtomArguments(r);
			for(int i = 0; i < args.length; i++) {
				XmlObject x = args[i];
				String attr = relName + "/" + rel.getAttrArray(i).getName();
				String key = null;
				
				if (x instanceof SKFunction) {
					SKFunction f = (SKFunction) x;
					key = skFunctionToString(f);
				}
				else if (x instanceof XmlString) {
					key = ((XmlString) x).getStringValue();
				}
				else
					throw new Exception("only support skolem functions and variables for now\n" + r.toString());
				addOrAppendMap(varToAttr,key,attr);
//				if (x instanceof FunctionType)//TODO not used in iBench right now
//					return functionToString ((FunctionType) arg);
			}
		}
		
		return varToAttr;
	}
	
	private String atomArgToString (XmlObject arg) throws Exception {
		if (arg instanceof SKFunction)
			return skFunctionToString ((SKFunction) arg);
		if (arg instanceof XmlString)
			return ((XmlString) arg).getStringValue(); 
		if (arg instanceof FunctionType)
			return functionToString ((FunctionType) arg);
		
		throw new Exception ("unexpected object type: " + arg.getClass());
	}

	private String functionToString(FunctionType f) throws Exception {
		StringBuilder b = new StringBuilder();
		int numElements = f.sizeOfConstantArray() + f.sizeOfFunctionArray() + 
				f.sizeOfSKFunctionArray() + f.sizeOfVarArray();

		b.append(f.getFname());
		XmlCursor c = f.newCursor();
		argsToString(c, b, numElements);
		
		return b.toString();
	}

	private void argsToString(XmlCursor c, StringBuilder b, int numElements)
			throws Exception {
		b.append("(");

		c.toChild(0);
		for(int i = 0; i < numElements; i++) {
			XmlObject o = (XmlObject) c.getObject();
			b.append(atomArgToString(o) + ", ");
			c.toNextSibling();
		}
		
		b.delete(b.length() - 2, b.length());
		b.append(")");
	}

	private String skFunctionToString (SKFunction f) throws Exception {
		StringBuilder b = new StringBuilder();
		int numElements = f.sizeOfFunctionArray() + 
				f.sizeOfSKFunctionArray() + f.sizeOfVarArray();

		b.append(f.getSkname());
		XmlCursor c = f.newCursor();
		argsToString(c, b, numElements);
		
		return b.toString();
	}

	
	
	/**
	 * Given a Map: String -> List<String> append a new element to the end of the list for parameter key.
	 * 
	 * @param m the map
	 * @param key the key for fetching the list
	 * @param newElem the new element to append to the end
	 */
	private void addOrAppendMap(Map<String,List<String>> m, String key, String newElem) {
		List<String> values;
		if (m.containsKey(key)) {
			values = m.get(key);
			values.add(newElem);
		}
		else {
			values = new ArrayList<String> ();
			values.add(newElem);
			m.put(key, values);
		}
	}


	/**
	 * Print <mapping> element in XSML file that equates source and targets attributes based on exchanged variables.
	 * For instance, if R(A,B), S(C,D), T(E,F) and R(x,y), S(y,z) -> T(x,z) then we would equate R.A = T.E AND S.D = T.F
	 * @param buf
	 * @param source
	 * @param target
	 * @throws Exception 
	 */
	private void printExchangedVarsMapping(MappingScenario s, StringBuffer buf,
			MapExprType source, MapExprType target) throws Exception {
		Map<String,List<String>> sMap;
		List<String> comparisons = new ArrayList<String> ();
		
		sMap = createVarToRelAttrMap(s, source, true);
		
		for(RelAtomType r: target.getAtomArray()) {
			String relName = r.getTableref();
			RelationType rel = s.getDoc().getRelForName(relName, true);
			XmlObject[] args = s.getDoc().getAtomArguments(r);
			for(int i = 0; i < args.length; i++) {
				String targetAttr = relName + "/" + rel.getAttrArray(i).getName();
				XmlObject x = args[i];
				
				// equate target attribute with skolem function, e.g., T(f(a,b), ...)
				if (x instanceof SKFunction) {
					SKFunction f = (SKFunction) x;
					String fName = f.getSkname();
					XmlObject[] skArgs = s.getDoc().getSKArguments(f);
					String fCall;
					StringBuilder skA = new StringBuilder();
					
					for(int j = 0; j < skArgs.length; j++) {
						XmlObject skArg = skArgs[j];
						if (skArg instanceof XmlString) {
							String v = ((XmlString) skArg).getStringValue();
							String sA = sMap.get(v).get(0);//TODO should always possible
							skA.append("$sm" + sA);
							if (j != skArgs.length - 1)
								skA.append(", ");
						}
						else
							throw new Exception("SK arguments should only be vars for now: " + x.toString());
					}
					
					fCall = fName + "(" + skA + ")";
					comparisons.add(fCall + " = " + "$tm" + targetAttr);
				}
				// equate target attribute with source attribute
				if (x instanceof XmlString) {
					String v = ((XmlString) x).getStringValue();
					if (sMap.containsKey(v)) {
						List<String> sourceAttrs = sMap.get(v);
						String firstSourceAttr = sourceAttrs.get(0);
						comparisons.add("$sm" + firstSourceAttr + " = " + "$tm" + targetAttr);
					}
				}
			}
		}
		if (comparisons.size() == 0)
			return;
		
		// output expressions
		buf.append("      <mapping>");
		appendListSepByAnd(buf, comparisons);		
		buf.append("</mapping>\n");
	}


	/**
	 * 
	 * @param buf
	 * @param a
	 * @param source
	 * @param schemaName
	 */
	private void printAtom(StringBuffer buf, RelAtomType a, boolean source, String schemaName) {
		String relName = a.getTableref();
		String schemaPostfix = source ? "_Src0" : "_Trg0";
		String relPrefix = source ? "sm" : "tm";
		buf.append("        <entity " 
				+ "value=\"$" + schemaName + schemaPostfix + "/" +  relName + "\" " 
				+ "name=\"" + relPrefix + relName + "\"/>\n");
	}


	//MN prints random source regular inclusion dependencies - 14 April 2014
	private void printSourceRegularInclusionDependencies(StringBuffer buf, boolean[] sourceFromIDs, boolean[] sourceToIDs, ArrayList<String> sourceIDs,
			boolean and){
		for(int i=0; i<sourceIDs.size(); i++)
			if((sourceFromIDs[i]== true) && (sourceToIDs[i]==true)){
				String id = sourceIDs.get(i);
				String fromRel = id.substring(0, id.indexOf("|"));
				
				id = id.substring(id.indexOf("|")+1);
				String fromRelAttr = id.substring(0, id.indexOf("|"));
				
				id = id.substring(id.indexOf("|")+1);
				String toRel = id.substring(0, id.indexOf("|"));
				
				id = id.substring(id.indexOf("|")+1);
				String toRelAttr = id.substring(0, id.length());
				
				if(and)
					buf.append(" AND ");
				
				buf.append("$sm" + fromRel + "/" + fromRelAttr + " = " + "$sm" + toRel + "/" + toRelAttr);
			}
	}
	
	//MN prints random target regular inclusion dependencies - 14 April 2014
	private void printTargetRegularInclusionDependencies(StringBuffer buf, boolean[] targetFromIDs, boolean[] targetToIDs, ArrayList<String> targetIDs,
			boolean and){
		for(int i=0; i<targetIDs.size(); i++)
			if((targetFromIDs[i]== true) && (targetToIDs[i]==true)){
				String id = targetIDs.get(i);
				
				String fromRel = id.substring(0, id.indexOf("|"));
				
				id = id.substring(id.indexOf("|")+1);
				String fromRelAttr = id.substring(0, id.indexOf("|"));
				
				id = id.substring(id.indexOf("|")+1);
				String toRel = id.substring(0, id.indexOf("|"));
				
				id = id.substring(id.indexOf("|")+1);
				String toRelAttr = id.substring(0, id.length());
				
				if(and)
					buf.append(" AND ");
				
				buf.append("$tm" + fromRel + "/" + fromRelAttr + " = " + "$tm" + toRel + "/" + toRelAttr);
			}
	}
	
	//MN this method deals with skolem terms - 12 April 2014
	private String dealSK(MappingScenario scenario, StringBuffer buf, String mapping, String targetMapping, 
			String toRel, int trgVarIndex, int skIndex, boolean and){
		targetMapping = targetMapping.substring(skIndex);
		//trgVarIndex++;
		
		String targetVar = (scenario.getTarget().getSubElement(toRel.substring(1, toRel.length()-1))).getSubElement(trgVarIndex).toString();
		String targetVarName = targetVar.split(":")[0];
		
		String srcSkolem = targetMapping.split("=")[1].split(">")[0].substring(1 , targetMapping.split("=")[1].split(">")[0].length()-1);
		
		if(and)
			buf.append(" AND ");
		//MN skolem function should be printed in the left-side -11 April 2014
		buf.append(srcSkolem + "(");
		
		boolean moreThanOneSkVar = false;
		//for each skolem term var
		while(true){
			targetMapping = targetMapping.substring(targetMapping.indexOf("<Var>"));
			String skVar = targetMapping.split("<Var>")[1].split("</")[0];
			//MN added one variable to support not to print more than one a at the same time - 16 April 2014
			boolean foundSkVar = false;
			
			String sourceMapping = mapping.substring(mapping.indexOf("<Foreach>"), mapping.indexOf("</Foreach>")+10);
		    //for each source atom
			while (true){
				String fromRel = ((sourceMapping.split("="))[1].split(">"))[0];
				int index = -1;
				
				//for each source variable
				while (true){
					String[] splitSourceVar = sourceMapping.split("<Var>");
					String srcVar = splitSourceVar[1].split("<")[0];
					
					index++;
					
					if((!foundSkVar) && skVar.equals(srcVar)){
						//MN I added this method to modularize the code - 12 April 2014
			    		printMapExprSK(scenario, fromRel, buf, index, moreThanOneSkVar);
			    		moreThanOneSkVar = true;
			    		and = true;
			    		//MN added equality for foundSkVar - 16 April 2014
			    		foundSkVar = true;
					}
					
					String checkSourceMapping = sourceMapping.substring(sourceMapping.indexOf("</Var>")+7);
					if (!(sourceMapping.indexOf("</Var>") == sourceMapping.lastIndexOf("</Var>")) && (checkSourceMapping.indexOf("</Atom>") >= checkSourceMapping.indexOf("<Var>")))
						sourceMapping = sourceMapping.substring(sourceMapping.indexOf("</Var>")+7);
					else
						break;
				}
				//for each source variable	 
				if (!(sourceMapping.indexOf("</Atom>") == sourceMapping.lastIndexOf("</Atom>")))
					sourceMapping = sourceMapping.substring(sourceMapping.indexOf("</Atom>")+8);
				else
					break;	
				}
			//for each source atom
			
			targetMapping = targetMapping.substring(targetMapping.indexOf("<Var>") +2);
			
			//**MN perhaps we need to add something for other special cases
			int indexNextVar = targetMapping.indexOf("<Var>");
			int indexNextSKFunc = targetMapping.indexOf("<SKFunction");
			int indexNextAtom = targetMapping.indexOf("<Atom>");
			int indexNextEndAtom = targetMapping.indexOf("</Atom>");
			
			if(indexNextVar == -1)
				break;
			
			if((indexNextSKFunc == -1) && (indexNextVar != -1) && (indexNextVar > targetMapping.indexOf("</SKFunction>")))
				break;
			
			if((indexNextSKFunc != -1) && (indexNextVar != -1) && (indexNextVar > targetMapping.indexOf("<SKFunction") ))
				break;
			
			if((indexNextVar != -1) && (indexNextVar>indexNextAtom) && (indexNextVar>indexNextEndAtom))
				break;
			
			//MN added to support OF - 16 April 2014
			if((indexNextSKFunc != -1) && (indexNextVar != -1) && (indexNextVar>targetMapping.indexOf("</SKFunction>"))
					&& (indexNextVar< indexNextSKFunc))
				break;
			
			targetMapping = targetMapping.substring(targetMapping.indexOf("</Var>") +2);
			targetMapping = targetMapping.substring(targetMapping.indexOf("<Var>"));
			
		}
		//for each skolem term var
		//MN skolem should be printed left-side of the expression - 11 April 2014
		buf.append(")" + " = " + "$tm" + toRel.substring(1, toRel.length()-1) + "/" + targetVarName);	
		return targetMapping;
	}
	
	//MN prints the mapping expression in the form of rel.attr = rel.attr - 12 April 2014
	private boolean[] printMapExpr (MappingScenario scenario, String fromRel, String toRel, StringBuffer buf, int index, int trgVarIndex, boolean found, boolean and){
		///String sourceVar = (scenario.getSource().getSubElement(fromRel.substring(1, fromRel.length()-1))).getSubElement(srcVarIndex).toString();
		String sourceVar = (scenario.getSource().getSubElement(fromRel.substring(1, fromRel.length()-1))).getSubElement(index).toString();
		String sourceVarName = sourceVar.split(":")[0];
		boolean[] b = new boolean [2];
		
		String targetVar = (scenario.getTarget().getSubElement(toRel.substring(1, toRel.length()-1))).getSubElement(trgVarIndex).toString();
		String targetVarName = targetVar.split(":")[0];
		if (!found){
			if(and)
				buf.append(" AND ");
			buf.append("$tm" + toRel.substring(1, toRel.length()-1) + "/" + targetVarName + " = " + "$sm" + fromRel.substring(1, fromRel.length()-1) + "/" + sourceVarName);
			found = true;
		}
		else{
			//MN should be modified - 12 April 2014
			int deleteIndex = buf.toString().lastIndexOf("$tm" + toRel.substring(1, toRel.length()-1) + "/" + targetVarName + " = ");
			buf.delete(deleteIndex, buf.length());
			//if(and)
				//buf.append(" AND ");
			buf.append("$tm" + toRel.substring(1, toRel.length()-1) + "/" + targetVarName + " = " + "$sm" + fromRel.substring(1, fromRel.length()-1) + "/" + sourceVarName);
			found = true;
		}
		
		and=true;
		b[0] = found;
		b[1] = and;
		return b;
	}
	
	//MN prints the mapping expression in the form of sk = rel.attr - 12 April 2014
	private void printMapExprSK (MappingScenario scenario, String fromRel, StringBuffer buf, int index, boolean moreThanOneSkVar){
		///String sourceVar = (scenario.getSource().getSubElement(fromRel.substring(1, fromRel.length()-1))).getSubElement(srcVarIndex).toString();
		String sourceVar = (scenario.getSource().getSubElement(fromRel.substring(1, fromRel.length()-1))).getSubElement(index).toString();
		String sourceVarName = sourceVar.split(":")[0];
		
		if(moreThanOneSkVar)
			buf.append(", ");
		buf.append("$sm" + fromRel.substring(1, fromRel.length()-1) + "/" + sourceVarName);
		
		//and=true;
		moreThanOneSkVar = true;
	}
	
}
