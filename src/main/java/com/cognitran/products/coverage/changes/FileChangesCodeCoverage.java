/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes;

import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

public class FileChangesCodeCoverage extends FileNewCodeCoverage
{
    private Set<Integer> changedLines = new HashSet<>();

    private Set<LineCodeCoverage> coverage = new TreeSet<>();

    public Set<Integer> getChangedLines()
    {
        return changedLines;
    }

    public void setChangedLines(final Set<Integer> changedLines)
    {
        this.changedLines = changedLines;
    }

    public Set<LineCodeCoverage> getCoverage()
    {
        return coverage;
    }

    @Override
    public String describe()
    {
        return summariseChangeAndCoverage() + ":\n" +
               coverage.stream().map(LineCodeCoverage::describe).map(s -> "    " + s).collect(Collectors.joining("\n"));
    }

    @Nonnull
    protected String describeChangeType()
    {
        return "Changed file";
    }

    @Override
    public int getCoveredChangedLinesCount()
    {
        return coverage.stream().mapToInt(NewCodeCoverage::getCoveredChangedLinesCount).sum();
    }

    @Override
    public int getMissedChangedLinesCount()
    {
        return coverage.stream().mapToInt(NewCodeCoverage::getMissedChangedLinesCount).sum();
    }

    @Override
    public int getTotalChangedLinesCount()
    {
        return coverage.stream().mapToInt(NewCodeCoverage::getTotalChangedLinesCount).sum();
    }

    @Override
    public int getCoveredChangedBranchesCount()
    {
        return coverage.stream().mapToInt(NewCodeCoverage::getCoveredChangedBranchesCount).sum();
    }

    @Override
    public int getMissedChangedBranchesCount()
    {
        return coverage.stream().mapToInt(NewCodeCoverage::getMissedChangedBranchesCount).sum();
    }

    @Override
    public int getTotalChangedBranchesCount()
    {
        return coverage.stream().mapToInt(NewCodeCoverage::getTotalChangedBranchesCount).sum();
    }
}
