package com.cognitran.products.coverage;

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
