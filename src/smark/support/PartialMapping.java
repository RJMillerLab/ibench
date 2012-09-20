package smark.support;

import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.vagabond.util.LoggerUtil;
import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.MapExprType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SKFunction;
import org.vagabond.xmlmodel.TransformationType;

import vtools.dataModel.expression.Query;
import vtools.dataModel.types.Set;

public class PartialMapping {

	static Logger log = Logger.getLogger(PartialMapping.class);
	
	private List<RelationType> sourceRels;
	private List<RelationType> targetRels;
	private List<CorrespondenceType> corrs;
	private List<MappingType> maps;
	private List<Query> queries;
	private List<TransformationType> trans;
	
	public PartialMapping () {
		sourceRels = new ArrayList<RelationType> ();
		targetRels = new ArrayList<RelationType> ();
		setCorrs(new ArrayList<CorrespondenceType> ());
		maps = new ArrayList<MappingType> ();
		queries = new ArrayList<Query> ();
		setTrans(new ArrayList<TransformationType> ());
	}

	public List<RelationType> getSourceRels() {
		return sourceRels;
	}

	public void setSourceRels(List<RelationType> sourceRels) {
		this.sourceRels = sourceRels;
	}
	
	public void addSourceRel (RelationType sourceRel) {
		this.sourceRels.add(sourceRel);
	}

	public List<RelationType> getTargetRels() {
		return targetRels;
	}

	public void setTargetRels(List<RelationType> targetRels) {
		this.targetRels = targetRels;
	}
	
	public void addTargetRel (RelationType targetRel) {
		this.targetRels.add(targetRel);
	}
	
	public SKFunction getSkolemFromAtom (MappingType m, boolean foreach, 
			int atomPos, int argPos) throws Exception {
		MapExprType clause = foreach ? m.getForeach() : m.getExists();
		RelAtomType atom = clause.getAtomArray(atomPos);
		XmlCursor c = atom.newCursor();
		c.toChild(argPos);
		XmlObject o = (XmlObject) c.getObject();
		if (!(o instanceof SKFunction))
			throw new Exception ("Expected an SK function: " + o.toString());
		return (SKFunction) o; 
	}
	
	public String[] getMapIds () {
		String[] result = new String[maps.size()];
		
		for(int i = 0; i < maps.size(); i++)
			result[i] = maps.get(i).getId();
		
		return result;
	}

	public List<MappingType> getMaps() {
		return maps;
	}

	public void setMaps(List<MappingType> maps) {
		this.maps = maps;
	}
	
	public void addMapping (MappingType m) {
		this.maps.add(m);
	}

	public List<Query> getQueries() {
		return queries;
	}

	public void setQueries(List<Query> trans) {
		this.queries = trans;
	}
	
	public void addQuery(Query trans) {
		this.queries.add(trans);
	}

	public List<CorrespondenceType> getCorrs() {
		return corrs;
	}
	
	public List<CorrespondenceType> getCorrs(int rel, boolean source) {
		String relName = getRelName(rel, source);
		List<CorrespondenceType> result = new ArrayList<CorrespondenceType> ();
		for(CorrespondenceType c: corrs) {
			if (source)
				if (c.getFrom().getTableref().equals(relName))
					result.add(c);
			else
				if (c.getTo().getTableref().equals(relName))
					result.add(c);
		}
		return result;
	}

	public void setCorrs(List<CorrespondenceType> corrs) {
		this.corrs = corrs;
	}
	
	public void addCorr (CorrespondenceType corr) {
		corrs.add(corr);
	}

	public List<TransformationType> getTrans() {
		return trans;
	}

	public void setTrans(List<TransformationType> trans) {
		this.trans = trans;
	}

	public void addTrans(TransformationType t) {
		this.trans.add(t);
	}
	
	public String getAttrId (int relId, int attrId, boolean source) {
		RelationType rel = source ? sourceRels.get(relId) 
				: targetRels.get(relId);
		return rel.getAttrArray()[attrId].getName();
	}
	
	public String[] getAttrIds (int relId, boolean source) {
		RelationType rel = source ? sourceRels.get(relId) 
				: targetRels.get(relId);
		String[] result = new String[rel.getAttrArray().length];
		for(int i = 0; i < rel.getAttrArray().length; i++)
			result[i] = rel.getAttrArray(i).getName();
		
		return result;
	}

	public String getRelName(int sRel, boolean source) {
		if (source)
			return sourceRels.get(sRel).getName();
		else
			return targetRels.get(sRel).getName();
	}
	
	public int[] getSourceNumAttrs () {
		int[] result = new int[sourceRels.size()];
		for (int i = 0; i < result.length; i++) {
			result[i] = sourceRels.get(i).sizeOfAttrArray();
		}
		
		return result;
	}
	
	public int getNumRelAttr (int relId, boolean source) {
		if (source)
			return sourceRels.get(relId).getAttrArray().length;
		else
			return targetRels.get(relId).getAttrArray().length;
	}

	@Override
	public String toString() {
		StringBuffer result = new StringBuffer(); 
		
		result.append("PARTIAL MAPPING:\n\n");
		for(RelationType rel: sourceRels)
			result.append(rel.toString());
		for(RelationType rel: targetRels)
			result.append(rel.toString());
		for(CorrespondenceType c: corrs)
			result.append(c.toString());
		for(MappingType map: maps)
			result.append(map.toString());
		for(TransformationType t: trans)
			result.append(t.toString());
		for(Query q: queries)
			try {
				result.append(q.toTrampString());
			}
			catch (Exception e) {
				LoggerUtil.logException(e, log);
			}
		return result.toString();
	}

}
