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
		nameSelector = new DummyRenamer();
	}
	
	public void applyRenaming (MappingScenario scen, Configuration conf) {
		SchemaType source = scen.getDoc().getSchema(true);
		
		RelationType[] sRels = source.getRelationArray();
		
		for(RelationType r: sRels) {
			String relName = r.getName();
			for(AttrDefType a: r.getAttrArray()) {
				String oldName = a.getName();
				String newName = nameSelector.renameSource(relName, oldName, null);
				a.setName(newName);
			}
		}
		
//		scen.
		
	}
	
}
