/*
 * Copyright (c) 2016 Cognitran Limited. All Rights Reserved.
 */

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import javax.annotation.Nonnull;
import javax.annotation.WillNotClose;
import javax.xml.XMLConstants;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.sun.org.apache.xerces.internal.dom.DOMInputImpl;

/**
 * SAX utility class.
 */
public class Sax
{
    /**
     * Validates an XML document.
     *
     * @param document       the XML document stream.
     * @param schemaLocation the location of schema to validate against.
     * @throws IOException  if an I/O error occurs.
     * @throws SAXException if any SAX errors occur during processing.
     */
    public static void validate(@Nonnull @WillNotClose final InputStream document,
                                @Nonnull final String schemaLocation) throws IOException, SAXException
    {
        final URL schemaUrl = Sax.class.getClassLoader().getResource(schemaLocation);
        final Source xmlFile = new StreamSource(document);
        final SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        schemaFactory.setResourceResolver((t, n, p, s, b) ->
                                          {
                                              final String path = '/' + s.replace("classpath://", "");
                                              final InputStream resource = Sax.class.getResourceAsStream(path);
                                              // input type doesn't matter, returns generic byte stream
                                              return new DOMInputImpl(p, s, b, resource, "UTF-8");
                                          });
        if (schemaUrl != null)
        {
            final Schema schema = schemaFactory.newSchema(schemaUrl);
            final Validator validator = schema.newValidator();
            validator.validate(xmlFile);
        }
    }

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
        doParse(document, handler, namespaceAware);
    }

    /**
     * Parses an XML document.
     *
     * @param document the XML document.
     * @param handler the SAX event handler.
     * @param namespaceAware whether the namespace aware mode should be enabled (recommended).
     * @throws IOException if an I/O error occurs.
     * @throws SAXException if any SAX errors occur during processing.
     */
    private static void doParse(final InputSource document, final DefaultHandler handler, final boolean namespaceAware)
        throws IOException, SAXException
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
