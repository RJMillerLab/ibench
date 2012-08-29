package tresc.benchmark.test.trampxml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	TestLoadingCreatedModel.class,
	TestLoadToDB.class,
	TestLoadToDBWithData.class,
	TestCreatingPartOfTheModelWithSO.class,
	TestCreationReusingSchemas.class,
	TestLoadingToDBWithDataReusingSchema.class,
	TestFormerCrashConfigurations.class
})
public class AllTrampXMLTests {

}
