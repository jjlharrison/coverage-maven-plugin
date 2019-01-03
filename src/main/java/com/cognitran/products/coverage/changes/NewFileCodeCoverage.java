/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes;

/**
 * Coverage information for a new file.
 */
public class NewFileCodeCoverage extends FileChangeCoverage
{
    /**
     * Constructor.
     *
     * @param filePath the path of the new file.
     */
    public NewFileCodeCoverage(final String filePath)
    {
        super(filePath);
    }

    @Override
    protected String summariseChangeType()
    {
        return "New file";
    }

    @Override
    public String describe()
    {
        return summariseChangeAndCoverage();
    }
}
