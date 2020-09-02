package com.bilbtopia.forager.dsm;

public class WithInners {

    public static class StaticA {
        static int x = 10;
    }
    
    public class DependentB {
        int z = 1;
    }
}
