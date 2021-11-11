package com.jjlharrison.coverage.update;import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

import javax.annotation.Nullable;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.plugins.annotations.LifecyclePhase;
import org.apache.maven.plugins.annotations.Mojo;
import org.apache.maven.plugins.annotations.Parameter;
import org.apache.maven.project.MavenProject;
import org.jsoup.Jsoup;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * Goal which update minimum test coverage requirements based on current coverage.
 */
@Mojo(name = "update-coverage-requirement",
      threadSafe = true,
      defaultPhase = LifecyclePhase.POST_SITE)
public class UpdateCoverageRequirementMojo extends AbstractMojo
{
    /** The Maven project. */
    @Parameter(defaultValue = "${project}", readonly = true)
    private MavenProject project;

    /** The JaCoCo HTML report file. */
    @Parameter(defaultValue = "${project.reporting.outputDirectory}/jacoco/index.html", required = true)
    private File jacocoHtmlReport;

    /** The name of the minimum branch coverage property. */
    @Parameter(defaultValue = "jacoco.coverage.branch.minimum")
    private String jacocoBranchCoverageMinimumProperty;

    /** The name of the minimum line coverage property. */
    @Parameter(defaultValue = "jacoco.coverage.line.minimum")
    private String jacocoLineCoverageMinimumProperty;

    /** The upper bound for the coverage requirement. */
    @Parameter(defaultValue = "92", required = true)
    private int requirementUpperBound;

    @Override
    public void execute() throws MojoExecutionException
    {
        switch (project.getPackaging())
        {
            case "pom":
                getLog().info("Skipping POM module.");
                break;
            case "ear":
                getLog().info("Skipping EAR module.");
                break;
            default:
                final File pom = new File(project.getBasedir(), "pom.xml");

                if (pom.isFile())
                {
                    final Document doc = parseDocumentFromFile(pom);

                    final boolean lineRequirementUpdated = updateCoverageRequirementProperty("line", doc,
                                                                                             jacocoLineCoverageMinimumProperty,
                                                                                             getCurrentLineCoveragePercentage());
                    final boolean branchRequirementUpdated = updateCoverageRequirementProperty("branch", doc,
                                                                                               jacocoBranchCoverageMinimumProperty,
                                                                                               getCurrentBranchCoveragePercentage());
                    if (lineRequirementUpdated || branchRequirementUpdated)
                    {
                        writeDocument(doc, pom);
                    }
                }
                break;
        }
    }

    /**
     * Updates the property value in the given document with the coverage requirement based on the JaCoCo report index.
     *
     * @param coverageType the coverage type to update the requirement property for.
     * @param doc the document to be updated (POM).
     * @param property the property to update.
     * @param currentCoveragePercentage the current coverage percentage.
     * @return whether the property value was changed.
     * @throws MojoExecutionException if the update fails due to invalid input.
     */
    private boolean updateCoverageRequirementProperty(final String coverageType, final Document doc, final String property,
                                                      final Integer currentCoveragePercentage)
        throws MojoExecutionException
    {
        final Node propertyNode = getPropertyNode(doc, property);
        if (propertyNode == null)
        {
            getLog().info("Could not find current value for property, skipping.");
        }
        else
        {
            if (currentCoveragePercentage == null)
            {
                getLog().info("Could not determine current coverage level, skipping.");
            }
            else
            {
                return setCoverageRequirement(coverageType, propertyNode, currentCoveragePercentage);
            }
        }
        return false;
    }

    /**
     * Sets the coverage requirement on the given property node.
     *
     * @param coverageType the coverage type being set.
     * @param propertyNode the property node.
     * @param currentCoverage the current coverage level.
     * @return whether the requirement was changed.
     */
    private boolean setCoverageRequirement(final String coverageType, final Node propertyNode, final int currentCoverage)
    {
        final Integer currentRequirement = getFirstDigit(propertyNode.getTextContent());
        final int newRequirement = Math.min(Math.max(currentRequirement == null ? 0 : currentRequirement,
                                                     currentCoverage - 1), requirementUpperBound);
        if (currentRequirement == null || currentRequirement != newRequirement)
        {
            getLog().info("Setting " + coverageType + " coverage requirement to " + newRequirement + "%.");
            propertyNode.setTextContent(newRequirement + "%");
            return true;
        }
        else
        {
            getLog().info("Requirement for " + coverageType + " coverage already set to " + currentRequirement + "%.");
        }
        return false;
    }

    /**
     * Returns the current branch coverage percentage from the JaCoCo HTML report.
     *
     * @return the coverage percentage, or {@code null} if the percentage could not be retrieved.
     */
    private Integer getCurrentBranchCoveragePercentage()
    {
        try
        {
            if (jacocoHtmlReport != null && jacocoHtmlReport.isFile())
            {
                final org.jsoup.nodes.Document jacocoReportDoc = Jsoup.parse(jacocoHtmlReport, "UTF-8");
                return getFirstDigit(jacocoReportDoc.select("#coveragetable > tfoot > tr > td:eq(" + 4 + ")").text());
            }
            getLog().info("JaCoCo report not found (" + jacocoHtmlReport + ").");
            return null;
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the current line coverage percentage from the JaCoCo HTML report.
     *
     * @return the coverage percentage, or {@code null} if the percentage could not be retrieved.
     */
    private Integer getCurrentLineCoveragePercentage()
    {
        try
        {
            if (jacocoHtmlReport != null && jacocoHtmlReport.isFile())
            {
                final org.jsoup.nodes.Document jacocoReportDoc = Jsoup.parse(jacocoHtmlReport, "UTF-8");
                final Integer missedLines = getFirstDigit(jacocoReportDoc.select("#coveragetable > tfoot > tr > td:eq(7)").text());
                final Integer totalLines = getFirstDigit(jacocoReportDoc.select("#coveragetable > tfoot > tr > td:eq(8)").text());
                return missedLines == null || totalLines == null ? null : (int) ((1 - ((double) missedLines / (double) totalLines)) * 100);
            }
            getLog().info("JaCoCo report not found (" + jacocoHtmlReport + ").");
            return null;
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Writes the updated document to the given file.
     *
     * @param document the document to write.
     * @param file the file to write to.
     */
    private void writeDocument(final Document document, final File file)
    {
        document.setXmlStandalone(true);
        try
        {
            final Transformer transformer = TransformerFactory.newInstance().newTransformer();
            transformer.setOutputProperty(OutputKeys.INDENT, "yes");
            try (FileOutputStream fileOutputStream = new FileOutputStream(file))
            {
                transformer.transform(new DOMSource(document), new StreamResult(fileOutputStream));
            }
        }
        catch (final IOException | TransformerException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the property node for the given property.
     *
     * @param doc the document (POM).
     * @param property the property name,
     * @return the node.
     * @throws MojoExecutionException if the document is not valid.
     */
    @Nullable
    private Node getPropertyNode(final Document doc, final String property) throws MojoExecutionException
    {
        try
        {
            final XPath xpath = XPathFactory.newInstance().newXPath();
            final NodeList nodeList = (NodeList) xpath.compile("//properties/" + property)
                                                     .evaluate(doc, XPathConstants.NODESET);

            if (nodeList.getLength() == 0)
            {
                final NodeList propertiesNodeList = (NodeList) xpath.compile("//properties").evaluate(doc, XPathConstants.NODESET);
                if (propertiesNodeList.getLength() == 1)
                {
                    final Element propertyNode = doc.createElement(property);
                    propertiesNodeList.item(0).appendChild(propertyNode);
                    return propertyNode;
                }
            }
            if (nodeList.getLength() > 1)
            {
                throw new MojoExecutionException("More than 1 property element found for property: " + property);
            }
            return nodeList.getLength() == 1 ? nodeList.item(0) : null;
        }
        catch (final XPathExpressionException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Parses a document from the given file.
     *
     * @param file the file.
     * @return the parsed document.
     */
    private Document parseDocumentFromFile(final File file)
    {
        try (FileInputStream fileInputStream = new FileInputStream(file);
             InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, StandardCharsets.UTF_8))
        {
            return DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(new InputSource(inputStreamReader));
        }
        catch (final IOException | ParserConfigurationException | SAXException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Returns the first number from a string that could contain non-digit characters.
     *
     * @param string the string to parse.
     * @return the first number in the string.
     */
    private Integer getFirstDigit(final String string)
    {
        if (string == null)
        {
            return null;
        }

        final StringBuilder builder = new StringBuilder(4);
        for (int i = 0; i < string.length(); i++)
        {
            final char c = string.charAt(i);
            if (Character.isDigit(c))
            {
                builder.append(c);
            }
            else if (c != ',' && builder.length() != 0)
            {
                break;
            }
        }
        return builder.length() > 0 ? Integer.parseInt(builder.toString()) : null;
    }
}
