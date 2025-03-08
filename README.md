# MapFish Print V2 

This project is no longer actively maintained by [camptocamp](https://www.camptocamp.com/en). New projects are strongly encouraged to use [mapfish-print](https://github.com/mapfish/mapfish-print) which is at Version 3 at the time of writing.

This project is no longer fully functional, consisting of ``mapfish-print-lib.jar`` distribution used
by downstream projects as described below.

[MapFish Print 2.3 documentation](https://mapfish.github.io/mapfish-print-v2/) 

## Background

Downstream projects making use of this technology:

- [geoserver](https://docs.geoserver.org/latest/en/user/extensions/printing/index.html) - Uses ``mapfish-print-lib`` for gs-printing extension
- [core-geonetwork](http://github.com/geonetwork/core-geonetwork) - uses mapfish-print-lib for activities such as thumbnail generation
- [GeoNode](https://github.com/geonode/) - ``mapfish-print-lib`` via geoserver printing extension
- [MapStore](https://github.com/geosolutions-it/MapStore2) - ``mapfish-print-lib`` via geoserver printing extension

GeoCat BV has mained this series as needed to support the core-geonetwork project:

- Updated to migrate to OpenPDF

GeoSolutions created a fork of the project in 2013:

- https://github.com/geosolutions-it/mapfish-print
- Some features introduced over time (see [wiki](https://github.com/geosolutions-it/mapfish-print/wiki) )
- Updated to reflect GeoTools changes including Java 11 and Log4j changes
- This work has been incorporated back into `mapfish-print-lib` for 2.3.0 release
- With ``GeoNode`` and ``MapStore`` successfully migrating to ``mapfish-print-lib`` this fork has concluded.

Outdated documentation:

- http://www.mapfish.org/doc/print/index.html

## Maven Build

Standard maven build targets are available:

1. To clean the ``target/`` folder:

   ```bash
   mvn clean
   ```

2. To compile:

   ```bash
   mvn compile
   ```

3. To create a ``print-lib-2.x-SNAPSHOT.jar`` jar:

   ```bash
   mvn package
   ```

4. To install SNAPSHOT jar into ``~/.m2/repository`` local maven repository:
  
   ```bash
   mvn install
   ```
  
   The use of a local maven repository allows for integration testing with other builds.

## IDE Build

To build in IntelliJ:

1. Open as a maven project.

To build in Eclipse:

1. Open as maven project.

To build in Eclipse as a Java project:

1. Create eclipse project ``.classpath`` and ``.project`` files:
   ```bash
   mvn eclipse:eclipse
   ```
   
2. Import project into Eclipse as a Java project.

When running in an IDE:

1. Main class is ``org.mapfish.print.ShellMapPrinter``

2. Program arguments: ``--config=samples/config.yaml --spec=samples/spec.json --output=$HOME/print.pdf``

## Deploy

To deploy SNAPSHOT to repo.osgeo.org:

```bash
mvn deploy
```

Your `~/.m2/settings.xml` requires credentials to access osgeo ``nexus`` server at repo.osgeo.org.
See https://wiki.osgeo.org/wiki/SAC:Repo to obtain credentials:

```xml
  <servers>
    <server>
      <username>OSGEO_ID</username>
      <password>OSGEO_PASSWORD</password>
      <id>nexus</id>
    </server>
  </servers>
```
## Docs

Uses Python3 environment for **sphinx-build** documentation:

```
virtualenv venv
source venv/bin/activate
pip install -r docs/requirements.txt
sphinx-build -b html -d docs/_build/doctrees docs docs/_build/html
open docs/_build/html/index.html 
```

Docs are created in ``docs/_build/html`` folder.

Maven `docs` profile will assemble the docs into a zip bundle if ``docs/_build/html/index.html`` exists.

```
mvn package
```

## Release

To create a release:

1. Update version in ``pom.xml``:
  
   ```xml
   <groupId>org.mapfish.print</groupId>
   <artifactId>print-lib</artifactId>
   <version>2.3.2</version>
   ```

2. Double check `ReleaseNotes.md` change-log and update if ndded.

3. Build docs:
   
   ```bash
   sphinx-build -b html -d docs/_build/doctrees docs docs/_build/html
   ```
   
3. Build confirming creation of ``print-lib-2.3.0.jar``

   ```bash
   mvn clean install
   ```

3. Commit the change to ``pom.xml``

   ```bash
   git add pom.xml
   git commit -m "Release 2.3.2"
   ```

4. Deploy to osgeo nexus

   ```bash
   mvn deploy -DskipTests
   ```

5. Push and tag the change:
   
   ```bash
   git push
   git tag -a release/2.3.2 -m "Release 2.3.2"
   git push origin release/2.3.0
   ```

6. Check the release in github:
   
   * https://github.com/mapfish/mapfish-print-v2/tags
   
7. Add any release-notes to the tag in GitHub.

   Upload jar and docs bundles from target folder.

9. Update the ``pom.xml`` against to return to SNAPSHOT developmentt:
   
   ```xml
   <groupId>org.mapfish.print</groupId>
   <artifactId>print-lib</artifactId>
   <version>2.3-SNAPSHOT</version>
   ```
   
   And push up the change:
   ```bash
   git add pom.xml
   git commit -m "Development 2.3-SNAPSHOT"
   git push
   ```