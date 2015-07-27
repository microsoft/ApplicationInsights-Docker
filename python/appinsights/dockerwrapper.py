__author__ = 'galha'
import json
from itertools import islice
from docker import errors
import time


class DockerClientWrapper(object):
    def __init__(self, docker_client):
        if docker_client is None:
            raise Exception('docker_client cannot be None')
        self._client = docker_client

    def get_name(self):
        return self._client.info().get('Name', 'N/A')

    def get_containers(self):
        return self._client.containers()

    def get_stats(self, container, stats_to_bring):
        try:
            return list(map(lambda stat: (time.time(), stat),
                            islice(self._client.stats(container=container, decode=True), 0, stats_to_bring, 1)))
        except errors.APIError:
            return []
