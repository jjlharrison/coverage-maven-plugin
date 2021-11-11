package com.jjlharrison.coverage.changes;

import java.util.Objects;

import javax.annotation.Nonnull;

/**
 * Coverage information about a changed line.
 */
public class LineCodeCoverage extends AbstractChangeCoverage implements Comparable<LineCodeCoverage>
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
    public String describe(final boolean includeCoveredDetail)
    {
        return summariseChangeAndCoverage();
    }

    @Override
    public String summariseChangeType()
    {
        return "Line change";
    }

    @Override
    public String summariseChangeAndCoverage()
    {
        final int branchesCount = getTotalChangedBranchesCount();
        return "Line " + getLineNumber() + ": line " + (getCoveredChangedLinesCount() == 1 ? "covered" : "not covered") +
               (branchesCount > 0 ? ", " + (getCoveredChangedBranchesCount() + "/" + branchesCount + " branches covered.") : "");
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

    @Override
    public boolean equals(final Object other)
    {
        if (other instanceof LineCodeCoverage)
        {
            final LineCodeCoverage that = (LineCodeCoverage) other;
            return that.canEqual(this) && this.getLineNumber() == that.getLineNumber() && super.equals(that);
        }
        return false;
    }

    @Override
    public boolean canEqual(final Object other)
    {
        return other instanceof LineCodeCoverage;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(super.hashCode(), getLineNumber());
    }
}
