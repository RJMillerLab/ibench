package org.vagabond.benchmark.explgen;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;
import java.util.Map.Entry;

import org.apache.log4j.Logger;
import org.vagabond.explanation.generation.SuperfluousMappingExplanationGenerator;
import org.vagabond.explanation.generation.prov.ProvenanceGenerator;
import org.vagabond.explanation.marker.IAttributeValueMarker;
import org.vagabond.explanation.marker.ISingleMarker;
import org.vagabond.explanation.marker.MarkerFactory;
import org.vagabond.explanation.marker.MarkerSetFlattenedView;
import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;
import org.vagabond.explanation.model.basic.SuperflousMappingError;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.util.CollectionUtils;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.util.LoggerUtil;
import org.vagabond.util.xmlbeans.ExplanationAndErrorXMLLoader;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.TransformationType;
import org.vagabond.xmlmodel.explanderror.ExplanationAndErrorsDocument;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants.DESErrorType;
import tresc.benchmark.Constants.OutputOption;
import tresc.benchmark.utils.Utils;

public class SuperfluousMappingGen extends AbstractExplanationGenerator {

	static Logger log = LogProviderHolder.getInstance().getLogger(
			MarkerSetFlattenedView.class);

	private SuperfluousMappingExplanationGenerator gen = new SuperfluousMappingExplanationGenerator();

	@Override
	public void generateExpl(MappingScenario scenario, Connection dbCon,
			ExplanationAndErrorsDocument eDoc, Configuration conf)
			throws Exception {
		super.generateExpl(scenario, dbCon, eDoc, conf);

		Vector<String> mappings = scenario.getDoc().getMapIds();
		SuperflousMappingError err;

		Collections.shuffle(mappings);
		mappings.setSize(numExpl);
		
		for (String mapping : mappings) {

			MappingType mapType = scenario.getDoc().getMapping(mapping);
			Set<String> mappingSet = CollectionUtils.makeSet(mapping);
			TransformationType[] transForMap = scenario.getDoc().getTransForMap(
					mapType);
			
//			for (TransformationType t : transForMap) {
//				String targetName = t.getCreates();
//				String cmd = "SELECT tid FROM (SELECT MAPPROV * FROM "
//						+ targetName + ") x " + "WHERE trans_prov LIKE '%"
//						+ mapping + "%' limit 1";
//				try {
//					ResultSet rs = ConnectionManager.getInstance().execQuery(
//							cmd);
//					String tid = null;
//					if (rs.next()) {
//						tid = rs.getString("tid");
//					}
//					if (tid != null) {
//						ISingleMarker errorMarker = MarkerFactory
//								.newAttrMarker(targetName, tid, 0);
//						err = new SuperflousMappingError(
//								(IAttributeValueMarker) errorMarker);
//						gen.setExpl(err);
//						gen.getExpl().addMapSE(mapType);
//						gen.getExpl().setTransSE(
//								CollectionUtils.makeList(transForMap));
//						break;
//					}
//				} catch (Exception e) {
//					LoggerUtil.logException(e, log);
//				}
//			}
//			for (TransformationType t: transForMap)
//				gen.computeSideEffects(t.getCreates(), mappingSet);
//			
//			e.addExplanation(gen.getExpl());
		}
	}

	@Override
	protected DESErrorType getType() {
		return DESErrorType.SuperfluousMapping;
	}

}
