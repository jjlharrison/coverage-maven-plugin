/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes;

import static com.cognitran.products.coverage.changes.Utilities.capacity;
import static org.apache.commons.io.output.NullOutputStream.NULL_OUTPUT_STREAM;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.function.ToIntFunction;
import java.util.stream.IntStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.storage.file.FileRepositoryBuilder;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.cognitran.products.coverage.changes.diff.ProjectChanges;
import com.cognitran.products.coverage.changes.jacoco.JacocoReportParser;
import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;

/**
 * Goal which calculates coverage levels for changed Java code and enforces minimum requirements.
 */
@Mojo(name = "change-coverage", threadSafe = true, defaultPhase = LifecyclePhase.POST_SITE)
public class ChangeCoverageMojo extends AbstractMojo
{

    /** The required coverage percentage for changed branches. */
    @Parameter(defaultValue = "92", property = "coverage.change.branch.requirement")
    private double changedBranchCoverageRequirementPercentage;

    /** The required coverage percentage for changed lines. */
    @Parameter(defaultValue = "92", property = "coverage.change.line.requirement")
    private double changedLineCoverageRequirementPercentage;

    /** Whether to skip change coverage check. */
    @Parameter(defaultValue = "false", property = "coverage.change.skip")
    private boolean skip;

    /** The branch to compare with to detect changes. */
    @Parameter(defaultValue = "develop", property = "coverage.change.branch")
    private String compareBranch;

    /** The JaCoCo XML report file. */
    @Parameter(defaultValue = "${project.reporting.outputDirectory}/jacoco/jacoco.xml", required = true)
    private File jacocoXmlReport;

    /** The Maven project. */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoFailureException
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
            final ProjectChanges changes;
            try (Repository repository = buildRepository())
            {
                if (repository == null)
                {
                    getLog().info("Not a Git repository, skipping.");
                    return;
                }
                else
                {
                    changes = getChanges(repository);
                }
            }
            catch (final IOException e)
            {
                throw new RuntimeException(e);
            }

            if (changes.hasChanges())
            {
                getLog().info(changes.toString());
                enforceChangeCoverageRequirements(changes);
            }
            else
            {
                getLog().info("No new code found.");
            }
        }
    }

    /**
     * Appends a trailing slash to the URI if it doesn't already have one.
     *
     * @param uri the URI.
     * @return the URI with the trailing slash.
     */
    private static URI addTrailingSlash(final URI uri)
    {
        return uri.toString().endsWith("/") ? uri : URI.create(uri.toString() + "/");
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
     * @return the coverage percentage.
     */
    private double calculateChangeCoveragePercentage(final List<ChangeCoverage> coverage,
                                                     final String type,
                                                     final ToIntFunction<ChangeCoverage> coveredExtractor,
                                                     final ToIntFunction<ChangeCoverage> totalExtractor)
    {
        final int totalChangedCount = coverage.stream().mapToInt(totalExtractor).sum();
        if (totalChangedCount > 0)
        {
            final int coveredChangedCount = coverage.stream().mapToInt(coveredExtractor).sum();

            final double changeCodeCoverage =
                ((double) coveredChangedCount / (double) totalChangedCount) * 100d;
            getLog().info("Changed " + type + " code coverage: " + new DecimalFormat("#.##").format(changeCodeCoverage) + "%");
            return changeCodeCoverage;
        }
        return 100d;
    }

    /**
     * Enforces change coverage requirements.
     *
     * @param changes the project changes.
     * @throws MojoFailureException if the requirements are not met.
     */
    private void enforceChangeCoverageRequirements(final ProjectChanges changes) throws MojoFailureException
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
                        .forEach(c -> c.describe(getLog()));

                    final double changeCodeBranchCoverage =
                        calculateChangeCoveragePercentage(coverage, "branch",
                                                          ChangeCoverage::getCoveredChangedBranchesCount,
                                                          ChangeCoverage::getTotalChangedBranchesCount);

                    final double changeCodeLineCoverage =
                        calculateChangeCoveragePercentage(coverage, "line",
                                                          ChangeCoverage::getCoveredChangedLinesCount,
                                                          ChangeCoverage::getTotalChangedLinesCount);
                    final boolean changedBranchRequirementMet = changeCodeBranchCoverage >= changedBranchCoverageRequirementPercentage;
                    final boolean changedLineRequirementMet = changeCodeLineCoverage >= changedLineCoverageRequirementPercentage;
                    // TODO Handle case when both requirements are met.
                    if (!changedBranchRequirementMet)
                    {
                        fail(changedBranchCoverageRequirementPercentage + "% requirement for test coverage of changed branches not met.");
                    }
                    else if (!changedLineRequirementMet)
                    {
                        fail(changedLineCoverageRequirementPercentage + "% requirement for test coverage of changed lines not met.");
                    }
                }
            }
        }
        catch (final IOException | SAXException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Fail build with the given message.
     *
     * @param message the message.
     * @throws MojoFailureException the exception to fail the build.
     */
    private void fail(final String message) throws MojoFailureException
    {
        getLog().error(message);
        throw new MojoFailureException(message);
    }

    /**
     * Returns the project change information.
     *
     * @param repository the repository.
     * @return the project changes.
     * @throws IOException if an I/O error occurs.
     */
    @Nonnull
    private ProjectChanges getChanges(final Repository repository) throws IOException
    {
        final URI repositoryRootDirectoryUri = addTrailingSlash(URI.create(repository.getDirectory().getPath()).resolve(""));
        final URI moduleRootDirectoryUri = addTrailingSlash(URI.create(project.getBasedir().getPath()));
        final URI repositoryRelativeModuleDirectoryUri = repositoryRootDirectoryUri.relativize(moduleRootDirectoryUri);

        /* TODO We may be able to speed up execution by caching the diff when run in the root module
         * and then using the cache in child modules. */
        //        final AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, from);
        final RevCommit target = getCommitForRef(repository, "refs/heads/" + compareBranch);
        getLog().info("Comparing current directory with " + target.getName() + ".");
        final AbstractTreeIterator oldTreeParser = new FileTreeIterator(repository);
        final AbstractTreeIterator newTreeParser = prepareTreeParser(repository, target);
        final DiffFormatter formatter = new DiffFormatter(NULL_OUTPUT_STREAM);
        formatter.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
        formatter.setRepository(repository);
        formatter.setContext(0);
        final List<DiffEntry> diffEntries = formatter.scan(newTreeParser, oldTreeParser);
        final Map<String, Set<Integer>> changedLinesByFile = new HashMap<>(capacity(diffEntries.size()));
        final Set<String> newFiles = new HashSet<>(capacity(diffEntries.size()));
        final ProjectChanges changes = new ProjectChanges(changedLinesByFile, newFiles);
        for (final DiffEntry entry : diffEntries)
        {
            final String filePath = entry.getNewPath();
            if (filePath.startsWith(repositoryRelativeModuleDirectoryUri.toString()))
            {
                for (final String compileSourceRoot : project.getCompileSourceRoots())
                {
                    final URI sourceRootRelativeFileUri = repositoryRootDirectoryUri.relativize(URI.create(compileSourceRoot))
                                                              .relativize(URI.create(filePath));
                    if (entry.getChangeType() == DiffEntry.ChangeType.MODIFY)
                    {
                        final FileHeader header = formatter.toFileHeader(entry);
                        for (final HunkHeader hunk : header.getHunks())
                        {
                            for (final Edit edit : hunk.toEditList())
                            {
                                final Edit.Type type = edit.getType();
                                if (type == Edit.Type.INSERT || type == Edit.Type.REPLACE)
                                {
                                    final Set<Integer> changedLines =
                                        changedLinesByFile.computeIfAbsent(sourceRootRelativeFileUri.toString(),
                                                                           k -> new TreeSet<>());
                                    IntStream.rangeClosed(edit.getBeginB() + 1, edit.getEndB()).forEachOrdered(changedLines::add);
                                }
                            }
                        }
                    }
                    else if (entry.getChangeType() == DiffEntry.ChangeType.ADD)
                    {
                        newFiles.add(sourceRootRelativeFileUri.toString());
                    }
                }
            }
        }
        return changes;
    }

    /**
     * Returns the commit for the given ref.
     *
     * @param repository the repository.
     * @param ref the ref.
     * @return the commit.
     * @throws IOException if an I/O error occurs.
     */
    private RevCommit getCommitForRef(final Repository repository, final String ref) throws IOException
    {
        final RevCommit commmit;
        try (RevWalk walk = new RevWalk(repository))
        {
            commmit = walk.parseCommit(repository.findRef(ref).getObjectId());
            walk.dispose();
        }
        return commmit;
    }

    /**
     * Prepares a tree iterator/parser for the given commit.
     *
     * @param repository the repository.
     * @param commit the commit.
     * @return the tree iterator.
     * @throws IOException if an I/O error occurs.
     */
    @Nonnull
    private static AbstractTreeIterator prepareTreeParser(final Repository repository, final RevCommit commit)
        throws IOException
    {
        try (RevWalk walk = new RevWalk(repository))
        {
            final RevTree tree = walk.parseTree(commit.getTree().getId());

            final CanonicalTreeParser treeParser = new CanonicalTreeParser();
            try (ObjectReader reader = repository.newObjectReader())
            {
                treeParser.reset(reader, tree.getId());
            }

            walk.dispose();

            return treeParser;
        }
    }
}
