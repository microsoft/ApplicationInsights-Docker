#
# ApplicationInsights-Docker
# Copyright (c) Microsoft Corporation
# All rights reserved.
#
# MIT License
# Permission is hereby granted, free of charge, to any person obtaining a copy of this
# software and associated documentation files (the ""Software""), to deal in the Software
# without restriction, including without limitation the rights to use, copy, modify, merge,
# publish, distribute, sublicense, and/or sell copies of the Software, and to permit
# persons to whom the Software is furnished to do so, subject to the following conditions:
# The above copyright notice and this permission notice shall be included in all copies or
# substantial portions of the Software.
# THE SOFTWARE IS PROVIDED *AS IS*, WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED,
# INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR
# PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE LIABLE
# FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
# OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER
# DEALINGS IN THE SOFTWARE.
#

# The name of this file shouldn't be changed.
# The purpose of this file is to avoid changing the Java code if the python file names will be changed.
# This file will be executed by the com.microsoft.applicationinsights.agent.AgentBootstrapper, and will invoke the script for fetching data from Docker Remote API.
import argparse
import os
from appinsights import program

_docker_socket = 'unix:///docker.sock'
_docker_info_path = '/usr/appinsights/docker/docker.info'
_sdk_info_file = '/usr/appinsights/docker/sdk.info'

parser = argparse.ArgumentParser(description="Application Insights container collector/injector")
parser.add_argument("method", help="The method to run.", choices=['collect', 'inject', 'custom', 'events'])
parser.add_argument("--script", help="The script to run when choosing 'custom' method")
parser.add_argument("--collect-interval", help="The interval (in seconds) in which the performance counters are collected", default=10)

args = parser.parse_args()
method = args.method
script = args.script
collect_interval = args.collect_interval

methods = {'collect': lambda: program.run_collect_performance_counters(docker_socket=_docker_socket, sdk_file=_sdk_info_file, docker_info_file=_docker_info_path, collect_interval=collect_interval),
           'inject': lambda: program.run_injector(docker_socket=_docker_socket, docker_info_path=_docker_info_path),
           'custom': lambda: os.system(script),
           'events': lambda : program.run_collect_containers_events(docker_socket=_docker_socket, docker_info_file=_docker_info_path, sdk_file=_sdk_info_file)}

assert method in methods
methods[method]()
