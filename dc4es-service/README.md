# DC4Cities Energy Service #

## Configuration management

Configuration files required by the application should be stored outside of the WAR file, so that the configuration can be changed without rebuilding the WAR and the same WAR can be deployed to different environments with different settings.
In order to load the configuration files the application assumes they are available at known locations on the classpath.
Required files are:
* dc4es.properties: contains the dc4es configuration
* log4j.xml: log4j configuration
Example files that can be used as a base for creating the required configuration are provided in the `src/main/conf` directory.

If using the service in replay mode, a CSV file with forecast data must also be provided and its path specified in dc4es.properties. See the files in `dc4es/datasets` for examples.

## How to deploy on Tomcat 7

The application includes a `META-INF/context.xml` file that is automatically recognized by Tomcat 7 and adds the `${catalina.base}/conf/dc4es-service` directory to the classpath. So configuration files must be placed at that location. Usually `catalina.base` is the directory where Tomcat is installed, such as `C:\Program Files\Apache Software Foundation\apache-tomcat-7.0.54` on Windows.

In order to deploy on Tomcat:
* Copy the required configuration files to `${catalina.base}/conf/dc4es-service`
* Copy `dc4es-service.war` to `${catalina.base}/webapps`
* If not already running, start Tomcat
* Check the server log for errors
