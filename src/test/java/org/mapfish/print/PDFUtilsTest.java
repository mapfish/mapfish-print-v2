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

package org.mapfish.print;

import com.lowagie.text.DocumentException;
import com.lowagie.text.Font;
import com.lowagie.text.Image;
import com.sun.net.httpserver.HttpExchange;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.config.Configurator;
import org.json.JSONObject;
import org.junit.Test;
import org.mapfish.print.config.AddressHostMatcher;
import org.mapfish.print.config.Config;
import org.mapfish.print.config.ConfigFactory;
import org.mapfish.print.config.ConfigTest;
import org.mapfish.print.config.HostMatcher;
import org.mapfish.print.utils.PJsonObject;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class PDFUtilsTest extends PdfTestCase {
    private static final String FIVE_HUNDRED_ROUTE = "/500";
    private static final String NOT_IMAGE_ROUTE = "/notImage";
    private static final String SVG_LEGEND_ROUTE = "/legend";
    private static final String TILE_ROUTE = "/tile.png";
    private static final String SVG_LEGEND = """
            <svg xmlns="http://www.w3.org/2000/svg" width="20px" height="10px">
              <rect width="20" height="10" fill="red"/>
            </svg>""";
    /** Batik renders SVG pixels at 96 DPI, PDF units are 1/72 inch. */
    private static final float SVG_PIXEL_TO_PDF = 96f / 72f;
    private FakeHttpd httpd;

    @Override
    public void setUp() throws Exception {
        super.setUp();
        Configurator.setLevel(LogManager.getLogger("org.apache.http"),Level.INFO);
        Configurator.setLevel(LogManager.getLogger("httpclient"), Level.INFO);

        httpd = new FakeHttpd(
                FakeHttpd.Route.errorResponse(FIVE_HUNDRED_ROUTE, 500, "Server error"),
                FakeHttpd.Route.textResponse(NOT_IMAGE_ROUTE, "Blahblah")
                );
        httpd.start();

    }

    @Override
    public void tearDown() throws Exception {
        httpd.shutdown();
        super.tearDown();
    }

    @Test
    public void testGetImageDirectWMSError() throws URISyntaxException, IOException, DocumentException {
        URI uri = new URI("http://localhost:" + httpd.getPort() + NOT_IMAGE_ROUTE);
        try {
            doc.newPage();
            PDFUtils.getImage(context, uri, 0, 0);
            fail("Supposed to have thrown an IOException");
        } catch (IOException ex) {
            //expected
            assertEquals("Didn't receive an image while reading: " + uri, ex.getMessage());
        }
    }

    @Test
    public void testGetImageDirectHTTPError() throws URISyntaxException, IOException, DocumentException {
        URI uri = new URI("http://localhost:" + httpd.getPort() + FIVE_HUNDRED_ROUTE);
        try {
            doc.newPage();
            PDFUtils.getImage(context, uri, 0, 0);
            fail("Supposed to have thrown an IOException");
        } catch (IOException ex) {
            //expected
            assertEquals("Error (status=500) while reading the image from " + uri + ": Internal Server Error", ex.getMessage());
        }
    }

    @Test
    public void testPlaceholder() throws URISyntaxException, IOException, DocumentException {
        URI uri = new URI("http://localhost:" + httpd.getPort() + FIVE_HUNDRED_ROUTE);
        try {
            doc.newPage();
            PDFUtils.getImage(context, uri, 0, 0);
            fail("Supposed to have thrown an IOException");
        } catch (IOException ex) {
            //expected
            assertEquals("Error (status=500) while reading the image from " + uri + ": Internal Server Error", ex.getMessage());
        }
    }

    @Test
    public void testRequestUrlOutsideHosts() throws Exception {
        assertRequestUrlRefused(new URI("http://192.0.2.1/legend.png"));
        assertRequestUrlRefused(new File("src/main/resources/default_error.png").toURI());
    }

    private void assertRequestUrlRefused(URI uri) throws Exception {
        try {
            PDFUtils.getImage(context, uri, 0, 0);
            fail("Supposed to have thrown an IOException");
        } catch (IOException ex) {
            assertEquals("URL not accepted by the configured hosts: " + uri, ex.getMessage());
        }
    }

    @Test
    public void testSvgIconFromServer() throws Exception {
        List<String> languages = new CopyOnWriteArrayList<>();
        httpd.addRoutes(new FakeHttpd.Route(SVG_LEGEND_ROUTE,
                new FakeHttpd.HttpAnswerer(200, "OK", "image/svg+xml", SVG_LEGEND) {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {
                        languages.add(exchange.getRequestHeaders().getFirst("Accept-Language"));
                        super.handle(exchange);
                    }
                }));
        RenderingContext withHeaders = new RenderingContext(doc, context.getWriter(), context.getConfig(),
                context.getGlobalParams(), null, context.getLayout(), Map.of("Accept-Language", "it"));
        String icon = "http://localhost:" + httpd.getPort() + SVG_LEGEND_ROUTE + "?FORMAT=image%2Fsvg%2Bxml";

        Image image = PDFUtils.createImageFromSVG(withHeaders, icon, 100, 100, 1);

        assertEquals(List.of("it"), languages);
        assertEquals(20 * SVG_PIXEL_TO_PDF, image.getScaledWidth(), 0.001);
        assertEquals(10 * SVG_PIXEL_TO_PDF, image.getScaledHeight(), 0.001);
    }

    @Test
    public void testSvgIconHTTPError() throws Exception {
        String icon = "http://localhost:" + httpd.getPort() + FIVE_HUNDRED_ROUTE + "?FORMAT=image%2Fsvg%2Bxml";
        try {
            PDFUtils.createImageFromSVG(context, icon, 100, 100, 1);
            fail("Supposed to have thrown an IOException");
        } catch (IOException ex) {
            assertEquals("Error (status=500) while reading " + icon + ": Internal Server Error",
                    ex.getMessage());
        }
    }

    @Test
    public void testMovedServer() throws Exception {
        FakeHttpd.HttpAnswerer tile = FakeHttpd.pngAnswerer();
        FakeHttpd newServer = moveTiles(tile);
        try {
            setHostPorts(httpd.getPort(), newServer.getPort());

            Image image = PDFUtils.getImage(context, new URI("http://127.0.0.1:" + httpd.getPort() + TILE_ROUTE), 0, 0);

            assertEquals(1, tile.getRequestCount());
            // default_error.png is 64x64 pixels
            assertEquals(64, image.getPlainWidth(), 0.001);
            assertEquals(64, image.getPlainHeight(), 0.001);
        } finally {
            newServer.shutdown();
        }
    }

    @Test
    public void testMovedServerOutsideHosts() throws Exception {
        FakeHttpd.HttpAnswerer tile = FakeHttpd.pngAnswerer();
        FakeHttpd newServer = moveTiles(tile);
        try {
            setHostPorts(httpd.getPort());

            PDFUtils.getImage(context, new URI("http://127.0.0.1:" + httpd.getPort() + TILE_ROUTE), 0, 0);
            fail("Supposed to have thrown an IOException");
        } catch (IOException ex) {
            String location = "http://127.0.0.1:" + newServer.getPort() + TILE_ROUTE;
            assertEquals("URL not accepted by the configured hosts: " + location, ex.getMessage());
        } finally {
            newServer.shutdown();
        }
        assertEquals(0, tile.getRequestCount());
    }

    /** Starts a server answering tile requests with {@code tile}, and moves the test server tiles to it. */
    private FakeHttpd moveTiles(FakeHttpd.HttpAnswerer tile) {
        FakeHttpd newServer = new FakeHttpd(new FakeHttpd.Route(TILE_ROUTE, tile));
        newServer.start();
        String location = "http://127.0.0.1:" + newServer.getPort() + TILE_ROUTE;
        httpd.addRoutes(new FakeHttpd.Route(TILE_ROUTE,
                new FakeHttpd.HttpAnswerer(301, "Moved Permanently", null, (byte[]) null) {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {
                        exchange.getResponseHeaders().add("Location", location);
                        super.handle(exchange);
                    }
                }));
        return newServer;
    }

    private void setHostPorts(int... ports) {
        List<HostMatcher> hosts = new ArrayList<>();
        for (int port : ports) {
            AddressHostMatcher host = new AddressHostMatcher();
            host.setIp("127.0.0.1");
            host.setPort(port);
            hosts.add(host);
        }
        context.getConfig().setHosts(hosts);
    }

    @Test
    public void testRenderString_Scale() throws Exception {
        final File file = ConfigTest.getSampleConfigFiles().get(ConfigTest.GEORCHESTRA_YAML);
        Config config = new ConfigFactory(this.threadResources).fromYaml(file);
        context = new RenderingContext(doc, context.getWriter(), config, context.getGlobalParams(), file.getParent(),
                context.getLayout(), context.getHeaders());
        JSONObject internal = new JSONObject();
        internal.accumulate("scaleLbl", "Scale Label");
        internal.append("bbox", "-10");
        internal.append("bbox", "-10");
        internal.append("bbox", "10");
        internal.append("bbox", "10");
        PJsonObject params = new PJsonObject(internal, "params");
        Font font = new Font();
        context.getLayout().getMainPage().getMap(null).setWidth("300");
        context.getLayout().getMainPage().getMap(null).setHeight("600");
        PDFUtils.renderString(context, params, "${scaleLbl}1:${format %,d scale}", 0, font, null, false);

    }

    @Test
    public void testRenderString_ScaleForMultipleMaps() throws Exception {
        final File file = ConfigTest.getSampleConfigFiles().get("configMultipleMaps.yaml");
        Config config = new ConfigFactory(this.threadResources).fromYaml(file);
        context = new RenderingContext(doc, context.getWriter(), config, context.getGlobalParams(), file.getParent(),
                config.getLayout("A4 portrait"), context.getHeaders());
        JSONObject internal = new JSONObject();



        internal.accumulate("scaleLbl", "Scale Label");

        JSONObject mainMap = new JSONObject();
        mainMap.append("bbox", "-10");
        mainMap.append("bbox", "-10");
        mainMap.append("bbox", "10");
        mainMap.append("bbox", "10");

        JSONObject otherMap = new JSONObject();
        otherMap.append("bbox", "-10000");
        otherMap.append("bbox", "-10000");
        otherMap.append("bbox", "10000");
        otherMap.append("bbox", "10000");

        JSONObject maps = new JSONObject();
        maps.accumulate("main", mainMap);
        maps.accumulate("other", otherMap);

        internal.accumulate("maps", maps);


        PJsonObject params = new PJsonObject(internal, "params");
        Font font = new Font();

        context.getLayout().getMainPage().getMap("main").setWidth("300");
        context.getLayout().getMainPage().getMap("main").setHeight("600");
        context.getLayout().getMainPage().getMap("other").setWidth("300");
        context.getLayout().getMainPage().getMap("other").setHeight("600");

        assertTrue(PDFUtils.renderString(context, params, "${scaleLbl}1:${format %,d scale.main}", 0, font, null, false).getContent().contains("1:25"));
        assertTrue(PDFUtils.renderString(context, params, "${scaleLbl}1:${format %,d scale.other}", 0, font, null, false).getContent().contains("1:200"));
    }

    @Test
    public void testClearMapProvidersPrivateKeys() {
        assertEquals("https://osm/map?layer=xxx&key=*****", PDFUtils.clearMapProvidersPrivateKeys("https://osm/map?layer=xxx&key=privateKey666"));
        assertEquals("https://google/maps?key=*****&layers=myCountry", PDFUtils.clearMapProvidersPrivateKeys("https://google/maps?key=gMapsPrivateKey&layers=myCountry"));
    }
}
