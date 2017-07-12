package vtools.dataModel.types;

public class CustomDataType extends DataType {
	
	
	public CustomDataType(String name, String classPath, float percentage, String dbType) {
		super(name, classPath, percentage, dbType);
	}
	
	public CustomDataType() {
		super.name = "";
		super.classPath = "";
		super.percentage = 0.0f;
		super.dbType = "";
		super.jarPath = null;
	}

}
