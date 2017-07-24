/**
 * 
 */
package vtools.dataModel.types;

import java.util.Set;

/**
 * @author lord_pretzel
 *
 */
public class UnionDataType extends DataType {

	private Set<DataType> dts;
	
	public UnionDataType (Set<DataType> dts) {
		this.setDts(dts);
	}

	public Set<DataType> getDts() {
		return dts;
	}

	public void setDts(Set<DataType> dts) {
		this.dts = dts;
	}
	
	public String getName() {
		StringBuilder st = new StringBuilder();
		st.append("[");
		for(DataType dt: dts) {
			st.append(dt.getName() + "||");
		}
		st.append("]");
		return st.toString();
	}
	
	public boolean equals(Object o) {
		UnionDataType d;
		if (!(o instanceof UnionDataType))
			return false;
		
		d = (UnionDataType) o;
		
		for(DataType ch: dts) {
			if (!d.dts.contains(ch))
				return false;
		}

		for(DataType ch: d.dts) {
			if (!dts.contains(ch))
				return false;
		}
		
		return true;
	}
	
	public int hashCode () {
		return dts.hashCode();
	}
	
}
