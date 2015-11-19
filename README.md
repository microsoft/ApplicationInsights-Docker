Application Insights for Docker
===============================

Application Insights for Docker helps you monitor your containerized applications by collecting telemetry about the performance and activity of your Docker host, Docker containers and the applications running within them.
The Application Insights container talks to the Docker agent and sends telemetry data back to [Application Insights][appinsights-home], providing you with deep diagnostics and powerful data analysis tools.

# What data will I get for my containerized apps?

## For applications which are not instrumented with an Application Insights SDK:
* Performance counters with Docker context (Docker host, image and container)
* Container events (e.g. start, stop, kill)
* Container error information

## For applications that are instrumented with the [Application Insights SDK for Java][appinsights-java-sdk]:
* All the data mentioned above
* Add the Docker context (Docker host, image and container) to all telemetry data types collected by the Application Insights SDK (exceptions, http requests, events, etc.)

# How to use this Image

1.	Obtain the instrumentation key of your Application Insights resource. If you don’t have one, see [create a new Application Insights resource][appinsights-create-resource].
2.	Run the following command, replacing <app_ikey> with your instrumentation key:
docker run -v /var/run/docker.sock:/docker.sock -d microsoft/applicationinsights ikey=<app_ikey>
3.	If your application is instrumented with the Application Insights SDK for Java, add the following line into the ApplicationInsights.xml file in your project, under the <TelemetryInitializers> element:
<Add type="com.microsoft.applicationinsights.extensibility.initializer.docker.DockerContextInitializer"/>

Note: Only a single container is required per Docker host. If your application is deployed on multiple Docker hosts, then create an instance of this image on every host.

[appinsights-home]: https://azure.microsoft.com/en-us/services/application-insights/
[appinsights-java-sdk]: https://azure.microsoft.com/en-us/documentation/articles/app-insights-java-get-started/
[appinsights-create-resource]: https://azure.microsoft.com/documentation/articles/app-insights-create-new-resource/