# Mapfish Print 2 Release notes

## Release 2.2.1

* Update to pdfbox 3.0.0

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