/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes.report;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * Change coverage report.
 */
@XmlRootElement(name = "change-coverage")
public class ChangeCoverageReport
{
    /** Summary of report. */
    private ChangeCoverageReportSummary summary;

    /**
     * Returns the summary of the report.
     *
     * @return the summary of the report.
     */
    public ChangeCoverageReportSummary getSummary()
    {
        return summary;
    }

    /**
     * Sets the summary of the report.
     *
     * @param summary the summary of the report.
     */
    public void setSummary(final ChangeCoverageReportSummary summary)
    {
        this.summary = summary;
    }
}
