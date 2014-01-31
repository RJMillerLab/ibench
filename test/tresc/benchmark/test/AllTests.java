package tresc.benchmark.test;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

import tresc.benchmark.test.toxgene.TestToxGene;
import tresc.benchmark.test.trampxml.AllTrampXMLTests;

@RunWith(Suite.class)
@Suite.SuiteClasses({
	TestToxGene.class,
	AllTrampXMLTests.class
})
public class AllTests {

}
