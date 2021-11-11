package com.jjlharrison.coverage.changes;

import java.io.File;

import javax.xml.bind.JAXB;

import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugin.logging.Log;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;

import com.jjlharrison.coverage.changes.report.ChangeCoverageReport;

/**
 * Goal which calculates coverage levels for changed Java code and enforces minimum requirements.
 */
@Mojo(name = "check", threadSafe = true, defaultPhase = LifecyclePhase.POST_SITE)
public class ChangeCoverageCheckMojo extends AbstractChangeCoverageMojo
{
    /** The required coverage percentage for changed branches. */
    @Parameter(defaultValue = "92", property = "coverage.change.branch.requirement")
    private double changedBranchCoverageRequirementPercentage;

    /** The required coverage percentage for changed lines. */
    @Parameter(defaultValue = "92", property = "coverage.change.line.requirement")
    private double changedLineCoverageRequirementPercentage;

    /** Whether to skip change coverage check. */
    @Parameter(defaultValue = "false", property = "change-coverage.check.skip")
    private boolean skip;

    /** The Maven project. */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoFailureException
    {
        final File xmlReportFile = getXmlReportFile();
        if (skip)
        {
            getLog().info("Skipping.");
        }
        else if ("pom".equals(project.getPackaging()))
        {
            getLog().info("Skipping POM module.");
        }
        else if (!xmlReportFile.exists())
        {
            getLog().info("Change coverage report not found. Ensure that the report goal has been executed.");
        }
        else
        {
            final ChangeCoverageReport report = JAXB.unmarshal(xmlReportFile, ChangeCoverageReport.class);
            final boolean changedBranchRequirementMet = report.getSummary().getBranch() >= changedBranchCoverageRequirementPercentage;
            final boolean changedLineRequirementMet = report.getSummary().getLine() >= changedLineCoverageRequirementPercentage;
            if (!changedBranchRequirementMet)
            {
                fail(changedBranchCoverageRequirementPercentage + "% requirement for test coverage of changed branches not met.",
                     getLog());
            }
            else if (!changedLineRequirementMet)
            {
                fail(changedLineCoverageRequirementPercentage + "% requirement for test coverage of changed lines not met.",
                     getLog());
            }
        }
    }

    /**
     * Fail build with the given message.
     *
     * @param message the message.
     * @param logger the logger.
     * @throws MojoFailureException the exception to fail the build.
     */
    private void fail(final String message, final Log logger) throws MojoFailureException
    {
        logger.error(message);
        throw new MojoFailureException(message);
    }
}
