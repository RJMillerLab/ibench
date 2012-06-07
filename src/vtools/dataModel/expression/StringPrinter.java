package vtools.dataModel.expression;

import vtools.visitor.VisitorImpl;

public class StringPrinter extends VisitorImpl
{
    public static StringPrinter StringPrinter = new StringPrinter();

    public Object visit(Rule rule, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        FromClauseList from = rule.getLeftTerms();
        for (int i = 0, imax = from.size(); i < imax; i++)
        {
            Expression expr = from.getExpression(i);
            Variable v = from.getExprVar(i);
            if (i != 0)
                buf.append(", ");
            buf.append(v.getName() + " IN ");
            expr.accept(this, args);
        }
        BooleanExpression bexpr = rule.getLeftConditions();
        if ((from.size() != 0) && (bexpr != null))
            buf.append(", ");
        if (bexpr != null)
            bexpr.accept(this, args);
        buf.append(" -->\n\t");

        from = rule.getRightTerms();
        for (int i = 0, imax = from.size(); i < imax; i++)
        {
            Expression expr = from.getExpression(i);
            Variable v = from.getExprVar(i);
            if (i != 0)
                buf.append(", ");
            buf.append(v.getName() + " IN ");
            expr.accept(this, args);
        }
        bexpr = rule.getRightConditions();
        if ((from.size() != 0) && (bexpr != null))
            buf.append(", ");
        if (bexpr != null)
            bexpr.accept(this, args);
        return null;
    }

    public Object visit(ConstantAtomicValue o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append(o.getValue().toString());
        return null;
    }

    public Object visit(ConstantSetValue o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append(o.getValue().toString());
        return null;
    }

    public Object visit(ConstantRcdValue o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append(o.getValue().toString());
        return null;
    }

    public Object visit(Root o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append("/");
        return null;
    }

    public Object visit(Current o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append(".");
        return null;
    }

    public Object visit(Projection o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        Path prefix = o.getPrefix();
        if (!(prefix instanceof Root))
            prefix.accept(this, args);
        buf.append("/" + o.getLabel());
        return null;
    }

    public Object visit(Variable o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append("$" + o.getName() + "");
        return null;
    }

    public Object visit(Function o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append(o.getName() + "(");
        for (int i = 0, imax = o.getNumOfArgs(); i < imax; i++)
        {
            ValueExpression exp = o.getArg(i);
            buf.append(((i != 0) ? ", " : ""));
            exp.accept(this, args);
        }
        buf.append(")");
        return null;
    }
    
    public Object visit(SKFunction o, Object[] args) {
    	final String CONCAT = " || ";
    	final String QUOTE = "'";
    	
    	StringBuffer buf = (StringBuffer) args[0];
        buf.append(QUOTE + o.getName() + QUOTE + CONCAT);
        for (int i = 0, imax = o.getNumOfArgs(); i < imax; i++)
        {
            ValueExpression exp = o.getArg(i);
            buf.append(((i != 0) ? CONCAT : ""));
            exp.accept(this, args);
        }
        
        return null;
    }    

    
    public Object visit(ComparisonOperator o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        o.getLeft().accept(this, args);
        buf.append(o.getSymbol());
        o.getRight().accept(this, args);
        return null;
    }

    public Object visit(EXISTS o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        Integer oldTabs = (Integer) args[1];
        Integer newTabs = new Integer(oldTabs.intValue() + 1);
        args[1] = newTabs;
        buf.append("EXISTS (\n");
        o.getRelation().accept(this, args);
        buf.append(")");
        args[1] = oldTabs;
        return null;
    }

    public Object visit(IN o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        Integer oldTabs = (Integer) args[1];
        Integer newTabs = new Integer(oldTabs.intValue() + 1);
        o.getExpression().accept(this, args);
        buf.append(" IN (");
        args[1] = newTabs;
        o.getRelation().accept(this, args);
        args[1] = oldTabs;
        buf.append(")");
        return null;
    }

    public Object visit(LogicOperator o, Object[] args)
    {
    	if(o.size() == 0)
    		return null;
    	
        StringBuffer buf = (StringBuffer) args[0];
        if (o instanceof NOT)
            buf.append(o.getSymbol());
        BooleanExpression exp = o.getComponent(0);
        if (exp instanceof LogicOperator)
            buf.append("(");
        exp.accept(this, args);
        if (exp instanceof LogicOperator)
            buf.append(")");
        for (int i = 1, imax = o.size(); i < imax; i++)
        {
            buf.append(" " + o.getSymbol() + "\n\t");
            exp = o.getComponent(i);
            if (exp instanceof LogicOperator)
                buf.append("(");
            exp.accept(this, args);
            if (exp instanceof LogicOperator)
                buf.append(")");
        }
        return null;
    }

    public Object visit(ConstantBooleanValue o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append(o.getValue().toString());
        return null;
    }

    public Object visit(SetOperation o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append(" ( ");
        Query q = o.getComponent(0);
        q.accept(this, args);
        for (int i = 1, imax = o.size(); i < imax; i++)
        {
            buf.append(")\n" + o.getSymbol() + "\n (");
            q = o.getComponent(i);
            q.accept(this, args);
        }
        buf.append(" ) ");
        return null;
    }

    public Object visit(Case o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append("CASE (");
        o.getCaseExpr().accept(this, args);
        buf.append("):");
        for (int i = 0, imax = o.size(); i < imax; i++)
        {
            buf.append("\nIF (");
            o.getWhenExpr(i).accept(this, args);
            buf.append(") THEN ");
            o.getThenExpr(i).accept(this, args);
        }
        buf.append("\nELSE ");
        o.getDefaultExpr().accept(this, args);
        return null;
    }

    public Object visit(SPJQuery o, Object[] args)
    {
        StringBuffer buf = (StringBuffer) args[0];
        buf.append("SELECT ");
        if (o.isDistinct())
            buf.append("DISTINCT ");
        int top = o.getTop();
        if (top != -1)
            buf.append("top " + top + " ");
        SelectClauseList select = o.getSelect();
        for (int i = 0, imax = select.size(); i < imax; i++)
        {
            buf.append(((i != 0) ? ", " : ""));
            Expression exp = select.getTerm(i);
            if (!(exp instanceof Path))
                buf.append("(");
            exp.accept(this, args);
            if (!(exp instanceof Path))
                buf.append(")");
            String label = select.getTermName(i);
            buf.append(" AS " + label);
        }
        FromClauseList from = o.getFrom();
        if (from.size() != 0)
            buf.append("\nFROM ");
        for (int i = 0, imax = from.size(); i < imax; i++)
        {
            Expression expr = from.getExpression(i);
            Variable v = from.getExprVar(i);
            if (i != 0)
                buf.append(", ");
            if (expr instanceof Query)
                buf.append("(");
            expr.accept(this, args);
            if (expr instanceof Query)
                buf.append(") AS ");
            buf.append(" "+v.getName() + " ANNOT('${" + i + "}')");
        }

        BooleanExpression where = o.getWhere();
        if (where != null)
        {
            buf.append("\nWHERE ");
            where.accept(this, args);
        }

        int groupBySize = o.getGroupBySize();
        if (groupBySize != 0)
        {
            buf.append("\ngroupby (");
            o.getGroupByTerm(0).accept(this, args);
            for (int i = 1, imax = groupBySize; i < imax; i++)
            {
                Expression exp = o.getGroupByTerm(i);
                buf.append(",");
                exp.accept(this, args);
            }
            buf.append(")");
        }
        return null;
    }
}
