package smark.support;

import java.util.ArrayList;
import java.util.List;

import org.vagabond.xmlmodel.MappingType;

import vtools.dataModel.expression.Query;
import vtools.dataModel.types.Set;

public class PartialMapping {

	private List<Set> sourceRels;
	private List<Set> targetRels;
	private List<MappingType> maps;
	private List<Query> trans;
	
	public PartialMapping () {
		sourceRels = new ArrayList<Set> ();
		targetRels = new ArrayList<Set> ();
		maps = new ArrayList<MappingType> ();
		trans = new ArrayList<Query> ();
	}

	public List<Set> getSourceRels() {
		return sourceRels;
	}

	public void setSourceRels(List<Set> sourceRels) {
		this.sourceRels = sourceRels;
	}
	
	public void addSourceRel (Set sourceRel) {
		this.sourceRels.add(sourceRel);
	}

	public List<Set> getTargetRels() {
		return targetRels;
	}

	public void setTargetRels(List<Set> targetRels) {
		this.targetRels = targetRels;
	}
	
	public void addTargetRel (Set targetRel) {
		this.targetRels.add(targetRel);
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

	public List<Query> getTrans() {
		return trans;
	}

	public void setTrans(List<Query> trans) {
		this.trans = trans;
	}
	
	public void addTrans(Query trans) {
		this.trans.add(trans);
	}
	
}
