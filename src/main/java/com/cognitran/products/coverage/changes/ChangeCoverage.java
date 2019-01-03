/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

import org.apache.maven.plugin.logging.Log;

/**
 * Change coverage information.
 */
public abstract class ChangeCoverage
{
    /** The count of changed branches that are covered by tests. */
    private int coveredChangedBranchesCount;

    /** The count of changed lines that are covered by tests. */
    private int coveredChangedLinesCount;

    /** The count of changed branches that are not covered by tests. */
    private int missedChangedBranchesCount;

    /** The count of changed lines that are not covered by tests. */
    private int missedChangedLinesCount;

    /**
     * Constructor.
     */
    public ChangeCoverage()
    {
        // Default
    }

    /**
     * Constructor.
     *
     * @param coveredChangedLinesCount the count of changed lines that are covered by tests.
     * @param missedChangedLinesCount the count of changed lines that are not covered by tests.
     * @param coveredChangedBranchesCount the count of changed branches that are covered by tests.
     * @param missedChangedBranchesCount the count of changed branches that are not covered by tests.
     */
    public ChangeCoverage(final int coveredChangedLinesCount, final int missedChangedLinesCount, final int coveredChangedBranchesCount,
                          final int missedChangedBranchesCount)
    {
        this.coveredChangedLinesCount = coveredChangedLinesCount;
        this.missedChangedLinesCount = missedChangedLinesCount;
        this.coveredChangedBranchesCount = coveredChangedBranchesCount;
        this.missedChangedBranchesCount = missedChangedBranchesCount;
    }

    /**
     * Describes the coverage information.
     *
     * @return the description of the coverage.
     */
    public abstract String describe();

    /**
     * Writes the description to the given log.
     *
     * @param log the log.
     */
    public void describe(final Log log)
    {
        final boolean coverageComplete = isCoverageComplete();
        if (!coverageComplete || log.isDebugEnabled())
        {
            final Consumer<CharSequence> logger = coverageComplete ? log::debug : log::warn;
            logger.accept(describe());
        }
    }

    /**
     * Returns the count of changed branches that are covered by tests.
     *
     * @return the count of changed branches that are covered by tests.
     */
    public int getCoveredChangedBranchesCount()
    {
        return coveredChangedBranchesCount;
    }

    /**
     * Sets the count of changed branches that are covered by tests.
     *
     * @param coveredChangedBranchesCount the count of changed branches that are covered by tests.
     */
    public void setCoveredChangedBranchesCount(final int coveredChangedBranchesCount)
    {
        this.coveredChangedBranchesCount = coveredChangedBranchesCount;
    }

    /**
     * Returns the count of changed lines that are covered by tests.
     *
     * @return the count of changed lines that are covered by tests.
     */
    public int getCoveredChangedLinesCount()
    {
        return coveredChangedLinesCount;
    }

    /**
     * Sets the count of changed lines that are covered by tests.
     *
     * @param coveredChangedLinesCount the count of changed lines that are covered by tests.
     */
    public void setCoveredChangedLinesCount(final int coveredChangedLinesCount)
    {
        this.coveredChangedLinesCount = coveredChangedLinesCount;
    }

    /**
     * Returns the count of changed branches that are not covered by tests.
     *
     * @return the count of changed branches that are not covered by tests.
     */
    public int getMissedChangedBranchesCount()
    {
        return missedChangedBranchesCount;
    }

    /**
     * Sets the count of changed branches that are not covered by tests.
     *
     * @param missedChangedBranchesCount the count of changed branches that are not covered by tests.
     */
    public void setMissedChangedBranchesCount(final int missedChangedBranchesCount)
    {
        this.missedChangedBranchesCount = missedChangedBranchesCount;
    }

    /**
     * Returns the count of changed lines that are not covered by tests.
     *
     * @return the count of changed lines that are not covered by tests.
     */
    public int getMissedChangedLinesCount()
    {
        return missedChangedLinesCount;
    }

    /**
     * Sets the count of changed lines that are not covered by tests.
     *
     * @param missedChangedLinesCount the count of changed lines that are not covered by tests.
     */
    public void setMissedChangedLinesCount(final int missedChangedLinesCount)
    {
        this.missedChangedLinesCount = missedChangedLinesCount;
    }

    /**
     * Returns the count of branches on changed lines.
     *
     * @return the count of branches on changed lines.
     */
    public int getTotalChangedBranchesCount()
    {
        return getCoveredChangedBranchesCount() + getMissedChangedBranchesCount();
    }

    /**
     * Returns the count of changes lines.
     *
     * @return the count of changes lines.
     */
    public int getTotalChangedLinesCount()
    {
        return getCoveredChangedLinesCount() + getMissedChangedLinesCount();
    }

    /**
     * Returns whether there are any testable instructions or branches in the changed code.
     *
     * @return whether there are any testable instructions or branches in the changed code.
     */
    public boolean hasTestableChanges()
    {
        return (getTotalChangedLinesCount() + getTotalChangedBranchesCount()) > 0;
    }

    @Override
    public final String toString()
    {
        return describe();
    }

    /**
     * Summarises the type of change.
     *
     * @return the summary of the change type.
     */
    protected abstract String summariseChangeType();

    /**
     * Returns whether all the testable branches or instructions are covered.
     *
     * @return whether all the testable branches or instructions are covered.
     */
    protected boolean isCoverageComplete()
    {
        return getTotalChangedBranchesCount() <= getCoveredChangedBranchesCount()
               && getTotalChangedLinesCount() <= getCoveredChangedLinesCount();
    }

    /**
     * Summarises the branch coverage.
     *
     * @return the summary of branch coverage.
     */
    @Nonnull
    protected String summariseBranchCoverage()
    {
        return getCoveredChangedBranchesCount() + "/" + getTotalChangedBranchesCount() + " new branches covered";
    }

    /**
     * Summarises the change and the associated coverage.
     *
     * @return the summary of the change and the associated coverage.
     */
    protected abstract String summariseChangeAndCoverage();

    /**
     * Summarises the line coverage.
     *
     * @return the summary of line coverage.
     */
    @Nonnull
    protected String summariseLineCoverage()
    {
        return getCoveredChangedLinesCount() + "/" + getTotalChangedLinesCount() + " new lines covered";
    }
}
