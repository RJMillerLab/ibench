package tresc.benchmark.test.trampxml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import tresc.benchmark.Constants.ScenarioName;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	TestCreatingPartOfTheModelWithSO.class,
	TestCreatingPartOfTheModelWithSOWithSourceSkolems.class,
	
	TestLoadingCreatedModel.class,
	TestLoadToDB.class,
	TestLoadToDBWithData.class,
	
	TestCreationReusingSchemas.class,
	TestLoadingToDBWithDataReusingSchema.class,
	
	TestFormerCrashConfigurations.class
})
public class AllTrampXMLTests {

	public static ScenarioName[] workingScen = new ScenarioName[] {
			ScenarioName.ADDATTRIBUTE,
			ScenarioName.ADDDELATTRIBUTE,
			ScenarioName.COPY,
			ScenarioName.DELATTRIBUTE,
			ScenarioName.FUSION,
			ScenarioName.HORIZPARTITION,
			ScenarioName.MERGEADD,
			ScenarioName.MERGING,
			ScenarioName.SELFJOINS,
			ScenarioName.SURROGATEKEY,
			ScenarioName.VALUEGEN,
			ScenarioName.VALUEMANAGEMENT,
			ScenarioName.VERTPARTITION,
			ScenarioName.VERTPARTITIONHASA,
			ScenarioName.VERTPARTITIONISA,
			ScenarioName.VERTPARTITIONNTOM
	};
	
}

