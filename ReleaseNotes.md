# Mapfish Print 2 Release notes

## Release 2.3.0

Release notes:

* Update to GeoTools 30.0 which requires Java 11
* Changed to maven build for Java 11 compatibility 
* Massive influx of new functionality from GeoSolutions
  * Max number of columns configuration for multi column legends
  * Simple colored box icon in legends
  * Explicit support of Geoserver CQL_FILTER parameter (also with layers merge support)
  * Legend fitting
  * Don't break legend items
  * Reorder legends block in columns
  * Images content
  * Dynamic images page
  * Multipage legends
  * Custom intervals in ScalebarBlock
  * Clustering Support
  * HTML rendering in text blocks
  * Extra Pages
  * Group Rendering in attribute blocks
  * Skip rendering of pages
  * Automatic X-Forwarded-For
  * Parsing of Base64 encoded images
* Release is focused on ``print-lib`` required for MapStore, GeoNode and GeoServer projects
* Deploy to OSGeo repository
* Updated docs to reflect new functionality
* Added user-doc bundle and publication to *


## Release 2.2.0

* Change from iText to OpenPDF library
* Update to PDFBox 
* Upgrade to Gradle 3.0 for compatibility with IntelliJ IDE
* Update to GeoTools 27.0
* Upgrade test environment to GeoServer 2.21.0
* Deploy to OSGeo repository

## Release 2.1.6

* Upgrading to PDFBox 2.0.7

## Release 2.1.5

* Legend consistency, reimplementation
 * [Legends Print on new line - and other issues](https://github.com/mapfish/mapfish-print/issues/33)
 * [Legends Block Improvements](https://github.com/mapfish/mapfish-print/pull/40)
* Map readers and output formats now plugins.
 * Plugins configured by spring configuration files:
 * src/main/resources/mapfish-spring-application-context.xml
* A war specifically built to use ImageMagik for converting PDF to images is built and published along with the normal artifacts