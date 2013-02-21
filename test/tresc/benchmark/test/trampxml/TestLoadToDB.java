package tresc.benchmark.test.trampxml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.LoggerUtil;
import org.vagabond.util.PropertyWrapper;

import tresc.benchmark.Configuration;
import tresc.benchmark.Constants.ScenarioName;

public class TestLoadToDB extends AbstractAllScenarioTester {

	static Logger log = Logger.getLogger(TestLoadToDB.class);
	
	
	@Before
	public void setUpConf () throws FileNotFoundException, IOException {
		PropertyWrapper prop = new PropertyWrapper("testresource/defconf.txt");
		conf = new Configuration();
		conf.readFromProperties(prop);
		conf.setInstancePathPrefix(OUT_DIR);
		conf.setSchemaPathPrefix(OUT_DIR);
	}

	@Override
	public void testSingleBasicScenario(ScenarioName n) throws Exception {
		log.info(n);
		conf.setScenarioRepetitions(n, 1);
		conf.resetRandomGenerator();
		b.runConfig(conf);
		testLoad(n, true, false);
		conf.setScenarioRepetitions(n, 0);
	}

	private void testLoad(ScenarioName n, boolean toDB, boolean withData) throws Exception {
		try {
			MapScenarioHolder doc = ModelLoader.getInstance().load(new File(OUT_DIR,"test.xml"));
			Connection dbCon = ConnectionManager.getInstance().getConnection(doc);
			DatabaseScenarioLoader.getInstance().loadScenarioNoData(dbCon, doc);
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
