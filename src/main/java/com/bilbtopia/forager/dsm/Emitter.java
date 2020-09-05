package com.bilbtopia.forager.dsm;

public interface Emitter<T> {
	void emit(BasePrinter<?> printer, T val);
}
