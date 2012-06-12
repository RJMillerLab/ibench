package org.vagabond.benchmark.model;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.AttrListType;
import org.vagabond.xmlmodel.ConnectionInfoType;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.CorrespondencesType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.MappingType.Uses;
import org.vagabond.xmlmodel.RelAtomType;
import org.vagabond.xmlmodel.RelInstanceFileType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SchemaType;
import org.vagabond.xmlmodel.TransformationType;
import org.vagabond.xmlmodel.TransformationType.Implements;

import smark.support.MappingScenario;
import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants.DBOption;
import tresc.benchmark.Constants.DataGenType;
import tresc.benchmark.Constants.OutputOption;
import tresc.benchmark.Constants.TrampXMLOutputSwitch;
import vtools.dataModel.expression.Key;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Set;

/**
 * Convenience methods to create new Java model elements for an Tramp mapping
 * scenario.
 * 
 * @author lord_pretzel
 */
public class TrampModelFactory {

	private TrampXMLModel doc;
	private UniqueIdGen idGen;
	private MappingScenario stScen;
	private Configuration conf;

	public TrampModelFactory(MappingScenario stScen) {
		this.setStScen(stScen);
		this.doc = stScen.getDoc();
		initIdGen();
	}

	public void initAllElem(Configuration conf) {
		this.conf = conf;
		if (conf.getTrampXMLOutputOption(TrampXMLOutputSwitch.Correspondences))
			doc.getScenario().addNewCorrespondences();
		if (conf.getTrampXMLOutputOption(TrampXMLOutputSwitch.ConnectionInfo))
			createConnectionInfo();
		if (conf.getTrampXMLOutputOption(TrampXMLOutputSwitch.Transformations))
			doc.getScenario().addNewTransformations();
	}

	private void createConnectionInfo() {
		ConnectionInfoType info = doc.getScenario().addNewConnectionInfo();
		info.setDB(conf.getDBOption(DBOption.DBName));
		info.setPassword(conf.getDBOption(DBOption.Password));
		info.setPort(Integer.parseInt(conf.getDBOption(DBOption.Port)));
		info.setHost(conf.getDBOption(DBOption.URL));
		info.setUser(conf.getDBOption(DBOption.User));
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

	public CorrespondenceType addCorrespondence(String fromRel,
			String fromAttr, String toRel, String toAttr) {
		return addCorrespondence(fromRel, new String[] { fromAttr }, toRel,
				new String[] { toAttr });
	}

	public CorrespondenceType addCorrespondence(String fromRel,
			String[] fromAttrs, String toRel, String[] toAttrs) {
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

	public RelationType addRelation(String hook, String name, String[] attrs,
			String[] dTypes, boolean source) {
		SchemaType schema =
				source ? doc.getScenario().getSchemas().getSourceSchema() : doc
						.getScenario().getSchemas().getTargetSchema();
		RelationType rel = schema.addNewRelation();
		rel.setName(name);
		for (int i = 0; i < attrs.length; i++) {
			AttrDefType a = rel.addNewAttr();
			a.setName(attrs[i]);
			a.setDataType(dTypes[i]);
		}

		addSTRelation(hook, name, attrs, dTypes, source);

		if (source && conf.getOutputOption(OutputOption.Data)
				&& conf.getTrampXMLOutputOption(TrampXMLOutputSwitch.Data))
			addDataElement(name);

		return rel;
	}

	public RelationType addRelation(String hook, String name, String[] attrs,
			boolean source) {
		String[] dTypes = new String[attrs.length];
		Arrays.fill(dTypes, "TEXT");
		return addRelation(hook, name, attrs, dTypes, source);
	}

	private void addDataElement(String name) {
		if (!doc.getScenario().isSetData())
			doc.getScenario().addNewData();

		switch (conf.getDataGen()) {
		case TrampCSV:
			RelInstanceFileType inst =
					doc.getScenario().getData().addNewInstanceFile();
			inst.setFileName(name + ".csv");
			inst.setColumnDelim("|");
			inst.setName(name);
			inst.setPath(Configuration.getAbsoluteInstancePath());
			break;
		case TrampXMLInline:
			// TODO
			break;
		}
	}

	private void addSTRelation(String hook, String name, String[] attrs,
			String[] dTypes, boolean source) {
		Schema s = source ? stScen.getSource() : stScen.getTarget();

		SMarkElement es = new SMarkElement(name, new Set(), null, 0, 0);
		es.setHook(hook + "NL0CE0");

		for (int i = 0; i < attrs.length; i++) {
			Atomic dt = getDT(dTypes[i]);
			SMarkElement att =
					new SMarkElement(attrs[i], dt, null, 0, 0);
			att.setHook(hook + "NL1AE" + i);
			es.addSubElement(att);
		}

		s.addSubElement(es);
	}

	private Atomic getDT(String string) {
		if (string.equals("TEXT"))
			return Atomic.STRING;
		if (string.equals("INT8"))
			return Atomic.INTEGER;
		return null;
	}

	public Key addPrimaryKey(String relName, String[] attrs, boolean source)
			throws Exception {
		RelationType rel = doc.getRelForName(relName, !source);
		Schema s = source ? stScen.getSource() : stScen.getTarget();
		AttrListType k = rel.addNewPrimaryKey();
		for (String a : attrs)
			k.addAttr(a);

		Key key = new Key();
		key.addLeftTerm(new Variable("X"), new Projection(Path.ROOT, relName));
		key.setEqualElement(new Variable("X"));
		s.addConstraint(key);
		for (String a : attrs)
			key.addKeyAttr(new Projection(new Variable("X"), a));

		return key;
	}

	public MappingType addMapping(List<CorrespondenceType> cs) {
		return addMapping(cs.toArray(new CorrespondenceType[] {}));
	}

	public MappingType addMapping(CorrespondenceType[] cs) {
		MappingType map;

		map = doc.getScenario().getMappings().addNewMapping();

		map.setId(idGen.createId("Map"));
		;
		map.addNewExists();

		if (cs.length > 0) {
			Uses u = map.addNewUses();
			for (CorrespondenceType c : cs)
				u.addNewCorrespondence().setRef(c.getId());
		}

		return map;
	}

	public void addForeachAtom(String mKey, String relName, String[] vars)
			throws Exception {
		MappingType m = doc.getMapping(mKey);

		if (!m.isSetForeach())
			m.addNewForeach();

		RelAtomType a = m.getForeach().addNewAtom();
		a.setTableref(relName);
		for (String var : vars)
			a.addNewVar().setStringValue(var);
	}

	public void addExistsAtom(String mKey, String relName, String[] vars)
			throws Exception {
		MappingType m = doc.getMapping(mKey);

		RelAtomType a = m.getExists().addNewAtom();
		a.setTableref(relName);
		for (String var : vars)
			a.addNewVar().setStringValue(var);
	}

	public TransformationType addTransformation(String code, String[] maps,
			String creates) throws Exception {
		return addTransformation(code, doc.getMappings(maps), creates);
	}

	public TransformationType addTransformation(String code,
			MappingType[] maps, String creates) {
		TransformationType t =
				doc.getScenario().getTransformations().addNewTransformation();

		t.setId(idGen.createId("Trans"));
		t.setCode(code);
		t.setCreates(creates);
		Implements i = t.addNewImplements();

		for (MappingType m : maps)
			i.addNewMapping().setRef(m.getId());

		return t;
	}

	public TransformationType addTransformation(String code,
			Collection<MappingType> maps, String creates) {
		return addTransformation(code, maps.toArray(new MappingType[] {}),
				creates);
	}

	public String[] getFreshVars(int startVar, int num) {
		String[] result = new String[num];
		for (int i = 0; i < num; i++)
			result[i] = getVarString(startVar + i);

		return result;
	}

	private String getVarString(int i) {
		if (i < 26)
			return ((char) ('a' + i)) + "";

		String result = "";
		while (i > 0) {
			result += (char) ('a' + (i % 26));
			i /= 26;
		}
		return result;
	}

	public MappingScenario getStScen() {
		return stScen;
	}

	public void setStScen(MappingScenario stScen) {
		this.stScen = stScen;
	}
}
