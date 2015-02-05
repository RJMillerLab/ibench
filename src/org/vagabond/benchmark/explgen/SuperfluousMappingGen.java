package org.vagabond.benchmark.explgen;

import java.sql.Connection;
import java.util.Collections;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.explanation.generation.SuperfluousMappingExplanationGenerator;
import org.vagabond.explanation.marker.MarkerSetFlattenedView;
import org.vagabond.util.LogProviderHolder;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.explanderror.ExplanationAndErrorsDocument;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants.DESErrorType;

public class SuperfluousMappingGen extends AbstractExplanationGenerator {

	static Logger log = LogProviderHolder.getInstance().getLogger(
			SuperfluousMappingExplanationGenerator.class);

	private SuperfluousMappingExplanationGenerator gen = new SuperfluousMappingExplanationGenerator();

	@Override
	public void generateExpl(MappingScenario scenario, Connection dbCon,
			ExplanationAndErrorsDocument eDoc, Configuration conf)
			throws Exception {
		super.generateExpl(scenario, dbCon, eDoc, conf);

		Vector<String> mappings = scenario.getDoc().getMapIds();
//		SuperflousMappingError err;

		Collections.shuffle(mappings);
		mappings.setSize(numExpl);
		
		for (String mapping : mappings) {

			MappingType mapType = scenario.getDoc().getMapping(mapping);
//			Set<String> mappingSet = CollectionUtils.makeSet(mapping);
//			TransformationType[] transForMap = scenario.getDoc().getTransForMap(
//					mapType);
			
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
