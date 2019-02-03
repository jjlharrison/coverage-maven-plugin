/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products;

import org.junit.Test;

public class SampleTest
{
    @Test
    public void test()
    {
        final Sample sample = new Sample();
        assert sample.coveredMethod(true).equals("Branch 1");
        assert sample.coveredMethod(false).equals("Branch 2");
        assert sample.methodWithUncoveredBranch(true).equals("Covered Branch");
    }
}
