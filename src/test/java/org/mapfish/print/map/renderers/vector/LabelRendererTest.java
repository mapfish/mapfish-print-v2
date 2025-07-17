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

import com.lowagie.text.pdf.PdfContentByte;
import org.json.JSONObject;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.geom.PrecisionModel;
import org.mapfish.print.RenderingContext;
import org.mapfish.print.utils.PJsonObject;
import org.mockito.Mockito;

import java.awt.geom.AffineTransform;
import java.io.FileNotFoundException;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class LabelRendererTest {

    @Test
    public void testOutlineIsBeingSet() {
        final PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING);
        final GeometryFactory jtsFactory = new GeometryFactory(pm, 4326);

        Point point = jtsFactory.createPoint(new Coordinate(new Coordinate(0, 1)));

        final PdfContentByte dc = Mockito.mock(PdfContentByte.class);
        JSONObject jsonObject = new JSONObject("{\n" +
                "                        \"fontSize\": 14,\n" +
                "                        \"fontFamily\": \"HELVETICA\",\n" +
                "                        \"fontWeight\": \"normal\",\n" +
                "                        \"labelAlign\": \"cb\",\n" +
                "                        \"labelXOffset\": 0,\n" +
                "                        \"labelYOffset\": 0,\n" +
                "                        \"rotation\": 0,\n" +
                "                        \"fontColor\": \"#333333\",\n" +
                "                        \"fontOpacity\": 1,\n" +
                "                        \"label\": \"Overlapping label\",\n" +
                "                        \"fillOpacity\": 0,\n" +
                "                        \"pointRadius\": 0,\n" +
                "                        \"strokeOpacity\": 0,\n" +
                "                        \"strokeWidth\": 0,\n" +
                "                        \"labelOutlineColor\":\"#ffffff\"," +
                "                        \"labelOutlineOpacity\":1," +
                "                        \"labelOutlineWidth\":3" +
                "                    }");

        RenderingContext context = Mockito.mock(RenderingContext.class);
        LabelRenderer.applyStyle(context, dc, new PJsonObject(jsonObject, "style"), point, AffineTransform.getTranslateInstance(0, 0));

        //Checking that outline rendering methods were called.
        verify(dc, times(1)).setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_STROKE);
        verify(dc, times(1)).setLineWidth(3);
        verify(dc, times(1)).setColorStroke(any());
    }

    @Test
    public void testOutlineWithStrokeMode() {
        final PrecisionModel pm = new PrecisionModel(PrecisionModel.FLOATING);
        final GeometryFactory jtsFactory = new GeometryFactory(pm, 4326);

        Point point = jtsFactory.createPoint(new Coordinate(new Coordinate(0, 1)));

        final PdfContentByte dc = Mockito.mock(PdfContentByte.class);
        JSONObject jsonObject = new JSONObject("{\n" +
                "                        \"fontSize\": 14,\n" +
                "                        \"fontFamily\": \"HELVETICA\",\n" +
                "                        \"fontWeight\": \"normal\",\n" +
                "                        \"labelAlign\": \"cb\",\n" +
                "                        \"labelXOffset\": 0,\n" +
                "                        \"labelYOffset\": 0,\n" +
                "                        \"rotation\": 0,\n" +
                "                        \"fontColor\": \"#333333\",\n" +
                "                        \"fontOpacity\": 1,\n" +
                "                        \"label\": \"Overlapping label\",\n" +
                "                        \"labelOutlineMode\": \"stroke\",\n" +
                "                        \"fillOpacity\": 0,\n" +
                "                        \"pointRadius\": 0,\n" +
                "                        \"strokeOpacity\": 0,\n" +
                "                        \"strokeWidth\": 0,\n" +
                "                        \"labelOutlineColor\":\"#ffffff\"," +
                "                        \"labelOutlineOpacity\":1," +
                "                        \"labelOutlineWidth\":3" +
                "                    }");

        RenderingContext context = Mockito.mock(RenderingContext.class);
        LabelRenderer.applyStyle(context, dc, new PJsonObject(jsonObject, "style"), point, AffineTransform.getTranslateInstance(0, 0));

        //Checking that outline rendering methods were called.
        verify(dc, times(1)).setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_STROKE);
        verify(dc, times(1)).setLineWidth(3);
        verify(dc, times(1)).setColorStroke(any());

        verify(dc, times(0)).setTextRenderingMode(PdfContentByte.TEXT_RENDER_MODE_FILL);
    }
}
