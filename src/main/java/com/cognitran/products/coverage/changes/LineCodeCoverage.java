/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Coverage information about a changed line.
 */
public class LineCodeCoverage extends ChangeCoverage implements Comparable<LineCodeCoverage>
{
    /** The number of the changed line. */
    private final int lineNumber;

    /**
     * Constructor.
     *
     * @param lineNumber the number of the changed line.
     * @param coveredLinesCount the count of changed lines that are covered by tests.
     * @param missedLinesCount the count of changed lines that are not covered by tests.
     * @param coveredBranchesCount the count of changed branches that are covered by tests.
     * @param missedBranchesCount the count of changed branches that are not covered by tests.
     */
    public LineCodeCoverage(final int lineNumber, final int coveredLinesCount, final int missedLinesCount, final int coveredBranchesCount,
                            final int missedBranchesCount)
    {
        super(coveredLinesCount, missedLinesCount, coveredBranchesCount, missedBranchesCount);
        this.lineNumber = lineNumber;
    }

    @Override
    public int compareTo(@Nonnull final LineCodeCoverage o)
    {
        return Integer.compare(getLineNumber(), o.getLineNumber());
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }
        final LineCodeCoverage that = (LineCodeCoverage) o;
        return getLineNumber() == that.getLineNumber();
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getLineNumber());
    }

    @Override
    public String describe()
    {
        return summariseChangeAndCoverage();
    }

    @Override
    protected String summariseChangeType()
    {
        return "Line change";
    }

    @Override
    protected String summariseChangeAndCoverage()
    {
        return "Line " + getLineNumber() + ": line " +
               (getCoveredChangedLinesCount() == 1 ? "covered" : "not covered") + ", " +
               (getCoveredChangedBranchesCount() + "/" + getTotalChangedBranchesCount() + " new branches covered.");
    }

    /**
     * Returns the number of the changed line.
     *
     * @return the number of the changed line.
     */
    public int getLineNumber()
    {
        return lineNumber;
    }
}
