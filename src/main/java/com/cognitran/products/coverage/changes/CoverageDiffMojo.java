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

import javax.annotation.Nonnull;

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
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.patch.HunkHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
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
@Mojo(name = "coverage-diff", threadSafe = true, defaultPhase = LifecyclePhase.POST_SITE)
public class CoverageDiffMojo extends AbstractMojo
{
    /** The JaCoCo XML report file. */
    @Parameter(defaultValue = "${project.reporting.outputDirectory}/jacoco/jacoco.xml", required = true)
    private File jacocoXmlReport;

    /** The Maven project. */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoFailureException
    {
        final ProjectChanges changes;
        try (Repository repository = buildRepository())
        {
            changes = getChanges(repository, getMergeBase(repository, "refs/heads/develop", Constants.HEAD));
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }

        //        changes.getNewFiles().forEach(f -> getLog().info("New file: " + f));
        //        changes.getChangedLinesByFile().forEach((k, v) -> getLog().info("Changed file: " + k));

        enforceChangeCoverageRequirements(changes);
    }

    /**
     * Builds the repository.
     *
     * @return the repository.
     * @throws IOException if an I/O error occurs.
     */
    @SuppressFBWarnings(value = "BC_UNCONFIRMED_CAST_OF_RETURN_VALUE", justification = "¯\\_(ツ)_/¯")
    private Repository buildRepository() throws IOException
    {
        return new FileRepositoryBuilder()
                   .readEnvironment()
                   .findGitDir(project.getBasedir())
                   .build();
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
        final int coveredChangedBranchesCount = coverage.stream().mapToInt(coveredExtractor).sum();
        final int totalChangedBranchesCount = coverage.stream().mapToInt(totalExtractor).sum();

        final double changeCodeBranchCoverage =
            ((double) coveredChangedBranchesCount / (double) totalChangedBranchesCount) * 100d;
        getLog().info("Changed " + type + " code coverage: " + new DecimalFormat("#.##").format(changeCodeBranchCoverage) + "%");
        return changeCodeBranchCoverage;
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
                    if (changeCodeBranchCoverage < 92d)
                    {
                        final String message = "92% requirement for test coverage on changed branches not met.";
                        getLog().error(message);
                        throw new MojoFailureException(message);
                    }
                    if (changeCodeLineCoverage < 92d)
                    {
                        final String message = "92% requirement for test coverage on changed lines not met.";
                        getLog().error(message);
                        throw new MojoFailureException(message);
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
     * Returns the project change information.
     *
     * @param repository the repository.
     * @param from the commit to compare with.
     * @return the project changes.
     * @throws IOException if an I/O error occurs.
     */
    @Nonnull
    private ProjectChanges getChanges(final Repository repository, final RevCommit from) throws IOException
    {
        /* TODO We may be able to speed up execution by caching the diff when run in the root module
         * and then using the cache in child modules. */
        final AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, from);
        final AbstractTreeIterator newTreeParser = new FileTreeIterator(repository);

        final DiffFormatter formatter = new DiffFormatter(NULL_OUTPUT_STREAM);
        formatter.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
        formatter.setRepository(repository);
        formatter.setContext(0);
        final URI repositoryRootDirectoryUri = URI.create(repository.getDirectory().getPath()).resolve("");
        final URI moduleRootDirectoryUri = URI.create(project.getBasedir().getPath());
        final URI repositoryRelativeModuleDirectoryUri = repositoryRootDirectoryUri.relativize(moduleRootDirectoryUri);
        final List<DiffEntry> diffEntries = formatter.scan(oldTreeParser, newTreeParser);
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
                        newFiles.add(filePath);
                    }
                }
            }
        }
        return changes;
    }

    /**
     * Finds a common ancestor between the source ref and the target ref.
     *
     * @param repository the repository.
     * @param source the source ref.
     * @param target the target ref.
     * @return the merge base.
     * @throws IOException if an I/O error occurs.
     */
    private RevCommit getMergeBase(final Repository repository, final String source, final String target) throws IOException
    {
        try (RevWalk walk = new RevWalk(repository))
        {
            final RevCommit revA = walk.parseCommit(repository.findRef(target).getObjectId());
            final RevCommit revB = walk.parseCommit(repository.findRef(source).getObjectId());
            walk.setRevFilter(RevFilter.MERGE_BASE);
            walk.markStart(revA);
            walk.markStart(revB);
            final RevCommit next = walk.next();
            walk.dispose();
            return next;
        }
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
