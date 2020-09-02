package com.bilbtopia.forager.dsm.test.annotations;

@NestedListedAnnoWithAnnoField(cs = { '\n', '\t', '\f', 'c' }, x = "goodbye", y = "word\b\b\b", theAnnos = {
        @PlaceholderAnno(nums = { 1, 2 }), @PlaceholderAnno(nums = { 3, 4 }) })
@NestedListedAnnoWithAnnoField(cs = { '\n', '\t', '\f', 'c' }, x = "ok", y = "word\b\b\b", theAnnos = {
        @PlaceholderAnno(nums = { 1, 2 }), @PlaceholderAnno(nums = { 3, 4 }) })
public class DoubleAnnotatedClass extends @PlaceholderAnno(nums = {
        0 }) Object implements Comparable<DoubleAnnotatedClass>, Cloneable{

    @Override
    public int compareTo(DoubleAnnotatedClass o) {
        return 0;
    }
}
