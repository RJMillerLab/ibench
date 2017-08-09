/**
 * 
 */
package tresc.benchmark.rename;


import smark.support.MappingScenario;

/**
 * @author lord_pretzel
 *
 */
public class RenameManager {

	public enum RenamerType {
		None,
		AllLowerCase
	}
	
	private IRenameHandler r;
	private RenamerType rType;
	
	public static RenameManager inst = new RenameManager();
	
	public RenameManager () {
		setR(new NoRename());
	}

	public IRenameHandler getR() {
		return r;
	}

	public void setR(RenamerType typ) {
		this.rType = typ;
		this.r = instantiateType(typ);
	}
	
	/**
	 * @param typ
	 * @return
	 */
	private IRenameHandler instantiateType(RenamerType typ) {
		switch (typ) {
		case AllLowerCase:
			return new AllLowerCaseRenamer();
		case None:
		default:
			return new NoRename();
		}
	}

	public void setR(IRenameHandler r) {
		this.r = r;
	}
	
	public void applyRenaming (MappingScenario t) {
		r.applyRenaming(t);
	}
	
}
