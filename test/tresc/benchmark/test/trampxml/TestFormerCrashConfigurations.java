package tresc.benchmark.test.trampxml;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.mapping.model.ModelLoader;
import org.vagabond.util.LoggerUtil;
import org.vagabond.util.PropertyWrapper;

import tresc.benchmark.Configuration;
import tresc.benchmark.STBenchmark;
import tresc.benchmark.Constants.ScenarioName;

public class TestFormerCrashConfigurations {

	static Logger log = Logger.getLogger(TestCreationReusingSchemas.class);
	
	private STBenchmark b = new STBenchmark();
	private Configuration conf;
	private static final String OUT_DIR = "./testout";

	
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
	
	public void setUpConf (String fileName) throws FileNotFoundException, IOException {
		PropertyWrapper prop = new PropertyWrapper("testresource/" + fileName);
		conf = new Configuration();
		conf.readFromProperties(prop);
		conf.setInstancePathPrefix(OUT_DIR);
		conf.setSchemaPathPrefix(OUT_DIR);
	}
	
	@Test
	public void testReuseCrash () throws Exception {
		setUpConf("reuseCrashConf.txt");
		b.runConfig(conf);
		testLoad("reuse");
	}
	
	private void testLoad(String name) throws Exception {
		try {
			MapScenarioHolder doc = ModelLoader.getInstance().load(new File(OUT_DIR,"test.xml"));
			log.debug(doc.getScenario().toString());
		}
		catch (Exception e) {
			log.error(name + "\n\n" + loadToString());
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
