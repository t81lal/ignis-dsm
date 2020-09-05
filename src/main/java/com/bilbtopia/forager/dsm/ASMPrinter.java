package com.bilbtopia.forager.dsm;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;

import org.objectweb.asm.Attribute;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InnerClassNode;
import org.objectweb.asm.tree.RecordComponentNode;
import org.objectweb.asm.tree.TypeAnnotationNode;

import com.bilbtopia.forager.dsm.model.Model;
import com.bilbtopia.forager.dsm.model.Option;
import com.bilbtopia.forager.dsm.model.Option.OptionKey;
import com.bilbtopia.forager.dsm.util.FunctionUtil;
import com.bilbtopia.forager.dsm.util.MapUtil;
import com.bilbtopia.forager.dsm.util.ReflectionUtil;
import com.bilbtopia.forager.dsm.util.StringUtil;

public class ASMPrinter extends ModelPrinter<ASMPrinter> {

	private final ClassNode klass;

	public ASMPrinter(ClassNode klass) {
		this.klass = klass;
	}


//	private ASMPrinter printKlassTypes(List<String> klassTypes) {
//		return printListLiterals(klassTypes, this::printKlassType);
//	}
//
//	private ASMPrinter printAnnotationTable(String tableName, List<? extends AnnotationNode> nodes) {
//		return printList(nodes, node -> {
//			printDirectiveName("tableAttr").emit(tableName).printAnnotationNode(node);
//		});
//	}
//
//	private ASMPrinter printRawAttributes(List<Attribute> attrs) {
//		return printList(attrs, attr -> {
//			byte[] data = ReflectionUtil.getAttributeContentRefl(attr);
//			printDirectiveName("rawAttr").printStringLiteral(attr.type).printStringLiteral(StringUtil.bytesToHex(data));
//		});
//	}
//
//	private ASMPrinter printInnerClasses(List<InnerClassNode> klasses) {
//		return printList(klasses, k -> {
//			// InnerClassName innerName and outerName are linked to this klass and this inner klass so don't repeat
//			if (!k.outerName.equals(klass.name)) {
//				//                throw new IllegalArgumentException("Outer: " + k.outerName + " but we're in " + klass.name);
//			}
//			if (!k.innerName.equals(StringUtil.getInnerKlassName(k.name))) {
//				throw new IllegalArgumentException("Full name: " + k.name + " but simple is: " + k.innerName);
//			}
//			printDirectiveName("innerClass").printInteger(k.access).printKlassType(k.name);
//		});
//	}
//
//	private ASMPrinter printRecordComponents(List<RecordComponentNode> recs) {
//		return printList(recs, r -> {
//			printDirectiveName("record").printRecordComponent(r);
//		});
//	}
//
//	private void printRecordComponent(RecordComponentNode r) {
//		emit("{").incIndent();
//
//		newLine().emit("name = ").printStringLiteral(r.name);
//		newLine().emit("descriptor = ").printTypeLiteral(Type.getType(r.descriptor));
//		if(r.signature != null) {
//			newLine().emit("signature = ").printStringLiteral(r.signature);
//		}
//
//		printAnnotationTable("invisible", r.invisibleAnnotations);
//		printAnnotationTable("invisibleType", r.invisibleTypeAnnotations);
//		printAnnotationTable("visible", r.visibleAnnotations);
//		printAnnotationTable("visibleType", r.visibleTypeAnnotations);
//		printRawAttributes(r.attrs);
//
//		decIndent().newLine().emit("}");
//	}


	public void printKlass() {
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

		printDirective("nestHostClass", klass.nestHostClass, this::printKlassType);
		printDirective("nestMembers", klass.nestMembers, this::printKlassTypes);

		// Add extra space before the class declaration
		newLine().emit("class").printKlassType(klass.name).newLine();

		incIndent();
		printList(klass.fields, this::printField);
		decIndent();
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
		// ClassReader cr = new ClassReader(ASMPrinter.class.getResourceAsStream("/recordtest/Person.class"));
		ClassReader cr = new ClassReader(WithInners.class.getCanonicalName());
		ClassNode cn = new ClassNode();
		cr.accept(cn, 0);

		ASMPrinter printer = new ASMPrinter(cn);
		
		Models.makeSourceModel().emit(printer, MapUtil.of("file", cn.sourceFile).and("debug", cn.sourceDebug), true);
		Models.makeClassModel().emit(printer, MapUtil.ofHetero("name", cn.name).and("superName", cn.superName).and("interfaces", cn.interfaces), true);
//		Model m = printer.superClassModel();
//		m.emit(printer, Map.of("value", cn.superName), true);
		
		Set<Model> models = new HashSet<>();
//		models.add(printer.makeLiteralModel(String.class, "", emitter))
		
		System.out.println(printer.toString());
	}
}
