package tresc.benchmark.test.trampxml;

import java.io.File;

import org.apache.log4j.Logger;
import org.apache.log4j.PropertyConfigurator;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import tresc.benchmark.Configuration;
import tresc.benchmark.Constants.JoinKind;
import tresc.benchmark.Constants.ParameterName;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.STBenchmark;

public abstract class AbstractAllScenarioTester {

	static Logger log = Logger.getLogger(AbstractAllScenarioTester.class);
	
	protected static String OUT_DIR="./testout";
	protected static ScenarioName[] scens = AllTrampXMLTests.workingScen;
	
	protected STBenchmark b = new STBenchmark();
	public Configuration conf;
	
	@BeforeClass
	public static void setUp () {
		PropertyConfigurator.configure("testresource/log4jproperties.txt");
		File outDir = new File(OUT_DIR);
		if (!outDir.exists())
			outDir.mkdir();
	}
	
	@AfterClass
	public static void tearDown () {
		/*File outDir = new File(OUT_DIR);
		if (outDir.exists()) {
			for(File child: outDir.listFiles()) {
				child.delete();
			}
			outDir.delete();
		}*/
	}

	public abstract void setUpConf() throws Exception;
	
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
		testSingleBasicScenario(ScenarioName.MERGEADD);
	}
	
	@Test
	public void testSJ () throws Exception {
		testSingleBasicScenario(ScenarioName.SELFJOINS);
	}
	
	@Test
	public void testSKey () throws Exception {
		testSingleBasicScenario(ScenarioName.SURROGATEKEY);
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
		testSingleBasicScenario(ScenarioName.VERTPARTITION);
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
		conf.setParam(ParameterName.NumOfNewAttributes, 3);
		conf.setParam(ParameterName.SkolemKind, 0);
		testSingleBasicScenario(ScenarioName.ADDATTRIBUTE);
		conf.setParam(ParameterName.SkolemKind, 1);
		testSingleBasicScenario(ScenarioName.ADDATTRIBUTE);
		conf.setParam(ParameterName.SkolemKind, 2);
		testSingleBasicScenario(ScenarioName.ADDATTRIBUTE);
	}
	
	@Test
	public void testAddDelAttr () throws Exception {
		conf.setParam(ParameterName.NumOfNewAttributes, 3);
		conf.setParam(ParameterName.SkolemKind, 0);
		testSingleBasicScenario(ScenarioName.ADDDELATTRIBUTE);
		conf.setParam(ParameterName.SkolemKind, 1);
		testSingleBasicScenario(ScenarioName.ADDDELATTRIBUTE);
		conf.setParam(ParameterName.SkolemKind, 2);
		testSingleBasicScenario(ScenarioName.ADDDELATTRIBUTE);
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
		testSingleBasicScenario(ScenarioName.MERGEADD);
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
		testSingleBasicScenario(ScenarioName.VERTPARTITION);
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
		conf.setParam(ParameterName.NumOfNewAttributes, 3);
		conf.setParam(ParameterName.SkolemKind, 0);
		testSingleBasicScenario(ScenarioName.ADDATTRIBUTE);
		conf.setParam(ParameterName.SkolemKind, 1);
		testSingleBasicScenario(ScenarioName.ADDATTRIBUTE);
		conf.setParam(ParameterName.SkolemKind, 2);
		testSingleBasicScenario(ScenarioName.ADDATTRIBUTE);
	}
	
	@Test
	public void testChainAddDelAttr () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		conf.setParam(ParameterName.NumOfNewAttributes, 3);
		conf.setParam(ParameterName.SkolemKind, 0);
		testSingleBasicScenario(ScenarioName.ADDDELATTRIBUTE);
		conf.setParam(ParameterName.SkolemKind, 1);
		testSingleBasicScenario(ScenarioName.ADDDELATTRIBUTE);
		conf.setParam(ParameterName.SkolemKind, 2);
		testSingleBasicScenario(ScenarioName.ADDDELATTRIBUTE);
	}
	
	@Test
	public void testChainDelAttr () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		testSingleBasicScenario(ScenarioName.DELATTRIBUTE);
	}

	
	@Test
	public void testAllBasicScenariosWithStartJoin () throws Exception {
		for(ScenarioName n: scens)
			testSingleBasicScenario(n);
	}

	@Test
	public void testAllBasicScenariosWithChainJoin () throws Exception {
		conf.setParam(ParameterName.JoinKind, JoinKind.CHAIN.ordinal());
		for(ScenarioName n: scens)
			testSingleBasicScenario(n);
	}
	
	public abstract void testSingleBasicScenario (ScenarioName n) throws Exception;
	
}
