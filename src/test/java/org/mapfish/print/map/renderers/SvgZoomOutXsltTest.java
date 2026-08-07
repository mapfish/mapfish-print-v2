/*
 * Copyright (C) 2013  Camptocamp
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

import java.io.InputStream;
import java.io.StringReader;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;

import org.junit.Test;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Exercises the {@code factorValue}/{@code factorArray} named templates in
 * {@code svgZoomOut.xsl}, which replaced the Xalan {@code CustomXPath}
 * extension functions of the same name.
 */
public class SvgZoomOutXsltTest {

    private static final String SVG_NS = "http://www.w3.org/2000/svg";

    private Element transform(String svg, double zoomFactor) throws Exception {
        try (InputStream xslStream = SvgZoomOutXsltTest.class.getResourceAsStream("svgZoomOut.xsl")) {
            Transformer xslt = TransformerFactory.newInstance().newTransformer(new StreamSource(xslStream));
            xslt.setParameter("zoomFactor", zoomFactor);

            DOMResult result = new DOMResult();
            xslt.transform(new StreamSource(new StringReader(svg)), result);
            return ((org.w3c.dom.Document) result.getNode()).getDocumentElement();
        }
    }

    private String attr(Element root, String tagName, String attrName) {
        NodeList nodes = root.getElementsByTagNameNS(SVG_NS, tagName);
        assertEquals("expected exactly one <" + tagName + "> element", 1, nodes.getLength());
        return ((Element) nodes.item(0)).getAttribute(attrName);
    }

    @Test
    public void testFactorValueScalesPlainAndUnitNumbers() throws Exception {
        String svg = "<svg><rect stroke-width=\"2\" rx=\"3.5\" ry=\"10px\" font-size=\"12pt\" fill=\"red\"/></svg>";

        Element root = transform(svg, 2);

        assertEquals("4", attr(root, "rect", "stroke-width"));
        assertEquals("7", attr(root, "rect", "rx"));
        assertEquals("20px", attr(root, "rect", "ry"));
        assertEquals("24pt", attr(root, "rect", "font-size"));
        // attributes not covered by factorValue/factorArray are copied verbatim
        assertEquals("red", attr(root, "rect", "fill"));
    }

    @Test
    public void testFactorValueKeepsFractionalResult() throws Exception {
        String svg = "<svg><rect stroke-width=\"3\"/></svg>";

        Element root = transform(svg, 1.5);

        assertEquals("4.5", attr(root, "rect", "stroke-width"));
    }

    @Test
    public void testFactorValueHandlesNegativeNumbers() throws Exception {
        String svg = "<svg><circle stroke-width=\"-4\"/></svg>";

        Element root = transform(svg, 2);

        assertEquals("-8", attr(root, "circle", "stroke-width"));
    }

    @Test
    public void testFactorArrayScalesCommaSeparatedList() throws Exception {
        String svg = "<svg><line stroke-dasharray=\"1, 2, 3\"/></svg>";

        Element root = transform(svg, 2);

        assertEquals("2,4,6", attr(root, "line", "stroke-dasharray"));
    }

    @Test
    public void testFactorArrayPassesThroughNone() throws Exception {
        String svg = "<svg><line stroke-dasharray=\"none\"/></svg>";

        Element root = transform(svg, 2);

        assertEquals("none", attr(root, "line", "stroke-dasharray"));
    }

    @Test
    public void testZoomFactorOfOneIsIdentity() throws Exception {
        String svg = "<svg><rect stroke-width=\"5\" stroke-dasharray=\"4,2\"/></svg>";

        Element root = transform(svg, 1);

        assertEquals("5", attr(root, "rect", "stroke-width"));
        assertEquals("4,2", attr(root, "rect", "stroke-dasharray"));
    }
}
