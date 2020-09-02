package com.bilbtopia.forager.dsm.util;

import java.lang.reflect.Field;

import org.objectweb.asm.Attribute;

public class ReflectionUtil {
    public static byte[] getAttributeContentRefl(Attribute attr) {
        try {
            Field f = attr.getClass().getDeclaredField("content");
            f.setAccessible(true);
            byte[] content = (byte[]) f.get(attr);
            f.setAccessible(false);
            return content;
        } catch (NoSuchFieldException | SecurityException | IllegalArgumentException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }
}
