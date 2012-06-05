package vtools.dataModel.expression;

import vtools.utils.structures.SetAssociativeArray;


public class SelectClauseList extends SetAssociativeArray implements Cloneable
{
	private static int _functionNumber = 0;

    public void add(String name, Expression expr)
    {
        super.add(name, expr);
    }
    
    public void add(Projection expr)
    {
        super.add(expr.getLabel(), expr);
    }
 
    public void insertAt (String name, Expression  expr, int position)
    {
    	super.insertAt(name, expr, position);
    }
    
    public void insertAt(Projection expr, int position)
    {
        super.insertAt(expr.getLabel(), expr, position);
    }
    
    public Expression getTerm(int i)
    {
        return (Expression) super.getValue(i);
    }

    public Expression getTerm(String label)
    {
        return (Expression) super.getValue(label);
    }
    
    public String getTermName(int i)
    {
        return (String) super.getKey(i);
    }

    public void setTermName(int i, String name)
    {
       super.setKeyAt(i, name);
    }
   
    public SelectClauseList clone()
    {
        SelectClauseList sel =  new SelectClauseList();
        for (int i = 0, imax = size(); i < imax; i++)
        {
            sel.add(getTermName(i), getTerm(i));
        }
        return sel;
    }
    
    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        for (int i = 0, imax = size(); i < imax; i++)
        {
            buf.append(((i != 0) ? ", " : ""));
            Object tmpo = getValue(i);   
            if (tmpo instanceof Projection)
            	buf.append(((Projection)tmpo).toString());
            else if (tmpo instanceof Query)
            	buf.append("(" + ((Query)tmpo).toString()+")");
            else if (tmpo instanceof Function)
            	buf.append(((Function)tmpo).toString().substring(0,2)+_functionNumber++); // only return the first 2 chars
        }
        return buf.toString();
    }
    
}
