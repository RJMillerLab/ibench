package org.vagabond.benchmark.model;

import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.AttrListType;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.CorrespondencesType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.MappingType.Uses;
import org.vagabond.xmlmodel.RelAtomType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SchemaType;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants.TrampXMLOutputSwitch;

/**
 * Convenience methods to create new Java model elements for an Tramp mapping
 * scenario.
 * 
 * @author lord_pretzel
 */
public class TrampModelFactory {

	private TrampXMLModel doc;

	private UniqueIdGen idGen;

	public TrampModelFactory(TrampXMLModel doc) {
		this.doc = doc;
		initIdGen();
	}

	public void initAllElem (Configuration conf) {
		if (conf.getTrampXMLOutputOption(TrampXMLOutputSwitch.Correspondences))
			doc.getScenario().addNewCorrespondences();
		if (conf.getTrampXMLOutputOption(TrampXMLOutputSwitch.ConnectionInfo))
			doc.getScenario().addNewConnectionInfo();
		if (conf.getTrampXMLOutputOption(TrampXMLOutputSwitch.Data))
			doc.getScenario().addNewData();
		if (conf.getTrampXMLOutputOption(TrampXMLOutputSwitch.Transformations))
			doc.getScenario().addNewTransformations();
	}
	
	private void initIdGen() {
		idGen = new UniqueIdGen();
		idGen.createIdType("Corr", "C");
		idGen.createIdType("Map", "M");
		idGen.createIdType("Trans", "T");
		idGen.createIdType("FK", "FK");
		idGen.createIdType("SK", "SK");
		idGen.createIdType("FD", "FD");
	}

	public CorrespondenceType addCorrespondence(String fromRel, String fromAttr,
			String toRel, String toAttr) {
		return addCorrespondence(fromRel, new String[] { fromAttr }, toRel,
				new String[] { toAttr });
	}

	public CorrespondenceType addCorrespondence(String fromRel, String[] fromAttrs,
			String toRel, String[] toAttrs) {
		CorrespondenceType c =
				doc.getDocument().getMappingScenario().getCorrespondences()
						.addNewCorrespondence();
		c.setId(idGen.createId("Corr"));
		c.addNewFrom();
		c.getFrom().setTableref(fromRel);
		for (String a : fromAttrs)
			c.getFrom().addAttr(a);

		c.addNewTo();
		c.getTo().setTableref(toRel);
		for (String a : toAttrs)
			c.getTo().addAttr(a);
		
		return c;
	}

	public RelationType addRelation(String name, String[] attrs, boolean source) {
		SchemaType schema = source ? 
				doc.getScenario().getSchemas().getSourceSchema() : 
				doc.getScenario().getSchemas().getTargetSchema();
		RelationType rel = schema.addNewRelation();
		rel.setName(name);
		for(String attr: attrs) {
			AttrDefType a = rel.addNewAttr();
			a.setName(attr);
			a.setDataType("TEXT");
		}
		
		return rel;
	}
	
	public void addPrimaryKey (String relName, String[] attrs, boolean source) throws Exception {
		RelationType rel = doc.getRelForName(relName, !source);
		AttrListType k = rel.addNewPrimaryKey();
		for(String a: attrs)
			k.addAttr(a);
	}
	
	public MappingType addMapping (CorrespondenceType[] cs) {
		MappingType map;
		
		map = doc.getScenario().getMappings().addNewMapping();
		
		map.setId(idGen.createId("Map"));
		
		map.addNewForeach();
		map.addNewExists();
		
		Uses u = map.addNewUses();
		for(CorrespondenceType c: cs)
			u.addNewCorrespondence().setRef(c.getId());
		
		return map;
	}
	
	public void addForeachAtom (String mKey, String relName, String[] vars) throws Exception {
		MappingType m = doc.getMapping(mKey);
		
		RelAtomType a = m.getForeach().addNewAtom();
		a.setTableref(relName);
		for(String var: vars)
			a.addNewVar().setStringValue(var);
	}
	
	public void addExistsAtom (String mKey, String relName, String[] vars) throws Exception {
		MappingType m = doc.getMapping(mKey);
		
		RelAtomType a = m.getExists().addNewAtom();
		a.setTableref(relName);
		for(String var: vars)
			a.addNewVar().setStringValue(var);
	}
	
	public String[] getFreshVars (int startVar, int num) {
		String[] result = new String[num];
		for(int i = 0; i < num; i++)
			result[i] = getVarString(startVar + i);
		
		return result;
	}

	private String getVarString(int i) {
		if (i < 26)
			return ('a' + i) + "";
		
		String result = "";
		while (i > 0) {
			result += ('a' + (i % 26));
			i /= 26;
		}
		return result;
	}
}
