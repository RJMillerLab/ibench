package vtools.dataModel.expression;

import java.util.ArrayList;
import java.util.Vector;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class SPJQuery extends Query implements Visitable, Cloneable
{
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
}
