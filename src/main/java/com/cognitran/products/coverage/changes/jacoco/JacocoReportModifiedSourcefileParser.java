/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes.jacoco;

import java.util.Set;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.cognitran.products.coverage.changes.LineCodeCoverage;
import com.cognitran.products.coverage.changes.ModifiedFileChangeCoverage;

/**
 * Parses the &lt;sourcefile&gt; element to extract the coverage information for a modified file.
 */
public class JacocoReportModifiedSourcefileParser extends DefaultHandler
{
    /** The coverage information for the modified file. */
    private final ModifiedFileChangeCoverage coverage;

    /**
     * Constructor.
     *
     * @param filePath the path of the modified file.
     * @param changedLineNumbers the changed line numbers.
     */
    public JacocoReportModifiedSourcefileParser(final String filePath, final Set<Integer> changedLineNumbers)
    {
        coverage = new ModifiedFileChangeCoverage(filePath, changedLineNumbers);
    }

    /**
     * Returns the coverage information for the modified file.
     *
     * @return the coverage information for the modified file.
     */
    public ModifiedFileChangeCoverage getCoverage()
    {
        return coverage;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
    {
        super.startElement(uri, localName, qName, attributes);

        if ("line".equals(qName) && coverage.getChangedLineNumbers().contains(Integer.valueOf(attributes.getValue("nr"))))
        {
            //<line nr="45" mi="0" ci="5" mb="0" cb="0"/>
            final boolean hasCoveredInstructions = parseNullableIntString(attributes.getValue("ci")) > 0;
            final boolean hasMissedInstructions = parseNullableIntString(attributes.getValue("mi")) > 0;
            coverage.getCoverage().add(
                new LineCodeCoverage(parseNullableIntString(attributes.getValue("nr")),
                                     hasCoveredInstructions ? 1 : 0,
                                     !hasCoveredInstructions && hasMissedInstructions ? 1 : 0,
                                     parseNullableIntString(attributes.getValue("cb")),
                                     parseNullableIntString(attributes.getValue("mb"))
                ));
        }
    }

    /**
     * Parses the given string to return an int value or 0 if the string is null.
     *
     * @param value the string to parse.
     * @return the int value.
     */
    private int parseNullableIntString(final String value)
    {
        return value == null ? 0 : Integer.parseInt(value);
    }
}
