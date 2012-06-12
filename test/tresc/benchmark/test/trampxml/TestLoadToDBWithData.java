package tresc.benchmark.test.trampxml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.apache.xmlbeans.XmlException;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.mapping.model.ValidationException;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.LoggerUtil;
import org.vagabond.util.PropertyWrapper;

import tresc.benchmark.Configuration;
import tresc.benchmark.Constants.JoinKind;
import tresc.benchmark.Constants.ParameterName;
import tresc.benchmark.STBenchmark;
import tresc.benchmark.Constants.ScenarioName;

public class TestLoadToDBWithData {

	static Logger log = Logger.getLogger(TestLoadToDBWithData.class);
	
	private STBenchmark b = new STBenchmark();
	private Configuration conf;
	private static final String OUT_DIR = "./testout";
	
	private static final ScenarioName[] scens = ScenarioName.values();
	
	@BeforeClass
	public static void setUp () {
		PropertyConfigurator.configure("testresource/log4jproperties.txt");
		File outDir = new File(OUT_DIR);
		if (!outDir.exists())
			outDir.mkdir();
	}
	
	@AfterClass
	public static void tearDown () {
		File outDir = new File(OUT_DIR);
//		if (outDir.exists()) {
//			for(File child: outDir.listFiles()) {
//				child.delete();
//			}
//			outDir.delete();
//		}
	}
	
	@Before
	public void setUpConf () throws FileNotFoundException, IOException {
		PropertyWrapper prop = new PropertyWrapper("testresource/defconf.txt");
		conf = new Configuration();
		conf.readFromProperties(prop);
		conf.setInstancePathPrefix(OUT_DIR);
		conf.setSchemaPathPrefix(OUT_DIR);
	}

	
	@Test
	public void testDBWithDataCopy () throws Exception {
		testSingleScenarioLoadToDBWithData(ScenarioName.COPY);
	}
	
	@Test
	public void testDBWithDataFusion () throws Exception {
		testSingleScenarioLoadToDBWithData(ScenarioName.FUSION);
	}
	
	@Test
	public void testDBWithDataHP () throws Exception {
		testSingleScenarioLoadToDBWithData(ScenarioName.HORIZPARTITION);
	}
	
	@Test
	public void testDBWithDataMerging () throws Exception {
		testSingleScenarioLoadToDBWithData(ScenarioName.MERGING);
	}
	
	@Test
	public void testDBWithDataSJ () throws Exception {
		testSingleScenarioLoadToDBWithData(ScenarioName.SELFJOINS);
	}
	
	@Test
	public void testDBWithDataSKey () throws Exception {
		testSingleScenarioLoadToDBWithData(ScenarioName.SURROGATEKEY);
	}
	

	@Test
	public void testDBWithDataValgen () throws Exception {
		testSingleScenarioLoadToDBWithData(ScenarioName.VALUEGEN);
	}
	
	@Test
	public void testDBWithDataAtomValueMan () throws Exception {
		testSingleScenarioLoadToDBWithData(ScenarioName.VALUEMANAGEMENT);
	}
	
	@Test
	public void testDBWithDataVerticalPart () throws Exception {
		testSingleScenarioLoadToDBWithData(ScenarioName.VERTPARTITION);
	}
	
	
	@Test
	public void testLoadEachBasicScenariosWithData () throws Exception {
		for(ScenarioName n: scens)
			testSingleScenarioLoadToDBWithData(n);
	}
	
	@Test
	public void testLoadEachBasicScenariosWithDataStarJoin () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		for(ScenarioName n: scens)
			testSingleScenarioLoadToDBWithData(n);
	}

	@Test
	public void testLoadAllBasicScenariosWithData () throws Exception {
		for(ScenarioName n: scens)
			conf.setScenarioRepetitions(n, 3);
		b.runConfig(conf);
		MapScenarioHolder doc = ModelLoader.getInstance().load(new File(OUT_DIR,"test.xml"));
		log.info(doc.getScenario().toString());
		Connection dbCon = ConnectionManager.getInstance().getConnection(doc);
		DatabaseScenarioLoader.getInstance().loadScenario(dbCon, doc);
		dbCon.close();
	}
	
	private void testSingleScenarioLoadToDBWithData(ScenarioName n)
			throws Exception {
		log.info(n);
		conf.setScenarioRepetitions(n, 1);
		b.runConfig(conf);
		testLoad(n);
		conf.setScenarioRepetitions(n, 0);
	}


	private void testLoad(ScenarioName n) throws Exception {
		try {
			MapScenarioHolder doc = ModelLoader.getInstance().load(new File(OUT_DIR,"test.xml"));
			Connection dbCon = ConnectionManager.getInstance().getConnection(doc);
			DatabaseScenarioLoader.getInstance().loadScenario(dbCon, doc);
			dbCon.close();			
		}
		catch (Exception e) {
			log.error(n + "\n\n" + loadToString());
			LoggerUtil.logException(e, log);
			throw e;	
		}
	}
	
	private String loadToString () throws IOException {
		StringBuffer result = new StringBuffer();
		BufferedReader in = new BufferedReader(new FileReader(new File(OUT_DIR, "test.xml")));
		
		while(in.ready()) {
			result.append(in.readLine() + "\n");
		}
		
		return result.toString();
	}

	
}
