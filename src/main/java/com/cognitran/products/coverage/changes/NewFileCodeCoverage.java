package com.cognitran.products.coverage.changes;

public class NewFileCodeCoverage extends FileNewCodeCoverage
{
    @Override
    public String describe()
    {
        return "New file " + getFilePath() + ": " +
               (getCoveredChangedLinesCount() + "/" + getTotalChangedLinesCount() + " new lines covered, " +
                getCoveredChangedBranchesCount() + "/" + getTotalChangedBranchesCount() + "new branches covered.");
    }
}
