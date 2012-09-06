package org.vagabond.benchmark.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.CollectionUtils;
import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.ForeignKeyType;
import org.vagabond.xmlmodel.MapExprType;
import org.vagabond.xmlmodel.MappingScenarioDocument;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SKFunction;
import org.vagabond.xmlmodel.SchemaType;

public class TrampXMLModel extends MapScenarioHolder {

	static Logger log = Logger.getLogger(TrampXMLModel.class);
	
	public TrampXMLModel () {
		initDoc();
	}

	private void initDoc() {
		init();
		doc = MappingScenarioDocument.Factory.newInstance();
    	doc.addNewMappingScenario();
    	MappingScenarioDocument.MappingScenario map = doc.getMappingScenario();
    	map.addNewSchemas().addNewSourceSchema();
    	map.getSchemas().addNewTargetSchema();
    	map.addNewMappings();
	}
	
	@Override
	public String toString() {
		return doc.toString(); 
	}
	
	public Vector<String> getMapIds () {
		Vector<String> result = new Vector<String> ();
		for(MappingType m: doc.getMappingScenario().getMappings().getMappingArray()) {
			result.add(m.getId());
		}
		return result;
	}
	
	/** 
	 * Retrieves all mappings generated as part of STBenchmark's current mapping scenario (a.k.a. schema mapping). 
	 * Two important notes. (1) When running STBenchmark with "MappingLanguage = FOtgds", the returned array of mappings
	 * (MappingType[]) represents a set of s-t tgds. (2) On the contrary, when running STBenchmark with 
	 * "MappingLanguage = SOtgds", the returned array of mappings models a single SO tgd. Thus, in this case, 
	 * each element in the array can be treated as a SO Clause.
	 *  
	 * @author prg
	 * 
	 * @return An array of mappings 
	 */
	
	// PRG ADD July 10, 2012
	public MappingType[] getMappings()
	{		
		return doc.getMappingScenario().getMappings().getMappingArray();

	}
	
	/** 
	 * Retrieves all the mappings associated with a specified relation
	 * 
	 * @author mdangelo
	 * 
	 * @param rel	The name of the relation
	 * @return		An array of mappings
	 */
	public MappingType[] getMappings(String rel)
	{
		// use a vector for the convenience of not having to determine the size
		Vector<MappingType> result = new Vector<MappingType> ();
		
		// loop through the mappings and check if any of the for each refer to the relation in question
		// if they do, add them to our result
		for(MappingType m: doc.getMappingScenario().getMappings().getMappingArray())
		{
			if(m.getForeach() == null)
				log.debug("ERROR: Scenario has no Foreach clause!");
			
			for (RelAtomType a: m.getForeach().getAtomArray())
				if(a.getTableref().equals(rel))
					result.add(m);
		}
		
		// convert the vector into an array
		MappingType[] ret = new MappingType[result.size()];
		int i = 0;
		for (MappingType m : result)
			ret[i++] = m;
			
		return ret;
	}
	
	/** 
	 * Returns the position of an atom in a given mapping
	 * 
	 * @author mdangelo
	 * 
	 * @param rel	The name of the relation (the atoms tableref should be passed in)
	 * @param m		The mapping for which we want to find the position
	 * 
	 * @return		The position of the atom if it exists, -1 otherwise
	 */
	public int getAtomPos(MappingType m, String rel) 
	{
		// go through each atom, increment i every time we don't find it
		int i = 0;
		for (RelAtomType a: m.getExists().getAtomArray())
		{
			// return the index if found
			if(a.getTableref().equals(rel))
				return i;
							
			i++;
		}
		
		// not found
		return -1;
	}
	
	public String getRelAttr (int rel, int attr, boolean source) {
		SchemaType s = getSchema(source);
		RelationType relation = s.getRelationArray()[rel];
		return relation.getAttrArray()[attr].getName();
	}
	
	public int getRelPos(String rel, boolean source) {
		SchemaType s = getSchema(source);
		for(int i = 0 ; i < s.sizeOfRelationArray(); i++) {
			RelationType r = s.getRelationArray(i);
			if (r.getName().equals(rel))
				return i;
		}
		return -1;
	}
	
	public int getRelAttrPos (String rel, String attr, boolean source) throws Exception {
		RelationType r = getRelForName(rel, !source);
		for(int i = 0; i < r.getAttrArray().length; i++) {
			AttrDefType a = r.getAttrArray(i);
			if (a.getName().equals(attr))
				return i;
		}
		return -1;
	}
	
	public String getRelName (int rel, boolean source) {
		SchemaType s = getSchema(source);
		RelationType relation = s.getRelationArray()[rel];
		return relation.getName();
	}

	public int getNumRels (boolean source) {
		SchemaType s = getSchema(source);
		return s.getRelationArray().length;
	}
	
	public RelationType getRel (int pos, boolean source) {
		SchemaType s = getSchema(source);
		return s.getRelationArray()[pos];
	}
	
	
	public AttrDefType[] getKeyAttrs (String rel, boolean source) throws Exception {
		RelationType rela = getRelForName(rel, !source);
		if (!rela.isSetPrimaryKey())
			return new AttrDefType[] {};
		
		return getAttrs(rel, rela.getPrimaryKey().getAttrArray(), source); 
	}
	
	public AttrDefType[] getAttrs (String rel, String[] ids, boolean source) throws Exception {
		List<AttrDefType> atts = new ArrayList<AttrDefType>();
		RelationType relation = getRelForName(rel, !source);
		for(AttrDefType a: relation.getAttrArray()) {
			if (CollectionUtils.search(ids, a.getName()))
				atts.add(a);
		}
		
		return atts.toArray(new AttrDefType[atts.size()]);
	}
	
	/** 
	 * Retreives all of the variables associated with a source relation.
	 * 
	 * @author mdangelo
	 * 
	 * @param rel	The name of the relation
	 * 
	 * @return		An array of strings containing all the variables in a foreach clause associated with the relation
	 */
	public String[] getAttrVars(String rel) throws Exception 
	{
		MappingType[] maps = getMappings(rel);
		String[] result = null;
		
		for (MappingType m : maps)
			for (RelAtomType a : m.getForeach().getAtomArray())
				if(a.getTableref().equals(rel))
					result = a.getVarArray();
		
		return result;
	}
	
	public String[] getAttrNames (String rel, int[] attrPos, boolean source) throws Exception {
		RelationType r = getRelForName(rel, !source);
		String[] result = new String[attrPos.length];
		
		for(int i = 0; i < result.length; i++)
			result[i] = r.getAttrArray(attrPos[i]).getName();
		
		return result;
	}
	
	public ForeignKeyType[] getFKs (String fromRel, String toRel, boolean source) {
		List<ForeignKeyType> result = new ArrayList<ForeignKeyType> ();
		SchemaType s = getSchema(source);
		for(ForeignKeyType fk: s.getForeignKeyArray()) {
			if (fk.getFrom().getTableref().equals(fromRel) 
					&& fk.getTo().getTableref().equals(toRel))
				result.add(fk);
		}
		
		return result.toArray(new ForeignKeyType[result.size()]);
	}

	public SchemaType getSchema(boolean source) {
		SchemaType s = source ? getScenario().getSchemas().getSourceSchema() :
			getScenario().getSchemas().getTargetSchema();
		return s;
	}
	
	public boolean hasPK (String rel, boolean source) throws Exception {
		return getRelForName(rel, !source).isSetPrimaryKey();
	}
	
	public String[] getPK (String rel, boolean source) throws Exception {
		RelationType r = getRelForName(rel, !source);
		if (r.isSetPrimaryKey())
			return r.getPrimaryKey().getAttrArray();
		return null;
	}
	
	public int[] getPKPos (String rel, boolean source) throws Exception {
		RelationType r = getRelForName(rel, !source);
		int[] result;
		
		String[] attNames = r.getPrimaryKey().getAttrArray();
		 
		result = new int[attNames.length];
		for(int i = 0; i < attNames.length; i++) {
			String a = attNames[i];
			result[i] = getRelAttrPos(rel, a, source);
		}
		
		return result;
	}
	
	/** 
	 * Retrieves all of the parameters of an atom in order (including skolems and vars)
	 * 
	 * @author mdangelo
	 * 
	 * @param m			The mapping to get the atomParameters of
	 * @param foreach	A boolean to determine whether to check the for each or exist clauses for the atom
	 * @param atomPos	The position of the atom within the mapping
	 * 
	 * @return			An array of objects
	 */
	public Object[] getAtomParameters (MappingType m, boolean foreach, int atomPos) 
	{
		MapExprType clause = foreach ? m.getForeach() : m.getExists();
		RelAtomType atom = clause.getAtomArray(atomPos);
		XmlCursor c = atom.newCursor();
		
		int size = atom.sizeOfSKFunctionArray() + atom.sizeOfVarArray();
		Object[] result = new Object[size];
		int varPos = 0;
		
		for(int i = 0; i < size; i++) 
		{
			if(i == 0)
				c.toChild(i);
			else
				c.toNextSibling();
			
			XmlObject x = c.getObject();
			
			if (x instanceof SKFunction)
				result[i] = x;
			else
				result[i] = atom.getVarArray(varPos++);
		}

		return result;
	}
	
	/** 
	 * Given an array of objects, sets them in the order to received as the parameters of an atom.
	 * 
	 * @author mdangelo
	 * 
	 * @param params	An array of objects (in order) which includes SKFunctions and vars
	 * @param a			The atom for which to set the parameters
	 */
	public void setAtomParameters (Object[] params, RelAtomType a) 
	{
		// remove elements
		a.setSKFunctionArray(new SKFunction[] {});
		a.setVarArray(new String[] {});
		
		for(Object x: params) 
		{
			if (x instanceof SKFunction) 
			{
				SKFunction in = (SKFunction) x;
				SKFunction f = a.addNewSKFunction();
				f.setSkname(in.getSkname());
				f.setVarArray(in.getVarArray());
			}
			else 
				a.addVar((String) x);
		}
	}
}
