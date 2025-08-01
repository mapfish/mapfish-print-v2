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

package org.mapfish.print.map.renderers.vector;

import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.mapfish.print.PDFUtils;
import org.mapfish.print.RenderingContext;
import org.mapfish.print.config.ColorWrapper;
import org.mapfish.print.utils.PJsonObject;

import com.lowagie.text.FontFactory;
import com.lowagie.text.pdf.BaseFont;
import com.lowagie.text.pdf.PdfContentByte;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.Geometry;

import java.awt.geom.AffineTransform;

public class LabelRenderer {

    public static final Logger LOGGER = LogManager.getLogger(LabelRenderer.class);
    private static final String HALO = "halo";
    private static final String STROKE = "stroke";

    static void applyStyle(RenderingContext context, PdfContentByte dc,
            PJsonObject style, Geometry geometry, AffineTransform affineTransform) {
        /*
         * See Feature/Vector.js for more information about labels
         */
        String label = style.optString("label");

        if (label != null && label.length() > 0) {
            /*
             * Valid values for horizontal alignment: "l"=left, "c"=center,
             * "r"=right. Valid values for vertical alignment: "t"=top,
             * "m"=middle, "b"=bottom.
             */
            String labelAlign = style.optString("labelAlign", "cm");
            float labelXOffset = style.optFloat("labelXOffset", (float) 0.0);
            float labelYOffset = style.optFloat("labelYOffset", (float) 0.0);
            float labelRotation = style.optFloat("rotation", (float) 0.0);
            if (labelRotation == 0.0f) {
                labelRotation = style.optFloat("labelRotation", (float) 0.0);
            }
            String labelOutlineMode = style.optString("labelOutlineMode", HALO);

            String fontColor = style.optString("fontColor", "#000000");
            /* Supported itext fonts: COURIER, HELVETICA, TIMES_ROMAN */
            String fontFamily = style.optString("fontFamily", "HELVETICA");
            String font = style.optString("font");
            String fontEncoding = style.optString("fontEncoding");
            if (font != null && !FontFactory.isRegistered(font)) {
                LOGGER.info("Font: '" + font +
                        "' not registered, one of the supported fonts from 'fontFamily' will be used");            	
            }
            else if (!"COURIER".equalsIgnoreCase(fontFamily)
                    && !"HELVETICA".equalsIgnoreCase(fontFamily)
                    && !"TIMES_ROMAN".equalsIgnoreCase(fontFamily)) {

                LOGGER.info("Font family: '" + fontFamily +
                        "' not supported, supported ones are 'HELVETICA', " +
                        "'COURIER', 'TIMES_ROMAN', defaults to 'HELVETICA'");
                fontFamily = "HELVETICA";
            }
            
            String[] labels = label.split("\n");
            String fontSize = style.optString("fontSize", "12");
            String fontWeight = style.optString("fontWeight", "normal");
            Coordinate center = geometry.getCentroid().getCoordinate();
            center = GeometriesRenderer.transformCoordinate(center, affineTransform);
            float f = context.getStyleFactor();
            BaseFont bf = PDFUtils
                    .getBaseFont(font, fontEncoding, fontFamily, fontSize, fontWeight);
            float fontHeight = (float) Double.parseDouble(fontSize
                    .toLowerCase().replaceAll("px", "")) * f;
            dc.setFontAndSize(bf, fontHeight);
            dc.setColorFill(ColorWrapper.convertColor(fontColor));
            String outlineColor = style.optString("labelOutlineColor", null);
            float outlineWidth = style.optFloat("labelOutlineWidth", 1);

            dc.beginText();
            dc.setTextMatrix((float) center.x + labelXOffset * f,
                (float) center.y + labelYOffset * f);
            for (int i = 0; i < labels.length; i++){
                float singleOffset =
                    PDFUtils.getVerticalOffset(labelAlign, fontHeight);
                float offset = singleOffset - ((singleOffset+2)*i);
                float yOffset = (float)Math.cos(labelRotation * Math.PI / 180.0) * offset;
                float xOffset = (float)Math.sin(labelRotation * Math.PI / 180.0) * offset;

                String labelText = labels[i];

                if (outlineColor != null) {
                    writeOutline(dc, outlineWidth, outlineColor, labelAlign, center, labelXOffset, f, xOffset, labelYOffset, yOffset, labelRotation, labelText);
                }
                if (!STROKE.equals(labelOutlineMode)) {
                    writeLabelText(dc, fontColor, labelAlign, labelText, center, labelXOffset, f, xOffset, labelYOffset, yOffset, labelRotation);
                }
            }
            dc.endText();
        }
    }

    private static void writeLabelText(PdfContentByte dc, String fontColor, String labelAlign, String labelText, Coordinate center, float labelXOffset, float f, float xOffset, float labelYOffset, float yOffset, float labelRotation) {
        dc.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
        dc.setColorFill(ColorWrapper.convertColor(fontColor));
        dc.showTextAligned(
            PDFUtils.getHorizontalAlignment(labelAlign),
                labelText,
            (float) center.x + labelXOffset * f + xOffset,
            (float) center.y + labelYOffset * f - yOffset,
                labelRotation);
    }

    private static void writeOutline(PdfContentByte dc, float outlineWidth, String outlineColor, String labelAlign, Coordinate center, float labelXOffset, float f, float xOffset, float labelYOffset, float yOffset, float labelRotation, String labels) {
        dc.setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_STROKE);
        dc.setLineWidth(outlineWidth);
        dc.setColorStroke(ColorWrapper.convertColor(outlineColor));

        String labelText = labels;

        dc.showTextAligned(
                PDFUtils.getHorizontalAlignment(labelAlign),
                labelText,
                (float) center.x + labelXOffset * f + xOffset,
                (float) center.y + labelYOffset * f - yOffset,
                labelRotation
        );
    }
}
