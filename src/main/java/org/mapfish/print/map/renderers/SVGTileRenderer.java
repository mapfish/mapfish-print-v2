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

import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URI;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.batik.ext.awt.RenderingHintsKeyExt;
import org.apache.batik.transcoder.TranscoderInput;
import org.apache.batik.transcoder.print.PrintTranscoder;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.mapfish.print.InvalidValueException;
import org.mapfish.print.RenderingContext;
import org.mapfish.print.Transformer;
import org.mapfish.print.map.MapTileTask;
import org.mapfish.print.map.ParallelMapTileLoader;
import org.w3c.dom.Document;

import com.lowagie.text.pdf.PdfGraphics2D;
import com.lowagie.text.DocumentException;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfGState;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.XMLConstants;

public class SVGTileRenderer extends TileRenderer {
    public static final Logger LOGGER = LogManager.getLogger(SVGTileRenderer.class);

    private static Document svgZoomOut = makeSvgZoomOut();

    private static Document makeSvgZoomOut() {
        String svgZoomFileName = "svgZoomOut.xsl";
        try (InputStream stream = SVGTileRenderer.class.getResourceAsStream(svgZoomFileName)) {
            if (stream == null) {
                String path = SVGTileRenderer.class.getResource(".").getPath() + svgZoomFileName;
                throw new RuntimeException("Cannot find the SVG transformation XSLT: expected it to be in: " + path);
            }
            DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
            dbf.setNamespaceAware(true);
            dbf.setValidating(false);
            dbf.setExpandEntityReferences(false);

            DocumentBuilder db = dbf.newDocumentBuilder();
            org.w3c.dom.Document document = db.parse(stream);

            return document;
        } catch (Exception e) {
            throw new RuntimeException("Cannot parse the SVG transformation XSLT", e);
        }
    }

    public void render(final Transformer transformer, List<URI> uris, ParallelMapTileLoader parallelMapTileLoader, final RenderingContext context, final float opacity, int nbTilesHorizontal, double offsetX, double offsetY, long bitmapTileW, long bitmapTileH) throws IOException {
        if (uris.size() != 1) {
            //tiling not supported in SVG
            throw new InvalidValueException("format", "application/x-pdf");
        }

        final URI uri = uris.get(0);

        parallelMapTileLoader.addTileToLoad(new MapTileTask() {
            public PrintTranscoder pt;

            @Override
            protected void readTile() throws IOException, DocumentException {
                LOGGER.debug(uri);
                final TranscoderInput ti = getTranscoderInput(uri.toURL(), transformer, context);
                if (ti != null) {
                    pt = new PrintTranscoder();
                    pt.transcode(ti, null);
                }
            }

            @Override
            protected void renderOnPdf(PdfContentByte dc) throws DocumentException {
                dc.transform(transformer.getSvgTransform());

                if (opacity < 1.0) {
                    PdfGState gs = new PdfGState();
                    gs.setFillOpacity(opacity);
                    gs.setStrokeOpacity(opacity);
                    //gs.setBlendMode(PdfGState.BM_SOFTLIGHT);
                    dc.setGState(gs);
                }
                PdfGraphics2D g2 = new PdfGraphics2D(dc, transformer.getRotatedSvgW(), transformer.getRotatedSvgH());

                //avoid a warning from Batik
                System.setProperty("org.apache.batik.warn_destination", "false");
                g2.setRenderingHint(RenderingHintsKeyExt.KEY_TRANSCODING, RenderingHintsKeyExt.VALUE_TRANSCODING_PRINTING);
                g2.setRenderingHint(RenderingHintsKeyExt.KEY_AVOID_TILE_PAINTING, RenderingHintsKeyExt.VALUE_AVOID_TILE_PAINTING_ON);

                Paper paper = new Paper();
                paper.setSize(transformer.getRotatedSvgW(), transformer.getRotatedSvgH());
                paper.setImageableArea(0, 0, transformer.getRotatedSvgW(), transformer.getRotatedSvgH());
                PageFormat pf = new PageFormat();
                pf.setPaper(paper);
                pt.print(g2, pf, 0);
                g2.dispose();
            }
        });
    }

    private TranscoderInput getTranscoderInput(URL url, Transformer transformer, RenderingContext context) {
        final float zoomFactor = transformer.getSvgFactor() * context.getStyleFactor();
        //final float zoomFactor = context.getStyleFactor();
        if (svgZoomOut != null && zoomFactor != 1.0f) {
            javax.xml.transform.Transformer xslt = null;
            try {
                DOMResult transformedSvg = new DOMResult();
                final TransformerFactory factory = TransformerFactory.newInstance();
                if (svgZoomOut.getTextContent() == null) {
                    svgZoomOut = makeSvgZoomOut(); // a bit of a hack
                }
                xslt = factory.newTransformer(new DOMSource(svgZoomOut));

                // zoomFactory supplied to svgZoomOut.xsl CustomXPath templates to adjust line widths
                xslt.setParameter("zoomFactor", zoomFactor);

                final URLConnection urlConnection = url.openConnection();
                for (Map.Entry<String, String> entry : context.getHeaders().entrySet()) {
                    urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
                }
                final InputStream inputStream = urlConnection.getInputStream();

                Document doc;
                try {
                    DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
                    dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
                    dbf.setValidating(false);
                    dbf.setExpandEntityReferences(false);

                    DocumentBuilder db = dbf.newDocumentBuilder();

                    Document docParse = db.parse(inputStream);
                    xslt.transform(new DOMSource(docParse), transformedSvg);

                    doc = (Document) transformedSvg.getNode();

                    if (LOGGER.isDebugEnabled()) {
                        printDom(doc);
                    }
                } finally {
                    inputStream.close();
                }
                return new TranscoderInput(doc);

            } catch (Exception e) {
                if (xslt == null) {
                    // some more information about the error
                    LOGGER.error("xslt = NULL, zoomFactor = "+
                            zoomFactor +", svgZoomOut = "+ svgZoomOut
                            +"\nsvgZoomOut.getTextContent() = "+ svgZoomOut.getTextContent()
                            +"\nsvgZoomOut.getChildNodes().getLength() = "+ svgZoomOut.getChildNodes().getLength());
                }

                context.addError(e);
                return null;
            }
        } else {
            return new TranscoderInput(url.toString());
        }
    }

    /**
     * Just for debugging XML.
     */
    public static void printDom(Document doc) throws IOException {
        try (StringWriter writer = new StringWriter()) {
            javax.xml.transform.Transformer serializer =
                    TransformerFactory.newInstance().newTransformer();
            serializer.setOutputProperty(OutputKeys.INDENT, "yes");
            serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
            // Xalan-specific indentation amount; ignored by transformers that don't support it.
            serializer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");

            serializer.transform(new DOMSource(doc), new StreamResult(writer));
            LOGGER.trace(writer.toString());
        } catch (Exception e) {
            throw new IOException("Failed to serialize DOM document", e);
        }
    }

}
