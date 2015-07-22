FROM python:3.4

RUN apt-get -y update
RUN apt-get -y install default-jre

COPY . /usr/docker
WORKDIR /usr/docker

// TODO: library version as parameter.
CMD ["java","-cp", "/usr/docker/ApplicationInsights-Docker-0.9.jar", "com.microsoft.applicationinsights.agent.DockerAgent"]