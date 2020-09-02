package com.bilbtopia.forager.dsm.test.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@NestedListedAnnoWithAnnoField(x = "hello", y = "word", theAnnos = @PlaceholderAnno(nums = { 1, 2, 3 }), cs = { 0 })
public @interface PlaceholderAnno {
    int[] nums();
}
