/**
 * 
 */
package tresc.benchmark.queryGen;

import tresc.benchmark.Constants.QueryTranslatorType;

/**
 * @author lord_pretzel
 *
 */
public class QueryTranslationHandler {

	public static QueryTranslationHandler inst;
	
	private QueryTranslator t;

	static {
		inst = new QueryTranslationHandler();
	}
	
	private QueryTranslationHandler () {
		
	}
	
	public static QueryTranslationHandler getInst() {
		return inst;
	}
	
	public void setT(QueryTranslatorType type) {
		switch(type) {
		case Perm:
			t = new PermQueryTranslator();
			break;
		case Postgres:
			t = new PostgresQueryTranslator();
			break;
		default:
			break;
		}
	}
	
	public QueryTranslator getT() {
		return t;
	}

	public void setT(QueryTranslator t) {
		this.t = t;
	}
	
	
}
