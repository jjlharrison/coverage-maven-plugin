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
 * Utilities that should be replaced or moved elsewhere (like products-utilities).
 */
public class Utilities
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
    public static void parse(final InputSource document, final DefaultHandler handler, final boolean namespaceAware) throws IOException,
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

    /**
     * Returns a capacity for a HashSet or HashMap that is sufficient to keep the map from being resized as
     * long as it grows no larger than expectedSize and the load factor is >= its
     * default (0.75).
     *
     * @param expectedSize the expected size.
     * @return the capacity.
     */
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
            // 1073741824 is the largest power of two that can be represented as an int.
            return expectedSize < 1073741824 ? (int) ((float) expectedSize / 0.75F + 1.0F) : Integer.MAX_VALUE;
        }
    }
}
