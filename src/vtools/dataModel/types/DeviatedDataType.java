/**
 * 
 */
package vtools.dataModel.types;

/**
 * @author lord_pretzel
 *
 */
public class DeviatedDataType extends DataType {

	private DataType original;
	private int deviation;
	
	public DeviatedDataType (DataType orig, int dev) {
		setOriginal(orig);
		setDeviation(dev);
	}

	public int getDeviation() {
		return deviation;
	}

	public void setDeviation(int deviation) {
		this.deviation = deviation;
	}

	public DataType getOriginal() {
		return original;
	}

	public void setOriginal(DataType original) {
		this.original = original;
	}
	
	public String getName() {
		return "DEV-" + original.getName() + "-" + deviation;
	}
	
	public boolean equals (Object o) {
		if (!super.equals(o))
			return false;
		if (!(o instanceof DeviatedDataType))
			return false;
		
		DeviatedDataType d = (DeviatedDataType) o;
		
		if (!original.equals(d.original))
			return false;
		if (deviation != d.deviation)
			return false;
		return true;
	}
	
	public String toString () {
		return "DEV-" + original.toString() + "-" + deviation;
	}
	
}
