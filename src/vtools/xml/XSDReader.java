package vtools.xml;

import org.apache.log4j.Logger;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.w3c.dom.Element; 
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Atomic;
import vtools.dataModel.types.Complex;
import vtools.dataModel.types.Rcd;
import vtools.dataModel.types.Set;
import vtools.dataModel.types.Structured;
import vtools.dataModel.types.Type;
import vtools.utils.structures.SetAssociativeArray;

public class XSDReader
{
	static Logger log = Logger.getLogger(XSDReader.class);
	
	SetAssociativeArray types; 
	int dummyNr;
	
	public XSDReader(){   }
	
	public XSDReader(String xsdfile){
		XMLReader xmlReader = new XMLReader();
		Node node = xmlReader.load(xsdfile);
		
		Element root = ((Document) node).getDocumentElement();
		removeWhiteSpaceNodes(root);
		
		Schema schemaNR = visitRoot(root,"SchemaExperiments");
		log.debug(schemaNR);
	}
	
	private Schema visitRoot(Node root, String nameSchema){
		Schema s = new Schema(nameSchema);
		types = new SetAssociativeArray();
		dummyNr = 0;
		//process the complexType nodes under the schema node
		NodeList children = root.getChildNodes();
		int nr=children.getLength();
		for(int j=0; j<nr; j++){
			if (children.item(j).getNodeName().equalsIgnoreCase("xs:complexType")){
				Node nodeCT = children.item(j);
				String name = getAttr("name",nodeCT);
				vtools.dataModel.schema.Element eCT = processComplexTypeNode(nodeCT,name,false);
				types.add(eCT.getLabel(), eCT.getType().clone());
			}	
		}
		//process the element nodes under the schema node
		for(int j=0; j<nr; j++){
			if (children.item(j).getNodeName().equalsIgnoreCase("xs:element")){
				vtools.dataModel.schema.Element elem = processElementNode(children.item(j));
				s.addRootElement(elem);
			}
		}
		
		return s;
	}
	
	private vtools.dataModel.schema.Element processElementNode(Node node){
		vtools.dataModel.schema.Element elem = null;
		String type = null;
		String name = null;
		String maxOccurs = null;
		
		if (node.getNodeName() != "xs:element"){
			throw new RuntimeException("Not an xs:element");
		}
		
		name = getAttr("name", node);
		type = getAttr("type", node);
		maxOccurs = getAttr("maxOccurs", node);
		
		//if there is a type(Atomic or Complex) for the current element node
		if (!type.isEmpty()){
			elem = processSimpleContent(name,type,maxOccurs);
		}
		else{
			Node complexNode = null;
			NodeList children = node.getChildNodes();
			for(int j=0, nr=children.getLength(); j<nr; j++){
				if (children.item(j).getNodeName().equalsIgnoreCase("xs:complexType"))
					complexNode = children.item(j);
			}
				 
			if (complexNode == null){
					 log.debug("Syntax Error");
			}
			else{
				if (! maxOccurs.equalsIgnoreCase("unbounded")){
					elem = processComplexTypeNode(complexNode,name,false);
				}
				else elem = processComplexTypeNode(complexNode,name,true);
			}
		}//end-if-else- there is a type
		
		return elem;
	}
	
	private vtools.dataModel.schema.Element processSimpleContent(String name,String type,String maxOccurs){
		vtools.dataModel.schema.Element e = null;
		vtools.dataModel.schema.Element eParent = null;
		Type typeElem = null;
		
		if (type.equalsIgnoreCase("xs:string"))  typeElem = Atomic.STRING ;
		else if (type.equalsIgnoreCase("xs:integer"))  typeElem = Atomic.INTEGER;
			 else typeElem = (Type)((Type)types.getValue(type)).clone();
		
		if (! maxOccurs.equalsIgnoreCase("unbounded")){
			eParent = new vtools.dataModel.schema.Element(name,typeElem,null);
		}
		else {
			e= new vtools.dataModel.schema.Element(name,typeElem,null);
			dummyNr++;
			eParent = new vtools.dataModel.schema.Element("dummy"+dummyNr,new Set(),null);
			eParent.addSubElement(e);
			e.setParent(eParent);
		}

		return eParent;
	}


	private vtools.dataModel.schema.Element processComplexTypeNode(Node nodeCT,String nameParent,boolean unbounded){
		vtools.dataModel.schema.Element elem = null;
		vtools.dataModel.schema.Element eChild = null;
		Structured rcd = null;
		Node childCTNode = nodeCT.getChildNodes().item(0);
		
		if (unbounded) rcd = new Set();
		else rcd = new Rcd();
		elem= new vtools.dataModel.schema.Element(nameParent,rcd,null);
		
		//process the group (i.e. Sequence/Choice/All) 
		eChild = processGroupNode(childCTNode);
		
		//optimize the dummy element of type Rcd
		int nrchild = eChild.size();
		if ((eChild.getType() instanceof Rcd) &&
		   (eChild.getLabel().contains("dummy"))){
			for(int t=0; t<nrchild; t++){
				elem.addSubElement(eChild.getSubElement(t));
			}
		}
		else{
				rcd.addField(eChild);
				eChild.setParent(elem);
		}//end-if-else
		
		//optimize when elem has 1 dummy child  
		int nr = elem.size();
		if (nr == 1){
			eChild = elem.getSubElement(0);
			if (eChild.getLabel().contains("dummy")){
				elem.setType(eChild.getType());
				eChild.getSubElement(0).setParent(elem);
				elem.removeSubElement(eChild.getLabel());	
			}
		}
		return elem;
	}

	private vtools.dataModel.schema.Element processGroupNode(Node nodeGroup){
		vtools.dataModel.schema.Element e = null;
		vtools.dataModel.schema.Element eGroup = null;
		Structured rcdGroup = null;
		
		String maxOccurs = getAttr("maxOccurs", nodeGroup);
		if (! maxOccurs.equalsIgnoreCase("unbounded")) rcdGroup = new Rcd();
		else rcdGroup = new Set();
		
		dummyNr ++;
		eGroup = new vtools.dataModel.schema.Element("dummy"+dummyNr,rcdGroup,null);
		
		NodeList child = nodeGroup.getChildNodes();
		int nr=child.getLength();
		for(int k=0; k<nr; k++){
			String kindOfNode = child.item(k).getNodeName();
			if (kindOfNode.equalsIgnoreCase("xs:choice")){
			 //todo
			}	
			if (kindOfNode.equalsIgnoreCase("xs:sequence"))  e = processGroupNode(child.item(k));
			if (kindOfNode.equalsIgnoreCase("xs:all"))  e = processGroupNode(child.item(k));
			if (kindOfNode.equalsIgnoreCase("xs:element")) e = processElementNode(child.item(k));
			
			rcdGroup.addField(e);
			e.setParent(eGroup);
		}//end-for-children
		
		//optimize if the eGroup has dummy children of type Rcd
		int nrGroup = eGroup.size();
		for(int t=0; t<nrGroup; t++){
			vtools.dataModel.schema.Element eChild = eGroup.getSubElement(t);
			if ((eChild.getType() instanceof Rcd) &&
					   (eChild.getLabel().contains("dummy"))){
				int nrChild = eChild.size();
				for(int k=0; k<nrChild; k++){
					eGroup.addSubElement(eChild.getSubElement(k));
				}
				//remove the element eChild from the list of children of eGroup
				eGroup.removeSubElement(eChild.getLabel());	
			}//end-if
		}//end-for children of eGroup
		
		return eGroup;
	}
	
	private String getAttr(String nameAttr, Node n){
		Element e = (Element) n;
		String value = e.getAttribute(nameAttr);
		return value;
	}

		private void printDOC(Node node, String indent){
		switch (node.getNodeType()){
		  case Node.DOCUMENT_NODE :
			log.debug("<xml version = \"1.0\">\n");
			NodeList nodes = node.getChildNodes();
			int nrNodes= nodes.getLength();
			if (nodes != null){
				for(int i=0; i<nrNodes; i++)
					printDOC(nodes.item(i),"");
			}
			
		case Node.ELEMENT_NODE:
			String name = node.getNodeName();
			log.debug(indent+ "<"+name);
			
			NamedNodeMap attributes = node.getAttributes();
			if (attributes != null){
				int nrAttr= attributes.getLength();
				//log.debug(" nr attr = "+nrAttr);
				for(int i=0; i<nrAttr; i++ ){
					Node current = attributes.item(i);
					log.debug(" "+current.getNodeName()+"=\""+current.getNodeValue()+"\"");
				}	
			}
			log.debug(">");
			
			NodeList children = node.getChildNodes();
			if (children != null){
				int nrChild= children.getLength();
				//log.debug(" nr children = "+nrChild);
				for(int i=0; i<nrChild; i++)
					printDOC(children.item(i),indent+" ");
			}
			
			log.debug("</"+name+">");
			break;
			
		case Node.TEXT_NODE:
			log.debug(node.getNodeValue());
			break;
		}
	}
	
	
	private void removeWhiteSpaceNodes(Element parent){
		for (Node child = parent.getFirstChild(); child != null; ){
			Node nextNode = child.getNextSibling();
			if (child.getNodeType() == Node.TEXT_NODE ){
				if (isIgnorableWhiteSpaces(child.getNodeValue())){
					parent.removeChild(child);
				}
			}
			else
				if (child.getNodeType() == Node.ELEMENT_NODE){
					removeWhiteSpaceNodes((Element) child);
				}
			child = nextNode;
		}
	}
	
	private boolean isIgnorableWhiteSpaces(String nodevalue){
		for (int i=0, len = nodevalue.length(); i<len; i++){
			char c = nodevalue.charAt(i);
			if ((c!=' ') && (c!='\t') && (c!='\n'))
				return false;
		}//end_for
		return true;
	}

	
/*old code	
	private vtools.dataModel.schema.Element processComplexTypeNode(Node nodeCT,String name,boolean unbounded){
		vtools.dataModel.schema.Element elem = null;
		ComplexType rcd = null;
		Node childCTNode = nodeCT.getChildNodes().item(0);
		
		if (childCTNode.getNodeName().equalsIgnoreCase("xs:choice")){
			//TODO
			log.debug("an element group type Choice");
		}
		
		if ((childCTNode.getNodeName().equalsIgnoreCase("xs:sequence")) || 
		    (childCTNode.getNodeName().equalsIgnoreCase("xs:all"))){
				 if (unbounded) rcd = new SetOfRcds();
				 //TODO if the Sequence group has macOccurs unbounded
				 else rcd = new Rcd();
				 elem= new vtools.dataModel.schema.Element(name,rcd,null);
				 
				 NodeList children = childCTNode.getChildNodes();
				 int nr=children.getLength();
				 if (nr == 1){
					 vtools.dataModel.schema.Element e = processElementNode(children.item(0));
					 if (e.getLabel().contains("dummy")) {
						 elem.setType(e.getType());
						 e.getSubElement(0).setParent(elem);
					 }
					 else {
						 rcd.addField(e);
						 e.setParent(elem);
					 }
				 }
				 else for(int k=0; k<nr; k++){
					 	vtools.dataModel.schema.Element e = processElementNode(children.item(k));
					 	rcd.addField(e);
					 	e.setParent(elem);
				 	  }//end-for
		} 
		
		return elem;
	}

	
	
	private void decideAndVisit(Node root){
			vtools.dataModel.schema.Element nestedRel = null;
		
			NodeList nodeChild = root.getChildNodes();
			int nr= nodeChild.getLength();
			if (nr == 1){
				nestedRel = visitNodeElement(nodeChild.item(0));
			}
			else{
				ComplexType rcd = new Rcd();
				nestedRel = new vtools.dataModel.schema.Element("schema",rcd,null);
				for(int i=0; i<nr; i++){
					vtools.dataModel.schema.Element el = visitNodeElement(nodeChild.item(i));
					 rcd.addField(el);
					 el.setParent(nestedRel);
				}
			}	
			log.debug("The Nested Relationl ");
			log.debug(nestedRel);
			
	}
	
	
	private vtools.dataModel.schema.Element visitNodeElement(Node node){
		vtools.dataModel.schema.Element elemNR = null;
		Atomic typeAtomic = null;
		boolean setOfRcds = false;
		String name = null;
		
		if (node.getNodeName() != "xs:element"){
			log.debug("Not an xs:element");
			return null;
		}
		
		//
		log.debug(" the name attr has value -"+getAttr("name", node));
		log.debug(" the type attr has value -"+getAttr("type", node));
		log.debug(" the maxOcc attr has value -"+getAttr("maxOccurs", node));
		
		//read the attributes of the node
		NamedNodeMap attributes = node.getAttributes();
		if (attributes != null)
			for (int i=0, nrAtt=attributes.getLength(); i<nrAtt; i++){
				Node current = attributes.item(i);
			 
				if (current.getNodeName().equalsIgnoreCase("name")) { 
					name = current.getNodeValue();
				}
				if (current.getNodeName().equalsIgnoreCase("maxOccurs") && 
					current.getNodeValue().equalsIgnoreCase("unbounded")) 
						setOfRcds = true;
			 
				//if the current node contains information regarding its type
				if (current.getNodeName().equalsIgnoreCase("type")){
					if (current.getNodeValue().equalsIgnoreCase("xs:string"))
						typeAtomic = Atomic.STRING ;
					else
						typeAtomic = Atomic.INTEGER;
				}
			}//end-For-attributes
		
		//if the current node contains a type
		if (typeAtomic != null){
			//if an atomic node 
			if (!setOfRcds){
				 elemNR= new vtools.dataModel.schema.Element(name,typeAtomic,null);
			}
		
			//if a complex node which has an atomic type
			if (setOfRcds){
				vtools.dataModel.schema.Element e= new vtools.dataModel.schema.Element(name,typeAtomic,null);
				elemNR= new vtools.dataModel.schema.Element("dummy",new SetOfRcds(),null);
				elemNR.addSubElement(e);
				e.setParent(elemNR);
			}
				 
		}
		//does not contain a type
		else{//read children nodes until you find a complexType node
			Node complexNode = null;
			Node childComplexNode = null;
			ComplexType rcd = null;
	
			NodeList children = node.getChildNodes();
			for(int j=0, nr=children.getLength(); j<nr; j++){
				if (children.item(j).getNodeName().equalsIgnoreCase("xs:complexType"))
					complexNode = children.item(j);
			}
				 
			if (complexNode == null){
					 log.debug("Syntax Error");
			}
			else{//read children nodes until you find a sequence/all node
				childComplexNode = complexNode.getChildNodes().item(0);
				if (childComplexNode.getNodeName().equalsIgnoreCase("xs:choice")){
					log.debug("an element group type Choice");
				}
				if ((childComplexNode.getNodeName().equalsIgnoreCase("xs:sequence")) || 
				    (childComplexNode.getNodeName().equalsIgnoreCase("xs:all"))){
						 if (setOfRcds) rcd = new SetOfRcds();
						 else rcd = new Rcd();
						 elemNR= new vtools.dataModel.schema.Element(name,rcd,null);
						 
						 NodeList child = childComplexNode.getChildNodes();
						 int n=child.getLength();
						 if (n == 1){
							 vtools.dataModel.schema.Element el = visitNodeElement(child.item(0));
							 if (el.getLabel().equalsIgnoreCase("dummy")) {
								 elemNR.setType(el.getType());
								 el.getSubElement(0).setParent(elemNR);
							 }
							 else {
								 rcd.addField(el);
								 el.setParent(elemNR);
							 }
						 }
						 else for(int k=0; k<n; k++){
							 	vtools.dataModel.schema.Element el = visitNodeElement(child.item(k));
							 	rcd.addField(el);
							 	el.setParent(elemNR);
						 	  }//end-for
							 
				}		 
			}				 
		}//end-if-typeAtomic
			 
		//log.debug(elemNR);
		return elemNR;
	}
	
*/	
	

	
}
