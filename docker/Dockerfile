FROM java:8u66

RUN apt-get -y -qq update
RUN apt-get -y -qq remove python
RUN apt-get -y -qq autoremove
RUN apt-get -y -qq install python3.4
RUN ln -s /usr/bin/python3.4 /usr/bin/python

# TODO: run a script to install all libraries from requirements.txt
RUN apt-get -y -qq install python3-pip
RUN python -m pip install python-dateutil==2.4.2

# docker-py is dependent on the 'requests' module which currently has a bug. Therefore, the docker-py
# must be installed last otherwise no other modules can be installed.
RUN python -m pip install docker-py==1.3.1

COPY . /usr/appinsights/docker
WORKDIR /usr/appinsights/docker

# TODO: library version as parameter.
ENTRYPOINT ["java","-cp", "/usr/appinsights/docker/ApplicationInsights-Docker-0.9.jar", "com.microsoft.applicationinsights.AgentBootstrapper"]