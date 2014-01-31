package vtools.dataModel.expression;

import org.vagabond.benchmark.model.IdGen;

public interface Trampable {

	public String toTrampString () throws Exception;
	public String toTrampString (String ... mappings) throws Exception;
	public String toTrampStringOneMap(String mapping) throws Exception;
	public String toTrampString(IdGen idGen) throws Exception;
	
}
