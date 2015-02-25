

import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

/**
 * Created by GJ on 24/02/2015.
 */
public class HTMLParser {

    public static List<String> getImageSrc(String htmlString) {

        ArrayList<String> sources = new ArrayList<>();

        XPathFactory xpathFactory = XPathFactory.newInstance();
        XPath xpath = xpathFactory.newXPath();
        InputSource source = new InputSource(new StringReader(htmlString));

        NodeList status;
        try {
            status = (NodeList) xpath.evaluate("//img/@src", source, XPathConstants.NODESET);

            int i = 0;
            while (status.item(i) != null) {
                sources.add(status.item(i).getTextContent());
            }

        } catch (XPathExpressionException e) {
            e.printStackTrace();
        }

        return sources;

    }

    public static void main(String[] args) {
        HTMLParser par = new HTMLParser();

        getImageSrc("");

    }
}
