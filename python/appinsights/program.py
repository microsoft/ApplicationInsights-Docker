__author__ = 'galha'

from docker import Client
from appinsights.dockercollector import DockerCollector
from appinsights.dockerwrapper import DockerClientWrapper
from appinsights.dockerinjector import DockerInjector
import time

def run_injector(docker_socket, docker_info_path):
    docker_client = Client(base_url=docker_socket)
    injector = DockerInjector(
        docker_wrapper=DockerClientWrapper(docker_client=docker_client),
        docker_info_path=docker_info_path)

    while True:
        injects = injector.inject()
        time.sleep(30)

def run_collector(docker_socket):
    docker_client = Client(base_url=docker_socket)
    collector = DockerCollector(
        docker_wrapper=DockerClientWrapper(docker_client=docker_client),
        send_event=print,
        samples_in_each_metric=5)

    while True:
        collector.collect_and_send()
        time.sleep(10)
