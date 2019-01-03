/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes;

import javax.annotation.Nonnull;

/**
 * Change coverage information for a file.
 */
public abstract class FileChangeCoverage extends ChangeCoverage
{
    /** The project/module source root relative file path. */
    private String filePath;

    /**
     * Constructor.
     *
     * @param filePath the path of the modified file.
     */
    public FileChangeCoverage(final String filePath)
    {
        this.filePath = filePath;
    }

    /**
     * Returns the project/module source root relative file path.
     *
     * @return the project/module source root relative file path.
     */
    public String getFilePath()
    {
        return filePath;
    }

    /**
     * Sets the project/module source root relative file path.
     *
     * @param filePath the project/module source root relative file path.
     */
    public void setFilePath(final String filePath)
    {
        this.filePath = filePath;
    }

    @Nonnull
    @Override
    protected String summariseChangeAndCoverage()
    {
        final String branches = getTotalChangedBranchesCount() > 0 ? (", " + summariseBranchCoverage()) : "";
        return summariseChangeType() + " " + getFilePath() + ": " + summariseLineCoverage() + branches;
    }
}
