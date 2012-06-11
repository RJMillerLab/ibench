package tresc.benchmark.test.trampxml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.LoggerUtil;
import org.vagabond.util.PropertyWrapper;

import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.STBenchmark;

public class TestLoadingCreatedModel {

	static Logger log = Logger.getLogger(TestLoadingCreatedModel.class);
	
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
	public void testCopy () throws Exception {
		testSingleBasicScenarios(ScenarioName.COPY);
	}
	
	@Test
	public void testFusion () throws Exception {
		testSingleBasicScenarios(ScenarioName.FUSION);
	}
	
	@Test
	public void testHP () throws Exception {
		testSingleBasicScenarios(ScenarioName.HORIZPARTITION);
	}
	
	@Test
	public void testMerging () throws Exception {
		testSingleBasicScenarios(ScenarioName.MERGING);
	}
	
	@Test
	public void testSJ () throws Exception {
		testSingleBasicScenarios(ScenarioName.SELFJOINS);
	}
	
	@Test
	public void testSKey () throws Exception {
		testSingleBasicScenarios(ScenarioName.SURROGATEKEY);
	}
	

	@Test
	public void testValgen () throws Exception {
		testSingleBasicScenarios(ScenarioName.VALUEGEN);
	}
	
	@Test
	public void testAtomValueMan () throws Exception {
		testSingleBasicScenarios(ScenarioName.VALUEMANAGEMENT);
	}
	
	@Test
	public void testVerticalPart () throws Exception {
		testSingleBasicScenarios(ScenarioName.VERTPARTITION);
	}
	
	@Test
	public void testAllBasicScenarios () throws Exception {
		for(ScenarioName n: Constants.ScenarioName.values())
			testSingleBasicScenarios(n);
	}
	
	private void testSingleBasicScenarios (ScenarioName n) throws Exception {
		log.info(n);
		conf.setScenarioRepetitions(n, 1);
		b.runConfig(conf);
		testLoad(n, false, false);
		conf.setScenarioRepetitions(n, 0);
	}

	
	
	private void testLoad(ScenarioName n, boolean toDB, boolean withData) throws Exception {
		try {
			MapScenarioHolder doc = ModelLoader.getInstance().load(new File(OUT_DIR,"test.xml"));
			if (toDB) {
				Connection dbCon = ConnectionManager.getInstance().getConnection(doc);
				if (withData)
					DatabaseScenarioLoader.getInstance().loadScenarioNoData(dbCon, doc);
				else
					DatabaseScenarioLoader.getInstance().loadScenario(dbCon, doc);
				dbCon.close();
			}
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
