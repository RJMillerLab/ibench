package vtools.dataModel.types;

public class CustomCSVDataType extends DataType {
	
	private String csvFile;
	private String attrName;
	
	public CustomCSVDataType(String name, String classPath, float percentage, String dbType, String csvFile, String attrName) {
		super(name, classPath, percentage, dbType);
		this.setCsvFile(csvFile);
		this.setAttrName(attrName);
	}
	
	public CustomCSVDataType() {
		super.name = "";
		super.classPath = "";
		super.percentage = 0.0f;
		super.dbType = "";
		super.jarPath = null;
	}

	public String getCsvFile() {
		return csvFile;
	}

	public void setCsvFile(String csvFile) {
		this.csvFile = csvFile;
	}

	public String getAttrName() {
		return attrName;
	}

	public void setAttrName(String attrName) {
		this.attrName = attrName;
	}

	public void inferName () {
		name = "CSV_" + csvFile + "@" + attrName;
	}
	
	public boolean equals (Object o) {
		if (!super.equals(o))
			return false;
		if (!(o instanceof CustomCSVDataType))
			return false;
		CustomCSVDataType d = (CustomCSVDataType) o;
		
		if ((csvFile == null && d.csvFile != null) || (csvFile != null && !csvFile.equals(d.csvFile)))
			return false;
		if ((attrName == null && d.attrName != null) || (attrName != null && !attrName.equals(d.attrName)))
			return false;
		
		return true;
	}
	
}
