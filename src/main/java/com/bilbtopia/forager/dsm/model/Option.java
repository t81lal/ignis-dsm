package com.bilbtopia.forager.dsm.model;

import java.util.Objects;

import com.bilbtopia.forager.dsm.Emitter;

public class Option<T> {
	private final OptionKey<T> key;
	private final Emitter<T> emitter;

	public Option(OptionKey<T> key, Emitter<T> emitter) {
		this.key = key;
		this.emitter = emitter;
	}

	public OptionKey<T> getKey() {
		return key;
	}

	public Emitter<T> getEmitter() {
		return emitter;
	}

	public static class OptionKey<T> {

		public static <T> OptionKey<T> getKey(Class<T> valueType, String name) {
			return new OptionKey<>(valueType, name);
		}

		private final Class<T> valueType;
		private final String name;

		public OptionKey(Class<T> valueType, String name) {
			this.valueType = valueType;
			this.name = name;
		}

		public Class<T> getValueType() {
			return valueType;
		}

		public String getName() {
			return name;
		}

		@Override
		public int hashCode() {
			return Objects.hash(name, valueType);
		}

		@Override
		public boolean equals(Object obj) {
			if (obj instanceof OptionKey) {
				OptionKey<?> other = (OptionKey<?>) obj;
				return Objects.equals(valueType, other.valueType) && Objects.equals(name, other.name);
			} else {
				return false;
			}
		}

		@Override
		public String toString() {
			return "OptionKey [valueType=" + valueType + ", name=" + name + "]";
		}
	}
}
