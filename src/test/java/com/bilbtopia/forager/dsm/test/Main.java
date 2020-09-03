package com.bilbtopia.forager.dsm.test;

import java.io.IOException;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import com.bilbtopia.forager.dsm.ASMPrinter;
import com.bilbtopia.forager.dsm.test.annotations.DoubleAnnotatedClass;

public class Main {

    public static void main(String[] args) throws IOException {
        ClassReader cr = new ClassReader(DoubleAnnotatedClass.class.getCanonicalName());
        ClassNode cn = new ClassNode();
        cr.accept(cn, 0);

        ASMPrinter printer = new ASMPrinter(cn);
        printer.printKlass();

        System.out.println(printer.toString());
    }
}
