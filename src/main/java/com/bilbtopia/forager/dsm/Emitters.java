package com.bilbtopia.forager.dsm;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

import com.bilbtopia.forager.dsm.util.MapUtil;
import com.bilbtopia.forager.dsm.util.StringUtil;

public class Emitters {

	private static Map<Class<? extends Number>, String> NUMERIC_LITERAL_PREFIX = Map.of(Byte.class, "b", Short.class,
			"s", Float.class, "f", Double.class, "d", Long.class, "l", Integer.class, "");

	private static void printStringLiteral(BasePrinter<?> printer, String str) {
		printer.emit("\"").emit(StringUtil.javaEscapeString(str), false).emit("\"", false);
	}

	private static <N extends Number> void printNumericLiteral(BasePrinter<?> printer, N n) {
		Class<? extends Number> type = n.getClass();
		if (NUMERIC_LITERAL_PREFIX.containsKey(type)) {
			printer.emit(NUMERIC_LITERAL_PREFIX.get(type));
		}
		printer.emit(n.toString(), false);
	}

	private static void printCharacterLiteral(BasePrinter<?> printer, char c) {
		printer.emit("'").emit(StringUtil.javaEscapeCharacter(c), false).emit("'", false);
	}

	private static boolean shouldPrintMultiline(List<? extends Object> list) {
		int stringLengths = 0;

		for (Object o : list) {
			if (o instanceof AnnotationNode) {
				return true;
			}

			if (o instanceof String) {
				stringLengths += ((String) o).length();
			} else {
				// estimated average (janky)
				stringLengths += 4;
			}
		}

		return stringLengths >= 60;
	}

	private static <T> void printListLiterals(BasePrinter<?> printer, List<T> values, Consumer<T> emitter) {
		if (values.isEmpty()) {
			printer.emit("[]");
			return;
		}
		printer.emit("[");
		boolean multiLine = shouldPrintMultiline(values);
		if (multiLine) {
			printer.incIndent();
		}
		for (T o : values) {
			if (multiLine) {
				printer.newLine();
			}
			emitter.accept(o);
		}
		if (multiLine) {
			printer.decIndent().newLine();
		}
		printer.emit("]");
	}

	private static void printTypeLiteral(BasePrinter<?> printer, Type t) {
		printer.emit(t.getDescriptor());
	}

	private static void printAnnotationNode(BasePrinter<?> printer, AnnotationNode node) {
		printer.emit("anno").emit(node.desc);

		Map<String, Object> pairs = MapUtil.getValuePairs(node.values);

		boolean isTypeAnno = node instanceof TypeAnnotationNode;
		boolean shouldPrintBody = !pairs.isEmpty() || isTypeAnno;

		if (!shouldPrintBody) {
			return;
		}

		printer.emit("{").incIndent();
		for (var e : pairs.entrySet()) {
			printer.newLine().emit(e.getKey()).emit("=");
			printLiteral(printer, e.getValue(), true);
		}

		if (isTypeAnno) {
			TypeAnnotationNode tan = (TypeAnnotationNode) node;
			printer.newLine().emit("#typeRef =");
			printLiteral(printer, tan.typeRef, false);
			if (tan.typePath != null) {
				printer.newLine().emit("#typePath =").emit(tan.typePath.toString());
			}
		}

		printer.decIndent().newLine().emit("}");
	}

	private static void printLiteral(BasePrinter<?> p, Object v, boolean printAnnotations) {
		Class<?> type = v.getClass();
		if (v instanceof Character) {
			printCharacterLiteral(p, (char) v);
		} else if (v instanceof String) {
			printStringLiteral(p, (String) v);
		} else if (v instanceof Number) {
			printNumericLiteral(p, (Number) v);
		} else if (v instanceof List) {
			printListLiterals(p, (List<?>) v, val -> printLiteral(p, val, true));
		} else if (v instanceof Type) {
			printTypeLiteral(p, (Type) v);
		} else if (printAnnotations && v instanceof AnnotationNode) {
			printAnnotationNode(p, (AnnotationNode) v);
		} else {
			throw new UnsupportedOperationException(String.valueOf(type));
		}
	}

	public static <T> Emitter<T> literal(boolean printAnnotations) {
		return (p, v) -> printLiteral(p, v, printAnnotations);
	}

	public static Emitter<String> klassType() {
		return (p, v) -> p.emit(v);
	}

	public static Emitter<String> signature() {
		return (p, v) -> p.emit(v);
	}

	public static <T> Emitter<List<T>> listOf(Emitter<T> perEmitter) {
		return (p, listT) -> {
			if (!listT.isEmpty()) {
				p.newLine();
			}

			for (T t : listT) {
				perEmitter.emit(p, t);
				p.forceNewLine();
			}
		};
	}
}
