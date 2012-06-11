package tresc.benchmark.schemaGen;

import java.util.HashMap;

import smark.support.MappingScenario;
import tresc.benchmark.Configuration;
import vtools.dataModel.schema.Schema;
import vtools.utils.structures.AssociativeArray;

/* 
 * Each generator of a scenario case subclasses this class. 
 */
public abstract class ScenarioGenerator
{
    protected final String _attributes = "abcdefghijklmnopqrstuvwxyz"; // Can only hold less than 26 attributes in one mapping
    
    protected HashMap<String, Character> attrMap = new HashMap<String, Character>();

    public abstract void generateScenario(MappingScenario scenario, Configuration configuration) throws Exception;
}
