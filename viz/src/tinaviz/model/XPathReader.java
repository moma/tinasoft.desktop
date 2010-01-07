/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package tinaviz.model;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import javax.xml.XMLConstants;
import javax.xml.namespace.QName;
import javax.xml.parsers.*;
import javax.xml.xpath.*;
import org.w3c.dom.Document;
import org.xml.sax.SAXException;


public class XPathReader {

    private InputStream xmlStream;

    private Document xmlDocument;
    private XPath xPath;

    public XPathReader(String uriString) throws URISyntaxException, MalformedURLException, IOException {

        this.xmlStream = new URI(uriString).toURL().openStream();
        /*
        BufferedReader in = new BufferedReader(
				new InputStreamReader(
				url.openStream()));
        this.xmlFile = xmlFile;*/
        initObjects();
    }

    private void initObjects(){
        try {
            xmlDocument = DocumentBuilderFactory.
			newInstance().newDocumentBuilder().
			parse(xmlStream);
            xPath =  XPathFactory.newInstance().
			newXPath();
        } catch (IOException ex) {
            //ex.printStackTrace();
            System.out.println(ex.getMessage());
        } catch (SAXException ex) {
            //ex.printStackTrace();
            System.out.println(ex.getMessage());
        } catch (ParserConfigurationException ex) {
            //ex.printStackTrace();
            System.out.println(ex.getMessage());
        }
    }

    public Object read(String expression,
			QName returnType) throws XPathExpressionException{
        //try {
            XPathExpression xPathExpression =
			xPath.compile(expression);

            return xPathExpression.evaluate
			(xmlDocument, returnType);
        /*} catch (XPathExpressionException ex) {
            //ex.printStackTrace();
            System.out.println(ex.getMessage());
            return null;
        }*/
    }
    
}