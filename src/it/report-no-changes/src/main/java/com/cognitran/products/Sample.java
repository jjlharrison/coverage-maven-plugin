/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products;

public class Sample
{
    public String coveredMethod(final boolean condition)
    {
        if (condition)
        {
            return "Branch 1";
        }
        else
        {
            return "Branch 2";
        }
    }

    public String methodWithUncoveredBranch(final boolean condition)
    {
        if (condition)
        {
            return "Covered Branch";
        }
        else
        {
            return "Uncovered Branch";
        }
    }
}
