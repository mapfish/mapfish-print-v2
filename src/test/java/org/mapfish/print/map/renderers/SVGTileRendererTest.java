/*
 * Copyright (C) 2026 GeoCat BV
 *
 * This file is part of MapFish Print
 *
 * MapFish Print is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * MapFish Print is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with MapFish Print.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.mapfish.print.map.renderers;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

import org.junit.Test;
import org.mapfish.print.PrintTestCase;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

public class SVGTileRendererTest extends PrintTestCase {

    @Test
    public void testLine() throws Exception {
        String svg = """
            <?xml version="1.0"?>
            <svg xmlns="http://www.w3.org/2000/svg" width="100" height="100" viewBox="0 0 100 100">
              <line x1="10" y1="10" x2="50" y2="50" stroke="black" stroke-width="2" stroke-dasharray="3,6, 9"/>
            </svg>
            """;

        Document result = svgZoomOut(svg, 2.0);
        assertNotNull("Result document should not be null", result);

        // widths should be scaled
        assertEquals("stroke-width", "4", attr(result, "line", "stroke-width"));
        assertEquals("stroke-dasharray", "6,12,18", attr(result, "line", "stroke-dasharray"));

        // other stuff unchanged
        assertEquals("viewBox unchanged", "0 0 100 100", attr(result, "viewBox"));
        assertEquals("width unchanged", "100", attr(result, "width"));

        String svg2 = """
            <?xml version="1.0"?>
            <svg xmlns="http://www.w3.org/2000/svg" width="100" height="100">
              <line x1="10" y1="10" x2="50" y2="50" stroke="black" stroke-dasharray="none"/>
            </svg>
            """;

        Document result2 = svgZoomOut(svg2, 2.0);
        assertEquals("preserve none", "none", attr(result2, "line", "stroke-dasharray"));

        String svg3 = """
            <?xml version="1.0"?>
            <svg xmlns="http://www.w3.org/2000/svg" width="100" height="100">
              <line x1="10" y1="10" x2="50" y2="50" stroke="black" stroke-width="2"/>
            </svg>
            """;

        Document result3 = svgZoomOut(svg3, 0.5f);
        assertEquals("strip decimal", "1", attr(result3, "line", "stroke-width"));

        String svg4 = """
            <?xml version="1.0"?>
            <svg xmlns="http://www.w3.org/2000/svg" width="100" height="100">
              <line x1="10" y1="10" x2="50" y2="50" stroke="black" stroke-width="2.5"/>
            </svg>
            """;

        Document result4 = svgZoomOut(svg4, 1.0f);
        assertEquals("unscaled", "2.5", attr(result4, "line", "stroke-width"));

    }

    @Test
    public void testRect() throws Exception {
        String svg = """
            <?xml version="1.0"?>
            <svg xmlns="http://www.w3.org/2000/svg" width="100" height="100">
              <rect x="10" y="10" width="30" height="30" rx="5" ry="3" fill="blue"/>
            </svg>
            """;

        Document result = svgZoomOut(svg, 2.0);
        assertEquals("10", attr(result, "rect", "rx"));
        assertEquals("6", attr(result, "rect", "ry"));
    }

    @Test
    public void testFont() throws Exception {
        String svg = """
            <?xml version="1.0"?>
            <svg xmlns="http://www.w3.org/2000/svg" width="100" height="100">
              <text x="10" y="20" font-size="12px" fill="red">Label</text>
            </svg>
            """;

        Document result = svgZoomOut(svg, 1.5);
        assertEquals("18px", attr(result, "text", "font-size"));
    }

    /**
     * Call svgZoomOut.xsl with the provided zoomFactor parameter.
     * @param svgContent SVG
     * @param zoomFactor Scale factor
     * @return Transformed document
     */
    private Document svgZoomOut(String svgContent, double zoomFactor) throws Exception {
        String xsltResource = "/org/mapfish/print/map/renderers/svgZoomOut.xsl";
        InputStream xsltStream = getClass().getResourceAsStream(xsltResource);

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        dbf.setNamespaceAware(true);
        dbf.setValidating(false);
        dbf.setExpandEntityReferences(false);

        DocumentBuilder db = dbf.newDocumentBuilder();
        Document xsltDoc;
        try (xsltStream) {
            xsltDoc = db.parse(xsltStream);
        }

        Document svgDoc = db.parse(new InputSource(new StringReader(svgContent)));

        TransformerFactory factory = TransformerFactory.newInstance();
        Transformer transformer = factory.newTransformer(new DOMSource(xsltDoc));
        transformer.setParameter("zoomFactor", zoomFactor);

        StringWriter output = new StringWriter();
        transformer.transform(new DOMSource(svgDoc), new StreamResult(output));

        return db.parse(new InputSource(new StringReader(output.toString())));
    }

    //
    // XPath Utility Functions
    //
    private String xpath(Document doc, String expression) throws Exception {
        XPath xpath = XPathFactory.newInstance().newXPath();
        return (String) xpath.evaluate(expression, doc, XPathConstants.STRING);
    }

    private String attr(Document doc, String elementName, String attributeName) throws Exception {
        return xpath(doc, "/*[local-name()='svg']/*[local-name()='" + elementName + "'][1]/@" + attributeName);
    }

    private String attr(Document doc, String attributeName) throws Exception {
        return xpath(doc, "/*[local-name()='svg']/@" + attributeName);
    }

}