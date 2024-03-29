Upgrade
*******

Version 2.3.0
-------------

Release notes:

* Change from gradle to maven build system
* Integration tests removed as they were not maintained, we will trust downstream applications GeoServer and GeoNode to provide integration testing
* Upgrade to GeoTools 30.x series: Java 11 is now required, some package names have changed from `org.opengis` to `org.geotools.api`

Functionality from geosolutions `mapfish-print 2.3-SNAPSHOT <https://github.com/mapfish/mapfish-print-v2>`__ incorporated to foster collaboration:

* LabelRenderer supports `rotation` and if this is 0 it checks `labelRotation`
* LabelRenderer supports `labelOutlineColor` and `labelOutlineWidth`
* LegendRenderer has considerable new functionality 
* LineStringRenderer supports dash-array


Developers upgrading from geosolutions mapfish-print 2.3-SNAPSHOT are advised:

* Please check 2.2.0 upgrade notes for api changes including `com.itextpdf.text.BaseColor` changes to `java.awt.Color`.
* LabelRenderer supports multi-line labels

Version 2.2.0
-------------

Release notes:

* Change from iText to OpenPDF library
* Update to PDFBox
* Upgrade to Gradle 3.0 for compatibility with IntelliJ IDE
* Update to GeoTools 27.0
* Upgrade test environment to GeoServer 2.21.0
* Deploy to OSGeo repository

Developers using mapfish-print-v2 as a library are adivsed:

* packages `com.itextpdf.text` change to `com.lowagie.text`
* `com.itextpdf.text.BaseColor` changes to `java.awt.Color`