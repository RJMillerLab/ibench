package tresc.benchmark.dataGen;

import java.util.Vector;

import smark.support.MappingScenario;
import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import vtools.dataModel.schema.Schema;

public abstract class DataGenerator 
{
	Schema schema;
	Vector<SMarkElement[][]> constraints;
	MappingScenario scen;
	Configuration config;
	// how many appearances for a repeatable element
	int repElemCount;
	// max length for string values
	int maxStringLength;
	// max for numeric values
	int maxNumValue;
	
	public DataGenerator(Configuration config)
	{
		this.config = config;
		this.scen = scen;
		initFromConfig();
	}
	
	protected void initFromConfig() {
		repElemCount = config.getRepElemCount();
		maxStringLength = config.getMaxStringLength();
		maxNumValue = config.getMaxNumValue();
	}

	public DataGenerator(Schema __schema, Configuration config)
	{
		this(config);
		schema=__schema;
	}
	
	public void setScenario (MappingScenario scen) {
		this.scen = scen;
	}
	
	public void setSchema(Schema __schema)
	{
		schema=__schema;
	}
	
	public void setConstraints(Vector<SMarkElement[][]> __constraints)
	{
		constraints=__constraints;
	}
	
	public abstract void generateData() throws Exception;
	public abstract StringBuffer getDataBuffer();
	
}
