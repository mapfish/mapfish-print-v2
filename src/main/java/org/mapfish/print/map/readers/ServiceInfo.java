package org.mapfish.print.map.readers;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

/**
 * Represents data loaded from a server that describes the service
 *
 * Created by Jesse on 1/17/14.
 */
public class ServiceInfo {

    protected final static DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();

    // Features to disable for protection against external entity injection
    private final static String[] featuresToDisable = {
            "http://xml.org/sax/features/external-general-entities",
            "http://xml.org/sax/features/external-parameter-entities",
            "http://apache.org/xml/features/nonvalidating/load-external-dtd"
    };

    static {
        for (String feature : featuresToDisable) {
            try {
                documentBuilderFactory.setFeature(feature, false);
            } catch (ParserConfigurationException exception) {
                throw new RuntimeException("Failed to set feature " + feature + " for document object factory");
            }
        }
        documentBuilderFactory.setValidating(false);  //doesn't work?!?!?
    }

}
