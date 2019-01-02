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

import com.cognitran.products.coverage.changes.NewCodeCoverage;

public class JacocoReportPackageParser extends DefaultHandler
{
    private final String packageName;

    private final Set<String> newFiles;

    private final Map<String, Set<Integer>> changedLinesByFile;

    private final List<NewCodeCoverage> coverage;

    private ContentHandler delegate;

    public JacocoReportPackageParser(final String packageName,
                                     final Set<String> newFiles,
                                     final Map<String, Set<Integer>> changedLinesByFile)
    {
        this.packageName = packageName;
        this.newFiles = newFiles;
        this.changedLinesByFile = changedLinesByFile;
        coverage = new ArrayList<>(newFiles.size() + changedLinesByFile.size());
    }

    public List<NewCodeCoverage> getCoverage()
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
        else if (qName.equals("sourcefile"))
        {
            final String name = packageName + '/' + attributes.getValue("name");
            if (newFiles.contains(name))
            {
                final JacocoReportNewFileParser newFileParser = new JacocoReportNewFileParser();
                // TODO filePath should be constructor arg.
                newFileParser.getCoverage().setFilePath(name);
                coverage.add(newFileParser.getCoverage());
                this.delegate = newFileParser;
            }
            else if (changedLinesByFile.containsKey(name))
            {
                final JacocoReportChangedFileParser changedFileParser = new JacocoReportChangedFileParser();
                // TODO filePath and changed lines should be constructor args.
                changedFileParser.getCoverage().setFilePath(name);
                changedFileParser.getCoverage().setChangedLines(changedLinesByFile.get(name));
                coverage.add(changedFileParser.getCoverage());
                this.delegate = changedFileParser;
            }
        }
        super.startElement(uri, localName, qName, attributes);
    }

    @Override
    public void endElement(final String uri, final String localName, final String qName) throws SAXException
    {
        super.endElement(uri, localName, qName);

        if (qName.equals("sourcefile"))
        {
            delegate = null;
        }
    }
}
