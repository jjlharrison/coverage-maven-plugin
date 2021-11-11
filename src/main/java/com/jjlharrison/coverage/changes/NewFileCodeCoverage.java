package com.jjlharrison.coverage.changes;

import javax.annotation.Nonnull;

/**
 * Coverage information for a new file.
 */
public class NewFileCodeCoverage extends AbstractChangeCoverage implements FileChangeCoverage
{
    /** The project/module source root relative file path. */
    private final String filePath;

    /**
     * Constructor.
     *
     * @param filePath the path of the new file.
     */
    public NewFileCodeCoverage(final String filePath)
    {
        this.filePath = filePath;
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

    @Nonnull
    @Override
    public String summariseChangeAndCoverage()
    {
        final String branches = getTotalChangedBranchesCount() > 0 ? (", " + summariseBranchCoverage()) : "";
        return summariseChangeType() + " " + getFilePath() + ": " + summariseLineCoverage() + branches;
    }

    @Override
    public String summariseChangeType()
    {
        return "New file";
    }

    @Override
    public String describe(final boolean includeCoveredDetail)
    {
        return summariseChangeAndCoverage();
    }
}
