package vtools.dataModel.types;

import java.io.File;

import vtools.visitor.Visitable;
import vtools.visitor.Visitor;

public class DataType extends Atomic implements Visitable, Cloneable {
	
	protected String name;
	protected String classPath;
	protected float percentage;
	protected String dbType;
	protected String jarPath;

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
    
    public boolean equals(Object o) {
    		DataType d;
        if (!(o instanceof DataType))
            return false;
        if (!super.equals(o))
            return false;
        
        d = (DataType) o;
        if ((name == null && d.name != null) || (name != null && !name.equals(d.name)))
        		return false;
        if ((classPath == null && d.classPath != null) || (classPath != null && !classPath.equals(d.classPath)))
        		return false;
        if (!(percentage == d.percentage))
        		return false;
        if ((dbType == null && d.dbType != null) || (dbType != null && !dbType.equals(d.dbType)))
        		return false;
        if ((jarPath == null && d.jarPath != null) || (jarPath != null && !jarPath.equals(d.jarPath)))
        		return false;
        
        return true;
    }
    
    public int hashCode() {
    		return name.hashCode() | classPath.hashCode();
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
		String nameS, classPathS, dbTypeS, jarPathS;
		
		nameS = name == null ? "null" : name;
		classPathS = classPath == null ? "null" : classPath;
		dbTypeS = dbType == null ? "null" : dbType;
		jarPathS = jarPath == null ? "null" : jarPath;
		
    	return "DataType: <" + name + "," + classPath + "," + percentage + "," + dbType + "," + jarPath + ">";
    }

	public String getJarPath() {
		return jarPath;
	}

	public void setJarPath(String jarPath) {
		this.jarPath = jarPath;
	}
}
