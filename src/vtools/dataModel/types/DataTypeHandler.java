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
	private float[] probabilities;
	
	private static DataTypeHandler inst = new DataTypeHandler();
	
	public static DataTypeHandler getInst () {
		return inst;
	}
	
	private DataTypeHandler () {
		
	}
	
	public void setProbabilities() {
		probabilities[0] = percentages[0]/100;
		for (int k = 0; k < numDTs; k++) {
			probabilities[k] = probabilities[k-1] + percentages[k]/100;
		}
	}
	
	public Atomic getRandomDT (Random randGen) {
		setProbabilities();
		float r = randGen.nextFloat();
		for(int i = 0; i < probabilities.length; i++) {
			if (r < probabilities[i])
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
