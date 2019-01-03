/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes;

import java.util.Objects;

import javax.annotation.Nonnull;

public class LineCodeCoverage extends NewCodeCoverage implements Comparable<LineCodeCoverage>
{
    private final int lineNumber;

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
    protected String describeChangeType()
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

    public int getLineNumber()
    {
        return lineNumber;
    }
}
