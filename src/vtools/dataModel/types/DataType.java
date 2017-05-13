package vtools.dataModel.types;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class DataType extends Atomic implements Visitable, Cloneable {
	
	private String name;
	private String classPath;
	private float percentage;
	private String dbType;

	public DataType(String name, String classPath, float percentage, String dbType) {
		this.name = name;
		this.classPath = classPath;
		this.percentage = percentage;
		this.dbType = dbType;
	}
	
	public DataType(String name, String classPath, float percentage) {
		this.name = name;
		this.classPath = classPath;
		this.percentage = percentage;
		this.dbType = null;
	}
	
	public DataType() { 
		this.name = "";
		this.classPath = "";
		this.percentage = 0.0f;
	}
	
	public Visitor getPrintVisitor()
    {
        return StringPrinter.StringPrinter;
    }
    
    public DataType clone()
    {
        if (this == Type.USERCLASSTYPE)
            return this;
        DataType dataType = (DataType) super.clone();
        return dataType;
    }
    
    public boolean equals(Object o)
    {
        if (!(o instanceof DataType))
            return false;
        if (!super.equals(o))
            return false;
        return true;
    }

	public float getPercentage() {
		return percentage;
	}

	public void setPercentage(float percentage) {
		this.percentage = percentage;
	}

	public String getClassPath() {
		return classPath;
	}

	public void setClassPath(String classPath) {
		this.classPath = classPath;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}
	
	public String toString () {
    	return "DataType: <" + name + "," + classPath + "," + percentage + "," + dbType + ">";
    }
}
