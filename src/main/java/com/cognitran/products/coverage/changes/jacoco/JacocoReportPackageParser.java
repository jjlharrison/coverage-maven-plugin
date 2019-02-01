/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes.jacoco;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.cognitran.products.coverage.changes.ChangeCoverage;

/**
 * Parses the &lt;package&gt; element to extract the change coverage information for a package.
 */
public class JacocoReportPackageParser extends DefaultHandler
{
    /** The package name to extract coverage information for. */
    private final String packageName;

    /** The new files to extract coverage information for. */
    private final Set<String> newFiles;

    /** The lines to extract coverage information indexed by the name of the modified files. */
    private final Map<String, Set<Integer>> changedLinesByFile;

    /** The coverage information for the changes. */
    private final List<ChangeCoverage> coverage;

    /** The delegate parser to parse child elements. */
    private ContentHandler delegate;

    /**
     * Constructor.
     *
     * @param packageName the package name to extract coverage information for.
     * @param newFiles the new files to extract coverage information for.
     * @param changedLinesByFile the lines to extract coverage information indexed by the name of the modified files.
     */
    public JacocoReportPackageParser(final String packageName,
                                     final Set<String> newFiles,
                                     final Map<String, Set<Integer>> changedLinesByFile)
    {
        this.packageName = packageName;
        this.newFiles = newFiles;
        this.changedLinesByFile = changedLinesByFile;
        coverage = new ArrayList<>(newFiles.size() + changedLinesByFile.size());
    }

    /**
     * Returns the coverage information for the changes.
     *
     * @return the coverage information for the changes.
     */
    public List<ChangeCoverage> getCoverage()
    {
        return coverage;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
    {
        if (delegate != null)
        {
            delegate.startElement(uri, localName, qName, attributes);
        }
        else if ("sourcefile".equals(qName))
        {
            final String filePath = (packageName.isEmpty() ? "" : packageName + '/') + attributes.getValue("name");
            if (newFiles.contains(filePath))
            {
                final JacocoReportNewFileParser newFileParser = new JacocoReportNewFileParser(filePath);
                coverage.add(newFileParser.getCoverage());
                this.delegate = newFileParser;
            }
            else if (changedLinesByFile.containsKey(filePath))
            {
                final JacocoReportModifiedSourcefileParser changedFileParser
                    = new JacocoReportModifiedSourcefileParser(filePath, changedLinesByFile.get(filePath));
                coverage.add(changedFileParser.getCoverage());
                this.delegate = changedFileParser;
            }
        }
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException
    {
        if ("sourcefile".equals(qName))
        {
            delegate = null;
        }
        else if (delegate != null)
        {
            delegate.endElement(uri, localName, qName);
        }
    }
}
