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
		
		for(int i = 0; i < numDTs; i++) {
			if (r < percentages[i])
				return types.get(i);
		}
		return Atomic.STRING;
	}
	
	
	
}
