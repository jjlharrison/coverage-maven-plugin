package com.jjlharrison.coverage.changes.jacoco;

import static com.jjlharrison.coverage.changes.Utilities.capacity;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.jjlharrison.coverage.changes.ChangeCoverage;

/**
 * Parses the JaCoCo XML report to extract coverage information for changes.
 */
public class JacocoReportParser extends DefaultHandler
{
    /** Packages that have changes. */
    private Set<String> interestingPackages;

    /** New files. */
    private final Set<String> newFiles;

    /** Lines that have been changed mapped by file. */
    private final Map<String, Set<Integer>> changedLinesByFile;

    /** The delegate parser to parse child elements. */
    private JacocoReportPackageParser delegate;

    /** Coverage information for changes. */
    private final List<ChangeCoverage> coverage;

    /**
     * Returns coverage information for changes.
     *
     * @return coverage information for changes.
     */
    public List<ChangeCoverage> getCoverage()
    {
        return coverage;
    }

    /**
     * Constructor.
     *
     * @param newFiles the new files to extract coverage information for.
     * @param changedLinesByFile the lines to extract coverage information indexed by the name of the modified files.
     */
    public JacocoReportParser(final Set<String> newFiles, final Map<String, Set<Integer>> changedLinesByFile)
    {
        this.newFiles = newFiles;
        this.changedLinesByFile = changedLinesByFile;
        final int changeCount = newFiles.size() + changedLinesByFile.size();
        interestingPackages = new HashSet<>(capacity(changeCount));
        Stream.concat(newFiles.stream(), changedLinesByFile.keySet().stream())
            .map(n -> n.contains("/") ? n.substring(0, n.lastIndexOf('/')) : "")
            .forEach(interestingPackages::add);
        coverage = new ArrayList<>(changeCount);
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
    {
        if (delegate != null)
        {
            delegate.startElement(uri, localName, qName, attributes);
        }
        if ("package".equals(qName))
        {
            final String packageName = attributes.getValue("name");
            if (interestingPackages.contains(packageName))
            {
                delegate = new JacocoReportPackageParser(packageName, newFiles, changedLinesByFile);
            }
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException
    {
        if (delegate != null)
        {
            if ("package".equals(qName))
            {
                coverage.addAll(delegate.getCoverage());
                delegate = null;
            }
            else
            {
                delegate.endElement(uri, localName, qName);
            }
        }
    }
}
