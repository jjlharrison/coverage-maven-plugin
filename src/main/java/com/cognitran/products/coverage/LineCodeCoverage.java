package com.cognitran.products.coverage;

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
    public String describe()
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
