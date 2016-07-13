package tresc.benchmark.dataGen;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.vagabond.util.CollectionUtils;
import org.vagabond.util.DirectedGraph;
import org.vagabond.util.IdMap;
import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.AttrListType;
import org.vagabond.xmlmodel.ForeignKeyType;
import org.vagabond.xmlmodel.RelationType;

import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import vtools.dataModel.schema.Element;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.DataTypeHandler;
import vtools.dataModel.types.Set;
import vtools.dataModel.types.Type;

public class ToXScriptOnlyDataGenerator extends DataGenerator {
	
	public String f (String s) {
		return s;//return "h"+Integer.toString(s.hashCode());
	}
	

	static Logger log = Logger.getLogger(ToXScriptOnlyDataGenerator.class);

	public static final String LIST_NAME_SUFFIX = "";//"_List";

	List<StringBuffer> toxLists;
	List<StringBuffer> toxTypes;
	DirectedGraph<String> dependencies;
	IdMap<String> toxListPos; 
	StringBuffer documentBuffer;
	StringBuffer templateBuffer;
	String documentName;
	List<SMarkElement> coveredAtomicElements;
	String outputPath;
	String instanceXMLFile;

	String template;

	public ToXScriptOnlyDataGenerator(Configuration config) {
		super(config);
		initBuffers();
	}
	
	public ToXScriptOnlyDataGenerator(Schema schema, Configuration config) {
		super(schema, config);
		initBuffers();
	}

	@Override
	protected void initFromConfig() {
		super.initFromConfig();
		documentName = config.getSourceDocumentName();
		outputPath = Configuration.getInstancePathPrefix();
		template = config.getSourceInstanceFile();
	}

	public void initBuffers() {
		toxLists = new ArrayList<StringBuffer>();
		toxTypes = new ArrayList<StringBuffer>();
		documentBuffer = new StringBuffer();
		templateBuffer = new StringBuffer();
		coveredAtomicElements = new ArrayList<SMarkElement>();
		documentName = config.getSourceDocumentName();
		dependencies = new DirectedGraph<String> (false);
		toxListPos = new IdMap<String> ();
	}

	@Override
	public void generateData() throws Exception {
		generateToxTemplate();
	}

	protected void generateToxTemplate() throws Exception {
		// just to make sure we start with empty buffers
		initBuffers();

		String schemaName = schema.getLabel();
		
		generateToxTypes();

		generateDocumentOpening(documentName, schemaName, documentBuffer);
		for (int i = 0; i < schema.size(); i++) {
			SMarkElement rootSetElt = (SMarkElement) schema.getSubElement(i);

			generateListIterationConstruct(rootSetElt.getLabel(),
					documentBuffer);

			generateToxList(rootSetElt, repElemCount);
		}

		generateDocumentClosing(schemaName, documentBuffer);

		// ///////////////////////////////////////

		generateTemplateOpening(templateBuffer);
		
		
		for (StringBuffer typeBuf : toxTypes)
			templateBuffer.append(typeBuf);

		outputToxLists(templateBuffer);

		templateBuffer.append(documentBuffer);

		generateTemplateClosing(templateBuffer);

		if (log.isDebugEnabled()) {log.debug(templateBuffer);};

		BufferedWriter bufWriter =
				new BufferedWriter(new FileWriter(new File(
						Configuration.getInstancePathPrefix(),
						config.getSourceInstanceFile())));
		bufWriter.write(templateBuffer.toString());
		bufWriter.close();
	}

	private void outputToxLists(StringBuffer templateBuffer2) throws Exception {
		List<String> topoSort = dependencies.topologicalSort();
		
		if (log.isDebugEnabled()) {log.debug("sorted as " + topoSort.toString() 
				+ "\n\n base on\n" + dependencies.toString());};
		
		for(int i = 0; i < topoSort.size(); i++) {
			int pos = toxListPos.getId(topoSort.get(i));
			templateBuffer.append(toxLists.get(pos));
		}
	}

	private void generateToxTypes() {
		
		StringBuffer benchStringTypeBuf = new StringBuffer();
		generateBenchStringType(benchStringTypeBuf);
		toxTypes.add(benchStringTypeBuf);

		StringBuffer intTypeBuf = new StringBuffer();
		generateBenchIntType(intTypeBuf);
		toxTypes.add(intTypeBuf);
	}

	private void
			visitSchemaElement(SMarkElement schemaElement, StringBuffer buf, int index) {
		String label = schemaElement.getLabel();
		Type type = schemaElement.getType();
		
		if (type instanceof Set) {
			generateComplexElementOpening(label, buf, repElemCount);
			for (int i = 0; i < schemaElement.size(); i++)
				visitSchemaElement(
						(SMarkElement) schemaElement.getSubElement(i), buf, i);
			generateComplexElementClosing(buf);
		}

		if (type instanceof Atomic) {
			if (!coveredAtomicElements.contains(schemaElement)) {
				SMarkElement[][] srcConstraint = findConstraint(schemaElement);

				// the element is not subject to any constraints,
				// or to a constraint in the same set
				// (for now, we don't sample in this case since
				// we need to sample from the list that is being generated now)
				if ((srcConstraint == null) || referencesSameSet(srcConstraint)) {
					Atomic atomicType = (Atomic) type;
					generateAtomicElementConstruct(label, buf, atomicType, index);
					coveredAtomicElements.add(schemaElement);
				}
				else {
					// the element is subject to a constraint
					// need to generate the tox-sample construct
					// for the constraint

					generateToxSampleConstruct(srcConstraint, buf);

				}
			}
		}

	}

	// checks whether the source and target of a constraint reference the same
	// set
	private boolean referencesSameSet(SMarkElement[][] srcConstraint) {
		for (int i = 0; i < srcConstraint.length; i++) {
			SMarkElement src = srcConstraint[i][0];
			SMarkElement tgt = srcConstraint[i][1];
			if (src.getParent() == tgt.getParent())
				return true;
		}

		return false;
	}

	// finds a source constraint with the schemaElement in the LHS
	// (if there is none, it returns null)
	private SMarkElement[][] findConstraint(SMarkElement schemaElement) {
		for (SMarkElement[][] constraint : constraints) {
			for (int i = 0; i < constraint.length; i++) {
				if (log.isDebugEnabled()) {log.debug(constraint[i][0]);};
				if (constraint[i][0].toString()
						.equals(schemaElement.toString()))
					return constraint;
			}
		}
		return null;
	}

	private void generateToxList(SMarkElement schemaElement, int eltCount)
			throws Exception {
		StringBuffer listBuf = new StringBuffer();

		String label = schemaElement.getLabel();
		String listName = label + LIST_NAME_SUFFIX;
		
		
		
		if (!hasSelfFK(schemaElement)) {
			String unique = generateUnique(schemaElement);
			generateListOpening(label, listBuf, eltCount, unique, "");

			for (int i = 0; i < schemaElement.size(); i++)
				visitSchemaElement(
						(SMarkElement) schemaElement.getSubElement(i), listBuf, i);

			generateListClosing(listBuf);
			
			// add to list of tox list and create node in dependency graph
			memToxList(listBuf, listName);
		}
		else {
			generateSetWithSelfJoin(schemaElement, eltCount);
		}
	}

	private void memToxList(StringBuffer listBuf, String listName)
			throws Exception {
		toxLists.add(listBuf);
		dependencies.addNode(listName);
		toxListPos.put(toxLists.size() - 1, listName);
	}

	private boolean hasSelfFK(SMarkElement schemaElement) {
		String rel = schemaElement.getLabel();
		return scen.getDoc().getFKs(rel, rel, true).length != 0;
	}

	// generate self-join constraints
	private void generateSetWithSelfJoin(SMarkElement schemaElement,
			int eltCount) throws Exception {
		StringBuffer listBuf = new StringBuffer();
		String label = schemaElement.getLabel();
		String labelListName = label + LIST_NAME_SUFFIX;
		String keyLabel = label + "_keys";
		String keyListName = keyLabel + LIST_NAME_SUFFIX;
		AttrDefType[] keyAttrs = scen.getDoc().getKeyAttrs(label, true);
		String[] keyAttrNames = new String[keyAttrs.length];
		
		// generate list for the key attribute values
		generateListOpening(keyLabel, listBuf, eltCount, 
				getUniqueCode(keyLabel, keyAttrs[0].getName()), "");
//TODO right now unique in one attribute as a workaround unil clear how to do uniqueness over multiple attrs
		
		
		for(int i = 0; i < keyAttrs.length; i++) {
			keyAttrNames[i] = keyAttrs[i].getName();
			generateAtomicElementConstruct(keyAttrNames[i], listBuf, 
					scen.getDocFac().getDT(keyAttrs[i].getDataType()), i);
		}
		
		generateListClosing(listBuf);
		memToxList(listBuf, keyListName);
		
		listBuf = new StringBuffer();
		
		// generate the list with the elements
		ForeignKeyType fk = scen.getDoc().getFKs(label, label, true)[0];
		AttrDefType[] fkAttrs = scen.getDoc().getAttrs(label, 
				fk.getFrom().getAttrArray(), true);
		AttrDefType[] attrs = scen.getDoc().getRelForName(label, false).getAttrArray();
		generateListOpening(label, listBuf, eltCount, "", "");
		String keyListPath = keyLabel + LIST_NAME_SUFFIX + "/" + keyLabel;
		
		int index = 0;
		// loop through attributes and 
		for(AttrDefType a: attrs) {
			Atomic dt = scen.getDocFac().getDT(a.getDataType());
			// is a key attr
			int pos = CollectionUtils.searchPos(keyAttrs, a);
			int fkPos = CollectionUtils.searchPos(fkAttrs, a);
			index++; // to save the last position
			if (pos != -1) {
				String expr = a.getName();
				generateAtomicFromSamplePathConstruct(a.getName(), listBuf, dt, keyListPath, expr);
			}
			// is FK
			else if (fkPos != -1) {
				String expr = keyAttrs[fkPos].getName();
				generateAtomicFromPathConstruct(a.getName(), listBuf, dt, keyListPath, expr);	
			} 
			// normal attr
			else {
				generateAtomicElementConstruct(a.getName(), listBuf, dt, index);
			}
		}
		generateListClosing(listBuf);
		memToxList(listBuf, labelListName);
		dependencies.addNodesAndEdge(keyListName, labelListName);
	}

	// generate primary key constraints
	private String generateUnique(SMarkElement schemaElement) throws Exception {
		RelationType rel = scen.getDoc().getRelForName(
				schemaElement.getLabel().toLowerCase(), false);

		if (!rel.isSetPrimaryKey())
			return "";

		AttrListType key = rel.getPrimaryKey();

//		if (key.getAttrArray().length == 1) {
			return getUniqueCode(rel.getName(), key.getAttrArray(0));
//		}

		//TODO how to do multiple attr keys in toxgene?

//		return "unique";
	}

	private String getUniqueCode(String rel, String keyAttr) {
		return " unique=\"" + f(rel) + "/" + f(keyAttr)
				+ "\" ";
	}

	private void generateListOpening(String eltName, StringBuffer buf,
			int eltCount, String unique, String where) {
		buf.append("<tox-list name=\"" + f(eltName) + LIST_NAME_SUFFIX + "\""
				+ unique + where + ">\n");
		generateComplexElementOpening(eltName, buf, eltCount);
	}

	private void generateListClosing(StringBuffer buf) {
		generateComplexElementClosing(buf);
		buf.append("</tox-list>\n");
	}

	private void generateComplexElementOpening(String eltName,
			StringBuffer buf, int eltCount) {
		buf.append("<element name=\"" + f(eltName) + "\" minOccurs=\"" + eltCount
				+ "\" maxOccurs=\"" + eltCount + "\">\n");
		buf.append("<complexType>\n");
	}

	private void generateComplexElementClosing(StringBuffer buf) {
		buf.append("</complexType>\n");
		buf.append("</element>\n");
	}

	private void generateAtomicElementConstruct(String eltName,
			StringBuffer buf, Atomic atomicType, int index) {
		
		
		String typeName = DataTypeHandler.getInst().getTypes().get(index).getName();
		
		//test
		String typeString = null;
		if (atomicType == Atomic.INTEGER) {
			typeString = "bench_int";
		} else if (atomicType == Atomic.STRING) {
			typeString = "bench_" + typeName.toLowerCase(); // string
		} else {
			typeString = "bench_" + typeName.toLowerCase();
		}
				
		//String typeString =
		//		(atomicType == Atomic.STRING) ? "bench_string" : "bench_int";
		buf.append("<element name=\"" + f(eltName) + "\" type=\"" + typeString
				+ "\"/>\n");
	}
	
	private void generateAtomicFromPathConstruct (String eltName,
			StringBuffer buf, Atomic atomicType, String path, String expr) {
		//Defines the type by element
		String typeString = null;
		if (atomicType == Atomic.INTEGER) {
			typeString = "bench_int";
		} else if (atomicType == Atomic.STRING) {
			typeString = "bench_email"; //string
		} else {
			typeString = "bench_email";
		}
		
		//String typeString =
		//		(atomicType == Atomic.STRING) ? "bench_string" : "bench_int";
		buf.append("<element name=\"" + eltName + "\" type=\"" + typeString
				+ "\">\n");
		buf.append("\t<simpleType>\n" + 
				"\t\t<restriction base=\"string\">\n");
		buf.append("\t\t\t<tox-sample path=\"[" + path + "]\">\n");		
		buf.append("\t\t\t\t<tox-expr value=\"[" + expr + "]\"/>\n");
		buf.append("\t\t\t</tox-sample>\n");
		buf.append("\t\t</restriction>\n\t</simpleType>");
		buf.append("</element>\n");
	}
	
	private void generateAtomicFromSamplePathConstruct (String eltName,
			StringBuffer buf, Atomic atomicType, String path, String expr) {
		
		
		String typeString = null;
		if (atomicType == Atomic.INTEGER) {
			typeString = "bench_int";
		} else if (atomicType == Atomic.STRING) {
			typeString = "bench_email"; //bench_string
		} else {
			typeString = "bench_email";
		}
		
		//String typeString =
		//		(atomicType == Atomic.STRING) ? "bench_string" : "bench_int";
		
		buf.append("<element name=\"" + eltName + "\" type=\"" + typeString
				+ "\">\n");
		
		buf.append("\t<simpleType>\n" + 
				"\t\t<restriction base=\"string\">\n");
		buf.append("\t\t\t<tox-scan path=\"[" + path + "]\">\n");		
		buf.append("\t\t\t\t<tox-expr value=\"[" + expr + "]\"/>\n");
		buf.append("\t\t\t</tox-scan>\n");
		buf.append("\t\t</restriction>\n\t</simpleType>");
		buf.append("</element>\n");
	}
	
	private void
			generateListIterationConstruct(String eltName, StringBuffer buf) {
		buf.append("<tox-foreach path=\"[" + f(eltName) + LIST_NAME_SUFFIX + "/"
				+ f(eltName) + "]\">\n");
		buf.append("<tox-expr value=\"[!]\"/>\n");
		buf.append("</tox-foreach>\n");
	}

	private void generateDocumentOpening(String docName, String schemaName,
			StringBuffer buf) {
		buf.append("<tox-document name=\"" + docName + "\">\n");
		buf.append("<element name=\"" + schemaName + "\">\n");
		buf.append("<complexType>\n");
	}

	private void generateDocumentClosing(String schemaName, StringBuffer buf) {
		buf.append("</complexType>\n");
		buf.append("</element>\n");
		buf.append("</tox-document>\n");
	}

	private void generateTemplateOpening(StringBuffer buf) {
		buf.append("<?xml version='1.0' encoding='ISO-8859-1' ?>\n");
		buf.append("<!DOCTYPE tox-template SYSTEM 'http://www.cs.toronto.edu/tox/toxgene/ToXgene2.dtd'>\n");
		buf.append("<tox-template>\n");
	}

	private void generateTemplateClosing(StringBuffer buf) {
		buf.append("</tox-template>\n");
	}

	private void generateBenchIntType(StringBuffer buf) {
		buf.append("<simpleType name=\"bench_int\">\n");
		buf.append("<restriction base=\"positiveInteger\">\n");
		buf.append("<minInclusive value=\"1\"/>\n");
		buf.append("<maxInclusive value=\"" + maxNumValue + "\"/>\n");
		buf.append("</restriction>\n");
		buf.append("</simpleType>\n");

	}

	private void generateBenchStringType(StringBuffer buf) {
		String[] benchStringTypes = {
				"gibberish", "text", "xmrk_text", "email", "fname", 
				"lname", "city", "country", "province", "domain", "word",
				"multiword"
				};
		
		for (int i = 0; i < benchStringTypes.length; i++) {
			buf.append("<simpleType name=\"bench_" + benchStringTypes[i] + "\">\n");
			buf.append("<restriction base=\"string\">\n");
			buf.append("<tox-string type=\"" + benchStringTypes[i] + "\" maxLength=\"" + maxStringLength
					+ "\"/>\n");
			buf.append("</restriction>\n");
			buf.append("</simpleType>\n");
		}

	}
		
	private void generateToxSampleConstruct(SMarkElement[][] constraint,
			StringBuffer buf) {
		// we get the set of the target and generate the tox-sample header
		SMarkElement parent = (SMarkElement) constraint[0][1].getParent();
		generateToxSampleOpening(parent, buf);

		for (int i = 0; i < constraint.length; i++) {
			SMarkElement thisElement = constraint[i][0];
			SMarkElement sampleFromElement = constraint[i][1];

			buf.append("<element name=\"" + thisElement.getLabel() + "\">\n");
			buf.append("<tox-expr value=\"[" + sampleFromElement.getLabel()
					+ "]\"/>\n");
			buf.append("</element>\n");

			coveredAtomicElements.add(thisElement);

		}

		generateToxSampleClosing(buf);

		String thisList = getParentList(constraint[0][0]);
		String otherList = getParentList(parent);
		dependencies.addNodesAndEdge(otherList, thisList);
		if (log.isDebugEnabled()) {log.debug("added dependency from <" + thisList + "> to <" + otherList + ">");};
	}

	private void generateToxSampleOpening(SMarkElement schemaElement,
			StringBuffer buf) {
		buf.append("<tox-sample path=\"[");
		String samplePath = getSamplePath(schemaElement);

		StringTokenizer st = new StringTokenizer(samplePath, "/");
		// st.nextToken();
		buf.append(st.nextToken() + LIST_NAME_SUFFIX);

		buf.append(samplePath);
		buf.append("]\">\n");
	}

	private String getParentList (Element schemaElement) {
		Element parent = schemaElement;
		while(parent.getParent().getParent() != null)
			parent = parent.getParent();
		return parent.getLabel() + LIST_NAME_SUFFIX;
	}
	
	private String getSamplePath(Element schemaElement) {
		Element parent = schemaElement.getParent();
		if (parent == null)
			return "";
		else
			return getSamplePath(parent) + "/" + schemaElement.getLabel();
	}

	private void generateToxSampleClosing(StringBuffer buf) {
		buf.append("</tox-sample>\n");
	}

	public StringBuffer getDataBuffer() {
		return templateBuffer;
	}

}
