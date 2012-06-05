package vtools.xml;

import sun.reflect.generics.tree.FieldTypeSignature;
import vtools.dataModel.schema.Element;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Int;
import vtools.dataModel.types.NameTypePair;
import vtools.dataModel.types.Rcd;
import vtools.dataModel.types.Set;
import vtools.dataModel.types.Str;
import vtools.dataModel.types.Type;

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

    public void print(StringBuffer buf, Schema schema, int ident)
    {
        // schemas should be of type record
        buf.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        buf.append("<xs:schema xmlns:xs=\"http://www.w3.org/2001/XMLSchema\" elementFormDefault=\"qualified\" attributeFormDefault=\"unqualified\">\n");
        Rcd rcd = (Rcd)schema.getType();      
        buf.append("<xs:element name=\"" + schema.getLabel() + "\">\n");
        print(buf, rcd, 1);
        buf.append("</xs:element>\n");
        buf.append("</xs:schema>\n");
    }

}
