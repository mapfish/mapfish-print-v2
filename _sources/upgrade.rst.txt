Upgrade
*******

Version 2.3.4
-------------

Release notes:

* Add an `labelOutlineMode` parameter

Version 2.3.3
-------------

Release notes:

* Fix for an outline parameters not being properly applied while rendering labels

Version 2.3.2
-------------

* Support for printing in a cloud environment and in an environment without sticky sessions : generates a requested printout metadata json file and stores it in the tempDir. With this information persisted, there is no need for sticky sessions and printing module can be used in a cloud environment with shared volume.

In case of cloud environment a pvc (persistence volume claim) or a shared volume mount can be used to sync this data between multiple instances of server, where as in case of non-cloud environment the tempDir can be shared between muiltiple instances running on the same machine. This path can be passed by setting the environment variable MAPFISH_PDF_FOLDER.


Version 2.3.1
-------------

Release notes:

* Fix for external entity injection (XXE)
* Bump org.jyaml.jyaml 1.3 to com.fasterxml.jackson.dataformat.jackson-dataformat-yaml 2.17.2
* Support custom pdf layer name (sent in request JSON parameter "pdfLayerName")
* Upgrade to GeoTools 31.3
* Bump Xalan from 2.7.0 to 2.7.3
* Update batik version to 1.17

Breaking Changes
================

* Deprecated `org.jyaml` in favour of `com.fasterxml.jackson.dataformat`

Migration Guide
===============

Update the `config.yaml` according to the following rules:

* Make sure the properties have a space between the name and value, e.g. `mask:0.0.0.0` 👎  - `mask: 0.0.0.0` 👍 
* Remove `@` from references, e.g. `username: @shared.privileged.geoserver.user@` 👎 - `username: shared.privileged.geoserver.user` 👍 
* Remove any reference to common properties or inherited sections, e.g. `footer: *commonFooter` 👎 

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
