package momotFiles;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.crypto.dsig.keyinfo.KeyValue;

import org.eclipse.core.runtime.Platform;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
import org.eclipse.emf.henshin.interpreter.ApplicationMonitor;
import org.eclipse.emf.henshin.interpreter.EGraph;
import org.eclipse.emf.henshin.interpreter.Engine;
import org.eclipse.emf.henshin.interpreter.UnitApplication;
import org.eclipse.emf.henshin.interpreter.impl.AssignmentImpl;
import org.eclipse.emf.henshin.interpreter.impl.EGraphImpl;
import org.eclipse.emf.henshin.interpreter.impl.EngineImpl;
import org.eclipse.emf.henshin.interpreter.impl.RuleApplicationImpl;
import org.eclipse.emf.henshin.interpreter.impl.UnitApplicationImpl;
import org.eclipse.emf.henshin.model.Module;
import org.eclipse.emf.henshin.model.Rule;
import org.eclipse.emf.henshin.model.Unit;
import org.eclipse.emf.henshin.model.impl.ModuleImpl;
import org.eclipse.emf.henshin.model.impl.RuleImpl;
import org.eclipse.emf.henshin.model.resource.HenshinResourceSet;
import org.gravity.typegraph.basic.BasicPackage;
import org.gravity.typegraph.basic.TClass;
import org.gravity.typegraph.basic.TField;
import org.gravity.typegraph.basic.TFieldDefinition;
import org.gravity.typegraph.basic.TFieldSignature;
import org.gravity.typegraph.basic.TMember;
import org.gravity.typegraph.basic.TMethod;
import org.gravity.typegraph.basic.TMethodDefinition;
import org.gravity.typegraph.basic.TMethodSignature;
import org.gravity.typegraph.basic.TParameterList;
import org.gravity.typegraph.basic.TSignature;
import org.gravity.typegraph.basic.TypeGraph;
import org.gravity.typegraph.basic.impl.TMethodDefinitionImpl;
import org.osgi.framework.Bundle;

public class HenshinExecutor {


	static HenshinResourceSet resourceSet;
	static Module module;
	static EGraph graph;
	static String modulePath = "transformations/MoveMethod.henshin";
	

	public static HashMap<String, Object> getMoveMethodParameter(TypeGraph pg, String methodName, String sourceClassName, String targetClassName){
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		
		
		TClass sourceClass = pg.getClass(sourceClassName);
		TClass targetClass=  pg.getClass(targetClassName);
		TMethodSignature methodSig = sourceClass.getMethodSignature(methodName);

		

		parameters.put("methodSig", methodSig);
		parameters.put("targetClass", targetClass);
		parameters.put("sourceClass", sourceClass);
		return parameters;
	}
	
	public static void initialize(EGraph egraph){
		// Create a resource set with a base directory:
		graph = egraph;
		resourceSet = new HenshinResourceSet("");
				
		
		Resource r = resourceSet.createResource(URI.createURI(""));
		Bundle bundle = Platform.getBundle("momot.movemethod.demo");
		URL res = bundle.getResource(modulePath);
		try(InputStream s = res.openStream()){
			r.load(s, Collections.EMPTY_MAP);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		module = (Module) r.getContents().get(0);
	}
	
	public static void main(String[] args) {
		
		//register
		BasicPackage.eINSTANCE.eClass();
		
		
		boolean saveResult = true;
		
		String graphPath  = "input/dyn1_Test";
		//String graphPath  = "input/14_Xerces2.7.0";
		

		resourceSet = new HenshinResourceSet("");
		module = resourceSet.getModule(modulePath);
		graph = new EGraphImpl(resourceSet.getResource(graphPath+".xmi"));
		// Load the module:
		//module = resourceSet.getModule(modulePath+".henshin", false);
		
		String sourceClass= "dyn1_Test.B";
		String targetClass = "package1.C";
		String method= "n()";

		
		
		
		HashMap<String, Object> parameters = getMoveMethodParameter((TypeGraph)graph.getRoots().get(0), method, sourceClass, targetClass);
		boolean success = executeCheckPreconditions(parameters);
		//HashMap<String, Object> parameters = getDynParameter("n()", "dyn1_Test.B", (TypeGraph)graph.getRoots().get(0));
		//boolean success = executeDyn(parameters);
		
		// Saving the result:
		if (saveResult) {
			resourceSet.saveEObject(graph.getRoots().get(0), graphPath+"_result.xmi");
		}
		
	}
	
	public static HashMap<String, Object> getDynParameter(String methodName, String sourceClassName, TypeGraph graph){
		HashMap<String, Object> parameters = new HashMap<String, Object>();
		TClass sourceClass= graph.getClass(sourceClassName);
		TMethodSignature methodSig = sourceClass.getMethodSignature(methodName);
		parameters.put("methodSig", methodSig);
		parameters.put("sourceClass", sourceClass);
		return parameters;
	}

	public static boolean executeDyn(HashMap<String, Object> parameters){
		return executeUnit("dynMoveMethod", parameters);
	}

	
	public static boolean executeMain(HashMap<String, Object> parameters){
		return executeUnit("main", parameters);
	}
	
	public static boolean executeMoveMethod(HashMap<String, Object> parameters){
		return executeUnit("MoveMethod", parameters);
	}
	
	
	public static boolean executeCheckPreconditions(HashMap<String, Object> parameters){
		return executeUnit("checkPreconditions", parameters);
	}
	
	public static boolean executepreconditions(HashMap<String, Object> parameters){
		return executeUnit("preconditions", parameters);
	}
	
	public static boolean executeUnit(String unitName, Map<String, Object> parameters ){
		// Create an engine and a rule application:
		Engine engine = new EngineImpl();
		UnitApplication unit = new UnitApplicationImpl(engine);
		unit.setEGraph(graph);
		
		// Creating unit 
		unit.setUnit(module.getUnit(unitName));
		
		for(String key: parameters.keySet()){
			unit.setParameterValue(key, parameters.get(key));
		}
		return unit.execute(null);
		

	}
}