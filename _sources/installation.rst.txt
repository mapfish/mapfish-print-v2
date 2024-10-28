Installation
************

Compilation
-----------

To build this project, you need:

* JDK >= 11, OpenJDK tested
* Download MapFish Print from GitHub

  * On systems with git installed::

      Go to https://github.com/mapfish/mapfish-print-v2
      Click the "Fork" button to fork a version to your user
      Assuming your username is myuser type

      git clone git@github.com:myuser/mapfish-print-v2.git

The other external libs are taken care of by maven.

The build command to use is:

.. code-block:: bash

    cd mapfish-print-v2
    mvn install

For detailed instructions please see [README.md](https://github.com/mapfish/mapfish-print-v2/blob/main/README.md)

Included print-lib in web application
-------------------------------------

If you are developping a web application, you can include the print module in it. This explanation works only if you use Maven to build your project. For others, you'll have to include the print-lib JAR file and all its dependencies.

1. Add the print-lib dependency to your WAR project by adding these two elements to your :file:`pom.xml` file (adjust the version)

   .. literalinclude:: /../pom.xml
      :language: xml
      :prepend: <dependency>
      :start-after: <modelVersion>4.0.0</modelVersion>
      :end-before: <description>
      :append: </dependency>
         
   .. code-block:: xml
   
      <dependency>
        <groupId>org.mapfish.print</groupId>
        <artifactId>print-lib</artifactId>
        <version>%RELEASE%</version>
      </dependency>

2. The jar is available from OSGeo repository:

   .. literalinclude:: ../pom.xml
      :language: xml
      :start-at: <repositories>
      :end-at: </repositories>

3. Add these three sections (correct the paths and URLs in function of your needs) to your :file:`WEB-INF/web.xml` file:
   
   .. code-block:: xml

        ...
        <listener>
          <listener-class>org.springframework.web.context.ContextLoaderListener</listener-class>
        </listener>
        <context-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>classpath:mapfish-spring-application-context.xml,classpath:*-mapfish-spring-application-context-override.xml</param-value>
        </context-param>
        ...
        <servlet>
          <servlet-name>mapfish.print</servlet-name>
          <servlet-class>org.mapfish.print.servlet.MapPrinterServlet</servlet-class>
          <init-param>
            <param-name>config</param-name>
            <param-value>config.yaml</param-value>
          </init-param>
        </servlet>
        <servlet-mapping>
          <servlet-name>mapfish.print</servlet-name>
          <url-pattern>/pdf/*</url-pattern>
        </servlet-mapping>
         ...

4. Add a :file:`config.yaml` file (from the `samples <hhttps://github.com/mapfish/mapfish-print-v2/blob/main/samples>`_, for example) to the root of your web application.

Configuring Servlet Temp Directory
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

By default the default servlet temporary directory will be used but that behaviour can be overridden setting the init-param *tempdir*.  If this parameter is set the servlet must have write access to the directory.

Command line
------------

For debugging or calling from other environments, you can run the print module from the command line.

Examples are assumed to be ran from the directory %MAPFISHDIR%/server/java/print.

If you have previously built you can run the existing jar using the following commands:

.. code-block:: java

   java -Djava.awt.headless=true -cp build/libs/print-standalone-%VERSION%.jar org.mapfish.print.ShellMapPrinter --config=samples/config.yaml --spec=samples/spec.json --output=$HOME/print.pdf

Help about the command line's option can be obtained like that:

.. code-block:: java

  java -Djava.awt.headless=true -cp build/libs/print-standalone-%VERSION%.jar org.mapfish.print.ShellMapPrinter


With Image output line
----------------------

At the moment to have robust image (png, gif, etc...) output options the image magick command line tool must be used.  The tool is cross platform and available on linux, windows and mac osx.  Before installing mapfish print first install both:

 * ImageMagick (specifically ensure that the convert tool has been installed)
 * Ghostscript

Once ImageMagick is installed (and Ghostscript) then the spring configuration needs to be updated so that org.mapfish.print.output.NativeProcessOutputFactory object will be used as an option for creating output files.  This OutputFactory uses native commandline processes for converting a PDF to an alternate format. The mapfish-spring-application-context.xml can be directly edited to configure the OutputFactory or the file https://github.com/mapfish/mapfish-print/blob/master/sample-spring/imagemagick/WEB-INF/classes/imagemagick-spring-application-context-override.xml can be copied into the mapfish-print/WEB-INF/classes folder (if a servlet is being used).

Since the ImageMagick support is a commonly requested configuration there is an IMAGEMAGICK artifact prebuild with the correct configuration.  To use this artifact change your maven dependency from: print-servlet-1.2-SNAPSHOT to print-servlet-1.2-SNAPSHOT-IMG-MAGICK (you will likely have to change the 1.2-SNAPSHOT portion to the version of mapfish that you are using).

By default ImageMagick will try to find the convert tool in /usr/bin/convert.  You will want to find the path to your convert tool and update imagemagick-spring-application-context-override.xml if that file is included in your build, or mapfish-spring-application-context.xml if you manually changed the mapfish-spring-application-context.xml file to include the imagemagick configuration.


Defining temp work directory for Printing module
------------------------------------------------

MAPFISH_PDF_FOLDER : optional environment variable to define the directory path for storing printouts generated by the printing module. When not defined default value is from MapPrinterServlet context
In case of cloud environment a pvc(persistence volume claim) or a shared volume mount can be passed to the variable MAPFISH_PDF_FOLDER and hence the data can be synched between multiple instances of server.
