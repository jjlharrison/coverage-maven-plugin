/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes;

import javax.annotation.Nonnull;

public abstract class FileNewCodeCoverage extends NewCodeCoverage
{
    private String filePath;

    public String getFilePath()
    {
        return filePath;
    }

    public void setFilePath(final String filePath)
    {
        this.filePath = filePath;
    }

    @Nonnull
    protected String summariseChangeAndCoverage()
    {
        final String branches = getTotalChangedBranchesCount() > 0 ? (", " + summariseBranchCoverage()) : "";
        return describeChangeType() + " " + getFilePath() + ": " + summariseLineCoverage() + branches;
    }
}
