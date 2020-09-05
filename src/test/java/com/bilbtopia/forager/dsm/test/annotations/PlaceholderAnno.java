package com.bilbtopia.forager.dsm.test.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE_USE)
@NestedListedAnnoWithAnnoField(x = "hello", y = "word", theAnnos = @PlaceholderAnno(nums = { 1, 2, 3 }), cs = { 0 })
public @interface PlaceholderAnno {
    int[] nums();
}
