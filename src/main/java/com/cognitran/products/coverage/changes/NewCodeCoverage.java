/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.apache.maven.plugin.logging.Log;

public abstract class NewCodeCoverage
{
    private int coveredChangedBranchesCount;

    private int coveredChangedLinesCount;

    private int missedChangedBranchesCount;

    private int missedChangedLinesCount;

    public NewCodeCoverage()
    {
        // Default
    }

    public NewCodeCoverage(final int coveredChangedLinesCount, final int missedChangedLinesCount, final int coveredChangedBranchesCount,
                           final int missedChangedBranchesCount)
    {
        this.coveredChangedLinesCount = coveredChangedLinesCount;
        this.missedChangedLinesCount = missedChangedLinesCount;
        this.coveredChangedBranchesCount = coveredChangedBranchesCount;
        this.missedChangedBranchesCount = missedChangedBranchesCount;
    }

    public abstract String describe();

    public void describe(final Log log)
    {
        final boolean coverageComplete = isCoverageComplete();
        if (!coverageComplete || log.isDebugEnabled())
        {
            final Consumer<CharSequence> logger = coverageComplete ? log::debug : log::warn;
            logger.accept(describe());
        }
    }

    public int getCoveredChangedBranchesCount()
    {
        return coveredChangedBranchesCount;
    }

    public void setCoveredChangedBranchesCount(final int coveredChangedBranchesCount)
    {
        this.coveredChangedBranchesCount = coveredChangedBranchesCount;
    }

    public int getCoveredChangedLinesCount()
    {
        return coveredChangedLinesCount;
    }

    public void setCoveredChangedLinesCount(final int coveredChangedLinesCount)
    {
        this.coveredChangedLinesCount = coveredChangedLinesCount;
    }

    public int getMissedChangedBranchesCount()
    {
        return missedChangedBranchesCount;
    }

    public void setMissedChangedBranchesCount(final int missedChangedBranchesCount)
    {
        this.missedChangedBranchesCount = missedChangedBranchesCount;
    }

    public int getMissedChangedLinesCount()
    {
        return missedChangedLinesCount;
    }

    public void setMissedChangedLinesCount(final int missedChangedLinesCount)
    {
        this.missedChangedLinesCount = missedChangedLinesCount;
    }

    public int getTotalChangedBranchesCount()
    {
        return getCoveredChangedBranchesCount() + getMissedChangedBranchesCount();
    }

    public int getTotalChangedLinesCount()
    {
        return getCoveredChangedLinesCount() + getMissedChangedLinesCount();
    }

    public boolean hasTestableChanges()
    {
        return (getTotalChangedLinesCount() + getTotalChangedBranchesCount()) > 0;
    }

    @Override
    public final String toString()
    {
        return describe();
    }

    protected abstract String describeChangeType();

    protected boolean isCoverageComplete()
    {
        return getTotalChangedBranchesCount() <= getCoveredChangedBranchesCount()
               && getTotalChangedLinesCount() <= getCoveredChangedLinesCount();
    }

    @Nonnull
    protected String summariseBranchCoverage()
    {
        return getCoveredChangedBranchesCount() + "/" + getTotalChangedBranchesCount() + " new branches covered";
    }

    protected abstract String summariseChangeAndCoverage();

    @Nonnull
    protected String summariseLineCoverage()
    {
        return getCoveredChangedLinesCount() + "/" + getTotalChangedLinesCount() + " new lines covered";
    }
}
