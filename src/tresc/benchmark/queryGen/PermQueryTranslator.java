/**
 * 
 */
package tresc.benchmark.queryGen;

import java.sql.SQLPermission;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

import org.apache.log4j.Logger;
import org.vagabond.benchmark.model.IdGen;

import vtools.dataModel.expression.BooleanExpression;
import vtools.dataModel.expression.FromClauseList;
import vtools.dataModel.expression.Projection;
import vtools.dataModel.expression.Query;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.expression.SetOperation;

/**
 * @author lord_pretzel
 *
 */
public class PermQueryTranslator implements QueryTranslator {

	static Logger log = Logger.getLogger(PermQueryTranslator.class);
	
	/* (non-Javadoc)
	 * @see tresc.benchmark.queryGen.QueryTranslator#queryToSQLCode(vtools.dataModel.expression.Query, org.vagabond.benchmark.model.IdGen)
	 */
	@Override
	public String queryToSQLCode(Query q, IdGen idGen) throws Exception {
		if (q instanceof SPJQuery)
			return spjQueryToSQLCode((SPJQuery) q, idGen);
		if (q instanceof SetOperation)
			return setQueryToSQLCode((SetOperation) q, idGen);
		return null;
	}

	private String spjQueryToSQLCode(SPJQuery q, IdGen idGen) throws Exception {
		StringBuffer result = new StringBuffer();
		List<String> attrs, rels;
		attrs = new ArrayList<String> ();
		rels = new ArrayList<String> ();
		SelectClauseList _select = q.getSelect();
		FromClauseList _from = q.getFrom();
		BooleanExpression _where = q.getWhere();
		Vector<Projection> _groupby = q.getGroupBys();
		
		result.append("SELECT ");
		
		if (q.isDistinct())
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
			if (log.isDebugEnabled()) {log.debug("Add attr " + attrString);};
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
		
		if (log.isDebugEnabled()) {log.debug(result);};
		return result.toString();

	}
	
	/* (non-Javadoc)
	 * @see tresc.benchmark.queryGen.QueryTranslator#queryToSQLCode(vtools.dataModel.expression.Query, java.lang.String)
	 */
	@Override
	public String queryToSQLCode(Query q, String mapping) throws Exception {
		if (q instanceof SPJQuery)
			return spjQueryToSQLCode((SPJQuery) q, mapping);
		if (q instanceof SetOperation)
			return setQueryToSQLCode((SetOperation) q, mapping);
		return null;

	}

	private String setQueryToSQLCode(SetOperation q, IdGen idGen) throws Exception {
		Vector<Query> _v = q.getComponents();
    	StringBuffer result = new StringBuffer();
    	for(int i = 0; i < _v.size(); i++) {
    		Query qsub = _v.get(i);
    		result.append(queryToSQLCode(qsub, idGen));
    		if (i != _v.size() - 1)
    			result.append("\n" + q.getSymbol() + "\n");
    	}
    	return result.toString();
	}
	
	private String spjQueryToSQLCode(SPJQuery q, String mapping) throws Exception {
		String result = queryToSQLCode(q, new IdGen());
		for(int i = 0; i < q.getNumberOfLeafs(); i++) {
			result = result.replace("${" + i + "}", mapping);
		}
		return result;
	}

	private String setQueryToSQLCode(SetOperation q, String mapping) throws Exception {
		String result = queryToSQLCode(q, new IdGen());
		int maxId = findMaxId(result);
		
		for(int i = 0; i < maxId; i++) {
			result = result.replace("${" + i + "}", mapping);
		}
		return result;
	}
	
    private int findMaxId (String serializedQ) {
    	int maxId = 0;
    	
    	while(serializedQ.contains("${" + ++maxId + "}"))
    		;
    	
    	return maxId;
    }
	
}
