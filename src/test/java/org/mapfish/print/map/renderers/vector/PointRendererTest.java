/*
 * Copyright (C) 2026  Camptocamp
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

import com.lowagie.text.Image;
import com.lowagie.text.pdf.PdfContentByte;
import org.json.JSONObject;
import org.junit.Test;
import org.locationtech.jts.geom.Coordinate;
import org.locationtech.jts.geom.GeometryFactory;
import org.locationtech.jts.geom.Point;
import org.mapfish.print.FakeHttpd;
import org.mapfish.print.InvalidValueException;
import org.mapfish.print.PdfTestCase;
import org.mapfish.print.utils.PJsonObject;
import org.mockito.ArgumentCaptor;

import java.awt.geom.AffineTransform;
import java.io.File;
import java.nio.file.Files;
import java.util.Base64;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

public class PointRendererTest extends PdfTestCase {
    private static final File GRAPHIC = new File("src/main/resources/default_error.png");
    private static final float GRAPHIC_SIZE = 16;

    private final Point point = new GeometryFactory().createPoint(new Coordinate(0, 0));
    private final PdfContentByte dc = mock(PdfContentByte.class);

    @Test
    public void testExternalGraphicInline() throws Exception {
        String inline = "data:image/png;base64," + Base64.getEncoder().encodeToString(Files.readAllBytes(GRAPHIC.toPath()));

        new PointRenderer().renderImpl(context, dc, style(inline), point, new AffineTransform());

        ArgumentCaptor<Image> image = ArgumentCaptor.forClass(Image.class);
        verify(dc).addImage(image.capture());
        assertEquals(GRAPHIC_SIZE * context.getStyleFactor(), image.getValue().getScaledWidth(), 0.001);
        assertEquals(GRAPHIC_SIZE * context.getStyleFactor(), image.getValue().getScaledHeight(), 0.001);
    }

    @Test
    public void testExternalGraphicOutsideConfiguredHosts() throws Exception {
        FakeHttpd httpd = new FakeHttpd(new FakeHttpd.Route("/graphic.png",
                FakeHttpd.pngAnswerer()));
        httpd.start();
        context.getConfig().setHosts(unrelatedHosts());
        String outside = "http://localhost:" + httpd.getPort() + "/graphic.png";
        try {
            new PointRenderer().renderImpl(context, dc, style(outside), point, new AffineTransform());
            fail("Expected an InvalidValueException");
        } catch (InvalidValueException e) {
            assertEquals("url has an invalid value: " + outside, e.getMessage());
        } finally {
            httpd.shutdown();
        }
        verify(dc, never()).addImage(any(Image.class));
    }

    private static PJsonObject style(String externalGraphic) {
        JSONObject style = new JSONObject();
        style.put("externalGraphic", externalGraphic);
        style.put("graphicWidth", GRAPHIC_SIZE);
        style.put("graphicHeight", GRAPHIC_SIZE);
        return new PJsonObject(style, "style");
    }
}
