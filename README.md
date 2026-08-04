# MapFish Print V2 2.2.x

This project is no longer actively maintained by [camptocamp](https://www.camptocamp.com/en). New projects are strongly encouragted to use [mapfish-print](https://github.com/mapfish/mapfish-print) which is at Version 3 at the time of writing.

Downstream projects making use of this technology:

- [core-geonetwork](http://github.com/geonetwork/core-geonetwork) - uses mapfish print for activities such as thumbnail generation
- [GeoNode](https://github.com/geonode/) - Uses geosolutions fork described below
- [MapStore](https://github.com/geosolutions-it/MapStore2) - Uses geosolutions fork described below


GeoCat BCV as mained this series as needed to support the core-geonetwork project:
- 2.2.x: Migrate to OpenPDF

GeoSolutions created a fork of the project in 2013:
- https://github.com/geosolutions-it/mapfish-print
- Some features introduced over time (see [wiki](https://github.com/geosolutions-it/mapfish-print/wiki) )
- Updated to reflect GeoTools changes including Java 11 and Log4j changes

Outdated documentation:

- https://www.mapfish.org/doc/index.html

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

## Release

To create a release:

1. Update version in ``pom.xml``:
  
   ```xml
   <groupId>org.mapfish.print</groupId>
   <artifactId>print-lib</artifactId>
   <version>2.2.1</version>
   ```

2. Double check `ReleaseNotes.md` change-log and update if ndded.

   Double check the `docs/upgrade.rst` and update if needed.

3. Build docs:
   
   ```bash
   source venv/bin/activate
   pip install -r docs/requirements.txt
   sphinx-build -b html -d docs/_build/doctrees docs docs/_build/html
   ```
   
3. Build confirming creation of ``print-lib-2.2.1.jar``

   ```bash
   mvn clean install
   ```

3. Commit the change to ``pom.xml``

   ```bash
   git add pom.xml
   git commit -m "Release 2.2.1"
   ```

4. Deploy to osgeo nexus

   ```bash
   mvn deploy -DskipTests
   ```

5. Push and tag the change:
   
   ```bash
   git push
   git tag -a release/2.3.5 -m "Release 2.2.1"
   git push origin release/2.2.1
   ```

6. Check the release in github:
   
   * https://github.com/mapfish/mapfish-print-v2/tags
   
7. Add any release-notes to the tag in GitHub.

   Upload jar and docs bundles from target folder.

9. Update the ``pom.xml`` against to return to SNAPSHOT developmentt:
   
   ```xml
   <groupId>org.mapfish.print</groupId>
   <artifactId>print-lib</artifactId>
   <version>2.2-SNAPSHOT</version>
   ```
   
   And push up the change:
   ```bash
   git add pom.xml
   git commit -m "Development 2.2-SNAPSHOT"
   git push
   ```