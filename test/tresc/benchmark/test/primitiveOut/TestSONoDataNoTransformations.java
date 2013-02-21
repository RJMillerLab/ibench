package tresc.benchmark.test.primitiveOut;

import java.io.File;

import org.apache.log4j.Logger;

import tresc.benchmark.Constants.MappingLanguageType;

public class TestSONoDataNoTransformations extends AbstractCheckExpectedOutputTester {

	static Logger log = Logger.getLogger(TestSONoDataNoTransformations.class);
	
	@Override
	public void setPaths() {
		expectedPath = new File("testresource/expected/SONoDataNoTransformation");
		confName = "primConf.txt";
	}

	@Override
	public void adaptConfiguration() {
		conf.setMapType(MappingLanguageType.SOtgds);
	}

	
	
}
