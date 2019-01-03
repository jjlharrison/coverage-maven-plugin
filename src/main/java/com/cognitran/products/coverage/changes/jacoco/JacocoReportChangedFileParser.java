/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes.jacoco;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.cognitran.products.coverage.changes.FileChangesCodeCoverage;
import com.cognitran.products.coverage.changes.LineCodeCoverage;

public class JacocoReportChangedFileParser extends DefaultHandler
{
    private final FileChangesCodeCoverage coverage = new FileChangesCodeCoverage();

    public FileChangesCodeCoverage getCoverage()
    {
        return coverage;
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
    {
        super.startElement(uri, localName, qName, attributes);

        if ("line".equals(qName) && coverage.getChangedLines().contains(Integer.valueOf(attributes.getValue("nr"))))
        {
            //<line nr="45" mi="0" ci="5" mb="0" cb="0"/>
            coverage.getCoverage().add(
                new LineCodeCoverage(parseIntAttribute(attributes, "nr"),
                                     parseIntAttribute(attributes, "ci") > 0 ? 1 : 0,
                                     parseIntAttribute(attributes, "mi") > 0 ? 1 : 0,
                                     parseIntAttribute(attributes, "cb"),
                                     parseIntAttribute(attributes, "mb")
                ));
        }
    }

    private int parseIntAttribute(final Attributes attributes, final String nr)
    {
        final String value = attributes.getValue(nr);
        return value == null ? 0 : Integer.parseInt(value);
    }
}
