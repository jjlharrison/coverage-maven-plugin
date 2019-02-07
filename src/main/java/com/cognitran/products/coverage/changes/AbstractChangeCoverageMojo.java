/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes;

import java.io.File;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.Parameter;

/**
 * Abstract class for change coverage MOJOs.
 */
public abstract class AbstractChangeCoverageMojo extends AbstractMojo
{
    /** The JaCoCo XML report file. */
    @Parameter(defaultValue = "${project.reporting.outputDirectory}/change-coverage/report.xml", required = true)
    private File xmlReportFile;

    /**
     * Returns the JaCoCo XML report file.
     *
     * @return the JaCoCo XML report file.
     */
    protected File getXmlReportFile()
    {
        return xmlReportFile;
    }
}
