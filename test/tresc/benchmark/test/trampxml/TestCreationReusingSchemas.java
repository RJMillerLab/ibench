package tresc.benchmark.test.trampxml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.util.LoggerUtil;
import org.vagabond.util.PropertyWrapper;

import tresc.benchmark.Configuration;
import tresc.benchmark.Constants.MappingLanguageType;
import tresc.benchmark.Constants.ParameterName;
import tresc.benchmark.Constants.ScenarioName;

public class TestCreationReusingSchemas extends AbstractAllScenarioTester {
	
	static Logger log = Logger.getLogger(TestCreationReusingSchemas.class);
	
	@Before
	public void setUpConf () throws Exception {
		PropertyWrapper prop = new PropertyWrapper("testresource/reuseconf.txt");
		conf = new Configuration();
		conf.readFromProperties(prop);
		conf.setInstancePathPrefix(OUT_DIR);
		conf.setSchemaPathPrefix(OUT_DIR);
		conf.setMapType(MappingLanguageType.FOtgds);
	}
	
	@Override
	public void testSingleBasicScenario (ScenarioName n) throws Exception {
		log.info(n);
		conf.setScenarioRepetitions(n, 2); // 2 so we can reuse
		// reuse source
		setReuse(100,0,conf);
		conf.resetRandomGenerator();
		b.runConfig(conf);
		testLoad(n, "source");
		// reuse target
		setReuse(0,100,conf);
		conf.resetRandomGenerator();
		b.runConfig(conf);
		testLoad(n, "target");
		if (!n.equals(ScenarioName.COPY))
			conf.setScenarioRepetitions(n, 0);
	}

	private void setReuse(int src, int target, Configuration conf) {
		conf.setParam(ParameterName.ReuseSourcePerc, src);
		conf.setParam(ParameterName.ReuseTargetPerc, target);
	}
	
	
	private void testLoad(ScenarioName n, String reuse) throws Exception {
		try {
			MapScenarioHolder doc = ModelLoader.getInstance().load(new File(OUT_DIR,"test.xml"));
			if (log.isDebugEnabled()) {log.debug(doc.getScenario().toString());};
		}
		catch (Exception e) {
			log.error(n + " " + reuse + "\n\n" + loadToString());
			LoggerUtil.logException(e, log);
			throw new Exception (n + " " + reuse, e);	
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
