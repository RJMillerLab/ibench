package tresc.benchmark.schemaGen;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;

public interface ScenarioGenerator {

	
	public void generateScenario(MappingScenario scenario,
			Configuration configuration) throws Exception;
}
