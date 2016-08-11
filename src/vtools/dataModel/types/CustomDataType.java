package vtools.dataModel.types;

public class CustomDataType extends DataType {
	
	private String name;
	private String classPath;
	private float percentage;
	private String dbType;
	
	public CustomDataType(String name, String classPath, float percentage, String dbType) {
		super(name, classPath, percentage, dbType);
	}
	
	public CustomDataType() {
		this.name = "";
		this.classPath = "";
		this.percentage = 0.0f;
		this.dbType = "";
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getClassPath() {
		return classPath;
	}

	public void setClassPath(String classPath) {
		this.classPath = classPath;
	}

	public float getPercentage() {
		return percentage;
	}

	public void setPercentage(float percentage) {
		this.percentage = percentage;
	}

	public String getDbType() {
		return dbType;
	}

	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

}
