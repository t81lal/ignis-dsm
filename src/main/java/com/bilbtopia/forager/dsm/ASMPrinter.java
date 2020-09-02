package com.bilbtopia.forager.dsm;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Function;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

import com.bilbtopia.forager.dsm.util.ReflectionUtil;
import com.bilbtopia.forager.dsm.util.StringUtil;

public class ASMPrinter extends BasePrinter<ASMPrinter> {

    private static Map<Class<? extends Number>, String> NUMERIC_LITERAL_PREFIX = Map.of(Byte.class, "b", Short.class,
            "s", Float.class, "f", Double.class, "d", Long.class, "l");

    private final ClassNode klass;

    public ASMPrinter(ClassNode klass) {
        this.klass = klass;
    }

    private ASMPrinter printDirectiveName(String directive) {
        return emit(":").emit(directive, false);
    }

    private <T> void printDirective(String name, T value, Consumer<T> emitter) {
        if (value != null) {
            printDirectiveName(name);
            emitter.accept(value);
            newLine();
        }
    }

    private <T> void printLiteralDirective(String name, T value) {
        printDirective(name, value, (v) -> printLiteral(v, false));
    }

    private ASMPrinter printInteger(int num, int base) {
        return emit(Integer.toString(num, base));
    }

    private ASMPrinter printInteger(int num) {
        return printInteger(num, 10);
    }

    private ASMPrinter printLiteral(Object o, boolean shouldPrintAnnotations) {
        Class<?> type = o.getClass();
        if (String.class.equals(type)) {
            return printStringLiteral((String) o);
        } else if (Character.class.equals(type)) {
            return printCharacterLiteral((char) o);
        } else if (Integer.class.equals(type)) {
            return printInteger((int) o);
        } else if (Number.class.isAssignableFrom(type)) {
            Number n = (Number) o;
            return printNumericLiteral(NUMERIC_LITERAL_PREFIX.get(type), n, x -> x.toString());
        } else if (shouldPrintAnnotations && AnnotationNode.class.equals(type)) {
            return printAnnotationNode((AnnotationNode) o);
        } else if (List.class.isAssignableFrom(type)) {
            return printListLiterals((List<?>) o, val -> printLiteral(val, true));
        } else if (Type.class.equals(o)) {
            return printTypeLiteral((Type) o);
        } else {
            throw new UnsupportedOperationException(String.valueOf(type));
        }
    }

    private ASMPrinter printKlassType(String klassType) {
        return emit(klassType);
    }

    private ASMPrinter printKlassTypes(List<String> klassTypes) {
        return printListLiterals(klassTypes, this::printKlassType);
    }

    private ASMPrinter printTypeLiteral(Type t) {
        return emit(t.getDescriptor());
    }

    private ASMPrinter emitSignature(String signature) {
        return emit(signature);
    }

    private boolean shouldPrintMultiline(List<? extends Object> list) {
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

    private <T> ASMPrinter printListLiterals(List<T> values, Consumer<T> emitter) {
        if (values.isEmpty()) {
            return emit("[]");
        }
        emit("[");
        boolean multiLine = shouldPrintMultiline(values);
        if (multiLine) {
            incIndent();
        }
        for (T o : values) {
            if (multiLine) {
                newLine();
            }
            emitter.accept(o);
        }
        if (multiLine) {
            decIndent().newLine();
        }
        emit("]");
        return this;
    }

    private ASMPrinter _emitCharacter(String charText) {
        return emit("'").emit(charText, false).emit("'", false);
    }

    private ASMPrinter printCharacterLiteral(char c) {
        return _emitCharacter(StringUtil.javaEscapeCharacter(c));
    }

    private <N extends Number> ASMPrinter printNumericLiteral(String prefix, N n, Function<N, String> toString) {
        return emit(prefix).emit(toString.apply(n), false);
    }

    private ASMPrinter printStringLiteral(String str) {
        return emit("\"").emit(StringUtil.javaEscapeString(str), false).emit("\"", false);
    }

    private ASMPrinter printAnnotationNode(AnnotationNode node) {
        emit("anno").emit(node.desc);

        Map<String, Object> pairs = getValuePairs(node.values);

        boolean isTypeAnno = node instanceof TypeAnnotationNode;
        boolean shouldPrintBody = !pairs.isEmpty() || isTypeAnno;

        if (!shouldPrintBody) {
            return this;
        }

        emit("{").incIndent();
        for (var e : pairs.entrySet()) {
            newLine().emit(e.getKey()).emit("=").printLiteral(e.getValue(), true);
        }

        if (isTypeAnno) {
            TypeAnnotationNode tan = (TypeAnnotationNode) node;
            newLine().emit("#typeRef =").printInteger(tan.typeRef);
            if (tan.typePath != null) {
                newLine().emit("#typePath =").emit(tan.typePath.toString());
            }
        }

        decIndent().newLine().emit("}");

        return this;
    }

    private ASMPrinter printAnnotationTable(String tableName, List<? extends AnnotationNode> nodes) {
        return printList(nodes, node -> {
            printDirectiveName("tableAttr").emit(tableName).printAnnotationNode(node);
        });
    }

    private ASMPrinter printRawAttributes(List<Attribute> attrs) {
        return printList(attrs, attr -> {
            byte[] data = ReflectionUtil.getAttributeContentRefl(attr);
            printDirectiveName("rawAttr").printStringLiteral(attr.type).printStringLiteral(StringUtil.bytesToHex(data));
        });
    }

    private ASMPrinter printInnerClasses(List<InnerClassNode> klasses) {
        return printList(klasses, k -> {
            // InnerClassName innerName and outerName are linked to this klass and this inner klass so don't repeat
            if (!k.outerName.equals(klass.name)) {
                throw new IllegalArgumentException("Outer: " + k.outerName + " but we're in " + k.name);
            }
            if (!k.innerName.equals(StringUtil.getInnerKlassName(k.name))) {
                throw new IllegalArgumentException("Full name: " + k.name + " but simple is: " + k.innerName);
            }
            printDirectiveName("innerClass").printInteger(k.access).printKlassType(k.name);
        });
    }

    private Map<String, Object> getValuePairs(List<Object> values) {
        if (values == null || values.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> pairs = new HashMap<>();
        final int n = values.size();
        for (int i = 0; i < n; i += 2) {
            pairs.put((String) values.get(i), values.get(i + 1));
        }
        return pairs;
    }

    private void printKlass() {
        printLiteralDirective("sourceFile", klass.sourceFile);
        printLiteralDirective("sourceDebug", klass.sourceDebug);

        printLiteralDirective("version", klass.version);
        printLiteralDirective("access", klass.access);
        printDirective("signature", klass.signature, this::emitSignature);
        printDirective("superName", klass.superName, this::printKlassType);
        printDirective("interfaces", klass.interfaces, this::printKlassTypes);

        // TODO: module
        printLiteralDirective("outerClass", klass.outerClass);
        printLiteralDirective("outerMethod", klass.outerMethod);
        printLiteralDirective("outerMethodDesc", klass.outerMethodDesc);

        printAnnotationTable("invisible", klass.invisibleAnnotations);
        printAnnotationTable("invisibleType", klass.invisibleTypeAnnotations);
        printAnnotationTable("visible", klass.visibleAnnotations);
        printAnnotationTable("visibleType", klass.visibleTypeAnnotations);
        printRawAttributes(klass.attrs);
        printInnerClasses(klass.innerClasses);

        // Add extra space before the class declaration
        newLine().emit("class").printKlassType(klass.name).newLine();

        incIndent();
        printList(klass.fields, this::printField);
        decIndent();
    }

    private <T> ASMPrinter printList(List<T> values, Consumer<T> perEmitter) {
        if (values == null)
            return this;

        if (!values.isEmpty()) {
            // Add extra space before the list
            newLine();
        }

        for (T val : values) {
            perEmitter.accept(val);
            newLine(); // space each item out
        }

        return this;
    }

    private ASMPrinter printField(FieldNode fn) {
        printLiteralDirective("access", fn.access);
        printDirective("signature", fn.signature, this::emitSignature);
        printLiteralDirective("value", fn.value);

        printAnnotationTable("invisible", fn.invisibleAnnotations);
        printAnnotationTable("invisibleType", fn.invisibleTypeAnnotations);
        printAnnotationTable("visible", fn.visibleAnnotations);
        printAnnotationTable("visibleType", fn.visibleTypeAnnotations);
        printRawAttributes(fn.attrs);

        emit("field").emit(fn.name).printTypeLiteral(Type.getType(fn.desc));
        newLine();

        return this;
    }

    public static void main(String[] args) throws IOException {
        ClassReader cr = new ClassReader(WithInners.class.getCanonicalName());
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        ASMPrinter printer = new ASMPrinter(cn);
        printer.printKlass();

        System.out.println(printer.toString());
    }
}
