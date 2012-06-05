package tresc.benchmark.dataGen;

import java.io.File;

import org.apache.log4j.Logger;

import tresc.benchmark.Configuration;
import tresc.benchmark.dataGen.toxgenewrap.ToXGeneWrapper;
import vtools.dataModel.schema.Schema;

public class ToXDataGenerator extends ToXScriptOnlyDataGenerator {
	
	static Logger log = Logger.getLogger(ToXDataGenerator.class);

	// static final int STRING_LENGTH=25;
	// static final int INT_DOMAIN=10000;

	ToXGeneWrapper toxGen;

	public ToXDataGenerator(Configuration config) {
		super(config);
	}

	public ToXDataGenerator(Schema schema, Configuration config) {
		super(schema, config);
	}

	@Override
	protected void initFromConfig() {
		super.initFromConfig();
		toxGen = new ToXGeneWrapper("./lib");
	}

	@Override
	public void generateData() throws Exception {
		super.generateData();
		generateInstanceXML();
	}

	protected void generateInstanceXML() throws Exception {
		instanceXMLFile =
				toxGen.generate(new File(outputPath, template), outputPath);
		log.debug("created XML file " + instanceXMLFile + " in folder " + outputPath);
	}

	public StringBuffer getDataBuffer() {
		return templateBuffer;
	}

}
