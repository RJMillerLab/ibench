/**
 * 
 */
package tresc.benchmark.rename;


/**
 * @author lord_pretzel
 *
 */
public interface IRenamer {

	public String renameSource (String oldName, String targetName);
	public String renameTarget (String oldSourceName, String oldName);
	
}
