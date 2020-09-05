package com.bilbtopia.forager.dsm;

import java.util.List;

import com.bilbtopia.forager.dsm.model.Model;
import com.bilbtopia.forager.dsm.model.Option;
import com.bilbtopia.forager.dsm.model.Option.OptionKey;

public class Models {
	public static <T> Option<T> makeOption(Class<T> valueType, String name, Emitter<T> emitter) {
		return new Option<>(OptionKey.getKey(valueType, name), emitter);
	}

	public static <T> Model makeLiteralModel(Class<T> valueType, String name, Emitter<T> emitter) {
		Model model = new Model(name);
		model.addOption(makeOption(valueType, "value", emitter));
		return model;
	}

	public static Model makeClassModel() {
		Model model = new Model("class");
		model.addOption(makeOption(String.class, "name", Emitters.klassType()));
		model.addOption(makeOption(String.class, "superName", Emitters.klassType()));
		// sigh... (it was almost perfect)
		model.addOption(makeOption(List.class, "interfaces", Emitters.listOf((Emitter)Emitters.klassType())));
		return model;
	}

	public static Model makeSourceModel() {
		Model model = new Model("source");
		model.addOption(makeOption(String.class, "file", Emitters.literal(true)));
		model.addOption(makeOption(String.class, "debug", Emitters.literal(true)));
		return model;
	}
}
