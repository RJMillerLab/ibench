/**
 * 
 */
package tresc.benchmark.rename;

/**
 * @author lord_pretzel
 *
 */
public class DummyRenamer implements IRenamer {

	/* (non-Javadoc)
	 * @see tresc.benchmark.rename.IRenamer#renameSource(java.lang.String, java.lang.String)
	 */
	@Override
	public String renameSource(String tableName, String oldName, String targetName) {
		// TODO Auto-generated method stub
		return "XXX";
	}

	/* (non-Javadoc)
	 * @see tresc.benchmark.rename.IRenamer#renameTarget(java.lang.String, java.lang.String)
	 */
	@Override
	public String renameTarget(String tableName, String oldSourceName, String oldName) {
		// TODO Auto-generated method stub
		return "XXX";
	}

}
