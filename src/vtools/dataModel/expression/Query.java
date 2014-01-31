package vtools.dataModel.expression;


import org.vagabond.benchmark.model.IdGen;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

/**
 * 
 */
public abstract class Query extends Expression implements Visitable, Cloneable, Trampable
{
	protected String code;
	
    public boolean equals(Object o)
    {
        if (!(o instanceof Query))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }
    
    public Query clone()
    {
        return (Query) super.clone();
    }

    public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
    
    
	@Override
	public String toTrampString(String... mappings) throws Exception {
		String result = toTrampString();
		
		for(int i = 0; i < mappings.length; i++)
			result = result.replace("${" + i + "}", mappings[i]);
		
		return result;
	}

    @Override
    public String toTrampString() throws Exception {
    	return toTrampString(new IdGen());
    }
    
    public void storeCode(String code) {
    	this.code = code;
    }
    
    public String getStoredCode() {
    	return code;
    }
    
    protected int findMaxId (String serializedQ) {
    	int maxId = 0;
    	
    	while(serializedQ.contains("${" + ++maxId + "}"))
    		;
    	
    	return maxId;
    }
    
    public abstract int getNumberOfLeafs ();
    
}
