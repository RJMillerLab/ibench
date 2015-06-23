/**
 * 
 */
package tresc.benchmark.rename;

import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SchemaType;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;

/**
 * @author lord_pretzel
 *
 */
public class RenameDriver {

	IRenamer nameSelector;
	
	public RenameDriver () {
		
	}
	
	public void applyRenaming (MappingScenario scen, Configuration conf) {
		SchemaType source = scen.getDoc().getSchema(true);
		
		RelationType[] sRels = source.getRelationArray();
		AttrDefType a = sRels[0].getAttrArray()[0];
		String targetName = null;
		a.setName(nameSelector.renameSource(a.getName(), targetName));

//		scen.
		
	}
	
}
