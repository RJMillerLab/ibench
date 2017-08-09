/**
 * 
 */
package tresc.benchmark.rename;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.log4j.Logger;
import org.vagabond.benchmark.model.TrampModelFactory;
import org.vagabond.benchmark.model.TrampXMLModel;
import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.FDType;
import org.vagabond.xmlmodel.ForeignKeyType;
import org.vagabond.xmlmodel.IDType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SchemaType;

import smark.support.MappingScenario;

/**
 * @author lord_pretzel
 *
 */
public class AllLowerCaseRenamer implements IRenameHandler {

	static Logger log = Logger.getLogger(AllLowerCaseRenamer.class);
	
	/* (non-Javadoc)
	 * @see tresc.benchmark.rename.IRenameHandler#applyRenaming(org.vagabond.benchmark.model.TrampXMLModel)
	 */
	@Override
	public void applyRenaming(MappingScenario m) {
		Map<String,String> rename = new HashMap<String,String> ();
		
		renameAttrInRels(m, rename);
		renameAttrInCorr(m, rename);
		renameAttrInQueries(m, rename);
	}

	/**
	 * Renames attributes in source and target relations
	 * @param m
	 */
	private void renameAttrInRels(MappingScenario m, Map<String,String> rename) {
		renameRels(m.getDoc().getSourceRels().values(), rename);
		renameRels(m.getDoc().getTargetRels().values(), rename);
		renameFKsAndFDs(m.getDoc().getDocument().getMappingScenario().getSchemas().getSourceSchema(), rename);
		renameFKsAndFDs(m.getDoc().getDocument().getMappingScenario().getSchemas().getTargetSchema(), rename);
	}

	/**
	 * @param schema
	 */
	private void renameFKsAndFDs(SchemaType schema, Map<String,String> rename) {
		String attrs[];
		for(ForeignKeyType f: schema.getForeignKeyArray()) {
			attrs = f.getFrom().getAttrArray();
			renameAttrsInArray(rename, attrs);
			f.getFrom().setAttrArray(attrs);
			
			attrs = f.getTo().getAttrArray();
			renameAttrsInArray(rename, attrs);
			f.getTo().setAttrArray(attrs);
		}
		
		for(FDType f: schema.getFDArray()) {
			attrs = f.getFrom().getAttrArray();
			renameAttrsInArray(rename, attrs);
			f.getFrom().setAttrArray(attrs);
			
			attrs = f.getTo().getAttrArray();
			renameAttrsInArray(rename, attrs);
			f.getTo().setAttrArray(attrs);	
		}
		
		for(IDType i: schema.getIDArray()) {
			attrs = i.getFrom().getAttrArray();
			renameAttrsInArray(rename, attrs);
			i.getFrom().setAttrArray(attrs);
			
			attrs = i.getTo().getAttrArray();
			renameAttrsInArray(rename, attrs);
			i.getTo().setAttrArray(attrs);
		}
	}

	private void renameRels(Collection<RelationType> rels, Map<String, String> rename) {
		for (RelationType r: rels) {
			for(AttrDefType a: r.getAttrArray()) {
				String origName = a.getName();
				if (!isAllLower(origName)) {
					String newName = origName.toLowerCase();
					rename.put(origName, newName);
					a.setName(newName);
					log.debug("rename attribute " + origName + " to " + newName);
				}
			}
			if (r.isSetPrimaryKey()) {
				String[] attrs = r.getPrimaryKey().getAttrArray();
				for(int i = 0; i < attrs.length; i++) {
					if (rename.containsKey(attrs[i])) 
						attrs[i] = rename.get(attrs[i]);
				}
				r.getPrimaryKey().setAttrArray(attrs);
			}
		}
	}

	private boolean isAllLower(String s) {
		for(char c: s.toCharArray()) {
			if (!Character.isLowerCase(c))
				return false;
		}
		return true;
	}
	
	/**
	 * Adapts attribute names in referenced in queries
	 * @param m
	 */
	private void renameAttrInQueries(MappingScenario m, Map<String,String> rename) {
		
	}

	/**
	 * Adapts attribute names referenced in correpondences
	 * @param m
	 */
	private void renameAttrInCorr(MappingScenario m, Map<String,String> rename) {
		for(CorrespondenceType c: m.getDoc().getScenario().getCorrespondences().getCorrespondenceArray()) {
			String[] attrs = c.getFrom().getAttrArray();
			renameAttrsInArray(rename, attrs);
			c.getFrom().setAttrArray(attrs);
			
			attrs = c.getTo().getAttrArray();
			renameAttrsInArray(rename, attrs);
			c.getTo().setAttrArray(attrs);
		}
	}

	/**
	 * 
	 * @param rename
	 * @param attrs
	 */
	private void renameAttrsInArray(Map<String, String> rename, String[] attrs) {
		for(int i = 0; i < attrs.length; i++) {
			String a = attrs[i];
			if (rename.containsKey(a)) {
				attrs[i] = rename.get(a);
			}
		}
	}

}
