package com.bilbtopia.forager.dsm.test.annotations;

import java.lang.annotation.Repeatable;

@Repeatable(value = NestedListedAnnoWithAnnoField.List.class)
public @interface NestedListedAnnoWithAnnoField {
    char[] cs();

    String x();

    String y();

    PlaceholderAnno[] theAnnos();

    @interface List {

        NestedListedAnnoWithAnnoField[] value();

    }
}
