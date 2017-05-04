/*
 *
 * Copyright 2016 Big Data Curation Lab, University of Toronto,
 * 		   	  	  	   				 Patricia Arocena,
 *   								 Boris Glavic,
 *  								 Renee J. Miller
 *
 * This software also contains code derived from STBenchmark as described in
 * with the permission of the authors:
 *
 * Bogdan Alexe, Wang-Chiew Tan, Yannis Velegrakis
 *
 * This code was originally described in:
 *
 * STBenchmark: Towards a Benchmark for Mapping Systems
 * Alexe, Bogdan and Tan, Wang-Chiew and Velegrakis, Yannis
 * PVLDB: Proceedings of the VLDB Endowment archive
 * 2008, vol. 1, no. 1, pp. 230-244
 *
 * The copyright of the ToxGene (included as a jar file: toxgene.jar) belongs to
 * Denilson Barbosa. The iBench distribution contains this jar file with the
 * permission of the author of ToxGene
 * (http://www.cs.toronto.edu/tox/toxgene/index.html)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */
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
