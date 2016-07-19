package org.vagabond.benchmark.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.AttrListType;
import org.vagabond.xmlmodel.AttrRefType;
import org.vagabond.xmlmodel.ConnectionInfoType;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.FDType;
import org.vagabond.xmlmodel.ForeignKeyType;
import org.vagabond.xmlmodel.FunctionType;
import org.vagabond.xmlmodel.IDType;
import org.vagabond.xmlmodel.MappingScenarioDocument;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.MappingType.Uses;
import org.vagabond.xmlmodel.RelAtomType;
import org.vagabond.xmlmodel.RelInstanceFileType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SKFunction;
import org.vagabond.xmlmodel.SchemaType;
import org.vagabond.xmlmodel.SchemasType;
import org.vagabond.xmlmodel.TransformationType;
import org.vagabond.xmlmodel.TransformationType.Implements;

import smark.support.MappingScenario;
import smark.support.PartialMapping;
import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants.DBOption;
import tresc.benchmark.Constants.OutputOption;
import tresc.benchmark.Constants.TrampXMLOutputSwitch;
import vtools.dataModel.expression.ForeignKey;
import vtools.dataModel.expression.Key;
import vtools.dataModel.expression.Path;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.Variable;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.DataType;
import vtools.dataModel.types.DataTypeHandler;
import vtools.dataModel.types.Set;

/**
 * Convenience methods to create new Java model elements for an Tramp mapping
 * scenario.
 * 
 * @author lord_pretzel
 */

// PRG ADD Instance Method getNumUniqueId() (from existing file version 351) - July 18, 2012 

public class TrampModelFactory {

	static Logger log = Logger.getLogger(TrampModelFactory.class);
	
	private TrampXMLModel doc;
	private UniqueIdGen idGen;
	private MappingScenario stScen;
	private Configuration conf;
	private PartialMapping p;
	private MappingScenarioDocument oDoc;

	public enum FuncParamType {
		Var,
		Const
	}
	
	public TrampModelFactory(MappingScenario stScen) {
		this.setStScen(stScen);
		this.doc = stScen.getDoc();
		initODoc();
		initIdGen();
	}
	
	/**
	 * 
	 */
	private void initODoc() {
		oDoc = MappingScenarioDocument.Factory.newInstance();
		SchemasType schemas = oDoc.addNewMappingScenario().addNewSchemas();
		schemas.addNewSourceSchema();
		schemas.addNewTargetSchema();
	}

	public TrampModelFactory(TrampXMLModel doc) {
		this.doc = doc;
		initIdGen();
	}

	public void setPartialMapping (PartialMapping p) {
		this.p = p;
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
		idGen.createIdType("ID", "ID");
		idGen.createIdType("R", "");
	}

	public String getNextId(String idType) {
		return idGen.createId(idType);
	}
	
	/*
	 * Instance Method getNumUniqueId() returns the total number of uniquely generated identifiers of a given idType
	 * 
	 * @author prg
	 */
	// PRG ADD July 16, 2012
	public Integer getNumUniqueId(String idType) {        
		return idGen.getNumIds(idType);
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
		doc.indexCorr(c);
		
		c.addNewFrom();
		c.getFrom().setTableref(fromRel);
		for (String a : fromAttrs)
			c.getFrom().addAttr(a);

		c.addNewTo();
		c.getTo().setTableref(toRel);
		for (String a : toAttrs)
			c.getTo().addAttr(a);

		p.addCorr(c);
		
		return c;
	}
	
	public CorrespondenceType addCorrespondence(CorrespondenceType c) {
		CorrespondenceType newC = doc.getDocument().getMappingScenario().getCorrespondences().addNewCorrespondence();
		newC.set(c);
		p.addCorr(newC);
		return newC;
	}
	
	public void addPKFds () throws Exception {
		for (RelationType r: doc.getScenario().getSchemas().getSourceSchema().getRelationArray()) {
			if (r.isSetPrimaryKey())
				addFD(r.getName(), doc.getPK(r.getName(), true), doc.getNonKeyAttr(r.getName(), true));
		}
	}
	
	public FDType addFD (String rel, String[] lAttrs, String[] rAttrs) {
		FDType fd = doc.getScenario().getSchemas().getSourceSchema().addNewFD();
		
		fd.setId(getNextId("FD"));
		fd.setTableref(rel);
		fd.addNewFrom();
		fd.addNewTo();
		
		for(String a: lAttrs)
			fd.getFrom().addAttr(a);
		
		for(String a: rAttrs)
			fd.getTo().addAttr(a);;
		
		return fd;
	}
	
	public FDType addFD (FDType fd) {
		FDType newFd = doc.getScenario().getSchemas().getSourceSchema().addNewFD();
		newFd.set(fd);
		return newFd;
	}
	
	public FDType[] getRelFDs (String rel) {
		FDType[] fd = doc.getScenario().getSchemas().getSourceSchema().getFDArray();
		
		return fd;
	}
	
	public IDType addID (String fromRel, String[] fromAttrs, String toRel, String[] toAttrs, boolean source) {
		SchemaType s = source ? doc.getScenario().getSchemas().getSourceSchema() : doc.getScenario().getSchemas().getTargetSchema();
		IDType id = s.addNewID();
		
		id.setId(getNextId("ID"));
		
		AttrRefType from = id.addNewFrom();
		from.setTableref(fromRel);
		for(String a: fromAttrs)
			from.addAttr(a);
		AttrRefType to = id.addNewTo();
		to.setTableref(toRel);
		for(String a: toAttrs)
			to.addAttr(a);
		
		return id;
	}
	

	public RelationType addRelation(String hook, String name, String[] attrs,
			String[] dTypes, boolean source) {
		SchemaType schema =
				source ? doc.getScenario().getSchemas().getSourceSchema() : doc
						.getScenario().getSchemas().getTargetSchema();
		RelationType rel = schema.addNewRelation();
		rel.setName(name);
		doc.indexRel(rel, source);
		
		addToIndex(rel, source);
		for (int i = 0; i < attrs.length; i++) {
			AttrDefType a = rel.addNewAttr();
			a.setName(attrs[i]);
			a.setDataType(dTypes[i]);
		}

		log.debug(Arrays.toString(dTypes));
		addSTRelation(hook, name, attrs, dTypes, source);

		if (source && conf.getOutputOption(OutputOption.Data)
				&& conf.getTrampXMLOutputOption(TrampXMLOutputSwitch.Data))
			addDataElement(name);

		if (source)
			p.addSourceRel(rel);
		else
			p.addTargetRel(rel);
		
		return rel;
	}
	
	public void addRelation(String hook, RelationType r, boolean source) {
		String[] attr = new String[r.sizeOfAttrArray()];
		String[] dTypes = new String[r.sizeOfAttrArray()];
		
		for(int i = 0; i < attr.length; i++) {
			AttrDefType a = r.getAttrArray(i);
			attr[i] = a.getName();
			dTypes[i] = a.getDataType();
		}
		
		SchemaType s = doc.getSchema(source);
		RelationType newR = s.addNewRelation();
		newR.set(r);
		
		doc.indexRel(newR, source);
		addSTRelation(hook, r.getName(), attr, dTypes, source);
		addToIndex(r, source);
		
		// create PK for iBench
		if (r.isSetPrimaryKey())
			addiBenchKey(r.getName(), r.getPrimaryKey().getAttrArray(), source);
		
		// create data element
		if (source && conf.getOutputOption(OutputOption.Data)
				&& conf.getTrampXMLOutputOption(TrampXMLOutputSwitch.Data))
			addDataElement(newR.getName());
		
		if (source)
			p.addSourceRel(r);
		else
			p.addTargetRel(r);
	}
	
	private void addToIndex (RelationType r, boolean source) {
		if (source)
			doc.getSourceRels().put(r.getName(), r);
		else
			doc.getTargetRels().put(r.getName(), r);
		doc.getRelPos().put("");
	}
	
	public RelationType addRelation(String hook, String name, String[] attrs,
			boolean source) {
		String[] dTypes = new String[attrs.length];
		String[] dTypesNames = new String[attrs.length];
		Arrays.fill(dTypes, null);
		
		Atomic data;
		
		for (int k = 0; k < dTypes.length; k++) {
			Random randGen = new Random();
			data = DataTypeHandler.getInst().getRandomDT(randGen);
			if (!(data instanceof DataType)) {
				dTypes[k] = "TEXT";
				dTypesNames[k] = "text";
			}
			else {
				dTypesNames[k] = ((DataType)data).getName().toLowerCase(); // fill with email, phoneNumber, names, etc
				dTypes[k] = DataTypeHandler.getInst().getDbType(dTypesNames[k]); // fill with DB types, text, int, etc
			}

		}
		DataTypeHandler.getInst().setTypesNamesOrder(dTypesNames);
		return addRelation(hook, name, attrs, dTypes, source);
		
	}

	@SuppressWarnings("incomplete-switch")
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

	public String getDT(Atomic dt) {
		if (dt == Atomic.INTEGER)
			return "INT8";
		if (dt == Atomic.STRING)
			return "TEXT";
		if (dt instanceof DataType)
		{
			DataType dataT = (DataType) dt;
			String dbType = dataT.getDbType();
			if (dbType != null)
				return dbType;
			return "TEXT";
		}
		
		return null;
	}
	
	public Atomic getDT(String string) {
		if (string.equals("TEXT"))
			return Atomic.STRING;
		if (string.equals("INT8"))
			return Atomic.INTEGER;
		//access the map

		log.error("USED DEFAULT FOR: " + string);

		log.error("SHOULD HAVE NEVER COME HERE");

		return Atomic.STRING;
	}
	
	public Key addPrimaryKey(String relName, String[] attrs, boolean source)
			throws Exception {
		RelationType rel = doc.getRelForName(relName, !source);
		Schema s = source ? stScen.getSource() : stScen.getTarget();
		AttrListType k = rel.addNewPrimaryKey();
		for (String a : attrs)
			k.addAttr(a);

		Key key = addiBenchKey(relName, attrs, source);

		return key;
	}

	private Key addiBenchKey(String relName, String[] attrs, boolean source) {
		Schema s = source ? stScen.getSource() : stScen.getTarget();
		Key key = new Key();
		key.addLeftTerm(new Variable("X"), new Projection(Path.ROOT, relName));
		key.setEqualElement(new Variable("X"));
		s.addConstraint(key);
		for (String a : attrs)
			key.addKeyAttr(new Projection(new Variable("X"), a));
		return key;
	}
	
	public void addPrimaryKey(String relName, int attrPos, boolean source) throws Exception {
		addPrimaryKey(relName, new int[] {attrPos}, source);
	}
	
	public void addPrimaryKey(String relName, int[] attrPos, boolean source) throws Exception {
		String[] attrs = new String[attrPos.length];
		RelationType r = doc.getRelForName(relName, !source);
		AttrDefType[] allAtt = r.getAttrArray();
		for(int i = 0; i  < attrPos.length; i++)
			attrs[i] = allAtt[attrPos[i]].getName();
		addPrimaryKey(relName, attrs, source);
	}
	
	public void addPrimaryKey(String relName, String attrId, boolean source) throws Exception {
		addPrimaryKey(relName, new String[] {attrId}, source);
	}
	
	public void addSymForeignKey (String fromRel, String[] fromA, String toRel, 
			String[] toA, boolean source) {
		addForeignKey(fromRel, fromA, toRel, toA, source);
		addForeignKey(toRel, toA, fromRel, fromA, source);
	}
	
	public void addForeignKey (String fromRel, String fromA, String toRel, 
			String toA, boolean source) {
		addForeignKey(fromRel, new String[] {fromA}, toRel, new String[] {toA}, source);
	}
	
	public void addForeignKey (String fromRel, String[] fromA, String toRel, 
			String[] toA, boolean source) {
		Schema s = source ? stScen.getSource() : stScen.getTarget();
		SchemaType st = source ? oDoc.getMappingScenario().getSchemas().getSourceSchema() :
			oDoc.getMappingScenario().getSchemas().getTargetSchema();
		
		ForeignKeyType fk = st.addNewForeignKey();
		AttrRefType from = fk.addNewFrom();
		from.setTableref(fromRel);
		for(String a: fromA)
			from.addAttr(a);
		AttrRefType to = fk.addNewTo();
		to.setTableref(toRel);
		for(String a: toA)
			to.addAttr(a);
		
		ForeignKey stFK = new ForeignKey();
		Variable fromVar = new Variable("F");
		stFK.addLeftTerm(fromVar, new Projection(Path.ROOT, fromRel));
		Variable toVar = new Variable("K");
		stFK.addRightTerm(toVar, new Projection(Path.ROOT, toRel));
		//MN BEGIN - 24 August 2014
		for(int count=0; count<toA.length; count++)
			stFK.addFKeyAttr(new Projection(toVar, toA[count]), 
				new Projection(fromVar, fromA[count])); //TODO check whether supports more than one attr in FK
		//MN END
		s.addConstraint(stFK);
	}
	
	public void addForeignKey (ForeignKeyType fks, boolean source) {
		Schema s = source ? stScen.getSource() : stScen.getTarget();
		SchemaType st = source ? doc.getScenario().getSchemas().getSourceSchema() :
			doc.getScenario().getSchemas().getTargetSchema();
		
		ForeignKeyType fk = st.addNewForeignKey();
		fk.set(fks);
		String fromRel = fk.getFrom().getTableref();
		String[] fromA = fk.getFrom().getAttrArray();
			
		String toRel = fk.getTo().getTableref();
		String[] toA = fk.getTo().getAttrArray();
		
		ForeignKey stFK = new ForeignKey();
		Variable fromVar = new Variable("F");
		stFK.addLeftTerm(fromVar, new Projection(Path.ROOT, fromRel));
		Variable toVar = new Variable("K");
		stFK.addRightTerm(toVar, new Projection(Path.ROOT, toRel));
		//MN BEGIN - 24 August 2014
		for(int count=0; count<toA.length; count++)
			stFK.addFKeyAttr(new Projection(toVar, toA[count]), 
				new Projection(fromVar, fromA[count])); //TODO check whether supports more than one attr in FK
		//MN END
		s.addConstraint(stFK);
	}

	public MappingType addMapping(List<CorrespondenceType> cs) {
		return addMapping(cs.toArray(new CorrespondenceType[] {}));
	}

	public MappingType addMapping(CorrespondenceType[] cs) {
		MappingType map;

		map = doc.getScenario().getMappings().addNewMapping();

		map.setId(idGen.createId("Map"));
		doc.indexMapping(map);
		map.addNewExists();

		if (cs != null && cs.length > 0) {
			Uses u = map.addNewUses();
			for (CorrespondenceType c : cs)
				u.addNewCorrespondence().setRef(c.getId());
		}

		p.addMapping(map);
		
		return map;
	}

	public MappingType addMapping (MappingType m) {
		MappingType newM = doc.getScenario().getMappings().addNewMapping();
		newM.set(m);
		doc.indexMapping(newM);
		p.addMapping(newM);
		return newM;
	}
	
	public void addForeachAtom(MappingType m, int rel, String[] vars) 
			throws Exception {
		addForeachAtom(m.getId(), p.getRelName(rel, true), vars);
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

	public RelAtomType addEmptyExistsAtom (MappingType m, int rel) {
		RelAtomType atom = m.getExists().addNewAtom();
		atom.setTableref(p.getRelName(rel, false));
		return atom;
	}
	
	public void addVarsToExistsAtom (MappingType m, int atom, String[] vars) {
		RelAtomType a = m.getExists().getAtomArray(atom);
		for(String var: vars)
			a.addNewVar().setStringValue(var);
	}
	
	public void addVarToExistsAtom (MappingType m, int atom, String var) {
		m.getExists().getAtomArray(atom).addNewVar().setStringValue(var);
	}
	
	public void addFuncToExistsAtom (MappingType m, int atom, String fName,  String[] params) {
		RelAtomType a = m.getExists().getAtomArray(atom);
		FunctionType f =  a.addNewFunction();
		f.setFname(fName);
		for(String p : params)
			f.addVar(p);
	}
	
	public void addFuncToExistsAtom (MappingType m, int atom, String fName, 
			String[] params, FuncParamType[] types) {
		RelAtomType a = m.getExists().getAtomArray(atom);
		FunctionType f =  a.addNewFunction();
		f.setFname(fName);
		for(int i = 0; i < params.length; i++) {
			String p = params[i];
			FuncParamType type = types[i];
			
			switch(type) {
			case Var:
				f.addVar(p);
				break;
			case Const:
				f.addConstant(p);
				break;
			}
		}
	}
	
	public String addSKToExistsAtom (MappingType m, int atom, String[] params) {
		RelAtomType a = m.getExists().getAtomArray(atom);
		SKFunction f = a.addNewSKFunction();
		f.setSkname(getNextId("SK"));
		for(String p: params)
			f.addNewVar().setStringValue(p);
		
		return f.getSkname();
	}
	
	public void addSKToExistsAtom (MappingType m, int atom, String[] params, String skId) {
		RelAtomType a = m.getExists().getAtomArray(atom);
		SKFunction f = a.addNewSKFunction();
		f.setSkname(skId);
		for(String p: params)
			f.addNewVar().setStringValue(p);
	}
	
	public void addExistsAtom(MappingType m, int rel, String[] vars) 
			throws Exception {
		addExistsAtom(m.getId(), p.getRelName(rel, false), vars);
	}
	
	public void addExistsAtom(String mKey, String relName, String[] vars)
			throws Exception {
		MappingType m = doc.getMapping(mKey);

		RelAtomType a = m.getExists().addNewAtom();
		a.setTableref(relName);
		for (String var : vars)
			a.addNewVar().setStringValue(var);
	}


	
	public TransformationType addTransformation(String code, String map,
			String creates) throws Exception {
		return addTransformation(code, doc.getMappings(new String[] {map}), creates);
	}
	
	public TransformationType addTransformation(String code, String[] maps,
			String creates) throws Exception {
		return addTransformation(code, doc.getMappings(maps), creates);
	}

	public TransformationType addTransformation(String code,
			MappingType[] maps, String creates) throws Exception {
		TransformationType t = getTransformation(creates);
		
		// no previous transformation for relation
		if (t == null) {
			t = doc.getScenario().getTransformations().addNewTransformation();
	
			t.setId(idGen.createId("Trans"));
			t.setCode(code);
			t.setCreates(creates);
			Implements i = t.addNewImplements();
	
			for (MappingType m : maps)
				i.addNewMapping().setRef(m.getId());
	
			p.addTrans(t);
		}
		// previous transformation just adapt code and add mappings
		else {
			t.setCode(code);
			
			for(MappingType m: maps)
				t.getImplements().addNewMapping().setRef(m.getId());
		}
		return t;
	}
	
	public TransformationType getTransformation (String relname) throws Exception {
		List<TransformationType> ts = doc.getTransCreatingRel(relname);
		if (ts.size() == 0)
			return null;
		return ts.get(0);
	}

	public TransformationType addTransformation(String code,
			Collection<MappingType> maps, String creates) throws Exception {
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

	public void indexMappings() {
		MappingType[] maps = doc.getMappings();
		Map<String, ArrayList<MappingType>> relToMap = doc.getMapsForSourceRel();
		
		for(MappingType m: maps) {
			if(m.getForeach() == null) {
				if (log.isDebugEnabled()) 
					log.debug("ERROR: Scenario has no Foreach clause!");
			}
			else {
				for (RelAtomType a: m.getForeach().getAtomArray()) {
					String tabName = a.getTableref();
					ArrayList<MappingType> relMaps = relToMap.get(tabName);
					if (relMaps == null) {
						relMaps = new ArrayList<MappingType> ();
						relToMap.put(tabName, relMaps);
					}
					if (!relMaps.contains(m)) // use set?
						relMaps.add(m);
				}
			}
		}
	}

	public void copyFKsToRealDoc () {
		SchemaType oSource, oTarget, source, target;
		source = doc.getSchema(true);
		oSource = oDoc.getMappingScenario().getSchemas().getSourceSchema();
		target = doc.getSchema(false);
		oTarget = oDoc.getMappingScenario().getSchemas().getTargetSchema();
		
		for(ForeignKeyType fk: oSource.getForeignKeyArray())
		{
			ForeignKeyType fkNew = source.addNewForeignKey();
			fkNew.set(fk);
		}
		
		for(ForeignKeyType fk: oTarget.getForeignKeyArray())
		{
			ForeignKeyType fkNew = target.addNewForeignKey();
			fkNew.set(fk);
		}
	}
}
