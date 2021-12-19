package com.local.util.annotations;

public interface CoreDataType<T> {

	public T getValue();

	public static final class TypeString implements CoreDataType<String> {

		private String value;

		public TypeString(String value) {
			this.value = value;
		}

		@Override
		public String getValue() {
			return value;
		}

	}

	public static final class TypeBoolean implements CoreDataType<Boolean> {

		private Boolean value;

		public TypeBoolean(Boolean value) {
			this.value = value;
		}

		@Override
		public Boolean getValue() {
			return value;
		}

	}

	public static final class TypeNumber implements CoreDataType<Number> {

		private Number value;

		public TypeNumber(Number value) {
			this.value = value;
		}

		@Override
		public Number getValue() {
			return value;
		}

	}

}
