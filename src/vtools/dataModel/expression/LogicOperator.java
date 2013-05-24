package vtools.dataModel.expression;

import java.util.Vector;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public abstract class LogicOperator extends BooleanExpression implements Visitable, Cloneable
{

    public static int AND = 1;
    public static int OR = 1;
    public static int NOT = 1;
    
    protected Vector<BooleanExpression> _v;

    protected LogicOperator()
    {
        _v = new Vector<BooleanExpression>();
    }

    public abstract int getCode();
    public abstract String getSymbol();
    
    public BooleanExpression remove(int i)
    {
        return (BooleanExpression)_v.remove(i);
    }
   
    public void add(BooleanExpression expr)
    {
        _v.add(expr);
    }
   
    protected BooleanExpression getComponent(int i)
    {
        return _v.get(i);
    }
    
    public int size()
    {
        return _v.size();
    }

    public LogicOperator clone()
    {
        LogicOperator op = (LogicOperator) super.clone();
        Vector<BooleanExpression> v = new Vector<BooleanExpression>();
        for (int i = 0, imax = _v.size(); i < imax; i++)
        {
            v.add(_v.elementAt(i).clone());
        }
        op._v = v;
        return op;
    }

    public boolean equals(Object o)
    {
        if (!(o instanceof LogicOperator))
            return false;
        if (!super.equals(o))
            return false;
        LogicOperator op = (LogicOperator) o;
        if (_v.size() != op._v.size())
            return false;
        for (int i = 0, imax = _v.size(); i < imax; i++)
        {
            if (!(_v.elementAt(i).equals(op._v.elementAt(i))))
                return false;
        }
        return true;
    }
    
    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
}
