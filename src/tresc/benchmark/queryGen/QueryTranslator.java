/**
 * 
 */
package tresc.benchmark.queryGen;

import org.vagabond.benchmark.model.IdGen;

import vtools.dataModel.expression.Query;

/**
 * @author lord_pretzel
 *
 */
public interface QueryTranslator {

	public String queryToSQLCode(Query q, IdGen idGen) throws Exception;
	public String queryToSQLCode(Query q, String mapping) throws Exception;
}
