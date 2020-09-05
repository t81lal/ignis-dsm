package com.bilbtopia.forager.dsm;

public class ModelPrinter<Self extends ModelPrinter<Self>> extends BasePrinter<Self> {

	public <T> Self printAttributeHead(String key) {
		emit(":").emit(key, false);
		return getSelf();
	}
}
