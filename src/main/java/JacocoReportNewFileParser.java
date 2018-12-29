import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.cognitran.products.coverage.NewFileCodeCoverage;

public class JacocoReportNewFileParser extends DefaultHandler
{
    private final NewFileCodeCoverage coverage = new NewFileCodeCoverage();

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
    {
        super.startElement(uri, localName, qName, attributes);

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

    public NewFileCodeCoverage getCoverage()
    {
        return coverage;
    }
}
