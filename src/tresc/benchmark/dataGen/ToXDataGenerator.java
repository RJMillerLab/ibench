package tresc.benchmark.dataGen;

import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.dataGen.toxgenewrap.ToXGeneWrapper;

import vtools.dataModel.schema.Schema;
import vtools.dataModel.schema.Element;
import vtools.dataModel.types.Rcd;
import vtools.dataModel.types.Set;
import vtools.dataModel.types.Type;
import vtools.dataModel.types.Atomic;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;

public class ToXDataGenerator extends DataGenerator {
	
	static Logger log = Logger.getLogger(ToXDataGenerator.class);
	
	static final String LIST_NAME_SUFFIX = "_List";

	// static final int STRING_LENGTH=25;
	// static final int INT_DOMAIN=10000;

	ToXGeneWrapper toxGen;

	List<StringBuffer> toxLists;
	List<StringBuffer> toxTypes;
	StringBuffer documentBuffer;
	StringBuffer templateBuffer;
	String documentName;
	List<SMarkElement> coveredAtomicElements;
	String outputPath;
	String instanceXMLFile;

	private String template;

	public ToXDataGenerator(Configuration config) {
		super(config);
		initBuffers();
	}

	public ToXDataGenerator(Schema schema, Configuration config) {
		super(schema, config);
		initBuffers();
	}

	@Override
	protected void initFromConfig() {
		super.initFromConfig();
		documentName = config.getSourceDocumentName();
		outputPath = Configuration.getInstancePathPrefix();
		template = config.getSourceInstanceFile();
		toxGen = new ToXGeneWrapper("./lib");
	}

	public void initBuffers() {
		toxLists = new ArrayList<StringBuffer>();
		toxTypes = new ArrayList<StringBuffer>();
		documentBuffer = new StringBuffer();
		templateBuffer = new StringBuffer();
		coveredAtomicElements = new ArrayList<SMarkElement>();
		documentName = config.getSourceDocumentName();
	}

	@Override
	public void generateData() throws Exception {
		generateToxTemplate();
		generateInstanceXML();
	}

	protected void generateInstanceXML() throws Exception {
		instanceXMLFile =
				toxGen.generate(new File(outputPath, template), outputPath);
		log.debug("created XML file " + instanceXMLFile + " in folder " + outputPath);
	}

	protected void generateToxTemplate() throws IOException {
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

		for (StringBuffer listBuf : toxLists)
			templateBuffer.append(listBuf);

		templateBuffer.append(documentBuffer);

		generateTemplateClosing(templateBuffer);

		// System.out.println(templateBuffer);
		File instFile = new File(
				Configuration.getInstancePathPrefix(),
				config.getSourceInstanceFile());
		BufferedWriter bufWriter =
				new BufferedWriter(new FileWriter(new File(
						Configuration.getInstancePathPrefix(),
						config.getSourceInstanceFile())));
		bufWriter.write(templateBuffer.toString());
		bufWriter.close();
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
			visitSchemaElement(SMarkElement schemaElement, StringBuffer buf) {
		String label = schemaElement.getLabel();
		Type type = schemaElement.getType();

		if (type instanceof Set) {
			generateComplexElementOpening(label, buf, repElemCount);
			for (int i = 0; i < schemaElement.size(); i++)
				visitSchemaElement(
						(SMarkElement) schemaElement.getSubElement(i), buf);
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
					generateAtomicElementConstruct(label, buf, atomicType);
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
		for (SMarkElement[][] constraint : constraints)
			for (int i = 0; i < constraint.length; i++)
				if (constraint[i][0] == schemaElement)
					return constraint;
		return null;
	}

	private void generateToxList(SMarkElement schemaElement, int eltCount) {
		StringBuffer listBuf = new StringBuffer();

		String label = schemaElement.getLabel();
		Type type = schemaElement.getType();

		generateListOpening(label, listBuf, eltCount);

		for (int i = 0; i < schemaElement.size(); i++)
			visitSchemaElement((SMarkElement) schemaElement.getSubElement(i),
					listBuf);

		generateListClosing(listBuf);

		toxLists.add(listBuf);

	}

	private void generateListOpening(String eltName, StringBuffer buf,
			int eltCount) {
		buf.append("<tox-list name=\"" + eltName + LIST_NAME_SUFFIX + "\">\n");
		generateComplexElementOpening(eltName, buf, eltCount);
	}

	private void generateListClosing(StringBuffer buf) {
		generateComplexElementClosing(buf);
		buf.append("</tox-list>\n");
	}

	private void generateComplexElementOpening(String eltName,
			StringBuffer buf, int eltCount) {
		buf.append("<element name=\"" + eltName + "\" minOccurs=\"" + eltCount
				+ "\" maxOccurs=\"" + eltCount + "\">\n");
		buf.append("<complexType>\n");
	}

	private void generateComplexElementClosing(StringBuffer buf) {
		buf.append("</complexType>\n");
		buf.append("</element>\n");
	}

	private void generateAtomicElementConstruct(String eltName,
			StringBuffer buf, Atomic atomicType) {
		String typeString =
				(atomicType == Atomic.STRING) ? "bench_string" : "bench_int";
		buf.append("<element name=\"" + eltName + "\" type=\"" + typeString
				+ "\"/>\n");

	}

	private void
			generateListIterationConstruct(String eltName, StringBuffer buf) {
		buf.append("<tox-foreach path=\"[" + eltName + LIST_NAME_SUFFIX + "/"
				+ eltName + "]\">\n");
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
		buf.append("<simpleType name=\"bench_string\">\n");
		buf.append("<restriction base=\"string\">\n");
		// buf.append("<tox-string type=\"xmrk_text\" maxLength=\""+maxStringLength+"\"/>\n");
		buf.append("<tox-string type=\"word\" maxLength=\"" + maxStringLength
				+ "\"/>\n");
		buf.append("</restriction>\n");
		buf.append("</simpleType>\n");

	}

	private void generateToxSampleConstruct(SMarkElement[][] constraint,
			StringBuffer buf) {
		// we get the set of the target and generate the tox-sample header
		generateToxSampleOpening((SMarkElement) constraint[0][1].getParent(),
				buf);

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
