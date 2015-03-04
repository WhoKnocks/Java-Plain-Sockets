package Helperclass;

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

    public static byte[] parseDataToContent(byte[] inputData) {
        StringBuilder headers = new StringBuilder();
        int indexStartContent = 0;
        int index = 0;
        String responseLine = "";
        do {
            //controleer of het einde van een rij bereikt is
            if (inputData[index] == '\n') {
                //check er geen lege string werd ingelezen (dus de twee lege regels tussen headers/content
                if (responseLine.length() != 0) {
                    responseLine = "";
                } else {
                    //lees de volgende bite
                    indexStartContent = index + 1;
                }
            } else {
                //kijk of het einde van een rij bereikt is
                if (inputData[index] != '\r')
                    responseLine += (char) inputData[index];
            }

            index++;
        } while (indexStartContent == 0);

        byte[] content = new byte[inputData.length - indexStartContent];
        System.arraycopy(inputData, indexStartContent, content, 0, content.length);
        return content;
    }

    public static String parseDataToHeaders(byte[] inputData) {
        StringBuilder headers = new StringBuilder();
        int indexStartContent = 0;
        int index = 0;
        String responseLine = "";
        Boolean firstLineRead = false;
        do {
            //controleer of het einde van een rij bereikt is
            if (inputData[index] == '\n') {
                //check er geen lege string werd ingelezen (dus de twee lege regels tussen headers/content
                if (responseLine.length() != 0) {
                    if (firstLineRead) {
                        headers.append(responseLine).append("\n");
                        responseLine = "";
                    }
                    firstLineRead = true;
                } else {
                    //lees de volgende bite
                    indexStartContent = index + 1;
                }
            } else {
                //kijk of het einde van een rij bereikt is
                if (inputData[index] != '\r')
                    responseLine += (char) inputData[index];
            }

            index++;
        } while (indexStartContent == 0);

        return headers.toString();
    }


    public static void main(String[] args) {
        HTMLParser par = new HTMLParser();

        getImageSrc("");

    }
}
