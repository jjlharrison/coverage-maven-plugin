/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes.report;

/**
 * Summary of the report.
 */
public class ChangeCoverageReportSummary
{
    /** Percentage of changed lines that are covered. */
    private double line;

    /** Percentage of changed branches that are covered. */
    private double branch;

    /**
     * Returns the percentage of changed lines that are covered.
     *
     * @return the percentage of changed lines that are covered.
     */
    public double getLine()
    {
        return line;
    }

    /**
     * Sets the percentage of changed lines that are covered.
     *
     * @param line the percentage of changed lines that are covered.
     */
    public void setLine(final double line)
    {
        this.line = line;
    }

    /**
     * Returns the percentage of changed branches that are covered.
     *
     * @return the percentage of changed branches that are covered.
     */
    public double getBranch()
    {
        return branch;
    }

    /**
     * Sets the percentage of changed branches that are covered.
     *
     * @param branch the percentage of changed branches that are covered.
     */
    public void setBranch(final double branch)
    {
        this.branch = branch;
    }
}
