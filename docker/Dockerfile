FROM java:8u66

WORKDIR /usr/docker
RUN apt-get -y -qq update \
	&& apt-get -y -qq remove python \
	&& apt-get -y -qq autoremove \
	&& apt-get -y -qq install python3.4 \
	&& ln -s /usr/bin/python3.4 /usr/bin/python \

	# TODO: run a script to install all libraries from requirements.txt
	&& apt-get -y -qq install python3-pip \
	&& python -m pip install python-dateutil==2.4.2 \

	# docker-py is dependent on the 'requests' module which currently has a bug. Therefore, the docker-py
	# must be installed last otherwise no other modules can be installed.
	&& python -m pip install docker-py==1.3.1 \
	
	# Download and build sources
	&& export TERM=${TERM:-dumb} \
	&& git clone https://github.com/Microsoft/ApplicationInsights-Docker \
	&& cd /usr/docker/ApplicationInsights-Docker \
	&& chmod +x ./gradlew \
	&& ./gradlew shadow
	
WORKDIR /usr/docker/ApplicationInsights-Docker/build/docker/
ENTRYPOINT ["java","-cp", "/usr/docker/ApplicationInsights-Docker/build/docker/ApplicationInsights-Docker-0.9.jar", "com.microsoft.applicationinsights.AgentBootstrapper"]