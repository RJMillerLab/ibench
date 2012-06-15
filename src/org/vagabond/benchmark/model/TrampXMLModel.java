package org.vagabond.benchmark.model;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.CollectionUtils;
import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.AttrListType;
import org.vagabond.xmlmodel.ForeignKeyType;
import org.vagabond.xmlmodel.MappingScenarioDocument;
import org.vagabond.xmlmodel.RelationType;
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
	
	public String getRelAttr (int rel, int attr, boolean source) {
		SchemaType s = getSchema(source);
		RelationType relation = s.getRelationArray()[rel];
		return relation.getAttrArray()[attr].getName();
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

	private SchemaType getSchema(boolean source) {
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
	
}
