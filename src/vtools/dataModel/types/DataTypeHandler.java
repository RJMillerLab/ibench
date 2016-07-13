/**
 * 
 */
package vtools.dataModel.types;

import java.util.List;
import java.util.Map;
import java.util.Random;

/**
 * @author lord_pretzel
 *
 */
public class DataTypeHandler {
	
	private List<DataType> types;
	private Map<String,DataType> nameToDTMap;
	private float[] percentages;
	private int numDTs;
	
	private static DataTypeHandler inst = new DataTypeHandler();
	
	public static DataTypeHandler getInst () {
		return inst;
	}
	
	private DataTypeHandler () {
		
	}
	
	public Atomic getRandomDT (Random randGen) {
		float r = randGen.nextFloat();
		
		for(int i = 0; i < getNumDTs(); i++) {
			if (r < getPercentages()[i])
				return getTypes().get(i);
		}
		return Atomic.STRING;
	}

	public List<DataType> getTypes() {
		return types;
	}

	public void setTypes(List<DataType> types) {
		this.types = types;
	}

	public Map<String,DataType> getNameToDTMap() {
		return nameToDTMap;
	}

	public void setNameToDTMap(Map<String,DataType> nameToDTMap) {
		this.nameToDTMap = nameToDTMap;
	}

	public float[] getPercentages() {
		return percentages;
	}

	public void setPercentages(float[] percentages) {
		this.percentages = percentages;
	}

	public int getNumDTs() {
		return numDTs;
	}

	public void setNumDTs(int numDTs) {
		this.numDTs = numDTs;
	}
	
	
	
}
