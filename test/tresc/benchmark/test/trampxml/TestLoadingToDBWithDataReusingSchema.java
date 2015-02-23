package tresc.benchmark.test.trampxml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.mapping.scenarioToDB.DatabaseScenarioLoader;
import org.vagabond.util.ConnectionManager;
import org.vagabond.util.LoggerUtil;
import org.vagabond.util.PropertyWrapper;

import tresc.benchmark.Configuration;
import tresc.benchmark.Constants.ParameterName;
import tresc.benchmark.Constants.ScenarioName;

public class TestLoadingToDBWithDataReusingSchema extends AbstractAllScenarioTester {


	static Logger log = Logger.getLogger(TestLoadingToDBWithDataReusingSchema.class);
	
	@Before
	public void setUpConf () throws Exception {
		PropertyWrapper prop = new PropertyWrapper("testresource/reuseDBconf.txt");
		conf = new Configuration();
		conf.readFromProperties(prop);
		conf.setInstancePathPrefix(OUT_DIR);
		conf.setSchemaPathPrefix(OUT_DIR);
	}

	@Test
	public void testLoadAllBasicScenariosWithData () throws Exception {
		for(ScenarioName n: scens)
			conf.setScenarioRepetitions(n, 1);
		b.runConfig(conf);
		MapScenarioHolder doc = ModelLoader.getInstance().load(new File(OUT_DIR,"test.xml"));
		log.info(doc.getScenario().toString());
		Connection dbCon = ConnectionManager.getInstance().getConnection(doc);
		DatabaseScenarioLoader.getInstance().loadScenario(dbCon, doc);
		dbCon.close();
	}
	
	@Override
	public void testSingleBasicScenario(ScenarioName n)
			throws Exception {
		log.info(n);
		conf.setScenarioRepetitions(n, 2);
		// reuse source
		conf.resetRandomGenerator();
		setReuse(100,0,conf);
		b.runConfig(conf);
		testLoad(n);
		// reuse target
		conf.resetRandomGenerator();
		setReuse(0,100,conf);
		b.runConfig(conf);
		testLoad(n);
		conf.setScenarioRepetitions(n, 0);
	}

	private void setReuse(int src, int target, Configuration conf) {
		conf.setParam(ParameterName.ReuseSourcePerc, src);
		conf.setParam(ParameterName.ReuseTargetPerc, target);
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
		
		in.close();
		
		return result.toString();
	}

	
}
