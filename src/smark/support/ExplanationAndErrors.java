package smark.support;

import java.util.HashMap;
import java.util.Map;

import org.vagabond.explanation.model.ExplanationFactory;
import org.vagabond.explanation.model.IExplanationSet;

import tresc.benchmark.Constants.DESErrorType;

public class ExplanationAndErrors {

	private Map<DESErrorType, IExplanationSet> expls;
	private IExplanationSet allExpls;
	
	public ExplanationAndErrors () {
		expls = new HashMap<DESErrorType, IExplanationSet> ();
		allExpls = ExplanationFactory.newExplanationSet();
	}
	
	public void addSet (DESErrorType type, IExplanationSet e) {
		expls.put(type, e);
		allExpls.addAll(e);
	}
	
	public IExplanationSet getAll () {
		return allExpls;
	}
	
	public IExplanationSet getSetForErrorType (DESErrorType type) {
		return expls.get(type);
	}
	
}
