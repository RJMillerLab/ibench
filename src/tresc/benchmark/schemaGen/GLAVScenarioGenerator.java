package tresc.benchmark.schemaGen;

import java.util.Random;
import java.util.Vector;

import org.apache.log4j.Logger;

import smark.support.MappingScenario;
import smark.support.SMarkElement;
import tresc.benchmark.Configuration;
import tresc.benchmark.Constants;
import tresc.benchmark.Constants.ScenarioName;
import tresc.benchmark.Modules;
import vtools.dataModel.expression.Expression;
import vtools.dataModel.expression.ForeignKey;
import vtools.dataModel.expression.SPJQuery;
import vtools.dataModel.expression.SelectClauseList;
import vtools.dataModel.schema.Schema;
import vtools.dataModel.types.Type;
import vtools.utils.structures.EqClassManager;


public class GLAVScenarioGenerator extends AbstractScenarioGenerator
{
	static Logger log = Logger.getLogger(GLAVScenarioGenerator.class);
	
    private final int FROM = 0;

    private final int TO = 1;

    // This is the module that generates the src schema.
    private final GLAVScenarioSrcGenerator srcGenerator = new GLAVScenarioSrcGenerator();

    private final GLAVScenarioTrgGenerator trgGenerator = new GLAVScenarioTrgGenerator();

    private final GLAVScenarioMappingGenerator mapGenerator = new GLAVScenarioMappingGenerator();

    private EqClassManager eqClassMgr;

    public static final String _stamp = "GL";

    public GLAVScenarioGenerator()
    {
        eqClassMgr = new EqClassManager(EqClassManager.ABSCHECK);
    }

    public void generateScenario(MappingScenario scenario, Configuration configuration)
    {
    	init(configuration, scenario);
    	for (int i = 0, imax = repetitions; i < imax; i++)
        {
            createGLAVcase(scenario, configuration, i);
        }
    }

    private void createGLAVcase(MappingScenario scenario, Configuration configuration, int repetition)
    {
        Schema src = scenario.getSource();
        Schema trg = scenario.getTarget();
        SPJQuery mapQuery = scenario.getTransformation();

        Object[] res = srcGenerator.generateSourceSchema(configuration, _generator, eqClassMgr, repetition);
        // Add the root sets in the source schema
        SMarkElement[] rootSets = (SMarkElement[]) res[0];
        for (int i = 0, imax = rootSets.length; i < imax; i++)
        {
            src.addRootElement(rootSets[i]);
        }
        // add the constraints in the source schema
        Vector<ForeignKey> fks = (Vector<ForeignKey>) res[1];
        for (int i = 0, imax = fks.size(); i < imax; i++)
        {
            src.addConstraint(fks.elementAt(i));
        }
        // Read the basis source query
        SPJQuery basisQuery = (SPJQuery) res[2];

        // Read the list of the atomic elements and create a copy of them. That
        // copies will be the elements we will use in the target. Each such
        // element becomes a member of the eqClass that the respective source
        // element belongs
        Vector<SMarkElement> atomicElements = (Vector<SMarkElement>) res[3];
        int M = atomicElements.size();
        Vector<SMarkElement> trgElements = new Vector<SMarkElement>(M);
        for (int i = 0; i < M; i++)
        {
            SMarkElement srcElement = atomicElements.elementAt(i);
            Object srcElClass = eqClassMgr.getEqClass(srcElement);
            String name = (String) srcElement.getHook();
            name = Modules.nameFactory.getARandomName() + "_" + name;
            Type t = srcElement.getType().clone();
            SMarkElement trgElement = new SMarkElement(name, t, null, -1, -1);
            eqClassMgr.classify(trgElement, srcElClass);
            trgElements.add(trgElement);
        }

        // print now the schema and the elements for debugging purposes
        log.debug("\n\n*****\nBasis Query:\n" + basisQuery + "\n");
        for (int i = 0, imax = 0; i < imax; i++)
        {
            SMarkElement el = atomicElements.elementAt(i);
            Object elClass = eqClassMgr.getEqClass(el);
            Object elClassValue = eqClassMgr.getEqClassHook(elClass);

            SMarkElement tel = trgElements.elementAt(i);
            Object telClass = eqClassMgr.getEqClass(tel);
            Object telClassValue = eqClassMgr.getEqClassHook(telClass);

            log.info(el + "[Class: " + elClass + " w/ Value: " + elClassValue + "\t\t"
            		+ tel + "[Class: " + telClass + " w/ Value: " + telClassValue);
        }


        // now we generate the target schema
        Object[] resT = trgGenerator.generateTargetSchema(trgElements, configuration, _generator, eqClassMgr,
            repetition);
        SMarkElement[] rootSetsT = (SMarkElement[]) resT[0];
        for (int i = 0, imax = rootSetsT.length; i < imax; i++)
        {
            trg.addRootElement(rootSetsT[i]);
        }
        // add the constraints in the target schema
        Vector<ForeignKey> fksT = (Vector<ForeignKey>) resT[1];
        for (int i = 0, imax = fksT.size(); i < imax; i++)
        {
            trg.addConstraint(fksT.elementAt(i));
        }

        //DEBUG
        //System.out.println(" schema \n"+ scenario);
        
        Vector<SPJQuery> mapRes = mapGenerator.generateMapping(rootSetsT, basisQuery, eqClassMgr);
        // each sub query from the vector has to be added in the select 
        // of the final transformation query
        SelectClauseList sel = mapQuery.getSelect();
        for (int i = 0, imax = mapRes.size(); i < imax; i++)
        {
            String subQueryName = mapRes.elementAt(i).getSelect().getTermName(0);
            Expression subQuery = mapRes.elementAt(i).getSelect().getTerm(0);
            sel.add(subQueryName, subQuery.clone());
        }
        mapQuery.setSelect(sel);
    }



	@Override
	protected void genSourceRels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void genTargetRels() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public ScenarioName getScenType() {
		return ScenarioName.GLAV;
	}

	@Override
	protected void genCorrespondences() {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void genMappings() throws Exception {
		// TODO Auto-generated method stub
		
	}

	@Override
	protected void genTransformations() throws Exception {
		// TODO Auto-generated method stub
		
	}
}
