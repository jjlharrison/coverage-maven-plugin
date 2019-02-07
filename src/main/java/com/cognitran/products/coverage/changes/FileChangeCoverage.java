/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes;

/**
 * Change coverage information for a file.
 */
public interface FileChangeCoverage extends ChangeCoverage
{
    /**
     * Returns the project/module source root relative file path.
     *
     * @return the project/module source root relative file path.
     */
    String getFilePath();
}
