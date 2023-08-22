# MapFish Print 2 

This project is no longer actively maintained by [camptocamp](https://www.camptocamp.com/en). New projects are strongly encouragted to use [mapfish-print](https://github.com/mapfish/mapfish-print) which is at Version 3 at the time of writing.

Downstream projects making use of this technology:

- [core-geonetwork](http://github.com/geonetwork/core-geonetwork) - uses mapfish print for activities such as thumbnail generation
- [GeoNode](https://github.com/geonode/) - Uses geosolutions fork described below
- [MapStore](https://github.com/geosolutions-it/MapStore2) - Uses geosolutions fork described below


GeoCat BC has mained this series as needed to support the core-geonetwork project:
- Updated to migrate to OpenPDF

GeoSolutions created a fork of the project in 2013:
- https://github.com/geosolutions-it/mapfish-print
- Some features introduced over time (see [wiki](https://github.com/geosolutions-it/mapfish-print/wiki) )
- Updated to reflect GeoTools changes including Java 11 and Log4j changes

Outdated documentation

- https://www.mapfish.org/doc/index.html

## Build

Execute the following command:
```bash
./gradlew build
```

This will build three artifacts:  ``print-servlet-xxx.war``, ``print-lib.jar``, ``print-standalone.jar``

## Run from commandline

The following command will run the mapfish printer.  If you do no supply any -Dxxx args then all argument options will be listed.

```bash
./gradlew run -Dconfig=samples/config.yaml -Dspec=samples/spec.json -Doutput=/tmp/print-out.pdf
```
## Install

To install SNAPSHOT into local maven repository:

```bash
./gradlew install
```

## Deploy

1. Create ``~/.gradle`` file based on template ``gradle.properties`` provided
2. Use osgeo id credentials to deploy to repo.osgeo.org
   ```
   enablePublishing=true
   osgeoUsername=gtbuild
   osgeoPassword=....
   ```
3. The following command will build and upload all artifacts to the osgeo repository.

   ```bash
   ./gradlew upload 
   ```

## Eclipse

To build in eclipse:

1. Create eclipse project metadata:
   ```bash
   ./gradlew eclipse
   ```
   
2. Import project into eclipse


To run in eclipse:

1. Create new Java Run Configuration
2. Main class is ``org.mapfish.print.ShellMapPrinter``
3. Program arguments: ``--config=samples/config.yaml --spec=samples/spec.json --output=$HOME/print.pdf``