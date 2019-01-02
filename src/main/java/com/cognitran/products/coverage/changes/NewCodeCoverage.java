/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes;

public abstract class NewCodeCoverage
{
    private int coveredChangedLinesCount;

    private int missedChangedLinesCount;

    private int coveredChangedBranchesCount;

    private int missedChangedBranchesCount;

    public NewCodeCoverage()
    {
    }

    public NewCodeCoverage(final int coveredChangedLinesCount, final int missedChangedLinesCount, final int coveredChangedBranchesCount, final int missedChangedBranchesCount)
    {
        this.coveredChangedLinesCount = coveredChangedLinesCount;
        this.missedChangedLinesCount = missedChangedLinesCount;
        this.coveredChangedBranchesCount = coveredChangedBranchesCount;
        this.missedChangedBranchesCount = missedChangedBranchesCount;
    }

    public int getCoveredChangedLinesCount()
    {
        return coveredChangedLinesCount;
    }

    public int getMissedChangedLinesCount()
    {
        return missedChangedLinesCount;
    }

    public int getTotalChangedLinesCount()
    {
        return getCoveredChangedLinesCount() + getMissedChangedLinesCount();
    }

    public int getCoveredChangedBranchesCount()
    {
        return coveredChangedBranchesCount;
    }

    public int getMissedChangedBranchesCount()
    {
        return missedChangedBranchesCount;
    }

    public int getTotalChangedBranchesCount()
    {
        return getCoveredChangedBranchesCount() + getMissedChangedBranchesCount();
    }

    public void setCoveredChangedLinesCount(final int coveredChangedLinesCount)
    {
        this.coveredChangedLinesCount = coveredChangedLinesCount;
    }

    public void setMissedChangedLinesCount(final int missedChangedLinesCount)
    {
        this.missedChangedLinesCount = missedChangedLinesCount;
    }

    public void setCoveredChangedBranchesCount(final int coveredChangedBranchesCount)
    {
        this.coveredChangedBranchesCount = coveredChangedBranchesCount;
    }

    public void setMissedChangedBranchesCount(final int missedChangedBranchesCount)
    {
        this.missedChangedBranchesCount = missedChangedBranchesCount;
    }

    public abstract String describe();

    public boolean hasTestableChanges()
    {
        return (getTotalChangedLinesCount() + getTotalChangedBranchesCount()) > 0;
    }

    @Override
    public final String toString()
    {
        return describe();
    }
}
