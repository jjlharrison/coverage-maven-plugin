/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes;

public class NewFileCodeCoverage extends FileNewCodeCoverage
{
    @Override
    protected String describeChangeType()
    {
        return "New file";
    }

    @Override
    public String describe()
    {
        return summariseChangeAndCoverage();
    }
}
