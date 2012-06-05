package vtools.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import smark.support.MappingScenario;
import smark.support.SMarkElement;
import sun.reflect.generics.tree.FieldTypeSignature;
import vtools.dataModel.expression.BooleanExpression;
import vtools.dataModel.expression.ForeignKey;
import vtools.dataModel.expression.FromClauseList;
import vtools.dataModel.expression.Key;
import vtools.dataModel.expression.Rule;
import vtools.dataModel.schema.Element;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Int;
import vtools.dataModel.types.NameTypePair;
import vtools.dataModel.types.Rcd;
import vtools.dataModel.types.Set;
import vtools.dataModel.types.Str;
import vtools.dataModel.types.Type;

public class XMLWriter
{
    private final String _tab = "   ";

    private static final Comparator<String> ID_COMP = new Comparator<String> () {

		@Override
		public int compare(String l, String r) {
			char[] lChars = l.toCharArray(); 
			char[] rChars = r.toCharArray();
			int pos = 0;
			
			if (l.length() != r.length())
				return l.length() < r.length() ? -1 : 1;
			
			// skip same char
			while(lChars[pos] == rChars[pos++] && pos < l.length())
				;
			pos--;
			
			// both have number
			if (Character.isDigit(lChars[pos]) && Character.isDigit(rChars[pos])) {
				// assume the rest is a number
				int lId = Integer.parseInt(l.substring(pos));
				int rId = Integer.parseInt(r.substring(pos));
				if (lId == rId)
					return 0;
				return lId < rId ? -1 : 1;
			}
			// no order one that is digit first
			else
				return Character.isDigit(lChars[pos]) ? -1 : 1;
		}
    	
    };
    
    public void print(StringBuffer buf, Object o, int ident)
    {
        throw new RuntimeException("DO not know how to print object " + o.getClass().getName());
    }

    public void print(StringBuffer buf, Set set, int ident)
    {
        for (int i = 0, imax = set.size(); i < imax; i++)
        {
            Element ntpair = (Element)set.getField(i);
            print(buf, ntpair, ident);
        }
    }


    public void print(StringBuffer buf, Schema s, int ident)
    {
        //for (int i = 0; i < ident; i++)
        //    buf.append(_tab);
    	Rcd rcd = (Rcd) s.getType();
        for (int i = 0, imax = rcd.size(); i < imax; i++)
        {
            Element ntpair = (Element)rcd.getField(i);
            printRel(buf, ntpair, s, ident + 1);
        }
    }
    
    public void printRel(StringBuffer buf, Element e, Schema s, int ident) {
        Type type = e.getType();
        for (int i = 0; i < ident; i++)
            buf.append(_tab);
        buf.append("<Relation name=\""+e.getLabel()+"\">\n");
        print(buf, (Set) type, ident + 1);
        Rule primaryKey = s.getMyKeyConstraint(e.getLabel());
        if (primaryKey != null)
        	printPrimaryKey(buf, e.getLabel(), primaryKey, ident+1);
        for (int i = 0; i < ident; i++)
            buf.append(_tab);
        buf.append("</Relation>\n");
    }

    public void print(StringBuffer buf, Element e, int ident)
    {
        Type type = e.getType();
        for (int i = 0; i < ident; i++)
            buf.append(_tab);
        //buf.append("<xs:element name=\"" + e.getLabel() + "\" minOccurs=\"0\"");

        if (type instanceof Set)
        {
        	buf.append("<Relation name=\""+e.getLabel()+"\">\n");
            print(buf, (Set) type, ident + 1);
            for (int i = 0; i < ident; i++)
                buf.append(_tab);
            buf.append("</Relation>\n");
        }
        else if (type instanceof Rcd)
        {
        	buf.append("<xs:element name=\"" + e.getLabel() + "\" minOccurs=\"0\"");
            buf.append(" maxOccurs=\"1\">\n");
            print(buf, (Rcd) type, ident + 1);
            for (int i = 0; i < ident; i++)
                buf.append(_tab);
            buf.append("</xs:element>\n");
        }
        else if (type instanceof Int)
        {
        	buf.append("<Attr><Name>" + e.getLabel() + "</Name><DataType>INT8</DataType></Attr>\n");
        }
        else if (type instanceof Str)
        {
        	buf.append("<Attr><Name>" + e.getLabel() + "</Name><DataType>TEXT</DataType></Attr>\n");
        }
        else throw new RuntimeException("Do not know how to handle type " + type.getClass().getName());
    }

    public void print(StringBuffer buf, MappingScenario scenario, int ident, String instancePathPrefix)
    {
    	Schema source = scenario.getSource();
    	Schema target = scenario.getTarget();
    	Map<String, String> correspondences = scenario.getCorrespondences();
    	Map<String, ArrayList<String>> mappings2Correspondences = scenario.getMappings2Correspondences();
    	Map<String, HashMap<String, ArrayList<Character>>> mappings2Sources = scenario.getMappings2Sources();
    	Map<String, HashMap<String, ArrayList<Character>>> mappings2Targets = scenario.getMappings2Targets();
    	Map<String, ArrayList<String>> transformationMappings = scenario.getTransformation2Mappings();
    	Map<String, String> transformationCode = scenario.getTransformationCode();
    	Map<String, String> transformationRelName = scenario.getTransformationRelName();
    	
        // schemas should be of type record
        buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        buf.append("<this:MappingScenario xmlns:this=\"org/vagabond/xmlmodel\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">\n");
        buf.append("<Schemas>\n");
        print(buf, source, ident, 0);
        print(buf, target, ident, 1);
        buf.append("</Schemas>\n");
        buf.append("<Correspondences>\n");
        print(buf, correspondences);
        buf.append("</Correspondences>\n");
        buf.append("<Mappings>\n");
        printMappings(buf, mappings2Correspondences, mappings2Sources, mappings2Targets);
        buf.append("</Mappings>\n");
        buf.append("<Transformations>\n");
        printTransformations(buf, transformationMappings, transformationCode, transformationRelName);
        buf.append("</Transformations>\n");
        printConnectionInfo(buf);
        printData(buf, scenario.getSource(), instancePathPrefix);
        buf.append("</this:MappingScenario>");
    }
    
    public void printTransformations(StringBuffer buf, 
    		Map<String, ArrayList<String>> tM,
    		Map<String, String> tC,
    		Map<String, String> tR) {
    	List<String> sortedKeys = new ArrayList<String> (tC.keySet());
    	Collections.sort(sortedKeys, ID_COMP);
    	
    	for (String tId: sortedKeys) {
			ArrayList<String> mappingList = tM.get(tId);
			buf.append(_tab+"<Transformation id=\""+tId+"\" creates=\""+tR.get(tId)+"\">\n");
			if (mappingList != null) {
				buf.append(_tab+_tab+"<Implements>");
				for (String mapping: mappingList) {
					buf.append("<Mapping>"+mapping+"</Mapping>");
				}
				buf.append("</Implements>\n");
			}
			// escape entities for XML
			String tCode = tC.get(tId);
			tCode = escapeXMLChars(tCode);
			buf.append(_tab+_tab+"<Code>\n");
			buf.append(tCode+"\n");
			buf.append(_tab+_tab+"</Code>\n");
			buf.append(_tab+"</Transformation>\n");
		}
    	
//    	
//    	if (!tM.isEmpty()) {
//    		for (Map.Entry<String, ArrayList<String>> entry : tM.entrySet()) {
//    			String tId = entry.getKey();
//    			ArrayList<String> mappingList = entry.getValue();
//    			buf.append(_tab+"<Transformation id=\""+tId+"\" creates=\""+tR.get(tId)+"\">\n");
//    			if (mappingList != null) {
//    				buf.append(_tab+_tab+"<Implements>");
//    				for (String mapping: mappingList) {
//    					buf.append("<Mapping>"+mapping+"</Mapping>");
//    				}
//    				buf.append("</Implements>\n");
//    			}
//    			String tCode = tC.get(tId);
//    			buf.append(_tab+_tab+"<Code>\n");
//    			buf.append(tCode+"\n");
//    			buf.append(_tab+_tab+"</Code>\n");
//    			buf.append(_tab+"</Transformation>\n");
//    		}
//    	} else {//TODO isn't this unnessesary involved?
//    		for (Map.Entry<String, String> entry : tR.entrySet()) {
//    			String tId = entry.getKey();
//    			buf.append(_tab+"<Transformation id=\""+tId+"\" creates=\""+tR.get(tId)+"\">\n");
//    			buf.append(_tab+_tab+"<Implements>");
//    			buf.append("<Mapping></Mapping>");
//    			buf.append("</Implements>\n");
//    			String tCode = tC.get(tId);
//    			buf.append(_tab+_tab+"<Code>\n");
//    			buf.append(tCode+"\n");
//    			buf.append(_tab+_tab+"</Code>\n");
//    			buf.append(_tab+"</Transformation>\n");
//    		}
//    	}
    	
    }
    
    private String escapeXMLChars(String tCode) {
		StringBuffer result = new StringBuffer();
		char[] chars = tCode.toCharArray();
		for(int i = 0; i < chars.length; i++) {
			switch(chars[i]) {
			case '<':
				result.append("&lt;");
				break;
			case '>':
				result.append("&gt;");
				break;
			case '"':
				result.append("&quot;");
				break;
			case '&':
				result.append("&amp;");
				break;
			case '\'':
				result.append("&apos;");
				break;
			default:
				result.append(chars[i]);
			}
		}
		
		return result.toString();
	}

	public void printMappings(StringBuffer buf,
    		Map<String, ArrayList<String>> mC,
    		Map<String, HashMap<String, ArrayList<Character>>> mS,
    		Map<String, HashMap<String, ArrayList<Character>>> mT) {
    	List<String> sortedKeys = new ArrayList<String> (mC.keySet());
    	Collections.sort(sortedKeys, ID_COMP);
    	
    	for (String mId: sortedKeys) {
    		ArrayList<String> corrList = mC.get(mId);
    		buf.append(_tab+"<Mapping id=\""+mId+"\">\n");
    		buf.append(_tab+_tab+"<Uses>\n");
    		for (String attr: corrList) {
    			buf.append(_tab+_tab+_tab+"<Correspondence>"+attr+"</Correspondence>\n");
    		}
    		buf.append(_tab+_tab+"</Uses>\n");
    		buf.append(_tab+_tab+"<Foreach>\n");
    		Map<String, ArrayList<Character>> sourceList = mS.get(mId);
        	for (Map.Entry<String, ArrayList<Character>> sourceEntry : sourceList.entrySet()) {
        		String sourceName = sourceEntry.getKey();
    			buf.append(_tab+_tab+_tab+"<Atom tableref=\""+sourceName+"\">");
        		ArrayList<Character> attrList = sourceEntry.getValue();
        		for (Character attr : attrList) {
        			buf.append("<Var>"+attr+"</Var>");
        		}
        		buf.append("</Atom>\n");
        	}
    		buf.append(_tab+_tab+"</Foreach>\n");
    		buf.append(_tab+_tab+"<Exists>\n");
    		Map<String, ArrayList<Character>> targetList = mT.get(mId);
        	for (Map.Entry<String, ArrayList<Character>> targetEntry : targetList.entrySet()) {
        		String targetName = targetEntry.getKey();
    			buf.append(_tab+_tab+_tab+"<Atom tableref=\""+targetName+"\">");
        		ArrayList<Character> attrList = targetEntry.getValue();
        		for (Character attr : attrList) {
        			buf.append("<Var>"+attr+"</Var>");
        		}
        		buf.append("</Atom>\n");
        	}
    		buf.append(_tab+_tab+"</Exists>\n");
    		buf.append(_tab+"</Mapping>\n");
    	}
    }
    
    
    private void print(StringBuffer buf, Map<String, String> correspondences) {
    	List<String> sortedKeys = new ArrayList<String> (correspondences.keySet());
    	Collections.sort(sortedKeys, ID_COMP);
    	
    	for (String key : sortedKeys) {
    		String cId = key;
    		String correspondence = correspondences.get(key);
    		String[] corrArr = correspondence.split("=");
    		String from = corrArr[0];
    		String[] fromRelAttr = from.split("\\.");
    		String to = corrArr[1];
    		String[] toRelAttr = to.split("\\.");
    		buf.append(_tab+"<Correspondence id=\""+cId+"\">\n");
    		buf.append(_tab+_tab+"<From tableref=\"");
    		buf.append(fromRelAttr[0]);
    		buf.append("\"><Attr>");
    		buf.append(fromRelAttr[1]);
    		buf.append("</Attr></From>\n");
    		buf.append(_tab+_tab+"<To tableref=\"");
    		buf.append(toRelAttr[0]);
    		buf.append("\"><Attr>");
    		buf.append(toRelAttr[1]);
    		buf.append("</Attr></To>\n");
    		buf.append(_tab+"</Correspondence>\n");
    	}
    }
    
    private void printConnectionInfo(StringBuffer buf) {
    	buf.append("<ConnectionInfo>\n");
    	buf.append(_tab);
    	buf.append("<Host>localhost</Host>\n");
    	buf.append(_tab);
    	buf.append("<DB>tramptest</DB>\n");
    	buf.append(_tab);
    	buf.append("<User>lordpretzel</User>\n");
    	buf.append(_tab);
    	buf.append("<Password/>\n");
    	buf.append("</ConnectionInfo>\n");
    }
    
    private void printData(StringBuffer buf, Schema schema, String instancePathPrefix) {
    	buf.append("<Data>\n");
    	for(int i = 0; i < schema.size(); i++) {
    		SMarkElement rootSetElt = (SMarkElement) schema.getSubElement(i);
    		printDataSource(buf, rootSetElt, instancePathPrefix);
    	}
    	buf.append("</Data>\n");
    }
    
    private void printDataSource(StringBuffer buf, SMarkElement rel, String path) {
    	buf.append("\t<InstanceFile name=\"person\">\n" + 
    			"\t\t<Path>" + path +"</Path>\n" +
    			"\t\t<FileName>" + rel.getLabel() + ".csv</FileName>\n" +
    			"\t\t<ColumnDelim>|</ColumnDelim>\n" +
    			"\t</InstanceFile>\n");
    }
    
    private void printIdents(StringBuffer buf, int ident) {
        for (int i = 0; i < ident; i++)
            buf.append(_tab);
   	
    }
    
    private void printForeignKeys(StringBuffer buf, ArrayList<Rule> constraints, int ident) {
    	int i = 0;
    	for (Rule constraint : constraints) {
    		// the foreign keys are both ways. we need only one of them.
    		//TODO however the direction is not irrelevant, we need the right one
    		if (i % 2 == 0) {
    			printIdents(buf, ident);
    			String id = getTableName(constraint.getLeftTerms()) + "_"
    				+ getTableName(constraint.getRightTerms()) + Integer.toString(i);
    			buf.append("<ForeignKey id=\"" + id + "\">\n");
    			printIdents(buf, ident);
    			buf.append(_tab+"<From tableref=\""+getTableName(constraint.getLeftTerms())+"\">");
    			buf.append("<Attr>"+getAttributes(constraint.getRightConditions())[1]+"</Attr>");
    			buf.append("</From>\n");
    			printIdents(buf, ident);
    			buf.append(_tab+"<To tableref=\""+getTableName(constraint.getRightTerms())+"\">");
    			buf.append("<Attr>"+getAttributes(constraint.getRightConditions())[0]+"</Attr>");
    			buf.append("</To>\n");
    			printIdents(buf, ident);
    			buf.append("</ForeignKey>\n");
    		}
    		i++;
    	}
    }
    
    private void printPrimaryKey(StringBuffer buf, String relName, Rule constraint, int ident) {
		printIdents(buf, ident);
		String id = relName + "_" + "PrimaryKey";
		buf.append("<PrimaryKey id=\"" + id + "\">\n");
		printIdents(buf, ident);
		buf.append(_tab+"<Attr>"+getAttributes(constraint.getLeftConditions())[0]+"</Attr>\n");
		printIdents(buf, ident);
		buf.append("</PrimaryKey>\n");
    	
    }
    
    private String getTableName(FromClauseList cl) {
    	int strLength = cl.toString().length();
    	return cl.toString().substring(1, strLength-3);
    }
    
    private String[] getAttributes(BooleanExpression be) {
    	String[] terms = be.toString().split("=");
    	String fromAttribute = terms[0].substring(3);
    	String toAttribute = terms[1].substring(3);
    	String[] retVal = new String[2];
    	retVal[0]=fromAttribute;
    	retVal[1]=toAttribute;
    	return retVal;
    }
    
    private void print(StringBuffer buf, Schema s, int ident, int schemaType) {
        if (schemaType ==0) buf.append(_tab+"<SourceSchema>\n");
        else buf.append(_tab+"<TargetSchema>\n");
        print(buf, s, 1);
        ArrayList<Rule> foreignKeys = s.getForeignKeyConstraints();
        printForeignKeys(buf, foreignKeys, ident+2);
        if (schemaType ==0) buf.append(_tab+"</SourceSchema>\n");
        else buf.append(_tab+"</TargetSchema>\n");
    }
}
