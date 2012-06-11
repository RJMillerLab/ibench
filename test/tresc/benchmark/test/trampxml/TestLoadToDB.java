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
import tresc.benchmark.STBenchmark;
import tresc.benchmark.Constants.ScenarioName;

public class TestLoadToDB {

	static Logger log = Logger.getLogger(TestLoadToDB.class);
	
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
	public void testDBCopy () throws Exception {
		testSingleScenarioLoadToDB(ScenarioName.COPY);
	}
	
	@Test
	public void testDBFusion () throws Exception {
		testSingleScenarioLoadToDB(ScenarioName.FUSION);
	}
	
	@Test
	public void testDBHP () throws Exception {
		testSingleScenarioLoadToDB(ScenarioName.HORIZPARTITION);
	}
	
	@Test
	public void testDBMerging () throws Exception {
		testSingleScenarioLoadToDB(ScenarioName.MERGING);
	}
	
	@Test
	public void testDBSJ () throws Exception {
		testSingleScenarioLoadToDB(ScenarioName.SELFJOINS);
	}
	
	@Test
	public void testDBSKey () throws Exception {
		testSingleScenarioLoadToDB(ScenarioName.SURROGATEKEY);
	}
	

	@Test
	public void testDBValgen () throws Exception {
		testSingleScenarioLoadToDB(ScenarioName.VALUEGEN);
	}
	
	@Test
	public void testDBAtomValueMan () throws Exception {
		testSingleScenarioLoadToDB(ScenarioName.VALUEMANAGEMENT);
	}
	
	@Test
	public void testDBVerticalPart () throws Exception {
		testSingleScenarioLoadToDB(ScenarioName.VERTPARTITION);
	}
	
	
	@Test
	public void testLoadSingleScenariosToDB () throws Exception {
		for(ScenarioName n: scens)
			testSingleScenarioLoadToDB(n);
	}

	private void testSingleScenarioLoadToDB(ScenarioName n) throws Exception {
		log.info(n);
		conf.setScenarioRepetitions(n, 1);
		b.runConfig(conf);
		testLoad(n, true, false);
		conf.setScenarioRepetitions(n, 0);
	}

	private void testLoad(ScenarioName n, boolean toDB, boolean withData) throws Exception {
		try {
			MapScenarioHolder doc = ModelLoader.getInstance().load(new File(OUT_DIR,"test.xml"));
			if (toDB) {
				Connection dbCon = ConnectionManager.getInstance().getConnection(doc);
				if (!withData)
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
