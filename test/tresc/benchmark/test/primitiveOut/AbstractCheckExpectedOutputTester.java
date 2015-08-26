package tresc.benchmark.test.primitiveOut;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.StringWriter;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.log4j.PropertyConfigurator;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.util.PropertyWrapper;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import tresc.benchmark.Configuration;
import tresc.benchmark.Constants.JoinKind;
import tresc.benchmark.Constants.ParameterName;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.iBench;



public abstract class AbstractCheckExpectedOutputTester {

	public static String OUT_DIR = "./testout"; 
	
	protected String confName;
	protected Configuration conf; 
	protected File expectedPath;
	protected iBench b;
	
	@BeforeClass
	public static void setUpLogger() {
		PropertyConfigurator.configure("testresource/log4jproperties.txt");
	}
	
	@Before
	public void readConf () throws Exception {
		b = new iBench();
		setPaths();
		PropertyWrapper prop = new PropertyWrapper("testresource/" + confName);
		conf = new Configuration();
		conf.readFromProperties(prop);
		conf.setInstancePathPrefix(OUT_DIR);
		conf.setSchemaPathPrefix(OUT_DIR);
		adaptConfiguration();
	}
	
	public abstract void setPaths ();
	public abstract void adaptConfiguration();
	
	public void testAllSkolemModes (ScenarioName n) throws Exception {
		conf.setParam(ParameterName.SkolemKind, 0);
		testSingleBasicScenario(n, "_Key");
		
		conf.setParam(ParameterName.SkolemKind, 1);
		testSingleBasicScenario(n, "_All");
		
		conf.setParam(ParameterName.SkolemKind, 2);
		testSingleBasicScenario(n, "_Random");
	}
	
	public void testSingleBasicScenario (ScenarioName n) throws Exception {
		testSingleBasicScenario(n,"");
	}
	
	public void testSingleBasicScenario (ScenarioName n, String suffix) throws Exception {
		String outFileName = n.toString(); 
		if (conf.getParam(ParameterName.JoinKind) == JoinKind.CHAIN.ordinal())
			outFileName += "_Chain";
		outFileName += suffix;
		conf.setSchemaFile(outFileName + ".xml");
		conf.setScenarioRepetitions(n, 1);
		conf.resetRandomGenerator();
		b.runConfig(conf);
		conf.setScenarioRepetitions(n, 0);
		compareFile(outFileName, outFileName);
	}
	
	public void compareFile(String fileLeft, String fileRight) throws SAXException, IOException, ParserConfigurationException, TransformerException {
		DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
		dbf.setNamespaceAware(true);
		dbf.setCoalescing(true);
		dbf.setIgnoringElementContentWhitespace(true);
		dbf.setIgnoringComments(true);

		DocumentBuilder db = dbf.newDocumentBuilder();

		Document doc1 = db.parse(new File(expectedPath + "/" + fileRight + ".xml"));
		doc1.normalizeDocument();

		Document doc2 = db.parse(new File(OUT_DIR + "/" + fileLeft + ".xml"));
		doc2.normalizeDocument();

		assertEquals("Output <" + fileLeft + "> is not the same as expected output <" +
				fileRight + ">", 
				docToString(doc1), 
				docToString(doc2));
	}
	
	public String docToString(final Document doc) throws TransformerException {
	    DOMSource domSource = new DOMSource(doc);
	    Transformer transformer = TransformerFactory.newInstance().newTransformer();
	    transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	    transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
	    transformer.setOutputProperty(OutputKeys.INDENT, "no");
	    StringWriter sw = new StringWriter();
	    StreamResult sr = new StreamResult(sw);
	    transformer.transform(domSource, sr);
	    
        return sw.toString();
    }
	
	@Test
	public void testCopy () throws Exception {
		testSingleBasicScenario(ScenarioName.COPY);
	}
	
	@Test
	public void testFusion () throws Exception {
		testSingleBasicScenario(ScenarioName.FUSION);
	}
	
	@Test
	public void testHP () throws Exception {
		testSingleBasicScenario(ScenarioName.HORIZPARTITION);
	}
	
	@Test
	public void testMerging () throws Exception {
		testSingleBasicScenario(ScenarioName.MERGING);
	}
	
	@Test
	public void testMergeAdd () throws Exception {
		testAllSkolemModes(ScenarioName.MERGEADD);
	}
	
	@Test
	public void testSJ () throws Exception {
		testSingleBasicScenario(ScenarioName.SELFJOINS);
	}
	
	@Test
	public void testSKey () throws Exception {
		testAllSkolemModes(ScenarioName.SURROGATEKEY);
	}
	

	@Test
	public void testValgen () throws Exception {
		testSingleBasicScenario(ScenarioName.VALUEGEN);
	}
	
	@Test
	public void testAtomValueMan () throws Exception {
		testSingleBasicScenario(ScenarioName.VALUEMANAGEMENT);
	}
	
	@Test
	public void testVerticalPart () throws Exception {
		testAllSkolemModes(ScenarioName.VERTPARTITION);
	}
	
	@Test
	public void testVerticalPartIsA () throws Exception {
		testSingleBasicScenario(ScenarioName.VERTPARTITIONISA);
	}
	
	@Test
	public void testVerticalPartHasA () throws Exception {
		testSingleBasicScenario(ScenarioName.VERTPARTITIONHASA);
	}
	
	@Test
	public void testVerticalPartNtoM () throws Exception {
		testSingleBasicScenario(ScenarioName.VERTPARTITIONNTOM);
	}
	
	@Test
	public void testAddAttr () throws Exception {
		testAllSkolemModes(ScenarioName.ADDATTRIBUTE);
	}
	
	@Test
	public void testAddDelAttr () throws Exception {
		testAllSkolemModes(ScenarioName.ADDDELATTRIBUTE);
	}
	
	@Test
	public void testDelAttr () throws Exception {
		testSingleBasicScenario(ScenarioName.DELATTRIBUTE);
	}
	
	@Test
	public void testChainCopy () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		testSingleBasicScenario(ScenarioName.COPY);
	}
	
	@Test
	public void testChainFusion () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		testSingleBasicScenario(ScenarioName.FUSION);
	}
	
	@Test
	public void testChainHP () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		testSingleBasicScenario(ScenarioName.HORIZPARTITION);
	}
	
	@Test
	public void testChainMerging () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		testSingleBasicScenario(ScenarioName.MERGING);
	}
	
	@Test
	public void testChainMergeAdd () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		testAllSkolemModes(ScenarioName.MERGEADD);
	}
	
	@Test
	public void testChainSJ () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		testSingleBasicScenario(ScenarioName.SELFJOINS);
	}
	
	@Test
	public void testChainSKey () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		testSingleBasicScenario(ScenarioName.SURROGATEKEY);
	}
	

	@Test
	public void testChainValgen () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		testSingleBasicScenario(ScenarioName.VALUEGEN);
	}
	
	@Test
	public void testChainAtomValueMan () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		testSingleBasicScenario(ScenarioName.VALUEMANAGEMENT);
	}
	
	@Test
	public void testChainVerticalPart () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		testAllSkolemModes(ScenarioName.VERTPARTITION);
	}
	
	@Test
	public void testChainVerticalPartIsA () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		testSingleBasicScenario(ScenarioName.VERTPARTITIONISA);
	}
	
	@Test
	public void testChainVerticalPartHasA () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		testSingleBasicScenario(ScenarioName.VERTPARTITIONHASA);
	}
	
	@Test
	public void testChainVerticalPartNtoM () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		testSingleBasicScenario(ScenarioName.VERTPARTITIONNTOM);
	}
	
	@Test
	public void testChainAddAttr () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		testAllSkolemModes(ScenarioName.ADDATTRIBUTE);
	}
	
	@Test
	public void testChainAddDelAttr () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		testAllSkolemModes(ScenarioName.ADDDELATTRIBUTE);
	}
	
	@Test
	public void testChainDelAttr () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		testSingleBasicScenario(ScenarioName.DELATTRIBUTE);
	}

	
}
