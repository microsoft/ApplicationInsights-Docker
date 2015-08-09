FROM python:3.4

RUN apt-get -y update
RUN apt-get -y install default-jre

# TODO: run a script to install all libraries from requirements.txt
RUN pip install docker-py
RUN pip install statistics
RUN pip install python-dateutil

COPY . /usr/appinsights/docker
WORKDIR /usr/appinsights/docker

# TODO: library version as parameter.
ENTRYPOINT ["java","-cp", "/usr/appinsights/docker/ApplicationInsights-Docker-0.9.jar", "com.microsoft.applicationinsights.AgentBootstrapper"]