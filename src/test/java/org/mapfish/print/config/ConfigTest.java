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

package org.mapfish.print.config;

import org.json.JSONWriter;
import org.junit.Test;
import org.mapfish.print.PrintTestCase;
import org.mapfish.print.ShellMapPrinter;
import org.mapfish.print.ThreadResources;
import org.mapfish.print.UrlSource;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

public class ConfigTest extends PrintTestCase {

    public static final String GEORCHESTRA_YAML = "config-georchestra.yaml";

    @Test
    public void testParse() throws FileNotFoundException {
        new ClassPathXmlApplicationContext(ShellMapPrinter.DEFAULT_SPRING_CONTEXT).getBean(ConfigFactory.class).fromYaml(new File("samples/config.yaml"));
    }

    @Test
    public void testBestScale() {
        Config config = new Config();
        try {
            TreeSet<Number> scales = new TreeSet<Number>(Arrays.asList(200000.0, 25000.0, 50000.0, 100000.0));
            config.setScales(scales);

            assertEquals("Too small scale => pick the smallest available", 25000, config.getBestScale(1), 0.000001);
            assertEquals("Exact match", 25000, config.getBestScale(25000.0), 0.000001);
            assertEquals("Just too big => should still take the previous one", 25000, config.getBestScale(25000.1), 0.000001);
            assertEquals("Normal behaviour", 200000, config.getBestScale(150000), 0.000001);
            assertEquals("Just a litle before", 200000, config.getBestScale(199999.9), 0.000001);
            assertEquals("When we want a scale that is too big, pick the highest available", 200000, config.getBestScale(99999999999.0), 0.000001);
        } finally {
            config.close();
        }
    }

    @Test
    public void testReadExampleConfigFiles() throws Exception {
        Map<String, File> filesInSamplesDir = getSampleConfigFiles();
        StringBuilder errorString = new StringBuilder("The following errors occurred while parsing yaml config files: \n");
        boolean error = false;
        ThreadResources threadResources = new ThreadResources();
        threadResources.init();
        try {
            final ConfigFactory configFactory = new ConfigFactory(threadResources);
            for (File file : filesInSamplesDir.values()) {
                if (file.getName().endsWith(".yaml")) {
                    try {
                        final Config config = configFactory.fromYaml(file);
                        config.getBestScale(1000);
                        JSONWriter json = new JSONWriter(new StringWriter());
                        json.object();
                        config.printClientConfig(json);

                        if (!config.getDpis().isEmpty()) {
                            final Integer next = config.getDpis().iterator().next();
                            assertNotNull(next);
                        }
                    } catch (Throwable e) {
                        error = true;
                        StringWriter trace = new StringWriter();
                        e.printStackTrace(new PrintWriter(trace));
                        errorString.append("\t" + file.getPath() + ": " + e.getMessage() + "\n");
                        errorString.append(trace.toString().replace("\n", "\n\t\t") + "\n\n");
                    }
                }
            }

            assertFalse(errorString.toString(), error);
        } finally {
            threadResources.destroy();
        }

    }

    @Test
    public void testLargeConfigFile() throws Exception {
        File configFile = getConfigLarge();
        StringBuilder errorString = new StringBuilder("The following errors occurred while parsing yaml config file: \n");
        boolean error = false;
        ThreadResources threadResources = new ThreadResources();
        threadResources.init();
        try {
            final ConfigFactory configFactory = new ConfigFactory(threadResources);
            try {
                final Config config = configFactory.fromYaml(configFile);
                config.getBestScale(1000);
                JSONWriter json = new JSONWriter(new StringWriter());
                json.object();
                config.printClientConfig(json);

                if (!config.getDpis().isEmpty()) {
                    final Integer next = config.getDpis().iterator().next();
                    assertNotNull(next);
                }
            } catch (Throwable e) {
                error = true;
                StringWriter trace = new StringWriter();
                e.printStackTrace(new PrintWriter(trace));
                errorString.append("\t" + configFile.getPath() + ": " + e.getMessage() + "\n");
                errorString.append(trace.toString().replace("\n", "\n\t\t") + "\n\n");
            }
            assertFalse(errorString.toString(), error);
        } finally {
            threadResources.destroy();
        }

    }



    @Test
    public void testValidateUri() throws Exception {
        // the default hosts list accepts the local network interfaces
        Config config = new Config();
        assertTrue(config.validateUri(new URI("http://localhost/wms")));
        assertTrue(config.validateUri(new URI("https://localhost/wms")));
        assertFalse(config.validateUri(new URI("http://192.0.2.1/wms")));
        assertFalse(config.validateUri(new URI("ftp://localhost/tiles/0/0/0.png")));
    }

    @Test
    public void testCheckUri() throws Exception {
        Config config = new Config();
        config.setHosts(PrintTestCase.unrelatedHosts()); // accepts only 192.0.2.1
        URI outside = new URI("http://localhost/legend.png");

        // a request URL outside the configured hosts is refused
        try {
            config.checkUri(outside, UrlSource.REQUEST);
            fail("expected an IOException");
        } catch (IOException e) {
            assertEquals("URL not accepted by the configured hosts: " + outside, e.getMessage());
        }

        // a configured URL is trusted, a data URL and an allowed host pass
        config.checkUri(outside, UrlSource.CONFIGURED);
        config.checkUri(new URI("data:image/png;base64,AAAA"), UrlSource.REQUEST);
        config.checkUri(new URI("http://192.0.2.1/legend.png"), UrlSource.REQUEST);
    }

    public static Map<String, File> getSampleConfigFiles() {
        final String configTestClassFile = ConfigTest.class.getResource(ConfigTest.class.getSimpleName() + ".class").getFile();
        final File[] sample_config_yamls = new File(new File(configTestClassFile).getParentFile(), "sample_config_yaml").listFiles();
        Map<String, File> nameToFileMap = new HashMap<String, File>();

        for (File file : sample_config_yamls) {
            nameToFileMap.put(file.getName(), file);
        }
        return nameToFileMap;
    }

    public static File getConfigLarge() {
        final String configTestClassFile = ConfigTest.class.getResource(ConfigTest.class.getSimpleName() + ".class").getFile();
        return new File(new File(configTestClassFile).getParentFile(), "sample_config_yaml" + File.separator + "specific_samples" + File.separator + "configLarge.yaml");
    }
}
