/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes;

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
}
