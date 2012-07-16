package vtools.dataModel.expression;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.benchmark.model.IdGen;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class SPJQuery extends Query implements Visitable, Cloneable, Trampable
{
	static Logger log = Logger.getLogger(SPJQuery.class);
	
    private int _top;

    private boolean _distinct;

    private SelectClauseList _select;

    private FromClauseList _from;

    private BooleanExpression _where;

    private Vector<Projection> _groupby;

    private Path _into;

    // determines whether the from clause will have joins or commas
    private boolean _withJOINS;
    
    private ArrayList<String> _targets = new ArrayList<String>();
    
    private ArrayList<String> _sources = new ArrayList<String>();

    public SPJQuery()
    {
        _select = new SelectClauseList();
        _from = new FromClauseList();
        _where = null;
        _groupby = new Vector<Projection>();
        _top = -1;
        _distinct = false;
        _withJOINS = true;
        _into = null;
    }

    public SPJQuery(String stringExpr)
    {
        // SOS TO BE CHECKED BY YANNIS
        // SQLQueryParser parser = new SQLQueryParser();
        // SPJQuery spjquery = (SPJQuery) parser.parse(stringExpr);
        // _select = spjquery._select;
        // _from = spjquery._from;
        // _where = spjquery._where;
        // _top = spjquery._top;
        // _distinct = spjquery._distinct;
        // _withJOINS = spjquery._withJOINS;
        // _into = spjquery._into;
    }

    public void setSelect(SelectClauseList sel)
    {
        _select = sel;
    }

    public SelectClauseList getSelect()
    {
        return _select;
    }

    public boolean isDistinct()
    {
        return _distinct;
    }

    public void setDistinct(boolean distinct)
    {
        _distinct = distinct;
    }

    public int getTop()
    {
        return _top;
    }

    public void setTop(int top)
    {
        _top = top;
    }

    public Path getIntoSpec()
    {
        return _into;
    }

    public void setIntoTable(Path into)
    {
        _into = into;
    }

    public FromClauseList getFrom()
    {
        return _from;
    }

    public void setFrom(FromClauseList from)
    {
        _from = from;
    }

    public BooleanExpression getWhere()
    {
        return _where;
    }

    public void setWhere(BooleanExpression where)
    {
        _where = where;
    }

    public void addAndInWhere(BooleanExpression cond)
    {
        if (cond == null)
            return;
        else if (_where == null)
            _where = cond;
        else if (_where instanceof AND)
            ((AND) _where).add(cond);
        else
        {
            AND and;
            if (cond instanceof AND)
            {
                and = (AND) cond;
                and.add(_where);
            }
            else
            {
                and = new AND();
                and.add(_where);
                and.add(cond);
            }
            _where = and;
        }
    }

    public void addGroupByTerm(Projection term)
    {
        _groupby.add(term);
    }

    public Projection getGroupByTerm(int position)
    {
        return _groupby.elementAt(position);
    }

    public int getGroupBySize()
    {
        return _groupby.size();
    }

    public SPJQuery clone()
    {
        SPJQuery spj = (SPJQuery) super.clone();
        spj._from = new FromClauseList();
        for (int i = 0, imax = _from.size(); i < imax; i++)
        {
            Variable key = ((Variable) _from.getKey(i)).clone();
            Expression val = ((Expression) _from.getValue(i)).clone();
            spj._from.add(key, val);
        }
        spj._select = new SelectClauseList();
        for (int i = 0, imax = _select.size(); i < imax; i++)
        {
            String key = new String((String) _select.getKey(i));
            Expression val = ((Expression) _select.getValue(i)).clone();
            spj._select.add(key, val);
        }
        spj._where = (_where == null) ? null : (BooleanExpression) _where.clone();
        spj._top = _top;
        spj._distinct = _distinct;
        spj._withJOINS = _withJOINS;
        spj._into = (_into == null) ? null : _into.clone();
        int size_grpby = _groupby.size();
        spj._groupby = new Vector<Projection>(size_grpby);
        for (int i = 0; i < size_grpby; i++)
            spj._groupby.add((Projection) _groupby.elementAt(i).clone());
        return spj;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof SPJQuery))
            return false;
        if (!super.equals(o))
            return false;
        SPJQuery spj = (SPJQuery) o;
        if (_from.size() != spj._from.size())
            return false;
        for (int i = 0, imax = _from.size(); i < imax; i++)
        {
            Variable key = spj._from.getExprVar(i);
            Expression val = spj._from.getExpression(i);
            if (!(_from.getKey(i).equals(key)))
                return false;
            if (!(_from.getValue(i).equals(val)))
                return false;
        }
        if (_select.size() != spj._select.size())
            return false;
        for (int i = 0, imax = _select.size(); i < imax; i++)
        {
            String key = spj._select.getTermName(i);
            Expression val = spj._select.getTerm(i);
            if (!(_select.getKey(i).equals(key)))
                return false;
            if (!(_select.getValue(i).equals(val)))
                return false;
        }
        if (!(_where.equals(spj._where)))
            return false;
        if (_top != spj._top)
            return false;
        if (_distinct != spj._distinct)
            return false;
        if (_withJOINS != spj._withJOINS)
            return false;
        if (_into.equals(spj._into))
            return false;
        if (_groupby.size() != spj._groupby.size())
            return false;
        for (int i = 0, imax = _groupby.size(); i < imax; i++)
        {
            Projection p = (Projection) spj._groupby.elementAt(i);
            if (!(_groupby.get(i).equals(p)))
                return false;
        }
        return true;
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
    
    public void addTarget(String t) {
    	_targets.add(t);
    }
    
    public String getTarget(int i) {
    	return _targets.get(i);
    }
    
    public ArrayList<String> getTargets() {
    	return _targets;
    }
    
    public void addSource(String t) {
    	_sources.add(t);
    }
    
    public String getSource(int i) {
    	return _sources.get(i);
    }
    
    public ArrayList<String> getSources() {
    	return _sources;
    }
    
	@Override
	public String toTrampString(IdGen idGen) throws Exception {
		StringBuffer result = new StringBuffer();
		List<String> attrs, rels;
		attrs = new ArrayList<String> ();
		rels = new ArrayList<String> ();
			
		result.append("SELECT ");
		
		if (isDistinct())
			result.append(" DISTINCT ");
		
		for (int i = 0; i < _select.size(); i++) {
			String expr = _select.getValue(i).toString().toLowerCase();
			expr = expr.replace("/", ".").replace("$", "");					
			String name = _select.getKey(i).toString().toLowerCase();
			String attrString = expr + " AS " + name;
			attrs.add(name);
			
			result.append(attrString);
			if (i != _select.size() - 1)
				result.append(", ");
			log.debug("Add attr " + attrString);
		}
		
		if (_from.size() > 0) {
			result.append("\nFROM ");
			for (int i = 0; i < _from.size(); i++) {
				// check whether we have a subquery or a relation 
				Object fromItem = _from.getValue(i);
				
				if (fromItem instanceof Query) {
					String subQuery = ""; // keep compiler quiet
					
					if (fromItem instanceof SPJQuery) {
						SPJQuery spj = (SPJQuery) fromItem;
						subQuery = spj.toTrampString(idGen);
					}
					else if (fromItem instanceof SetOperation) {
						SetOperation setOp = (SetOperation) fromItem;
						subQuery = setOp.toTrampString(idGen);
					}
					else
						throw new Exception("Unkown subquery type " + fromItem.getClass());
					
					subQuery = "(" + subQuery + ")";
					
					String key = _from.getKey(i).toString();
					key = key.substring(1).toLowerCase();
					String relCode;
					
					
					relCode = subQuery + " AS " + key;
					result.append(relCode);
				}
				else if (fromItem instanceof Projection) {
					String key = _from.getKey(i).toString();
					key = key.substring(1).toLowerCase();
					String relName = _from.getValue(i).toString().toLowerCase();
					relName = "source." + relName.substring(1); // remove the first "/"
					String relCode = relName + " ANNOT('${" + idGen.getNextId() + "}') AS " + key;
					rels.add(relName);
					
					result.append(relCode);
					if (i != _from.size() - 1)
						result.append(", ");
				}
				else {
					throw new Exception("unkown from clause element " + fromItem.getClass());
				}
			}
		}

		if (_where != null) {
			result.append("\nWHERE ");
			
			String where = _where.toString();
			
			// find R.A patterns in where clause and lowercase them
			where = where.replaceAll("\\$(\\w*)/", "$1.");
			where = where.toLowerCase();
			where = where.replace("and", "AND");
			//TODO check if ok with string consts 
			
			result.append(where);
		}
		
		if (_groupby.size() != 0) {
			result.append("\nGROUP BY ");
			
			String group = "";
			
			for(int i = 0; i < _groupby.size(); i++) {
				Projection p = _groupby.get(i);
				String grExpr = p.toString().toLowerCase();
				
				if (i != _groupby.size() - 1)
					grExpr += ", ";
				group += grExpr;
			}
			group = group.replaceAll("\\$(\\w*)/", "$1.");
			group = group.toLowerCase();
			
			result.append(group);
		}
		
		log.debug(result);
		return result.toString();
    }

	@Override
	public String toTrampStringOneMap(String mapping) throws Exception {
		String result = toTrampString();
		for(int i = 0; i < getNumberOfLeafs(); i++) {
			result = result.replace("${" + i + "}", mapping);
		}
		return result;
	}
	
	public int getNumberOfLeafs () {
		int numLeafs = 0;
		for(int i = 0; i < _from.size(); i++) {
			Object fromItem = _from.getValue(i);
			
			if (fromItem instanceof Query)
				numLeafs += ((Query) fromItem).getNumberOfLeafs();
			if (fromItem instanceof Projection)
				numLeafs++;
		}
			
		return numLeafs;
	}
    
}
