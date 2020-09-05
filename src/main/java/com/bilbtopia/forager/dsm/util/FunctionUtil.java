package com.bilbtopia.forager.dsm.util;

import java.util.function.BiFunction;
import java.util.function.Function;

public class FunctionUtil {

	public static <A, B, C> Function<B, C> objectUncurry(A base, BiFunction<A, B, C> f) {
		return (b) -> f.apply(base, b);
	}

	public static <A, B, C> BiFunction<B, A, C> flip(BiFunction<A, B, C> f) {
		return (b, a) -> f.apply(a, b);
	}

	public static <A, B, C> Function<B, C> partialApply(BiFunction<A, B, C> f, A a) {
		return b -> f.apply(a, b);
	}

	public static <A, B, C> Function<A, C> partialApply2(BiFunction<A, B, C> f, B b) {
		return a -> f.apply(a, b);
	}
	
//	public static <A, B, C> BiFunction<A, B, C> prepend(A a, Function<B, C> f) {
////		return y
//	}
}
