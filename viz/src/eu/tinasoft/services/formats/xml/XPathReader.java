/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.tinasoft.services.formats.xml;

import eu.tinasoft.services.debug.Console;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import javax.xml.namespace.QName;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;

public class XPathReader {

    private InputStream xmlStream;
    private Document xmlDocument;
    private XPath xPath;

    public XPathReader() {
    }

    public void parseFromURI(String uriString) throws URISyntaxException, MalformedURLException, IOException {

        this.xmlStream = new URI(uriString).toURL().openStream();
        initObjects();
    }

    public void parseFromStream(InputStream inputStream) {

        this.xmlStream = inputStream;
        initObjects();
    }

    public void parseFromString(String inputString) {
        this.xmlStream = new ByteArrayInputStream(inputString.getBytes());
        initObjects();
    }

    private void initObjects() {
        try {
            xmlDocument = DocumentBuilderFactory.newInstance().newDocumentBuilder().
                    parse(xmlStream);
            xPath = XPathFactory.newInstance().
                    newXPath();
        } catch (IOException ex) {
            Console.log(ex.getMessage());
        } catch (SAXException ex) {
            Console.log(ex.getMessage());
        } catch (ParserConfigurationException ex) {
            Console.log(ex.getMessage());
        }
    }

    public Object read(String expression,
            QName returnType) throws XPathExpressionException {
        XPathExpression xPathExpression =
                xPath.compile(expression);
        return xPathExpression.evaluate(xmlDocument, returnType);
    }
}
