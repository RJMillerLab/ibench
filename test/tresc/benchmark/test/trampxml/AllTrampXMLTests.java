package tresc.benchmark.test.trampxml;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	TestLoadingCreatedModel.class,
	TestLoadToDB.class,
	TestLoadToDBWithData.class
})
public class AllTrampXMLTests {

}
