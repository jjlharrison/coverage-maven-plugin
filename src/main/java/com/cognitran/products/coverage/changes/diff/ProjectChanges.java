/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes.diff;

import java.util.Map;
import java.util.Set;

/**
 * Information about the changes made to the project (or module).
 */
public class ProjectChanges
{
    /** The lines that have been changed mapped by file. */
    private final Map<String, Set<Integer>> changedLinesByFile;

    /** New files that have been added. */
    private final Set<String> newFiles;

    /**
     * Constructor.
     *
     * @param changedLinesByFile lines that have been changed mapped by file.
     * @param newFiles new files that have been added.
     */
    public ProjectChanges(final Map<String, Set<Integer>> changedLinesByFile, final Set<String> newFiles)
    {
        this.changedLinesByFile = changedLinesByFile;
        this.newFiles = newFiles;
    }

    /**
     * Returns lines that have been changed mapped by file.
     *
     * @return lines that have been changed mapped by file.
     */
    public Map<String, Set<Integer>> getChangedLinesByFile()
    {
        return changedLinesByFile;
    }

    /**
     * Returns new files that have been added.
     *
     * @return new files that have been added.
     */
    public Set<String> getNewFiles()
    {
        return newFiles;
    }
}
