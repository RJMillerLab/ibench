/**
 * 
 */
package tresc.benchmark.rename;

/**
 * @author lord_pretzel
 *
 */
public interface IRenamer {

	public String renameSource(String tableName, String oldName, String targetName);
	public String renameTarget(String tableName, String oldSourceName, String oldName);
	
}
