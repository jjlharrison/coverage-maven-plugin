/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes;

import static com.cognitran.products.coverage.changes.Utilities.capacity;
import static org.apache.commons.io.output.NullOutputStream.NULL_OUTPUT_STREAM;
import static org.eclipse.jgit.lib.Constants.R_REMOTES;
import static org.eclipse.jgit.lib.Repository.shortenRefName;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import javax.annotation.CheckForNull;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.diff.DiffEntry;
import org.eclipse.jgit.diff.DiffFormatter;
import org.eclipse.jgit.diff.Edit;
import org.eclipse.jgit.diff.RawTextComparator;
import org.eclipse.jgit.lib.BranchTrackingStatus;
import org.eclipse.jgit.lib.Constants;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.patch.FileHeader;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.revwalk.filter.RevFilter;
import org.eclipse.jgit.treewalk.AbstractTreeIterator;
import org.eclipse.jgit.treewalk.CanonicalTreeParser;
import org.eclipse.jgit.treewalk.FileTreeIterator;
import org.eclipse.jgit.treewalk.filter.OrTreeFilter;
import org.eclipse.jgit.treewalk.filter.PathFilter;
import org.eclipse.jgit.treewalk.filter.TreeFilter;

import com.cognitran.products.coverage.changes.diff.ProjectChanges;

/**
 * Change resolver that resolves changes from a Git diff.
 */
public class GitDiffChangeResolver
{
    /** The branch to compare with. */
    private String compareBranch;

    /** The compile source roots to check. */
    private Collection<String> compileSourceRoots;

    /** The logger. */
    private Logger log;

    /** The project base directory path. */
    private String projectBaseDirectoryPath;

    /** The Git repository. */
    private Repository repository;

    /**
     * Constructor.
     *
     * @param repository the Git Repository.
     * @param projectBaseDirectoryPath the project base directory path.
     * @param compareBranch the branch to compare with.
     * @param compileSourceRoots the compile source roots to check.
     * @param log the logger.
     */
    public GitDiffChangeResolver(final Repository repository, final String projectBaseDirectoryPath, final String compareBranch,
                                 final Collection<String> compileSourceRoots, final Logger log)
    {
        this.projectBaseDirectoryPath = projectBaseDirectoryPath;
        this.compareBranch = compareBranch;
        this.compileSourceRoots = compileSourceRoots;
        this.repository = repository;
        this.log = log;
    }

    /**
     * Resolves the changes.
     *
     * @return the changes.
     * @throws IOException if an I/O error occurs.
     */
    public ProjectChanges resolve() throws IOException
    {
        if (!compileSourceRoots.isEmpty())
        {
            final URI repositoryRootDirectoryUri = repository.getDirectory().toURI().resolve("..");
            final URI moduleRootDirectoryUri = new File(projectBaseDirectoryPath).toURI();
            final URI repositoryRelativeModuleUri = repositoryRootDirectoryUri.relativize(moduleRootDirectoryUri);

            final String longBranchName = resolveCompareBranch();

            if (longBranchName == null)
            {
                throw new RuntimeException("Could not resolve branch named " + compareBranch + ".");
            }

            final AbstractTreeIterator oldTreeParser = new FileTreeIterator(repository);
            final RevCommit mergeBase = getMergeBase(repository, "HEAD", longBranchName);
            final AbstractTreeIterator newTreeParser = prepareTreeParser(repository,
                                                                         mergeBase);
            final DiffFormatter formatter = new DiffFormatter(NULL_OUTPUT_STREAM);
            formatter.setDiffComparator(RawTextComparator.WS_IGNORE_ALL);
            formatter.setRepository(repository);
            formatter.setContext(0);

            // Filter files not in source roots.
            final List<TreeFilter> pathFilters = compileSourceRoots.stream().map(File::new).map(File::toURI)
                                                     .map(s -> repositoryRootDirectoryUri.relativize(s).getPath())
                                                     .map(PathFilter::create)
                                                     .collect(Collectors.toList());
            formatter.setPathFilter(pathFilters.size() > 1 ? OrTreeFilter.create(pathFilters) : pathFilters.get(0));

            final List<DiffEntry> diffEntries = formatter.scan(newTreeParser, oldTreeParser);
            final Map<String, Set<Integer>> changedLinesByFile = new HashMap<>(capacity(diffEntries.size()));
            final Set<String> newFiles = new HashSet<>(capacity(diffEntries.size()));
            final ProjectChanges changes = new ProjectChanges(changedLinesByFile, newFiles);
            for (final DiffEntry entry : diffEntries)
            {
                final String filePath = entry.getNewPath();
                if (filePath.startsWith(repositoryRelativeModuleUri.toString()))
                {
                    final List<URI> entrySourceRoots = compileSourceRoots.stream()
                                                           .map(File::new)
                                                           .map(File::toURI)
                                                           .map(repositoryRootDirectoryUri::relativize)
                                                           .filter(u -> filePath.startsWith(u.getPath()))
                                                           .collect(Collectors.toList());
                    for (final URI repositoryRelativeSourceRootUri : entrySourceRoots)
                    {
                        processDiffEntry(formatter, entry, repositoryRelativeSourceRootUri, newFiles, changedLinesByFile);
                    }
                }
            }
            return changes;
        }
        return new ProjectChanges(Collections.emptyMap(), Collections.emptySet());
    }

    /**
     * Resolves the branch to compare with.
     *
     * @return the branch to compare with.
     * @throws IOException if an I/O error occurs.
     */
    protected String resolveCompareBranch() throws IOException
    {
        String longBranchName = Constants.R_HEADS + shortenRefName(compareBranch);
        final RevCommit refCommit = getCommitForRef(repository, longBranchName);
        if (refCommit == null)
        {
            return fallbackToRemoteBranch();
        }
        else
        {
            final BranchTrackingStatus trackingStatus = BranchTrackingStatus.of(repository, compareBranch);
            if (trackingStatus != null)
            {
                if (trackingStatus.getBehindCount() > 0)
                {
                    longBranchName = trackingStatus.getRemoteTrackingBranch();
                    log.warn(String.format("%s is behind remote tracking branch, comparing with %s instead.",
                                           compareBranch, shortenRefName(longBranchName)));
                }
                else if (longBranchName.equals(repository.getFullBranch()) && trackingStatus.getAheadCount() > 0)
                {
                    longBranchName = trackingStatus.getRemoteTrackingBranch();
                    log.warn(String.format("%s is ahead of remote tracking branch, comparing with %s instead.",
                                           compareBranch, shortenRefName(longBranchName)));
                }
            }
        }
        return longBranchName;
    }

    /**
     * Fallback to the remote branch if the local branch doesn't exist.
     *
     * @return the remote branch ref.
     */
    @Nullable
    private String fallbackToRemoteBranch()
    {
        try
        {
            final List<Ref> refList = new Git(repository).branchList().setListMode(ListBranchCommand.ListMode.REMOTE).call();
            final Ref ref = refList.stream()
                                // Try origin/compareBranch first.
                                .filter(r -> r.getName().equals(R_REMOTES + "origin/" + compareBranch))
                                .findAny()
                                .orElseGet(() -> refList.stream()
                                                     // Fallback to */compareBranch
                                                     .filter(r2 -> r2.getName().matches(R_REMOTES + "[^/]+/" + compareBranch))
                                                     .findAny()
                                                     .orElse(null));
            if (ref != null)
            {
                final String remoteBranch = ref.getName();
                log.warn(String.format("Local branch named %s not found, using %s instead.",
                                       compareBranch, shortenRefName(remoteBranch)));
                return remoteBranch;
            }
        }
        catch (final GitAPIException e)
        {
            throw new RuntimeException(e);
        }
        return null;
    }

    /**
     * Processes a diff entry to extract the change information.
     *
     * @param formatter the diff formatter.
     * @param entry the diff entry.
     * @param repositoryRelativeSourceRootUri the source root URI relative to the Git repository root.
     * @param newFiles the set to add change information about new files to.
     * @param changedLinesByFile the map to add change information about changed files to.
     * @throws IOException if an I/O error occurs.
     */
    protected void processDiffEntry(final DiffFormatter formatter, final DiffEntry entry,
                                    final URI repositoryRelativeSourceRootUri, final Set<String> newFiles,
                                    final Map<String, Set<Integer>> changedLinesByFile) throws IOException
    {
        final URI sourceRootRelativeFileUri = repositoryRelativeSourceRootUri.relativize(URI.create(entry.getNewPath()));
        if (entry.getChangeType() == DiffEntry.ChangeType.MODIFY)
        {
            final FileHeader header = formatter.toFileHeader(entry);
            header.getHunks().stream()
                .flatMap(hunk -> hunk.toEditList().stream())
                .filter(e -> e.getType() == Edit.Type.INSERT || e.getType() == Edit.Type.REPLACE)
                .forEach(edit -> {
                    final Set<Integer> changedLines = changedLinesByFile.computeIfAbsent(sourceRootRelativeFileUri.toString(),
                                                                                         k -> new TreeSet<>());
                    IntStream.rangeClosed(edit.getBeginB() + 1, edit.getEndB()).forEachOrdered(changedLines::add);
                });
        }
        else if (entry.getChangeType() == DiffEntry.ChangeType.ADD)
        {
            newFiles.add(sourceRootRelativeFileUri.toString());
        }
    }

    /**
     * Returns the commit for the given ref.
     *
     * @param repository the repository.
     * @param ref the ref.
     * @return the commit.
     * @throws IOException if an I/O error occurs.
     */
    @Nullable
    @CheckForNull
    protected RevCommit getCommitForRef(final Repository repository, final String ref) throws IOException
    {
        final RevCommit commit;
        try (RevWalk walk = new RevWalk(repository))
        {
            final Ref repositoryRef = repository.findRef(ref);
            commit = repositoryRef == null ? null : walk.parseCommit(repositoryRef.getObjectId());
            walk.dispose();
        }
        return commit;
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
    protected RevCommit getMergeBase(final Repository repository, final String source, final String target) throws IOException
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
    protected static AbstractTreeIterator prepareTreeParser(final Repository repository, final RevCommit commit)
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
