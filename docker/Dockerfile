FROM python:3.4

RUN apt-get -y update
RUN apt-get -y install default-jre
RUN pip install docker-py
RUN pip install statistics

COPY . /usr/docker
WORKDIR /usr/docker

# TODO: library version as parameter.
ENTRYPOINT ["java","-cp", "/usr/docker/ApplicationInsights-Docker-0.9.jar", "com.microsoft.applicationinsights.agent.AgentBootstrapper"]