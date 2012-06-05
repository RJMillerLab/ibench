package smark.support;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import vtools.dataModel.expression.Expression;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.BooleanExpression;
import vtools.dataModel.expression.AND;
import vtools.dataModel.expression.EQ;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.Rule;
import vtools.dataModel.expression.ForeignKey;
import vtools.dataModel.expression.FromClauseList;
import vtools.dataModel.schema.Schema;

/**
 * A mapping scenario is a triplet that consists of a source schema, a target
 * schema, and some specification on how to map from the source to the target.
 */
public class MappingScenario
{
    private Schema _source;

    private Schema _target;

    private SPJQuery _transformation;
    
    private HashMap<String, String> _correspondences 
    				= new HashMap<String, String>(); // <cid, source.attr=target.attr>
    
    private HashMap<String, ArrayList<String>> _mappings2Correspondences 
    				= new HashMap<String, ArrayList<String>>(); // <mid, <c1, c2, ... >>
    private HashMap<String, HashMap<String, ArrayList<Character>>> _mappings2Sources
    				= new HashMap<String, HashMap<String, ArrayList<Character>>>(); 	// <mid, <relName, <attr1, attr2, ... >>>
    private HashMap<String, HashMap<String, ArrayList<Character>>> _mappings2Targets
    				= new HashMap<String, HashMap<String, ArrayList<Character>>>(); 	// <mid, <relName, <attr1, attr2, ... >>>
    private HashMap<String, ArrayList<String>> _transformation2Mappings 
    				= new HashMap<String, ArrayList<String>>(); // <tid, <mid1, mid2, ...>>
    private HashMap<String, String> _transformationCode 
    				= new HashMap<String, String>(); // <tid, code>
    private HashMap<String, String> _transformationRelName 
    				= new HashMap<String, String>(); // <tid, relName>
    private int _cid = 0; // correspondence id
    private int _mid = 0; // mapping id
    private int _tid = 0; // transformation id
    private int _sk = 0;
    
    // this is a set of strings that describe how the mapping will be done.
    private Vector<String> _spec;

    public MappingScenario()
    {
        _source = new Schema("Source");
        _target = new Schema("Target");
        _transformation = new SPJQuery();
        _spec = new Vector<String>();
    }

    public Schema getSource()
    {
        return _source;
    }

    public SPJQuery getTransformation()
    {
        return _transformation;
    }
    
    public HashMap<String, String> getCorrespondences() {
    	return _correspondences;
    }
    
    public HashMap<String, ArrayList<String>> getMappings2Correspondences() {
    	return _mappings2Correspondences;
    }
    
    public HashMap<String, HashMap<String, ArrayList<Character>>> getMappings2Sources() {
    	return _mappings2Sources;
    }
    
    public HashMap<String, HashMap<String, ArrayList<Character>>> getMappings2Targets() {
    	return _mappings2Targets;
    }
    
    public HashMap<String, ArrayList<String>> getTransformation2Mappings() {
    	return _transformation2Mappings;
    }
    
    public HashMap<String, String> getTransformationCode() {
    	return _transformationCode;
    }
    
    public HashMap<String, String> getTransformationRelName() {
    	return _transformationRelName;
    }

    public void setSource(Schema source)
    {
        _source = source;
    }

    public Schema getTarget()
    {
        return _target;
    }

    public void setTarget(Schema target)
    {
        _target = target;
    }
    
    public synchronized String getNextCid() {
    	return "c"+_cid++;
    }
    
    public synchronized String getNextMid() {
    	return "m"+_mid++;
    }
    
    public synchronized String getNextTid() {
    	return "t"+_tid++;
    }
    
    public synchronized String getNextSK() {
    	return "" + _sk++;
    }
    
    public void setCorrespondences(Map<String, String> corr) {
    	_correspondences.putAll((HashMap<String, String>)corr);
    }
    
    public void putCorrespondences(String key, String value) {
    	_correspondences.put(key, value);
    }
    
    public void setMappings2Correspondences(Map<String, ArrayList<String>> m) {
    	_mappings2Correspondences.putAll((HashMap<String, ArrayList<String>>)m);
    }
    
    public void putMappings2Correspondences(String key, ArrayList<String> value) {
    	_mappings2Correspondences.put(key, value);
    }
    
    public void setMappings2Sources(Map<String, HashMap<String, ArrayList<Character>>> m) {
    	_mappings2Sources.putAll((HashMap<String, HashMap<String, ArrayList<Character>>>)m);
    }
    
    public void putMappings2Sources(String key, HashMap<String, ArrayList<Character>> value) {
    	_mappings2Sources.put(key, value);
    }
    
    public void setMappings2Targets(Map<String, HashMap<String, ArrayList<Character>>> m) {
    	_mappings2Targets.putAll((HashMap<String, HashMap<String, ArrayList<Character>>>)m);
    }
    
    public void putMappings2Targets(String key, HashMap<String, ArrayList<Character>> value) {
    	_mappings2Targets.put(key, value);
    }
    
    public void setTransformation2Mappings(Map<String, ArrayList<String>> t) {
    	_transformation2Mappings.putAll((HashMap<String, ArrayList<String>>)t);
    }

    public void putTransformation2Mappings(String key, ArrayList<String> value) {
    	_transformation2Mappings.put(key, value);
    }
    
    public void setTransformationCode(Map<String, String> t) {
    	_transformationCode.putAll((HashMap<String, String>)t);
    }
    
    public void putTransformationCode(String key, String value) {
    	_transformationCode.put(key, value);
    }
    
    public void setTransformationRelName(Map<String, String> t) {
    	_transformationRelName.putAll((HashMap<String, String>)t);
    }
    
    public void putTransformationRelName(String key, String value) {
    	_transformationRelName.put(key, value);
    }
    
    public void prettyPrint(StringBuffer buf, int noUse)
    {
        buf.append("\n");
        buf.append("==========================================================================================\n");
        buf.append("==========================================================================================\n");
        buf.append("Source Schema:\n");
        buf.append(_source.toString());
        buf.append("\n");
        buf.append("==========================================================================================\n");
        buf.append("==========================================================================================\n");
        buf.append("Target Schema:\n");
        buf.append(_target.toString());
        buf.append("\n");
        buf.append("==========================================================================================\n");
        buf.append("==========================================================================================\n");
        buf.append("Mapping Specifications:\n");
        if (_transformation.getFrom().size() != 0)
            buf.append(_transformation.toString());
        else
        {
            SelectClauseList sel = _transformation.getSelect();
            for (int i = 0, imax = sel.size(); i < imax; i++)
            {
                Expression exp = sel.getTerm(i);
                String str = sel.getTermName(i);
                buf.append("insert into " + str + "\n");
                buf.append(exp.toString() + "\n\n");
            }
        }
    }

    public void addSpec(String txt)
    {
        _spec.add(txt);
    }

    public String getSpec(int i)
    {
        return _spec.elementAt(i);
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        prettyPrint(buf, 0);
        return buf.toString();
    }
    
    public Vector<SMarkElement[][]> getSrcConstraints()
    {
    	Vector<SMarkElement[][]> srcConstraints=new Vector<SMarkElement[][]>();
    	for(int i=0;i<_source.getConstrSize();i++)
    	{
    		Rule thisConstraint=_source.getConstraint(i);
    		if(thisConstraint instanceof ForeignKey)
    		{
    			SMarkElement[][] thisElementArray=rule2ElementArray(thisConstraint);
    			srcConstraints.add(thisElementArray);
    		}
    	}
    	return srcConstraints;
    }
    
    private SMarkElement[][] rule2ElementArray(Rule constraint)
    {
 	
    	
    	FromClauseList constraintSrcTerms=constraint.getLeftTerms();
    	FromClauseList constraintTgtTerms=constraint.getRightTerms();
    	    	
    	BooleanExpression rightConditions= constraint.getRightConditions();
    	
    	if(rightConditions instanceof EQ)
    	{
    		SMarkElement[][] elementArray=new SMarkElement[1][2];

    		EQ equality=(EQ)(constraint.getRightConditions());

    		Projection constraintSrcProjection = (Projection) equality.getRight();
    		Projection constraintTgtProjection = (Projection) equality.getLeft();

    		SMarkElement constraintSrcElement = getLeafElement(constraintSrcTerms, constraintSrcProjection);
    		SMarkElement constraintTgtElement = getLeafElement(constraintTgtTerms, constraintTgtProjection);

    		elementArray[0][0]=constraintSrcElement;
    		elementArray[0][1]=constraintTgtElement;

    		return elementArray;
    	}
    	else
    	{
    		AND conjunction=(AND)(constraint.getRightConditions());
    		int nEqualities = conjunction.size();
    		
    		SMarkElement[][] elementArray=new SMarkElement[nEqualities][2];
    		
    		for(int i=0;i<nEqualities;i++)
    		{
        		EQ equality=(EQ)(conjunction.getComponent(i));

        		Projection constraintSrcProjection = (Projection) equality.getRight();
        		Projection constraintTgtProjection = (Projection) equality.getLeft();

        		SMarkElement constraintSrcElement = getLeafElement(constraintSrcTerms, constraintSrcProjection);
        		SMarkElement constraintTgtElement = getLeafElement(constraintTgtTerms, constraintTgtProjection);

        		elementArray[i][0]=constraintSrcElement;
        		elementArray[i][1]=constraintTgtElement;    			
    		}
    		
    		return elementArray;
    	}
    	
    	
    }
    
    private SMarkElement getLeafElement(FromClauseList constraintTerms, Projection leafProjection)
    {
    	Projection rootProjection = (Projection)(constraintTerms.getExpression(0));
    	SMarkElement currentElement=(SMarkElement)(_source.getRootElement(rootProjection.getLabel()));    	
    	    	
    	int nTerms=constraintTerms.size();
    	if(nTerms>1)
    	{
    		for(int i=1;i<nTerms;i++)
    		{
    			Projection thisProjection=(Projection)(constraintTerms.getExpression(i));

    			String thisLabel=thisProjection.getLabel();

    			currentElement=(SMarkElement)(currentElement.getSubElement(thisLabel));

    		}
    	}
    	
    	currentElement=(SMarkElement)(currentElement.getSubElement(leafProjection.getLabel()));
    	
    	return currentElement;
    }
    
    
}