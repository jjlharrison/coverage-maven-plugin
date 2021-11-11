package com.jjlharrison.coverage.changes;

import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;

import javax.annotation.Nonnull;
import javax.annotation.concurrent.Immutable;

/**
 * Change coverage information for a modified file.
 */
@Immutable
public class ModifiedFileChangeCoverage implements FileChangeCoverage
{
    /** The changed line numbers. */
    private final Set<Integer> changedLineNumbers;

    /** The coverage information for changed lines. */
    private final Set<LineCodeCoverage> coverage = new TreeSet<>();

    /** The project/module source root relative file path. */
    private final String filePath;

    /**
     * Constructor.
     *
     * @param filePath the path of the modified file.
     * @param changedLineNumbers the changed line numbers.
     */
    public ModifiedFileChangeCoverage(final String filePath, final Set<Integer> changedLineNumbers)
    {
        this.filePath = filePath;
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
     * Returns the project/module source root relative file path.
     *
     * @return the project/module source root relative file path.
     */
    @Override
    public String getFilePath()
    {
        return filePath;
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
    public String describe(final boolean includeCoveredDetail)
    {
        return summariseChangeAndCoverage() + ":\n" +
               coverage.stream()
                   .filter(c -> includeCoveredDetail || !c.isCoverageComplete())
                   .map(c -> c.describe(includeCoveredDetail))
                   .map(s -> "    " + s)
                   .collect(Collectors.joining("\n"));
    }

    @Nonnull
    @Override
    public String summariseChangeAndCoverage()
    {
        final String branches = getTotalChangedBranchesCount() > 0 ? (", " + summariseBranchCoverage()) : "";
        return summariseChangeType() + " " + getFilePath() + ": " + summariseLineCoverage() + branches;
    }

    @Nonnull
    @Override
    public String summariseChangeType()
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
