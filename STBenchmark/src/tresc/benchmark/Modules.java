package tresc.benchmark;

import tresc.benchmark.data.NameFactory;
import tresc.benchmark.data.NamingPolicy;
import vtools.utils.structures.EqClassManager;

public class Modules {
	public static final NameFactory nameFactory = new NameFactory();

	public static Generator scenarioGenerator;

	public static final NamingPolicy namingPolicy = new NamingPolicy();

	public static final EqClassManager eqClassManager = new EqClassManager(
			EqClassManager.ABSCHECK);

}
