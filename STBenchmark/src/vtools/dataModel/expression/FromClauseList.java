package vtools.dataModel.expression;

import javax.swing.text.Position;
import vtools.utils.structures.SetAssociativeArray;


public class FromClauseList extends SetAssociativeArray implements Cloneable
{

    public void add(Variable var, Expression expr)
    {
        super.add(var, expr);
    }

    public void add(Projection expr)
    {
        Variable var = new Variable(expr.getLabel());
        super.add(var, expr);
    }

    public void insertAt(Variable var, Expression expr, int position)
    {
        super.insertAt(var, expr, position);
    }

    public void insertAt(Projection expr, int position)
    {
        Variable var = new Variable(expr.getLabel());
        super.insertAt(var, expr, position);
    }

    public int getExprVarPosition(Variable var)
    {
        return getKeyPosition(var);
    }

    public Variable getExprVar(int i)
    {
        return (Variable) super.getKey(i);
    }

    public Expression getVarExpression(Variable var)
    {
        int pos = getExprVarPosition(var);
        if (pos == -1)
            return null;
        return getExpression(pos);
    }

    public void setExprVar(int i, Variable var)
    {
        super.setKeyAt(i, var);
    }

    public Expression getExpression(int i)
    {
        return (Expression) super.getValue(i);
    }

    public void setExpression(Expression expr, int i)
    {
        super.setValueAt(i, expr);
    }

    public FromClauseList clone()
    {
        FromClauseList from = new FromClauseList();
        for (int i = 0, imax = size(); i < imax; i++)
        {
            from.add(getExprVar(i), getExpression(i));
        }
        return from;
    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0, imax = size(); i < imax; i++)
        {
            String qStr = null;
            Expression expr = getExpression(i);
            if (expr instanceof Query)
                qStr = "(" + expr.toString() + ") AS";
            else qStr = expr.toString();
            buf.append(((i != 0) ? ", " : "") + qStr + " " + getExprVar(i).toString());
        }
        return buf.toString();
    }

}
