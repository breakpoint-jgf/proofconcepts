package com.local.util.annotations;

import java.util.Arrays;

public class CoreLoggerFactory {

	public static CoreLogger getLogger(Class<?> clazz) {
		return new CoreLogger(clazz);
	}

	public static class CoreLogger {

		private String clazzName;

		private CoreLogger(Class<?> clazz) {
			this.clazzName = clazz.getName();
		}

		public void info(String msg) {
			System.out.println(clazzName + " :: " + msg);
		}

		public void error(String msg, Throwable e) {
			System.out.print(clazzName + " :: " + msg + " " + Arrays.asList(e.getStackTrace()));
		}
		
		public void debug(String msg) {
			System.out.println(clazzName + " :: " + msg);
		}

	}

}
