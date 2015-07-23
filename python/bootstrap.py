# The name of this file shouldn't be changed.
# The purpose of this file is to avoid changing the Java code if the python file names will be changed.
# This file will be executed by the com.microsoft.applicationinsights.agent.DockerAgent, and will invoke the script for fetching data from Docker Remote API.
from appinsights import program

program.run(docker_socket='unix:///docker.sock')