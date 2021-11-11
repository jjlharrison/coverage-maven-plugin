package com.jjlharrison.coverage.changes;

import java.util.function.Consumer;

import javax.annotation.Nonnull;

/**
 * Change coverage information.
 */
public interface ChangeCoverage
{
    /**
     * Describes the coverage information.
     *
     * @param includeCoveredDetail whether to include detail about covered changes (in addition to detail about uncovered changes).
     * @return the description of the coverage.
     */
    default String describe(final boolean includeCoveredDetail)
    {
        return summariseChangeAndCoverage();
    }

    /**
     * Writes the description to the given log.
     *
     * @param log the log.
     */
    default void describe(final Logger log)
    {
        final boolean coverageComplete = isCoverageComplete();
        final boolean includeCoveredDetail = log.isDebugEnabled();
        if (!coverageComplete || includeCoveredDetail)
        {
            final Consumer<CharSequence> logger = coverageComplete ? log::debug : log::warn;
            logger.accept(describe(includeCoveredDetail));
        }
    }

    /**
     * Returns the count of changed branches that are covered by tests.
     *
     * @return the count of changed branches that are covered by tests.
     */
    int getCoveredChangedBranchesCount();

    /**
     * Returns the count of changed lines that are covered by tests.
     *
     * @return the count of changed lines that are covered by tests.
     */
    int getCoveredChangedLinesCount();

    /**
     * Returns the count of changed branches that are not covered by tests.
     *
     * @return the count of changed branches that are not covered by tests.
     */
    int getMissedChangedBranchesCount();

    /**
     * Returns the count of changed lines that are not covered by tests.
     *
     * @return the count of changed lines that are not covered by tests.
     */
    int getMissedChangedLinesCount();

    /**
     * Returns the count of branches on changed lines.
     *
     * @return the count of branches on changed lines.
     */
    int getTotalChangedBranchesCount();

    /**
     * Returns the count of changes lines.
     *
     * @return the count of changes lines.
     */
    int getTotalChangedLinesCount();

    /**
     * Returns whether there are any testable instructions or branches in the changed code.
     *
     * @return whether there are any testable instructions or branches in the changed code.
     */
    default boolean hasTestableChanges()
    {
        return (getTotalChangedLinesCount() + getTotalChangedBranchesCount()) > 0;
    }

    /**
     * Summarises the type of change.
     *
     * @return the summary of the change type.
     */
    String summariseChangeType();

    /**
     * Returns whether all the testable branches or instructions are covered.
     *
     * @return whether all the testable branches or instructions are covered.
     */
    default boolean isCoverageComplete()
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
    default String summariseBranchCoverage()
    {
        return getCoveredChangedBranchesCount() + "/" + getTotalChangedBranchesCount() + " changed branches covered";
    }

    /**
     * Summarises the change and the associated coverage.
     *
     * @return the summary of the change and the associated coverage.
     */
    String summariseChangeAndCoverage();

    /**
     * Summarises the line coverage.
     *
     * @return the summary of line coverage.
     */
    @Nonnull
    default String summariseLineCoverage()
    {
        return getCoveredChangedLinesCount() + "/" + getTotalChangedLinesCount() + " changed lines covered";
    }
}
