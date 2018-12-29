/*
 *  Copyright (c) 2017 Cognitran Limited. All Rights Reserved.
 */

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.URI;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.IntStream;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugin.MojoFailureException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
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
import org.jetbrains.annotations.NotNull;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import com.cognitran.products.coverage.NewCodeCoverage;

/**
 * Goal which update minimum test coverage requirements based on current coverage.
 */
@Mojo(name = "coverage-diff",
      threadSafe = true,
      defaultPhase = LifecyclePhase.POST_SITE)
public class CoverageDiffMojo extends AbstractMojo
{
    /** The Maven project. */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /** The JaCoCo XML report file. */
    @Parameter(defaultValue = "${project.reporting.outputDirectory}/jacoco/jacoco.xml", required = true)
    private File jacocoHtmlReport;

    @Override
    public void execute() throws MojoExecutionException, MojoFailureException
    {
        try (Repository repository = getRepository(); Git git = new Git(repository); RevWalk walk = new RevWalk(repository))
        {
            // if (repository.exactRef("refs/heads/testbranch") == null)
            // {
            // // first we need to ensure that the remote branch is visible locally
            // final Ref ref = git.branchCreate().setName("testbranch").setStartPoint("origin/testbranch").call();
            // // System.out.println("Created local testbranch with ref: " + ref);
            // }

            // the diff works on TreeIterators, we prepare two for the two branches
            final AbstractTreeIterator oldTreeParser = prepareTreeParser(repository, walk, getMergeBase(repository, "refs/heads/master",
                                                                                                        Constants.HEAD));
            final AbstractTreeIterator newTreeParser = prepareTreeParser(repository, walk, Constants.HEAD);

            // then the procelain diff-command returns a list of diff entries
            // final List<DiffEntry> diff = git.diff().setOldTree(oldTreeParser).setNewTree(newTreeParser).setContextLines(0).call();
            final ByteArrayOutputStream out = new ByteArrayOutputStream();
            final DiffFormatter formatter = new DiffFormatter(out);
            formatter.setRepository(repository);
            formatter.setContext(0);
            final URI repositoryRootDirectoryUri = URI.create(repository.getDirectory().getPath()).resolve("");
            final URI moduleRootDirectoryUri = URI.create(project.getBasedir().getPath());
            final URI repositoryRelativeModuleDirectoryUri = repositoryRootDirectoryUri.relativize(moduleRootDirectoryUri);
            final List<DiffEntry> diffEntries = formatter.scan(oldTreeParser, newTreeParser);
            final HashMap<String, Set<Integer>> changedLinesByFile = new HashMap<>(capacity(diffEntries.size()));
            final HashSet<String> newFiles = new HashSet<>(capacity(diffEntries.size()));
            for (final DiffEntry entry : diffEntries)
            {
                final String filePath = entry.getNewPath();
                if (filePath.startsWith(repositoryRelativeModuleDirectoryUri.toString()))
                {
                    for (final String compileSourceRoot : project.getCompileSourceRoots())
                    {
                        final URI sourceRootRelativeFileUri = repositoryRootDirectoryUri.relativize(URI.create(compileSourceRoot))
                                                                  .relativize(URI.create(filePath));
                        // TODO Check if file exists.
                        if (entry.getChangeType() == DiffEntry.ChangeType.MODIFY)
                        {
                            final Set<Integer> changedLines = changedLinesByFile.computeIfAbsent(sourceRootRelativeFileUri.toString(),
                                                                                                 k -> new TreeSet<>());
                            // getLog().info("Entry: " + entry);
                            final FileHeader header = formatter.toFileHeader(entry);
                            // getLog().info("File: " + header.getNewPath());
                            for (final HunkHeader hunk : header.getHunks())
                            {
                                for (final Edit edit : hunk.toEditList())
                                {
                                    final Edit.Type type = edit.getType();
                                    if (type == Edit.Type.INSERT || type == Edit.Type.REPLACE)
                                    {
                                        IntStream.rangeClosed(edit.getBeginB() + 1, edit.getEndB()).forEachOrdered(changedLines::add);
                                        // for (int i = edit.getBeginB(); i < edit.getEndB(); i++)
                                        // {
                                        // getLog().info("Line: " + (i + 1));
                                        // }
                                    }
                                }
                            }
                        }
                        else if (entry.getChangeType() == DiffEntry.ChangeType.ADD)
                        {
                            // getLog().info("New file: " + filePath);
                            newFiles.add(filePath);
                        }
                    }
                }
                // getLog().info("Changed Lines: " + changedLinesByFile.toString());
                // getLog().info("New Files: " + newFiles.toString());
                // formatter.format(entry);
                // getLog().info(new String(out.toByteArray()));

                out.reset();
            }

            final File jacocoXmlReport = new File(project.getBuild().getDirectory(), "site/jacoco/jacoco.xml");
            if (jacocoXmlReport.isFile())
            {
                try (final FileInputStream inputStream = new FileInputStream(jacocoXmlReport))
                {
                    final JacocoReportParser parser = new JacocoReportParser(newFiles, changedLinesByFile);
                    Sax.parse(new InputSource(inputStream), parser, false);
                    parser.getCoverage().stream()
                        .filter(NewCodeCoverage::hasTestableChanges)
                        .forEach(coverage -> getLog().info(coverage.toString()));

                    final int coveredChangedBranchesCount =
                        parser.getCoverage().stream().mapToInt(NewCodeCoverage::getCoveredChangedBranchesCount).sum();
                    final int totalChangedBranchesCount =
                        parser.getCoverage().stream().mapToInt(NewCodeCoverage::getTotalChangedBranchesCount).sum();

                    final double changeCodeBranchCoverage =
                        ((double) coveredChangedBranchesCount / (double) totalChangedBranchesCount) * 100d;
                    getLog().info("Changed branches code coverage: " + new DecimalFormat("#.##").format(changeCodeBranchCoverage) + "%");

                    final int coveredChangedLinesCount =
                        parser.getCoverage().stream().mapToInt(NewCodeCoverage::getCoveredChangedLinesCount).sum();
                    final int totalChangedLinesCount =
                        parser.getCoverage().stream().mapToInt(NewCodeCoverage::getTotalChangedLinesCount).sum();

                    final double changeCodeLineCoverage =
                        ((double) coveredChangedLinesCount / (double) totalChangedLinesCount) * 100d;
                    getLog().info("Changed line code coverage: " + new DecimalFormat("#.##").format(changeCodeLineCoverage) + "%");

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

    private Repository getRepository() throws IOException
    {
        final FileRepositoryBuilder builder = new FileRepositoryBuilder();
        return builder.readEnvironment()
                   .findGitDir(project.getBasedir())
                   .build();
    }

    private static AbstractTreeIterator prepareTreeParser(final Repository repository, final RevWalk walk, final String ref)
        throws IOException
    {
        // from the commit we can build the tree which allows us to construct the TreeParser
        final Ref head = repository.findRef(ref);
        final RevCommit commit = walk.parseCommit(head.getObjectId());
        return prepareTreeParser(repository, walk, commit);
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

    public static int capacity(final int expectedSize)
    {
        if (expectedSize < 1)
        {
            return 1;
        }
        if (expectedSize < 3)
        {
            return expectedSize + 1;
        }
        else
        {
            return expectedSize < 1073741824 ? (int) ((float) expectedSize / 0.75F + 1.0F) : 2147483647;
        }
    }
}
