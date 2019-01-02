package com.cognitran.products.coverage.changes.diff;

import java.util.Map;
import java.util.Set;

public class Changes
{
    private final Map<String, Set<Integer>> changedLinesByFile;

    private final Set<String> newFiles;

    public Changes(final Map<String, Set<Integer>> changedLinesByFile, final Set<String> newFiles)
    {
        this.changedLinesByFile = changedLinesByFile;
        this.newFiles = newFiles;
    }

    public Map<String, Set<Integer>> getChangedLinesByFile()
    {
        return changedLinesByFile;
    }

    public Set<String> getNewFiles()
    {
        return newFiles;
    }
}
