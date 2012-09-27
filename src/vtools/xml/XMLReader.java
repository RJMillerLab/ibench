package vtools.xml;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
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
