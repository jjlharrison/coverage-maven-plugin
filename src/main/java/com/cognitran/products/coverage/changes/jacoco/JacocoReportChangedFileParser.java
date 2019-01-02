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
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException
    {
        super.startElement(uri, localName, qName, attributes);

        if (qName.equals("line") && coverage.getChangedLines().contains(Integer.valueOf(attributes.getValue("nr"))))
        {
            //<line nr="45" mi="0" ci="5" mb="0" cb="0"/>
            coverage.getCoverage().add(
                new LineCodeCoverage(Integer.parseInt(attributes.getValue("nr")),
                                     Integer.parseInt(attributes.getValue("ci")) > 0 ? 1 : 0,
                                     Integer.parseInt(attributes.getValue("mi")) > 0 ? 1 : 0,
                                     Integer.parseInt(attributes.getValue("cb")),
                                     Integer.parseInt(attributes.getValue("mb"))
                ));
        }
    }
}
