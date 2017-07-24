/**
 * 
 */
package tresc.benchmark.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import org.apache.log4j.Logger;
import org.vagabond.benchmark.model.TrampModelFactory;
import org.vagabond.benchmark.model.TrampXMLModel;
import org.vagabond.xmlmodel.AttrRefType;
import org.vagabond.xmlmodel.CorrespondenceType;

import smark.support.MappingScenario;
import toxgene.util.cdata.xmark.CSVDataType;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants.ParameterName;
import tresc.benchmark.Constants.SkolemKind;
import tresc.benchmark.data.ToXgeneTypes.DeviatedCSVDataType;
import vtools.dataModel.types.CustomCSVDataType;
import vtools.dataModel.types.DataType;
import vtools.dataModel.types.DataTypeHandler;
import vtools.dataModel.types.DeviatedDataType;
import vtools.dataModel.types.UnionDataType;

/**
 * @author lord_pretzel
 *
 */
public class SrcToTargetDTMapper {

	static Logger log = Logger.getLogger(SrcToTargetDTMapper.class);

	protected TrampModelFactory fac;
	protected TrampXMLModel model;
	private Random _generator;
	
	public static SrcToTargetDTMapper inst = new SrcToTargetDTMapper(); 
	
	public SrcToTargetDTMapper () {
		
	}
	
	public static SrcToTargetDTMapper getInst () {
		return inst;
	}
	
	private String attrRefToString (AttrRefType a) {
		return a.getTableref() + "." + a.getAttrArray(0);
	}
	
	public void propagateTypesFromSrcToTarget (MappingScenario scenario, Configuration configuration) throws Exception {
		HashMap<String,Set<String>> propagatedDTs = new HashMap<String,Set<String>> ();
		
		_generator = configuration.getRandomGenerator();
		fac = scenario.getDocFac();
		model = scenario.getDoc();
		fac.indexMappings();
		int devChance = configuration.getParam(ParameterName.PropagateDTsChanceOfDeviation);
		int devDegree = configuration.getParam(ParameterName.PropagateDTsDegreeOfDeviation);
		boolean doDev = devChance != 0;
		
		// collect all DTs from source
		for(CorrespondenceType c: scenario.getDoc().getScenario().getCorrespondences().getCorrespondenceArray()) {
			AttrRefType src = c.getFrom();
			AttrRefType trg = c.getTo();
			String dt;
			
			log.debug("process correspondence " + c.toString());
			
			dt = DataTypeHandler.getInst().getTypeNameForRel(model, true, src.getTableref(), src.getAttrArray(0)); //TODO do we ever use more than one attr?
			
			addDT(propagatedDTs, attrRefToString(trg), dt);
		}
		
		// modify DTs if this is asked for
		for(String aStr: propagatedDTs.keySet()) {
			AttrRefType a = AttrRefType.Factory.newInstance();
			a.setTableref(aStr.split("[.]")[0]);
			a.addAttr(aStr.split("[.]")[1]);
			Set<String> allDts = propagatedDTs.get(aStr);			
			boolean applyDev = false;
			
			log.debug("Attribute " + aStr + " with assigned DTs " +  allDts.toString());
			
			if (doDev) {
				if (_generator.nextInt(100) <= devChance) {
					applyDev = true;
				}
			}
			
			// if there if only DT then use this one
			if (allDts.size() == 1) {
				String dtName = allDts.iterator().next();
				DataType dt = DataTypeHandler.getInst().getDataType(dtName);
				if (applyDev) {
					dt = registerDevType(dt, devDegree);
					dtName = dt.getName();
				}
				DataTypeHandler.getInst().setTypeNameForAttr(model, false, a.getTableref(), a.getAttrArray(0), dtName);	
			}
			// otherwise register a new DT that is union of all DTs
			else {
				DataType udt;
				udt = registerUnionType(allDts);
				if (applyDev) {
					udt = registerDevType(udt, devDegree);
				}
				DataTypeHandler.getInst().setTypeNameForAttr(model, false, a.getTableref(), a.getAttrArray(0), udt.getName());
			}
		}
	}
	
	private DataType registerDevType (DataType dt, int dev) {
		DataType orig = dt;
		DeviatedDataType newType;
		
		newType = new DeviatedDataType(orig, dev);
		
		DataTypeHandler.getInst().addDT(newType.getName(), newType);
		return newType;
	}
	
	private DataType registerUnionType (Set<String> dts) {
		UnionDataType newType;
		Set<DataType> children = new HashSet<DataType> ();
		
		for(String dtName: dts) {
			children.add(DataTypeHandler.getInst().getDataType(dtName));
		}
		
		newType = new UnionDataType(children);
		
		DataTypeHandler.getInst().addDT(newType.getName(), newType);
		
		return newType;
	}
	
	public void addDT (HashMap<String, Set<String>> m, String a, String val) {
		Set<String> attrs = m.get(a);
		
		if (attrs == null) {
			attrs = new HashSet<String> ();
			m.put(a, attrs);
		}
		
		attrs.add(val);
	}
	
}
