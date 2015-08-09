import requests
from requests.packages.urllib3.exceptions import ReadTimeoutError

__author__ = 'galha'

from itertools import islice
from docker import errors
import time


class DockerClientWrapper(object):
    def __init__(self, docker_client):
        assert docker_client is not None, 'docker_client cannot be None'
        self._client = docker_client

    def get_host_name(self):
        return self._client.info().get('Name', 'N/A')

    def get_containers(self):
        return self._client.containers()

    def get_stats(self, container, stats_to_bring):
        list = []
        try:
            for stat in islice(self._client.stats(container=container, decode=True), 0, stats_to_bring, 1):
                list.append((time.time(), stat))
        except (errors.APIError, requests.exceptions.ReadTimeout) as e:
            pass
        return list

    def run_command(self, container, cmd):
        exec_id = self._client.exec_create(container, cmd)
        output = self._client.exec_start(exec_id=exec_id)
        return output.decode('utf-8')

    def get_events(self):
        for event in self._client.events(decode=True):
            event['Id']=event['id']
            yield event

    def get_inspection(self, container):
        return self._client.inspect_container(container=container)