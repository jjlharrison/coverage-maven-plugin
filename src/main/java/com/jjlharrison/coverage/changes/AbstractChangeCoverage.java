package com.jjlharrison.coverage.changes;

import java.util.Objects;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Change coverage information.
 */
public abstract class AbstractChangeCoverage implements ChangeCoverage
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
    public AbstractChangeCoverage()
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
    public AbstractChangeCoverage(final int coveredChangedLinesCount,
                                  final int missedChangedLinesCount,
                                  final int coveredChangedBranchesCount,
                                  final int missedChangedBranchesCount)
    {
        this.coveredChangedLinesCount = coveredChangedLinesCount;
        this.missedChangedLinesCount = missedChangedLinesCount;
        this.coveredChangedBranchesCount = coveredChangedBranchesCount;
        this.missedChangedBranchesCount = missedChangedBranchesCount;
    }

    /**
     * Returns the count of changed branches that are covered by tests.
     *
     * @return the count of changed branches that are covered by tests.
     */
    @Override
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
    @Override
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
    @Override
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
    @Override
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
    @Override
    public int getTotalChangedBranchesCount()
    {
        return getCoveredChangedBranchesCount() + getMissedChangedBranchesCount();
    }

    /**
     * Returns the count of changes lines.
     *
     * @return the count of changes lines.
     */
    @Override
    public int getTotalChangedLinesCount()
    {
        return getCoveredChangedLinesCount() + getMissedChangedLinesCount();
    }

    @Override
    public final String toString()
    {
        return describe(true);
    }

    @Override
    @SuppressFBWarnings(value = "EQ_UNUSUAL", justification = "SpotBugs does not recognise the canEqual pattern")
    public boolean equals(final Object other)
    {
        if (other instanceof LineCodeCoverage)
        {
            final LineCodeCoverage that = (LineCodeCoverage) other;
            return that.canEqual(this) &&
                   getCoveredChangedBranchesCount() == that.getCoveredChangedBranchesCount() &&
                   getCoveredChangedLinesCount() == that.getCoveredChangedLinesCount() &&
                   getMissedChangedBranchesCount() == that.getMissedChangedBranchesCount() &&
                   getMissedChangedLinesCount() == that.getMissedChangedLinesCount();
        }
        return false;
    }

    /**
     * Whether the given objects type can be equal to an object of this type.
     * See <a href="https://www.artima.com/lejava/articles/equality.html">How to Write an Equality Method in Java</a>.
     *
     * @param other the other object.
     * @return whether the given objects type can be equal to an object of this type.
     */
    public boolean canEqual(final Object other)
    {
        return other instanceof AbstractChangeCoverage;
    }

    @Override
    public int hashCode()
    {
        return Objects.hash(getCoveredChangedBranchesCount(),
                            getCoveredChangedLinesCount(),
                            getMissedChangedBranchesCount(),
                            getMissedChangedLinesCount());
    }
}
