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

package org.mapfish.print.config.layout;

import com.lowagie.text.Chunk;
import com.lowagie.text.Element;
import org.json.JSONObject;
import org.junit.Test;
import org.mapfish.print.FakeHttpd;
import org.mapfish.print.PdfTestCase;
import org.mapfish.print.utils.PJsonObject;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class ImageBlockTest extends PdfTestCase {
    /** default_error.png is 64x64 pixels. */
    private static final String PNG_FILE = "src/main/resources/default_error.png";
    private static final String LOGO_ROUTE = "/logo.png";

    private final List<Element> added = new ArrayList<>();

    @Test
    public void testLayoutFileImage() throws Exception {
        render(new File(PNG_FILE).toURI().toString());

        assertImageChunk();
    }

    @Test
    public void testParamUrl() throws Exception {
        FakeHttpd.HttpAnswerer logo = FakeHttpd.pngAnswerer();
        FakeHttpd server = new FakeHttpd(new FakeHttpd.Route(LOGO_ROUTE, logo));
        server.start();
        try {
            render("${logo}", "http://localhost:" + server.getPort() + LOGO_ROUTE);

            assertEquals(1, logo.getRequestCount());
            assertImageChunk();
        } finally {
            server.shutdown();
        }
    }

    private void render(String url) throws Exception {
        render(url, null);
    }

    /** Renders an image block with {@code url}, and {@code logo} as the "logo" value of the print spec. */
    private void render(String url, String logo) throws Exception {
        ImageBlock block = new ImageBlock();
        block.setUrl(url);
        context.getGlobalParams().getInternalObj().put("logo", logo);
        block.render(new PJsonObject(new JSONObject(), "params"), added::add, context);
    }

    private void assertImageChunk() {
        assertEquals(1, added.size());
        Chunk chunk = (Chunk) added.get(0);
        assertEquals(64, chunk.getImage().getScaledWidth(), 0.001);
        assertEquals(64, chunk.getImage().getScaledHeight(), 0.001);
    }
}
