# The name of this file shouldn't be changed.
# The purpose of this file is to avoid changing the Java code if the python file names will be changed.
# This file will be executed by the com.microsoft.applicationinsights.agent.AgentBootstrapper, and will invoke the script for fetching data from Docker Remote API.

import sys
from appinsights import program

if sys.argv != 1:
    print("Process type must be provided: container_context or metric_collection")
    exit(1)

if sys.argv[0] == 'metric_collection':
    program.run(docker_socket='unix:///docker.sock')
else:
    # TODO: Call to container context after merge will Gal.
    print("Call to container_context")