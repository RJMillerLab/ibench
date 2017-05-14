/**
 * 
 */
package vtools.dataModel.types;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.apache.log4j.Logger;

/**
 * @author lord_pretzel
 *
 */
public class DataTypeHandler {
	
	static Logger log = Logger.getLogger(DataTypeHandler.class);
	
	private List<DataType> types;
	private Map<String,DataType> nameToDTMap;
	private double[] percentages;
	private int numDTs;
	private double[] probabilities;
//	private String[] typesNamesOrder;
	private Map<String, String[]> typesNamesNewOrder;
	
	private static DataTypeHandler inst = new DataTypeHandler();
	
	public static DataTypeHandler getInst () {
		return inst;
	}
	
	private DataTypeHandler () {
		typesNamesNewOrder = new HashMap<String,String[]>();
	}
	
			

	public void setProbabilities() {
		if (percentages.length > 0)
			log.debug(percentages[0]);
		probabilities = new double[numDTs];
		if (probabilities.length > 0) {
			probabilities[0] = (percentages[0]/100.0);
			for (int k = 1; k < numDTs; k++) {
				probabilities[k] = probabilities[k-1] + (percentages[k]/100.0);
			}
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
	

	public String getDbType(String dataType) {
		DataType data = 
				this.nameToDTMap.get(dataType);
		return data.getDbType();
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

	public double[] getPercentages() {
		return percentages;
	}

	public void setPercentages(double[] percentages) {
		this.percentages = percentages;
	}

	public int getNumDTs() {
		return numDTs;
	}

	public void setNumDTs(int numDTs) {
		this.numDTs = numDTs;
	}

	public List<CustomDataType> getAllCustomTypes () {
		List<CustomDataType> list = new ArrayList<CustomDataType>();
		for (DataType dt : types) {
			if (dt instanceof CustomDataType) {
				list.add((CustomDataType)dt);
			}
		}
		return list;
	}
	
	public List<DataType> getAllNonCSVDTs () {
		List<DataType> result = new ArrayList<DataType> ();
		for (DataType dt : types) {
			if (! (dt instanceof CustomDataType)) {
				result.add(dt);
			}
		}
		return result;
	}
	
	public boolean hasTypesNamesOrder (boolean source, String tableName) {
		String schema = source ? "Source." : "Target.";
		String fullName = schema + tableName;
		return typesNamesNewOrder.containsKey(fullName);
	}
	
	public String[] getTypesNamesOrder (boolean source, String tableName) {
		String schema = source ? "Source." : "Target.";
		String fullName = schema + tableName;
		return typesNamesNewOrder.get(fullName);
	}
	
	public void setTypesNamesOrder (boolean source, String tableName, String[] order) {
		String schema = source ? "Source." : "Target.";
		String fullName = schema + tableName;
		typesNamesNewOrder.put(fullName, order);
		log.info(fullName + ":" + Arrays.toString(order));
	}

	public String toString() {
		StringBuilder st = new StringBuilder();
		
		st.append("num DTs: " + numDTs + "\n\n");
		
		for(int i = 0; i < numDTs; i++) {
			DataType dt = types.get(i);
			String name = dt.getName() == null ? "" : dt.getName();
			
			st.append("\tDataType <" + name + "> with " + percentages[i] + " probability:\n" + dt.toString() +"\n\n");
		}
		
		
		return st.toString();
	}
	
}
