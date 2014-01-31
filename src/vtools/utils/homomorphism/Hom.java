package vtools.utils.homomorphism;

import java.util.Vector;

import vtools.dataModel.expression.AND;
import vtools.dataModel.expression.BooleanExpression;
import vtools.dataModel.expression.EQ;
import vtools.dataModel.expression.FromClauseList;
import vtools.dataModel.expression.SPJQuery;
import vtools.utils.structures.SetAssociativeArray;
//import vtools.SQLOld.ConstantValue;
//import vtools.SQLOld.Length1PathExpression;
//import vtools.SQLOld.Relation;
//import vtools.SQLOld.RelationalAttributeExpression;
//import vtools.SQLOld.RelationalTableReference;
//import vtools.SQLOld.UnaryExpr;
//import vtools.dataModel.expression.PathVariable;

/**
 * 
 */
public class Hom extends SetAssociativeArray
{
    public Hom()
    {
        super();
    }

    public void addMap(String the, String to)
    {
        super.add(the, to);
    }

    /**
     * Returns the string to which the var has been mapped
     */
    public String apply(String var)
    {
        return (String) getValue(var);
    }

    public FromClauseList apply(FromClauseList fromClause)
    {
        FromClauseList newFrom = new FromClauseList();
        for (int i = 0, imax = fromClause.size(); i < imax; i++)
        {
//            String var = fromClause.getRelationName(i);
//            var = apply(var);
//            if (var == null)
//                return null;
//            Relation r = (Relation) fromClause.getRelation(i).clone();
//            newFrom.add(var, r);
        }
        return newFrom;

    }

//    public RelationalAttributeExpression apply(RelationalAttributeExpression attrExpr)
//    {
//        PathVariable tblExpr = attrExpr.getPath().getVariable();
//        String tblName = tblExpr.getName();
//        tblName = apply(tblName);
//        if (tblName == null)
//            return null;
//        RelationalAttributeExpression newExpr = new RelationalAttributeExpression(new Length1PathExpression(new PathVariable(tblName), attrExpr.getPath().getLabel()));
//        return newExpr;
//    }

    public String toString()
    {
        StringBuffer buf = new StringBuffer();
        buf.append("[");
        for (int i = 0, imax = size(); i < imax; i++)
            buf.append(((i != 0) ? ", " : "") + getKey(i) + "-->" + getValue(i));
        buf.append("] ");
        return buf.toString();
    }



    /*
     * Finds all the possible partial max homs from the left to the right query
     */
    public static Vector findMappings(SPJQuery fromQ, SPJQuery toQ)
    {
        int NF = fromQ.getFrom().size();
        int NT = toQ.getFrom().size();
        int[] srcArr = new int[NF];
        int[] trgArr = new int[NT];
        for (int i = 0, imax = NF; i < imax; i++)
            srcArr[i] = -1;
        for (int i = 0, imax = NT; i < imax; i++)
            trgArr[i] = -1;

        Vector mappings = new Vector();
        Hom.tryToMap(0, srcArr, trgArr, fromQ.getFrom(), toQ.getFrom(), mappings);
        return mappings;
    }



    private static void tryToMap(int pos, int[] srcArr, int[] trgArr, FromClauseList fromCl, FromClauseList toCl,
            Vector foundHoms)
    {
        // if we reached the end of the src Arr it means that we have checked
        // them all so test if we have mapped something or not. If yes, then we
        // report it
        if (pos == srcArr.length)
        {
            Hom hom = new Hom();
//            for (int i = 0, imax = srcArr.length; i < imax; i++)
//                if (srcArr[i] != -1)
//                    hom.addMap(fromCl.getRelationName(i), toCl.getRelationName(srcArr[i]));
            if (hom.size() != 0)
            {
                // we are about to insert it but before we do, let's make sure
                // that there is no other one already computed that includes it.
                boolean isIncludedInAnother = false;
                for (int ii = 0, iimax = foundHoms.size(); ii < iimax; ii++)
                {
                    Hom tmpHom = (Hom) foundHoms.elementAt(ii);
                    if (tmpHom.includes(hom))
                    {
                        isIncludedInAnother = true;
                        break;
                    }
                }
                // if none includes it insert it.
                if (!isIncludedInAnother)
                    foundHoms.add(0, hom);
            }
            return;
        }

        // Get the relation you try to mapp
//        String srcTbl = ((RelationalTableReference) fromCl.getRelation(pos)).getPath().getLabel();
        // try to map it
        for (int i = 0, imax = trgArr.length; i < imax; i++)
        {
            if (trgArr[i] != -1)
                continue; // cannot map to this one
//            String toTbl = ((RelationalTableReference) toCl.getRelation(i)).getPath().getLabel();
//            if (!toTbl.equals(srcTbl))
//                continue; // not compatible tables
            // otherwise they can map
            srcArr[pos] = i;
            trgArr[i] = pos;
            tryToMap(pos + 1, srcArr, trgArr, fromCl, toCl, foundHoms);
            srcArr[pos] = -1;
            trgArr[i] = -1;
        }
        // consider also the case where the pos table is not mapped at all.
        srcArr[pos] = -1;
        tryToMap(pos + 1, srcArr, trgArr, fromCl, toCl, foundHoms);
    }

    private boolean includes(Hom hom)
    {
        for (int i = 0, imax = hom.size(); i < imax; i++)
        {
            String srcR = (String) hom.getKey(i);
            String trgR = (String) hom.getValue(i);
            String localMap = (String) getValue(srcR);
            if (localMap == null)
                return false;
            if (!localMap.equals(trgR))
                return false;
        }
        return true;
    }

    public AND apply(AND and)
    {
        AND newAnd = new AND();
        for (int i = 0, imax = and.size(); i < imax; i++)
        {
            EQ eq = (EQ)and.getComponent(i);
            newAnd.add(apply(eq));
        }
        return newAnd;
    }
    
//    public EQ apply(EQ eq)
//    {
//        UnaryExpr left = eq.getLeft();
//        left = apply(left);
//        UnaryExpr right = eq.getRight();
//        right = apply(right);
//        return new EQ(left, right);
//    }
//    
//    public UnaryExpr apply(UnaryExpr ue)
//    {
//        if (ue instanceof RelationalAttributeExpression)
//            return apply((RelationalAttributeExpression)ue);
//        else if (ue instanceof ConstantAtomicValue)
//            return (ConstantAtomicValue)((ConstantAtomicValue)ue).clone();
//        throw new RuntimeException("Not supported yet");
//    }

    
    public BooleanExpression apply(BooleanExpression cond)
    {
        if (cond instanceof AND)
            return apply((AND)cond);
        else if (cond instanceof EQ)
            return apply((EQ)cond);
        throw new RuntimeException("Not supported yet");
    }

    public Hom reverse()
    {
        Hom newHom = new Hom();
        for (int i=0, imax=size(); i< imax; i++)
            newHom.add(getValue(i), getKey(i));
        return newHom;
    }
}
