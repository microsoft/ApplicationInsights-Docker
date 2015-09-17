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

__author__ = 'galha'

from appinsights.dockercollector import DockerCollector
from appinsights.dockerwrapper import get_production_docker_wrapper
from appinsights.dockerinjector import DockerInjector
import time
import sys

def run_injector(docker_socket, docker_info_path):
    injector = DockerInjector(
        get_production_docker_wrapper(base_url=docker_socket),
        docker_info_path=docker_info_path)

    while True:
        injector.start()
        time.sleep(30)

def run_collect_performance_counters(docker_socket, sdk_file, docker_info_file, collect_interval):
    docker_wrapper=get_production_docker_wrapper(base_url=docker_socket)
    docker_injector  = DockerInjector(docker_wrapper=docker_wrapper, docker_info_path=docker_info_file)
    collector = DockerCollector(
        docker_wrapper=docker_wrapper,
        docker_injector=docker_injector,
        samples_in_each_metric=5,
        sdk_file=sdk_file)

    while True:
        collector.collect_stats_and_send()
        time.sleep(float(collect_interval))

def run_collect_containers_events(docker_socket, docker_info_file, sdk_file):
    docker_wrapper=get_production_docker_wrapper(base_url=docker_socket)
    docker_injector  = DockerInjector(docker_wrapper=docker_wrapper, docker_info_path=docker_info_file)
    collector = DockerCollector(
        docker_wrapper=docker_wrapper,
        docker_injector=docker_injector,
        samples_in_each_metric=5,
        sdk_file=sdk_file)
    while True:
        try:
            collector.collect_container_events()
        except Exception as e:
            print(e, file=sys.stderr)
            time.sleep(10)