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
package vtools.dataModel.types;

import java.util.Arrays;

public class RandSrcSkolem 
{
	private int _attrPosition;
	private int[] _argPositions;
	
	private String _attr;
	private String _attrVar;
	
	private String[] _argAttrs;
	private String[] _argVars;
	
	private String _skId;
	
	public int getAttrPosition()
	{
		return _attrPosition;
	}
	
	public int[] getArgPositions() 
	{
		return _argPositions;
	}
	
	public String getAttr()
	{
		return _attr;
	}
	
	public String[] getArgAttrs()
	{
		return _argAttrs;
	}
	
	public String getAttrVar()
	{
		return _attrVar;
	}
	
	public String[] getArgVars()
	{
		return _argVars;
	}
	
	public String getSkId() 
	{
		return _skId;
	}
	
	public void setAttrPosition(int attrPosition)
	{
		_attrPosition = attrPosition;
	}
	
	public void setArgPositions(int[] argPositions) 
	{
		_argPositions = argPositions;
	}
	
	public void setAttr (String attr)
	{
		_attr = attr;
	}
	
	public void setArgAttrs (String[] argAttrs)
	{
		_argAttrs = argAttrs;
	}

	public void setAttrVar (String attrVar)
	{
		_attrVar = attrVar;
	}
	
	public void setArgVars (String[] argVars)
	{
		_argVars = argVars;
	}

	public void setSkId(String skId) 
	{
		_skId = skId;
	}
	
	@Override
	public String toString () {
		StringBuilder b = new StringBuilder();
		
		b.append("AttrPosition: " + _attrPosition + "\n");
		b.append("Arg Positions: " + Arrays.toString(_argPositions) + "\n");
		b.append("Attr: " + _attr + "\n");
		b.append("Attr Var: " + _attrVar + "\n");
		b.append("Arg Attrs: " + Arrays.toString(_argAttrs) + "\n");
		b.append("Arg Vars: " + Arrays.toString(_argVars) + "\n");
		b.append("SKId: " + _skId + "\n");
		
		return b.toString();
	}
}
