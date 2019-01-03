/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;

/**
 * Change coverage information for a modified file.
 */
public class ModifiedFileChangeCoverage extends FileChangeCoverage
{
    /** The changed line numbers. */
    private Set<Integer> changedLineNumbers;

    /** The coverage information for changed lines. */
    private Set<LineCodeCoverage> coverage = new TreeSet<>();

    /**
     * Constructor.
     *
     * @param filePath the path of the modified file.
     * @param changedLineNumbers the changed line numbers.
     */
    public ModifiedFileChangeCoverage(final String filePath, final Set<Integer> changedLineNumbers)
    {
        super(filePath);
        this.changedLineNumbers = changedLineNumbers;
    }

    /**
     * Returns the changed line numbers.
     *
     * @return the changed line numbers.
     */
    public Set<Integer> getChangedLineNumbers()
    {
        return changedLineNumbers;
    }

    /**
     * Sets the changed line numbers.
     *
     * @param changedLineNumbers the changed line numbers.
     */
    public void setChangedLineNumbers(final Set<Integer> changedLineNumbers)
    {
        this.changedLineNumbers = changedLineNumbers;
    }

    /**
     * Returns the coverage information for changed lines.
     *
     * @return the coverage information for changed lines.
     */
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
    @Override
    protected String summariseChangeType()
    {
        return "Changed file";
    }

    @Override
    public int getCoveredChangedLinesCount()
    {
        return coverage.stream().mapToInt(ChangeCoverage::getCoveredChangedLinesCount).sum();
    }

    @Override
    public int getMissedChangedLinesCount()
    {
        return coverage.stream().mapToInt(ChangeCoverage::getMissedChangedLinesCount).sum();
    }

    @Override
    public int getTotalChangedLinesCount()
    {
        return coverage.stream().mapToInt(ChangeCoverage::getTotalChangedLinesCount).sum();
    }

    @Override
    public int getCoveredChangedBranchesCount()
    {
        return coverage.stream().mapToInt(ChangeCoverage::getCoveredChangedBranchesCount).sum();
    }

    @Override
    public int getMissedChangedBranchesCount()
    {
        return coverage.stream().mapToInt(ChangeCoverage::getMissedChangedBranchesCount).sum();
    }

    @Override
    public int getTotalChangedBranchesCount()
    {
        return coverage.stream().mapToInt(ChangeCoverage::getTotalChangedBranchesCount).sum();
    }
}
