package org.mapfish.print.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

import com.codahale.metrics.MetricRegistry;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import org.mapfish.print.ThreadResources;
import org.mapfish.print.map.readers.MapReaderFactoryFinder;
import org.mapfish.print.output.OutputFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Used by MapPrinter to create configuration objects.  Typically injected by spring
 *
 * @author jeichar
 */
public class ConfigFactory {
    @Autowired
    private OutputFactory outputFactoryFinder;
    @Autowired
    private MapReaderFactoryFinder mapReaderFactoryFinder;
    @Autowired
    private ThreadResources threadResources;
    @Autowired
    private MetricRegistry metricRegistry;
    
    private ObjectMapper mapper;


    public ConfigFactory() {
        initMapper();
    }

    public ConfigFactory(ThreadResources threadResources) {
        // this is mainly for testing.  normally a factory should be part of spring configuration.
        initMapper();
        this.threadResources = threadResources;
    }

    private void initMapper() {
        mapper = new ObjectMapper(new YAMLFactory());
        mapper.enable(MapperFeature.ACCEPT_CASE_INSENSITIVE_ENUMS);
    }

    /**
     * Create an instance out of the given file.
     */
    public Config fromYaml(File file) {
        Config result = parseConfigFromFile(file);
        initializeConfig(result);
        result.validate();
        return result;
    }

    private Config parseConfigFromFile(File file) {
        try {
            return mapper.readValue(file, Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Config fromInputStream(InputStream instream) {
        Config result = parseConfigFromStream(instream);
        initializeConfig(result);
        result.validate();
        return result;
    }

    private Config parseConfigFromStream(InputStream instream) {
        try {
            return mapper.readValue(instream, Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Config fromString(String strConfig) {
        Config result = parseConfigFromString(strConfig);
        initializeConfig(result);
        result.validate();
        return result;
    }

    private Config parseConfigFromString(String strConfig) {
        try {
            return mapper.readValue(strConfig, Config.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void initializeConfig(Config result) {
        result.setOutputFactory(outputFactoryFinder);
        result.setMapReaderFactoryFinder(mapReaderFactoryFinder);
        result.setThreadResources(this.threadResources);
        result.setMetricRegistry(this.metricRegistry);
    }
}
