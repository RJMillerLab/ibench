package vtools.xml;

import java.io.IOException;

import org.apache.xerces.parsers.DOMParser;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class XMLReader
{
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
			System.out.println("<xml version = \"1.0\">\n");
			NodeList nodes = node.getChildNodes();
			int nrNodes= nodes.getLength();
			if (nodes != null){
				for(int i=0; i<nrNodes; i++)
					printDOC(nodes.item(i),"");
			}
			
		case Node.ELEMENT_NODE:
			String name = node.getNodeName();
			System.out.print(indent+ "<"+name);
			
			NamedNodeMap attributes = node.getAttributes();
			if (attributes != null){
				int nrAttr= attributes.getLength();
				System.out.print(" nr attr = "+nrAttr);
				for(int i=0; i<nrAttr; i++ ){
					Node current = attributes.item(i);
					System.out.print(" "+current.getNodeName()+"=\""+current.getNodeValue()+"\"");
				}	
			}
			System.out.print(">");
			
			NodeList children = node.getChildNodes();
			if (children != null){
				int nrChild= children.getLength();
				System.out.print(" nr children = "+nrChild);
				for(int i=0; i<nrChild; i++)
					printDOC(children.item(i),indent+" ");
			}
			
			System.out.print("</"+name+">");
			break;
			
		case Node.TEXT_NODE:
			System.out.print(node.getNodeValue());
			break;
		}
		
	}
	
}
