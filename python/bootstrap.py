# The name of this file shouldn't be changed.
# The purpose of this file is to avoid changing the Java code if the python file names will be changed.
# This file will be executed by the com.microsoft.applicationinsights.agent.AgentBootstrapper, and will invoke the script for fetching data from Docker Remote API.
import argparse
import os
from appinsights import program

_docker_socket = 'unix:///docker.sock'
_docker_info_path = '/usr/appinsights/docker/docker.info'

parser = argparse.ArgumentParser(description="Application Insights container collector/injector")
parser.add_argument("method", help="The method to run 'collect' or 'inject'")
parser.add_argument("script", help="The script to run when choosing custom method")

args = parser.parse_args()
method = args.method
script = args.script

methods = {'collect': lambda: program.run_collector(docker_socket=_docker_socket),
           'inject': lambda: program.run_injector(docker_socket=_docker_socket, docker_info_path=_docker_info_path),
           'custom': lambda: os.system(script)}

assert method in methods
methods[method]()