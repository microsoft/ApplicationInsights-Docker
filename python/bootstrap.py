# The name of this file shouldn't be changed.
# The purpose of this file is to avoid changing the Java code if the python file names will be changed.
# This file will be executed by the com.microsoft.applicationinsights.agent.AgentBootstrapper, and will invoke the script for fetching data from Docker Remote API.
import argparse
from appinsights import program

_docker_socket = 'unix:///docker.sock'
_docker_info_path = '/usr/appinsights/docker/docker.info'


methods = {'collect': lambda: program.run_collector(docker_socket=_docker_socket),
           'inject': lambda: program.run_injector(docker_socket=_docker_socket, docker_info_path=_docker_info_path)}

parser = argparse.ArgumentParser(description="Application Insights container collector/injector")
parser.add_argument("method", help="The method to run 'collect' or 'inject'")
args = parser.parse_args()
method = args.method

assert method in methods
methods[method]()
