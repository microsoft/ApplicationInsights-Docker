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

def run_collect_performance_counters(docker_socket, sdk_file, docker_info_file):
    docker_wrapper=get_production_docker_wrapper(base_url=docker_socket)
    docker_injector  = DockerInjector(docker_wrapper=docker_wrapper, docker_info_path=docker_info_file)
    collector = DockerCollector(
        docker_wrapper=docker_wrapper,
        docker_injector=docker_injector,
        samples_in_each_metric=5,
        sdk_file=sdk_file)

    while True:
        collector.collect_stats_and_send()
        time.sleep(10)

def run_collect_containers_events(docker_socket, docker_info_file):
    docker_wrapper=get_production_docker_wrapper(base_url=docker_socket)
    docker_injector  = DockerInjector(docker_wrapper=docker_wrapper, docker_info_path=docker_info_file)
    collector = DockerCollector(
        docker_wrapper=docker_wrapper,
        docker_injector=docker_injector,
        samples_in_each_metric=5,
        sdk_file=None)
    while True:
        try:
            collector.collect_container_events()
        except Exception as e:
            print(e, file=sys.stderr)
            time.sleep(10)