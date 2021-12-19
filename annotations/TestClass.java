package com.local.util.annotations;

import java.lang.reflect.InvocationTargetException;

public class TestClass {

	public static class Data implements DataSupplierMainParam {

		private String message;

		public Data(String message) {
			this.message = message;
		}

		public String getMessage() {
			return message;
		}

		@Override
		public String getCoreParamName() {
			return "data";
		}

	}

	
	public String doSay(@CoreParam(name = "data") Data data, @CoreParam(name = "name")  CoreDataType.TypeString name, @CoreParam(name = "age") CoreDataType.TypeNumber age, String s) {
		return "Hi "+name.getValue()+". Your age is "+age.getValue()+". Your message is "+data.getMessage();
	}
	
	
	public static void main(String [] s) throws IllegalAccessException, IllegalArgumentException, InvocationTargetException {
		
		String classAndMethodInfo = "com.local.util.annotations.TestClass#doSay=>name=Jayson|age=36";
		
		DataSupplierFunctionInvoker dataSupplierFunctionInvoker = new DataSupplierFunctionInvoker();
		
		String result = dataSupplierFunctionInvoker.invoke(new TestClass(), new Data("Hellow world"), classAndMethodInfo, String.class);
		
		System.out.println(result);
	}
	
}
