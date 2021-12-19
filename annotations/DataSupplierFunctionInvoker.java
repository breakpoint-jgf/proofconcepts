package com.local.util.annotations;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.local.util.annotations.CoreLoggerFactory.CoreLogger;

public class DataSupplierFunctionInvoker {

	private static final CoreLogger logger = CoreLoggerFactory.getLogger(DataSupplierFunctionInvoker.class);
	
	
	/*
	 * Following are symbols used to split the String. Any symbol value assigned that happen to be clashing with Regex pattern symbols
	 * requires to have an escape character when used during split string operation.
	 */
	private static final String CLASS_AND_METHOD_SEPARATOR = "#";
	private static final String METHOD_AND_PARAMS_SEPARATOR = "=>";
	private static final String PARAMS_SEPARATOR = "|";
	private static final String PARAM_NAME_AND_VALUE_SEPARATOR = "=";

	
	/**
	 *  results to <pre>com.some_package.SomeClass#someMethod</pre>
	 */
	private static final String PARAM_SAMPLE_CLASS_AND_METHOD = "com.some_package.SomeClass"
			+ CLASS_AND_METHOD_SEPARATOR + "someMethod";
	
	/**
	 * results to <pre>com.some_package.SomeClass#someMethod=>param1String=this is a string|param2Number=123|param3Boolean=true</pre>
	 */
	private static final String PARAM_SAMPLE_CLASS_AND_METHOD_WITH_PARAMS = "com.some_package.SomeClass"
			+ CLASS_AND_METHOD_SEPARATOR + "someMethod" + METHOD_AND_PARAMS_SEPARATOR + "param1String"
			+ PARAM_NAME_AND_VALUE_SEPARATOR + "this is a String" + PARAMS_SEPARATOR + "param2Number"
			+ PARAM_NAME_AND_VALUE_SEPARATOR + "1" + PARAMS_SEPARATOR + "param3Boolean" + PARAM_NAME_AND_VALUE_SEPARATOR
			+ "true";
	
	private static final String PARAM_SAMPLE = PARAM_SAMPLE_CLASS_AND_METHOD + " or "
			+ PARAM_SAMPLE_CLASS_AND_METHOD_WITH_PARAMS;

	
	private static final Set<Class<?>> SUPPORTED_PARAM_TYPES = new HashSet<Class<?>>(Arrays.asList(
				DataSupplierMainParam.class,
				CoreDataType.class
			));
	


	public <T> T invoke(Object dataSupplierInstance, DataSupplierMainParam dataSupplierMainParam,  String classAndMethodInfo,
			Class<T> returnType) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {

		logger.info("invoke :: " + classAndMethodInfo);
		
		MethodInfo methodInfo = convertToMethodInfo(classAndMethodInfo);
		Method[] allMethods  = dataSupplierInstance.getClass().getDeclaredMethods();
		
		//DataSupplierMainParam is always be there hence method argument size is always at least 1
		final int sizeOfMethodArgsExpected = 1 + methodInfo.getMethodExtraParamMap().size();
		
		Object result = null;
		boolean isMethodFound = false;
		
		
		for(Method method : allMethods) {
			
			logger.debug("invoke :: detected method = "+method.getName() +", parameter size = "+method.getParameterCount());
			
			if(!methodInfo.getMethodName().equals(method.getName())) continue;
			
			if(method.getParameterCount() != sizeOfMethodArgsExpected) continue;
			
			isMethodFound = true;
			
			Object [] methodParams = prepareMethodParams(method, methodInfo, dataSupplierMainParam);
			
			logger.debug("invoke :: arguments to method  "+Arrays.asList(methodParams));
			logger.debug("invoke :: method parameters "+Arrays.asList(method.getParameters()));
			
			
			result =  method.invoke(dataSupplierInstance, methodParams);
			
		}
		
		
		if(!isMethodFound) {
			
			List<String> parameters = new ArrayList<String>();
			parameters.add(dataSupplierMainParam.getCoreParamName());
			parameters.addAll(methodInfo.getMethodExtraParamMap().keySet());
			
			throw new IllegalStateException("method to execute "+methodInfo.getMethodClazzName() + "." + methodInfo.getMethodName() + parameters
					+ " implementation not found. Check class, method name or parameter names are correct.");
		}
		
		return returnType.cast(result);
	}

	
	private static Object[] prepareMethodParams(Method method, MethodInfo methodInfo, DataSupplierMainParam dataSupplierMainParam) {
		
		Map<String,String> extraParamMap = methodInfo.getMethodExtraParamMap();
		
		Parameter[] parameters = method.getParameters();
		
		if(parameters == null || parameters.length == 0) {
			return new Object[] {};
		}
		
		final List<Object> prepParams = new ArrayList<>();
		
		for(Parameter parameter : parameters) {
			
			if(!isSupportedParamType(parameter.getType())) {
				throw new IllegalStateException(method.getClass().getName() + "."+method.getName()+"'s parameter = "+parameter.getName()+" is not a supported data type. Supported types =  "+SUPPORTED_PARAM_TYPES);
			}
			
			CoreParam[] requiredParam =   parameter.getAnnotationsByType(CoreParam.class);
			
			if(requiredParam == null || requiredParam.length == 0) {
				throw new IllegalStateException(method.getClass().getName() + "."+method.getName()+"'s parameter is required to annotate with @"+CoreParam.class.getSimpleName()+"! param = "+parameter.getName());
			}
			
			String paramName = requiredParam[0].name();
			
			if(!extraParamMap.containsKey(paramName) && !dataSupplierMainParam.getCoreParamName().equals(paramName)) {
				throw new IllegalStateException(method.getClass().getName() + "."+method.getName()+"'s parameter = "+paramName+" is required but not found in arguments");
			}
			
			
			if(dataSupplierMainParam.getCoreParamName().equals(paramName)) {
				prepParams.add(dataSupplierMainParam);
				continue;
			}
			
			String value = extraParamMap.get(paramName);
			
			if(CoreDataType.TypeString.class.equals(parameter.getType())) {
				prepParams.add(new CoreDataType.TypeString((String) value));
			}
			
			else if(CoreDataType.TypeBoolean.class.equals(parameter.getType())) {
				prepParams.add(new CoreDataType.TypeBoolean(Boolean.valueOf((String) value)));
			}

			else if(CoreDataType.TypeNumber.class.equals(parameter.getType())) {
				prepParams.add(new CoreDataType.TypeNumber(numberParse((String) value)));
			}
		
			else {
				throw new IllegalStateException(
						method.getClass().getName() + "." + method.getName() + "'s parameter " + parameter.getName()
								+ " data type is nor programmatically handled! Type = " + parameter.getType());
			}
		}
		
		
		return prepParams.toArray();
		
	}
	
	
	private static boolean isSupportedParamType(Class<?> clazz) {
		
		for (Class<?> supportedClazz : SUPPORTED_PARAM_TYPES) {
			if(supportedClazz.isAssignableFrom(clazz)) {
				return true;
			}
		}
		
		return false;
		
	}
	
	
	public static MethodInfo convertToMethodInfo(String classAndMethodInfo) {

		if (classAndMethodInfo == null || classAndMethodInfo.isEmpty()) {
			throw new IllegalArgumentException("classAndMethodInfo can't be blank");
		}

		String[] classNameAndMethod = classAndMethodInfo.split(CLASS_AND_METHOD_SEPARATOR);

		logger.debug("extractNameAndValue :: classNameAndMethod = "+Arrays.asList(classNameAndMethod));
		
		if (classNameAndMethod.length < 2) {
			throw new IllegalArgumentException(
					"Unable to extract class and method. Should follow pattern " + PARAM_SAMPLE);
		}

		final String className = classNameAndMethod[0];
		String methodName = classNameAndMethod[1];

		boolean hasParams = methodName.contains(METHOD_AND_PARAMS_SEPARATOR);

		if (!hasParams) {
			return MethodInfo.build(className, methodName, null);
		}

		String[] methodAndParams = methodName.split(METHOD_AND_PARAMS_SEPARATOR);

		
		logger.debug("extractNameAndValue :: methodAndParams = "+Arrays.asList(methodAndParams));
		
		if (methodAndParams.length < 2) {
			throw new IllegalArgumentException(
					"Unable to extract method and parameters. Should follow pattern " + PARAM_SAMPLE);
		}

		final String methodNameWithParams = methodAndParams[0];
		String params = methodAndParams[1];

		//add regex escape symbol --> \
		String[] paramsNameValues = params.split("\\"+PARAMS_SEPARATOR);

		logger.debug("extractNameAndValue :: paramsNameValues = "+Arrays.asList(paramsNameValues));
		
		if (paramsNameValues.length == 0) {
			throw new IllegalArgumentException("Unable to extract parameters. Should follow pattern " + PARAM_SAMPLE);
		}

		/*
		 * extract param's name and value. value at this point is still String. data
		 * type conversion of value will handle during reflection method invocation
		 */
		Map<String, String> paramMap = Arrays.asList(paramsNameValues).stream()
				.map(paramNameAndValue -> extractNameAndValue(paramNameAndValue))
					.collect(Collectors.toMap(param -> param[0].trim(), param -> param[1].trim()));

		
		
		
		return MethodInfo.build(className, methodNameWithParams, paramMap);
	}

	
	
	private static String [] extractNameAndValue(String paramNameAndValue) {
		
		logger.debug("extractNameAndValue :: paramNameAndValue = "+paramNameAndValue);
		
		String [] nameAndValue = paramNameAndValue.split(PARAM_NAME_AND_VALUE_SEPARATOR);
		
		if(nameAndValue.length < 2) {
			throw new IllegalArgumentException("Unable to extract parameter's name and value. Should follow pattern " + PARAM_SAMPLE);
		}
		return nameAndValue;
	}
	
	
	
	
	private static Number numberParse(String val) {

		try {
			return Long.parseLong(val);
		} catch (Exception e) {
			try {
				return Double.parseDouble(val);
			} catch (Exception e2) {
				throw new IllegalArgumentException("unable to convert value (val = " + val
						+ ") to Number. If type intended is a String, enclose it with single-quotes symbol instead. E.g. 'yourValue' ");
			}
		}

	}

	public static class MethodInfo {

		private String methodClazzName;
		private String methodName;
		private Map<String, String> methodExtraParamMap;

		public static MethodInfo build(String methodClazzName, String methodName, Map<String, String> methodExtraParamMap) {
			MethodInfo obj = new MethodInfo();
			obj.methodClazzName = methodClazzName;
			obj.methodName = methodName;
			obj.methodExtraParamMap = methodExtraParamMap != null ? methodExtraParamMap : Collections.emptyMap();
			return obj;
		}

		
		public String getMethodName() {
			return methodName;
		}


		public String getMethodClazzName() {
			return methodClazzName;
		}

		public Map<String, String> getMethodExtraParamMap() {
			return methodExtraParamMap;
		}

		@Override
		public String toString() {
			return new StringBuilder("methodClazzName = ").append(methodClazzName).append(", methodName = ")
					.append(methodName).append(", methodExtraParamMap = ").append(methodExtraParamMap).toString();
		}

	}

}
