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

import requests
from requests.packages.urllib3.exceptions import ReadTimeoutError, HTTPError
from itertools import islice
from docker import errors
from docker import Client
import time

def get_production_docker_wrapper(base_url):
    return ProductionWrapper(base_url=base_url)


class DockerClientWrapper(object):
    """ A wrapper class on the docker client
    """

    def __init__(self, docker_client):
        """ Initializes a new instance of the class.
        :param docker_client:
        :return:
        """
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
            if 'id' in event:
                event['Id'] = event['id']
                yield event

    def get_inspection(self, container):
        try:
            return self._client.inspect_container(container=container)
        except (errors.APIError, ReadTimeoutError, requests.exceptions.ReadTimeout, HTTPError) as e:
            raise DockerWrapperError(e)


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
