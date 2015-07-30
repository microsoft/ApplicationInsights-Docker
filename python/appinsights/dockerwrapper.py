__author__ = 'galha'
import json
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
        try:
            return list(map(lambda stat: (time.time(), stat),
                            islice(self._client.stats(container=container, decode=True), 0, stats_to_bring, 1)))
        except errors.APIError:
            return []

    def run_command(self, container, cmd):
        exec_id = self._client.exec_create(container, cmd)
        output = self._client.exec_start(exec_id=exec_id)
        return output
