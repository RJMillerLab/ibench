package vtools.xml;

import org.vagabond.xmlmodel.ForeignKeyType;
import org.vagabond.xmlmodel.RelationType;

import smark.support.MappingScenario;
import vtools.dataModel.schema.Element;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Int;
import vtools.dataModel.types.Rcd;
import vtools.dataModel.types.Set;
import vtools.dataModel.types.Str;
import vtools.dataModel.types.Type;

//MN ADD four methods to print source/target primary and foreign keys - 3 April 2014
public class XSDWriter
{
    private final String _tab = "   ";

    public void print(StringBuffer buf, Object o, int ident)
    {
        throw new RuntimeException("DO not know how to print object " + o.getClass().getName());
    }

    public void print(StringBuffer buf, Set set, int ident)
    {
        for (int i = 0; i < ident; i++)
            buf.append(_tab);
        buf.append("<xs:complexType>\n");
        for (int i = 0; i < ident + 1; i++)
            buf.append(_tab);
        buf.append("<xs:sequence>\n");
        for (int i = 0, imax = set.size(); i < imax; i++)
        {
            Element ntpair = (Element)set.getField(i);
            print(buf, ntpair, ident + 2);
        }
        for (int i = 0; i < ident + 1; i++)
            buf.append(_tab);
        buf.append("</xs:sequence>\n");
        for (int i = 0; i < ident; i++)
            buf.append(_tab);
        buf.append("</xs:complexType>\n");
    }

    public void print(StringBuffer buf, Rcd rcd, int ident)
    {
        for (int i = 0; i < ident; i++)
            buf.append(_tab);
        buf.append("<xs:complexType>\n");
        for (int i = 0; i < ident + 1; i++)
            buf.append(_tab);
        buf.append("<xs:sequence>\n");
        for (int i = 0, imax = rcd.size(); i < imax; i++)
        {
            Element ntpair = (Element)rcd.getField(i);
            print(buf, ntpair, ident + 2);
        }
        for (int i = 0; i < ident + 1; i++)
            buf.append(_tab);
        buf.append("</xs:sequence>\n");
        for (int i = 0; i < ident; i++)
            buf.append(_tab);
        buf.append("</xs:complexType>\n");
    }

    public void print(StringBuffer buf, Element e, int ident)
    {
        Type type = e.getType();
        for (int i = 0; i < ident; i++)
            buf.append(_tab);
        buf.append("<xs:element name=\"" + e.getLabel() + "\" minOccurs=\"0\"");

        if (type instanceof Set)
        {
            buf.append(" maxOccurs=\"unbounded\">\n");
            print(buf, (Set) type, ident + 1);
            for (int i = 0; i < ident; i++)
                buf.append(_tab);
            buf.append("</xs:element>\n");
        }
        else if (type instanceof Rcd)
        {
            buf.append(" maxOccurs=\"1\">\n");
            print(buf, (Rcd) type, ident + 1);
            for (int i = 0; i < ident; i++)
                buf.append(_tab);
            buf.append("</xs:element>\n");
        }
        else if (type instanceof Int)
        {
            buf.append(" maxOccurs=\"1\" type=\"xs:integer\"/>\n");
        }
        else if (type instanceof Str)
        {
            buf.append(" maxOccurs=\"1\" type=\"xs:string\"/>\n");
        }
        else throw new RuntimeException("Do not know how to handle type " + type.getClass().getName());
    }

    //MN prints source primary keys
    public void printSourcePK(StringBuffer buf, MappingScenario scenario) throws Exception
    {
    	for (RelationType r : scenario.getDoc().getSchema(true).getRelationArray())
    	{
    		String[] pkAttrs = scenario.getDoc().getPK(r.getName(), true);
    		if(pkAttrs != null)
    		{
    			for(int j=0; j<pkAttrs.length; j++)
    				buf.append("<xs:key name=\""+ r.getName() + pkAttrs[j] + "\"><xs:selector xpath=\"" + "." + "/" + r.getName() + "\"/><xs:field xpath=\"" + pkAttrs[j] + "\"/></xs:key>\n");
    		}
    	}
    }
    
    //MN prints target primary keys
    public void printTargetPK(StringBuffer buf, MappingScenario scenario) throws Exception
    {
    	for (RelationType r : scenario.getDoc().getSchema(false).getRelationArray())
    	{
    		String[] pkAttrs = scenario.getDoc().getPK(r.getName(), false);
    		if(pkAttrs != null)
    		{
    			for(int j=0; j<pkAttrs.length; j++)
    				buf.append("<xs:key name=\""+ r.getName() + pkAttrs[j] + "\"><xs:selector xpath=\"" + "." + "/" + r.getName() + "\"/><xs:field xpath=\"" + pkAttrs[j] + "\"/></xs:key>\n");
    		}
    	}
    }
    
    //MN prints source foreign keys
    public void printSourceFK(StringBuffer buf, MappingScenario scenario)
    {
    	for (RelationType from : scenario.getDoc().getSchema(true).getRelationArray())
    		for (RelationType to : scenario.getDoc().getSchema(true).getRelationArray())
    		{
    			ForeignKeyType[] fkAttrs = scenario.getDoc().getFKs(from.getName(), to.getName(), true);
    			if(fkAttrs != null)
    			{
    				for(int j=0; j<fkAttrs.length; j++)
    					buf.append("<xs:keyref refer=\"" + to.getName() + fkAttrs[j].getTo().toString().split("<Attr>")[1].split("<")[0] + "\" name=\"" + "fk" + to.getName() + fkAttrs[j].getTo().toString().split("<Attr>")[1].split("<")[0] + "\"><xs:selector xpath=\"./" + from.getName() + "\"/><xs:field xpath=\"" + fkAttrs[j].getFrom().toString().split("<Attr>")[1].split("<")[0] + "\"/></xs:keyref>\n");
    			}
    		}
    }
    
    //MN prints target foreign keys 
    public void printTargetFK(StringBuffer buf, MappingScenario scenario)
    {
    	for (RelationType from : scenario.getDoc().getSchema(false).getRelationArray())
    		for (RelationType to : scenario.getDoc().getSchema(false).getRelationArray())
    		{
    			ForeignKeyType[] fkAttrs = scenario.getDoc().getFKs(from.getName(), to.getName(), false);
    			if(fkAttrs != null)
    			{
    				for(int j=0; j<fkAttrs.length; j++)
    					buf.append("<xs:keyref refer=\"" + to.getName() + fkAttrs[j].getTo().toString().split("<Attr>")[1].split("<")[0] + "\" name=\"" + "fk" + to.getName() + fkAttrs[j].getTo().toString().split("<Attr>")[1].split("<")[0] + "\"><xs:selector xpath=\"./" + from.getName() + "\"/><xs:field xpath=\"" + fkAttrs[j].getFrom().toString().split("<Attr>")[1].split("<")[0] + "\"/></xs:keyref>\n");
    			}
    		}
    }
    
    public void printSource(StringBuffer buf, MappingScenario scenario, String name, int ident) throws Exception
    {
        // schemas should be of type record
        buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        buf.append("<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\n");
        Rcd rcd = (Rcd)scenario.getSource().getType();      
        buf.append("<xs:element name=\"" + name + "\">\n");
        print(buf, rcd, 1);
        //MN ADD two methods to print source primary and foreign keys in XSD file - 3 April 2014
        printSourcePK(buf, scenario);
        printSourceFK(buf, scenario);
        buf.append("</xs:element>\n");
        buf.append("</xs:schema>\n");
    }
    
    public void printTarget(StringBuffer buf, MappingScenario mapping, String name, int ident) throws Exception
    {
        // schemas should be of type record
        buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        buf.append("<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\n");
        Rcd rcd = (Rcd)mapping.getTarget().getType();      
        buf.append("<xs:element name=\"" + name + "\">\n");
        print(buf, rcd, 1);
        //MN ADD two methods to print target primary and foreign keys in XSD file - 3 April 2014
        printTargetPK(buf, mapping);
        printTargetFK(buf, mapping);
        buf.append("</xs:element>\n");
        buf.append("</xs:schema>\n");
    }

}
