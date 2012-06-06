package tresc.benchmark;

import java.util.HashMap;
import java.util.Map;

import org.vagabond.benchmark.explgen.CorrespondenceGen;
import org.vagabond.benchmark.explgen.ExplanationGenerator;
import org.vagabond.benchmark.explgen.SourceCopyGen;
import org.vagabond.benchmark.explgen.SourceJoinGen;
import org.vagabond.benchmark.explgen.SourceSkeletonGen;
import org.vagabond.benchmark.explgen.SuperfluousMappingGen;
import org.vagabond.benchmark.explgen.TargetSkeletonGen;
import org.vagabond.explanation.model.basic.SuperflousMappingError;

import tresc.benchmark.dataGen.DataGenerator;
import tresc.benchmark.dataGen.ToXDataGenerator;
import tresc.benchmark.dataGen.ToXScriptOnlyDataGenerator;
import tresc.benchmark.dataGen.TrampCSVGen;
import tresc.benchmark.dataGen.TrampXMLinlineGen;


public class Constants
{
    public enum ParameterName {
        NumOfSubElements,
        NestingDepth,
        JoinSize,
        JoinKind,
        NumOfJoinAttributes,
        NumOfParamsInFunctions,
        NumOfNewAttributes,
        NumofAttributestoDelete,
        SkolemKind,
    };

    public enum JoinKind {
        STAR,
        CHAIN,
        VARIABLE;
    };

    public enum ScenarioName {
        COPY,
        VALUEGEN,
        HORIZPARTITION,
        SURROGATEKEY,
        MERGING,
        VALUEMANAGEMENT,
        FUSION,
        FLATTENING,
        NESTING,
        SELFJOINS,
        VERTPARTITION,
        GLAV,
        ADDATTRIBUTE,
        DELATTRIBUTE,
        VERTPARTITIONISA
    };
    
    public static final Map<ScenarioName,String> nameForScenarios 
    		= new HashMap<ScenarioName, String> ();
    
    static {
    	nameForScenarios.put(ScenarioName.COPY, "_CP");
    	nameForScenarios.put(ScenarioName.VALUEGEN, "_VG");
    	nameForScenarios.put(ScenarioName.HORIZPARTITION, "_HP");
    	nameForScenarios.put(ScenarioName.SURROGATEKEY, "_SK");
    	nameForScenarios.put(ScenarioName.MERGING, "_DE");
    	nameForScenarios.put(ScenarioName.VALUEMANAGEMENT, "_AV");
    	nameForScenarios.put(ScenarioName.FUSION, "_OF");
    	nameForScenarios.put(ScenarioName.FLATTENING, "_FL");
    	nameForScenarios.put(ScenarioName.NESTING, "_NE");
    	nameForScenarios.put(ScenarioName.SELFJOINS, "_CF");
    	nameForScenarios.put(ScenarioName.VERTPARTITION, "_VP");
    	nameForScenarios.put(ScenarioName.GLAV, "_GL");
    	nameForScenarios.put(ScenarioName.VERTPARTITIONISA, "_VI");
    	nameForScenarios.put(ScenarioName.ADDATTRIBUTE, "_AD");
    	nameForScenarios.put(ScenarioName.DELATTRIBUTE, "_DL");
    }
    
    public enum OutputOption {
    	HTMLSchemas,
    	Data,
    	XMLSchemas,
    	HTMLMapping,
    	TrampXML,
    	ErrorsAndExplanations
    }
    
    public static final Map<OutputOption, Boolean> defaultOutputOptionValues
    		= new HashMap<OutputOption, Boolean> ();
    
    static {
    	defaultOutputOptionValues.put(OutputOption.HTMLSchemas, Boolean.FALSE);
    	defaultOutputOptionValues.put(OutputOption.HTMLMapping, Boolean.FALSE);
    	defaultOutputOptionValues.put(OutputOption.XMLSchemas, Boolean.FALSE);
    	defaultOutputOptionValues.put(OutputOption.Data, Boolean.TRUE);
    	defaultOutputOptionValues.put(OutputOption.TrampXML, Boolean.TRUE);
    	defaultOutputOptionValues.put(OutputOption.ErrorsAndExplanations, Boolean.FALSE);
    }
    
    public enum DataGenType {
    	ToXGeneScriptOnly,
    	ToXGene,
    	TrampCSV,
    	TrampXMLInline
    }
    
    public static final Map<DataGenType, Class<? extends DataGenerator>> dataGens 
    		= new HashMap<DataGenType, Class<? extends DataGenerator>> ();
    
    static {
    	dataGens.put(DataGenType.ToXGeneScriptOnly, ToXScriptOnlyDataGenerator.class);
    	dataGens.put(DataGenType.ToXGene, ToXDataGenerator.class);
    	dataGens.put(DataGenType.TrampCSV, TrampCSVGen.class);
    	dataGens.put(DataGenType.TrampXMLInline, TrampXMLinlineGen.class); //TODO
    }
    
    public enum TrampXMLOutputSwitch {
    	Correspondences,
    	Transformations,
    	Data,
    	ConnectionInfo
    }
    
    public static final Map<TrampXMLOutputSwitch, Boolean> trampXmlOutDefaults
    		= new HashMap<TrampXMLOutputSwitch, Boolean> ();
    
    static {
    	trampXmlOutDefaults.put(TrampXMLOutputSwitch.Correspondences, Boolean.TRUE);
    	trampXmlOutDefaults.put(TrampXMLOutputSwitch.Transformations, Boolean.TRUE);
    	trampXmlOutDefaults.put(TrampXMLOutputSwitch.Data, Boolean.TRUE);
    	trampXmlOutDefaults.put(TrampXMLOutputSwitch.ConnectionInfo, Boolean.TRUE);
    }
    
    public enum MappingLanguageType {
    	FOtgds,
    	SOtgds
    }
    
    public enum DESErrorType {
    	SuperfluousMapping,
    	SourceCopyError,
    	SourceJoinValueError,
    	SourceSkeletonError,
    	TargetSkeletonError,
    	CorrespondenceError
    }
    
    public static final Map<DESErrorType, ExplanationGenerator> errorGenerators
    		= new HashMap<DESErrorType, ExplanationGenerator> ();
    
    static {
    	errorGenerators.put(DESErrorType.SourceCopyError, new SourceCopyGen());
    	errorGenerators.put(DESErrorType.SourceJoinValueError, new SourceJoinGen());
    	errorGenerators.put(DESErrorType.SuperfluousMapping, new SuperfluousMappingGen());
    	errorGenerators.put(DESErrorType.SourceSkeletonError, new SourceSkeletonGen());
    	errorGenerators.put(DESErrorType.TargetSkeletonError, new TargetSkeletonGen());
    	errorGenerators.put(DESErrorType.CorrespondenceError, new CorrespondenceGen());
    }
}
