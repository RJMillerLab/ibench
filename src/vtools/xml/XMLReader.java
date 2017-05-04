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
package vtools.xml;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLReader
{
	static Logger log = Logger.getLogger(XMLReader.class);
	
	public XMLReader() {}
	
	public Node load(String xmlfile) {
		Document doc = null;
		
	 try{	
		DOMParser parser = new DOMParser();
		parser.reset();
		parser.parse(xmlfile);
		doc = parser.getDocument();
	 }	
	 catch (IOException ioe) 
	        {ioe.printStackTrace();}
	 catch (SAXException saxe)
	 		{saxe.printStackTrace();}
	
	 return doc;
	}

	public void printDOC(Node node, String indent){
	
		switch (node.getNodeType()){
		
		  case Node.DOCUMENT_NODE :
			if (log.isDebugEnabled()) {log.debug("<xml version = \"1.0\">\n");};
			NodeList nodes = node.getChildNodes();
			int nrNodes= nodes.getLength();
			if (nodes != null){
				for(int i=0; i<nrNodes; i++)
					printDOC(nodes.item(i),"");
			}
			
		case Node.ELEMENT_NODE:
			String name = node.getNodeName();
			if (log.isDebugEnabled()) {log.debug(indent+ "<"+name);};
			
			NamedNodeMap attributes = node.getAttributes();
			if (attributes != null){
				int nrAttr= attributes.getLength();
				if (log.isDebugEnabled()) {log.debug(" nr attr = "+nrAttr);};
				for(int i=0; i<nrAttr; i++ ){
					Node current = attributes.item(i);
					if (log.isDebugEnabled()) {log.debug(" "+current.getNodeName()+"=\""+current.getNodeValue()+"\"");};
				}	
			}
			if (log.isDebugEnabled()) {log.debug(">");};
			
			NodeList children = node.getChildNodes();
			if (children != null){
				int nrChild= children.getLength();
				if (log.isDebugEnabled()) {log.debug(" nr children = "+nrChild);};
				for(int i=0; i<nrChild; i++)
					printDOC(children.item(i),indent+" ");
			}
			
			if (log.isDebugEnabled()) {log.debug("</"+name+">");};
			break;
			
		case Node.TEXT_NODE:
			if (log.isDebugEnabled()) {log.debug(node.getNodeValue());};
			break;
		}
		
	}
	
}
