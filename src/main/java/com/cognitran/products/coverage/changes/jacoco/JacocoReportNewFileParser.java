/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes.jacoco;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.cognitran.products.coverage.changes.NewFileCodeCoverage;

/**
 * Parses the &lt;sourcefile&gt; element to extract the coverage information for a new file.
 */
public class JacocoReportNewFileParser extends DefaultHandler
{
    /** The coverage information for the new file. */
    private final NewFileCodeCoverage coverage;

    /**
     * Constructor.
     *
     * @param filePath the path of the modified file.
     */
    public JacocoReportNewFileParser(final String filePath)
    {
        coverage = new NewFileCodeCoverage(filePath);
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
    {
        if ("counter".equals(qName))
        {
            final String type = attributes.getValue("type");
            if ("BRANCH".equals(type))
            {
                coverage.setMissedChangedBranchesCount(Integer.parseInt(attributes.getValue("missed")));
                coverage.setCoveredChangedBranchesCount(Integer.parseInt(attributes.getValue("covered")));
            }
            else if ("LINE".equals(type))
            {
                coverage.setMissedChangedLinesCount(Integer.parseInt(attributes.getValue("missed")));
                coverage.setCoveredChangedLinesCount(Integer.parseInt(attributes.getValue("covered")));
            }
        }
    }

    /**
     * Returns the coverage information for the new file.
     *
     * @return the coverage information for the new file.
     */
    public NewFileCodeCoverage getCoverage()
    {
        return coverage;
    }
}
