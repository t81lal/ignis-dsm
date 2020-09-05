package com.bilbtopia.forager.dsm.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;

import com.bilbtopia.forager.dsm.ModelPrinter;
import com.bilbtopia.forager.dsm.model.Option.OptionKey;

public class Model {

	private final String modelName;
	// Needs to be ordered
	private final List<Option<?>> options;

	public Model(String modelName) {
		this.modelName = modelName;
		options = new ArrayList<>();
	}

	public <T> void addOption(Option<T> option) {
		this.options.add(option);
	}

	public void emit(ModelPrinter<?> printer, Map<String, ?> values, boolean forceLongPrint) {
		EmitterState state = new EmitterState(values);

		// print only if any of the model options are fulfiled
		if(!state.init()) return;
		
		printer.printAttributeHead(modelName);

		if (forceLongPrint) {
			state.doLongEmit(printer);
		} else {
			state.doShortEmit(printer);
		}
	}

	class EmitterState {
		Map<Option<?>, Object> presentOptionValues;

		EmitterState(Map<String, ?> values) {
			this.presentOptionValues = getPresentValues(values);
		}
		
		public boolean init() {
			return !presentOptionValues.isEmpty();
		}

		public void doShortEmit(ModelPrinter<?> printer) {
			for (Option<?> option : options) {
				tryEmitOption(printer, option, true);
			}
		}

		public void doLongEmit(ModelPrinter<?> printer) {
			printer.emit("{").incIndent().newLine();
			for (Option<?> option : options) {
				tryEmitOption(printer, option, false);
			}
			printer.decIndent().newLine().emit("}").newLine();
		}

		private Map<Option<?>, Object> getPresentValues(Map<String, ?> values) {
			Map<Option<?>, Object> present = new LinkedHashMap<>();
			for (Option<?> option : options) {
				OptionKey<?> optionKey = option.getKey();
				Optional<?> valueOption = findObjectForKey(values, optionKey);
				if (valueOption.isPresent()) {
					present.put(option, valueOption.get());
				}
			}
			return present;
		}

		<T> Optional<T> findObjectForKey(Map<String, ?> values, OptionKey<T> key) {
			for (Entry<String, ?> e : values.entrySet()) {
				if (!e.getKey().equals(key.getName())) {
					continue;
				}

				Object value = e.getValue();
				if (value == null) {
					continue;
				}
				Class<?> givenValueType = value.getClass();
				if (key.getValueType().isAssignableFrom(givenValueType)) {
					return Optional.of(key.getValueType().cast(value));
				}
			}
			return Optional.empty();
		}

		private <T> Optional<T> getCachedValue(Option<T> option) {
			if (presentOptionValues.containsKey(option)) {
				OptionKey<T> optionKey = option.getKey();
				return Optional.of(optionKey.getValueType().cast(presentOptionValues.get(option)));
			} else {
				return Optional.empty();
			}
		}

		private <T> void tryEmitOption(ModelPrinter<?> printer, Option<T> option, boolean valueOnly) {
			Optional<T> valueOption = getCachedValue(option);
			valueOption.ifPresent(v -> {
				if (valueOnly) {
					option.getEmitter().emit(printer, v);
				} else {
					printer.printAttributeHead(option.getKey().getName());
					option.getEmitter().emit(printer, v);
					printer.newLine();
				}
			});
		}
	}
}
