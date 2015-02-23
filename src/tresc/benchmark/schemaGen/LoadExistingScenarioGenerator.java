/**
 * 
 */
package tresc.benchmark.schemaGen;

import org.apache.xmlbeans.XmlCursor;
import org.apache.xmlbeans.XmlObject;
import org.vagabond.mapping.model.MapScenarioHolder;
import org.vagabond.xmlmodel.AttrDefType;
import org.vagabond.xmlmodel.CorrespondenceType;
import org.vagabond.xmlmodel.FDType;
import org.vagabond.xmlmodel.ForeignKeyType;
import org.vagabond.xmlmodel.FunctionType;
import org.vagabond.xmlmodel.MappingType;
import org.vagabond.xmlmodel.RelAtomType;
import org.vagabond.xmlmodel.RelationType;
import org.vagabond.xmlmodel.SKFunction;
import org.vagabond.xmlmodel.SchemaType;
import org.vagabond.xmlmodel.StringRefType;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.utils.Utils;
import vtools.dataModel.expression.ForeignKey;

/**
 * @author lord_pretzel
 *
 */
public class LoadExistingScenarioGenerator extends AbstractScenarioGenerator {

	private MapScenarioHolder s;
	private String scenName;
	private String namingPrefix; // prepended to every scenario element ot make the names unique
	
	public LoadExistingScenarioGenerator (MapScenarioHolder scen, String scenName) {
		this.s = scen;
		this.scenName = scenName;
	}
	
	@Override
	public void init(Configuration configuration,
			MappingScenario mappingScenario) {
		super.init(configuration, mappingScenario);
		int pos = configuration.getLoadScenarioNames().indexOf(scenName);
		repetitions = configuration.getNumLoadScenarioInsts()[pos];
	}
	
    protected void initPartialMapping() 
    {
    	super.initPartialMapping();
    	// create random naming prefix
    	namingPrefix = randomRelName(0) + "_";
    }

	
	/* (non-Javadoc)
	 * @see tresc.benchmark.schemaGen.AbstractScenarioGenerator#genCorrespondences()
	 */
	@Override
	protected void genCorrespondences() {
		for(CorrespondenceType c: s.getDocument().getMappingScenario().getCorrespondences().getCorrespondenceArray()) {
			CorrespondenceType newC = (CorrespondenceType) c.copy();
			int i = 0;
			
			newC.setId(namingPrefix + newC.getId());
			newC.getFrom().setTableref(namingPrefix + newC.getFrom().getTableref());
			for (String a : newC.getFrom().getAttrArray()) {
				newC.getFrom().setAttrArray(i++, namingPrefix + a);
				i++;
			}

			newC.addNewTo();
			newC.getTo().setTableref(namingPrefix + newC.getTo().getTableref());
			i = 0;
			for (String a : newC.getTo().getAttrArray()) {
				newC.getTo().setAttrArray(i++, namingPrefix + a);
				i++;
			}
			
			fac.addCorrespondence(newC);
		}
	}

	/* (non-Javadoc)
	 * @see tresc.benchmark.schemaGen.AbstractScenarioGenerator#genMappings()
	 */
	@Override
	protected void genMappings() throws Exception {
		for(MappingType m: s.getDocument().getMappingScenario().getMappings().getMappingArray()) {
			MappingType newM = (MappingType) m.copy();
			
			newM.setId(namingPrefix + newM.getId());
			StringRefType[] corrs = newM.getUses().getCorrespondenceArray();
			for(int i = 0; i < corrs.length; i++)
				corrs[i].setRef(namingPrefix + corrs[i].getRef());
			
			RelAtomType[] foreach = newM.getForeach().getAtomArray();
			for(int i = 0; i < foreach.length; i++) {
				foreach[i].setTableref(namingPrefix + foreach[i].getTableref());
				addPrefixToArgs(foreach[i], namingPrefix);
			}
			
			RelAtomType[] exists = newM.getExists().getAtomArray();
			for(int i = 0; i < exists.length; i++) {
				exists[i].setTableref(namingPrefix + exists[i].getTableref());
				addPrefixToArgs(exists[i], namingPrefix);
			}
						
			fac.addMapping(newM);
		}
	}
	
	protected void addPrefixToArgs(XmlObject a, String prefix) {
		XmlCursor c = a.newCursor();
		int i = 0;
		while(c.toChild(i++))
		{
			XmlObject o = (XmlObject) c.getObject();
			if (o instanceof SKFunction) {
				SKFunction f = (SKFunction) o;
				f.setSkname(prefix + f.getSkname());
				addPrefixToArgs(f, prefix);
			}
			else if (o instanceof FunctionType) {
				FunctionType f = (FunctionType) o;
				f.setFname(prefix + f.getFname());
				addPrefixToArgs(f, prefix);
			}
			c.toParent();
		}
		c.dispose();
	}

	/* (non-Javadoc)
	 * @see tresc.benchmark.schemaGen.AbstractScenarioGenerator#genTransformations()
	 */
	@Override
	protected void genTransformations() throws Exception {
		// TODO copy transformations

	}

	/* (non-Javadoc)
	 * @see tresc.benchmark.schemaGen.AbstractScenarioGenerator#genSourceRels()
	 */
	@Override
	protected void genSourceRels() throws Exception {
		int h = 0;
		
		SchemaType sourceSchema = s.getDocument().getMappingScenario().getSchemas().getSourceSchema();
		for(RelationType r: sourceSchema.getRelationArray()) {
			RelationType newR = copyRelAddPrefix(r);
			fac.addRelation(getRelHook(h++), newR, true);
		}
		for(ForeignKeyType fk: sourceSchema.getForeignKeyArray()) {
			ForeignKeyType newFK = copyFK(fk);
			fac.addForeignKey(newFK, true);
		}
		for(FDType fd: sourceSchema.getFDArray()) {
			FDType newFd = (FDType) fd.copy();
			fd.setId(namingPrefix + fd.getId());
			
			String[] fromAttrs = fd.getFrom().getAttrArray();
			for(int i = 0; i < fromAttrs.length; i++) {
				fromAttrs[i] = namingPrefix + fromAttrs[i];
			}
			
			String[] toAttrs = fd.getTo().getAttrArray();
			for(int i = 0; i < toAttrs.length; i++) {
				toAttrs[i] = namingPrefix + toAttrs[i];
			}
			
			fd.setTableref(namingPrefix + fd.getTableref());
			
			fac.addFD(newFd);
		}
	}
	
	private ForeignKeyType copyFK (ForeignKeyType fk) {
		ForeignKeyType newFK = (ForeignKeyType) fk.copy();
		
		newFK.setId(namingPrefix + newFK.getId());
		newFK.getFrom().setTableref(namingPrefix + newFK.getFrom().getTableref());
		String[] fromAtts = newFK.getFrom().getAttrArray();		
		for(int i = 0; i < fromAtts.length; i++)
			newFK.getFrom().setAttrArray(i, namingPrefix + fromAtts[i]);
		
		newFK.getTo().setTableref(namingPrefix + newFK.getTo().getTableref());
		String[] toAtts = newFK.getTo().getAttrArray();		
		for(int i = 0; i < toAtts.length; i++)
			newFK.getTo().setAttrArray(i, namingPrefix + toAtts[i]);
		
		return newFK;
	}

	private RelationType copyRelAddPrefix(RelationType r) {
		RelationType newR = (RelationType) r.copy();
		newR.setName(namingPrefix + r.getName());
		AttrDefType[] attrs = newR.getAttrArray();
		for(int i = 0; i < attrs.length; i++) {
			attrs[i].setName(namingPrefix + attrs[i].getName());
		}
		
		String[] pk = newR.getPrimaryKey().getAttrArray();
		for(int i = 0; i < pk.length; i++) {
			newR.getPrimaryKey().setAttrArray(i, namingPrefix + pk[i]);
		}
		return newR;
	}

	/* (non-Javadoc)
	 * @see tresc.benchmark.schemaGen.AbstractScenarioGenerator#genTargetRels()
	 */
	@Override
	protected void genTargetRels() throws Exception {
		int h = 0;
		SchemaType targetSchema = s.getDocument().getMappingScenario().getSchemas().getTargetSchema();
		for(RelationType r: targetSchema.getRelationArray()) {
			RelationType newR = copyRelAddPrefix(r);
			fac.addRelation(getRelHook(h++), newR, false);
		}
		for(ForeignKeyType fk: targetSchema.getForeignKeyArray()) {
			ForeignKeyType newFK = copyFK(fk);
			fac.addForeignKey(newFK, false);
		}
	}

	/* (non-Javadoc)
	 * @see tresc.benchmark.schemaGen.AbstractScenarioGenerator#getScenType()
	 */
	@Override
	public ScenarioName getScenType() {
		return ScenarioName.LOADEXISTING;
	}

}
