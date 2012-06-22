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
        ReuseSourcePerc,	// reuse previously generated schema elements
        ReuseTargetPerc,
        NoReuseScenPerc,	// percentage of basic scenarios where we do not do a reuse
        SourceSkolemPerc,	// associates skolems with source  
        SourceSkolemNumAttr		// attributes and use them in mappings
    };

    public static final Map<ParameterName, Integer> defaultParameterValues
			= new HashMap<ParameterName, Integer> ();
    
    static {
    	defaultParameterValues.put(ParameterName.NumOfSubElements, 3);
    	defaultParameterValues.put(ParameterName.NestingDepth, 1);
    	defaultParameterValues.put(ParameterName.JoinSize, 2);
    	defaultParameterValues.put(ParameterName.JoinKind, 0);
    	defaultParameterValues.put(ParameterName.NumOfJoinAttributes, 1);
    	defaultParameterValues.put(ParameterName.NumOfParamsInFunctions, 2);
    	defaultParameterValues.put(ParameterName.NumOfNewAttributes, 2);
    	defaultParameterValues.put(ParameterName.NumofAttributestoDelete, 1);
    	defaultParameterValues.put(ParameterName.SkolemKind, 0);
    	defaultParameterValues.put(ParameterName.ReuseSourcePerc, 0);
    	defaultParameterValues.put(ParameterName.ReuseTargetPerc, 0);
    	defaultParameterValues.put(ParameterName.SourceSkolemPerc, 0);
    	defaultParameterValues.put(ParameterName.SourceSkolemNumAttr, 0);
    	defaultParameterValues.put(ParameterName.NoReuseScenPerc, 100);
    }
    
    public static final Map<ParameterName, Integer> defaultParameterDeviation
			= new HashMap<ParameterName, Integer> ();
    
    static {
    	defaultParameterDeviation.put(ParameterName.NumOfSubElements, 0);
    	defaultParameterDeviation.put(ParameterName.NestingDepth, 0);
    	defaultParameterDeviation.put(ParameterName.JoinSize, 0);
    	defaultParameterDeviation.put(ParameterName.JoinKind, 0);
    	defaultParameterDeviation.put(ParameterName.NumOfJoinAttributes, 0);
    	defaultParameterDeviation.put(ParameterName.NumOfParamsInFunctions, 0);
    	defaultParameterDeviation.put(ParameterName.NumOfNewAttributes, 0);
    	defaultParameterDeviation.put(ParameterName.NumofAttributestoDelete, 0);
    	defaultParameterDeviation.put(ParameterName.SkolemKind, 0);
    	defaultParameterDeviation.put(ParameterName.ReuseSourcePerc, 0);
    	defaultParameterDeviation.put(ParameterName.ReuseTargetPerc, 0);
    	defaultParameterDeviation.put(ParameterName.SourceSkolemPerc, 0);
    	defaultParameterDeviation.put(ParameterName.SourceSkolemNumAttr, 0);
    	defaultParameterDeviation.put(ParameterName.NoReuseScenPerc, 0);
    }
    
    public enum JoinKind {
        STAR,
        CHAIN,
        VARIABLE;
    };
    
    public enum SkolemKind {
        ALL,
        RANDOM,
        KEY;
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
    
  
    public static final Map<DESErrorType, Integer> defaultGroundTruthValues
    		= new HashMap<DESErrorType, Integer> ();
    		
    static {
    	defaultGroundTruthValues.put(DESErrorType.SuperfluousMapping, 1);
    	defaultGroundTruthValues.put(DESErrorType.CorrespondenceError, 0);
    	defaultGroundTruthValues.put(DESErrorType.SourceCopyError, 0);
    	defaultGroundTruthValues.put(DESErrorType.SourceJoinValueError, 0);
    	defaultGroundTruthValues.put(DESErrorType.SourceSkeletonError, 0);
    	defaultGroundTruthValues.put(DESErrorType.TargetSkeletonError, 0);
    }
    
    public static final Map<DESErrorType, Integer> defaultGroundTruthDeviation
	= new HashMap<DESErrorType, Integer> ();
	
	static {
		defaultGroundTruthDeviation.put(DESErrorType.SuperfluousMapping, 0);
		defaultGroundTruthDeviation.put(DESErrorType.CorrespondenceError, 0);
		defaultGroundTruthDeviation.put(DESErrorType.SourceCopyError, 0);
		defaultGroundTruthDeviation.put(DESErrorType.SourceJoinValueError, 0);
		defaultGroundTruthDeviation.put(DESErrorType.SourceSkeletonError, 0);
		defaultGroundTruthDeviation.put(DESErrorType.TargetSkeletonError, 0);	
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
    	ConnectionInfo,
    	FDs
    }
    
    public static final Map<TrampXMLOutputSwitch, Boolean> trampXmlOutDefaults
    		= new HashMap<TrampXMLOutputSwitch, Boolean> ();
    
    static {
    	trampXmlOutDefaults.put(TrampXMLOutputSwitch.Correspondences, Boolean.TRUE);
    	trampXmlOutDefaults.put(TrampXMLOutputSwitch.Transformations, Boolean.TRUE);
    	trampXmlOutDefaults.put(TrampXMLOutputSwitch.Data, Boolean.TRUE);
    	trampXmlOutDefaults.put(TrampXMLOutputSwitch.ConnectionInfo, Boolean.TRUE);
    	trampXmlOutDefaults.put(TrampXMLOutputSwitch.FDs, Boolean.FALSE);
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
    
    public enum DBOption {
    	User,
    	URL,
    	Password,
    	Port,
    	DBName
    }
    
    public static final Map<DBOption, String> dbOptionDefaults = 
    		new HashMap<DBOption, String> ();
    
    static {
    	dbOptionDefaults.put(DBOption.DBName, "tramptest");
    	dbOptionDefaults.put(DBOption.Password, "");
    	dbOptionDefaults.put(DBOption.URL, "127.0.0.1");
    	dbOptionDefaults.put(DBOption.User, "postgres");
    	dbOptionDefaults.put(DBOption.Port, "5432");
    }
}
