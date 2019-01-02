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
import org.jetbrains.annotations.NotNull;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.cognitran.products.coverage.changes.diff.Changes;
import com.cognitran.products.coverage.changes.jacoco.JacocoReportParser;

/**
 * Goal which update minimum test coverage requirements based on current coverage.
 */
@Mojo(name = "coverage-diff",
      threadSafe = true,
      defaultPhase = LifecyclePhase.POST_SITE)
public class CoverageDiffMojo extends AbstractMojo
{
    /** The JaCoCo XML report file. */
    @Parameter(defaultValue = "${project.reporting.outputDirectory}/jacoco/jacoco.xml", required = true)
    private File jacocoHtmlReport;

    /** The Maven project. */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    @Override
    public void execute() throws MojoFailureException
    {
        final Changes changes;
        try (Repository repository = getRepository(); RevWalk walk = new RevWalk(repository))
        {
            // if (repository.exactRef("refs/heads/testbranch") == null)
            // {
            // // first we need to ensure that the remote branch is visible locally
            // final Ref ref = git.branchCreate().setName("testbranch").setStartPoint("origin/testbranch").call();
            // // System.out.println("Created local testbranch with ref: " + ref);
            // }
            changes = getChanges(repository, walk, getMergeBase(repository, "refs/heads/develop", Constants.HEAD));
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }

        changes.getNewFiles().forEach(f -> getLog().info("New file: " + f));
        changes.getChangedLinesByFile().forEach((k, v) -> getLog().info("Changed file: " + k));

        theRest(changes);
    }

    private double calculateChangeCoveragePercentage(final List<NewCodeCoverage> coverage,
                                                     final String type,
                                                     final ToIntFunction<NewCodeCoverage> coveredExtractor,
                                                     final ToIntFunction<NewCodeCoverage> totalExtractor)
    {
        final int coveredChangedBranchesCount = coverage.stream().mapToInt(coveredExtractor).sum();
        final int totalChangedBranchesCount = coverage.stream().mapToInt(totalExtractor).sum();

        final double changeCodeBranchCoverage =
            ((double) coveredChangedBranchesCount / (double) totalChangedBranchesCount) * 100d;
        getLog().info("Changed " + type + " code coverage: " + new DecimalFormat("#.##").format(changeCodeBranchCoverage) + "%");
        return changeCodeBranchCoverage;
    }

    @NotNull
    private Changes getChanges(final Repository repository, final RevWalk walk, final RevCommit from) throws IOException
    {
        /* TODO We may be able to speed up execution by caching the diff when run in the root module
         * and then using the cache in child modules. */
        final AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, walk, from);
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
        final Changes changes = new Changes(changedLinesByFile, newFiles);
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

    private RevCommit getMergeBase(final Repository repository, final String source, final String target) throws IOException
    {
        try (final RevWalk walk = new RevWalk(repository))
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

    private Repository getRepository() throws IOException
    {
        final FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder.readEnvironment()
                   .findGitDir(project.getBasedir())
                   .build();
    }

    @NotNull
    private static AbstractTreeIterator prepareTreeParser(final Repository repository, final RevWalk walk, final RevCommit commit)
        throws IOException
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

    private void theRest(final Changes changes) throws MojoFailureException
    {
        try
        {
            final File jacocoXmlReport = new File(project.getBuild().getDirectory(), "site/jacoco/jacoco.xml");
            if (jacocoXmlReport.isFile())
            {
                try (final FileInputStream inputStream = new FileInputStream(jacocoXmlReport))
                {
                    final JacocoReportParser parser = new JacocoReportParser(changes.getNewFiles(), changes.getChangedLinesByFile());
                    Utilities.parse(new InputSource(inputStream), parser, false);
                    final List<NewCodeCoverage> coverage = parser.getCoverage();
                    coverage.stream()
                        .filter(NewCodeCoverage::hasTestableChanges)
                        .forEach(c -> getLog().info(c.toString()));

                    final double changeCodeBranchCoverage =
                        calculateChangeCoveragePercentage(coverage, "branch",
                                                          NewCodeCoverage::getCoveredChangedBranchesCount,
                                                          NewCodeCoverage::getTotalChangedBranchesCount);

                    final double changeCodeLineCoverage =
                        calculateChangeCoveragePercentage(coverage, "line",
                                                          NewCodeCoverage::getCoveredChangedLinesCount,
                                                          NewCodeCoverage::getTotalChangedLinesCount);
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
}
