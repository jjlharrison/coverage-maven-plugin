import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.cognitran.products.coverage.NewCodeCoverage;

public class JacocoReportParser extends DefaultHandler
{
    private Set<String> interestingPackages;

    private final Set<String> newFiles;

    private final Map<String, Set<Integer>> changedLinesByFile;

    private JacocoReportPackageParser delegate;

    private final List<NewCodeCoverage> coverage = new ArrayList<>();

    public List<NewCodeCoverage> getCoverage()
    {
        return coverage;
    }

    public JacocoReportParser(final Set<String> newFiles, final Map<String, Set<Integer>> changedLinesByFile)
    {
        this.newFiles = newFiles;
        this.changedLinesByFile = changedLinesByFile;
        interestingPackages = new HashSet<>(CoverageDiffMojo.capacity(newFiles.size() + changedLinesByFile.size()));
        Stream.concat(newFiles.stream(), changedLinesByFile.keySet().stream())
            .map(n -> n.substring(0, n.lastIndexOf('/')))
            .forEach(interestingPackages::add);
    }

    @Override
    public void startElement(final String uri, final String localName, final String qName, final Attributes attributes) throws SAXException
    {
        if (delegate != null)
        {
            delegate.startElement(uri, localName, qName, attributes);
        }
        if (qName.equals("package"))
        {
            final String packageName = attributes.getValue("name");
            if (interestingPackages.contains(packageName))
            {
                delegate = new JacocoReportPackageParser(packageName, newFiles, changedLinesByFile);
            }
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException
    {
        super.endElement(uri, localName, qName);

        if (qName.equals("package") && delegate != null)
        {
            coverage.addAll(delegate.getCoverage());
            delegate = null;
        }
    }
}
