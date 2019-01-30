/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes;

import static java.nio.charset.StandardCharsets.UTF_8;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.Reader;
import java.text.DecimalFormat;
import java.util.List;
import java.util.function.ToIntFunction;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.xml.bind.JAXB;

import org.apache.commons.io.IOUtils;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.cognitran.products.coverage.changes.diff.ProjectChanges;
import com.cognitran.products.coverage.changes.jacoco.JacocoReportParser;
import com.cognitran.products.coverage.changes.report.ChangeCoverageReport;
import com.cognitran.products.coverage.changes.report.ChangeCoverageReportSummary;
import com.cognitran.products.utilities.io.Files;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Goal which calculates coverage levels for changed Java code and enforces minimum requirements.
 */
@Mojo(name = "report", threadSafe = true, defaultPhase = LifecyclePhase.POST_SITE)
public class ChangeCoverageReportMojo extends AbstractMojo
{
    /** Log to guard write to the aggregate log file. */
    private static final Object AGGREGATE_LOG_WRITE_LOCK = new Object();

    /** The branch to compare with to detect changes. */
    @Parameter(defaultValue = "develop", property = "coverage.change.branch")
    private String compareBranch;

    /** The JaCoCo XML report file. */
    @Parameter(defaultValue = "${project.reporting.outputDirectory}/jacoco/jacoco.xml", required = true)
    private File jacocoXmlReport;

    /** The JaCoCo XML report file. */
    @Parameter(defaultValue = "${project.reporting.outputDirectory}/change-coverage/report.xml", required = true)
    private File xmlReportFile;

    /** The Maven project. */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /** Whether to skip change coverage check. */
    @Parameter(defaultValue = "false", property = "change-coverage.skip")
    private boolean skip;

    @Override
    public void execute()
    {
        if (skip)
        {
            getLog().info("Skipping.");
        }
        else if ("pom".equals(project.getPackaging()))
        {
            getLog().info("Skipping POM module.");
        }
        else if (!jacocoXmlReport.isFile())
        {
            getLog().info("JaCoCo report not found (" + jacocoXmlReport.getPath() + "). "
                          + "Ensure that the jacoco:report goal has been executed.");
        }
        else
        {
            final File logFile = new File(project.getBuild().getDirectory(), "change-coverage.log");
            try (Logger logger = new Logger(getLog(), logFile))
            {
                final ProjectChanges changes;
                final File repositoryLogFile;
                try (Repository repository = buildRepository())
                {
                    if (repository == null)
                    {
                        logger.info("Not a Git repository, skipping.");
                        return;
                    }
                    else
                    {
                        final File target = new File(repository.getDirectory().getParentFile(), "target");
                        repositoryLogFile = new File(target, "change-coverage.log");
                        changes = getChanges(repository, logger);
                    }
                }
                catch (final IOException e)
                {
                    throw new RuntimeException(e);
                }

                if (changes.hasChanges())
                {
                    logger.info(changes.toString());

                    reportChangeCoverageMeasures(changes, logger);

                    appendLogFileToRepositoryLogFile(logger, logFile, repositoryLogFile);
                }
                else
                {
                    logger.info("No new code found.");
                }
            }
        }
    }

    /**
     * Appends the module log file to the repository log file.
     *
     * @param logger the logger.
     * @param logFile the module log file.
     * @param repositoryLogFile the repository log file.
     */
    protected void appendLogFileToRepositoryLogFile(final Logger logger, final File logFile, final File repositoryLogFile)
    {
        if (!logFile.getAbsolutePath().equals(repositoryLogFile.getAbsolutePath()))
        {
            synchronized (AGGREGATE_LOG_WRITE_LOCK)
            {
                logger.flush();
                try
                {
                    Files.forceMkdir(repositoryLogFile.getParentFile());
                    try (FileOutputStream fileOutputStream = new FileOutputStream(repositoryLogFile, true);
                         OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream, UTF_8);
                         PrintWriter printWriter = new PrintWriter(outputStreamWriter);
                         InputStream logFileInputStream = new FileInputStream(logFile);
                         Reader logFileReader = new InputStreamReader(logFileInputStream, UTF_8))
                    {
                        printWriter.println("------------------------------------------------------------------------");
                        printWriter.println("Project: " + project.getName());
                        printWriter.println("------------------------------------------------------------------------");
                        printWriter.println();
                        IOUtils.copy(logFileReader, printWriter);
                        printWriter.println();
                    }
                }
                catch (final IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
    }

    /**
     * Builds the repository.
     *
     * @return the repository.
     * @throws IOException if an I/O error occurs.
     */
    @Nullable
    @CheckForNull
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "¯\\_(ツ)_/¯")
    private Repository buildRepository() throws IOException
    {
        final FileRepositoryBuilder builder = new FileRepositoryBuilder()
                                                  .readEnvironment()
                                                  .findGitDir(project.getBasedir());
        return builder.getGitDir() == null ? null : builder.build();
    }

    /**
     * Calculate the coverage percentage.
     *
     * @param coverage the coverage information.
     * @param type the coverage type.
     * @param coveredExtractor the function to extract covered counts from the coverage information.
     * @param totalExtractor the function to extract total change counts from the coverage information.
     * @param logger the logger.
     * @return the coverage percentage.
     */
    private double calculateChangeCoveragePercentage(final List<ChangeCoverage> coverage,
                                                     final String type,
                                                     final ToIntFunction<ChangeCoverage> coveredExtractor,
                                                     final ToIntFunction<ChangeCoverage> totalExtractor,
                                                     final Logger logger)
    {
        final int totalChangedCount = coverage.stream().mapToInt(totalExtractor).sum();
        if (totalChangedCount > 0)
        {
            final int coveredChangedCount = coverage.stream().mapToInt(coveredExtractor).sum();

            final double changeCodeCoverage =
                ((double) coveredChangedCount / (double) totalChangedCount) * 100d;
            logger.info("Changed " + type + " code coverage: " + new DecimalFormat("#.##").format(changeCodeCoverage) + "%");
            return changeCodeCoverage;
        }
        return 100d;
    }

    /**
     * Reports the change coverage measures.
     *
     * @param changes the project changes.
     * @param logger the logger.
     */
    private void reportChangeCoverageMeasures(final ProjectChanges changes, final Logger logger)
    {
        try
        {
            if (jacocoXmlReport.isFile())
            {
                try (FileInputStream inputStream = new FileInputStream(jacocoXmlReport))
                {
                    final JacocoReportParser parser = new JacocoReportParser(changes.getNewFiles(), changes.getChangedLinesByFile());
                    Utilities.parse(new InputSource(inputStream), parser, false);
                    final List<ChangeCoverage> coverage = parser.getCoverage();
                    coverage.stream()
                        .filter(ChangeCoverage::hasTestableChanges)
                        .forEach(c -> c.describe(logger));

                    final double changeCodeBranchCoverage =
                        calculateChangeCoveragePercentage(coverage, "branch",
                                                          ChangeCoverage::getCoveredChangedBranchesCount,
                                                          ChangeCoverage::getTotalChangedBranchesCount, logger);

                    final double changeCodeLineCoverage =
                        calculateChangeCoveragePercentage(coverage, "line",
                                                          ChangeCoverage::getCoveredChangedLinesCount,
                                                          ChangeCoverage::getTotalChangedLinesCount, logger);

                    final ChangeCoverageReport report = new ChangeCoverageReport();
                    final ChangeCoverageReportSummary summary = new ChangeCoverageReportSummary();
                    summary.setBranch(changeCodeBranchCoverage);
                    summary.setLine(changeCodeLineCoverage);
                    report.setSummary(summary);
                    Files.forceMkdir(xmlReportFile.getParentFile());
                    JAXB.marshal(report, xmlReportFile);
                }
            }
        }
        catch (final IOException | SAXException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the project change information.
     *
     * @param repository the repository.
     * @param logger the logger.
     * @return the project changes.
     * @throws IOException if an I/O error occurs.
     */
    @Nonnull
    private ProjectChanges getChanges(final Repository repository, final Logger logger) throws IOException
    {
        return new GitDiffChangeResolver(repository,
                                         project.getBasedir().getPath(),
                                         compareBranch,
                                         project.getCompileSourceRoots(),
                                         logger).resolve();
    }
}
