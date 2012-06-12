package org.vagabond.benchmark.model;

import org.apache.log4j.Logger;
import org.vagabond.mapping.model.MapScenarioHolder;
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
		SchemaType s = source ? doc.getMappingScenario().getSchemas().getSourceSchema() :
			doc.getMappingScenario().getSchemas().getTargetSchema();
		RelationType relation = s.getRelationArray()[rel];
		return relation.getAttrArray()[attr].getName();
	}
	
	public String getRelName (int rel, boolean source) {
		SchemaType s = source ? doc.getMappingScenario().getSchemas().getSourceSchema() :
			doc.getMappingScenario().getSchemas().getTargetSchema();
		RelationType relation = s.getRelationArray()[rel];
		return relation.getName();
	}
	
}
