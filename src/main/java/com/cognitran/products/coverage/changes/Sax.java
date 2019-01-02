/*
 * Copyright (c) 2019 Cognitran Limited. All Rights Reserved.
 */
package com.cognitran.products.coverage.changes;/*
 * Copyright (c) 2016 Cognitran Limited. All Rights Reserved.
 */

import java.io.IOException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**
 * SAX utility class.
 */
class Sax
{
    /**
     * Parses an XML document.
     *
     * @param document the XML document.
     * @param handler the SAX event handler.
     * @param namespaceAware whether the namespace aware mode should be enabled (recommended).
     * @throws IOException if an I/O error occurs.
     * @throws SAXException if any SAX errors occur during processing.
     */
    static void parse(final InputSource document, final DefaultHandler handler, final boolean namespaceAware) throws IOException,
                                                                                                                            SAXException
    {
        try
        {
            final SAXParserFactory factory = SAXParserFactory.newInstance();
            factory.setNamespaceAware(namespaceAware);
            factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

            final SAXParser parser = factory.newSAXParser();

            parser.parse(document, handler);
        }
        catch (final ParserConfigurationException e)
        {
            throw new RuntimeException(e);
        }
    }
}
