__author__ = 'galha'

import requests
from requests.packages.urllib3.exceptions import ReadTimeoutError, HTTPError
from itertools import islice
from docker import errors
from docker import Client
import time

def get_production_docker_wrapper(base_url):
    return ProductionWrapper(base_url=base_url)


class DockerClientWrapper(object):
    def __init__(self, docker_client):
        assert docker_client is not None, 'docker_client cannot be None'
        self._client = docker_client

    def get_host_name(self):
        return self._client.info().get('Name', 'N/A')

    def get_containers(self):
        return self._client.containers()

    def get_stats(self, container, stats_to_bring):
        stats = []
        try:
            for stat in islice(self._client.stats(container=container, decode=True), 0, stats_to_bring, 1):
                stats.append((time.time(), stat))
        except (errors.APIError, ReadTimeoutError, requests.exceptions.ReadTimeout, HTTPError):
            pass
        return stats

    def run_command(self, container, cmd):
        try:
            exec_id = self._client.exec_create(container, cmd)
            output = self._client.exec_start(exec_id=exec_id)
            return output.decode('utf-8')
        except (errors.APIError, ReadTimeoutError, requests.exceptions.ReadTimeout, HTTPError) as e:
            raise DockerWrapperError(e)

    def get_events(self):
        for event in self._client.events(decode=True):
            event['Id'] = event['id']
            yield event

    def get_inspection(self, container):
        return self._client.inspect_container(container=container)


class ProductionWrapper(object):
    def __init__(self, base_url):
        self._fast_operations_client = DockerClientWrapper(Client(base_url=base_url, timeout=10))
        self._slow_operations_client = DockerClientWrapper(Client(base_url=base_url, timeout=60))

    def get_host_name(self):
        return self._fast_operations_client.get_host_name()

    def get_containers(self):
        return self._fast_operations_client.get_containers()

    def get_stats(self, container, stats_to_bring):
        return self._fast_operations_client.get_stats(container=container, stats_to_bring=stats_to_bring)

    def run_command(self, container, cmd):
        return self._slow_operations_client.run_command(container=container, cmd=cmd)

    def get_events(self):
        return self._fast_operations_client.get_events()

    def get_inspection(self, container):
        return self._slow_operations_client.get_inspection(container=container)


class DockerWrapperError(Exception):
    pass
