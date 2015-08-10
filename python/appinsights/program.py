__author__ = 'galha'

from docker import Client
from appinsights.dockercollector import DockerCollector
from appinsights.dockerwrapper import DockerClientWrapper
from appinsights.dockerinjector import DockerInjector
import time
import sys

def run_injector(docker_socket, docker_info_path):
    docker_client = Client(base_url=docker_socket)
    docker_client.inspect_container
    injector = DockerInjector(
        docker_wrapper=DockerClientWrapper(docker_client=docker_client),
        docker_info_path=docker_info_path)

    while True:
        injector.inject()
        time.sleep(30)

def run_collect_performance_counters(docker_socket, sdk_file):
    docker_client = Client(base_url=docker_socket, timeout=15)
    collector = DockerCollector(
        docker_wrapper=DockerClientWrapper(docker_client=docker_client),
        samples_in_each_metric=5,
        sdk_file=sdk_file)

    while True:
        collector.collect_stats_and_send()
        time.sleep(10)

def run_collect_containers_events(docker_socket):
    docker_client = Client(base_url=docker_socket)
    collector = DockerCollector(
        docker_wrapper=DockerClientWrapper(docker_client=docker_client),
        samples_in_each_metric=5,
        sdk_file=None)
    while True:
        try:
            collector.collect_container_events()
        except Exception as e:
            print(e, file=sys.stderr)